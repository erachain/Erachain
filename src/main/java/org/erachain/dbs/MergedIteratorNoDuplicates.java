package org.erachain.dbs;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * пробегает по итератором сортируя значения и пи этом пропуская дублирующие значения на входе
 *
 * @param <T>
 */
public class MergedIteratorNoDuplicates<T> extends UnmodifiableIterator<T> {

    final Queue<PeekingIterator<T>> queue;
    protected T lastNext;
    final Comparator<? super T> itemComparator;

    public MergedIteratorNoDuplicates(Iterable<? extends Iterator<? extends T>> iterators,
                                      final Comparator<? super T> itemComparator) {
        // A comparator that's used by the heap, allowing the heap
        // to be sorted based on the top of each iterator.
        Comparator<PeekingIterator<T>> heapComparator =
                new Comparator<PeekingIterator<T>>() {
                    @Override
                    public int compare(PeekingIterator<T> o1, PeekingIterator<T> o2) {
                        return itemComparator.compare(o1.peek(), o2.peek());
                    }
                };

        queue = new PriorityQueue<PeekingIterator<T>>(2, heapComparator);
        this.itemComparator = itemComparator;

        for (Iterator<? extends T> iterator : iterators) {
            if (iterator.hasNext()) {
                queue.add(Iterators.peekingIterator(iterator));
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
