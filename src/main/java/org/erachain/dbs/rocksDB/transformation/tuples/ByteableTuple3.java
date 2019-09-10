package org.erachain.dbs.rocksDB.transformation.tuples;

import lombok.Setter;
import org.apache.flink.api.java.tuple.Tuple3;
import org.erachain.dbs.rocksDB.transformation.Byteable;

import java.util.Arrays;

@Setter
public abstract class ByteableTuple3<F0, F1, F2> implements Byteable<Tuple3<F0, F1, F2>> {

    public abstract int[] sizeElements();

    private Byteable[] byteables;


    @Override
    public Tuple3<F0, F1, F2> receiveObjectFromBytes(byte[] bytes) {
        int[] limits = sizeElements();
        byte[] bytesF0 = Arrays.copyOfRange(bytes, 0, limits[0]);
        byte[] bytesF1 = Arrays.copyOfRange(bytes, limits[0], limits[0] + limits[1]);
        byte[] bytesF2 = Arrays.copyOfRange(bytes, limits[0] + limits[1], bytes.length);
        return Tuple3.of(
                (F0) byteables[0].receiveObjectFromBytes(bytesF0),
                (F1) byteables[1].receiveObjectFromBytes(bytesF1),
                (F2) byteables[2].receiveObjectFromBytes(bytesF2));
    }

    @Override
    public byte[] toBytesObject(Tuple3<F0, F1, F2> value) {
        return org.bouncycastle.util.Arrays.concatenate(
                byteables[0].toBytesObject(value.f0),
                byteables[1].toBytesObject(value.f1),
                byteables[2].toBytesObject(value.f2));
    }
}
