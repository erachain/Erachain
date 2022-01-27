package org.erachain.dbs.rocksDB.transformation;

public class ByteableByte implements Byteable<Byte> {
    @Override
    public Byte receiveObjectFromBytes(byte[] bytes) {
        return bytes[0];
    }

    @Override
    public byte[] toBytesObject(Byte value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        return new byte[]{value};
    }
}
