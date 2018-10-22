package org.erachain.database.serializer;

import org.erachain.core.voting.Poll;
import org.apache.log4j.Logger;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class PollSerializer implements Serializer<Poll>, Serializable {
    private static final long serialVersionUID = -6538913048331349777L;
    static Logger LOGGER = Logger.getLogger(PollSerializer.class.getName());

    @Override
    public void serialize(DataOutput out, Poll value) throws IOException {
        out.writeInt(value.getDataLength());
        out.write(value.toBytes());
    }

    @Override
    public Poll deserialize(DataInput in, int available) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {
            return Poll.parse(bytes);
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
