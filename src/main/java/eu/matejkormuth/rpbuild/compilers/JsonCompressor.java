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
package eu.matejkormuth.rpbuild.compilers;

import eu.matejkormuth.rpbuild.FileCompiler;
import eu.matejkormuth.rpbuild.OpenedFile;

/**
 * Represents compiler that compresses JSON files by removing all whitespace
 * from them.
 */
public class JsonCompressor extends FileCompiler {
	@Override
	public void compile(OpenedFile file) {
		// Replace all whitespace.
		file.setContents(new String(file.getContents(), this.getCharset())
				.replaceAll("\\s+", "").getBytes(this.getCharset()));
	}
}
