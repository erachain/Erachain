package org.erachain.dbs.rocksDB.transformation;

import java.util.UUID;

public class ByteableUUID implements Byteable<UUID> {
    private ByteableString byteableString = new ByteableString();
    @Override
    public UUID receiveObjectFromBytes(byte[] bytes) {
        return UUID.nameUUIDFromBytes(bytes);
    }

    @Override
    public byte[] toBytesObject(UUID value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        return byteableString.toBytesObject(value.toString());
    }
}
