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

public class FileExtensionCompilerList implements List<Compiler> {
	private String fileExtension;
	private List<Compiler> builders;

	public FileExtensionCompilerList(String fileExtension) {
		this.builders = new ArrayList<Compiler>();
		this.fileExtension = fileExtension;
	}

	public int size() {
		return builders.size();
	}

	public boolean isEmpty() {
		return builders.isEmpty();
	}

	public boolean contains(Object o) {
		return builders.contains(o);
	}

	public Iterator<Compiler> iterator() {
		return builders.iterator();
	}

	public Object[] toArray() {
		return builders.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return builders.toArray(a);
	}

	public boolean add(Compiler e) {
		return builders.add(e);
	}

	public boolean remove(Object o) {
		return builders.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return builders.containsAll(c);
	}

	public boolean addAll(Collection<? extends Compiler> c) {
		return builders.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends Compiler> c) {
		return builders.addAll(index, c);
	}

	public boolean removeAll(Collection<?> c) {
		return builders.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return builders.retainAll(c);
	}

	public void clear() {
		builders.clear();
	}

	public boolean equals(Object o) {
		return builders.equals(o);
	}

	public int hashCode() {
		return builders.hashCode();
	}

	public Compiler get(int index) {
		return builders.get(index);
	}

	public Compiler set(int index, Compiler element) {
		return builders.set(index, element);
	}

	public void add(int index, Compiler element) {
		builders.add(index, element);
	}

	public Compiler remove(int index) {
		return builders.remove(index);
	}

	public int indexOf(Object o) {
		return builders.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		return builders.lastIndexOf(o);
	}

	public ListIterator<Compiler> listIterator() {
		return builders.listIterator();
	}

	public ListIterator<Compiler> listIterator(int index) {
		return builders.listIterator(index);
	}

	public List<Compiler> subList(int fromIndex, int toIndex) {
		return builders.subList(fromIndex, toIndex);
	}

	public String getFileExtension() {
		return fileExtension;
	}
}