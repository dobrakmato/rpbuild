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

public abstract class BuildStep {

	public static BuildStep compile(Compiler compiler, String fileType) {
		return new CompileBuildStep(compiler, fileType);
	}

	public static BuildStep compile(Compiler compiler, String... fileTypes) {
		return new CompileBuildStep(compiler, fileTypes);
	}

	public static BuildStep generate(Generator generator) {
		return new GenerateBuildStep(generator);
	}

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
