package org.erachain.core;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.DLSet;
import org.erachain.datachain.DCSet;
import org.erachain.network.Network;
import org.erachain.network.Peer;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

;

@Slf4j
public class SynchronizerTests {

    static Logger LOGGER = LoggerFactory.getLogger(SynchronizerTests.class.getName());

    //CREATE KNOWN ACCOUNT
    List<PrivateKeyAccount> privKeys = new ArrayList<>();

    Peer peer = new Peer(InetAddress.getByAddress(new byte[]{126, 2, 3, 4}));
    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();
    DCSet databaseSet;
    GenesisBlock genesisBlock;
    BlockChain blockChain;

    Fun.Tuple2<List<Transaction>, Integer> orderedTransactions = new Fun.Tuple2<>(new ArrayList<Transaction>(), 0);

    byte[] transactionsHash = new byte[Crypto.HASH_LENGTH];

    public SynchronizerTests() throws UnknownHostException {
    }

    @Before
    public void init() {
        Settings.genesisStamp = Settings.DEFAULT_DEMO_NET_STAMP;
        databaseSet = DCSet.createEmptyDatabaseSet(0);
        genesisBlock = new GenesisBlock();
        try {
            blockChain = new BlockChain(databaseSet);
        } catch (Exception e1) {
        }
        Controller.getInstance().blockChain = blockChain;

        //CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test 456".getBytes());
        int nonce = 0;
        while (nonce++ < 20) {
            privKeys.add(new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce)));
        }

    }

    @Ignore
    //TODO actualize the test
    @Test
    public void synchronizeNoCommonBlock() {

        //GENERATE 5 BLOCKS FROM ACCOUNT 1
        //GENERATE 5 NEXT BLOCKS
        Block lastBlock = genesisBlock;
        BlockGenerator blockGenerator = new BlockGenerator(databaseSet, blockChain, false);
        List<Block> firstBlocks = new ArrayList<Block>();
        for (int i = 0; i < 5; i++) {
            //GENERATE NEXT BLOCK
            Block newBlock = blockGenerator.generateNextBlock(privKeys.get(lastBlock.heightBlock),
                    lastBlock, orderedTransactions,
                    1000, 1000l, 1000l);

            //ADD TRANSACTION SIGNATURE
            //byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getSignature());
            newBlock.makeTransactionsRAWandHASH();

            //PROCESS NEW BLOCK
            try {
                newBlock.process(databaseSet, false);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //ADD TO LIST
            firstBlocks.add(newBlock);

            //LAST BLOCK IS NEW BLOCK
            lastBlock = newBlock;
        }

        Block lastOne = lastBlock;
        //GENERATE NEXT 5 BLOCK FROM ACCOUNT 2 ON FORK

        //FORK
        DCSet fork = databaseSet.fork(this.toString());
        lastBlock = fork.getBlockMap().last();

        BlockGenerator blockGeneratorFork = new BlockGenerator(fork, blockChain, false);
        //GENERATE NEXT 5 BLOCKS
        List<Block> newBlocks = new ArrayList<Block>();
        for (int i = 0; i < 5; i++) {
            //GENERATE NEXT BLOCK
            Block newBlock = blockGenerator.generateNextBlock(privKeys.get(lastBlock.heightBlock),
                    lastBlock, orderedTransactions,
                    1000, 1000l, 1000l);

            //ADD TRANSACTION SIGNATURE
            //byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getSignature());
            //newBlock.makeTransactionsHash();

            //PROCESS NEW BLOCK
            try {
                newBlock.process(fork, false);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //ADD TO LIST
            newBlocks.add(newBlock);

            //LAST BLOCK IS NEW BLOCK
            lastBlock = newBlock;
        }

        //SYNCHRONIZE DB FROM ACCOUNT 1 WITH NEXT 5 BLOCKS OF ACCOUNT 2
        Synchronizer synchronizer = new Synchronizer(Controller.getInstance());

        try {
            synchronizer.synchronizeNewBlocks(databaseSet, lastOne, 1, newBlocks, peer);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during synchronize: " + e.getMessage());
        }

        //CHECK LAST 5 BLOCKS
        lastBlock = databaseSet.getBlockMap().last();
        for (int i = 4; i >= 0; i--) {
            //CHECK LAST BLOCK
            assertEquals(true, Arrays.equals(newBlocks.get(i).getSignature(), lastBlock.getSignature()));
            lastBlock = lastBlock.getParent(databaseSet);
        }

        //CHECK LAST 5 BLOCKS
        for (int i = 4; i >= 0; i--) {
            //CHECK LAST BLOCK
            assertEquals(true, Arrays.equals(firstBlocks.get(i).getSignature(), lastBlock.getSignature()));
            lastBlock = lastBlock.getParent(databaseSet);
        }

        //CHECK LAST BLOCK
        assertEquals(true, Arrays.equals(lastBlock.getSignature(), genesisBlock.getSignature()));

        //CHECK HEIGHT
        assertEquals(11, databaseSet.getBlockMap().last().getHeight());
    }

    @Test
    public void synchronizeCommonBlock() {

        //GENERATE 5 BLOCKS FROM ACCOUNT 1
        DCSet databaseSet1 = DCSet.createEmptyDatabaseSet(0);
        DCSet databaseSet2 = DCSet.createEmptyDatabaseSet(0);

        GenesisBlock gb1;
        GenesisBlock gb2;

        //PROCESS GENESISBLOCK
        GenesisBlock genesisBlock = new GenesisBlock();
        try {
            Controller.getInstance().initBlockChain(databaseSet1);
            gb1 = Controller.getInstance().getBlockChain().getGenesisBlock();

            //Controller.getInstance().initBlockChain(databaseSet2);
            gb2 = Controller.getInstance().getBlockChain().getGenesisBlock();

            genesisBlock.process(databaseSet1, false);
            //genesisBlock.process(databaseSet2);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }

        try {
            blockChain = new BlockChain(databaseSet);
        } catch (Exception e1) {
        }

        //GENERATE 5 NEXT BLOCKS
        Block lastBlock = gb1;
        BlockGenerator blockGenerator1 = new BlockGenerator(databaseSet1, null, false);
        for (int i = 0; i < 5; i++) {
            //GENERATE NEXT BLOCK
            Block newBlock = blockGenerator1.generateNextBlock(privKeys.get(lastBlock.heightBlock),
                    lastBlock, orderedTransactions,
                    1000, 1000l, 1000l);

            //ADD TRANSACTION SIGNATURE
            //byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getSignature());
            newBlock.makeTransactionsRAWandHASH();

            //PROCESS NEW BLOCK
            try {
                newBlock.process(databaseSet1, false);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                fail(e.getMessage());
            }

            //LAST BLOCK IS NEW BLOCK
            lastBlock = newBlock;
        }

        //GENERATE NEXT 10 BLOCKS
        lastBlock = gb2;
        BlockGenerator blockGenerator2 = new BlockGenerator(databaseSet2, null, false);
        List<Block> newBlocks = new ArrayList<Block>();
        for (int i = 0; i < 10; i++) {
            //GENERATE NEXT BLOCK
            Block newBlock = blockGenerator2.generateNextBlock(privKeys.get(lastBlock.heightBlock),
                    lastBlock, orderedTransactions,
                    1000, 1000l, 1000l);

            //ADD TRANSACTION SIGNATURE
            //byte[] transactionsSignature = Crypto.getInstance().sign(generator2, newBlock.getSignature());
            newBlock.makeTransactionsRAWandHASH();

            //PROCESS NEW BLOCK
            try {
                //newBlock.process(databaseSet2);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                fail(e.getMessage());
            }

            //ADD TO LIST
            newBlocks.add(newBlock);

            //LAST BLOCK IS NEW BLOCK
            lastBlock = newBlock;
        }

        //SYNCHRONIZE DB FROM ACCOUNT 1 WITH NEXT 5 BLOCKS OF ACCOUNT 2
        Synchronizer synchronizer = new Synchronizer(Controller.getInstance());

        try {
            synchronizer.synchronizeNewBlocks(databaseSet1, gb1, 1, newBlocks, null);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
            fail("Exception during synchronize:" + e.getMessage());
        }

        //CHECK BLOCKS
        lastBlock = databaseSet1.getBlockMap().last();
        for (int i = 9; i >= 0; i--) {
            //CHECK LAST BLOCK
            assertEquals(true, Arrays.equals(newBlocks.get(i).getSignature(), lastBlock.getSignature()));
            lastBlock = lastBlock.getParent(databaseSet1);
        }

        //CHECK LAST BLOCK
        assertEquals(true, Arrays.equals(lastBlock.getSignature(), gb1.getSignature()));

        //CHECK HEIGHT
        assertEquals(11, databaseSet1.getBlockMap().last().getHeight());
    }

    /**
     * Проверка всех подписей в цепочке
     */
    @Test
    public void checkMyHeaders() {

        Controller cnt = Controller.getInstance();

        DCSet dcSet = null;
        try {

            cnt.dlSet = new DLSet(null, DCSet.makeDBinMemory(), false, false);

            dcSet = DCSet.getInstance(false, false, false);
            cnt.setDCSet(dcSet);

            cnt.network = new Network(cnt);

            // CREATE BLOCKCHAIN
            cnt.blockChain = new BlockChain(dcSet);

            // CREATE SYNCHRONIZOR
            cnt.synchronizer = new Synchronizer(cnt);

            Peer peer = null;
            try {
                peer = new Peer(InetAddress.getByName("89.235.184.229"));
            } catch (Exception e) {
                assertEquals("", "not connected");
            }

            //cnt.network.addPeer(peer, 0);

            if (!peer.connect(null, cnt.network, "connected TEST ")) {
                assertEquals("", "not connected");
            }

            List<byte[]> headers = null;

            int myHeight = dcSet.getBlockSignsMap().size();
            logger.info(" start TO: " + myHeight);
            int height = 1;
            byte[] lastSignature = cnt.blockChain.getGenesisBlock().getSignature();
            while (myHeight > height) {
                try {
                    headers = cnt.synchronizer.getBlockSignatures(lastSignature, peer);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                for (byte[] signature : headers) {
                    if (dcSet.getBlockSignsMap().get(signature) == null) {
                        logger.error("HEIGHT fork: " + height);
                    }
                    lastSignature = signature;
                    height++;
                }

                if (height % 1000 == 0) {
                    logger.info(" checked: " + height);
                }

            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            dcSet.close();
            cnt.dlSet.close();
        }
    }

    // Yandex test
    private void gen(String res, int open, int closed, int n) {
        if (res.length() == 2 * n) {
            if (open == closed) {
                System.out.println(res);
            }
            return;
        }
        gen(res + "(", open + 1, closed, n);

        if (closed < open)
            gen(res + ")", open, closed + 1, n);

    }

    @Test
    public void testParens() {

        gen("", 0, 0, 3);

    }
}