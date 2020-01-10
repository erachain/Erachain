package org.erachain.dbs.rocksDB.transformation.differentLength;

import org.erachain.core.BlockChain;
import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.mapdb.Fun.Tuple3;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;

public class ByteableTuple3BigDecimal implements Byteable<Tuple3<BigDecimal, BigDecimal, BigDecimal>> {
    private final ByteableBigDecimal byteableBigDecimal = new ByteableBigDecimal();
    private final ByteableInteger byteableInteger = new ByteableInteger();

    @Override
    public Tuple3<BigDecimal, BigDecimal, BigDecimal> receiveObjectFromBytes(byte[] bytes) {
        byte[] sizeArray = Arrays.copyOfRange(bytes, 0, Integer.BYTES);
        Integer size = byteableInteger.receiveObjectFromBytes(sizeArray);
        byte[] f0 = Arrays.copyOfRange(bytes, Integer.BYTES, Integer.BYTES + size);
        int newZero = Integer.BYTES + size;
        byte[] sizeArray2 = Arrays.copyOfRange(bytes, newZero, newZero + Integer.BYTES);
        Integer size2 = byteableInteger.receiveObjectFromBytes(sizeArray2);
        byte[] f1 = Arrays.copyOfRange(bytes, newZero + Integer.BYTES, newZero + Integer.BYTES + size2);
        int newZero2 = newZero + Integer.BYTES + size2;
        byte[] sizeArray3 = Arrays.copyOfRange(bytes, newZero2, newZero2 + Integer.BYTES);
        Integer size3 = byteableInteger.receiveObjectFromBytes(sizeArray3);
        byte[] f2 = Arrays.copyOfRange(bytes, newZero2 + Integer.BYTES, newZero2 + Integer.BYTES + size3);
        return new Tuple3(new BigDecimal(new BigInteger(f0)).setScale(BlockChain.AMOUNT_DEDAULT_SCALE, RoundingMode.HALF_DOWN),
                new BigDecimal(new BigInteger(f1)).setScale(BlockChain.AMOUNT_DEDAULT_SCALE, RoundingMode.HALF_DOWN),
                new BigDecimal(new BigInteger(f2)).setScale(BlockChain.AMOUNT_DEDAULT_SCALE, RoundingMode.HALF_DOWN));
    }

    @Override
    public byte[] toBytesObject(Tuple3<BigDecimal, BigDecimal, BigDecimal> value) {
        return org.bouncycastle.util.Arrays.concatenate(
                byteableBigDecimal.toBytesObject(value.a),
                byteableBigDecimal.toBytesObject(value.b),
                byteableBigDecimal.toBytesObject(value.c));
    }
}
