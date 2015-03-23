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
package eu.matejkormuth.rpbuild.api;

import eu.matejkormuth.rpbuild.FileGenerator;
import eu.matejkormuth.rpbuild.LegacyBuildStep;

/**
 * {@link LegacyBuildStep} that represents <b>generate</b> build phase in which
 * the files are generated using {@link FileGenerator}s.
 */
public abstract class BuildStepGenerate extends BuildStep {
	public abstract FileGenerator getGenerator() throws Exception;
}
