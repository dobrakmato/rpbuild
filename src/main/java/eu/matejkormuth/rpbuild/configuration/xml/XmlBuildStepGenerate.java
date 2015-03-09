package eu.matejkormuth.rpbuild.configuration.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlBuildStepGenerate extends XmlBuildStep {
	// Configuration

	public XmlBuildStepGenerate() {
	}
	
	public XmlBuildStepGenerate(Class<?> generatorClass) {
		this.clazz = generatorClass.getCanonicalName();
		this.clazzObj = generatorClass;
	}
	
	public XmlBuildStepGenerate(String generatorClass) {
		this.clazz = generatorClass;
	}
}
