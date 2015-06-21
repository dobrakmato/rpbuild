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
package eu.matejkormuth.rpbuild.generators.sounds;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import org.json.JSONObject;

import eu.matejkormuth.rpbuild.Generator;
import eu.matejkormuth.rpbuild.OpenedFile;
import eu.matejkormuth.rpbuild.exceptions.BuildError;

public class FileTreeSoundsJsonGenerator extends Generator implements
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
	public OpenedFile generate() throws BuildError {
		this.root = new JSONObject();

		// Start walking on root of sounds.
		try {
			Files.walkFileTree(this.getPath("assets/minecraft/sounds/"), this);
		} catch (IOException e) {
			throw new BuildError("Can't walk file tree!", e);
		}

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
