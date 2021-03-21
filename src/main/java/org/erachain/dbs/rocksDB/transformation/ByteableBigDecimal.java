package org.erachain.dbs.rocksDB.transformation;


import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Dmitrii Ermolaev (icreator)
 */
public class ByteableBigDecimal implements Byteable<BigDecimal>{
    @Override
    public BigDecimal receiveObjectFromBytes(byte[] bytes) {
        int scale = bytes[0];
        bytes[0] = (byte)0; // clear SCALE
        return new BigDecimal(new BigInteger(bytes), scale);
    }

    @Override
    public byte[] toBytesObject(BigDecimal value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        byte[] buf = value.unscaledValue().toByteArray();
        byte[] buff2 = new byte[buf.length + 1];
        System.arraycopy(buf, 0, buff2, 1, buf.length);
        buff2[0] = (byte) value.scale();
        return buff2;
    }
}
