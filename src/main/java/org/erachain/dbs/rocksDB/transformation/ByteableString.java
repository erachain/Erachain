package org.erachain.dbs.rocksDB.transformation;

import java.nio.charset.StandardCharsets;

public class ByteableString implements Byteable<String> {
    @Override
    public String receiveObjectFromBytes(byte[] bytes) {
        return new String(bytes,StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytesObject(String value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        return value.getBytes(StandardCharsets.UTF_8);
    }
}
