/**
 * Minecraft resource pack compiler and assembler - rpBuild - Build system for Minecraft resource packs.
 * Copyright (c) 2015, Matej Kormuth <http://www.github.com/dobrakmato>
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents list of {@link Compiler} associated to one file type.
 */
public class CompilerListByFileExtension implements List<Compiler> {
	private String fileExtension;
	private List<Compiler> compilers;

	public CompilerListByFileExtension(String fileExtension) {
		this.compilers = new ArrayList<Compiler>();
		this.fileExtension = fileExtension;
	}

	public int size() {
		return compilers.size();
	}

	public boolean isEmpty() {
		return compilers.isEmpty();
	}

	public boolean contains(Object o) {
		return compilers.contains(o);
	}

	public Iterator<Compiler> iterator() {
		return compilers.iterator();
	}

	public Object[] toArray() {
		return compilers.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return compilers.toArray(a);
	}

	public boolean add(Compiler e) {
		return compilers.add(e);
	}

	public boolean remove(Object o) {
		return compilers.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return compilers.containsAll(c);
	}

	public boolean addAll(Collection<? extends Compiler> c) {
		return compilers.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends Compiler> c) {
		return compilers.addAll(index, c);
	}

	public boolean removeAll(Collection<?> c) {
		return compilers.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return compilers.retainAll(c);
	}

	public void clear() {
		compilers.clear();
	}

	public boolean equals(Object o) {
		return compilers.equals(o);
	}

	public int hashCode() {
		return compilers.hashCode();
	}

	public Compiler get(int index) {
		return compilers.get(index);
	}

	public Compiler set(int index, Compiler element) {
		return compilers.set(index, element);
	}

	public void add(int index, Compiler element) {
		compilers.add(index, element);
	}

	public Compiler remove(int index) {
		return compilers.remove(index);
	}

	public int indexOf(Object o) {
		return compilers.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		return compilers.lastIndexOf(o);
	}

	public ListIterator<Compiler> listIterator() {
		return compilers.listIterator();
	}

	public ListIterator<Compiler> listIterator(int index) {
		return compilers.listIterator(index);
	}

	public List<Compiler> subList(int fromIndex, int toIndex) {
		return compilers.subList(fromIndex, toIndex);
	}

	public String getFileExtension() {
		return fileExtension;
	}
}