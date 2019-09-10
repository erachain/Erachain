package org.erachain.rocksDB.transformation.tuples;

import lombok.Setter;
import org.apache.flink.api.java.tuple.Tuple4;
import org.erachain.rocksDB.transformation.Byteable;

import java.util.Arrays;

@Setter
public abstract class ByteableTuple4<F0, F1, F2, F3> implements Byteable<Tuple4<F0, F1, F2, F3>> {


    public abstract int[] sizeElements();

    private Byteable[] byteables;


    @Override
    public Tuple4<F0, F1, F2, F3> receiveObjectFromBytes(byte[] bytes) {
        int[] limits = sizeElements();
        byte[] bytesF0 = Arrays.copyOfRange(bytes, 0, limits[0]);
        byte[] bytesF1 = Arrays.copyOfRange(bytes, limits[0], limits[0] + limits[1]);
        byte[] bytesF2 = Arrays.copyOfRange(bytes, limits[0] + limits[1], limits[0] + limits[1] + limits[2]);
        byte[] bytesF3 = Arrays.copyOfRange(bytes, limits[0] + limits[1] + limits[2], bytes.length);
        return Tuple4.of(
                (F0) byteables[0].receiveObjectFromBytes(bytesF0),
                (F1) byteables[1].receiveObjectFromBytes(bytesF1),
                (F2) byteables[2].receiveObjectFromBytes(bytesF2),
                (F3) byteables[2].receiveObjectFromBytes(bytesF3));
    }

    @Override
    public byte[] toBytesObject(Tuple4<F0, F1, F2, F3> value) {
        return org.bouncycastle.util.Arrays.concatenate(
                byteables[0].toBytesObject(value.f0),
                byteables[1].toBytesObject(value.f1),
                byteables[2].toBytesObject(value.f2),
                byteables[2].toBytesObject(value.f3));
    }


}
