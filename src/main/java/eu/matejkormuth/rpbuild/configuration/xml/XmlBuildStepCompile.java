package eu.matejkormuth.rpbuild.configuration.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import eu.matejkormuth.rpbuild.BuildError;
import eu.matejkormuth.rpbuild.Compiler;
import eu.matejkormuth.rpbuild.api.BuildStepCompile;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlBuildStepCompile extends BuildStepCompile {

	@XmlAttribute(name = "class")
	protected String clazz;

	protected transient Class<?> clazzObj;

	@XmlAttribute(name = "files")
	private String fileType;

	public XmlBuildStepCompile() {
	}

	public XmlBuildStepCompile(Class<?> compilerClass, String fileType) {
		this.clazz = compilerClass.getCanonicalName();
		this.clazzObj = compilerClass;
		this.fileType = fileType;
	}

	public XmlBuildStepCompile(String compilerClass, String fileType) {
		this.clazz = compilerClass;
		this.fileType = fileType;
	}

	public String getFileType() {
		return fileType;
	}

	@Override
	public Compiler getCompiler() throws Exception {
		Object obj = this.getComponentClass().getConstructor().newInstance();
		if (obj instanceof Compiler) {
			return (Compiler) obj;
		} else {
			throw new RuntimeException("Class '" + this.getComponentClassName()
					+ "' is not a Compiler class!");
		}
	}

	@Override
	public String[] getFileTypes() {
		return new String[] { this.getFileType() };
	}

	public String getComponentClassName() {
		return clazz;
	}

	public Class<?> getComponentClass() {
		if (clazzObj == null) {
			try {
				return (clazzObj = Class.forName(this.clazz));
			} catch (ClassNotFoundException e) {
				throw new BuildError(e);
			}
		} else {
			return clazzObj;
		}
	}
}
