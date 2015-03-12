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
package eu.matejkormuth.rpbuild;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.matejkormuth.rpbuild.api.Project;
import eu.matejkormuth.rpbuild.configuration.xml.XmlSetting;

/**
 * Abstract class that represents build system component. Contains some useful
 * methods.
 * 
 * @see Compiler
 * @see Generator
 */
public abstract class Component {
	protected static Logger log;

	private Assembler assembler;
	private XmlSetting[] settings;

	public Component() {
		log = LoggerFactory.getLogger(this.getClass());
	}
	
	public void init() {
	}

	protected void setAssembler(Assembler assembler) {
		if (this.assembler != null) {
			throw new IllegalAccessError(
					"Assembler has been already set for this component!");
		}
		this.assembler = assembler;
	}

	protected void setSettings(XmlSetting[] settings) {
		if (this.settings != null) {
			throw new IllegalAccessError(
					"Settings have been already set for this component!");
		}
		this.settings = settings;
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
	 * Returns absolute path from resourcepack root and relative path string.
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
	public XmlSetting getSetting(String key) {
		for (XmlSetting setting : this.settings) {
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
	 * @return XmlSetting for specified key or default value if setting not
	 *         present
	 */
	public XmlSetting getSetting(String key, String defaultValue) {
		for (XmlSetting setting : this.settings) {
			if (setting.getKey().equals(key)) {
				return setting;
			}
		}
		return new XmlSetting(key, defaultValue);
	}
}
