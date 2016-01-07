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
package eu.matejkormuth.rpbuild.tasks;

import eu.matejkormuth.rpbuild.UnsafeUtils;
import eu.matejkormuth.rpbuild.profiler.Profiler;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents abstract task, that can be run.
 */
public abstract class AbstractTask {

    /**
     * Provides way to communicate between tasks.
     */
    private static Map<String, Object> outputs = new HashMap<>();

    /**
     * Sets value of output (export) by key. Other tasks can access it.
     *
     * @param key    key of output
     * @param object value of output
     */
    protected static void setOutput(String key, Object object) {
        outputs.put(key, object);
    }

    /**
     * Returns output (export) set by any task.
     *
     * @param key key of output
     * @param <T> type of output
     * @return output or null, if not found
     */
    protected static <T> T getOutput(String key) {
        return UnsafeUtils.cast(outputs.get(key));
    }

    /**
     * Removes all outputs and discards internal storage.
     */
    public static void clearOutputs() {
        outputs.clear();
        outputs = null;
    }

    /**
     * Profiler of this task.
     */
    @Getter
    @Setter
    private Profiler profiler;

    /**
     * Runs this task.
     */
    public abstract void run();

}
