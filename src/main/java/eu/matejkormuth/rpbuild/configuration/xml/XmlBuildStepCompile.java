package eu.matejkormuth.rpbuild.configuration.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlBuildStepCompile extends XmlBuildStep {
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
}
