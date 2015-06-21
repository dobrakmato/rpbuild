/**
 * Minecraft resource pack compiler and assembler - rpBuild - Build system for Minecraft resource packs.
 * Copyright (c) 2015, Matej Kormuth <http://www.github.com/dobrakmato>
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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents class that walks through file tree and finds relevant project
 * files.
 */
public class FileFinder extends SimpleFileVisitor<Path> {
	private List<Path> paths;
	private boolean ignoreGit;

	/**
	 * Creates new instance of FileFinder.
	 */
	public FileFinder() {
		this.paths = new ArrayList<Path>();
	}

	/**
	 * Finds all files in all directories (recursively) from start path. Note
	 * that this method removes any found files from internal list.
	 * 
	 * @param start
	 *            path to start.
	 * @return amount of files found
	 * @throws IOException if an I/O error is thrown by a visitor method
	 */
	public int find(Path start) throws IOException {
		this.paths.clear();
		Files.walkFileTree(start, this);
		return this.paths.size();
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {
		if (this.ignoreGit && file.toString().contains(".git")) {
			return FileVisitResult.SKIP_SUBTREE;
		}

		this.paths.add(file);
		return super.visitFile(file, attrs);
	}

	/**
	 * Returns list of all found files as Path list.
	 * 
	 * @return list of paths
	 */
	public List<Path> getPaths() {
		return paths;
	}

	/**
	 * Returns list of all found files as File list.
	 * 
	 * @return list of files
	 */
	public List<File> getFiles() {
		List<File> files = new ArrayList<File>(this.paths.size());
		for (Path p : this.paths) {
			files.add(p.toFile());
		}
		return files;
	}

	/**
	 * Returns list of all found files with specified file type (extension) as
	 * File list.
	 * 
	 * @param fileExtension file extension to look for
	 * @return list of files
	 */
	public List<File> getFiles(String fileExtension) {
		List<File> files = new ArrayList<File>();
		for (Path p : this.paths) {
			if (p.endsWith(fileExtension)) {
				files.add(p.toFile());
			}
		}
		return files;
	}

	/**
	 * Returns list of all found files with specified file type (extension) as
	 * Path list.
	 * 
	 * @param fileExtension file extension to look for
	 * @return list of paths
	 */
	public List<Path> getPaths(String fileExtension) {
		List<Path> files = new ArrayList<Path>();
		for (Path p : this.paths) {
			if (p.toString().endsWith(fileExtension)) {
				files.add(p);
			}
		}
		return files;
	}

	/**
	 * Specifies whether this finder should ignore possible git folders.
	 * 
	 * @param ignoreGit
	 *            true if this FileFinder should ignore git folder, false
	 *            otherwise.
	 */
	public void setIgnoreGit(boolean ignoreGit) {
		this.ignoreGit = ignoreGit;
	}
}
