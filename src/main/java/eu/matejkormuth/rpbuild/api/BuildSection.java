/**
 * rpBuild 2 - Improved build system for Minecraft resource packs.
 * Copyright (c) 2015 - 2016, Matej Kormuth <http://www.github.com/dobrakmato>
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
package eu.matejkormuth.rpbuild.api;

import eu.matejkormuth.rpbuild.Application;
import lombok.Data;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

/**
 * Represents build section (folder select).
 */
@Data
public class BuildSection {

    /**
     * Name of folder or path (separated by dots).
     */
    private String name;

    /**
     * Parent build section. For top level build section, this is null.
     */
    private BuildSection parent;

    /**
     * File exclude rules.
     */
    private List<PathMatcher> exclude;

    /**
     * Plugins that should run on files in this section.
     */
    private List<PluginConfiguration> plugins;

    /**
     * Children sections (sub-directory selects).
     */
    private List<BuildSection> children;
    private Object project;

    /**
     * Returns absolute path to this build section on disk drive.
     *
     * @return absolute path
     */
    public Path getAbsolutePath() {
        if (this.parent == null) {
            return Application.resolve(Project.class).getSource();
        } else {
            return this.parent.getAbsolutePath().resolve(name.replace('.', '/'));
        }
    }

}
