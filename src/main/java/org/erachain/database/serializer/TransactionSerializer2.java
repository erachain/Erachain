package org.erachain.database.serializer;
// upd 09/03

import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class TransactionSerializer2 implements Serializer<Transaction>, Serializable {
    private static final long serialVersionUID = -6538913048331349777L;
    static Logger LOGGER = LoggerFactory.getLogger(TransactionSerializer2.class.getName());

    @Override
    public void serialize(DataOutput out, Transaction value) throws IOException {
        if (false) {
            out.writeInt(value.getDataLength(Transaction.FOR_DB_RECORD, true));
            out.write(value.toBytes(Transaction.FOR_DB_RECORD, true));
        } else {
            out.writeInt(value.getDataLength(Transaction.FOR_NETWORK, true));
            out.write(value.toBytes(Transaction.FOR_NETWORK, true));
        }
    }

    @Override
    public Transaction deserialize(DataInput in, int available) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {
            if (false) {
                return TransactionFactory.getInstance().parse(bytes, Transaction.FOR_DB_RECORD);
            } else {
                return TransactionFactory.getInstance().parse(bytes, Transaction.FOR_NETWORK);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public int fixedSize() {
        return -1;
    }
}
