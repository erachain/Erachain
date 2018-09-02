package test.blocks;

import controller.Controller;
import core.BlockChain;
import core.BlockGenerator;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.transaction.GenesisTransferAssetTransaction;
import core.transaction.R_Send;
import core.transaction.Transaction;
import core.wallet.Wallet;
import datachain.DCSet;
import ntp.NTP;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

//import core.transaction.GenesisTransaction;
@Ignore
public class GeneratorTests {

    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[Crypto.SIGNATURE_LENGTH];
    long timestamp = NTP.getTime();
    Controller cntrlr = Controller.getInstance();
    private DCSet db;
    long flags = 0l;

    PrivateKeyAccount generator1;

    List<Transaction> transactions = new ArrayList<Transaction>();
    Fun.Tuple2<List<Transaction>, Integer> orderedTransactions = new Fun.Tuple2<>(new ArrayList<Transaction>(), 0);
    byte[] transactionsHash = new byte[Crypto.HASH_LENGTH];

    //CREATE EMPTY MEMORY DATABASE
    DCSet dcSet = DCSet.createEmptyDatabaseSet();

    @Before
    public void init() {
        db = DCSet.createEmptyDatabaseSet();
    }


    @Test
    public void generateNewBlock() {

        //PROCESS GENESISBLOCK
        GenesisBlock genesisBlock = new GenesisBlock();
        Controller.getInstance().initBlockChain(dcSet);
        try {
            genesisBlock.process(dcSet);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


        //CREATE KNOWN ACCOUNT
        int nonce = 1;
        //CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test".getBytes());
        PrivateKeyAccount generator1 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
        PrivateKeyAccount generator2 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
        PrivateKeyAccount generator3 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
        PrivateKeyAccount generator4 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));

        int height = 1;
        int seq = 1;
        PrivateKeyAccount generator;
        //PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
        // AND LAST REFERENCE
        // AND WIN_DATA
        Transaction transaction;
        transaction = new GenesisTransferAssetTransaction(generator1, ERM_KEY, BigDecimal.valueOf(1000000));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        transaction = new GenesisTransferAssetTransaction(generator2, ERM_KEY, BigDecimal.valueOf(10000000));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        transaction = new GenesisTransferAssetTransaction(generator3, ERM_KEY, BigDecimal.valueOf(300000));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        transaction = new GenesisTransferAssetTransaction(generator4, ERM_KEY, BigDecimal.valueOf(3000000));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        transaction = new GenesisTransferAssetTransaction(generator1, FEE_KEY, BigDecimal.valueOf(10));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        assertEquals(1000000, generator1.getBalanceUSE(ERM_KEY, dcSet).longValue());

        //GENERATE 2000 NEXT BLOCKS
        Block lastBlock = genesisBlock;
        for (int i = 0; i < 200; i++) {

            if (NTP.getTime() - lastBlock.getTimestamp() < BlockChain.GENERATING_MIN_BLOCK_TIME_MS) {
                break;
            }

            if (i < 10)
                generator = generator1;
            else if (i < 30)
                generator = generator2;
            else if (i < 40)
                generator = generator3;
            else if (i < 100)
                generator = generator1;
            else
                generator = generator4;

            //GENERATE NEXT BLOCK
            Block newBlock = BlockGenerator.generateNextBlock(dcSet, generator,
                    lastBlock, orderedTransactions,
                    height,  1000, 1000l, 1000l);

            //ADD TRANSACTION SIGNATURE
            //byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getSignature());
            newBlock.makeTransactionsHash();
            newBlock.sign(generator);

            //CHECK IF BLOCK SIGNATURE IS VALID
            assertEquals(true, newBlock.isSignatureValid());

            //CHECK IF BLOCK IS VALID
            if (!newBlock.isValid(dcSet, false))
                assertEquals(true, newBlock.isValid(dcSet, false));

            height = Controller.getInstance().getMyHeight();
            assertEquals(height, i + 1);

			/*
			if (i == 0) {
				assertEquals(1464, generator1.calcWinValueTargeted(dcSet, height));
				assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
			}
			else if (i == 1) {
				assertEquals(1464, generator1.calcWinValueTargeted(dcSet, height));
				assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
			}
			else if (i == 2) {
				assertEquals(1464, generator1.calcWinValueTargeted(dcSet, height));
				assertEquals(15716, generator2.calcWinValueTargeted(dcSet, height));
			}
			else if (i == 9) {
				assertEquals(1464, generator1.calcWinValueTargeted(dcSet, height));
				assertEquals(23803, generator2.calcWinValueTargeted(dcSet, height));
			}
			else if (i == 10) {
				assertEquals(1464, generator1.calcWinValueTargeted(dcSet, height));
				assertEquals(25024, generator2.calcWinValueTargeted(dcSet, height));
			}
			else if (i == 11) {
				assertEquals(1464, generator1.calcWinValueTargeted(dcSet, height));
				assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
			}
			else if (i == 12) {
				assertEquals(1571, generator1.calcWinValueTargeted(dcSet, height));
				assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
			}
			else if (i == 29) {
				assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
				assertEquals(2018, generator3.calcWinValueTargeted(dcSet, height));
			}
			else if (i == 30) {
				assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
				assertEquals(2124, generator3.calcWinValueTargeted(dcSet, height));
			}
			else if (i == 31) {
				assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
				assertEquals(439, generator3.calcWinValueTargeted(dcSet, height));
			}
			else if (i == 39) {
				assertEquals(439, generator3.calcWinValueTargeted(dcSet, height));
				assertEquals(6729, generator1.calcWinValueTargeted(dcSet, height));
			}
			else if (i == 40) {
				assertEquals(439, generator3.calcWinValueTargeted(dcSet, height));
				assertEquals(7080, generator1.calcWinValueTargeted(dcSet, height));
			}
			else if (i == 41) {
				assertEquals(439, generator3.calcWinValueTargeted(dcSet, height));
				assertEquals(1464, generator1.calcWinValueTargeted(dcSet, height));
			}
			else if (i == 42) {
				assertEquals(439, generator3.calcWinValueTargeted(dcSet, height));
				assertEquals(439, generator1.calcWinValueTargeted(dcSet, height));
			}
			 */


            //PROCESS NEW BLOCK
            try {
                newBlock.process(dcSet);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            height = newBlock.getHeight();
            assertEquals(height, i + 2);

            if (i == 0) {
                //assertEquals(1464, generator1.calcWinValueTargeted(dcSet, height));
                assertEquals(1024, newBlock.calcWinValueTargeted());
                //assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
            } else if (i == 1) {
                //assertEquals(1464, generator1.calcWinValueTargeted(dcSet, height));
                assertEquals(1464, newBlock.calcWinValueTargeted());
                //assertEquals(15716, generator2.calcWinValueTargeted(dcSet, height));
            } else if (i == 2) {
                //assertEquals(1464, generator1.calcWinValueTargeted(dcSet, height));
                assertEquals(1464, newBlock.calcWinValueTargeted());
                //assertEquals(16784, generator2.calcWinValueTargeted(dcSet, height));
            } else if (i == 9) {
                //assertEquals(1464, generator1.calcWinValueTargeted(dcSet, height));
                assertEquals(1464, newBlock.calcWinValueTargeted());
                //assertEquals(25024, generator2.calcWinValueTargeted(dcSet, height));
            } else if (i == 10) {
                //assertEquals(1464, generator1.calcWinValueTargeted(dcSet, height));
                assertEquals(25024, newBlock.calcWinValueTargeted());
                //assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
            } else if (i == 11) {
                //assertEquals(1571, generator1.calcWinValueTargeted(dcSet, height));
                assertEquals(14648, newBlock.calcWinValueTargeted());
                //assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
            } else if (i == 12) {
                //assertEquals(1678, generator1.calcWinValueTargeted(dcSet, height));
                assertEquals(14648, newBlock.calcWinValueTargeted());
                //assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
            } else if (i == 29) {
                //assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
                assertEquals(14648, newBlock.calcWinValueTargeted());
                //assertEquals(2124, generator3.calcWinValueTargeted(dcSet, height));
            } else if (i == 30) {
                //assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
                assertEquals(2124, newBlock.calcWinValueTargeted());
                //assertEquals(439, generator3.calcWinValueTargeted(dcSet, height));
            } else if (i == 31) {
                //assertEquals(15716, generator2.calcWinValueTargeted(dcSet, height));
                assertEquals(439, newBlock.calcWinValueTargeted());
                //assertEquals(439, generator3.calcWinValueTargeted(dcSet, height));
            } else if (i == 39) {
                //assertEquals(439, generator3.calcWinValueTargeted(dcSet, height));
                assertEquals(439, newBlock.calcWinValueTargeted());
                //assertEquals(7080, generator1.calcWinValueTargeted(dcSet, height));
            } else if (i == 40) {
                //assertEquals(439, generator3.calcWinValueTargeted(dcSet, height));
                assertEquals(7080, newBlock.calcWinValueTargeted());
                //assertEquals(1464, generator1.calcWinValueTargeted(dcSet, height));
            } else if (i == 41) {
                //assertEquals(471, generator3.calcWinValueTargeted(dcSet, height));
                assertEquals(1464, newBlock.calcWinValueTargeted());
                //assertEquals(1464, generator1.calcWinValueTargeted(dcSet, height));
            }

            //LAST BLOCK IS NEW BLOCK
            lastBlock = newBlock;
        }
    }

    @Test
    public void fullWeight() {

        //PROCESS GENESISBLOCK
        GenesisBlock genesisBlock = new GenesisBlock();
        cntrlr.initBlockChain(dcSet);
        // already processed genesisBlock.process(dcSet);


        //CREATE KNOWN ACCOUNT
        int nonce = 1;
        //CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test".getBytes());
        PrivateKeyAccount generator0 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
        PrivateKeyAccount generator1 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
        PrivateKeyAccount generator2 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
        PrivateKeyAccount generator3 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
        PrivateKeyAccount generator4 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
        PrivateKeyAccount generator5 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
        PrivateKeyAccount generator6 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
        PrivateKeyAccount generator7 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
        PrivateKeyAccount generator8 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
        PrivateKeyAccount generator9 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));

        int height = 1;
        int seq = 1;
        PrivateKeyAccount generator;
        //PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
        // AND LAST REFERENCE
        // AND WIN_DATA
        Transaction transaction;
        transaction = new GenesisTransferAssetTransaction(generator0, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL / 10));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        transaction = new GenesisTransferAssetTransaction(generator1, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL / 10));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        transaction = new GenesisTransferAssetTransaction(generator2, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL / 20));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        transaction = new GenesisTransferAssetTransaction(generator3, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL * 0.045));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        transaction = new GenesisTransferAssetTransaction(generator4, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL * 0.045));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        transaction = new GenesisTransferAssetTransaction(generator5, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL * 0.03));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        transaction = new GenesisTransferAssetTransaction(generator6, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL * 0.02));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        transaction = new GenesisTransferAssetTransaction(generator7, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL * 0.02));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        transaction = new GenesisTransferAssetTransaction(generator8, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL * 0.03));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        transaction = new GenesisTransferAssetTransaction(generator9, ERM_KEY, BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL * 0.033));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        ///////////////
        transaction = new GenesisTransferAssetTransaction(generator1, FEE_KEY, BigDecimal.valueOf(10));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        dcSet.getTransactionFinalMap().add(height, seq++, transaction);

        assertEquals(1000000, generator1.getBalanceUSE(ERM_KEY, dcSet).longValue());

        TreeMap<Integer, Tuple2<Integer, Long>> buffer = new TreeMap();

        // break on NUM
        int i_break = 3;

        //GENERATE 2000 NEXT BLOCKS
        Block lastBlock = genesisBlock;
        for (int i = 2; i < 120; i++) {

            if (NTP.getTime() - lastBlock.getTimestamp() < BlockChain.GENERATING_MIN_BLOCK_TIME_MS) {
                break;
            }

            if (i % 10 == 0)
                generator = generator0;
            else if (i % 10 == 1)
                generator = generator1;
            else if (i % 10 == 2)
                generator = generator2;
            else if (i % 10 == 3)
                generator = generator3;
            else if (i % 10 == 4)
                generator = generator4;
            else if (i % 10 == 5)
                generator = generator5;
            else if (i % 10 == 6)
                generator = generator6;
            else if (i % 10 == 7)
                generator = generator7;
            else if (i % 10 == 8)
                generator = generator8;
            else if (i % 10 == 9)
                generator = generator9;
            else
                generator = generator0;

            //GENERATE NEXT BLOCK
            Block newBlock = BlockGenerator.generateNextBlock(dcSet, generator,
                    lastBlock, orderedTransactions,
                    height,  1000, 1000l, 1000l);

            //ADD TRANSACTION SIGNATURE
            byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getSignature());
            newBlock.makeTransactionsHash();
            newBlock.sign(generator);

            //CHECK IF BLOCK SIGNATURE IS VALID
            assertEquals(true, newBlock.isSignatureValid());

            long weight_old = newBlock.calcWinValueTargeted();
            //CHECK IF BLOCK IS VALID
            if (!newBlock.isValid(dcSet, false))
                assertEquals(false, newBlock.isValid(dcSet, false));

            Tuple2<Integer, Long> hWeight_old = cntrlr.getBlockChain().getHWeightFull(dcSet);

            if (i == 2) {
                // for debug breakpoint
                i_break++;
            }

            //PROCESS NEW BLOCK
            try {
                newBlock.process(dcSet);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            long weight = newBlock.calcWinValueTargeted();
            if (weight != weight_old) {
                i_break++;
            }
            assertEquals(weight, weight_old);

            height = newBlock.getHeight();

            Tuple2<Integer, Long> hWeight = cntrlr.getBlockChain().getHWeightFull(dcSet);

            assertEquals(hWeight_old.b + weight_old, (long) hWeight.b);

            assertEquals((int) hWeight.a, hWeight_old.a + 1);
            assertEquals((int) hWeight.a, height);

            buffer.put(height, hWeight);

            if (i == 2) {
                assertEquals(8192, newBlock.calcWinValueTargeted());
                //assertEquals(14648, generator2.calcWinValueTargetedTargeted(dcSet, height));
                assertEquals((long) hWeight.b, 9192); // GENESIS + new first block
            } else if (i == 3) {
                assertEquals(7369, newBlock.calcWinValueTargeted());
                assertEquals((int) hWeight.a, height);
                assertEquals((long) hWeight.b, 9192 + 7369);
            } else if (i == 4) {
                assertEquals(15522, newBlock.calcWinValueTargeted());
                assertEquals((long) hWeight.b, 9192 + 7369 + 15522);
            } else if (i == 11) {
                assertEquals(18873, newBlock.calcWinValueTargeted());
                assertEquals((long) hWeight.b, 130617);
                //assertEquals(25024, generator2.calcWinValueTargeted(dcSet, height));
            } else if (i == 12) {
                assertEquals(15963, newBlock.calcWinValueTargeted());
                //assertEquals(26245, generator2.calcWinValueTargeted(dcSet, height));
            } else if (i == 13) {
                //assertEquals(1678, generator1.calcWinValueTargeted(dcSet, height));
                assertEquals(13226, newBlock.calcWinValueTargeted());
                //assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
            } else if (i == 14) {
                //assertEquals(1785, generator1.calcWinValueTargeted(dcSet, height));
                assertEquals(12581, newBlock.calcWinValueTargeted());
                //assertEquals(14648, generator2.calcWinValueTargeted(dcSet, height));
            } else if (i == 30) {
                //assertEquals(21240, generator3.calcWinValueTargeted(dcSet, height));
                assertEquals(13911, newBlock.calcWinValueTargeted());
            } else if (i == 31) {
                //assertEquals(22338, generator3.calcWinValueTargeted(dcSet, height));
                assertEquals(13578, newBlock.calcWinValueTargeted());
            } else if (i == 32) {
                //assertEquals(4394, generator3.calcWinValueTargeted(dcSet, height));
                assertEquals(10218, newBlock.calcWinValueTargeted());
            } else if (i == 13) {
            }

            //LAST BLOCK IS NEW BLOCK
            lastBlock = newBlock;
        }

        ////////////////////////////
        // P
        for (int i_height = height; i_height > 32; i_height--) {

            if (i_height == 102) {
                i_break++;
            }


            //GENERATE NEXT BLOCK
            Block block = dcSet.getBlockMap().get(i_height);
            if (block == null) {
                i_break++;
            }

            Tuple2<Integer, Long> hWeight_old = cntrlr.getBlockChain().getHWeightFull(dcSet);
            long weight = block.calcWinValueTargeted();

            //PRPHAN BLOCK
            try {
                block.orphan(dcSet);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            int height_bad = block.getHeight();
            assertEquals(-1, height_bad);

            Block parent = block.getParent(dcSet);
            int parentHeight = parent.getHeight();

            Tuple2<Integer, Long> hWeight = cntrlr.getBlockChain().getHWeightFull(dcSet);

            assertEquals((int) hWeight.a, parentHeight);

            if (hWeight_old.b - weight != hWeight.b) {
                int ii = 2;
            }
            assertEquals(hWeight_old.b - weight, (long) hWeight.b);

            assertEquals(buffer.get(parentHeight).b, hWeight.b);

            if (i_height == 0) {
                assertEquals(1464, block.calcWinValueTargeted());
                //assertEquals(14648, generator2.calcWinValueTargeted(dcSet, i_height));
                assertEquals((long) hWeight.b, 1464);
            } else if (i_height == 1) {
                assertEquals(1464, block.calcWinValueTargeted());
                //assertEquals(15716, generator2.calcWinValueTargeted(dcSet, i_height));
                assertEquals((int) hWeight.a, i_height);
                assertEquals((long) hWeight.b, 1464 + 1464);
            } else if (i_height == 2) {
                assertEquals(1464, block.calcWinValueTargeted());
                assertEquals((long) hWeight.b, (i_height - 1) * 1464);
            } else if (i_height == 9) {
            } else if (i_height == 10) {
            } else if (i_height == 11) {
            }

            //LAST BLOCK IS NEW BLOCK
            //lastBlock = block;
        }

        height = i_break++;

    }

    @Test
    public void addTransactions() {
        // TEN


        //PROCESS GENESISBLOCK
        GenesisBlock genesisBlock = new GenesisBlock();
        try {
            genesisBlock.process(dcSet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);

        //PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
        //Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(100000), NTP.getTime());
        //transaction.process(databaseSet, false);
        generator.setLastTimestamp(genesisBlock.getTimestamp(), dcSet);
        generator.changeBalance(dcSet, false, ERM_KEY, BigDecimal.valueOf(10000), false);
        generator.changeBalance(dcSet, false, FEE_KEY, BigDecimal.valueOf(10000), false);

        //GENERATE NEXT BLOCK
        BlockGenerator blockGenerator = new BlockGenerator(false);
        Block newBlock = BlockGenerator.generateNextBlock(dcSet, generator,
                genesisBlock, orderedTransactions,
                2,  1000, 1000l, 1000l);

        // get timestamp for block
        long timestamp = newBlock.getTimestamp() - BlockChain.GENERATING_MIN_BLOCK_TIME_MS / 2;

        //ADD 10 UNCONFIRMED VALID TRANSACTIONS
        Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
        DCSet snapshot = dcSet.fork();
        for (int i = 0; i < 10; i++) {

            //CREATE VALID PAYMENT
            Transaction payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(0.01), timestamp++, generator.getLastTimestamp(snapshot));
            payment.sign(generator, Transaction.FOR_NETWORK);

            //PROCESS IN DB
            payment.process(genesisBlock, Transaction.FOR_NETWORK);

            //ADD TO UNCONFIRMED TRANSACTIONS
            dcSet.getTransactionMap().add(payment);

        }

        Tuple2<List<Transaction>, Integer> transactionsItem = BlockGenerator.getUnconfirmedTransactions(dcSet, newBlock.getTimestamp(), null, 0l);
        transactions = transactionsItem.a;
        // CALCULATE HASH for that transactions
        byte[] transactionsHash = Block.makeTransactionsHash(generator.getPrivateKey(), transactions, null);

        //ADD UNCONFIRMED TRANSACTIONS TO BLOCK
        newBlock = BlockGenerator.generateNextBlock(dcSet, generator,
                genesisBlock, orderedTransactions,
                2,  1000, 1000l, 1000l);
        newBlock.setTransactions(transactions);

        //CHECK IF BLOCK IS VALID
        assertEquals(true, newBlock.isValid(dcSet, false));

        newBlock.sign(generator);
        //CHECK IF BLOCK IS VALID
        assertEquals(true, newBlock.isSignatureValid());
    }

    @Test
    public void addManyTransactions() {

        //PROCESS GENESISBLOCK
        GenesisBlock genesisBlock = new GenesisBlock();
        try {
            genesisBlock.process(dcSet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);

        //PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
        //Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(100000), NTP.getTime());
        //transaction.process(databaseSet, false);
        generator.setLastTimestamp(genesisBlock.getTimestamp(), dcSet);
        generator.changeBalance(dcSet, false, ERM_KEY, BigDecimal.valueOf(10000), false);
        generator.changeBalance(dcSet, false, FEE_KEY, BigDecimal.valueOf(100000), false);


        //GENERATE NEXT BLOCK
        BlockGenerator blockGenerator = new BlockGenerator(false);
        Block newBlock = BlockGenerator.generateNextBlock(dcSet, generator,
                genesisBlock, orderedTransactions,
                2,  1000, 1000l, 1000l);

        // get timestamp for block
        long timestampStart = newBlock.getTimestamp() - BlockChain.GENERATING_MIN_BLOCK_TIME_MS / 2;
        long timestamp = timestampStart;

        //ADD 10000 UNCONFIRMED VALID TRANSACTIONS
        Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
        DCSet snapshot = dcSet.fork();
        int max_count = 2000;
        for (int i = 0; i < max_count; i++) {

            //CREATE VALID PAYMENT
            Transaction payment = new R_Send(generator, FEE_POWER, recipient, FEE_KEY, BigDecimal.valueOf(0.001),
                    "sss", new byte[3000], new byte[]{1}, new byte[]{0},
                    timestamp++, generator.getLastTimestamp(snapshot));

            payment.setDC(db, Transaction.FOR_NETWORK, 1, 1);
            payment.sign(generator, Transaction.FOR_NETWORK);
            assertEquals(payment.isValid(Transaction.FOR_NETWORK, flags), Transaction.VALIDATE_OK);

            //PROCESS IN DB
            payment.process(genesisBlock, Transaction.FOR_NETWORK);

            //ADD TO UNCONFIRMED TRANSACTIONS
            dcSet.getTransactionMap().add(payment);

        }

        //ADD UNCONFIRMED TRANSACTIONS TO BLOCK
        transactions = BlockGenerator.getUnconfirmedTransactions(dcSet, newBlock.getTimestamp(), null, 0l).a;

        //CHECK THAT NOT ALL TRANSACTIONS WERE ADDED TO BLOCK
        assertEquals(true, max_count > transactions.size());

        // CALCULATE HASH for that transactions
        byte[] transactionsHash = Block.makeTransactionsHash(generator.getPrivateKey(), transactions, null);

        //ADD UNCONFIRMED TRANSACTIONS TO BLOCK
        newBlock = BlockGenerator.generateNextBlock(dcSet, generator,
                genesisBlock, orderedTransactions,
                2,  1000, 1000l, 1000l);
        newBlock.setTransactions(transactions);

        //CHECK THAT NOT ALL TRANSACTIONS WERE ADDED TO BLOCK
        assertEquals(transactions.size(), newBlock.getTransactionCount());

        //CHECK IF BLOCK IS VALID
        assertEquals(true, newBlock.isValid(dcSet, false));
    }

    @Test
    public void winValues() {
        // as win values updated on block process

        //PROCESS GENESISBLOCK
        GenesisBlock genesisBlock = new GenesisBlock();
        try {
            genesisBlock.process(dcSet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int nonce = 1;
        //CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test".getBytes());
        PrivateKeyAccount userAccount1 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));
        PrivateKeyAccount userAccount2 = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce++));

        //PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
        Transaction transaction = new GenesisTransferAssetTransaction(userAccount1, ERM_KEY, BigDecimal.valueOf(100000));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        transaction = new GenesisTransferAssetTransaction(userAccount1, FEE_KEY, BigDecimal.valueOf(1));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        transaction = new GenesisTransferAssetTransaction(userAccount2, ERM_KEY, BigDecimal.valueOf(10000));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);
        transaction = new GenesisTransferAssetTransaction(userAccount2, FEE_KEY, BigDecimal.valueOf(1));
        transaction.process(genesisBlock, Transaction.FOR_NETWORK);


        //GENERATE NEXT BLOCK
        BlockGenerator blockGenerator = new BlockGenerator(false);
        Block newBlock = BlockGenerator.generateNextBlock(dcSet, userAccount1,
                genesisBlock, orderedTransactions,
                2,  1000, 1000l, 1000l);

        // get timestamp for block
        long timestampStart = newBlock.getTimestamp() - BlockChain.GENERATING_MIN_BLOCK_TIME_MS / 2;
        long timestamp = timestampStart;

        Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
        Transaction payment = new R_Send(userAccount1, FEE_POWER, recipient, ERM_KEY, BigDecimal.valueOf(2000),
                timestamp++, userAccount1.getLastTimestamp(dcSet));
        payment.sign(userAccount1, Transaction.FOR_NETWORK);
        assertEquals(payment.isValid(Transaction.FOR_NETWORK, flags), Transaction.VALIDATE_OK);

        //ADD TO UNCONFIRMED TRANSACTIONS
        dcSet.getTransactionMap().add(payment);

        //ADD UNCONFIRMED TRANSACTIONS TO BLOCK
        transactions = BlockGenerator.getUnconfirmedTransactions(dcSet, newBlock.getTimestamp(), null, 0l).a;

        // CALCULATE HASH for that transactions
        byte[] transactionsHash = Block.makeTransactionsHash(userAccount1.getPrivateKey(), transactions, null);

        //ADD UNCONFIRMED TRANSACTIONS TO BLOCK
        newBlock = BlockGenerator.generateNextBlock(dcSet, userAccount1,
                genesisBlock, orderedTransactions,
                2,  1000, 1000l, 1000l);
        newBlock.setTransactions(transactions);
        try {
            newBlock.process(dcSet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int height = newBlock.getHeight();
        //CHECK THAT NOT ALL TRANSACTIONS WERE ADDED TO BLOCK
        Tuple2<Integer, Integer> forgingData = userAccount1.getForgingData(dcSet, height);
        assertEquals((int) forgingData.a, 2);

        forgingData = recipient.getForgingData(dcSet, height);
        assertEquals((int) forgingData.a, 2);

    }

    @Test
    public void winValuesOnRun() {

        int target = 100000;
        int generatingBalance = target;
        int previousForgingHeight = 0;
        long winned_value = 0;
        int base = 0;
        int targetedWinValue = 0;
        for (int height = 2; height < 1000; height++) {

            if (height < BlockChain.REPEAT_WIN)
                previousForgingHeight = 1;
            else if (height < BlockChain.BASE_TARGET)
                previousForgingHeight = BlockChain.REPEAT_WIN + height >> 1;
            else if (height < BlockChain.BASE_TARGET << 3)
                previousForgingHeight = BlockChain.REPEAT_WIN + (BlockChain.BASE_TARGET >> 1) + (height >> 2);

            previousForgingHeight = height;
            winned_value = BlockChain.calcWinValue(dcSet, generator1, height, generator1.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue());
            base = BlockChain.getTargetedMin(height);
            targetedWinValue = BlockChain.calcWinValueTargetedBase(dcSet, height, winned_value, target);

            assertEquals(true, targetedWinValue > 0);


        }
    }
}
