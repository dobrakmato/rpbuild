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
package eu.matejkormuth.rpbuild.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * Copies all files from one directory to another recursively while applying exclude filters..
 */
@Slf4j
public class CopyFileVisitor extends SimpleFileVisitor<Path> {
    private final Path targetPath;
    private Path sourcePath = null;

    public CopyFileVisitor(Path targetPath, List<PathMatcher> excludeFilters) {
        this.targetPath = targetPath;
        this.excludeFilters = excludeFilters;
    }

    /**
     * List of all exclude filters.
     */
    private final List<PathMatcher> excludeFilters;

    @Override
    public FileVisitResult preVisitDirectory(final Path dir,
                                             final BasicFileAttributes attrs) throws IOException {
        for (int i = 0; i < excludeFilters.size(); i++) {
            if (excludeFilters.get(i).matches(dir)) {
                log.info("Skipping copy of {} because it matches {}.", dir, excludeFilters.get(i));
                // If the filter is triggered, continue to next file without adding.
                return FileVisitResult.SKIP_SUBTREE;
            }
        }

        if (sourcePath == null) {
            sourcePath = dir;
        } else {
            Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file,
                                     final BasicFileAttributes attrs) throws IOException {
        for (int i = 0; i < excludeFilters.size(); i++) {
            if (excludeFilters.get(i).matches(file)) {
                // If the filter is triggered, continue to next file without adding.
                return FileVisitResult.CONTINUE;
            }
        }
        try {
            Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
        } catch (Exception e) {
            log.error("Can't copy file {} due to {}!", file, e.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }
}