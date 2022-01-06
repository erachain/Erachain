package org.erachain.dbs.rocksDB.transformation;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;
import org.erachain.dbs.rocksDB.exceptions.WrongParseException;

/**
 * for Unconfirmed transaction
 */
@Slf4j
public class ByteableTransactionUnc implements Byteable<Transaction> {

    @Override
    public Transaction receiveObjectFromBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return TransactionFactory.getInstance().parse(bytes, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new WrongParseException(e);
        }
    }


    @Override
    public byte[] toBytesObject(Transaction value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        return value.toBytes(Transaction.FOR_NETWORK, true);
    }
}
