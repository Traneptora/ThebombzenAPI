package thebombzen.mods.thebombzenapi;

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

	private List<T> elements = new ArrayList<T>();
	
	public ComparableTuple(T... elements){
		this.elements.addAll(Arrays.asList(elements));
	}
	
	public int getSize(){
		return elements.size();
	}
	
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
