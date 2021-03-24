package org.erachain.dbs;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * !!!! ВНИМАНИЕ - он не пашет так как значение ключа в итераторе - неотсортровано же!
 * и поэтому по проходу не получится узнать есть ли И совпадения - нужно перебирать целиком все значения
 * Делает логическое И по итераторам по ключам - так что ключ должен быть вов сех итерторах - тогда он принимается как NEXT
 * пробегает по итератором сортируя значения и пи этом пропуская дублирующие значения на входе
 * Правда в теста вылетает ошибка доступа при закрытии - org.erachain.datachain.TradeMapImplTest#getTradesByTimestamp()
 *
 * @param <T>
 */
@Slf4j

@Deprecated
public class MergeAND_Iterators<T> extends IteratorCloseableImpl<T> {

    boolean descending;
    List<PeekingIteratorCloseable<T>> iterators;
    final Comparator<? super T> itemComparator;

    /**
     * !!! НЕ ПАШЕТ!
     *
     * @param iterators
     * @param itemComparator
     * @param descending
     */
    public MergeAND_Iterators(Iterable<? extends IteratorCloseable<? extends T>> iterators,
                              final Comparator<? super T> itemComparator, boolean descending) {

        this.descending = descending;

        // A comparator that's used by the heap, allowing the heap
        // to be sorted based on the top of each iterator.
        Comparator<PeekingIteratorCloseable<T>> heapComparator =
                new Comparator<PeekingIteratorCloseable<T>>() {
                    @Override
                    public int compare(PeekingIteratorCloseable<T> o1, PeekingIteratorCloseable<T> o2) {
                        return itemComparator.compare(o1.peek(), o2.peek());
                    }
                };

        this.iterators = new ArrayList<>();
        for (Iterator<? extends T> iterator : iterators) {
            if (iterator.hasNext()) {
                this.iterators.add(new PeekingIteratorCloseable(iterator));
            }
        }
        this.itemComparator = itemComparator;

    }

    public static <T> IteratorCloseableImpl<T> make(List<IteratorCloseableImpl<T>> iterators,
                                                    final Comparator<T> itemComparator, boolean descending) {
        if (iterators.size() > 1) {
            return new MergeAND_Iterators(iterators, itemComparator, descending);
        }
        return iterators.get(0);
    }

    @Override
    public boolean hasNext() {

        T startKey;
        T roundtKey = null;
        T currentKey;

        do {

            startKey = roundtKey;

            // перебор по каждому итератору
            for (PeekingIteratorCloseable<T> nextIter : iterators) {

                if (!nextIter.hasNext()) {
                    // такого ключа вообще нет в итераторе - значит весь просмотр - кончился
                    return false;
                }

                currentKey = nextIter.peek();
                if (roundtKey == null) {
                    // первое значение - примем сразу
                    roundtKey = currentKey;
                } else {
                    int compare = itemComparator.compare(currentKey, roundtKey);
                    while (compare > 0 ^ descending) {
                        nextIter.next();
                        currentKey = nextIter.peek();
                        compare = itemComparator.compare(currentKey, roundtKey);
                        if (compare != 0 && !nextIter.hasNext())
                            // такого ключа вообще нет в итераторе - значит весь просмотр - кончился
                            return false;
                    }
                    if (compare == 0) {
                        // успех Нашли такой же ключ
                        continue;
                    } else {
                        // иначе проскочили ниже по ключу - значит его теперь берем за следующее значение
                        roundtKey = currentKey;
                    }
                }
            }
        } while (startKey != roundtKey);

        return true;
    }

    @Override
    public T next() {
        // любой из итераторов годится
        return iterators.get(0).next();
    }

    @Deprecated
    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        for (PeekingIteratorCloseable<? extends T> iterator : iterators) {
            try {
                iterator.close();
            } catch (Exception e) {
            }
        }
        isClosed = true;

    }

}
