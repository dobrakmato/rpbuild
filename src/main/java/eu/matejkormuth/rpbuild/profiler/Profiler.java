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
package eu.matejkormuth.rpbuild.profiler;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Profiler {

    private final String name;
    private final ArrayList<Profiler> children = new ArrayList<>();
    private Profiler parent;
    private long total = 0;
    private long count = 0;
    private long lastStart = 0;

    public Profiler(String name) {
        this.name = name;
    }

    private Profiler(String name, Profiler parent) {
        this.name = name;
        this.parent = parent;
    }

    public Profiler createChild(String childName) {
        Profiler child = new Profiler(childName, this);
        children.add(child);
        return child;
    }

    /**
     * Starts counting time.
     */
    public void begin() {
        lastStart = System.nanoTime();
    }

    /**
     * Stops counting time.
     */
    public void end() {
        total += System.nanoTime() - lastStart;
        count++;
    }

    public String generateReport() {
        return generateReport0(0, null).toString();
    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SSS");
    private static final DecimalFormat df = new DecimalFormat("##.##%");

    private StringBuilder generateReport0(int level, StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        } else {
            sb.append('\n');
        }

        for (int i = 0; i < level; i++) {
            sb.append(' ');
        }

        sb.append(this.name).append(' ');
        sb.append(sdf.format(new Date(total / 1_000_000)));
        if (parent == null) {
            sb.append(' ').append("100%");
        } else {
            double r = (double) getTotal() / (double) getParent().getTotal();
            sb.append(' ').append(df.format(r));
        }

        for (Profiler profiler : children) {
            profiler.generateReport0(level + 1, sb);
        }

        return sb;
    }

    public long getTotal() {
        return total;
    }

    public long getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public Profiler getParent() {
        return parent;
    }
}
