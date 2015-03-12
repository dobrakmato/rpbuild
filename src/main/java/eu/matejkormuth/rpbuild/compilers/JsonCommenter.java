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

import eu.matejkormuth.rpbuild.Compiler;
import eu.matejkormuth.rpbuild.OpenedFile;

public class JsonCommenter extends Compiler {

	private String comment;

	@Override
	public void init() {
		this.comment = this.getSetting("comment",
				"Please set comment message in your build config.").getValue();
	}

	@Override
	public void compile(OpenedFile file) throws Exception {
		if (this.getSetting("formatted", "false").equals("true")) {
			this.commentFormatted(file);
		} else {
			this.commentUnformatted(file);
		}
	}

	private void commentUnformatted(OpenedFile file) {
		String contents = new String(file.getContents(), this.getCharset());
		// { "__comment" : "comment value",
		contents = contents.replace("{", "{\"__comment\":\"" + this.comment
				+ "\",");
		file.setContents(contents.getBytes(this.getCharset()));
	}

	private void commentFormatted(OpenedFile file) {
		// Until someone starts complaining.
		this.commentUnformatted(file);
	}
}
