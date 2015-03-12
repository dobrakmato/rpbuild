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

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents opened file. Contains file path and file contents.
 */
public class OpenedFile {
	private Path path;
	private byte[] contents;

	public OpenedFile(Path path) {
		this.path = path;
		try {
			this.contents = Files.readAllBytes(path);
		} catch (Exception e) {
			throw new BuildError(e);
		}
	}

	public OpenedFile(Path path, byte[] contents) {
		this.path = path;
		this.contents = contents;
	}

	public Path getPath() {
		return path;
	}

	public byte[] getContents() {
		return contents;
	}

	public void setContents(byte[] contents) {
		this.contents = contents;
	}

	public void save() {
		try {
			Files.write(this.path, this.contents);
		} catch (Exception e) {
			throw new BuildError(e);
		}
	}
}
