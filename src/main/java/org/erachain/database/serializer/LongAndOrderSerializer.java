package org.erachain.database.serializer;
// upd 09/03

import org.erachain.core.item.assets.Order;
import org.mapdb.Fun;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class LongAndOrderSerializer implements Serializer<Fun.Tuple2<Long, Order>>, Serializable {
    private static final long serialVersionUID = -6538913048331349555L;
    static Logger LOGGER = LoggerFactory.getLogger(LongAndOrderSerializer.class.getName());

    @Override
    public void serialize(DataOutput out, Fun.Tuple2<Long, Order> value) throws IOException {
        out.writeLong(value.a);
        out.writeInt(value.b.getDataLength());
        out.write(value.b.toBytes());
    }

    @Override
    public Fun.Tuple2<Long, Order> deserialize(DataInput in, int available) throws IOException {

        // READ LONG
        long number = in.readLong();

        // REST - as Order
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {
            return new Fun.Tuple2<Long, Order>(number,
                    Order.parse(bytes));
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
