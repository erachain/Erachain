package org.erachain.dapp;

import org.erachain.core.block.Block;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;

public interface DAPPTimed {
    boolean processByTime(DCSet dcSet, Block block, Transaction transaction);

    void orphanByTime(DCSet dcSet, Block block, Transaction transaction);

}
