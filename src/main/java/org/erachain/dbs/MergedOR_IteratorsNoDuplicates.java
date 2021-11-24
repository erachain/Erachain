package org.erachain.dbs;

import lombok.extern.slf4j.Slf4j;
import org.erachain.utils.ByteArrayUtils;
import org.erachain.utils.ReverseComparator;

import java.util.*;

/**
 * пробегает по итератором сортируя значения и при этом пропуская дублирующие значения на входе
 * Правда в теста вылетает ошибка доступа при закрытии - org.erachain.datachain.TradeMapImplTest#getTradesByTimestamp()
 *
 * @param <T>
 */
@Slf4j
public class MergedOR_IteratorsNoDuplicates<T> extends IteratorCloseableImpl<T> {

    final Queue<PeekingIteratorCloseable<T>> queue;
    protected T lastNext;
    final Comparator<? super T> itemComparator;
    protected boolean hasNextUsedBefore = false;

    /**
     * пробегает по итератором сортируя значения и при этом пропуская дублирующие значения на входе
     * Правда в тестах вылетает ошибка доступа при закрытии - org.erachain.datachain.TradeMapImplTest#getTradesByTimestamp()
     *
     * @param iterators
     * @param itemComparator
     * @param descending
     */
    public MergedOR_IteratorsNoDuplicates(Iterable<? extends IteratorCloseable<? extends T>> iterators,
                                          Comparator<? super T> itemComparator, boolean descending) {
        // A comparator that's used by the heap, allowing the heap
        // to be sorted based on the top of each iterator.

        final Comparator<? super T> localVomparator;
        if (descending)
            localVomparator = new ReverseComparator<>(itemComparator);
        else
            localVomparator = itemComparator;

        Comparator<PeekingIteratorCloseable<T>> heapComparator =
                new Comparator<PeekingIteratorCloseable<T>>() {
                    @Override
                    public int compare(PeekingIteratorCloseable<T> o1, PeekingIteratorCloseable<T> o2) {
                        // for BYTES use Fun.BYTE_ARRAY_COMPARATOR
                        if (false) {
                            T peek1 = o1.peek();
                            T peek2 = o2.peek();
                            if (peek1 instanceof byte[]) {
                                return ByteArrayUtils.compareUnsignedAsMask((byte[]) peek1, (byte[]) peek2);
                            }
                        }
                        return localVomparator.compare(o1.peek(), o2.peek());
                    }
                };

        queue = new PriorityQueue<PeekingIteratorCloseable<T>>(2, heapComparator);
        this.itemComparator = localVomparator;

        for (Iterator<? extends T> iterator : iterators) {
            if (iterator.hasNext()) {
                queue.add(new PeekingIteratorCloseable(iterator));
            }
        }
    }

    @Override
    public boolean hasNext() {

        hasNextUsedBefore = true;

        if (lastNext == null) {
            return !queue.isEmpty();
        }

        // перебор по каждому итератору
        while (!queue.isEmpty()) {
            PeekingIteratorCloseable<T> nextIter = queue.remove();
            if (nextIter.hasNext()) {

                try {
                    if (itemComparator.compare(nextIter.peek(), lastNext) == 0) {
                        nextIter.next();
                    } else {
                        break;
                    }
                } finally {
                    if (nextIter.hasNext()) {
                        queue.add(nextIter);
                        break;
                    }
                }
            }
        }

        return !queue.isEmpty();
    }

    @Override
    public T next() {

        if (!hasNextUsedBefore) {
            hasNext();
        }

        if (queue.isEmpty()) {
            throw new NoSuchElementException();
        }

        hasNextUsedBefore = false;

        PeekingIteratorCloseable<T> nextIter = queue.remove();
        lastNext = nextIter.next();
        if (nextIter.hasNext()) {
            queue.add(nextIter);
        }
        return lastNext;
    }

    @Deprecated
    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        for (PeekingIteratorCloseable<? extends T> iterator : queue) {
            try {
                iterator.close();
            } catch (Exception e) {
            }
        }
        isClosed = true;

    }

}
