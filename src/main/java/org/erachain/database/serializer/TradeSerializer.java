package org.erachain.database.serializer;

import org.erachain.core.item.assets.Trade;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class TradeSerializer implements Serializer<Trade>, Serializable {
    private static final long serialVersionUID = -6538913048331349777L;
    static Logger LOGGER = LoggerFactory.getLogger(TradeSerializer.class.getName());

    @Override
    public void serialize(DataOutput out, Trade value) throws IOException {
        out.writeInt(value.getDataLength());
        out.write(value.toBytes());
    }

    @Override
    public Trade deserialize(DataInput in, int available) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {
            return Trade.parse(bytes);
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
