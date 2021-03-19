package org.erachain.dbs.rocksDB.transformation.differentLength;

import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ByteableTuple2BigDecimal implements Byteable<Tuple2<BigDecimal, BigDecimal>> {

    /**
     * [0] - длинна первого BigDecimal, [1] - scale1, [2] - scale2
     * @param bytes
     * @return
     */
    public Tuple2<BigDecimal, BigDecimal> receiveObjectFromBytes(byte[] bytes) {
        int length1 = bytes[0];
        byte[] buff1 = new byte[length1];

        System.arraycopy(bytes, 3, buff1, 0, length1);

        int length2 = bytes.length - length1 - 3;
        byte[] buff2 = new byte[length2];
        System.arraycopy(bytes, 3 + length1, buff2, 0, length2);

        return new Tuple2(new BigDecimal(new BigInteger(buff1), bytes[1]),  new BigDecimal(new BigInteger(buff2), bytes[2]));
    }

    @Override
    public byte[] toBytesObject(Tuple2<BigDecimal, BigDecimal> value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        byte[] buff1 = value.a.unscaledValue().toByteArray();
        byte[] buff2 = value.b.unscaledValue().toByteArray();

        byte[] buff = new byte[3 + buff1.length + buff2.length];
        int length1 = buff1.length;
        buff[0] = (byte) length1;
        buff[1] = (byte) value.a.scale();
        buff[2] = (byte) value.b.scale();

        System.arraycopy(buff1, 0, buff, 3, length1);
        System.arraycopy(buff2, 0, buff, 3 + length1, buff2.length);

        return buff;
    }
}
