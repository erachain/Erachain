package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.rocksDB.common.DBIterator;
import org.erachain.dbs.rocksDB.transformation.Byteable;

import java.io.IOException;

@Slf4j
public class IteratorCloseableImpl<K> implements IteratorCloseable<K> {

    private DBIterator iterator;
    private Byteable byteableKey;


    public IteratorCloseableImpl(DBIterator iterator, Byteable byteableKey) {
        this.iterator = iterator;
        this.byteableKey = byteableKey;
    }

    /// нужно обязательно освобождать память, см https://github.com/facebook/rocksdb/wiki/RocksJava-Basics
    @Override
    public void close() {
        try {
            iterator.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
    @Override
    public void finalize() {
        close();
        logger.warn("FINALIZE used");
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public K next() {
        return (K) byteableKey.receiveObjectFromBytes(iterator.next());
    }

}
