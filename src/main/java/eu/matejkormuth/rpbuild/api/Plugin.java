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

import com.typesafe.config.Config;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents rpbuild plugin.
 */
public abstract class Plugin {

    /**
     * Returns name / ID of this plugin.
     *
     * @return name of this plugin
     */
    public abstract String getName();

    /**
     * Returns name of author of this plugin.
     *
     * @return author of this plugin
     */
    public abstract String getAuthor();

    /**
     * Returns version of this plugin.
     *
     * @return version of this plugin
     */
    public abstract String getVersion();

    /**
     * Returns type of this plugin.
     *
     * @return type of this plugin
     */
    public abstract PluginType getType();

    /**
     * Returns glob pattern to select transformed files.
     *
     * @return glob pattern
     */
    public String getGlobPattern() {
        return "**.*";
    }

    /**
     * Override in GENERATE_BEFORE_LIST plugins.
     *
     * @param config configuration values fro build configuration
     * @return list of generated files
     */
    public List<OpenedFile> generate(Config config) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    /**
     * Override in GENERATE_AFTER_LIST plugins.
     *
     * @param config configuration values fro build configuration
     * @param files  list of all files in current directory and all subdirectories
     * @return list of generated files
     */
    public List<OpenedFile> generate(Config config, List<Path> files) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    /**
     * Override in TRANSFORM_FILES plugins.
     *
     * @param config configuration values fro build configuration
     * @param file   list of all files as OpenedFile-s in current directory and all subdirectories
     */
    public void transform(Config config, OpenedFile file) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }
}
