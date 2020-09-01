package org.erachain.database.serializer;
// upd 09/03

import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class TransactionSerializer implements Serializer<Transaction>, Serializable {
    private static final long serialVersionUID = -6538913048331349777L;
    static Logger LOGGER = LoggerFactory.getLogger(TransactionSerializer.class.getName());

    @Override
    public void serialize(DataOutput out, Transaction value) throws IOException {
        out.writeInt(value.getDataLength(Transaction.FOR_DB_RECORD, true));
        out.write(value.toBytes(Transaction.FOR_DB_RECORD, true));
    }

    @Override
    public Transaction deserialize(DataInput in, int available) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {
            return TransactionFactory.getInstance().parse(bytes, Transaction.FOR_DB_RECORD);
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
