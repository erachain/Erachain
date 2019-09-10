package org.erachain.dbs.rocksDB.transformation.differentLength;

import lombok.Getter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.erachain.core.BlockChain;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;

public class ByteableTuple2BigDecimal implements Byteable<Tuple2<BigDecimal, BigDecimal>> {
    private final ByteableBigDecimal byteableBigDecimal = new ByteableBigDecimal();
    private final ByteableInteger byteableInteger = new ByteableInteger();
    @Getter
    private int summurySize;

    @Override
    public Tuple2<BigDecimal, BigDecimal> receiveObjectFromBytes(byte[] bytes) {
        byte[] sizeArray = Arrays.copyOfRange(bytes, 0, Integer.BYTES);
        Integer size = byteableInteger.receiveObjectFromBytes(sizeArray);
        byte[] f0 = Arrays.copyOfRange(bytes, Integer.BYTES,Integer.BYTES+size);
        int newZero = Integer.BYTES + size;
        byte[] sizeArray2 = Arrays.copyOfRange(bytes, newZero, newZero + Integer.BYTES);
        Integer size2 = byteableInteger.receiveObjectFromBytes(sizeArray2);
        summurySize = newZero + Integer.BYTES + size2;
        byte[] f1 = Arrays.copyOfRange(bytes, newZero + Integer.BYTES, summurySize);
        return Tuple2.of(new BigDecimal(new BigInteger(f0)).setScale(BlockChain.AMOUNT_DEDAULT_SCALE, RoundingMode.HALF_DOWN),
                new BigDecimal(new BigInteger(f1)).setScale(BlockChain.AMOUNT_DEDAULT_SCALE, RoundingMode.HALF_DOWN));
    }

    @Override
    public byte[] toBytesObject(Tuple2<BigDecimal, BigDecimal> value) {
        return org.bouncycastle.util.Arrays.concatenate(byteableBigDecimal.toBytesObject(value.f0), byteableBigDecimal.toBytesObject(value.f1));
    }
}
