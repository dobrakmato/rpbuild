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
import eu.matejkormuth.rpbuild.Application;
import eu.matejkormuth.rpbuild.Options;
import eu.matejkormuth.rpbuild.annotations.Optional;
import eu.matejkormuth.rpbuild.api.*;
import eu.matejkormuth.rpbuild.exceptions.TaskException;
import eu.matejkormuth.rpbuild.nio.ListFileExcludesVisitor;
import eu.matejkormuth.rpbuild.tasks.AbstractTask;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CompileAndGenerateTask extends AbstractTask {

    @Override
    public void run() {
        // Get project currently built.
        Project project = Application.resolve(Project.class);

        log.info("Starting build...");

        // Recursively generate and build.
        recurBuild(project.getBuild());
    }

    private void recurBuild(BuildSection section) {
        // Find out the path of current working directory.
        Path currentPath = section.getAbsolutePath();

        log.info("Recursively building section {}...", section.getName());
        // !We are visiting tree depth-first.

        // Check if this section exists on filesystem.
        if (!Files.exists(currentPath)) {
            log.warn("Specified section {} does not exists in filesystem at {}! Skipping all subsections.",
                    section.getName(), section.getAbsolutePath());
            return;
        }

        /*
         * First we traverse to the most specific declaration. We run plugins
         * first on specific folders, then on parent folders so the plugins specified
         * in parent directory will also run on files in subdirectories BUT after the
         * subdirectory specific plugins are run.
         */
        section.getChildren().forEach(this::recurBuild);

        // Run GENERATE_BEFORE_LIST plugins.
        runPlugins(section, PluginType.GENERATE_BEFORE_LIST, null, currentPath);

        // List files.
        ListFileExcludesVisitor visitor = new ListFileExcludesVisitor(section.getExclude());
        try {
            Files.walkFileTree(currentPath, visitor);
        } catch (IOException e) {
            log.error("Can't walk fire tree!", e);
            throw new TaskException("Can't walk fire tree!", e);
        }

        // Run GENERATE_AFTER_LIST plugins.
        runPlugins(section, PluginType.GENERATE_AFTER_LIST, visitor.getFiles(), currentPath);

        // Run TRANSFORM_FILES plugins.
        runPlugins(section, PluginType.TRANSFORM_FILES, visitor.getFiles(), currentPath);
    }

    private void runPlugins(BuildSection section, PluginType type, @Optional List<Path> files, Path currentPath) {
        Application application = Application.resolve(Application.class);

        for (PluginConfiguration pConf : section.getPlugins()) {
            Plugin plugin = application.getPlugin(pConf.getPluginVersionDeclaration());

            if (plugin == null) {
                throw new RuntimeException("Plugin was not loaded! Plugin: " + pConf.toString());
            }

            if (pConf.getConfiguration().hasPath("disable") && pConf.getConfiguration().getBoolean("disable")) {
                log.info("Execution of plugin {} is disabled.", plugin.getName());
                continue;
            }

            // Initialize plugin if needed.
            if (!plugin.isInitialized()) {
                try {
                    plugin.initialize();
                    plugin.setProfiler(getProfiler().createChild(plugin.getName()));
                    plugin.setInitialized(true);
                } catch (Exception e) {
                    log.error("Can't initialize plugin {}!", plugin.getName());
                    logPluginException(plugin, e);
                }
            }

            // Run only plugins with right type.
            if (plugin.getType() == type) {
                switch (type) {
                    case GENERATE_BEFORE_LIST:
                        runGenerateBefore(currentPath, plugin, pConf.getConfiguration());
                        break;
                    case GENERATE_AFTER_LIST:
                        runGenerateAfter(files, currentPath, plugin, pConf.getConfiguration());
                        break;
                    case TRANSFORM_FILES:
                        runTransform(files, plugin, pConf.getConfiguration());
                        break;
                    case TRANSFORM_ALL_FILES:
                        runTransformAll(files, plugin, pConf.getConfiguration());
                        break;
                }
            }

        }
    }

    private void runTransformAll(List<Path> files, Plugin plugin, Config configuration) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + plugin.getGlobPattern());

        log.info("Transforming files ({}) with plugin {}...", plugin.getGlobPattern(), plugin.getName());

        int fileCount = 0;
        List<OpenedFile> openedFiles = new ArrayList<>(files.size());

        for (Path file : files) {
            if (!matcher.matches(file)) {
                continue;
            }

            // Some plugins only use file path, so we better lazy load content
            // to save resources.
            OpenedFile openedFile = OpenedFile.lazyLoaded(file);
            openedFiles.add(openedFile);
        }

        // Process all files in one call.
        try {
            plugin.getProfiler().begin();
            plugin.transformAll(configuration, openedFiles);
            plugin.getProfiler().end();
            log.info("{} transformed {} files!", plugin.getName(), openedFiles.size());
            fileCount++;
        } catch (Exception e) {
            logPluginException(plugin, e);
        }

        // Write changed files.
        for (OpenedFile openedFile : openedFiles) {
            // Write only if file content has been changed.
            if (openedFile.isDirty()) {
                try {
                    Files.write(openedFile.getAbsolutePath(), openedFile.getData());
                } catch (IOException e) {
                    log.error("Can't read/write bytes from file " + openedFile.getAbsolutePath().toString(), e);
                    throw new TaskException("Can't read/write file " + openedFile.getAbsolutePath().toString(), e);
                }
            }
        }

        log.info("Transformed {} files.", fileCount);
    }

    private void runTransform(List<Path> files, Plugin plugin, Config configuration) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + plugin.getGlobPattern());

        log.info("Transforming files ({}) with plugin {}...", plugin.getGlobPattern(), plugin.getName());

        int fileCount = 0;

        for (Path file : files) {
            if (!matcher.matches(file)) {
                continue;
            }

            // Open file, transform content, save file.
            try {
                // Some plugins only use file path, so we better lazy load content
                // to save resources.
                OpenedFile openedFile = OpenedFile.lazyLoaded(file);

                try {
                    plugin.getProfiler().begin();
                    plugin.transform(configuration, openedFile);
                    plugin.getProfiler().end();
                    log.info("{} transformed: {}", plugin.getName(), file);
                    fileCount++;
                } catch (Exception e) {
                    logPluginException(plugin, e);
                }

                // Write only if file content has been changed.
                if (openedFile.isDirty()) {
                    Files.write(file, openedFile.getData());
                }
            } catch (IOException e) {
                log.error("Can't read/write bytes from file " + file.toString(), e);
                throw new TaskException("Can't read/write file " + file.toString(), e);
            }
        }

        log.info("Transformed {} files.", fileCount);
    }

    private void logPluginException(Plugin plugin, Exception e) {
        // Perform intelligent exception logging to not spam logs with lots of stack traces.
        plugin.incrementExceptionCount();

        if (plugin.getExceptionCount() == 10) {
            log.info("Plugin {} generated too many exceptions. Further stack traces will be hidden.", plugin.getName());
            log.info("If you want to see all stack traces, please run rpbuild with --verbose (-v) option.");
        }

        if (plugin.getExceptionCount() < 10 || Application.resolve(Options.class).isVerbose()) {
            log.error("Plugin " + plugin.getName() + " generated an exception: ", e);
        } else {
            log.error("Plugin " + plugin.getName() + " generated an exception: ", e.getMessage());
        }
    }

    private void runGenerateAfter(List<Path> files, Path currentPath, Plugin plugin, Config configuration) {
        log.info("Generating files with plugin {}...", plugin.getName());

        int filesCount = 0;

        // Generate files.
        List<OpenedFile> generatedFiles = new ArrayList<>();
        try {
            plugin.getProfiler().begin();
            generatedFiles.addAll(plugin.generate(configuration, files));
            plugin.getProfiler().end();
        } catch (Exception e) {
            logPluginException(plugin, e);
        }

        for (OpenedFile openedFile : generatedFiles) {
            filesCount++;
            log.info(" {} generated: {}", plugin.getName(), openedFile.getName());
            processGeneratedFile(files, currentPath, openedFile);
        }

        log.info("Generated {} file(s).", files);
    }

    private void processGeneratedFile(List<Path> files, Path currentPath, OpenedFile openedFile) {
        // Update absolute path.
        openedFile.setAbsolutePath(currentPath.resolve(openedFile.getName()));

        // Save.
        try {
            Files.write(openedFile.getAbsolutePath(), openedFile.getData());
        } catch (IOException e) {
            throw new TaskException("Can't save generated file!", e);
        }

        // Add to list of all files.
        files.add(openedFile.getAbsolutePath());

        // Discard file content, because we will never use it again.
        openedFile.setData(null);
    }

    /**
     * Runs GENERATE_BEFORE plugin task.
     *
     * @param currentPath   current path
     * @param plugin        plugin to use
     * @param configuration configuration
     */
    private void runGenerateBefore(Path currentPath, Plugin plugin, Config configuration) {
        if (currentPath == null) {
            throw new NullPointerException("currentPath can't be null!");
        }

        log.info("Generating files with plugin {}...", plugin.getName());

        int files = 0;

        List<OpenedFile> generatedFiles = new ArrayList<>();
        try {
            plugin.getProfiler().begin();
            generatedFiles.addAll(plugin.generate(configuration));
            plugin.getProfiler().end();
        } catch (Exception e) {
            logPluginException(plugin, e);
        }

        for (OpenedFile openedFile : generatedFiles) {

            if (openedFile == null) {
                log.warn("Plugin {} returned null as generated file! Please contact it's author about this!", plugin.getName());
                continue;
            }

            // Update absolute path.
            openedFile.setAbsolutePath(currentPath.resolve(openedFile.getName()));

            log.info(" {} generated: {}", plugin.getName(), openedFile.getName());

            // Save.
            try {
                files++;
                Files.write(openedFile.getAbsolutePath(), openedFile.getData());
            } catch (IOException e) {
                throw new TaskException("Can't save generated file!", e);
            }

            // Discard file content, because we will never use it again.
            openedFile.setData(null);
        }

        log.info("Generated {} file(s).", files);
    }

}
