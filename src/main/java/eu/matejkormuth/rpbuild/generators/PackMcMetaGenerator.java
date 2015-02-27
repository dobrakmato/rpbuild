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
package eu.matejkormuth.rpbuild.generators;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import eu.matejkormuth.rpbuild.GeneratedFile;
import eu.matejkormuth.rpbuild.Generator;

public class PackMcMetaGenerator extends Generator {
	private JSONObject json = new JSONObject();

	@Override
	public GeneratedFile generate() {
		String description = null;
		if (this.getOptions().resourcePackDescription != null) {
			description = this.getOptions().resourcePackDescription;
		} else {
			description = this.getOptions().projectName + " - "
					+ new SimpleDateFormat().format(new Date());
		}

		JSONObject pack = new JSONObject();
		pack.put("pack_format", 1);
		pack.put("description", description);
		json.put("pack", pack);

		return new GeneratedFile("pack.mcmeta", json.toString(2).getBytes(
				this.getCharset()));
	}
}
