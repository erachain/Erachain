package org.erachain.database.serializer;
// upd 09/03

import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;
import org.mapdb.Fun;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class LongAndTransactionSerializer implements Serializer<Fun.Tuple2<Long, Transaction>>, Serializable {
    private static final long serialVersionUID = -6538913048331349555L;
    static Logger LOGGER = LoggerFactory.getLogger(LongAndTransactionSerializer.class.getName());

    @Override
    public void serialize(DataOutput out, Fun.Tuple2<Long, Transaction> value) throws IOException {
        out.writeLong(value.a);
        out.writeInt(value.b.getDataLength(Transaction.FOR_DB_RECORD, true));
        out.write(value.b.toBytes(Transaction.FOR_DB_RECORD, true));
    }

    @Override
    public Fun.Tuple2<Long, Transaction> deserialize(DataInput in, int available) throws IOException {
        // READ LONG
        long number = in.readLong();
        // REST - as Transaction
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {
            return new Fun.Tuple2<Long, Transaction>(number,
                    TransactionFactory.getInstance().parse(bytes, Transaction.FOR_DB_RECORD));
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
