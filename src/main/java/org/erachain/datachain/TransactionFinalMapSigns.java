package org.erachain.datachain;

import org.erachain.dbs.DBTab;

public interface TransactionFinalMapSigns extends DBTab<byte[], Long> {
    boolean contains(byte[] signature);

    Long get(byte[] signature);

    void delete(byte[] signature);

    Long remove(byte[] signature);

    boolean set(byte[] signature, Long refernce);
}
