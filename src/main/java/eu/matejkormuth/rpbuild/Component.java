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

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Component {
	protected static Logger log;

	private Assembler assembler;

	public Component() {
		log = LoggerFactory.getLogger(this.getClass());
	}

	protected void setAssembler(Assembler assembler) {
		if (this.assembler != null) {
			throw new IllegalAccessError(
					"Assembler has been alredy set for this component!");
		}
		this.assembler = assembler;
	}

	public Options getOptions() {
		return this.assembler.getOptions();
	}

	public Charset getCharset() {
		return this.assembler.getCharset();
	}

	public Path getPath(String relative) {
		return this.assembler.getRootPath().resolve(Paths.get(relative));
	}
}
