package org.erachain.network.message;

import org.erachain.core.block.Block;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MessageTest {

    @Test
    public void setBytes() {
        //message.getBytes();
        Block block = null;
        Message message = new BlockWinMessage(block);
        Arrays.equals(message.toBytes(), message.getBytes());

    }

    @Test
    public void toBytes() {
    }
}