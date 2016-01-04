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
package eu.matejkormuth.rpbuild.tasks.install;

import eu.matejkormuth.rpbuild.Application;
import eu.matejkormuth.rpbuild.exceptions.TaskException;
import eu.matejkormuth.rpbuild.tasks.AbstractTask;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Creates symbolic links to allow CLI usage.
 */
@Slf4j
public class CreateLinksTask extends AbstractTask {

    @Override
    public void run() {
        Application application = Application.resolve(Application.class);

        if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).startsWith("win")) {
            try {
                log.info("Installing CLI on windows...");
                createWindowsLinks(application);
            } catch (IOException | InterruptedException e) {
                throw new TaskException(e);
            }
        } else {
            // Suppose linux.
            try {
                log.info("Installing CLI on linux.");
                createLinuxLinks(application);
            } catch (IOException e) {
                throw new TaskException(e);
            }
        }
    }

    private Path createHomeBin(Application application) {
        Path homeBin = application.getHomePath().resolve("bin");
        try {
            if (Files.notExists(homeBin)) {
                log.info("Crating bin directory...");
                Files.createDirectory(homeBin);
            }
        } catch (IOException e) {
            log.error("Cannot create directory.", e);
        }
        return homeBin;
    }

    private void createLinuxLinks(Application application) throws IOException {
        // Create rpbuild_home/bin.
        Path bin = createHomeBin(application);
        try {
            createScriptsLinux(bin);
        } catch (IOException e) {
            throw new TaskException(e);
        }

        // Make links to /usr/bin/rpbuild
        Path link = Paths.get("/usr/bin/rpbuild");
        log.info("Creating symlink {} -> {}", bin.resolve("rpbuild.sh").toAbsolutePath(), link);
        if (!Files.isSymbolicLink(link)) {
            Files.createSymbolicLink(link, bin.resolve("rpbuild.sh"));
        }
        link.toFile().setExecutable(true);

        // Make links to /bin/rpbuild-gui
        Path link2 = Paths.get("/usr/bin/rpbuild-gui");
        log.info("Creating symlink {} -> {}", bin.resolve("rpbuild-gui.sh").toAbsolutePath(), link2);
        if (!Files.isSymbolicLink(link2)) {
            Files.createSymbolicLink(link2, bin.resolve("rpbuild-gui.sh"));
        }
        link2.toFile().setExecutable(true);

        log.info("Links created! Rp Build CLI should work now!");
    }

    private void createWindowsLinks(Application application) throws IOException, InterruptedException {
        // Create rpbuild_home/bin.
        Path bin = createHomeBin(application);
        try {
            createScriptsWindows(bin);
        } catch (IOException e) {
            throw new TaskException(e);
        }

        // Add rpbuild_home/bin to PATH variable.
        // Modifying PATH variable is hell!

        // Add two links to WINDOWS directory.

        try {
            // Make links to C:/WINDOWS/rpbuild.bat
            Path link = Paths.get("C:/WINDOWS/rpbuild.bat");
            log.info("Creating symlink {} -> {}", bin.resolve("rpbuild.bat").toAbsolutePath(), link);
            if (!Files.isSymbolicLink(link)) {
                Files.createSymbolicLink(link, bin.resolve("rpbuild.bat"));
            }
            link.toFile().setExecutable(true);

            // Make links to C:/WINDOWS/rpbuild-gui.bat
            Path link2 = Paths.get("C:/WINDOWS/rpbuild-gui.bat");
            log.info("Creating symlink {} -> {}", bin.resolve("rpbuild-gui.bat").toAbsolutePath(), link2);
            if (!Files.isSymbolicLink(link2)) {
                Files.createSymbolicLink(link2, bin.resolve("rpbuild-gui.bat"));
            }
            link2.toFile().setExecutable(true);

            log.info("Links created! Rp Build CLI should work now!");
        } catch (FileSystemException e) {
            log.error("\n####################################################################");
            log.error("Cannot create links automatically!");
            log.error("Error: {}", e.getMessage().replace('\n', '\0').replace('\r', '\0'));
            log.error("You have these options: ");
            log.error(" 1) You can try to run again with administrator privileges.");
            log.error(" 2) Manually add to your PATH variable folder '{}'. ", bin.toAbsolutePath());
            log.error("####################################################################\n");
        }

    }

    private void createScriptsLinux(Path bin) throws IOException {
        log.info("Generating rpbuild linux script...");
        StringBuilder scriptRpBuild = new StringBuilder();
        scriptRpBuild.append("#!/bin/sh\n");
        scriptRpBuild.append("java -jar " + bin.getParent().toAbsolutePath().toString() + "/rpbuild.jar $@\n");
        Files.write(bin.resolve("rpbuild.sh"), scriptRpBuild.toString().getBytes("UTF-8"));
        log.info("Fixing permissions on rpbuild.sh...");
        bin.resolve("rpbuild.sh").toFile().setExecutable(true);

        log.info("Generating rpbuild-gui linux script...");
        StringBuilder scriptRpBuildGui = new StringBuilder();
        scriptRpBuildGui.append("#!/bin/sh\n");
        scriptRpBuildGui.append("java -jar " + bin.getParent().toAbsolutePath().toString() + "/rpbuild-gui.jar $@");
        Files.write(bin.resolve("rpbuild-gui.sh"), scriptRpBuildGui.toString().getBytes("UTF-8"));
        log.info("Fixing permissions on rpbuild-gui.sh...");
        bin.resolve("rpbuild-gui.sh").toFile().setExecutable(true);
    }

    private void createScriptsWindows(Path bin) throws IOException {
        log.info("Generating rpbuild windows script...");
        StringBuilder scriptRpBuild = new StringBuilder();
        scriptRpBuild.append("@java -jar " + bin.getParent().toAbsolutePath().toString() + "/rpbuild.jar %*\r\n");
        Files.write(bin.resolve("rpbuild.bat"), scriptRpBuild.toString().getBytes("UTF-8"));

        log.info("Generating rpbuild-gui windows script...");
        StringBuilder scriptRpBuildGui = new StringBuilder();
        scriptRpBuildGui.append("@java -jar " + bin.getParent().toAbsolutePath().toString() + "/rpbuild-gui.jar %*\r\n");
        Files.write(bin.resolve("rpbuild-gui.bat"), scriptRpBuildGui.toString().getBytes("UTF-8"));
    }

}
