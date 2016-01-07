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
import eu.matejkormuth.rpbuild.ZipArchive;
import eu.matejkormuth.rpbuild.api.BuildSection;
import eu.matejkormuth.rpbuild.api.Project;
import eu.matejkormuth.rpbuild.exceptions.TaskException;
import eu.matejkormuth.rpbuild.tasks.AbstractTask;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Assembles the files to one archive.
 */
@Slf4j
public class AssembleTask extends AbstractTask {

    @Override
    public void run() {
        Project project = Application.resolve(Project.class);

        log.info("Recursively searching for excludes...");

        // Recursively obtain all excludes in build configuration.
        Map<Path, List<PathMatcher>> excludes = new HashMap<>(); // Path (directory) -> List<PathMatcher>
        recurCollectExcludes(project.getBuild(), excludes, null);

        log.info("Recursively collection files...");

        // Recursively obtain all files to include in zip.
        List<Path> files = new ArrayList<>();
        try {
            recurListFilesTo(project.getSource(), files, excludes);
        } catch (IOException e) {
            throw new TaskException(e);
        }
        log.info("Collected {} files.", files.size());

        log.info("Building archive...");
        // Create zip archive.
        ZipArchive zipArchive = new ZipArchive(project.getSource(),
                project.getTarget().toFile(), project.getCompress().getLevel());

        // Add all files to zip.
        for (Path file : files) {
            try {
                zipArchive.addFile(file);
            } catch (IOException e) {
                log.error("For some reason, we can't add file {} to zip! Perhaps not enough disk space?", e);
            }
        }

        // Close archive.
        zipArchive.close();

        log.info("Archive built!");

        long size = project.getTarget().toFile().length();
        log.info("Size of final archive is: {} MB.", size / 1024 / 1024);
        if (size > 1024 * 1024 * 50) {
            log.warn("#################################");
            log.warn("Size of the zip is greater then 50 MB!");
            log.warn("That means, you will not be able to send this resource pack as server resource pack automatically!");
            log.warn("This is limitation of Minecraft.");
            log.warn("If you want to learn more: https://github.com/dobrakmato/rpbuild/wiki/abcdefghi");
            log.warn("#################################");
        }

    }

    private void recurCollectExcludes(BuildSection section, Map<Path, List<PathMatcher>> excludes, List<PathMatcher> parentExcludes) {
        Path directory = section.getAbsolutePath();

        List<PathMatcher> localExcludes = new ArrayList<>(section.getExclude().size());
        localExcludes.addAll(section.getExclude());
        if (parentExcludes != null) {
            localExcludes.addAll(parentExcludes);
        }

        excludes.put(directory, localExcludes);

        for (BuildSection section1 : section.getChildren()) {
            recurCollectExcludes(section1, excludes, localExcludes);
        }
    }

    private void recurListFilesTo(Path directory, List<Path> to, Map<Path, List<PathMatcher>> excludes) throws IOException {
        List<PathMatcher> localExcludes;
        if (excludes.containsKey(directory)) {
            localExcludes = excludes.get(directory);
        } else {
            localExcludes = new ArrayList<>(0);
        }

        // Get entries in current directory **excluded by filters**.
        DirectoryStream<Path> entries = Files.newDirectoryStream(directory, entry -> {
            for (PathMatcher filter : localExcludes) {
                if (filter.matches(entry)) {
                    return false;
                }
            }
            return true;
        });

        for (Path entry : entries) {
            if (Files.isDirectory(entry)) {
                recurListFilesTo(entry.toAbsolutePath(), to, excludes);
            } else {
                to.add(entry.toAbsolutePath());
            }
        }

    }

}
