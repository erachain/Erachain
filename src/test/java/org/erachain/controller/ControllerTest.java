package org.erachain.controller;

import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.RVouch;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.network.message.TransactionMessage;
import org.erachain.settings.Settings;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerTest.class.getSimpleName());

    byte[] seed = Crypto.getInstance().digest("tes213sdffsdft".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);

    byte[] signature;

    byte feePow = 0;
    Controller cnt;
    DCSet dcSet;
    BlockChain chain;
    GenesisBlock gb;

    private void init() {
        Settings.genesisStamp = Settings.DEFAULT_MAINNET_STAMP;

        DCSet.reCreateDBinMEmory(false, false);

        cnt = Controller.getInstance();
        cnt.setDCSet(dcSet);

        try {
            cnt.start();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            fail(e.getMessage());
        }

        chain = cnt.blockChain;
        dcSet = cnt.getDCSet();

        gb = chain.getGenesisBlock();

    }

    @Test
    public void checkInvalidSignature() {
        init();

        Transaction rVouch = new RVouch(maker, feePow, 1, 1,
                chain.getTimestamp(this.dcSet), 0L);
        rVouch.sign(maker, Transaction.FOR_NETWORK);

        signature = rVouch.getSignature();

        rVouch.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);

        // as NETWORK MESSAGE - WILL BE TEST SIGNATURE
        cnt.transactionsPool.processMessage(new TransactionMessage(rVouch));
        assertEquals(rVouch.errorValue, null);

        // make signature INVALID
        signature[2]++;

        // as NETWORK MESSAGE - WILL BE TEST SIGNATURE
        cnt.transactionsPool.processMessage(new TransactionMessage(rVouch));
        assertEquals(rVouch.errorValue, "INVALID SIGNATURE");

    }


}