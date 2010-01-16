package org.klomp.snark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FileRefList implements List<FileRef> {

	private List<FileRef> refs;
	private int[] pool;
	
	public FileRefList(int maxopen) {
		refs = new ArrayList<FileRef>();
		pool = new int[maxopen];
	}

	public boolean add(FileRef e) {
		return refs.add(e);
	}

	public void add(int index, FileRef element) {
		refs.add(index, element);
		
	}

	public boolean addAll(Collection<? extends FileRef> c) {
		return addAll(c);
	}

	public boolean addAll(int index, Collection<? extends FileRef> c) {
		return refs.addAll(index,c);
	}

	public void clear() {
		refs.clear();
	}

	public boolean contains(Object o) {
		return refs.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return refs.containsAll(c);
	}

	public FileRef get(int index) {
		return refs.get(index);
	}

	public int indexOf(Object o) {
		return refs.indexOf(o);
	}

	public boolean isEmpty() {
		return refs.isEmpty();
	}

	public Iterator<FileRef> iterator() {
		return refs.iterator();
	}

	public int lastIndexOf(Object o) {
		return refs.lastIndexOf(o);
	}

	public ListIterator<FileRef> listIterator() {
		return refs.listIterator();
	}

	public ListIterator<FileRef> listIterator(int index) {
		return refs.listIterator(index);
	}

	public boolean remove(Object o) {
		return refs.remove(o);
	}

	public FileRef remove(int index) {
		return refs.remove(index);
	}

	public boolean removeAll(Collection<?> c) {
		return refs.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return refs.retainAll(c);
	}

	public FileRef set(int index, FileRef element) {
		return refs.set(index, element);
	}

	public int size() {
		return refs.size();
	}

	public List<FileRef> subList(int fromIndex, int toIndex) {
		return refs.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		return refs.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return refs.toArray(a);
	}
	
	//
	
	
}
