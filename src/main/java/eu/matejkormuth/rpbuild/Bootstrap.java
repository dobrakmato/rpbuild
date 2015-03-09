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

import eu.matejkormuth.rpbuild.configuration.legacy.LegacyOptions;
import eu.matejkormuth.rpbuild.configuration.legacy.LegacyOptionsParser;

/**
 * Represents class with application entry point that handles startup logic of
 * application.
 */
public class Bootstrap {
	public static void main(String[] args) {
		// In case out client know how to use application.
		if (args.length == 1) {
			runAssembler(args[0]);
		} else {
			// Let's find file for him!
			findDescriptor();
		}
	}

	private static void findDescriptor() {
		String descriptorFile = null;
		int matchesFound = 0;
		for (String fileName : new File(".").list()) {
			if (fileName.equalsIgnoreCase("rpbuild.properties")) {
				descriptorFile = fileName;
				matchesFound++;
			} else if (fileName.endsWith(".rpbuild")) {
				descriptorFile = fileName;
				matchesFound++;
			}
		}

		if (descriptorFile != null) {
			if (matchesFound == 1) {
				System.out
						.println("Build descriptor not explicitly specified. Using file '"
								+ descriptorFile
								+ "' as build descriptor for this build.");
				runAssembler(descriptorFile);
			} else {
				System.out
						.println("More then one file matches conditions to be a build file in working directory. Please specify build descriptor explicitly!");
				printUsage();
				System.exit(1);
			}
		} else {
			System.out
					.println("We were unable to find valid build descriptor in working directory. Please specify it!");
			printUsage();
			System.exit(1);
		}
	}

	/**
	 * Starts build process with specified file as build descriptor.
	 * 
	 * @param file
	 *            build descriptor
	 */
	private static void runAssembler(String file) {
		LegacyOptions options = new LegacyOptionsParser(new File(file)).parse();
		new Assembler(options).build();
	}

	/**
	 * Prints help message to console.
	 */
	private static void printUsage() {
		System.out.println("Usage: rpbuild.jar <buildFile>");
	}
}
