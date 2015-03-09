package eu.matejkormuth.rpbuild.configuration.xml;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.matejkormuth.rpbuild.api.BuildStep;
import eu.matejkormuth.rpbuild.api.Project;
import eu.matejkormuth.rpbuild.compilers.JsonCompressor;
import eu.matejkormuth.rpbuild.generators.PackMcMetaGenerator;

@XmlRootElement(name = "project")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlProject implements Project {
	@XmlElement
	private String name = "ResourcePack";
	@XmlElement
	private String encoding = "UTF-8";
	@XmlElement
	private transient Charset charset = Charset.forName(this.encoding);
	@XmlElement
	private boolean gitPull = false;
	@XmlElement
	private boolean ignoreGitFolders = true;
	@XmlElement
	private Path src = Paths.get(".");
	@XmlElement
	private Path target = Paths.get("latest.zip");
	@XmlElement
	private BuildStep[] build = new XmlBuildStep[] {
			new XmlBuildStepGenerate(PackMcMetaGenerator.class),
			new XmlBuildStepCompile(JsonCompressor.class, ".json") };
	@XmlElement
	private String[] filter = new String[] {};

	public XmlProject() {
	}

	public XmlProject(String name, String encoding, Charset charset,
			boolean gitPull, boolean ignoreGitFolders, Path src, Path target,
			XmlBuildStep[] build) {
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
}
