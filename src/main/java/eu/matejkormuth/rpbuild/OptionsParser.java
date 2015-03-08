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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Represents class that is used to parse {@link Options} file.
 */
public class OptionsParser extends Properties {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates new parser from specified file.
	 * 
	 * @param file
	 *            file to load
	 */
	public OptionsParser(File file) {
		super();
		try {
			this.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns {@link Options} object that file passed to this parser
	 * represents.
	 * 
	 * @return options instance
	 */
	public Options parse() {
		Options o = new Options();
		o.optimizeFiles = Conv
				.toBool(this.getProperty("optimizeFiles", "true"));
		o.ignoreGit = Conv.toBool(this.getProperty("ignoreGit", "true"));
		o.fileFilters = this.getProperty("filters",
				".jar;.zip;.php;.rpbuild;.md;.bat;.sh;").split("\\;");
		o.projectName = this.getProperty("projectName", "resourcePack");
		o.resourcePackDescription = this.getProperty("resourcePackDescription");
		o.root = Paths.get(this.getProperty("root", new File(".").toPath()
				.toAbsolutePath().toString()));
		o.zipName = this.getProperty("zipName", "resourcePackCompiled.zip");
		o.gitPull = Conv.toBool(this.getProperty("gitPull"));
		return o;
	}
}
