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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import eu.matejkormuth.rpbuild.BuildError;
import eu.matejkormuth.rpbuild.FileGenerator;
import eu.matejkormuth.rpbuild.api.BuildStepGenerate;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlBuildStepGenerate extends BuildStepGenerate {
	// Configuration

	@XmlAttribute(name = "class")
	protected String clazz;

	@XmlElement(name = "setting")
	@XmlElementWrapper(name = "settings")
	protected XmlSetting[] settings;

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
	public FileGenerator getGenerator() throws Exception {
		Object obj = this.getComponentClass().getConstructor().newInstance();
		if (obj instanceof FileGenerator) {
			return (FileGenerator) obj;
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

	public XmlSetting[] getSettings() {
		return settings;
	}
}
