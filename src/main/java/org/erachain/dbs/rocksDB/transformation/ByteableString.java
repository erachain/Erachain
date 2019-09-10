package org.erachain.rocksDB.transformation;

import java.nio.charset.StandardCharsets;

public class ByteableString implements Byteable<String> {
    @Override
    public String receiveObjectFromBytes(byte[] bytes) {
        return new String(bytes,StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytesObject(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
