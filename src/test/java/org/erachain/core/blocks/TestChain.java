package org.erachain.core.blocks;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.BlocksHeadsMap;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Ignore
public class TestChain {

    static Logger LOGGER = LoggerFactory.getLogger(TestChain.class.getName());
    byte[] assetReference = new byte[64];
    Long releaserReference = null;
    BigDecimal BG_ZERO = BigDecimal.ZERO;
    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    //long ALIVE_KEY = StatusCls.ALIVE_KEY;
    byte FEE_POWER = (byte) 1;
    byte[] personReference = new byte[64];
    long timestamp = NTP.getTime();
    Long last_ref;
    boolean asPack = false;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    //int version = 0; // without signs of person
    int version = 1; // with signs of person
    private BlockChain blockChain;
    //CREATE EMPTY MEMORY DATABASE
    private DCSet dcSet;
    private GenesisBlock gb;

    // INIT PERSONS
    private void init() {

        //dcSet = DLSet.createEmptyDatabaseSet();
        dcSet = DCSet.getInstance();

    }

    @Test
    public void onMessage_GetSignatures() {

        init();

        // CREATE BLOCKCHAIN
        blockChain = Controller.getInstance().getBlockChain();

        Block block = Controller.getInstance().getBlockByHeight(dcSet, 2081);
        byte[] blockSignature = block.getSignature();

        // 	test controller.Controller.onMessage(Message) -> GET_SIGNATURES_TYPE
        List<byte[]> headers = blockChain
                .getSignatures(dcSet, blockSignature);

        assertEquals(30, headers.size());


    }


    @Test
    public void orphan_db() {

        init();

        // GET BLOCKCHAIN
        Controller.getInstance().initBlockChain(dcSet);
        gb = Controller.getInstance().getBlockChain().getGenesisBlock();
        blockChain = Controller.getInstance().getBlockChain();

        Block block = blockChain.getLastBlock(dcSet);
        int height = block.getHeight();
        Account creator = block.getCreator();
        Tuple3<Integer, Integer, Integer> forging = creator.getForgingData(dcSet, height);
        Tuple3<Integer, Integer, Integer> lastForging = creator.getLastForgingData(dcSet);

        DCSet fork = dcSet.fork(this.toString());

        try {
            block.orphan(fork);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Tuple3<Integer, Integer, Integer> forging_o = creator.getForgingData(dcSet, height);
        Tuple3<Integer, Integer, Integer> lastForging_o = creator.getLastForgingData(dcSet);
        int height_0 = block.getHeight();

        assertEquals(1, forging);


    }

    //
    // TEST WIN_VALUE and TOTAL WEIGHT for CHAIN id DB
    //
    @Test
    public void find_wrong_win_walue_db() {

        init();

        // GET BLOCKCHAIN
        Controller.getInstance().initBlockChain(dcSet);
        gb = Controller.getInstance().getBlockChain().getGenesisBlock();
        blockChain = Controller.getInstance().getBlockChain();
        BlocksHeadsMap dbHeight = dcSet.getBlocksHeadsMap();

        int lastH = 0;

        Block block;
        long totalWin = 0l;
        int i = 1;
        while (i <= blockChain.getHeight(dcSet)) {
            block = blockChain.getBlock(dcSet, i);
            int win_value = block.calcWinValueTargeted();
            long www = block.calcWinValue(dcSet);
            if (www != win_value) {
                //assertEquals(www, win_value);
                long diff = www - win_value;
                i = i + 1 - 1;
                win_value = block.calcWinValueTargeted();
                lastH = block.getCreator().getForgingData(dcSet, i).a;
                int h_i = lastH - 1;
                do {
                    lastH = block.getCreator().getForgingData(dcSet, h_i--).a;
                } while (lastH == -1);
                lastH = block.getCreator().getForgingData(dcSet, i + 1).a;
            }
            totalWin += win_value;
            i++;
        }

        long realWeight = blockChain.getFullWeight(dcSet);
        int diff = (int) (realWeight - totalWin);
        assertEquals(0, diff);

        assertEquals(realWeight, totalWin);

    }

}
