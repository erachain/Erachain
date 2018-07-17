package test.blocks;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.persons.PersonCls;
import core.transaction.IssuePersonRecord;
import core.transaction.R_SertifyPubKeys;
import core.transaction.Transaction;
import datachain.BlocksHeadsMap;
import datachain.DCSet;
import ntp.NTP;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Ignore
public class TestChain {

    static Logger LOGGER = Logger.getLogger(TestChain.class.getName());
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
    PersonCls personGeneral;
    PersonCls person;
    long personKey = -1;
    IssuePersonRecord issuePersonTransaction;
    R_SertifyPubKeys r_SertifyPubKeys;
    //int version = 0; // without signs of person
    int version = 1; // with signs of person
    private BlockChain blockChain;
    //CREATE EMPTY MEMORY DATABASE
    private DCSet dcSet;
    private GenesisBlock gb;

    // INIT PERSONS
    private void init() {

        //dcSet = DBSet.createEmptyDatabaseSet();
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
        int height = block.getHeight(dcSet);
        Account creator = block.getCreator();
        Tuple2<Integer, Integer> forging = creator.getForgingData(dcSet, height);
        Tuple2<Integer, Integer> lastForging = creator.getLastForgingData(dcSet);

        DCSet fork = dcSet.fork();

        try {
            block.orphan(fork);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Tuple2<Integer, Integer> forging_o = creator.getForgingData(dcSet, height);
        Tuple2<Integer, Integer> lastForging_o = creator.getLastForgingData(dcSet);
        int height_0 = block.getHeight(dcSet);

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
