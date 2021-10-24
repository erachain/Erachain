package org.erachain.core.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.block.GenesisBlock;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.settings.Settings;
import org.junit.Before;
import org.junit.Test;

public class GenesisTransferAssetTransactionTest {

    DCSet dcSet;
    BlockChain chain;
    GenesisBlock gb;

    @Before
    public void init() {
        Settings.genesisStamp = Settings.DEFAULT_MAINNET_STAMP;
        dcSet = DCSet.createEmptyDatabaseSet(IDB.DBS_MAP_DB);
        try {
            chain = new BlockChain(dcSet);
        } catch (Exception e1) {
        }

        gb = chain.getGenesisBlock();

        Controller.getInstance().blockChain = chain;

    }

    @Test
    public void parse() {
    }

    @Test
    public void toBytes() {
        ;

        init();
        Transaction gsend = gb.getTransaction(111);

    }
}