package org.erachain.dbs.rocksDB.transformation.differentLength;

//import org.apache.flink.api.java.tuple.Tuple2;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.mapdb.Fun;

import java.util.Arrays;

public class ByteableTuple2StringLong implements Byteable<Fun.Tuple2<String, Long>> {
    @Override
    public Fun.Tuple2<String, Long> receiveObjectFromBytes(byte[] bytes) {
        byte[] bytesF1 = Arrays.copyOfRange(bytes, 0, bytes.length-Long.BYTES);
        byte[] bytesF2 = Arrays.copyOfRange(bytes, bytes.length - Long.BYTES, bytes.length);
        return new Fun.Tuple2(new ByteableString().receiveObjectFromBytes(bytesF1), new ByteableLong().receiveObjectFromBytes(bytesF2));
    }

    @Override
    public byte[] toBytesObject(Fun.Tuple2<String, Long> value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        return org.bouncycastle.util.Arrays.concatenate(new ByteableString().toBytesObject(value.a), new ByteableLong().toBytesObject(value.b));
    }
}
