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

public class Bootstrap {
	public static void main(String[] args) {
		if (args.length == 0) {
			printHelpMessage();
			System.exit(1);
		}

		if (args.length == 1) {
			String file = args[0];
			Options options = new OptionsParser(new File(file)).parse();
			new Assembler(options).build();
		} else {
			printHelpMessage();
		}
	}

	private static void printHelpMessage() {
		System.out.println("Incorrect usage!");
		System.out.println("Usage: rpbuild.jar <buildFile>");
	}
}
