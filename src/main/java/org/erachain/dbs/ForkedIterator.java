package org.erachain.dbs;

import com.google.common.collect.ImmutableList;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

public class ForkedIterator<T> {

    public static <T> IteratorCloseable<T> make(Iterator<T> iteratorParent,
                                                Iterator<T> iteratorForked, Map deleted,
                                                Comparator<? super T> comparator, boolean descending) {
        return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                new IteratorParent(iteratorParent, deleted),
                iteratorForked), comparator, descending);
    }

}
