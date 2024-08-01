package de.dnb.basics.applicationComponents;

import java.util.Iterator;

public class NullIterator<E> implements Iterator<E> {

	@Override
	public final boolean hasNext() {
		return false;
	}

	@Override
	public final E next() {
		return null;
	}

	@Override
	public final void remove() {
		throw new UnsupportedOperationException();
	}

}
