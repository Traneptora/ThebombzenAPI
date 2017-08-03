package com.thebombzen.mods.thebombzenapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An immutable tuple designed for comparison. It allows us to compare a set of numbers without messing around with primes.
 * By the Cantor Pairing Function, this is equivalent to an integer, in case those computability theorists care.
 * The comparison is done first by length and then lexicographically:
 * 1. A shorter tuple is always less than a larger one.
 * 2. If two tuples are the same size, then they are compared lexicographically.
 * @author thebombzen
 * @throws NullPointerException In accordance with the specification for Comparable, null is incomparable to anything. This means that if any of the elements are null, undefined behavior results. Do not expect to get anything useful other than a NoobProgrammerException (NPE). 
 */
public class ComparableTuple<T extends Comparable<T>> implements Comparable<ComparableTuple<T>> {

	@Override
	public String toString() {
		return elements.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((elements == null) ? 0 : elements.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComparableTuple<?> other = (ComparableTuple<?>) obj;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.equals(other.elements))
			return false;
		return true;
	}

	/**
	 * This list contains the underlying elements.
	 * Despite being in a mutable container, it's never changed. 
	 */
	private List<T> elements = new ArrayList<T>();
	
	/**
	 * Construct a ComparableTuple from several elements.
	 * @param elements The elements comprising this tuple.
	 */
	public ComparableTuple(T... elements){
		this.elements.addAll(Arrays.asList(elements));
	}
	
	/**
	 * Returns the number of elements in the tuple.
	 */
	public int getSize(){
		return elements.size();
	}
	
	/**
	 * Returns the element at the specified index in the tuple.
	 * @param index An integer between 0 and size, inclusive/exclusive
	 * @throws IndexOutOfBoundsException if the index is not between 0 and size, i/e.
	 */
	public T get(int index){
		return elements.get(index);
	}

	@Override
	public int compareTo(ComparableTuple<T> other) {
		int size = Integer.compare(this.getSize(), other.getSize());
		if (size != 0){
			return size;
		}
		for (int index = 0; index < this.getSize(); index++){
			int comp = this.get(index).compareTo(other.get(index));
			if (comp != 0){
				return comp;
			}
		}
		return 0;
	}
	
	
}
