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

import eu.matejkormuth.rpbuild.tasks.AbstractTask;
import eu.matejkormuth.rpbuild.tasks.ClearCacheTask;
import eu.matejkormuth.rpbuild.tasks.build.*;
import eu.matejkormuth.rpbuild.tasks.install.CreateLinksTask;
import eu.matejkormuth.rpbuild.tasks.install.InstallCLITask;
import eu.matejkormuth.rpbuild.tasks.update.UpdateRpBuildTask;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;

/**
 * Entry point class.
 */
@Slf4j
public class Bootstrap {
    /**
     * Entry point.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        Args arguments = new Args(args);

        if (arguments.empty()) {
            runBuild(arguments);
        } else {
            String command = arguments.nextString();
            switch (command) {
                case "build":
                case "b":
                    runBuild(arguments);
                    break;
                case "clear-cache":
                case "c":
                    runClearCache();
                    break;
                case "install":
                case "i":
                    runInstall();
                    break;
                case "update":
                case "u":
                    runUpdate(arguments);
                    break;
                case "help":
                case "h":
                default:
                    runHelp();
                    break;
            }
        }
    }

    /**
     * Updates rpbuild CLI.
     *
     * @param args arguments
     */
    private static void runUpdate(Args args) {
        log.info("Updating rpBuild CLI...");

        LinkedList<AbstractTask> tasks = new LinkedList<>();
        tasks.addLast(new UpdateRpBuildTask());

        Options options = new Options();
        options.setAutoUpdate(false);

        Application application = new Application(options, tasks);
        application.run();
    }

    /**
     * Shows help message.
     */
    private static void runHelp() {
        log.info("rpbuild [command] [arguments]");
        log.info("Commands: ");
        log.info(" (b)uild [file] - Builds by the specified file.");
        log.info(" (c)lear-cache - Clears the local repository.");
        log.info(" (i)nstall - Installs / reinstalls the the rpbuild CLI.");
        log.info(" (u)pdate - Performs update of rpbuild CLI if available.");
        log.info(" (h)elp - Shows help message.");
    }

    /**
     * Installs rpbuild as CLI.
     */
    private static void runInstall() {
        log.info("Installing rpbuild as CLI...");

        LinkedList<AbstractTask> tasks = new LinkedList<>();
        tasks.addLast(new InstallCLITask());
        tasks.addLast(new UpdateRpBuildTask());
        tasks.addLast(new CreateLinksTask());

        Options options = new Options();
        options.setAutoUpdate(false);

        Application application = new Application(options, tasks);
        application.run();
    }

    /**
     * Clears the cache.
     */
    private static void runClearCache() {
        LinkedList<AbstractTask> tasks = new LinkedList<>();
        tasks.addLast(new ClearCacheTask());

        Application application = new Application(new Options(), tasks);
        application.run();
    }

    /**
     * Runs a build with rpbuild.
     *
     * @param args arguments
     */
    private static void runBuild(Args args) {
        String fileName = "rpbuild.conf";
        if (!args.empty()) {
            fileName = args.nextString();
        }

        Options options = new Options();
        options.setFile(fileName);
        options.parseArgs(args);

        LinkedList<AbstractTask> tasks = new LinkedList<>();
        tasks.addLast(new LoadConfigurationTask());
        tasks.addLast(new CollectPluginsTask());
        tasks.addLast(new GitPullTask());
        tasks.addLast(new MoveToTempDirectoryTask());
        tasks.addLast(new CompileAndGenerateTask());
        tasks.addLast(new AssembleTask());

        Application application = new Application(options, tasks);
        application.run();
    }
}
