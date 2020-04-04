package org.erachain.dbs;

import com.google.common.collect.Iterators;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class IteratorParentTest {

    @Test
    public void hasNext() {
    }

    @Test
    public void next() {
        Set<Long> parent = new TreeSet<Long>() {{
            add(112L);
            add(10L);
            add(212L);
        }};

        Map<Long, Boolean> deleted = new TreeMap<Long, Boolean>() {{
            put(10L, true);
            put(212L, true);
        }};

        Iterator<Long> parentIterator = parent.iterator();
        IteratorCloseable iterator = new IteratorParent(parentIterator, deleted);

        assertEquals(Iterators.size(iterator), 1);

        // refresh
        parentIterator = parent.iterator();
        iterator = new IteratorParent(parentIterator, deleted);

        assertEquals(iterator.next(), 112L);
        assertEquals(iterator.hasNext(), false);


        //////////////////////////////////////
        deleted = new TreeMap<Long, Boolean>() {{
            put(10L, true);
            put(212L, true);
            put(112L, true);
        }};

        parentIterator = parent.iterator();
        iterator = new IteratorParent(parentIterator, deleted);

        assertEquals(Iterators.size(iterator), 0);

        // refresh
        parentIterator = parent.iterator();
        iterator = new IteratorParent(parentIterator, deleted);

        try {
            assertEquals(iterator.next(), 112L);
            assertEquals("java.util.NoSuchElementException", false);
        } catch (java.util.NoSuchElementException e) {
        }
        assertEquals(iterator.hasNext(), false);


        //////////////////////////////////////
        deleted = new TreeMap<Long, Boolean>() {{
            put(10L, true);
        }};

        parentIterator = parent.iterator();
        iterator = new IteratorParent(parentIterator, deleted);

        assertEquals(Iterators.size(iterator), 2);

        // refresh
        parentIterator = parent.iterator();
        iterator = new IteratorParent(parentIterator, deleted);

        assertEquals(iterator.next(), 112L);
        assertEquals(iterator.hasNext(), true);

        assertEquals(iterator.next(), 212L);
        assertEquals(iterator.hasNext(), false);

        //////////////////////////////////////
        deleted = new TreeMap<Long, Boolean>() {{
            put(212L, true);
        }};

        parentIterator = parent.iterator();
        iterator = new IteratorParent(parentIterator, deleted);

        assertEquals(Iterators.size(iterator), 2);

        // refresh
        parentIterator = parent.iterator();
        iterator = new IteratorParent(parentIterator, deleted);

        assertEquals(iterator.next(), 10L);
        assertEquals(iterator.hasNext(), true);

        assertEquals(iterator.next(), 112L);
        assertEquals(iterator.hasNext(), false);

    }

    /**
     * проверяем что без вызова hasNext тоже перебор правильно работает
     */
    @Test
    public void nextContinuous() {
        Set<Long> parent = new TreeSet<Long>() {{
            add(112L);
            add(113L);
            add(21L);
            add(10L);
            add(212L);
        }};

        Map<Long, Boolean> deleted = new TreeMap<Long, Boolean>() {{
            put(10L, true);
            put(113L, true);
            put(212L, true);
        }};

        List<Long> list = new ArrayList<Long>() {{
            add(21L);
            add(112L);
        }};

        Iterator<Long> parentIterator = parent.iterator();
        IteratorCloseable iterator = new IteratorParent(parentIterator, deleted);

        assertEquals(Iterators.size(iterator), list.size());

        // refresh
        parentIterator = parent.iterator();
        iterator = new IteratorParent(parentIterator, deleted);

        for (Long item : list) {
            assertEquals(item, iterator.next());
        }

        // это должен быть уже конец и ошибка должна быть:
        try {
            assertEquals(iterator.next(), "must be Exception: java.util.NoSuchElementException");
        } catch (java.util.NoSuchElementException e) {
        }
        assertEquals(iterator.hasNext(), false);

    }

}