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
package eu.matejkormuth.rpbuild.compilers;

import java.nio.file.Files;

import org.json.JSONObject;

import eu.matejkormuth.rpbuild.Compiler;
import eu.matejkormuth.rpbuild.OpenedFile;
import eu.matejkormuth.rpbuild.exceptions.BuildError;

public class ModelValidator extends Compiler {

	@Override
	public void compile(OpenedFile file) throws BuildError {
		JSONObject model = new JSONObject(new String(file.getContents(),
				this.getCharset()));
		boolean failed = false;

		failed |= failParticleTex(file, model);

		if (failed) {
			log.error("Model " + file.getPath().toString()
					+ " has some errors!");
		}
	}

	private boolean failParticleTex(OpenedFile file, JSONObject model) {
		JSONObject texturesObj = model.getJSONObject("textures");
		if (texturesObj.has("particle")) {
			String possiblePath = "assets/minecraft/textures/"
					+ texturesObj.getString("particle");
			boolean exists = Files.exists(this.getPath(possiblePath + ".png"))
					|| Files.exists(this.getPath(possiblePath + ".jpg"));
			if (!exists) {
				log.error("Model " + file.getPath().toString()
						+ " has declared non-existing particle texture '"
						+ possiblePath + "'!");
				return true;
			}
			return false;
		} else {
			log.error("Model " + file.getPath().toString()
					+ " is missing particle texture definition!");
			return true;
		}
	}

}
