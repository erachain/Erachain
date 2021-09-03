package org.erachain.network.message;


import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.core.block.Block;
import org.erachain.core.block.BlockFactory;

import java.util.Arrays;

public class BlockMessage extends Message {

    private static final int HEIGHT_LENGTH = Block.HEIGHT_LENGTH;

    private Block block;
    private int height;

    public BlockMessage(Block block) {
        super(BLOCK_TYPE);

        this.block = block;
        this.height = block.heightBlock;
    }

    public static BlockMessage parse(byte[] data) throws Exception {

        // TEST DATA LEN
        if (data.length == 0) {
            BlockMessage message = new BlockMessage(null);
            //message.height = -1;
            return message;
        }

        //PARSE HEIGHT
        byte[] heightBytes = Arrays.copyOfRange(data, 0, HEIGHT_LENGTH);
        int height = Ints.fromByteArray(heightBytes);

        //PARSE BLOCK
        Block block = BlockFactory.getInstance().parse(Arrays.copyOfRange(data, HEIGHT_LENGTH, data.length ), height);

        //CREATE MESSAGE
        BlockMessage message = new BlockMessage(block);
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
        byte[] heightBytes = Ints.toByteArray(this.height);
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
