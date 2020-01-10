package org.erachain.dbs.rocksDB.indexes.indexByteables;

import org.erachain.dbs.rocksDB.indexes.IndexByteable;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;

/**
 * Это преобразователь для вторичного индекса - причем зачем-то еще входит Ключ первичный сюда
 */

public class IndexByteableLong implements IndexByteable<Long, Long> {
    private ByteableLong byteableLong = new ByteableLong();
    @Override

    /**
     * result - то что прилетело из уровня создания вторичного ключа - в simpleIndexDB.getBiFunction().apply(key, value);
     * смотри org.erachain.dbs.rocksDB.integration.DBRocksDBTable#put(java.lang.Object, java.lang.Object)
     * key - первичный ключ
     */
    public byte[] toBytes(Long result) {
        return byteableLong.toBytesObject(result);
    }
}
