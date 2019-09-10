package org.erachain.rocksDB.transformation.differentLength;

import org.apache.flink.api.java.tuple.Tuple2;
import org.erachain.rocksDB.transformation.Byteable;
import org.erachain.rocksDB.transformation.ByteableLong;
import org.erachain.rocksDB.transformation.ByteableString;

import java.util.Arrays;

public class ByteableTuple2StringLong implements Byteable<Tuple2<String, Long>> {
    @Override
    public Tuple2<String, Long> receiveObjectFromBytes(byte[] bytes) {
        byte[] bytesF1 = Arrays.copyOfRange(bytes, 0, bytes.length-Long.BYTES);
        byte[] bytesF2 = Arrays.copyOfRange(bytes, bytes.length - Long.BYTES, bytes.length);
        return Tuple2.of(new ByteableString().receiveObjectFromBytes(bytesF1), new ByteableLong().receiveObjectFromBytes(bytesF2));
    }

    @Override
    public byte[] toBytesObject(Tuple2<String, Long> value) {
        return org.bouncycastle.util.Arrays.concatenate(new ByteableString().toBytesObject(value.f0),new ByteableLong().toBytesObject(value.f1));
    }
}
