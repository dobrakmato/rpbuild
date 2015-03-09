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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.matejkormuth.rpbuild.BuildStep.CompileBuildStep;
import eu.matejkormuth.rpbuild.BuildStep.GenerateBuildStep;
import eu.matejkormuth.rpbuild.api.Project;
import eu.matejkormuth.rpbuild.compilers.JsonCompressor;
import eu.matejkormuth.rpbuild.generators.PackMcMetaGenerator;
import eu.matejkormuth.rpbuild.generators.StarvingSoundsJsonGenerator;

/**
 * Represents main part of build system. Assembles files and manages build
 * process and logging.
 */
public class Assembler {
	private static final Logger log = LoggerFactory.getLogger(Assembler.class);

	private List<Generator> generators;
	private List<FileExtensionCompilerList> compilerLists;
	private FileFinder fileFinder;
	private SimpleDateFormat dateTimeFormat;
	private SimpleDateFormat timeSpanFormat;
	private Project project;

	public Assembler(Project project) {
		this.generators = new ArrayList<Generator>();
		this.compilerLists = new ArrayList<FileExtensionCompilerList>();
		this.dateTimeFormat = new SimpleDateFormat();
		this.timeSpanFormat = new SimpleDateFormat("mm:ss.SSS");

		this.project = project;

		this.addBuildStep(BuildStep.generate(new PackMcMetaGenerator()));
		this.addBuildStep(BuildStep.generate(new StarvingSoundsJsonGenerator()));
		this.addBuildStep(BuildStep.compile(new JsonCompressor(), ".json"));

		this.fileFinder = new FileFinder();
		this.fileFinder.setIgnoreGit(this.project.isIgnoreGitFolders());
	}

	private void findFiles() {
		log.info("Looking for files...");
		try {
			int count = this.fileFinder.find(this.project.getSrc());
			log.info("Found {} files!", count);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void build() {
		printBuildStart();

		long startTime = System.currentTimeMillis();

		if (this.project.isGitPull()) {
			log.info("Git pull is enabled in this build! Pulling using shell command (git pull).");
			try {
				Process gitProcess = Runtime.getRuntime().exec("git pull");
				int exitCode = gitProcess.waitFor();
				log.info("Git process exited with exit code {}.", exitCode);

			} catch (IOException | InterruptedException e) {
				log.error("Can't complete git pull! Giving up!", e);
				printBuildEnd(System.currentTimeMillis() - startTime, "FAILURE");
			}
			printSeparator();
		}

		this.findFiles();

		try {
			this.generateFiles();

			this.printSeparator();
			this.findFiles();

			this.buildFiles();
			this.assembly();

			this.printSeparator();
			this.findFiles();

			this.archive();
		} catch (BuildError error) {
			log.error("Build failed: ", error);
			printBuildEnd(System.currentTimeMillis() - startTime, "FAILURE");
			return;
		}

		long elapsedTime = System.currentTimeMillis() - startTime;

		printBuildEnd(elapsedTime, "SUCCESS");
	}

	private void printBuildEnd(long elapsedTime, String status) {
		printSeparator();
		log.info("Build of project {} finished!", project.getProjectName());
		printSeparator();
		log.info("Status: {}", status);
		log.info("Memory: {} MB / {} MB",
				(Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
						.freeMemory()) / 1024 / 1024, Runtime.getRuntime()
						.totalMemory() / 1024 / 1024);
		log.info("Total time: {}",
				this.timeSpanFormat.format(new Date(elapsedTime)));
		printSeparator();
	}

	private void printBuildStart() {
		printSeparator();
		log.info("Build of project " + project.getProjectName()
				+ " started at " + this.dateTimeFormat.format(new Date()));
		log.info("Used charset/encoding: " + this.project.getEncoding());
		printSeparator();
	}

	private void printSeparator() {
		log.info("------------------------------------------------------");
	}

	private void generateFiles() {
		printSeparator();
		int count = 0;
		for (Generator g : this.generators) {
			log.info("Running generator: {}", g.getClass().getSimpleName());
			try {
				GeneratedFile file = g.generate();
				if (file == null) {
					log.warn("Generator {} generated null file!", g.getClass()
							.getSimpleName());
					continue;
				}
				this.saveFile(file);
				count++;
			} catch (Exception e) {
				throw new BuildError(e);
			}
		}
		log.info("Totally generated {} files!", count);
	}

	private void buildFiles() {
		printSeparator();
		log.info("Compiling files...");
		int count = 0;
		for (FileExtensionCompilerList list : this.compilerLists) {
			List<Path> paths = this.fileFinder
					.getPaths(list.getFileExtension());

			for (Compiler c : list) {
				int ccount = 0;
				for (Path path : paths) {
					c.compile(path);
					count++;
					ccount++;
				}
				log.info("Compiled {} files with {}.", ccount, c.getClass()
						.getSimpleName());
			}
		}
		log.info("Totally compiled {} files!", count);
	}

	private void assembly() {
		printSeparator();
		log.info("Assembling files together...");
	}

	private void archive() {
		printSeparator();
		log.info("Archiving assebled files to zip file...");
		log.info("File name: {}", this.project.getTarget().toString());

		try {
			int count = 0;
			ZipArchive zipper = new ZipArchive(this.project.getSrc()
					.toAbsolutePath(), this.project.getTarget().toFile());
			for (Path path : this.fileFinder.getPaths()) {
				if (!isFiltered(path)) {
					zipper.addFile(path);
					count++;
				}
			}
			zipper.close();
			log.info("Created archive with {} files!", count);
		} catch (Exception e) {
			throw new BuildError(e);
		}
	}

	private boolean isFiltered(Path path) {
		for (String endFilter : this.project.getFilters()) {
			if (path.toString().endsWith(endFilter)) {
				return true;
			}
		}
		return false;
	}

	private void saveFile(GeneratedFile file) {
		try {
			Files.write(
					this.project.getSrc().resolve(
							Paths.get(file.getName()).toAbsolutePath()),
					file.getContents(), StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING,
					StandardOpenOption.WRITE);
		} catch (IOException e) {
			throw new BuildError(e);
		}
	}

	public void addBuildStep(BuildStep buildStep) {
		if (buildStep instanceof CompileBuildStep) {
			this.addCompileStep(((CompileBuildStep) buildStep).getCompiler(),
					((CompileBuildStep) buildStep).getFileTypes());
		} else if (buildStep instanceof GenerateBuildStep) {
			this.addGenerateStep(((GenerateBuildStep) buildStep).getGenerator());
		} else {
			// Unsupported type.
			log.warn("Tried to register unsupported build step: {}", buildStep
					.getClass().getSimpleName());
		}
	}

	private void addCompileStep(Compiler compiler, String... fileExtensions) {
		for (String fileExtension : fileExtensions) {
			this.addCompileStep(compiler, fileExtension);
		}
	}

	private void addCompileStep(Compiler compiler, String fileExtension) {
		FileExtensionCompilerList compilerList = findBuilder(fileExtension);
		if (compilerList == null) {
			FileExtensionCompilerList newList = new FileExtensionCompilerList(
					fileExtension);
			compiler.setAssembler(this);
			newList.add(compiler);
			this.compilerLists.add(newList);
		} else {
			compilerList.add(compiler);
		}
	}

	private void addGenerateStep(Generator generator) {
		generator.setAssembler(this);
		this.generators.add(generator);
	}

	private FileExtensionCompilerList findBuilder(String fileExtension) {
		for (FileExtensionCompilerList b : this.compilerLists) {
			if (b.getFileExtension().equalsIgnoreCase(fileExtension)) {
				return b;
			}
		}
		return null;
	}

	public Project getProject() {
		return this.project;
	}

	public Charset getCharset() {
		return this.project.getCharset();
	}

	public Path getSourcePath() {
		return this.project.getSrc();
	}
}
