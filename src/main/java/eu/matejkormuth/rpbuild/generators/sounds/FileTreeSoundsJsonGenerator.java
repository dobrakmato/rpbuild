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
package eu.matejkormuth.rpbuild.generators.sounds;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import org.json.JSONObject;

import eu.matejkormuth.rpbuild.FileGenerator;
import eu.matejkormuth.rpbuild.OpenedFile;

public class FileTreeSoundsJsonGenerator extends FileGenerator implements
		FileVisitor<Path> {

	private JSONObject root;

	private void addSound(String groupKey, String sound) {
		if (root.has(groupKey)) {
			((JSONObject) root.get(groupKey)).getJSONArray("sounds").put(sound);
		} else {
			JSONObject sounds = new JSONObject();
			sounds.put("sounds", Arrays.asList(sound));
			root.put(groupKey, sounds);
		}
	}

	@Override
	public OpenedFile generate() throws Exception {
		this.root = new JSONObject();

		// Start walking on root of sounds.
		Files.walkFileTree(this.getPath("assets/minecraft/sounds/"), this);

		return new OpenedFile(this.getPath("assets/minecraft/sounds.json"),
				this.root.toString(2).getBytes(this.getCharset()));
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {
		// Only ogg files.
		if (attrs.isRegularFile()) {
			// Add this file.
			final String fsSeparator = file.getFileSystem().getSeparator();
			String path = file.toString().replace(".ogg", "")
					.replace(fsSeparator, "/")
					.replace("./assets/minecraft/sounds/", "");
			String key = file.toString().replace(fsSeparator, ".")
					.replace("..assets.minecraft.sounds.", "")
					.replace(".ogg", "");
			if (key.contains("_")) {
				this.addSound(key.substring(0, key.indexOf("_")), path);
			} else {
				this.addSound(key, path);
			}
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc)
			throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc)
			throws IOException {
		return FileVisitResult.CONTINUE;
	}
}
