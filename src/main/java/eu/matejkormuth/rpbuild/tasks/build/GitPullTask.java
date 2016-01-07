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
import eu.matejkormuth.rpbuild.api.Project;
import eu.matejkormuth.rpbuild.exceptions.TaskException;
import eu.matejkormuth.rpbuild.tasks.AbstractTask;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Enables Git VS integration to rpbuild.
 */
@Slf4j
public class GitPullTask extends AbstractTask {

    @Override
    public void run() {
        // Get project object.
        Project project = Application.resolve(Project.class);

        // Skip if not requested.
        if (project.getGit() == null || !project.getGit().isPull()) {
            log.info("Skipping git pull because it's disabled.");
            return;
        }

        // Check if repository exists?
        try {
            Path possibleDotGit = project.getSource().resolve(".git");
            if (Files.exists(possibleDotGit)) {
                doGitPull(project.getGit().getBranch());
            } else {
                doGitClone(project.getGit().getUrl(), project.getGit().getBranch());
            }
        } catch (Exception e) {
            log.error("Something strange happened! ", e);
            throw new TaskException(e);
        }
    }

    /**
     * Performs Git pull on specified branch.
     *
     * @param branch branch to pull from
     * @throws IOException          when problem occurs
     * @throws InterruptedException when interruption occurs
     */
    private void doGitPull(String branch) throws IOException, InterruptedException {
        log.info("Folder .git found! We will be pulling changes from repo soon.");

        // Destroy local changes.
        log.info("Executing: git stash save --keep-index.");
        execute("git stash save --keep-index");
        log.info("Executing: git stash drop.");
        execute("git stash drop");

        // Switch branch.
        log.info("Executing: git branch {}.", branch);
        execute("git branch " + branch);

        // Pull.
        log.info("Pulling changes...");
        execute("git pull");
    }

    /**
     * Performs Git clone from specified Url and specified branch.
     *
     * @param url    url of git repository
     * @param branch branch to use
     * @throws IOException          when problem occurs
     * @throws InterruptedException when interruption occurs
     */
    private void doGitClone(String url, String branch) throws IOException, InterruptedException {

        // TODO: Just a note to myself. This might be broken, because git allows cloning to current directory
        // TODO: only if its empty. But actually current directory probably contains rpbuild's jar and configuration.
        // TODO: We can get around this by using more git commands.

        log.info("Cloning git repository from {}...", url);
        String cmd = "git clone -b \"" + branch + "\" --single-branch \"" + url + "\" .";
        log.info("Executing: {}.", cmd);
        execute(cmd);
    }

    /**
     * Executes specified command and writes output to log.
     *
     * @param cmd command(s)
     */
    private static void execute(String cmd) {
        Project project = Application.resolve(Project.class);

        BufferedReader reader = null;
        try {
            Process process = new ProcessBuilder(cmd.split(" "))
                    .directory(project.getSource().toFile())
                    .redirectErrorStream(true)
                    .start();

            // Read process output.
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(" > {}", line);
            }
            reader.close();

        } catch (Exception e) {
            log.error("Error while executing '" + cmd + "'!", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Be silent.
                }
            }
        }
    }
}