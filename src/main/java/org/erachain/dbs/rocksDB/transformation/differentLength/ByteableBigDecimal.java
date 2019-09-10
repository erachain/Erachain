package org.erachain.dbs.rocksDB.transformation.differentLength;

import org.erachain.core.BlockChain;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;

public class ByteableBigDecimal implements Byteable<BigDecimal> {
    private final ByteableInteger byteableInteger = new ByteableInteger();

    @Override
    public BigDecimal receiveObjectFromBytes(byte[] bytes) {
        byte[] sizeArray = Arrays.copyOfRange(bytes, 0, Integer.BYTES);
        Integer size = byteableInteger.receiveObjectFromBytes(sizeArray);
        byte[] number = Arrays.copyOfRange(bytes, Integer.BYTES, Integer.BYTES + size);
        BigDecimal bigDecimal = new BigDecimal(new BigInteger(number));
        return bigDecimal.setScale(BlockChain.AMOUNT_DEDAULT_SCALE, RoundingMode.HALF_DOWN);
    }

    @Override
    public byte[] toBytesObject(BigDecimal value) {
        byte[] bytes = value.setScale(BlockChain.AMOUNT_DEDAULT_SCALE, RoundingMode.HALF_DOWN).toBigInteger().toByteArray();
        return org.bouncycastle.util.Arrays.concatenate(byteableInteger.toBytesObject(bytes.length), bytes);
    }


}
