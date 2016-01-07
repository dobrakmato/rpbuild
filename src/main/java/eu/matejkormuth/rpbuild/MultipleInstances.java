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
package eu.matejkormuth.rpbuild;

import eu.matejkormuth.rpbuild.api.OpenedFile;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultipleInstances {
    private final Semaphore semaphore;
    private final ExecutorService executors;

    public MultipleInstances(String threadNames, int maxInstances) {
        semaphore = new Semaphore(maxInstances);
        executors = Executors.newFixedThreadPool(maxInstances, new LocalThreadFactory(threadNames));
    }

    private void work0(Executable work, OpenedFile file) {
        try {
            work.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Release this thread.
        semaphore.release();
    }

    public void sendWait(OpenedFile file, Executable work) {
        try {
            // Wait for worker thread.
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            // Submit to executor (probably the one empty thread).
            executors.submit(() -> work0(work, file)).get();
        } catch (ExecutionException | InterruptedException e) {
            // Propagate exceptions.
            throw new RuntimeException(e);
        }
    }

    private static class LocalThreadFactory implements ThreadFactory {
        private final AtomicInteger counter;
        private final String threadNames;

        public LocalThreadFactory(String threadNames) {
            this.threadNames = threadNames;
            counter = new AtomicInteger();
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(threadNames.replace("%d", Integer.toString(counter.incrementAndGet())));
        }
    }
}
