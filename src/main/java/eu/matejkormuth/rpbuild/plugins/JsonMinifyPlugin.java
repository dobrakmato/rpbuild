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
import eu.matejkormuth.rpbuild.Application;
import eu.matejkormuth.rpbuild.api.OpenedFile;
import eu.matejkormuth.rpbuild.api.Plugin;
import eu.matejkormuth.rpbuild.api.PluginType;
import eu.matejkormuth.rpbuild.api.Project;
import eu.matejkormuth.rpbuild.concurrent.Executable;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Minifies json files by removing whitespace.
 */
@Slf4j
public class JsonMinifyPlugin extends Plugin {

    private static final String NAME = "rpbuild-jsonminify-plugin";
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
    public String getGlobPattern() {
        return "**.json";
    }

    @Override
    public PluginType getType() {
        return PluginType.TRANSFORM_FILES;
    }

    @Override
    public void transform(Config config, OpenedFile file) {
        Project project = Application.resolve(Project.class);

        file.setData(new String(file.getData(), project.getEncoding())
                .replaceAll("\\s+", "").getBytes(project.getEncoding()));
    }

    private final int workerCount = 8;
    private final String workerName = "JsonMinifyWorker-%d";
    private Thread[] workers;
    private final LinkedBlockingQueue<Executable> queue = new LinkedBlockingQueue<>();

    @Override
    public void transformAll(Config config, List<OpenedFile> files) throws Exception {
        // Fill the queue.
        for (OpenedFile file : files) {
            queue.offer(() -> {
                // Do the work.
                transform(config, file);
                // Notify control thread.
                synchronized (queue) {
                    if (queue.isEmpty())
                        queue.notify();
                }
            });
        }

        // Spawn workers if not done already.
        if (workers == null) {
            workers = new Thread[workerCount];
            for (int i = 0; i < workerCount; i++) {
                workers[i] = new Thread(() -> {
                    while (!queue.isEmpty()) {
                        try {
                            Executable executable = queue.take();
                            executable.run();
                        } catch (InterruptedException e) {
                            // Be silent about this one.
                        } catch (Exception e) {
                            log.error("Exception in thread " + Thread.currentThread().getName(), e);
                        }
                    }
                }, workerName.replace("%d", Integer.toString(i, 10)));
                workers[i].start();
            }
        }

        // Wait until done.
        synchronized (queue) {
            while (!queue.isEmpty())
                queue.wait();
        }

        // Destroy workers.
        for (int i = 0; i < workerCount; i++) {
            if (workers[i] != null) {
                if (workers[i].isAlive()) {
                    workers[i].interrupt();
                }
            }
            workers[i] = null;
        }
    }
}
