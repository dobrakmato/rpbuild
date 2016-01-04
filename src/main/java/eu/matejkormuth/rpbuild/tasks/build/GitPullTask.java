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

import java.io.IOException;
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
        Runtime.getRuntime().exec("git stash save --keep-index").waitFor();
        log.info("Executing: git stash drop.");
        Runtime.getRuntime().exec("git stash drop").waitFor();

        // Switch branch.
        log.info("Executing: git branch {}.", branch);
        Runtime.getRuntime().exec("git branch " + branch).waitFor();

        // Pull.
        log.info("Pulling changes...");
        Process gitProcess = Runtime.getRuntime().exec("git pull");
        int exitCode = gitProcess.waitFor();
        if (exitCode != 0) {
            log.warn("Git process exited with exit code {}.", exitCode);
            log.warn("This might be a problem! If your resource pack is built incorrectly, try running git pull by yourself.");
        } else {
            log.info("Git process exited with exit code {}.", exitCode);
        }
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
        Process gitProcess = Runtime.getRuntime().exec(cmd);
        int exitCode = gitProcess.waitFor();
        if (exitCode != 0) {
            log.warn("Git process exited with exit code {}.", exitCode);
            log.warn("This might be a problem! If your resource pack is built incorrectly, try running git clone by yourself.");
        } else {
            log.info("Git process exited with exit code {}.", exitCode);
        }
    }
}