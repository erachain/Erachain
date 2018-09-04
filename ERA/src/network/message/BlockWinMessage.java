package network.message;


import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import controller.Controller;
import core.block.Block;
import core.transaction.Transaction;
import datachain.DCSet;

import javax.naming.ldap.Control;
import java.awt.*;
import java.util.Arrays;

public class BlockWinMessage extends Message {

    private static final int HEIGHT_LENGTH = Block.HEIGHT_LENGTH;

    private Block block;
    private int height;

    public BlockWinMessage(Block block) {
        super(WIN_BLOCK_TYPE);

        this.block = block;
        this.height = block.heightBlock;
    }

    public static BlockWinMessage parse(byte[] data) throws Exception {
        //PARSE HEIGHT
        byte[] heightBytes = Arrays.copyOfRange(data, 0, HEIGHT_LENGTH);
        int height = Ints.fromByteArray(heightBytes);
        Block block = null;
        if (height == 0) {
            // from VER 4.10
            try {
                //PARSE BLOCK
                block = Block.parse(Arrays.copyOfRange(data, HEIGHT_LENGTH, data.length), 0);
            } catch (Exception e) {
                height = Controller.getInstance().getMyHeight() + 1;
                block = Block.parse(Arrays.copyOfRange(data, HEIGHT_LENGTH, data.length), height);
            }
        } else {
            //PARSE BLOCK
            block = Block.parse(Arrays.copyOfRange(data, HEIGHT_LENGTH, data.length), height);
        }

        //CREATE MESSAGE
        BlockWinMessage message = new BlockWinMessage(block);
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
        return HEIGHT_LENGTH + this.block.getDataLength(false);
    }

}
