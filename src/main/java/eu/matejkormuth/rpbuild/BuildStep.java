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

/**
 * Represents step in build. This class is abstract. You better use
 * <b>compile</b> or <b>generate</b> static methods to create new buildsteps.
 */
public abstract class BuildStep {
	/**
	 * Creates new <b>compile</b> build step with specified Compiler and file
	 * type that this compiler compiles.
	 * 
	 * @param compiler
	 *            the compiler
	 * @param fileType
	 *            file type (extension) associated with this compiler
	 * @return
	 */
	public static BuildStep compile(Compiler compiler, String fileType) {
		return new CompileBuildStep(compiler, fileType);
	}

	/**
	 * Creates new <b>compile</b> build step with specified Compiler and file
	 * type that this compiler compiles.
	 * 
	 * @param compiler
	 *            the compiler
	 * @param fileTypes
	 *            file types (extensions) associated with this compiler
	 * @return build step
	 */
	public static BuildStep compile(Compiler compiler, String... fileTypes) {
		return new CompileBuildStep(compiler, fileTypes);
	}

	/**
	 * Creates new <b>generate</b> build step with specified generator.
	 * 
	 * @param generator
	 *            the generator
	 * @return build step
	 */
	public static BuildStep generate(Generator generator) {
		return new GenerateBuildStep(generator);
	}

	/**
	 * {@link BuildStep} that represents <b>compile</b> build phase in which the
	 * specified file types are compiled using {@link Compiler}.
	 */
	public static class CompileBuildStep extends BuildStep {
		private Compiler compiler;
		private String[] fileTypes;

		public CompileBuildStep(Compiler compiler, String fileType) {
			this.compiler = compiler;
			this.fileTypes = new String[] { fileType };
		}

		public CompileBuildStep(Compiler compiler, String... fileTypes) {
			this.compiler = compiler;
			this.fileTypes = fileTypes;
		}

		public Compiler getCompiler() {
			return compiler;
		}

		public String[] getFileTypes() {
			return fileTypes;
		}
	}

	/**
	 * {@link BuildStep} that represents <b>generate</b> build phase in which
	 * the files are generated using {@link Generator}s.
	 */
	public static class GenerateBuildStep extends BuildStep {
		private Generator generator;

		public GenerateBuildStep(Generator generator) {
			this.generator = generator;
		}

		public Generator getGenerator() {
			return generator;
		}
	}
}
