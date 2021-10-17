package org.erachain.dbs;

import org.mapdb.Fun.Tuple2;

import java.util.Iterator;
import java.util.Map;

public class IteratoкOfSetParent<T extends Tuple2<K, V>, K, V> implements IteratorCloseable<Tuple2<K, V>> {
    protected PeekingIteratorCloseable<Tuple2<K, V>> iterator;
    private Map deleted;
    private boolean hasNextUsedBefore = false;


    /**
     * С учетом удаленных в форке - для вторичных ключей на основе Set
     *
     * @param iterator
     * @param deleted
     */
    public IteratoкOfSetParent(Iterator<Tuple2<K, V>> iterator, Map deleted) {
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

        while (iterator.hasNext()) {
            V key = iterator.peek().b;
            if (!deleted.containsKey(key))
                break;

            iterator.next();
        }

        return iterator.hasNext();

    }

    @Override
    public Tuple2<K, V> next() {
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