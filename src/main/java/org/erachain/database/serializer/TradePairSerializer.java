package org.erachain.database.serializer;

import org.erachain.core.item.assets.TradePair;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class TradePairSerializer implements Serializer<TradePair>, Serializable {
    private static final long serialVersionUID = -65348331349777L;
    static Logger LOGGER = LoggerFactory.getLogger(TradePairSerializer.class.getName());

    @Override
    public void serialize(DataOutput out, TradePair value) throws IOException {
        out.writeInt(value.getDataLength());
        out.write(value.toBytes());
    }

    @Override
    public TradePair deserialize(DataInput in, int available) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {
            return TradePair.parse(bytes);
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
