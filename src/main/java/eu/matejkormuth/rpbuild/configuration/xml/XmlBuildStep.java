package eu.matejkormuth.rpbuild.configuration.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;

import eu.matejkormuth.rpbuild.BuildError;
import eu.matejkormuth.rpbuild.api.BuildStep;

@XmlSeeAlso({ XmlBuildStepCompile.class, XmlBuildStepGenerate.class })
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class XmlBuildStep implements BuildStep {
	@XmlAttribute(name = "class")
	protected String clazz;

	protected transient Class<?> clazzObj;

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
