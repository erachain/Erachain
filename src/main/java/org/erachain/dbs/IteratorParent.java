package org.erachain.dbs;

import java.util.Iterator;
import java.util.Map;

public class IteratorParent<T> implements IteratorCloseable<T> {
    protected PeekingIteratorCloseable<? extends T> iterator;
    private Map deleted;
    private boolean hasNextUsedBefore = false;


    /**
     * С учетом удаленных в форке - для первичны ключей и для getIndexIterator, то есть преобразованных вторичных
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

        // мы сделали перебор удаленных
        hasNextUsedBefore = true;

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
        if (!hasNextUsedBefore) {
            // если проверки не было то надо промотать по Делетед иначе
            // возьмет даже удаленный
            hasNext();
        }

        hasNextUsedBefore = false;
        return iterator.next();
    }

    @Override
    public void close() {
        deleted = null;
        iterator.close();
    }

}