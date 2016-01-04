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
package eu.matejkormuth.rpbuild;

import eu.matejkormuth.rpbuild.api.Plugin;
import eu.matejkormuth.rpbuild.api.PluginVersion;
import eu.matejkormuth.rpbuild.api.Repository;
import eu.matejkormuth.rpbuild.exceptions.InvalidPluginException;
import eu.matejkormuth.rpbuild.exceptions.TaskException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Properties;

/**
 * Special case of repository, which is store on local filesystem instead of
 * remote server.
 */
@Slf4j
public class LocalRepository extends Repository {

    /**
     * Root of repository.
     */
    private final Path root;

    public LocalRepository(Path root) {
        this.root = root;
    }

    @Override
    public URL getUrl() {
        try {
            return root.toUri().toURL();
        } catch (MalformedURLException e) {
            log.error("Can't convert local repository path to URL!");
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the root of repository.
     *
     * @return root of repository
     */
    public Path getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return "Local Repository";
    }

    @Override
    public boolean hasPlugin(PluginVersion pluginVersion) {
        return Files.exists(root.resolve(makePath(pluginVersion)));
    }

    @Override
    public boolean downloadToLocal(PluginVersion pluginVersion) {
        throw new UnsupportedOperationException("Cannot download from local to local!");
    }

    @Override
    public void load(PluginVersion pluginVersion) {
        Application application = Application.resolve(Application.class);

        // Load to class path and inject to application container.
        try {
            URL jarUrl = root.resolve(makePath(pluginVersion)).toUri().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl});

            Properties properties = new Properties();
            properties.load(classLoader.getResourceAsStream("plugin.properties"));

            String pluginClass = properties.getProperty("main");

            Class<?> possiblePluginClass = classLoader.loadClass(pluginClass);
            if (Plugin.class.isAssignableFrom(possiblePluginClass)) {
                Constructor<?> ctr = possiblePluginClass.getConstructor();
                Object possiblePlugin = ctr.newInstance();

                // Create loaded plugin object.
                LoadedPlugin loadedPlugin = new LoadedPlugin();
                loadedPlugin.setClassLoader(classLoader);
                loadedPlugin.setInstance((Plugin) possiblePlugin);

                // Add to application container.
                application.addPlugin(loadedPlugin);

            } else {
                throw new InvalidPluginException("Plugin " + possiblePluginClass.getName()
                        + " does not extend " + Plugin.class.getName());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new InvalidPluginException(e);
        }
    }

    /**
     * Saves JAR as specified plugin with name and version from specified URL.
     *
     * @param pluginVersion plugin name (id) and version
     * @param urlFrom       url of plugin jar
     * @return true if succeeded, false otherwise
     */
    public boolean save(PluginVersion pluginVersion, URL urlFrom) {
        try {
            log.info("Downloading {}...", urlFrom);

            HttpURLConnection con = (HttpURLConnection) urlFrom.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-US) AppleWebKit/532.5 (KHTML, like Gecko) Chrome/4.0.249.0 Safari/532.5");

            ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
            FileOutputStream fos = new FileOutputStream(root.resolve(makePath(pluginVersion)).toFile());
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            return true;
        } catch (IOException e) {
            log.error("Can't complete plugin download!", e);
            return false;
        }
    }

    @Override
    public FileTime getLastModified(PluginVersion pluginVersion) {
        try {
            return Files.getLastModifiedTime(root.resolve(makePath(pluginVersion)));
        } catch (IOException e) {
            throw new TaskException(e);
        }
    }

    @Override
    public boolean shouldUpdate(PluginVersion pluginVersion) {
        return getLastModified(pluginVersion).toMillis() + 1000 * 60 * 60 * 24 < System.currentTimeMillis();
    }
}
