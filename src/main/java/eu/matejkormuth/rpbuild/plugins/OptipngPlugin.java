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
package eu.matejkormuth.rpbuild.plugins;

import com.typesafe.config.Config;
import eu.matejkormuth.rpbuild.annotations.PluginDocumentation;
import eu.matejkormuth.rpbuild.annotations.PluginOption;
import eu.matejkormuth.rpbuild.api.OpenedFile;
import eu.matejkormuth.rpbuild.api.Plugin;
import eu.matejkormuth.rpbuild.api.PluginType;
import eu.matejkormuth.rpbuild.exceptions.InvalidConfigurationException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Provides support for optipng application.
 */
@Slf4j
@PluginDocumentation(
        name = "rpbuild-optipng-plugin",
        author = "Matej Kormuth",
        version = "1.0",
        description = "OptiPNG wrapper as rpbuild plugin. Loselessly compresses PNGs.",
        options = {
                @PluginOption(
                        name = "level",
                        decription = "Optimization level for optipng (0-7), defaults to 2."
                )
        }
)
public class OptipngPlugin extends Plugin {

    private static final String NAME = "rpbuild-optipng-plugin";
    private static final String VERSION = "1.0";
    private static final String AUTHOR = "Matej Kormuth";

    private static final String WARN = "Uses original OptiPNG software by Cosmin Truta and the Contributing Authors.";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public String getGlobPattern() {
        return "**.png";
    }

    @Override
    public PluginType getType() {
        return PluginType.TRANSFORM_FILES;
    }

    @Override
    public void initialize() {
        // Print warn message.
        log.info(WARN);

        // Extract executables.
    }

    @Override
    public void transform(Config config, OpenedFile file) {

        int optimizationLevel = 2;

        if (config.hasPath("level")) {
            optimizationLevel = config.getInt("level");
            if (optimizationLevel > 7 || optimizationLevel < 0) {
                throw new InvalidConfigurationException("Level for optipng must be between 0-7 (including).");
            }
        }

        // Start optipng process.
        try {
            Process process = new ProcessBuilder("optipng", "-o" + optimizationLevel, "\"" + file.getAbsolutePath().toString() + "\"")
                    .inheritIO()
                    .start();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            log.error("Error while executing optipng!", e);
        }
    }
}
