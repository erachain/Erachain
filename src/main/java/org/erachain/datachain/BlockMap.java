package org.erachain.datachain;

import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.dbs.DBTab;

public interface BlockMap extends DBTab<Integer, Block> {

    int HEIGHT_INDEX = 1; // for GUI

    Block last();

    byte[] getLastBlockSignature();

    void setLastBlockSignature(byte[] signature);

    void resetLastBlockSignature();

    boolean isProcessing();

    void setProcessing(boolean processing);

    Block getWithMind(int height);

    Block get(Integer height);

    void put(Block block);

    void delete(byte[] signature, byte[] reference, PublicKeyAccount creator);

    void notifyResetChain();

    void notifyProcessChain(Block block);

    void notifyOrphanChain(Block block);
}
