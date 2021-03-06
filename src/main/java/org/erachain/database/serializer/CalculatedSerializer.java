package org.erachain.database.serializer;
// upd 09/03

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mapdb.Serializer;

import org.erachain.core.transCalculated.Calculated;
import org.erachain.core.transCalculated.CalculatedFactory;

public class CalculatedSerializer implements Serializer<Calculated>, Serializable {
    private static final long serialVersionUID = -6538913048331349777L;
    static Logger LOGGER = LoggerFactory.getLogger(CalculatedSerializer.class.getName());

    @Override
    public void serialize(DataOutput out, Calculated value) throws IOException {
        out.writeInt(value.getDataLength());
        out.write(value.toBytes());
    }

    @Override
    public Calculated deserialize(DataInput in, int available) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {
            return CalculatedFactory.getInstance().parse(bytes);
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
