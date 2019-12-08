package org.erachain.dbs;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;

@Slf4j
public class PeekingIteratorCloseable<T> implements PeekingIterator<T>, Closeable {

    private PeekingIterator<T> iteratorPeeking;
    private IteratorCloseable<T> parentIterator;

    PeekingIteratorCloseable(IteratorCloseable iterator) {
        this.parentIterator = iterator;
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
        try {
            /// сообщим о том что объект не закрывали вручную
            Long err = null;
            err++;
        } catch (Exception e) {
            logger.warn("FINALIZE used", e);
        }
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
