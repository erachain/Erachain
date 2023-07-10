package org.erachain.network.message;


import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;

import java.util.Arrays;

public class BlockWinMessage extends Message {

    private static final int HEIGHT_LENGTH = Block.HEIGHT_LENGTH;
    private static final int HASH_POSITION = Block.HEIGHT_LENGTH + Block.VERSION_LENGTH + Block.REFERENCE_LENGTH
            + Block.CREATOR_LENGTH + Block.HEIGHT_LENGTH + Block.TRANSACTIONS_COUNT_LENGTH + Block.TRANSACTIONS_HASH_LENGTH
            + 10;

    private Block block;
    private int height;

    public BlockWinMessage(Block block) {
        super(WIN_BLOCK_TYPE);

        this.block = block;
        this.height = block.heightBlock;
    }

    @Override
    public boolean isHandled() { return true; }

    // берем создателя с транзакции и трансформируем в Целое
    public static Integer getHandledID(byte[] data) {

        return Ints.fromBytes(data[HASH_POSITION + 1], data[HASH_POSITION + 2], data[HASH_POSITION + 3], data[HASH_POSITION + 4]);

    }

    @Override
    public Integer getHandledID() {
        return getHandledID(this.getLoadBytes());
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

    protected byte[] generateChecksum(byte[] data) {
        this.loadBytes = data;
        byte[] checksum = Crypto.getInstance().digest(data);
        checksum = Arrays.copyOfRange(checksum, 0, CHECKSUM_LENGTH);
        return checksum;
    }

    @Override
    public int getDataLength() {
        return HEIGHT_LENGTH + this.block.getDataLength(false);
    }

}
