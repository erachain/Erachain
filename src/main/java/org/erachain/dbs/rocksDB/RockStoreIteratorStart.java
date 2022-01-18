package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksIterator;

import java.util.NoSuchElementException;

import static org.erachain.utils.ByteArrayUtils.compareUnsignedAsMask;

@Slf4j
public final class RockStoreIteratorStart extends RockStoreIterator {

    byte[] stopKey;
    byte[] startKey;

    public RockStoreIteratorStart(RocksIterator dbIterator, boolean descending, boolean isIndex, byte[] startKey, byte[] stopKey) {
        super(dbIterator, descending, isIndex);
        this.startKey = startKey;
        this.stopKey = stopKey;
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = false;
        try {
            if (jumpToLast) {
                if (startKey != null) {
                    dbIterator.seekForPrev(startKey);
                } else {
                    dbIterator.seekToLast();
                }

                jumpToLast = false;
                first = false;
            }
            if (first) {
                if (startKey != null) {
                    dbIterator.seek(startKey);
                } else {
                    dbIterator.seekToFirst();
                }
                first = false;
            }
            if (!(hasNext = dbIterator.isValid())) {
                dbIterator.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            try {
                dbIterator.close();
            } catch (Exception e1) {
                logger.error(e1.getMessage(), e1);
            }
        }

        if (stopKey == null) {
            return hasNext;
        } else {
            if (descending) {
                return hasNext && compareUnsignedAsMask(stopKey, dbIterator.key()) <= 0;
            } else {
                return hasNext && compareUnsignedAsMask(stopKey, dbIterator.key()) >= 0;
            }
        }
    }

    @Override
    public byte[] next() {
        if (!dbIterator.isValid()) {
            throw new NoSuchElementException();
        }
        byte[] key;
        if (isIndex) {
            key = dbIterator.value();
        } else {
            key = dbIterator.key();
        }
        if (descending) {
            dbIterator.prev();
        } else {
            dbIterator.next();
        }
        return key;
    }
}