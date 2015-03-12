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
package eu.matejkormuth.rpbuild.configuration.xml;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import eu.matejkormuth.rpbuild.api.BuildStep;
import eu.matejkormuth.rpbuild.api.Project;
import eu.matejkormuth.rpbuild.compilers.JsonCompressor;
import eu.matejkormuth.rpbuild.generators.PackMcmetaGenerator;

@XmlRootElement(name = "project")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlProject implements Project {
	@XmlElement
	protected String name = "ResourcePack";
	@XmlElement
	protected String encoding = "UTF-8";
	protected transient Charset charset = Charset.forName(this.encoding);
	@XmlElement
	protected boolean gitPull = false;
	@XmlElement
	protected boolean ignoreGitFolders = true;
	@XmlElement
	protected Path src = Paths.get(".");
	@XmlElement
	protected Path target = Paths.get("latest.zip");
	@XmlElements({
			@XmlElement(name = "generate", type = XmlBuildStepGenerate.class),
			@XmlElement(name = "compile", type = XmlBuildStepCompile.class) })
	@XmlElementWrapper(name = "build")
	protected BuildStep[] build = new BuildStep[] {
			new XmlBuildStepGenerate(PackMcmetaGenerator.class),
			new XmlBuildStepCompile(JsonCompressor.class, ".json") };
	@XmlElement
	@XmlElementWrapper(name = "filters")
	protected String[] filter = new String[] { "rpbuild.xml", ".jar", ".zip" };

	public XmlProject() {
	}

	public XmlProject(String name, String encoding, Charset charset,
			boolean gitPull, boolean ignoreGitFolders, Path src, Path target,
			BuildStep[] build) {
		this.name = name;
		this.encoding = encoding;
		this.charset = charset;
		this.gitPull = gitPull;
		this.ignoreGitFolders = ignoreGitFolders;
		this.src = src;
		this.target = target;
		this.build = build;
	}

	@Override
	public String getProjectName() {
		return name;
	}

	@Override
	public String getEncoding() {
		return encoding;
	}

	@Override
	public Charset getCharset() {
		if (charset == null) {
			charset = Charset.forName(this.encoding);
		}

		return charset;
	}

	@Override
	public boolean isGitPull() {
		return gitPull;
	}

	@Override
	public boolean isIgnoreGitFolders() {
		return ignoreGitFolders;
	}

	@Override
	public Path getSrc() {
		return src;
	}

	@Override
	public Path getTarget() {
		return target;
	}

	@Override
	public BuildStep[] getBuild() {
		return build;
	}

	@Override
	public List<String> getFilters() {
		return Arrays.asList(this.filter);
	}

	public void setBuild(BuildStep[] build) {
		this.build = build;
	}
}
