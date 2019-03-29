package org.erachain.database.serializer;

import org.erachain.at.ATTransaction;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class ATTransactionSerializer implements Serializer<ATTransaction>, Serializable {
    private static final long serialVersionUID = -6538913048331349777L;
    static Logger LOGGER = LoggerFactory.getLogger(ATTransactionSerializer.class.getName());

    @Override
    public void serialize(DataOutput out, ATTransaction value) throws IOException {
        out.writeInt(value.getSize());
        out.write(value.toBytes());
    }

    @Override
    public ATTransaction deserialize(DataInput in, int available) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {
            return ATTransaction.fromBytes(bytes);
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
