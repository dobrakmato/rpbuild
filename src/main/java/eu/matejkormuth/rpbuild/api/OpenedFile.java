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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents file with it's content loaded in memory
 * (there is also lazy-load version see OpenedFile#lazyLoaded(Path) ).
 *
 * @see #lazyLoaded(Path)
 */
@Slf4j
@EqualsAndHashCode
@ToString
public class OpenedFile {

    /**
     * Absolute path to file on disk (may not exists).
     */
    @Getter
    @Setter
    protected Path absolutePath;

    /**
     * Name of file.
     */
    @Getter
    @Setter
    protected String name;

    /**
     * Contents of file.
     */
    @Getter
    protected byte[] data;

    // Whether the file has been changed.
    protected boolean dirty = false;

    /**
     * Sets data of this file.
     *
     * @param data data
     */
    public void setData(byte[] data) {
        this.data = data;

        // Mark dirty flag, so the file will be saved!
        this.dirty = true;
    }

    /**
     * Name or relative path of this file.
     *
     * @param name name of file
     */
    public OpenedFile(String name) {
        this.name = name;
    }

    protected OpenedFile(Path absolutePath, byte[] data) {
        this.absolutePath = absolutePath;
        this.data = data;
        this.name = absolutePath.getFileName().toString();
    }

    /**
     * Represents OpenedFile with no content loaded in memory. Content is loaded the first time is getContent() called.
     *
     * @param absolutePath path to file
     * @return OpenedFile instance
     */
    public static OpenedFile lazyLoaded(Path absolutePath) {

        // Return custom version of opened file.
        return new OpenedFile(absolutePath, null) {
            @Override
            public byte[] getData() {
                if (this.data == null) {
                    try {
                        load0();
                    } catch (IOException e) {
                        log.error("Can't lazy load content of file" + this.absolutePath.toString() + " !", e);
                    }
                }
                return this.data;
            }

            private void load0() throws IOException {
                this.data = Files.readAllBytes(this.absolutePath);
            }
        };
    }

    public boolean isDirty() {
        return dirty;
    }
}
