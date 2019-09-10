package org.erachain.dbs.rocksDB.transformation;

public class ByteableTrivial implements Byteable<byte[]> {
    @Override
    public byte[] receiveObjectFromBytes(byte[] bytes) {
        return bytes;
    }

    @Override
    public byte[] toBytesObject(byte[] value) {
        return value;
    }
}
