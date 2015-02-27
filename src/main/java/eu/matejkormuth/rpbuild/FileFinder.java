/*
 *  rpbuild - RPBuild is a build system for Minecraft resource packs.
 *  Copyright (C) 2015 Matej Kormuth 
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  "Minecraft" is a trademark of Mojang AB
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
	 * @throws IOException
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
	 * @return list of files
	 */
	public List<File> getFiles(String fileType) {
		List<File> files = new ArrayList<File>();
		for (Path p : this.paths) {
			if (p.endsWith(fileType)) {
				files.add(p.toFile());
			}
		}
		return files;
	}

	/**
	 * Returns list of all found files with specified file type (extension) as
	 * Path list.
	 * 
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
