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

/**
 * Wraps arguments as object.
 */
public class Args {
    private final String[] array;
    private int index;

    public Args(String[] array) {
        this.array = array;
        this.index = 0;
    }

    public boolean hasFlag(String... flag) {
        for (String oneFlags : flag) {
            for (String arg : array) {
                if (arg.equalsIgnoreCase(oneFlags)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getParam(String name) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(name)) {
                return array[i + 1];
            }
        }
        return null;
    }

    /**
     * No more args?
     *
     * @return true or false
     */
    public boolean empty() {
        return array.length == index;
    }

    /**
     * Next arg as string.
     *
     * @return next arg
     */
    public String nextString() {
        return this.array[index++];
    }

    /**
     * Next arg as int.
     *
     * @return next arg
     */
    public int nextInt() {
        return Integer.valueOf(nextString());
    }
}