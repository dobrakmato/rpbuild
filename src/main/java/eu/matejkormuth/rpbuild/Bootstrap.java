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
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import eu.matejkormuth.rpbuild.api.BuildStep;
import eu.matejkormuth.rpbuild.api.Project;
import eu.matejkormuth.rpbuild.compilers.JsonCompressor;
import eu.matejkormuth.rpbuild.configuration.xml.XmlBuildStepCompile;
import eu.matejkormuth.rpbuild.configuration.xml.XmlBuildStepGenerate;
import eu.matejkormuth.rpbuild.configuration.xml.XmlProject;
import eu.matejkormuth.rpbuild.configuration.xml.XmlSetting;
import eu.matejkormuth.rpbuild.generators.PackMcmetaGenerator;
import eu.matejkormuth.rpbuild.generators.sounds.FileTreeSoundsJsonGenerator;

/**
 * Represents class with application entry point that handles startup logic of
 * application.
 */
public class Bootstrap {
	public static void main(String[] args) {
		printInfo();
		// In case our client know how to use application.
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("create")) {
				createDefault();
			} else {
				runAssembler(args[0]);
			}
		} else {
			// Let's find file for him!
			findDescriptor();
		}
	}

	private static void createDefault() {
		System.out.println("Creating default configuration file rpbuild.xml...");
		try {
			Marshaller m = JAXBContext.newInstance(XmlProject.class,
					XmlBuildStepCompile.class, XmlBuildStepGenerate.class,
					XmlSetting.class).createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			XmlProject proj = new XmlProject();

			// Search for .git folder and automatically set up git pull.
			if (new File("./.git/").exists()) {

				System.out.println("Git folder found in current directory!\n");

				System.out.println("        --------------------------------------------");
				System.out.println("        |         WARNING! READ CAREFULLY!         |");
				System.out.println("        |------------------------------------------|");
				System.out.println("        | When git pull is enabled, rpbuild destr- |");
				System.out.println("        | oys all local changes! If you do not wa- |");
				System.out.println("        | nt that, please disable gitPull in your  |");
				System.out.println("        | rpbuild.xml!                             |");
				System.out.println("        --------------------------------------------");

				System.out.println("\nAutomatically setting up git pull for this rpbuild.xml!");

				proj.setGitPull(true);
			}

			// Add deafult build steps.
			BuildStep[] build = new BuildStep[] {
					new XmlBuildStepGenerate(PackMcmetaGenerator.class),
					new XmlBuildStepGenerate(FileTreeSoundsJsonGenerator.class),
					new XmlBuildStepCompile(JsonCompressor.class, ".json") };

			proj.setBuild(build);
			m.marshal(proj, new FileWriter(new File("rpbuild.xml")));
			System.out.println("File rpbuild.xml has been created!");
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}
	}

	private static void findDescriptor() {
		String descriptorFile = null;
		int matchesFound = 0;
		for (String fileName : new File(".").list()) {
			if (fileName.equalsIgnoreCase("rpbuild.xml")) {
				descriptorFile = fileName;
				matchesFound++;
			} else if (fileName.equalsIgnoreCase("rpbuild.properties")) {
				descriptorFile = fileName;
				matchesFound++;
			} else if (fileName.endsWith(".rpbuild")) {
				descriptorFile = fileName;
				matchesFound++;
			}
		}

		if (descriptorFile != null) {
			if (matchesFound == 1) {
				System.out.println("Build descriptor not explicitly specified. Using file '"
								+ descriptorFile
								+ "' as build descriptor for this build.");
				runAssembler(descriptorFile);
			} else {
				System.out.println("More then one file matches conditions to be a "
								+ "build file in working directory. Please specify build descriptor explicitly!");
				printUsage();
				System.exit(1);
			}
		} else {
			System.out.println("No valid valid build descriptor was found in working directory. Please specify it!");
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
		if (file.endsWith(".xml")) {
			try {
				JAXBContext context = JAXBContext.newInstance(XmlProject.class,
						XmlBuildStepCompile.class, XmlBuildStepGenerate.class);
				Object projectObj = context.createUnmarshaller().unmarshal(
						new File(file));
				Project options = (Project) projectObj;
				new Assembler(options).build();
			} catch (Exception e) {
				System.out.println("Can't initialize");
				e.printStackTrace();
			}
		} else {
			System.out.println("Sorry, rpbuild does not support legacy build desccriptors anymore!");
			System.out.println("You should convert your build descriptor to new xml format "
							+ "which provides more options and more control over your build.");
			System.out.println("For more information please visit github page: https://github.com/dobrakmato/rpbuild#xml-configuration");
		}
	}

	/**
	 * Prints help message to console.
	 */
	private static void printUsage() {
		System.out.println("Usage: rpbuild.jar <buildFile>");
	}
	
	private static void printInfo() {
		System.out.println("rpbuild.jar - " + Bootstrap.class.getPackage().getImplementationVersion());
		System.out.println("If you run into troubles: https://github.com/dobrakmato/rpbuild/issues");
		System.out.println();
	}
}
