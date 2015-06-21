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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import eu.matejkormuth.rpbuild.Generator;
import eu.matejkormuth.rpbuild.api.BuildStepGenerate;
import eu.matejkormuth.rpbuild.api.Setting;
import eu.matejkormuth.rpbuild.exceptions.InvalidComponentError;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlBuildStepGenerate implements BuildStepGenerate {
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
	public Generator getGenerator() throws InvalidComponentError {
		try {
			Object obj = this.getComponentClass().getConstructor()
					.newInstance();
			if (obj instanceof Generator) {
				return (Generator) obj;
			} else {
				throw new InvalidComponentError("Class '"
						+ this.getComponentClassName()
						+ "' is not a Generator class!");
			}
		} catch (Exception e) {
			throw new InvalidComponentError(e);
		}
	}

	public String getComponentClassName() {
		return clazz;
	}

	public Class<?> getComponentClass() throws InvalidComponentError {
		if (clazzObj == null) {
			try {
				return (clazzObj = Class.forName(this.clazz));
			} catch (ClassNotFoundException e) {
				throw new InvalidComponentError(e);
			}
		} else {
			return clazzObj;
		}
	}

	public Setting[] getSettings() {
		return settings;
	}
}
