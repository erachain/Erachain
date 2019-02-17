package org.erachain.network.message;


import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.network.Peer;

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

    @Override
    public Long getHash() {
        return Longs.fromByteArray(this.block.getCreator().getShortAddressBytes());
    }

    public static boolean isHandled() { return true; }

    // берем создателя с транзакции и трансформируем в Целое
    public static Integer getHandledID(byte[] data) {

        // KEY BY CREATOR
        int position = Block.HEIGHT_LENGTH
                + Block.VERSION_LENGTH
                + Block.REFERENCE_LENGTH
                //+ Block.CREATOR_LENGTH
                //+ Block.HEIGHT_LENGTH
                //+ Block.TRANSACTIONS_HASH_LENGTH
                ;

        return Ints.fromBytes(data[position + 1], data[position + 2], data[position + 3], data[position + 4]);

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
