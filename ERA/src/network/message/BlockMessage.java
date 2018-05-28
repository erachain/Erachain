package network.message;


import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import core.block.Block;
import core.block.BlockFactory;
import datachain.DCSet;

import java.util.Arrays;

public class BlockMessage extends Message {

    private static final int HEIGHT_LENGTH = 4;

    private Block block;
    private int height;

    public BlockMessage(Block block) {
        super(BLOCK_TYPE);

        this.block = block;
    }

    public static BlockMessage parse(byte[] data) throws Exception {

        // TEST DATA LEN
        if (data.length == 0) {
            BlockMessage message = new BlockMessage(null);
            message.height = -1;
            return message;
        }

        //PARSE HEIGHT
        byte[] heightBytes = Arrays.copyOfRange(data, 0, HEIGHT_LENGTH);
        int height = Ints.fromByteArray(heightBytes);

        //PARSE BLOCK
        //Block block = Block.parse(Arrays.copyOfRange(data, HEIGHT_LENGTH, data.length + 1), false);
        Block block = BlockFactory.getInstance().parse(Arrays.copyOfRange(data, HEIGHT_LENGTH, data.length + 1), false);
        //block.getGeneratingBalance(dbSet);

        //CREATE MESSAGE
        BlockMessage message = new BlockMessage(block);
        message.height = height;
        return message;
    }

    public Block getBlock() {
        return this.block;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isRequest() {
        return false;
    }

    public byte[] toBytes() {
        byte[] data = new byte[0];

        if (this.block == null) {
            return data;
        }
        //WRITE BLOCK HEIGHT
        byte[] heightBytes = Ints.toByteArray(this.block.getHeightByParent(DCSet.getInstance()));
        data = Bytes.concat(data, heightBytes);

        //WRITE BLOCK
        byte[] blockBytes = this.block.toBytes(true, false);
        data = Bytes.concat(data, blockBytes);

        //ADD CHECKSUM
        data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);

        return data;
    }

    @Override
    public int getDataLength() {
        return HEIGHT_LENGTH + (this.block == null ? 0 : this.block.getDataLength(false));
    }

}
