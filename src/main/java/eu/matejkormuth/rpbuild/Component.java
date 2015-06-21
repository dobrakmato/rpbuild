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
package eu.matejkormuth.rpbuild;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.matejkormuth.rpbuild.api.Project;
import eu.matejkormuth.rpbuild.api.Setting;
import eu.matejkormuth.rpbuild.configuration.xml.XmlSetting;
import eu.matejkormuth.rpbuild.exceptions.InvalidSettingsError;

/**
 * Abstract class that represents build system component. Contains some useful
 * methods.
 * 
 * @see Compiler
 * @see Generator
 */
public abstract class Component {
	// Logger object by default.
	protected final Logger log;

	private Assembler assembler;
	private Setting[] settings;

	public Component() {
		log = LoggerFactory.getLogger(this.getClass());
	}

	void setAssembler(Assembler assembler) {
		if (this.assembler != null) {
			throw new IllegalAccessError(
					"Assembler has been already set for this component!");
		}
		this.assembler = assembler;
	}

	void setSettings(Setting[] settings) {
		if (this.settings != null) {
			throw new IllegalAccessError(
					"Settings have been already set for this component!");
		}
		this.settings = settings;
	}

	// This method should be overridden by concrete classes.
	/**
	 * Initializes component's settings and state. <b>Should be overridden</b>.
	 * 
	 * @throws InvalidSettingsError
	 *             when settings are invalid
	 */
	public void onInit() throws InvalidSettingsError {
	}

	public Logger getLogger() {
		return log;
	}

	/**
	 * Returns current build options.
	 * 
	 * @return options of the current build
	 */
	public Project getProject() {
		return this.assembler.getProject();
	}

	/**
	 * Returns Charset used for this build.
	 * 
	 * @return build charset
	 */
	public Charset getCharset() {
		return this.assembler.getCharset();
	}

	/**
	 * Returns absolute path from resource pack root and relative path string.
	 * 
	 * @param relative
	 *            relative path as string
	 * @return absolute Path
	 */
	public Path getPath(String relative) {
		return this.assembler.getSourcePath().resolve(Paths.get(relative));
	}

	/**
	 * Returns value of setting specified by key or null if specified setting is
	 * not present.
	 * 
	 * @param key
	 *            key of setting
	 * @return XmlSetting for specified key or null if setting not present
	 */
	public Setting getSetting(String key) {
		for (Setting setting : this.settings) {
			if (setting.getKey().equals(key)) {
				return setting;
			}
		}
		return null;
	}

	/**
	 * Returns value of setting specified by key or default value if specified
	 * setting is not present.
	 * 
	 * @param key
	 *            key of setting
	 * @param defaultValue value that should be used when there is no value specificated
	 *           
	 * @return XmlSetting for specified key or default value if setting not
	 *         present
	 */
	public Setting getSetting(String key, String defaultValue) {
		for (Setting setting : this.settings) {
			if (setting.getKey().equals(key)) {
				return setting;
			}
		}
		return new XmlSetting(key, defaultValue);
	}
}
