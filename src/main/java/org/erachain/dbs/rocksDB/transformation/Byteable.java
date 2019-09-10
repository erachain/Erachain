package org.erachain.rocksDB.transformation;

public interface Byteable<T> {

    T receiveObjectFromBytes(byte[] bytes);

    byte[] toBytesObject(T value);
}