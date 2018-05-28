package network.message;


import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import core.block.Block;
import datachain.DCSet;

import java.util.Arrays;

public class BlockWinMessage extends Message {

    private static final int HEIGHT_LENGTH = 4;

    private Block block;
    private int height;

    public BlockWinMessage(Block block) {
        super(WIN_BLOCK_TYPE);

        this.block = block;
    }

    public static BlockWinMessage parse(byte[] data) throws Exception {
        //PARSE HEIGHT
        byte[] heightBytes = Arrays.copyOfRange(data, 0, HEIGHT_LENGTH);
        int height = Ints.fromByteArray(heightBytes);

        //PARSE BLOCK
        Block block = Block.parse(Arrays.copyOfRange(data, HEIGHT_LENGTH, data.length + 1), false);

        //CREATE MESSAGE
        BlockWinMessage message = new BlockWinMessage(block);
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
        return HEIGHT_LENGTH + this.block.getDataLength(false);
    }

}
