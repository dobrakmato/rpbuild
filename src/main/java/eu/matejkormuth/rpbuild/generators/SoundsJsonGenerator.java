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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONObject;

import eu.matejkormuth.rpbuild.GeneratedFile;
import eu.matejkormuth.rpbuild.Generator;

public class SoundsJsonGenerator extends Generator {
	private JSONObject root;

	public GeneratedFile generate() {
		this.create();

		this.addFirearms();
		this.addOtherSounds();

		return this.build();
	}

	private GeneratedFile build() {
		return new GeneratedFile("assets/minecraft/sounds.json", this.root
				.toString(2).getBytes(this.getCharset()));
	}

	private void addOtherSounds() {
		// Currently nothing.
	}

	private void addFirearms() {
		// Get files
		File f = null;
		Path path = this.getPath("assets/minecraft/sounds/firearms/")
				.toAbsolutePath();
		for (String fireArmDir : path.toFile().list()) {
			if ((f = new File(path.toString() + "/" + fireArmDir))
					.isDirectory()) {
				this.addFirearm(f);
			}
		}
	}

	private void addFirearm(File fireArmDir) {
		File[] fileList = fireArmDir.listFiles();
		String gunName = fireArmDir.getName();

		// Reload record.
		List<String> reloadSounds = new ArrayList<>();
		for (int i = 0; i < fileList.length; i++) {
			File file = fileList[i];
			if (file.getName().toLowerCase().contains("reload")) {
				reloadSounds.add("/firearms/" + gunName + "/"
						+ file.getName().replace(".ogg", ""));
			}
		}
		addSound(gunName + "_reload", reloadSounds);

		// Fire record.
		List<String> fireSounds = new ArrayList<>();
		for (int i = 0; i < fileList.length; i++) {
			File file = fileList[i];
			if (file.getName().toLowerCase().contains("fire")) {
				fireSounds.add("/firearms/" + gunName + "/"
						+ file.getName().replace(".ogg", ""));
			}
		}
		addSound(gunName + "_fire", fireSounds);
	}

	private void addSound(String key, Collection<String> sounds) {
		JSONObject sound = new JSONObject();
		sound.put("sounds", sounds);
		root.put(key, sound);
	}

	private void create() {
		root = new JSONObject();
	}
}
