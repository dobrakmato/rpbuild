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
package eu.matejkormuth.rpbuild.api;

import eu.matejkormuth.rpbuild.Application;
import eu.matejkormuth.rpbuild.LocalRepository;
import eu.matejkormuth.rpbuild.exceptions.TaskException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.attribute.FileTime;

/**
 * Represents remote repository that contains plugins.
 */
@Slf4j
@Data
public class Repository {

    /**
     * Name / ID of repository.
     */
    private String name;

    /**
     * URL to root of repository.
     */
    private URL url;

    /**
     * Returns whether this repository can be used to download specified plugin at specified version.
     *
     * @param pluginVersion plugin version and name (id)
     * @return true if this repository can provide specified plugin, false otherwise
     */
    public boolean hasPlugin(PluginVersion pluginVersion) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url.toString() + "/" + makePath(pluginVersion))
                    .openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            log.warn("Can't HEAD remote server.", e);
            return false;
        }
    }

    /**
     * Returns the date of last modification on this repository.
     *
     * @param pluginVersion plugin name (id) and version
     * @return date of modification
     */
    public FileTime getLastModified(PluginVersion pluginVersion) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url.toString() + "/" + makePath(pluginVersion))
                    .openConnection();
            con.setRequestMethod("HEAD");
            // Huh, I'm ninja!
            return FileTime.fromMillis(con.getLastModified());
        } catch (Exception e) {
            log.warn("Can't HEAD remote server.", e);
            throw new TaskException(e);
        }
    }

    /**
     * Creates relative path in repository to specified plugin file.
     *
     * @param pv plugin name (id) and version
     * @return path to plugin file relative to repository root
     */
    public String makePath(PluginVersion pv) {
        if (pv.getVersion().equals(PluginVersion.NONE_VERSION)) {
            return pv.getName() + ".jar";
        } else {
            return pv.getName() + "/" + pv.getVersion() + "/" + pv.getName() + ".jar";
        }
    }

    /**
     * Download specified plugin from this repository to local repository.
     *
     * @param pluginVersion plugin name (id) and version
     * @return true if download succeeded, false otherwise
     */
    public boolean downloadToLocal(PluginVersion pluginVersion) {
        LocalRepository localRepository = (LocalRepository) Application.resolve(Application.class).getLocalRepository();
        String path = makePath(pluginVersion);

        // Download to local repository.
        try {
            return localRepository.save(pluginVersion, new URL(url.toString() + "/" + path + "?random=" + Math.random()));
        } catch (MalformedURLException e) {
            log.error("Can't make URL!", e);
            return false;
        }
    }

    /**
     * Loads plugin's JAR into application.
     *
     * @param pluginVersion plugin name (id) and version
     */
    public void load(PluginVersion pluginVersion) {
        throw new UnsupportedOperationException("Cannot load JARs from remote repositories!");
    }

    public boolean shouldUpdate(PluginVersion pluginVersion) {
        return false;
    }
}
