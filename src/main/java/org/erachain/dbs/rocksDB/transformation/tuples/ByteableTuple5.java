package org.erachain.dbs.rocksDB.transformation.tuples;

import lombok.Setter;
import org.apache.flink.api.java.tuple.Tuple5;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import java.util.Arrays;

@Setter
public abstract class ByteableTuple5<F0, F1, F2, F3, F4> implements Byteable<Tuple5<F0, F1, F2, F3, F4>> {
    public abstract int[] sizeElements();

    private Byteable[] byteables;


    @Override
    public Tuple5<F0, F1, F2,F3,F4> receiveObjectFromBytes(byte[] bytes) {
        int[] limits = sizeElements();
        byte[] bytesF0 = Arrays.copyOfRange(bytes, 0, limits[0]);
        int to1 = limits[0] + limits[1];
        byte[] bytesF1 = Arrays.copyOfRange(bytes, limits[0], to1);
        int to2 = to1 + limits[2];
        byte[] bytesF2 = Arrays.copyOfRange(bytes,  to1, to2);
        int to3 = to2 + limits[3];
        byte[] bytesF3 = Arrays.copyOfRange(bytes,  to2, to3);
        byte[] bytesF4 = Arrays.copyOfRange(bytes, to3, bytes.length);
        return Tuple5.of(
                (F0) byteables[0].receiveObjectFromBytes(bytesF0),
                (F1) byteables[1].receiveObjectFromBytes(bytesF1),
                (F2) byteables[2].receiveObjectFromBytes(bytesF2),
                (F3) byteables[3].receiveObjectFromBytes(bytesF3),
                (F4) byteables[4].receiveObjectFromBytes(bytesF4));
    }

    @Override
    public byte[] toBytesObject(Tuple5<F0, F1, F2,F3,F4> value) {
        return
                org.bouncycastle.util.Arrays.concatenate(
                        org.bouncycastle.util.Arrays.concatenate(
                                byteables[0].toBytesObject(value.f0),
                                byteables[1].toBytesObject(value.f1),
                                byteables[2].toBytesObject(value.f2)),
                        org.bouncycastle.util.Arrays.concatenate(
                                byteables[3].toBytesObject(value.f3),
                                byteables[4].toBytesObject(value.f4)));
    }
}
