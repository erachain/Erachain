package org.erachain.database.serializer;

import org.erachain.core.naming.Name;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class NameSerializer implements Serializer<Name>, Serializable {
    private static final long serialVersionUID = -6538913048331349777L;
    static Logger LOGGER = LoggerFactory.getLogger(NameSerializer.class.getName());

    @Override
    public void serialize(DataOutput out, Name value) throws IOException {
        out.writeInt(value.getDataLength());
        out.write(value.toBytes());
    }

    @Override
    public Name deserialize(DataInput in, int available) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {
            return Name.Parse(bytes);
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
