package org.erachain.dbs.rocksDB.transformation;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.transaction.Transaction;
import org.erachain.dbs.rocksDB.exceptions.WrongParseException;
import org.mapdb.Fun.Tuple2;

import java.util.Arrays;
@Slf4j
public class ByteableLongAndTransaction implements Byteable<Tuple2<Long, Transaction>> {

    private ByteableLong byteableLong = new ByteableLong();

    private ByteableTransaction byteableTransaction = new ByteableTransaction();

    @Override
    public Tuple2<Long, Transaction> receiveObjectFromBytes(byte[] bytes) {
        byte[] longBytes = Arrays.copyOf(bytes, Long.BYTES);
        Long number = (Long) byteableLong.receiveObjectFromBytes(longBytes);
        byte[] bytesTransaction = Arrays.copyOfRange(bytes, Long.BYTES, bytes.length);
        try {
            return new Tuple2<>(number,
                    byteableTransaction.receiveObjectFromBytes(bytesTransaction));
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new WrongParseException(e);
        }
    }

    @Override
    public byte[] toBytesObject(Tuple2<Long, Transaction> value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        Long aLong = value.a;
        Transaction transaction = value.b;
        byte[] bytesLong = byteableLong.toBytesObject(aLong);
        byte[] bytesTransaction = byteableTransaction.toBytesObject(transaction);
        return org.bouncycastle.util.Arrays.concatenate(bytesLong, bytesTransaction);
    }
}
