package org.erachain.core.block;

import com.google.common.primitives.Ints;
import org.erachain.core.account.PublicKeyAccount;

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

    /*
    // not signed and not getGeneratingBalance
    public Block create(int version, byte[] reference, PublicKeyAccount generator, int height, byte[] unconfirmedTransactionsHash, byte[] atBytes) {
        return new Block(version, reference, generator, height, unconfirmedTransactionsHash, atBytes);
    }
    */
	
}
