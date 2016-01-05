/**
 * rpBuild 2 - Improved build system for Minecraft resource packs.
 * Copyright (c) 2015 - 2016, Matej Kormuth <http://www.github.com/dobrakmato>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * <p>
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
 * <p>
 * "Minecraft" is a trademark of Mojang AB
 */
package eu.matejkormuth.rpbuild.plugins;

import com.typesafe.config.Config;
import eu.matejkormuth.rpbuild.Application;
import eu.matejkormuth.rpbuild.api.OpenedFile;
import eu.matejkormuth.rpbuild.api.Plugin;
import eu.matejkormuth.rpbuild.api.PluginType;
import eu.matejkormuth.rpbuild.api.Project;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Generates standard resource pack descriptor required by
 * Minecraft client (pack.mcmeta).
 */
public class PackMcMetaPlugin extends Plugin {

    private static final String NAME = "rpbuild-packmcmeta-plugin";
    private static final String VERSION = "1.0";
    private static final String AUTHOR = "Matej Kormuth";

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
    public PluginType getType() {
        return PluginType.GENERATE_BEFORE_LIST;
    }

    @Override
    public List<OpenedFile> generate(Config config) {
        Project project = Application.resolve(Project.class);

        String description = project.getName() + " - " + new SimpleDateFormat().format(new Date());

        if (config.hasPath("description")) {
            description = config.getString("description");
        }

        JSONObject json = new JSONObject();
        JSONObject pack = new JSONObject();

        pack.put("pack_format", 1);
        pack.put("description", description);
        json.put("pack", pack);

        OpenedFile packMcMeta = new OpenedFile("pack.mcmeta");
        packMcMeta.setData(json.toString(2).getBytes(project.getEncoding()));

        return Collections.singletonList(packMcMeta);
    }
}
