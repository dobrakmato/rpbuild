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
package eu.matejkormuth.rpbuild.concurrent;

import com.typesafe.config.Config;
import eu.matejkormuth.rpbuild.api.OpenedFile;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Provides support for multithreading.
 */
@Slf4j
public class Multithreading {

    /**
     * Count of worker threads.
     */
    private final int workerCount;

    /**
     * Template for worker thread name.
     */
    private final String workerName;

    /**
     * Workers.
     */
    private Thread[] workers;

    /**
     * Work queue.
     */
    private final LinkedBlockingQueue<Executable> queue = new LinkedBlockingQueue<>();

    /**
     * Function used to transform one file.
     */
    private final Transformer transformer;

    /**
     * Creates new multithreading support with specified amount of worker threads and transformer function.
     *
     * @param workerCount amount of worker threads
     * @param workerName  template for name of worker thread
     * @param transformer function used to transform one file
     */
    public Multithreading(int workerCount, String workerName, Transformer transformer) {
        this.workerCount = workerCount;
        this.workerName = workerName;
        this.transformer = transformer;
    }

    /**
     * Processes all files in multiple threads.
     *
     * @param config config
     * @param files  list of files
     * @throws Exception when exception occurs
     */
    public void processAll(Config config, List<OpenedFile> files) throws Exception {
        // Fill the queue.
        for (OpenedFile file : files) {
            queue.offer(() -> {
                // Do the work.
                transformer.transform(config, file);
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

        // Wait until queue is empty.
        synchronized (queue) {
            while (!queue.isEmpty())
                queue.wait();
        }

        // Wait for workers to end.
        for (int i = 0; i < workerCount; i++) {
            if (workers[i] != null) {
                workers[i].join();
                // We are sure that worker thread is already dead.
            }
            workers[i] = null;
        }
        workers = null;
    }
}
