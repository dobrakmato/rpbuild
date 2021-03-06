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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that creates zip file from specified input files.
 */
public class ZipArchive {
	private static final Logger log = LoggerFactory.getLogger(ZipArchive.class);

	private ZipOutputStream stream;
	private Path absolute;

	/**
	 * Creates new instance of ZIP archive.
	 * 
	 * @param absolutePath
	 *            absolute path that will be used to resolve path when calling
	 *            <code>addFile(Path relative)</code>.
	 * @param out archive file
	 * @param compressionLevel ZIP compression level
	 */
	public ZipArchive(Path absolutePath, File out, int compressionLevel) {
		try {
			this.stream = new ZipOutputStream(new FileOutputStream(out));
			this.stream.setComment("Generated by: rpbuild by dobrakmato. <http://github.com/dobrakmato>");
			this.stream.setLevel(compressionLevel);
			this.stream.setMethod(ZipOutputStream.DEFLATED);

			this.absolute = absolutePath;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds file at specified path to this zip archive.
	 * 
	 * @param path
	 *            path of file to be added
	 * @throws IOException if an I/O error occurs.
	 */
	public void addFile(Path path) throws IOException {
		log.debug("Adding file: {}", path);

		ZipEntry entry = new ZipEntry(this.absolute.relativize(
				path.toAbsolutePath()).toString());

		this.stream.putNextEntry(entry);
		this.stream.write(Files.readAllBytes(path));
		this.stream.closeEntry();
	}

	/**
	 * Closes this zip archive and underlying ZipOutputStream.
	 */
	public void close() {
		try {
			this.stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
