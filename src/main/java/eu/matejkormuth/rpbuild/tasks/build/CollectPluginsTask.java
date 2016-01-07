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

import eu.matejkormuth.rpbuild.Application;
import eu.matejkormuth.rpbuild.api.*;
import eu.matejkormuth.rpbuild.exceptions.TaskException;
import eu.matejkormuth.rpbuild.tasks.AbstractTask;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Collects plugin declaration from build configuration and then collects
 * plugins from repositories and loads them
 */
@Slf4j
public class CollectPluginsTask extends AbstractTask {

    @Override
    public void run() {
        Project project = Application.resolve(Project.class);
        Application application = Application.resolve(Application.class);
        Repository localRepository = application.getLocalRepository();

        // Collect all different plugin versions from config.
        Set<PluginVersion> plugins = new HashSet<>();
        collectTo(project.getBuild(), plugins);

        log.info("We will need {} different plugins for this build.", plugins.size());

        // Collect versions from repositories.
        for (PluginVersion pluginVersion : plugins) {

            // Check if it's not bundled.
            if (application.hasPlugin(pluginVersion)) {
                log.info("Plugin {} is bundled.", pluginVersion.getName());
                // Do nothing at all. lol.
                continue;
            }

            // Check local repository.
            if (localRepository.hasPlugin(pluginVersion)) {
                // If it's at least 24 hours old. Check for new version.
                if (localRepository.shouldUpdate(pluginVersion)) {

                    boolean success = downloadPluginIfChanged(project, localRepository, pluginVersion);

                    if (!success) {
                        log.warn("Can't check for updates for plugin {} from any available repository!", pluginVersion);
                    }
                }

                localRepository.load(pluginVersion);
                log.info("Plugin {} was loaded from local repo.", pluginVersion.getName());
                continue;
            }

            log.info("Plugin {} not found! Obtaining from remote repositories...", pluginVersion);

            // Try to download it from each repository.
            boolean success = downloadPlugin(project, localRepository, pluginVersion);

            // If we didn't get plugin from any repo, fail.
            if (!success) {
                log.error("Can't download plugin {} from any available repository!", pluginVersion);
                throw new TaskException("Unresolvable dependency: " + pluginVersion.toString());
            }
        }
    }

    private boolean downloadPluginIfChanged(Project project, Repository localRepository, PluginVersion pluginVersion) {
        boolean success = false;
        for (Repository repository : project.getRepositories()) {
            if (repository.hasPlugin(pluginVersion)) {
                log.debug("Checking last modified at repo {}...", repository.getName());
                FileTime remoteModTime = repository.getLastModified(pluginVersion);
                FileTime localModTime = localRepository.getLastModified(pluginVersion);

                log.debug("Remote mod: {}", remoteModTime);
                log.debug("Local mod: {}", localModTime);

                if (remoteModTime.compareTo(localModTime) > 0) {
                    log.debug("Updating from this repo...");
                    // Download to local repository.
                    success = repository.downloadToLocal(pluginVersion);
                    // If we succeeded at downloading, load it, otherwise try another repository.
                    if (success) {
                        // Load from local repository.
                        localRepository.load(pluginVersion);
                        break;
                    }
                }
            }
        }
        return success;
    }

    private boolean downloadPlugin(Project project, Repository localRepository, PluginVersion pluginVersion) {
        boolean success = false;
        for (Repository repository : project.getRepositories()) {
            log.info("Checking repository: {}", repository.getName());

            if (repository.hasPlugin(pluginVersion)) {
                // Download to local repository.
                success = repository.downloadToLocal(pluginVersion);
                // If we succeeded at downloading, load it, otherwise try another repository.
                if (success) {
                    // Load from local repository.
                    localRepository.load(pluginVersion);
                    break;
                }
            }
        }
        return success;
    }

    /**
     * Recursively collects all plugin declarations in build configuration.
     *
     * @param section section to collect from
     * @param to      list to collect plugin declarations to
     */
    private void collectTo(BuildSection section, Set<PluginVersion> to) {
        // Collect plugins from current section.
        for (PluginConfiguration configuration : section.getPlugins()) {
            to.add(new PluginVersion(configuration.getPluginName(), configuration.getPluginVersion()));
        }

        // Collect from all subsections.
        for (BuildSection section1 : section.getChildren()) {
            collectTo(section1, to);
        }
    }

}
