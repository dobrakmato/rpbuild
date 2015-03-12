/*
 *  rpbuild - RPBuild is a build system for Minecraft resource packs.
 *  Copyright (C) 2015 Matej Kormuth 
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  "Minecraft" is a trademark of Mojang AB
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