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
import eu.matejkormuth.rpbuild.nio.CopyFileVisitor;
import eu.matejkormuth.rpbuild.tasks.AbstractTask;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;

/**
 * Moves files from working directory to temporary directory.
 */
@Slf4j
public class MoveToTempDirectoryTask extends AbstractTask {

    @Override
    public void run() {
        // Get project object.
        Project project = Application.resolve(Project.class);

        log.info("Creating temporary directory for build...");
        // Create temp directory.
        Path tempDirectory = createTempDirectory(project.getName());
        // Request folder removal on rpbuild's exit.
        tempDirectory.toFile().deleteOnExit();

        // Copy files.
        final Path originalSource = project.getSource();
        final Path newSource = tempDirectory;

        // Only exclude .git if wanted. We don't want ot complicate this, just copy all stuff.
        ArrayList<PathMatcher> excludes = new ArrayList<>(1);
        if(project.getGit() != null && project.getGit().isIgnoreGitFolders()) {
            excludes.add(FileSystems.getDefault().getPathMatcher("glob:**/.git"));
            excludes.add(FileSystems.getDefault().getPathMatcher("glob:**/.git/**"));
        }

        log.info("Copying files...");
        try {
            Files.walkFileTree(originalSource, new CopyFileVisitor(newSource, excludes));
        } catch (IOException e) {
            throw new TaskException(e);
        }

        log.info("Done! Updating project source.");
        // Update source directory in project object.
        project.setSource(newSource);
    }

    /**
     * Creates temporary directory with specified name.
     *
     * @param name name of this project
     * @return path of new temp directory
     * @throws TaskException when directory couldn't be created
     */
    private Path createTempDirectory(String name) throws TaskException {
        try {
            return Files.createTempDirectory("rpbuild_" + name.toLowerCase().replace(" ", "_").substring(0, 5));
        } catch (IOException e) {
            throw new TaskException("Can't create temporary directory!", e);
        }
    }

}
