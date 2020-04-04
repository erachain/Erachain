package org.erachain.dbs;

import java.util.Iterator;
import java.util.Map;

public class IteratorParent<T> implements IteratorCloseable<T> {
    protected PeekingIteratorCloseable<? extends T> iterator;
    Map deleted;

    /**
     * С учетом удаленных в форке
     *
     * @param iterator
     * @param deleted
     */
    public IteratorParent(Iterator<? extends T> iterator, Map deleted) {
        this.iterator = new PeekingIteratorCloseable(iterator);
        this.deleted = deleted;
    }

    @Override
    public boolean hasNext() {
        if (deleted == null)
            return iterator.hasNext();

        if (!iterator.hasNext())
            return false;

        while (iterator.hasNext() && deleted.containsKey(iterator.peek())) {
            iterator.next();
        }

        return iterator.hasNext();

    }

    @Override
    public T next() {
        return iterator.next();
    }

    @Override
    public void close() {
        iterator.close();
    }

}