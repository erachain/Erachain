package org.erachain.datachain;

import org.erachain.dbs.IteratorCloseable;
import org.mapdb.Fun.Tuple2;

import java.util.Iterator;
import java.util.NavigableSet;

public class IndexIterator<T> implements IteratorCloseable<T> {

    private Iterator<Tuple2<?, T>> iterator;
    private int index;

    public IndexIterator(NavigableSet<Tuple2<?, T>> set) {
        this.iterator = set.iterator();
        this.index = 0;
    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public T next() {
        this.index++;
        return this.iterator.next().b;
    }

    @Override
    public void remove() {
        this.iterator.remove();
    }

    @Override
    public void close() {
    }

}
