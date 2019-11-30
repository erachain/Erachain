package org.erachain.dbs.mapDB;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.UnmodifiableIterator;
import org.mapdb.BTreeMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * пробегает по итератором сортируя значения и пи этом пропуская дублирующие значения на входе
 *
 * @param <T>
 */
public class MergedIteratorNoDuplicates<T> extends UnmodifiableIterator<T> {


    final List<ConcurrentNavigableMap> maps;
    final List<PeekingIterator<T>> peekedIterators = new ArrayList<>();
    protected T lastNext;
    final Comparator<? super T> itemComparator;

    public MergedIteratorNoDuplicates(List<ConcurrentNavigableMap> maps,
                                      final Comparator<? super T> itemComparator) {
        this.maps = maps;
        this.itemComparator = itemComparator;
        for (ConcurrentNavigableMap map: maps) {
            Iterator iterator = map.navigableKeySet().iterator();
            if (iterator.hasNext()) {
                peekedIterators.add(Iterators.peekingIterator(iterator));
            }
        }

    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public synchronized T next() {
        do {

            BTreeMap map1 = poll.get(0);
            map1.keySet();

            PeekingIterator<T> nextIter = queue.remove();
            T next = nextIter.next();
            if (nextIter.hasNext()) {
                queue.add(nextIter);
            }
            if (itemComparator.compare(next, lastNext) != 0) {
                lastNext = next;
                break;
            }
        } while (true);

        return lastNext;
    }
}
