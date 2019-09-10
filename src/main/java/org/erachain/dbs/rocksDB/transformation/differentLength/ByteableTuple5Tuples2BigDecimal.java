package org.erachain.dbs.rocksDB.transformation.differentLength;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple5;
import org.erachain.dbs.rocksDB.transformation.Byteable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ByteableTuple5Tuples2BigDecimal implements Byteable<
        Tuple5<
                Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>>> {
    private final ByteableTuple2BigDecimal byteableTuple2BigDecimal = new ByteableTuple2BigDecimal();

    @Override
    public Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> receiveObjectFromBytes(byte[] bytes) {
        List<Tuple2<BigDecimal, BigDecimal>> list = new ArrayList<>();
        int n = 5;
        byte[] copy = bytes;
        for (int i = 0; i < n; i++) {
            Tuple2<BigDecimal, BigDecimal> tuple2 = byteableTuple2BigDecimal.receiveObjectFromBytes(copy);
            copy = Arrays.copyOfRange(copy, byteableTuple2BigDecimal.getSummurySize(), copy.length);
            list.add(tuple2);
        }
        return Tuple5.of(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4));

    }

    @Override
    public byte[] toBytesObject(Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {
        return org.bouncycastle.util.Arrays.concatenate(
                org.bouncycastle.util.Arrays.concatenate(
                        byteableTuple2BigDecimal.toBytesObject(value.f0),
                        byteableTuple2BigDecimal.toBytesObject(value.f1),
                        byteableTuple2BigDecimal.toBytesObject(value.f2)),
                org.bouncycastle.util.Arrays.concatenate(
                        byteableTuple2BigDecimal.toBytesObject(value.f3),
                        byteableTuple2BigDecimal.toBytesObject(value.f4)));
    }
}
