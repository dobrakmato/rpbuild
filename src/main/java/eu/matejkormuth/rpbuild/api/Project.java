package eu.matejkormuth.rpbuild.api;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

import eu.matejkormuth.rpbuild.configuration.xml.XmlBuildStep;

public interface Project {

	public abstract String getProjectName();

	public abstract String getEncoding();

	public abstract Charset getCharset();

	public abstract boolean isGitPull();

	public abstract boolean isIgnoreGitFolders();

	public abstract Path getSrc();

	public abstract Path getTarget();

	public abstract XmlBuildStep[] getBuild();
	
	public abstract List<String> getFilters();

}