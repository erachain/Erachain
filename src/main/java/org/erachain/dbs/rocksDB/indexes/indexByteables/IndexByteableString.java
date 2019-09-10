package org.erachain.dbs.rocksDB.indexes.indexByteables;

import org.erachain.dbs.rocksDB.indexes.IndexByteable;

import java.nio.charset.StandardCharsets;

public class IndexByteableString implements IndexByteable<String,Long> {
    @Override
    public byte[] toBytes(String result, Long key) {
        return result.getBytes(StandardCharsets.UTF_8);
    }
}
