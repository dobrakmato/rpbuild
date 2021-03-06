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

import eu.matejkormuth.rpbuild.Compiler;
import eu.matejkormuth.rpbuild.OpenedFile;
import eu.matejkormuth.rpbuild.exceptions.BuildError;

public class JsonCommenter extends Compiler {

	private String comment;

	@Override
	public void onInit() {
		this.comment = this.getSetting("comment",
				"Please set comment message in your build config.").getValue();

		log.warn("We are sorry, but JsonCommenter is broken atm, please do not use it!");
	}

	@Override
	public void compile(OpenedFile file) throws BuildError {
		if (this.getSetting("formatted", "false").equals("true")) {
			this.commentFormatted(file);
		} else {
			this.commentUnformatted(file);
		}
	}

	private void commentUnformatted(OpenedFile file) {
		String contents = new String(file.getContents(), this.getCharset());
		// { "__comment" : "comment value",
		contents = contents.replaceFirst("\\{", "{\"__comment\":\""
				+ this.comment + "\",");
		file.setContents(contents.getBytes(this.getCharset()));
	}

	private void commentFormatted(OpenedFile file) {
		// Until someone starts complaining.
		this.commentUnformatted(file);
	}
}
