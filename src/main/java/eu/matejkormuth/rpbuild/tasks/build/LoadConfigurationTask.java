/**
 * rpBuild 2 - Improved build system for Minecraft resource packs.
 * Copyright (c) 2015 - 2016, Matej Kormuth <http://www.github.com/dobrakmato>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * "Minecraft" is a trademark of Mojang AB
 */
package eu.matejkormuth.rpbuild.tasks.build;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import eu.matejkormuth.rpbuild.Application;
import eu.matejkormuth.rpbuild.api.*;
import eu.matejkormuth.rpbuild.exceptions.InvalidConfigurationException;
import eu.matejkormuth.rpbuild.tasks.AbstractTask;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads configuration values from build configuration files.
 */
@Slf4j
public class LoadConfigurationTask extends AbstractTask {

    public void run() {
        Application application = Application.resolve(Application.class);

        log.info("Using build descriptor {}.", application.getOptions().getFile());

        if (!Files.exists(Paths.get(application.getOptions().getFile()))) {
            throw new InvalidConfigurationException("File does not eixsts!");
        }

        // Load and parse configuration.
        Config config = ConfigFactory.parseFile(new File(application.getOptions().getFile())).resolve();

        Project project = new Project();

        log.info("Loading build descriptor...");

        // Load config values.
        loadProjectTo(config, project);

        log.info("############ CURRENTLY BUILDING ################");
        log.info("# Project Name: {}", project.getName());
        log.info("# Encoding: {}", project.getEncoding().displayName());
        log.info("# Project source: {}", project.getSource());
        log.info("# Project target: {}", project.getTarget());
        log.info("################################################");

        // Store project in application container.
        Application.store(Project.class, project);
    }

    /**
     * Loads project node to specified Project object.
     *
     * @param config  config
     * @param project project
     */
    private void loadProjectTo(Config config, Project project) {
        if (!config.hasPath("project")) {
            throw new InvalidConfigurationException("Configuration must contain root 'project' node!");
        }

        Config c = config.getConfig("project");

        if (c.hasPath("name")) {
            project.setName(c.getString("name"));
        } else {
            log.warn("Your project does not have a name!");
            project.setName("Resource Pack");
        }

        if (c.hasPath("encoding")) {
            project.setEncoding(Charset.forName(c.getString("encoding")));
        } else {
            project.setEncoding(Charset.forName("UTF-8"));
        }

        if (!c.hasPath("source") || !c.hasPath("target")) {
            throw new InvalidConfigurationException("Configuration must contain both source and target values!");
        }

        // Use absolute path for more safety.
        project.setSource(Paths.get(c.getString("source")).toAbsolutePath());
        project.setTarget(Paths.get(c.getString("target")).toAbsolutePath());

        Git git = new Git();
        if (c.hasPath("git")) {
            loadGitTo(c, git);
        }
        project.setGit(git);

        Compress compress = new Compress();
        if (c.hasPath("compress")) {
            loadCompressTo(c, compress);
        }
        project.setCompress(compress);

        RepositoryList repositoryList = new RepositoryList();
        if (c.hasPath("repositories")) {
            loadRepositoriesTo(c, repositoryList);
        }
        project.setRepositories(repositoryList);

        if (c.hasPath("properties")) {
            Config properties = c.getConfig("properties");
            project.setProperties(properties);
        }

        if (!c.hasPath("build")) {
            throw new InvalidConfigurationException("Configuration must contain build value!");
        }

        BuildSection build = new BuildSection();
        loadBuildTo(c, build);
        project.setBuild(build);

        // Fix .git ignore.
        if (git.isIgnoreGitFolders()) {
            recurAddExclude(build, "**/.git");
            recurAddExclude(build, "**/.git/**");
        }
    }

    private void recurAddExclude(BuildSection build, String s) {
        build.getExclude().add(FileSystems.getDefault().getPathMatcher("glob:" + s));
        build.getChildren().stream().forEach((section) -> recurAddExclude(section, s));
    }

    /**
     * Loads values from specified config to main build section.
     *
     * @param config config
     * @param build  main build section
     */
    private void loadBuildTo(Config config, BuildSection build) {
        Config c = config.getConfig("build");

        build.setName("#global#");
        build.setParent(null);

        loadExcludesTo(c, build);

        loadPluginsTo(c, build);
        loadBuildSubSections(c, config.getObject("build"), build);
    }

    private void loadBuildSubSections(Config config, ConfigObject configConfig, BuildSection build) {
        List<BuildSection> children = new ArrayList<>();

        for (String subSectionName : configConfig.keySet()) {
            // Don't load reserved names as subsections.
            if (subSectionName.equalsIgnoreCase("exclude") || subSectionName.equalsIgnoreCase("plugins")) {
                continue;
            }

            Config c = config.getConfig(subSectionName);
            ConfigObject cc = config.getObject(subSectionName);

            BuildSection section = new BuildSection();
            section.setName(subSectionName);
            section.setParent(build);

            loadBuildSectionTo(c, cc, section);
            children.add(section);
        }

        build.setChildren(children);
    }

    private void loadBuildSectionTo(Config c, ConfigObject cc, BuildSection section) {
        loadExcludesTo(c, section);
        loadPluginsTo(c, section);
        loadBuildSubSections(c, cc, section);
    }

    private void loadPluginsTo(Config config, BuildSection build) {
        if (config.hasPath("plugins")) {
            ConfigObject c = config.getObject("plugins");
            Config cc = config.getConfig("plugins");

            List<PluginConfiguration> configurations = new ArrayList<>(4);

            for (String pluginExp : c.keySet()) {
                PluginConfiguration pluginConfiguration = new PluginConfiguration();
                loadPluginConfigurationTo(cc, pluginExp, pluginConfiguration);
                configurations.add(pluginConfiguration);
            }

            build.setPlugins(configurations);
        } else {
            build.setPlugins(new ArrayList<>(0));
        }
    }

    private void loadPluginConfigurationTo(Config config, String pluginExp, PluginConfiguration pluginConfiguration) {
        String name = pluginExp.contains(":") ? pluginExp.substring(0, pluginExp.indexOf(':')) : pluginExp;
        String version = pluginExp.contains(":") ? pluginExp.substring(pluginExp.indexOf(':') + 1) : PluginVersion.NONE_VERSION;

        pluginConfiguration.setPluginName(name);
        pluginConfiguration.setPluginVersion(version);
        pluginConfiguration.setConfiguration(config.getConfig(pluginExp));
    }

    private void loadExcludesTo(Config c, BuildSection build) {
        if (c.hasPath("exclude")) {
            List<String> excludes = c.getStringList("exclude");
            List<PathMatcher> filters = new ArrayList<>(excludes.size());

            filters.addAll(excludes
                            .stream()
                            .map(string -> FileSystems.getDefault().getPathMatcher("glob:" + string))
                            .collect(Collectors.toList())
            );
            build.setExclude(filters);
        } else {
            build.setExclude(new ArrayList<>(0));
        }
    }

    private void loadRepositoriesTo(Config config, RepositoryList repositoryList) {
        ConfigObject c = config.getObject("repositories");
        Config cc = config.getConfig("repositories");

        for (String repositoryId : c.keySet()) {
            Repository repository = new Repository();
            loadRepositoryTo(cc, repository, repositoryId);
            repositoryList.add(repository);
        }
    }

    private void loadRepositoryTo(Config config, Repository repository, String repositoryId) {
        Config c = config.getConfig(repositoryId);

        repository.setName(repositoryId);

        if (!c.hasPath("url")) {
            throw new InvalidConfigurationException("Repository '" + repositoryId + "' must have url value!");
        }

        try {
            repository.setUrl(new URL(c.getString("url")));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadCompressTo(Config config, Compress compress) {
        Config c = config.getConfig("compress");

        if (c.hasPath("level")) {
            compress.setLevel(c.getInt("level"));
        } else {
            log.info("Compression level not specified. Using level 9 (highest).");
            compress.setLevel(9);
        }
    }

    private void loadGitTo(Config config, Git git) {
        Config c = config.getConfig("git");

        if (c.hasPath("branch")) {
            git.setBranch(c.getString("branch"));
        } else {
            log.warn("Branch not specified! Using 'master' branch.");
            git.setBranch("master");
        }

        if (c.hasPath("ignoreGitFolders")) {
            git.setIgnoreGitFolders(c.getBoolean("ignoreGitFolders"));
        } else {
            git.setIgnoreGitFolders(true);
        }

        if (c.hasPath("pull")) {
            git.setPull(c.getBoolean("pull"));
        } else {
            git.setPull(false);
        }

        if (c.hasPath("url")) {
            git.setUrl(c.getString("url"));
        } else {
            log.warn("Repository URL not specified! If you don't have .git folder in source Git integration will don't work.");
        }
    }
}
