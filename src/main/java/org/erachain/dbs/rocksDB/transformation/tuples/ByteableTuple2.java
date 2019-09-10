package org.erachain.dbs.rocksDB.transformation.tuples;

import lombok.Setter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.erachain.dbs.rocksDB.transformation.Byteable;

import java.util.Arrays;

@Setter
public abstract class ByteableTuple2<F0, F1> implements Byteable<Tuple2<F0, F1>> {

    public abstract int sizeElements();
    private Byteable[] byteables;


    @Override
    public Tuple2<F0,F1> receiveObjectFromBytes(byte[] bytes) {
        byte[] bytesF1 = Arrays.copyOfRange(bytes, 0, sizeElements());
        byte[] bytesF2 = Arrays.copyOfRange(bytes, bytes.length - sizeElements(), bytes.length);
        return Tuple2.of((F0) byteables[0].receiveObjectFromBytes(bytesF1), (F1) byteables[1].receiveObjectFromBytes(bytesF2));
    }

    @Override
    public byte[] toBytesObject(Tuple2<F0,F1> value) {
        return org.bouncycastle.util.Arrays.concatenate(byteables[0].toBytesObject(value.f0), byteables[1].toBytesObject(value.f1));
    }
}
