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
package eu.matejkormuth.rpbuild.configuration.legacy;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import eu.matejkormuth.rpbuild.LegacyBuildStep;
import eu.matejkormuth.rpbuild.api.BuildStep;
import eu.matejkormuth.rpbuild.api.Project;
import eu.matejkormuth.rpbuild.compilers.JsonCompressor;
import eu.matejkormuth.rpbuild.generators.PackMcMetaGenerator;

/**
 * Represents options of build / build configuration.
 */
public class LegacyOptions implements Project {
	protected boolean optimizeFiles = true;
	protected String projectName = "ResourcePack";
	protected Path root = new File(".").toPath();
	protected Path zipName = Paths.get("latest.zip");
	protected String[] fileFilters = new String[] { ".php", ".zip", ".jar",
			".rpbuild", ".log" };
	protected String resourcePackDescription = null;
	protected boolean ignoreGit = true;
	protected String encoding = "UTF-8";
	protected boolean gitPull = false;

	@Override
	public String getProjectName() {
		return this.projectName;
	}

	@Override
	public String getEncoding() {
		return this.encoding;
	}

	@Override
	public Charset getCharset() {
		return Charset.forName(this.encoding);
	}

	@Override
	public boolean isGitPull() {
		return this.gitPull;
	}

	@Override
	public boolean isIgnoreGitFolders() {
		return this.ignoreGit;
	}

	@Override
	public Path getSrc() {
		return this.root;
	}

	@Override
	public Path getTarget() {
		return this.zipName;
	}

	@Override
	public BuildStep[] getBuild() {
		return new BuildStep[] {
				LegacyBuildStep.generate(new PackMcMetaGenerator()),
				LegacyBuildStep.compile(new JsonCompressor(), ".json") };
	}

	@Override
	public List<String> getFilters() {
		return Arrays.asList(this.fileFilters);
	}
}
