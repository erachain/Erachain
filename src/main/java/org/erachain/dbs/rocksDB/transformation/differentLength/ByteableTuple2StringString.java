package org.erachain.rocksDB.transformation.differentLength;

import org.apache.flink.api.java.tuple.Tuple2;
import org.erachain.rocksDB.transformation.Byteable;
import org.erachain.rocksDB.transformation.ByteableInteger;
import org.erachain.rocksDB.transformation.ByteableString;

import java.util.Arrays;

public class ByteableTuple2StringString implements Byteable<Tuple2<String, String>> {
    private final ByteableString byteableString = new ByteableString();
    private final ByteableInteger byteableInteger = new ByteableInteger();

    @Override
    public Tuple2<String, String> receiveObjectFromBytes(byte[] bytes) {
        byte[] sizeArray = Arrays.copyOfRange(bytes, 0, Integer.BYTES);
        Integer size = byteableInteger.receiveObjectFromBytes(sizeArray);
        byte[] f0 = Arrays.copyOfRange(bytes, Integer.BYTES, Integer.BYTES + size);
        int newZero = Integer.BYTES + size;
        byte[] sizeArray2 = Arrays.copyOfRange(bytes, newZero, newZero + Integer.BYTES);
        Integer size2 = byteableInteger.receiveObjectFromBytes(sizeArray2);
        byte[] f1 = Arrays.copyOfRange(bytes, newZero + Integer.BYTES, newZero + Integer.BYTES + size2);
        return Tuple2.of(byteableString.receiveObjectFromBytes(f0), byteableString.receiveObjectFromBytes(f1));
    }

    @Override
    public byte[] toBytesObject(Tuple2<String, String> value) {
        byte[] bytesValueF0 = byteableString.toBytesObject(value.f0);
        byte[] bytesValueF1 = byteableString.toBytesObject(value.f1);
        return org.bouncycastle.util.Arrays.concatenate(
                byteableInteger.toBytesObject(bytesValueF0.length),
                bytesValueF0,
                byteableInteger.toBytesObject(bytesValueF1.length),
                bytesValueF1);
    }
}
