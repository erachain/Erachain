package database.serializer;

import core.block.Block;
import core.block.BlockFactory;
import org.apache.log4j.Logger;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class BlockSerializer implements Serializer<Block>, Serializable {
    private static final long serialVersionUID = -6538913048331349777L;
    static Logger LOGGER = Logger.getLogger(BlockSerializer.class.getName());

    @Override
    public void serialize(DataOutput out, Block value) throws IOException {
        out.writeInt(value.getDataLength(true));
        out.write(value.toBytes(true, true));
    }

    @Override
    public Block deserialize(DataInput in, int available) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try {
            return BlockFactory.getInstance().parse(bytes, 0);
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
