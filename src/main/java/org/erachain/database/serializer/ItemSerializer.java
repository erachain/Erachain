package org.erachain.database.serializer;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.ItemFactory;
import org.erachain.core.transaction.Transaction;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class ItemSerializer implements Serializer<ItemCls>, Serializable {
    private static final long serialVersionUID = -6538913048331349777L;
    static Logger LOGGER = LoggerFactory.getLogger(ItemSerializer.class.getName());
    private int type;

    public ItemSerializer(int type) {
        this.type = type;
    }

    @Override
    public void serialize(DataOutput out, ItemCls value) throws IOException {
        out.writeInt(value.getDataLength(true));
        out.write(value.toBytes(Transaction.FOR_DB_RECORD, true, false));
    }

    @Override
    public ItemCls deserialize(DataInput in, int available) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {

            return ItemFactory.getInstance().parse(Transaction.FOR_DB_RECORD, this.type, bytes, true);
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
