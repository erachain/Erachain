package org.erachain.dbs;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * пробегает по итератором сортируя значения и пи этом пропуская дублирующие значения на входе
 * Правда в теста вылетает ошибка доступа при закрытии - org.erachain.datachain.TradeMapImplTest#getTradesByTimestamp()
 *
 * @param <T>
 */
@Slf4j
public class MergedIteratorNoDuplicates<T> extends IteratorCloseableImpl<T> {

    final Queue<PeekingIteratorCloseable<T>> queue;
    protected T lastNext;
    final Comparator<? super T> itemComparator;
    protected boolean isClosed;

    protected MergedIteratorNoDuplicates() {
        queue = null;
        itemComparator = null;
    }

    /**
     * пробегает по итератором сортируя значения и пи этом пропуская дублирующие значения на входе
     * Правда в тестах вылетает ошибка доступа при закрытии - org.erachain.datachain.TradeMapImplTest#getTradesByTimestamp()
     *
     * @param iterators
     * @param itemComparator
     */
    public MergedIteratorNoDuplicates(Iterable<? extends IteratorCloseable<? extends T>> iterators,
                                      final Comparator<? super T> itemComparator) {
        // A comparator that's used by the heap, allowing the heap
        // to be sorted based on the top of each iterator.
        Comparator<PeekingIteratorCloseable<T>> heapComparator =
                new Comparator<PeekingIteratorCloseable<T>>() {
                    @Override
                    public int compare(PeekingIteratorCloseable<T> o1, PeekingIteratorCloseable<T> o2) {
                        return itemComparator.compare(o1.peek(), o2.peek());
                    }
                };

        queue = new PriorityQueue<PeekingIteratorCloseable<T>>(2, heapComparator);
        this.itemComparator = itemComparator;

        for (Iterator<? extends T> iterator : iterators) {
            if (iterator.hasNext()) {
                queue.add(new PeekingIteratorCloseable(iterator));
            }
        }
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public T next() {
        do {
            PeekingIteratorCloseable<T> nextIter = queue.remove();
            T next = nextIter.next();
            if (nextIter.hasNext()) {
                queue.add(nextIter);
            }
            if (itemComparator.compare(next, lastNext) != 0) {
                lastNext = next;
                break;
            }
        } while (!queue.isEmpty());

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

    @Override
    public void finalize() throws Throwable {
        if (!isClosed) {
            close();
            logger.warn("FINALIZE used queue");
        }
        super.finalize();
    }

}
