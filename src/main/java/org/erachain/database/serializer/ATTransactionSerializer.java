package org.erachain.database.serializer;

import org.erachain.at.AT_Transaction;
import org.apache.log4j.Logger;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class ATTransactionSerializer implements Serializer<AT_Transaction>, Serializable {
    private static final long serialVersionUID = -6538913048331349777L;
    static Logger LOGGER = Logger.getLogger(ATTransactionSerializer.class.getName());

    @Override
    public void serialize(DataOutput out, AT_Transaction value) throws IOException {
        out.writeInt(value.getSize());
        out.write(value.toBytes());
    }

    @Override
    public AT_Transaction deserialize(DataInput in, int available) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {
            return AT_Transaction.fromBytes(bytes);
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
