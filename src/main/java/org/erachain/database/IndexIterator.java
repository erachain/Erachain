package org.erachain.database;

import org.mapdb.Fun.Tuple2;

import java.util.Iterator;
import java.util.NavigableSet;

public class IndexIterator<T> implements Iterator<T> {

    private Iterator<Tuple2<?, T>> iterator;

    public IndexIterator(NavigableSet<Tuple2<?, T>> set) {
        this.iterator = set.iterator();
    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public T next() {
        return this.iterator.next().b;
    }

    @Override
    public void remove() {
        this.iterator.remove();
    }
}
