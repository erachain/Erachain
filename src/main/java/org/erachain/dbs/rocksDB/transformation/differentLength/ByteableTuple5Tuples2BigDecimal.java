package org.erachain.dbs.rocksDB.transformation.differentLength;

import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;

public class ByteableTuple5Tuples2BigDecimal implements Byteable<
        Tuple5<
                Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>>> {
    private final ByteableTuple2BigDecimal byteableTuple2BigDecimal = new ByteableTuple2BigDecimal();

    @Override
    public Tuple5<
            Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>> receiveObjectFromBytes(byte[] bytes) {
        Tuple2<BigDecimal, BigDecimal>[] tuples = new Tuple2[5];

        int n = 5;
        int pos = 0;
        int length;
        for (int i = 0; i < n; i++) {
            length = bytes[i];
            byte[] buff = new byte[length];
            System.arraycopy(bytes, 5 + pos, buff, 0, length);
            tuples[i] = byteableTuple2BigDecimal.receiveObjectFromBytes(buff);
            pos += length;
        }
        return new Tuple5(tuples[0], tuples[1], tuples[2], tuples[3], tuples[4]);

    }

    @Override
    public byte[] toBytesObject(Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {
        byte[] buff1 = byteableTuple2BigDecimal.toBytesObject(value.a);
        byte[] buff2 = byteableTuple2BigDecimal.toBytesObject(value.b);
        byte[] buff3 = byteableTuple2BigDecimal.toBytesObject(value.c);
        byte[] buff4 = byteableTuple2BigDecimal.toBytesObject(value.d);
        byte[] buff5 = byteableTuple2BigDecimal.toBytesObject(value.e);
        byte[] buff = new byte[5 + buff1.length+ buff2.length + buff3.length + buff4.length + buff5.length];

        buff[0] = (byte)buff1.length;
        buff[1] = (byte)buff2.length;
        buff[2] = (byte)buff3.length;
        buff[3] = (byte)buff4.length;
        buff[4] = (byte)buff5.length;

        int pos = 0;
        System.arraycopy(buff1, 0, buff, 5 + pos, buff1.length);
        pos += buff1.length;
        System.arraycopy(buff2, 0, buff, 5 + pos, buff2.length);
        pos += buff2.length;
        System.arraycopy(buff3, 0, buff, 5 + pos, buff3.length);
        pos += buff3.length;
        System.arraycopy(buff4, 0, buff, 5 + pos, buff4.length);
        pos += buff4.length;
        System.arraycopy(buff5, 0, buff, 5 + pos, buff5.length);
        //pos += buff5.length;

        return buff;
    }
}
