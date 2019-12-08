package org.erachain.dbs;

import com.google.common.collect.UnmodifiableIterator;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * пробегает по итератором сортируя значения и пи этом пропуская дублирующие значения на входе
 * Правда в теста вылетает ошибка доступа при закрытии - org.erachain.datachain.TradeMapImplTest#getTradesByTimestamp()
 *
 * @param <T>
 */
@Slf4j
public class MergedIteratorNoDuplicates<T> extends UnmodifiableIterator<T> implements Closeable {

    final Queue<PeekingIteratorCloseable<T>> queue;
    protected T lastNext;
    final Comparator<? super T> itemComparator;

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

        for (IteratorCloseable<? extends T> iterator : iterators) {
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
    public synchronized T next() {
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
        } while (true);

        return lastNext;
    }

    @Override
    public void close() {
        for (PeekingIteratorCloseable<? extends T> iterator : queue) {
            iterator.close();
        }
    }

    @Override
    public void finalize() {
        close();
        try {
            /// сообщим о том что объект не закрывали вручную
            Long err = null;
            err++;
        } catch (Exception e) {
            logger.warn("FINALIZE used", e);
        }
    }

}
