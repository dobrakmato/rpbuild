package eu.matejkormuth.rpbuild.configuration.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import eu.matejkormuth.rpbuild.BuildError;
import eu.matejkormuth.rpbuild.Generator;
import eu.matejkormuth.rpbuild.api.BuildStepGenerate;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlBuildStepGenerate extends BuildStepGenerate {
	// Configuration

	@XmlAttribute(name = "class")
	protected String clazz;

	protected transient Class<?> clazzObj;

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
