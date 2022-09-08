package org.erachain;
// 30/03

import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.blockexplorer.BlockExplorer;
import org.erachain.core.blockexplorer.BlockExplorer.Stopwatch;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.settings.Settings;
import org.erachain.utils.Pair;
import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.assertEquals;

@Ignore
public class BlockExplorerTest {


    static Logger LOGGER = LoggerFactory.getLogger(BlockExplorerTest.class.getName());

    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value

    public static DCSet createRealEmptyDatabaseSet() {
        //OPEN DB
        File dbFile = new File(Settings.getInstance().getDataChainPath(), "data2.dat");
        dbFile.getParentFile().mkdirs();

        //CREATE DATABASE
        DB database = DBMaker.newFileDB(dbFile)
                .closeOnJvmShutdown()
                .cacheSize(2048)
                .checksumEnable()
                .mmapFileEnableIfSupported()
                .make();

        //CREATE INSTANCE
        return new DCSet(dbFile, database, false, false, false, 0);
    }

    public void maxBalance() {
        byte[] amountBytes = new byte[]{127, 127, 127, 127, 127, 127, 127, 127};
        BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
        LOGGER.error(amount.toPlainString());
        amountBytes = new byte[]{-128, -128, -128, -128, -128, -128, -128, -128};
        amount = new BigDecimal(new BigInteger(amountBytes), BlockChain.AMOUNT_DEDAULT_SCALE);
        LOGGER.error(amount.toPlainString());
    }

    public void minBalance() {

        Block block = new GenesisBlock();

        DCSet databaseSet = createRealEmptyDatabaseSet();

        List<Pair<Block, BigDecimal>> balancesBlocks = new ArrayList<>();

        Stopwatch stopwatchAll = new Stopwatch();

        //ADD ERM ASSET
        AssetVenture ermAsset = new AssetVenture(null, block.getCreator(), "ERM", icon, image, ".",
                0, 8, 100000000l);
        databaseSet.getItemAssetMap().set(0l, ermAsset);

        do {

            try {
                block.process(databaseSet, false);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (block.getHeight() % 2000 == 0) {
                LOGGER.error(" height: " + block.getHeight());
            }

            balancesBlocks.add(new Pair<>(block, block.getCreator().getBalance(databaseSet, Transaction.FEE_KEY).a.b));

            block = block.getChild(DCSet.getInstance());

        } while (block != null);

        LOGGER.error(stopwatchAll.elapsedTime() / 1000 + " secs");

        Collections.sort(balancesBlocks, new BalancesBlocksComparator());

        for (int i = 0; i < 400; i++) {
            System.out.print(
                    Base58.encode(balancesBlocks.get(i).getA().getSignature())
            );
            System.out.print(" " +
                    balancesBlocks.get(i).getA().getCreator().getAddress());
            LOGGER.error(" " + balancesBlocks.get(i).getB().toPlainString());
        }


    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void blockExplorer() throws Exception {

        DCSet.getInstance();

        ArrayList<String> addrs = new ArrayList();
        addrs.add("QXncuwPehVZ21ymE1jawXg1Uv3sZZ4TvYk");
        addrs.add("QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ");
        addrs.add("QYsLsfwMRBPnunmuWmFkM4hvGsfooY8ssU");
        addrs.add("QQPsGx3khgEboJXWPiDBMVDG5ngu9wDo3k");
        addrs.add("Qd9jQKZSXoYgFypTQySJUSbXcZvjgdiemn");
        addrs.add("QfyocFSGghfpANqUmQFpoG2sk5TVg8LvEm");
        addrs.add("QMu6HXfZCnwaNmyFjjhWTYAUW7k1x7PoVr");
        addrs.add("QdrhixdevE7ZJqSHAfV19yVYrYsys8VLgz");
        addrs.add("QPVknSmwDryB98Hh8xB7E6U75dGFYwNkJ4");

        DCSet.getInstance();

        for (int i = 0; i < addrs.size(); i++) {

            String addr = addrs.get(i);
            List<String> listaddr = new ArrayList<>();
            listaddr.add(addr);

            Map<Object, Map> output = BlockExplorer.getInstance().jsonQueryAddress(listaddr.get(0), 1, null);

            Map<Long, String> totalBalance = (Map<Long, String>) output.get("balance").get("total");

            Account account = new Account(addr);

            LOGGER.error(addr);
            for (Map.Entry<Long, String> e : totalBalance.entrySet()) {
                Long key = e.getKey();

                BigDecimal blockExplorerBalance = new BigDecimal(e.getValue());

                System.out.print("(" + key + ") " + " BlockExplorerBalance: " + blockExplorerBalance);

                BigDecimal nativeBalance = account.getBalanceUSE(key);

                System.out.print("; NantiveBalance: " + nativeBalance);

                if (blockExplorerBalance.equals(nativeBalance)) {
                    LOGGER.error(" OK.");
                } else {
                    LOGGER.error(" Fail!!!");
                }

                assertEquals(blockExplorerBalance, nativeBalance);
            }
        }

        DCSet.getInstance().close();
    }

    public void getTransactionsByAddress() {

        DCSet.getInstance().getTransactionFinalMap().contains(Transaction.makeDBRef(1, 1));

        int type = 31;
        Boolean isCreator = null;
        Long fromID = null;

        Stopwatch stopwatchAll = new Stopwatch();

        List<Object> all = new ArrayList<Object>();

        all.addAll(DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressLimit(
                Crypto.getInstance().getShortBytesFromAddress("QPVknSmwDryB98Hh8xB7E6U75dGFYwNkJ4"), type, isCreator, fromID, 0, 555, true, false));

        LOGGER.error("getTransactionsByAddress QPVknSmwDryB98Hh8xB7E6U75dGFYwNkJ4. " + all.size() + " " + stopwatchAll.elapsedTime());

        all.clear();
        stopwatchAll = new Stopwatch();

        all.addAll(DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressLimit(
                Crypto.getInstance().getShortBytesFromAddress("QYsLsfwMRBPnunmuWmFkM4hvGsfooY8ssU"), type, isCreator, fromID, 0, 555, true, false));

        LOGGER.error("getTransactionsByAddress QYsLsfwMRBPnunmuWmFkM4hvGsfooY8ssU. " + all.size() + " " + stopwatchAll.elapsedTime());

        all.clear();

    }


    public void getTransactionsByTypeAndAddress() {

        DCSet.getInstance().getTransactionFinalMap().contains(Transaction.makeDBRef(1, 1));

        Boolean isCreator = null;
        Long fromID = null;

        Stopwatch stopwatchAll = new Stopwatch();

        List<Object> all = new ArrayList<Object>();

        List<Transaction> transactions = new ArrayList<Transaction>();
        for (int type = 1; type <= 23; type++) {  // 17 - The number of transaction types. 23 - for the future
            transactions.addAll(DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressAndType("QPVknSmwDryB98Hh8xB7E6U75dGFYwNkJ4", type, 0, 0));
        }

        Map<String, Boolean> signatures = new LinkedHashMap<String, Boolean>();

        for (Transaction transaction : transactions) {
            byte[] signature = transaction.getSignature();
            if (!signatures.containsKey(new String(signature))) {
                signatures.put(new String(signature), true);
                all.add(transaction);
            }
        }

        LOGGER.error("getTransactionsByTypeAndAddress QPVknSmwDryB98Hh8xB7E6U75dGFYwNkJ4. " + all.size() + " " + stopwatchAll.elapsedTime());

        all.clear();
        stopwatchAll = new Stopwatch();

        transactions = new ArrayList<Transaction>();
        for (int type = 1; type <= 23; type++) {  // 17 - The number of transaction types. 23 - for the future
            transactions.addAll(DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressAndType("QYsLsfwMRBPnunmuWmFkM4hvGsfooY8ssU", type, 0, 0));
        }

        signatures = new LinkedHashMap<String, Boolean>();

        for (Transaction transaction : transactions) {
            byte[] signature = transaction.getSignature();
            if (!signatures.containsKey(new String(signature))) {
                signatures.put(new String(signature), true);
                all.add(transaction);
            }
        }

        LOGGER.error("getTransactionsByTypeAndAddress QYsLsfwMRBPnunmuWmFkM4hvGsfooY8ssU. " + all.size() + " " + stopwatchAll.elapsedTime());

        all.clear();


        stopwatchAll = new Stopwatch();

        transactions = new ArrayList<Transaction>();
        for (int type = 1; type <= 23; type++) {  // 17 - The number of transaction types. 23 - for the future
            transactions.addAll(DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressAndType("QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ", type, 0, 0));
        }

        for (Transaction transaction : transactions) {
            LOGGER.error(Base58.encode(transaction.getSignature()));
        }

        LOGGER.error("getTransactionsByTypeAndAddress QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ. " + transactions.size() + " " + stopwatchAll.elapsedTime());

        all.clear();

        stopwatchAll = new Stopwatch();

        all.addAll(DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressLimit(
                Crypto.getInstance().getShortBytesFromAddress("QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ"), 31, isCreator, fromID, 0, 555, true, false));

        LOGGER.error("getTransactionsByAddress QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ. " + all.size() + " " + stopwatchAll.elapsedTime());

        stopwatchAll = new Stopwatch();
        all.clear();
        all.addAll(DCSet.getInstance().getTransactionFinalMap().getTransactionsByCreator("QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ", 0, 0));

        for (Object transaction : all) {
            LOGGER.error(Base58.encode(((Transaction) transaction).getSignature()));
        }

        LOGGER.error("getTransactionsByAddress QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ. " + all.size() + " " + stopwatchAll.elapsedTime());

        all.clear();

        all.addAll(DCSet.getInstance().getTransactionFinalMap().getTransactionsByRecipient("QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ"));

        for (Object transaction : all) {
            LOGGER.error(Base58.encode(((Transaction) transaction).getSignature()));
        }

        LOGGER.error("getTransactionsByAddress QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ. " + all.size() + " " + stopwatchAll.elapsedTime());

    }

    public void txItSelf() throws Exception {

        Transaction transaction = getTransaction(Base58.decode("4JXPXqdP7GT743AoX2m8vHBeWNrKvBcf71TcDLfLeMn6rmV5uyVRDcV5gLspNquZyatY4tHB9RXDWKahEM85oTJv"));
        Account account = new Account("QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ");
        //logger.error(transaction.getAmount(account));

        transaction = getTransaction(Base58.decode("4JXPXqdP7GT743AoX2m8vHBeWNrKvBcf71TcDLfLeMn6rmV5uyVRDcV5gLspNquZyatY4tHB9RXDWKahEM85oTJv"));
        account = new Account("QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ");
        //logger.error(transaction.getAmount(account));
    }

    public Transaction getTransaction(byte[] signature) {

        return getTransaction(signature, DCSet.getInstance());
    }

    public Transaction getTransaction(byte[] signature, DCSet database) {

        // CHECK IF IN BLOCK
        Long tuple_Tx = database.getTransactionFinalMapSigns().get(signature);
        if (tuple_Tx != null) {
            return database.getTransactionFinalMap().get(tuple_Tx);

        }


        // CHECK IF IN TRANSACTION DATABASE
        return DCSet.getInstance().getTransactionTab().get(signature);
    }

    public class BalancesBlocksComparator implements Comparator<Pair<Block, BigDecimal>> {

        @Override
        public int compare(Pair<Block, BigDecimal> one, Pair<Block, BigDecimal> two) {
            return one.getB().compareTo(two.getB());
        }
    }
}
