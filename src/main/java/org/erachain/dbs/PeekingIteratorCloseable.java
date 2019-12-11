package org.erachain.dbs;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

@Slf4j
public class PeekingIteratorCloseable<T> implements PeekingIterator<T>, Closeable {

    private IteratorCloseable<T> parentIterator;
    private PeekingIterator<T> iteratorPeeking;

    PeekingIteratorCloseable(IteratorCloseable iterator) {
        this.parentIterator = iterator;
        this.iteratorPeeking = Iterators.peekingIterator(iterator);
    }

    PeekingIteratorCloseable(Iterator iterator) {
        this.parentIterator = IteratorCloseableImpl.make(iterator);
        this.iteratorPeeking = Iterators.peekingIterator(iterator);
    }

    @Override
    public void close() {
        try {
            parentIterator.close();
        } catch (IOException e) {
        }
    }

    @Override
    public void finalize() {
        close();
        logger.warn("FINALIZE used");
    }

    @Override
    public boolean hasNext() {
        return iteratorPeeking.hasNext();
    }

    @Override
    public T peek() {
        return iteratorPeeking.peek();
    }

    @Override
    public T next() {
        return iteratorPeeking.next();
    }

    @Override
    public void remove() {
        iteratorPeeking.remove();
    }
}
