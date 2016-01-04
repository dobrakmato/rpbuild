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
package eu.matejkormuth.rpbuild.tasks.update;

import eu.matejkormuth.rpbuild.Application;
import eu.matejkormuth.rpbuild.exceptions.TaskException;
import eu.matejkormuth.rpbuild.tasks.AbstractTask;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Updates both rpbuild CLI and rpbuild gui.
 */
@Slf4j
public class UpdateRpBuildTask extends AbstractTask {

    // Download URLs.
    private static final String RPBUILD_ROOT = "https://matejkormuth.eu/rpbuild/";
    private static final String RPBUILD_CLI = RPBUILD_ROOT + "rpbuild.jar";
    private static final String RPBUILD_GUI = RPBUILD_ROOT + "rpbuild-gui.jar";

    @Override
    public void run() {
        Application application = Application.resolve(Application.class);

        // Check if files exists (if we can update something?).
        if (Files.notExists(application.getRpBuildPath()) || Files.notExists(application.getRpBuildGuiPath())) {
            log.error("Can't update! Nothing to update!");
            log.info("Did you run `rpbuild install` first?");
            throw new TaskException("Nothing is installed, nothing to update!");
        }

        log.info("Checking for updates...");

        Boolean forceRaw = AbstractTask.getOutput("forceUpdate");
        boolean force = forceRaw != null && forceRaw;

        try {
            updateCli(application, force);
        } catch (IOException e) {
            log.error("Cannot update CLI!", e);
        }

        try {
            updateGui(application, force);
        } catch (IOException e) {
            log.error("Cannot update GUI!", e);
        }
    }

    private void updateCli(Application application, boolean force) throws IOException {
        FileTime localMod = Files.getLastModifiedTime(application.getRpBuildPath());
        FileTime remoteMod = getRemoteMod(new URL(RPBUILD_CLI + "?random=" + Math.random()));

        if (remoteMod.compareTo(localMod) > 0 || force) {
            log.info("Updating CLI...");

            String downloadPath = application.getRpBuildPath().toAbsolutePath().toString().replace("rpbuild.jar", "rpbuild_new.jar");

            downloadTo(new URL(RPBUILD_CLI + "?random=" + Math.random()), Paths.get(downloadPath));
            scheduleFileReplace(application.getRpBuildPath().toAbsolutePath().toString(),
                    application.getRpBuildPath().toAbsolutePath().toString().replace("rpbuild.jar", "rpbuild.bak"));
            scheduleFileReplace(downloadPath, application.getRpBuildPath().toAbsolutePath().toString());
        }
    }

    private void updateGui(Application application, boolean force) throws IOException {
        FileTime localMod = Files.getLastModifiedTime(application.getRpBuildGuiPath());
        FileTime remoteMod = getRemoteMod(new URL(RPBUILD_GUI + "?random=" + Math.random()));

        if (remoteMod.compareTo(localMod) > 0 || force) {
            log.info("Updating GUI...");

            String downloadPath = application.getRpBuildGuiPath().toAbsolutePath().toString().replace("rpbuild-gui.jar", "rpbuild-gui_new.jar");

            downloadTo(new URL(RPBUILD_GUI + "?random=" + Math.random()), Paths.get(downloadPath));
            scheduleFileReplace(application.getRpBuildGuiPath().toAbsolutePath().toString(),
                    application.getRpBuildGuiPath().toAbsolutePath().toString().replace("rpbuild-gui.jar", "rpbuild-gui.bak"));
            scheduleFileReplace(downloadPath, application.getRpBuildGuiPath().toAbsolutePath().toString());
        }
    }

    private static Map<String, String> replaces = new LinkedHashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook-Update-ReplaceFiles") {
            @Override
            public void run() {
                if (replaces.isEmpty()) {
                    return;
                }

                // Create and run script.
                if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).startsWith("win")) {
                    // Create bat script.
                    StringBuilder script = new StringBuilder();

                    /**
                     * Solution for sleep(int seconds); Thank you Windows!
                     */
                    script.append("ping -n 3 127.0.0.1 > nul\r\n"); // Waits 2 seconds.

                    for (Map.Entry<String, String> entry : replaces.entrySet()) {
                        script.append("move /Y \"").append(entry.getKey()).append("\" \"").append(entry.getValue()).append("\"\r\n");
                    }
                    script.append("exit\r\n");

                    try {
                        Files.write(Paths.get("update.bat"), script.toString().getBytes("UTF-8"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        new ProcessBuilder("cmd", "/c", "start", "update.bat").directory(Paths.get(".").toAbsolutePath().toFile()).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    // Create linux script.
                    StringBuilder script = new StringBuilder();

                    script.append("sleep 2\n");

                    for (Map.Entry<String, String> entry : replaces.entrySet()) {
                        script.append("mv \"").append(entry.getKey()).append("\" \"").append(entry.getValue()).append("\"\n");
                    }

                    try {
                        Files.write(Paths.get("update.sh"), script.toString().getBytes("UTF-8"));
                        Paths.get("update.sh").toFile().setExecutable(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        Runtime.getRuntime().exec("sh update.sh");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // !Quickly exit JVM!
            }
        });
    }

    private static void scheduleFileReplace(String from, String to) {
        replaces.put(from, to);
    }

    private static FileTime getRemoteMod(URL url) {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-US) AppleWebKit/532.5 (KHTML, like Gecko) Chrome/4.0.249.0 Safari/532.5");
            con.setRequestMethod("HEAD");

            for (Map.Entry<String, List<String>> entry : con.getHeaderFields().entrySet()) {
                log.debug("Header: {} -> {}", entry.getKey(), String.join(", ", entry.getValue()));
            }

            // Huh, I'm ninja!
            return FileTime.fromMillis(con.getLastModified());
        } catch (Exception e) {
            log.warn("Can't HEAD remote server.", e);
            throw new TaskException("Can't check remote version!", e);
        }
    }

    private static void downloadTo(URL from, Path to) {
        try {
            log.info("Downloading {}...", from);
            HttpURLConnection con = (HttpURLConnection) from.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-US) AppleWebKit/532.5 (KHTML, like Gecko) Chrome/4.0.249.0 Safari/532.5");

            ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
            FileOutputStream fos = new FileOutputStream(to.toFile());
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            log.error("Can't complete download!", e);
            throw new TaskException("Cannot complete download!", e);
        }
    }

}
