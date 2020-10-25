package org.erachain.database.serializer;

import org.erachain.core.exdata.exLink.ExLink;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class ExLinkSerializer implements Serializer<ExLink>, Serializable {
    static Logger LOGGER = LoggerFactory.getLogger(ExLinkSerializer.class.getName());

    @Override
    public void serialize(DataOutput out, ExLink value) throws IOException {
        out.writeInt(value.length());
        out.write(value.toBytes());
    }

    @Override
    public ExLink deserialize(DataInput in, int available) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {
            return ExLink.parse(bytes);
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
