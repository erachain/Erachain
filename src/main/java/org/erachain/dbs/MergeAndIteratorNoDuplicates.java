package org.erachain.dbs;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * Делает логическое И по итераторам по ключам - так что ключ должен быть вов сех итерторах - тогда он принимается как NEXT
 * пробегает по итератором сортируя значения и пи этом пропуская дублирующие значения на входе
 * Правда в теста вылетает ошибка доступа при закрытии - org.erachain.datachain.TradeMapImplTest#getTradesByTimestamp()
 *
 * @param <T>
 */
@Slf4j
public class MergeAndIteratorNoDuplicates<T> extends MergedIteratorNoDuplicates<T> {

    boolean descending;

    public MergeAndIteratorNoDuplicates(Iterable<? extends IteratorCloseable<? extends T>> iterators,
                                        final Comparator<? super T> itemComparator, boolean descending) {
        super(iterators, itemComparator);
        this.descending = descending;
    }

    @Override
    public boolean hasNext() {

        hasNextUsedBefore = true;

        if (lastNext == null) {
            return !queue.isEmpty();
        }

        T startKey;
        T roundtKey = null;
        T currentKey;

        do {

            startKey = roundtKey;

            // перебор по каждому итератору
            for (PeekingIteratorCloseable<T> nextIter : queue) {

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

        if (!hasNextUsedBefore) {
            hasNext();
        }

        if (queue.isEmpty()) {
            throw new NoSuchElementException();
        }

        hasNextUsedBefore = false;

        lastNext = queue.peek().next();
        return lastNext;
    }

}
