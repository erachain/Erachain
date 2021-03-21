package org.erachain.dbs.rocksDB.transformation.differentLength;

import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.mapdb.Fun.Tuple2;

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
        return new Tuple2(byteableString.receiveObjectFromBytes(f0), byteableString.receiveObjectFromBytes(f1));
    }

    @Override
    public byte[] toBytesObject(Tuple2<String, String> value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        byte[] bytesValueF0 = byteableString.toBytesObject(value.a);
        byte[] bytesValueF1 = byteableString.toBytesObject(value.b);
        return org.bouncycastle.util.Arrays.concatenate(
                byteableInteger.toBytesObject(bytesValueF0.length),
                bytesValueF0,
                byteableInteger.toBytesObject(bytesValueF1.length),
                bytesValueF1);
    }
}
