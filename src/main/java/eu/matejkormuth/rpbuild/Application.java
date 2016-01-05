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
import eu.matejkormuth.rpbuild.api.Project;
import eu.matejkormuth.rpbuild.api.Repository;
import eu.matejkormuth.rpbuild.exceptions.TaskException;
import eu.matejkormuth.rpbuild.plugins.DownscalePlugin;
import eu.matejkormuth.rpbuild.plugins.JsonMinifyPlugin;
import eu.matejkormuth.rpbuild.plugins.OptipngPlugin;
import eu.matejkormuth.rpbuild.plugins.PackMcMetaPlugin;
import eu.matejkormuth.rpbuild.tasks.AbstractTask;
import eu.matejkormuth.rpbuild.tasks.update.UpdateRpBuildTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.Manifest;

/**
 * Represents main rpbuild application.
 */
@Slf4j
public class Application {

    /**
     * Application container map.
     */
    private static Map<Class<?>, Object> container = new HashMap<>();

    /**
     * Resolves object from application container by specified object key.
     *
     * @param key key of object to resolve (usually class of the requested type)
     * @param <T> requested type
     * @return instance of requested object
     * @see #store(Class, Object)
     */
    public static <T> T resolve(Class<T> key) {
        return UnsafeUtils.cast(container.get(key));
    }

    /**
     * Stores specified value in application container by specified key.
     *
     * @param key   key
     * @param value value
     * @param <T>   type
     * @see #resolve(Class)
     */
    public static <T> void store(Class<T> key, T value) {
        if (container.containsKey(key)) {
            throw new IllegalStateException("Can't have two values with the same key!");
        }

        container.put(key, value);
    }

    /**
     * Clears application container.
     */
    public static void clearContainer() {
        container.clear();
        container = null;
    }

    private static final String[] MESSAGES = {"Easier Resource Packs!",
            "Building resource packs since 2015!",
            "New era of resource packs!"};

    private SimpleDateFormat timeSpanFormat = new SimpleDateFormat("mm:ss.SSS");

    /**
     * Build steps.
     */
    private LinkedList<AbstractTask> tasks = new LinkedList<>();

    /**
     * Loaded plugins.
     */
    private List<LoadedPlugin> plugins = new ArrayList<>();

    /**
     * Local plugin repository.
     */
    @Getter
    private Repository localRepository;

    /**
     * Application options.
     */
    @Getter
    private Options options;

    public Application(Options options, LinkedList<AbstractTask> tasks) {
        store(Application.class, this);
        store(Options.class, options);

        // Print short application intro.
        printIntro();

        // Set options as field so others can access it.
        this.options = options;

        // Create local folder and local plugin repository.
        createHome();

        // Add bundled plugins.
        addBundledPlugins();

        // Add build steps.
        this.tasks.addAll(tasks);

        // Plan auto update.
        if (options.isAutoUpdate() && Files.exists(getRpBuildPath())) {
            new Thread(() -> {
                log.info("CLI instalation found! Checking for updates...");
                new UpdateRpBuildTask().run();
            }, "AutoUpdate").start();
        }
    }

    /**
     * Print short introduction of rpbuild.
     */
    private void printIntro() {
        printLine();
        log.info("RP Build 2 - {}", MESSAGES[((int) (Math.random() * MESSAGES.length))]);

        String rawVersion = Bootstrap.class.getPackage().getImplementationVersion();
        String version = rawVersion == null ? "null (custom build?)" : rawVersion;

        String describe = "unknown";
        String date = "unknown";

        try {
            URL url = ((URLClassLoader) getClass().getClassLoader()).findResource("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(url.openStream());

            if (manifest.getMainAttributes().getValue("BuildDate") != null) {
                date = manifest.getMainAttributes().getValue("BuildDate");
            }

            if (manifest.getMainAttributes().getValue("BuildGitDescribe") != null) {
                describe = manifest.getMainAttributes().getValue("BuildGitDescribe");
            }
        } catch (Exception e) {
            // Be silent.
        }

        log.info("Version: {} ({}) built on {}", version, describe, date);
        log.info("If you run into troubles: https://github.com/dobrakmato/rpbuild/issues");
        printLine();
    }

    private static String line = "---------------------------------------------------";

    private void printLine() {
        log.info(line);
    }

    private void printLineStrCentered(String str) {
        if (str.length() > line.length()) {
            throw new IllegalArgumentException("Too long string.");
        }

        int strLen = str.length();
        int lLen = line.length();
        int diff = lLen - strLen;
        int index = diff < 0 ? 0 : diff / 2;

        log.info(line.substring(0, index) + str + line.substring(0, index));
    }

    /**
     * Adds all bundled plugins to application.
     */
    private void addBundledPlugins() {
        addPlugin(new LoadedPlugin(getClass().getClassLoader(), new DownscalePlugin()));
        addPlugin(new LoadedPlugin(getClass().getClassLoader(), new JsonMinifyPlugin()));
        addPlugin(new LoadedPlugin(getClass().getClassLoader(), new PackMcMetaPlugin()));

        addPlugin(new LoadedPlugin(getClass().getClassLoader(), new OptipngPlugin()));

        log.info("There are {} bundled plugins in this version of rpbuild.", this.plugins.size());
    }

    /**
     * Creates home directory for rpbuild and repository.
     */
    private void createHome() {
        try {
            Path home = Paths.get(System.getProperty("user.home"), ".rpbuild");
            if (Files.notExists(home)) {
                log.info("Creating rpbuild's home directory...");
                Files.createDirectory(home);
                Files.setAttribute(home, "dos:hidden", Boolean.TRUE);
            }

            Path localRepo = home.resolve("repository");
            if (Files.notExists(localRepo)) {
                log.info("Creating rpbuild's local repository...");
                Files.createDirectory(localRepo);
            }

            // Create instance of local repository.
            this.localRepository = new LocalRepository(localRepo);

        } catch (IOException e) {
            log.error("Can't create home directory!", e);
            log.error("Application may not work!");
        }
    }

    /**
     * Returns rpbuild's home directory.
     *
     * @return home directory of rpbuild
     */
    public Path getHomePath() {
        return Paths.get(System.getProperty("user.home"), ".rpbuild");
    }

    /**
     * Returns rpbuild's jar path.
     *
     * @return rpbuild's jar file
     */
    public Path getRpBuildPath() {
        return Paths.get(System.getProperty("user.home"), ".rpbuild", "rpbuild.jar");
    }

    /**
     * Returns rpbuild's gui jar path.
     *
     * @return rpbuild's gui jar file
     */
    public Path getRpBuildGuiPath() {
        return Paths.get(System.getProperty("user.home"), ".rpbuild", "rpbuild-gui.jar");
    }

    /**
     * Runs the build process.
     */
    public void run() {

        log.info("Starting rpbuild...");
        long startTime = System.currentTimeMillis();

        for (AbstractTask task : tasks) {
            printLineStrCentered(task.getClass().getSimpleName());
            try {
                task.run();
            } catch (TaskException e) {
                log.error("Can't complete build: " + e.getMessage());
                log.error("There is a problem with *this* build! Please check plugins and configuration.", e);
                printBuildError(startTime, e.getMessage());

                // Clean up.
                AbstractTask.clearOutputs();
                Application.clearContainer();
                return;

            } catch (RuntimeException e) {
                log.error("Internal error occurred! Please report this to author!", e);
                printBuildError(startTime, "Internal Error! Please report to author!");

                // Clean up.
                AbstractTask.clearOutputs();
                Application.clearContainer();
                return;
            }
            printLine();
        }

        printLine();
        printBuildSuccess(startTime);

        // Clean up.
        AbstractTask.clearOutputs();
        Application.clearContainer();
    }

    /**
     * Prints success build message.
     *
     * @param startTime build start time
     */
    private void printBuildSuccess(long startTime) {
        printLine();
        Project project = Application.resolve(Project.class);

        if (project != null) {
            log.info("Project {} is built!", project.getName());
            log.info("Artifact: {}", project.getTarget().toAbsolutePath());
        }

        log.info("Memory: {} MB / {} MB",
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                        .freeMemory()) / 1024 / 1024, Runtime.getRuntime()
                        .totalMemory() / 1024 / 1024);
        log.info("Total time: {}",
                this.timeSpanFormat.format(new Date(System.currentTimeMillis() - startTime)));
        printLine();
        log.info("Thanks for using rpbuild 2! :)");
        printLine();
    }

    /**
     * Prints error build message.
     *
     * @param startTime build start time
     */
    private void printBuildError(long startTime, String problem) {
        Project project = Application.resolve(Project.class);

        printLine();
        if (project != null) {
            log.info("Project {} is not built!", project.getName());
        }

        log.info("Problem: {}", problem);
        log.info("More information can be found in logs.");
        log.info("Memory: {} MB / {} MB",
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                        .freeMemory()) / 1024 / 1024, Runtime.getRuntime()
                        .totalMemory() / 1024 / 1024);
        log.info("Total time: {}",
                this.timeSpanFormat.format(new Date(System.currentTimeMillis() - startTime)));
        printLine();
    }

    /**
     * Returns whether the plugin that satisfies specified plugin version declaration is
     * currently loaded and available to use.
     *
     * @param pluginVersion plugin version declaration
     * @return true if we have plugin like that, false otherwise
     */
    public boolean hasPlugin(PluginVersion pluginVersion) {
        return getPlugin(pluginVersion) != null;
    }

    /**
     * Returns plugin that satisfies specified plugin version declaration if available,
     * otherwise returns null.
     *
     * @param pv plugin version declaration
     * @return plugin or null
     */
    public Plugin getPlugin(PluginVersion pv) {
        if (pv.getVersion().equals(PluginVersion.NONE_VERSION)) {
            // Check only for name.
            for (LoadedPlugin plugin : plugins) {
                if (plugin.getName().equals(pv.getName())) {
                    return plugin.getInstance();
                }
            }
        } else {
            // Check for name and version.
            for (LoadedPlugin plugin : plugins) {
                if (plugin.getName().equals(pv.getName()) && plugin.getVersion().equals(pv.getVersion())) {
                    return plugin.getInstance();
                }
            }
        }
        return null;
    }

    /**
     * Adds specified loaded plugin to application.
     *
     * @param loadedPlugin plugin to add
     */
    public void addPlugin(LoadedPlugin loadedPlugin) {
        this.plugins.add(loadedPlugin);
    }
}
