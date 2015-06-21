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
	@XmlElement(defaultValue = "9")
	protected int compressionLevel;
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
	public int getCompressionLevel() {
		return this.compressionLevel;
	}
	
	@Override
	public List<String> getFilters() {
		return Arrays.asList(this.filter);
	}

	public void setBuild(BuildStep[] build) {
		this.build = build;
	}
	
	public void setGitPull(boolean gitPull) {
		this.gitPull = gitPull;
	}
	
}
