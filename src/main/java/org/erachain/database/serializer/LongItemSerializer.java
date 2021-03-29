package org.erachain.database.serializer;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.ItemFactory;
import org.erachain.core.transaction.Transaction;
import org.mapdb.Fun;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class LongItemSerializer implements Serializer<Fun.Tuple2<Long, ItemCls>>, Serializable {
    private static final long serialVersionUID = -6538913048331349777L;
    static Logger LOGGER = LoggerFactory.getLogger(LongItemSerializer.class.getName());
    private int type;

    public LongItemSerializer(int type) {
        //super();
        this.type = type;
    }


    @Override
    public void serialize(DataOutput out, Fun.Tuple2<Long, ItemCls> value) throws IOException {
        out.writeLong(value.a);
        out.writeInt(value.b.getDataLength(true));
        out.write(value.b.toBytes(Transaction.FOR_DB_RECORD, true, false));
    }

    @Override
    public Fun.Tuple2<Long, ItemCls> deserialize(DataInput in, int available) throws IOException {

        // READ LONG
        long number = in.readLong();

        // READ LONG
        int length = in.readInt();

        // REST - as ItemCls
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {
            return new Fun.Tuple2<Long, ItemCls>(number,
                    ItemFactory.getInstance().parse(Transaction.FOR_DB_RECORD, this.type, bytes, true));

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
