package eu.matejkormuth.rpbuild.configuration.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import eu.matejkormuth.rpbuild.Generator;
import eu.matejkormuth.rpbuild.api.BuildStepGenerate;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlBuildStepGenerate extends XmlBuildStep implements
		BuildStepGenerate {
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

	@Override
	public Generator getGenerator() throws Exception {
		Object obj = this.getComponentClass().getConstructor().newInstance();
		if (obj instanceof Generator) {
			return (Generator) obj;
		} else {
			throw new RuntimeException("Class '" + this.getComponentClassName()
					+ "' is not a Generator class!");
		}
	}
}
