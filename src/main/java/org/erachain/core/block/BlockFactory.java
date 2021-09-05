package org.erachain.core.block;

import com.google.common.primitives.Ints;

import java.util.Arrays;

public class BlockFactory {

    private static BlockFactory instance;

    private BlockFactory() {

    }

    public static BlockFactory getInstance() {
        if (instance == null) {
            instance = new BlockFactory();
        }

        return instance;
    }

    public Block parse(byte[] data, int height) throws Exception {
        if (true)
            return Block.parse(data, height);

        ///////// OLD VERSION

        //READ VERSION
        byte[] versionBytes = Arrays.copyOfRange(data, 0, Block.VERSION_LENGTH);
        int version = Ints.fromByteArray(versionBytes);

        if (version == 0) {
            //PARSE GENESIS BLOCK
            return GenesisBlock.parse(data, height);
        } else {
            //PARSE BLOCK
            return Block.parse(data, height);
        }
    }

}
