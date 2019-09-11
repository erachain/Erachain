package org.erachain.dbs.rocksDB.transformation.differentLength;

import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

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
        return new Tuple5(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4));

    }

    @Override
    public byte[] toBytesObject(Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {
        return org.bouncycastle.util.Arrays.concatenate(
                org.bouncycastle.util.Arrays.concatenate(
                        byteableTuple2BigDecimal.toBytesObject(value.a),
                        byteableTuple2BigDecimal.toBytesObject(value.b),
                        byteableTuple2BigDecimal.toBytesObject(value.c)),
                org.bouncycastle.util.Arrays.concatenate(
                        byteableTuple2BigDecimal.toBytesObject(value.d),
                        byteableTuple2BigDecimal.toBytesObject(value.e)));
    }
}
