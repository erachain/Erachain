package org.erachain.network;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.erachain.network.message.TelegramMessage;
import org.erachain.ntp.NTP;
import org.erachain.smartcontracts.SmartContract;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class TelegramManagerTest {

    //Long Transaction.FOR_NETWORK = null;
    long assetKeyTest = 1011;
    long ERA_KEY = AssetCls.ERA_KEY;
    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 0;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();

    TelegramMessage telegram;
    List<TelegramMessage> telegrams;
    TelegramManager telegramer;

    DCSet dcSet;
    ExLink exLink = null;
    SmartContract smartContract = null;


    long flags = 0l;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
    Account recipient1 = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
    Account recipient2 = new Account("7Hjfz1Dce5GQxm47JhpfRm6H8fzgBcwtFf");
    Account recipient3 = new Account("7MJRKdJJSnAuza9QjKUSsPoG6kSDmgbGzF");
    BigDecimal amount = BigDecimal.valueOf(10).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
    String title = "headdd";
    byte[] message = "test123!".getBytes();
    byte[] isText = new byte[]{1};
    byte[] encrypted = new byte[]{0};
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;

    // INIT ASSETS
    private void init() {

        dcSet = DCSet.createEmptyDatabaseSet(0);
        telegramer = new TelegramManager(null, null, null, null);
        telegramer.start();
    }

    @Test
    public void delete() {
    }

    @Test
    public void deleteList() {

        init();

        boolean outcomes = false;
        Transaction transaction;

        Controller cntr = Controller.getInstance();
        for (int i=0; i < 100; ++i) {
            // CREATE TX MESSAGE

            //transaction = cntr.r_Send(
            //        sender, FEE_POWER, recipient1, 0l, amount,
            //        title + i, isText, data, encrypted);
            transaction = new RSend(sender, exLink, smartContract, FEE_POWER, recipient1, 0l, amount, title + i, message, isText, encrypted, timestamp, 0l);
            transaction.sign(sender, Transaction.FOR_NETWORK);


            // CREATE MESSAGE
            telegram = (TelegramMessage)MessageFactory.getInstance().createTelegramMessage(transaction);

            telegramer.add(telegram);

        }

        telegrams = telegramer.getTelegramsFromTimestamp(0l, null, null, outcomes);
        assertEquals(telegrams.size(), 100);

        transaction = telegrams.get(10).getTransaction();
        String signanureStr = Base58.encode(transaction.getSignature());

        ///////////// DELETE ONE
        telegramer.delete(signanureStr, true, true);

        assertEquals((int)telegramer.telegramCount(), 100 - 1);

        telegrams = telegramer.getTelegramsFromTimestamp(0l, null, null, outcomes);
        assertEquals(telegrams.size(), 100 - 1);

        telegrams = telegramer.getTelegramsForAddress(recipient1.getAddress(), 0, null);
        assertEquals(telegrams.size(), 100 - 1);

        List<String> signsList = new ArrayList<String>();
        transaction = telegrams.get(13).getTransaction();
        signsList.add(Base58.encode(transaction.getSignature()));
        transaction = telegrams.get(25).getTransaction();
        signsList.add(Base58.encode(transaction.getSignature()));
        transaction = telegrams.get(77).getTransaction();
        signsList.add(Base58.encode(transaction.getSignature()));

        ///////////// DELETE LIST
        telegramer.deleteList(signsList);

        assertEquals((int)telegramer.telegramCount(), 100 - 1 - signsList.size());

        telegrams = telegramer.getTelegramsFromTimestamp(0l, null, null, outcomes);
        assertEquals(telegrams.size(), 100 - 1 - signsList.size());

        telegrams = telegramer.getTelegramsForAddress(recipient1.getAddress(), 0, null);
        assertEquals(telegrams.size(), 100 - 1 - signsList.size());

    }

    @Test
    public void generateRandomTelegram() {

        init();

        ArrayList<PrivateKeyAccount> arrayListCreator = new ArrayList();
        ArrayList<Account> arrayListRecipient = new ArrayList();

        Random random = new Random();
        byte[] nonceBytes;
        byte[] addressSeed;

        // MAKE CREATORS
        int creatorCount = 1000;
        for (int i = 0; i < creatorCount; i++) {

            nonceBytes = Ints.toByteArray(Integer.valueOf(i));
            addressSeed = Crypto.getInstance().doubleDigest(Bytes.concat(nonceBytes, seed, nonceBytes));
            byte[] privateKey = Crypto.getInstance().createKeyPair(addressSeed).getA();
            PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
            arrayListCreator.add(sender);

        }

        // MAKE RECIPIENTS
        int recipientCount = 10000;
        for (int i = 0; i < recipientCount; i++) {

            nonceBytes = Ints.toByteArray(Integer.valueOf(-i));
            addressSeed = Crypto.getInstance().doubleDigest(Bytes.concat(nonceBytes, seed, nonceBytes));
            byte[] publicKey = Crypto.getInstance().createKeyPair(addressSeed).getB();
            Account recipient = new Account(new PublicKeyAccount(publicKey).getAddress());
            arrayListRecipient.add(recipient);

        }

        long date = System.currentTimeMillis();

        // MAKE TELEGRAMS
        for (int i = 0; i < 100000; i++) {

            PrivateKeyAccount creator = arrayListCreator.get(random.nextInt(creatorCount));
            Account recipient = arrayListRecipient.get(random.nextInt(recipientCount));
            int user = random.nextInt(33465666);
            //int expire = random.nextInt(1243555959);
            int randomPrice = random.nextInt(10000);

            String phone = String.valueOf(random.nextInt(999 - 100) + 100) +
                    String.valueOf(random.nextInt(999 - 100) + 100) +
                    String.valueOf(random.nextInt(9999 - 1000) + 1000);

            String message = user + ":" + randomPrice;

            Transaction transaction = new RSend(creator, exLink, smartContract, (byte) 0, recipient, 0, amount, phone,
                    message.getBytes(), new byte[1], new byte[1],
                    System.currentTimeMillis(), 0l);
            transaction.sign(creator, Transaction.FOR_NETWORK);

            Message telegram = MessageFactory.getInstance().createTelegramMessage(transaction);

            telegramer.add((TelegramMessage) telegram);
        }

        long date2 = System.currentTimeMillis();
        long diff = date2 - date;

        // CHECK network.TelegramManager.MAX_HANDLED_TELEGRAMS_SIZE
        int listCounter = telegramer.telegramCount();
        System.out.println("Making " + listCounter + " telegrams: " + diff);

        long i = 0;
        List<TelegramMessage> list = telegramer.toList();

        int testInx = 1000;
        // TEST SPEED
        date = System.currentTimeMillis();
        for (long k = 0; k < testInx; k++) {
            for (TelegramMessage item : list) {
                if (Arrays.equals(item.getTransaction().getSignature(),
                        list.get(random.nextInt(listCounter)).getTransaction().getSignature()))
                    i++;
            }
        }

        date2 = System.currentTimeMillis();
        diff = date2 - date;
        System.out.println("time for seek of " + testInx + "times telegrams in List: " + (diff / 2) + " ms");

        ///
        // TEST SPEED
        date = System.currentTimeMillis();
        for (long k = 0; k < testInx; k++) {
            if (telegramer.getTelegram(Base58.encode(
                    list.get(random.nextInt(listCounter)).getTransaction().getSignature())) != null)
                i++;
        }

        date2 = System.currentTimeMillis();
        diff = date2 - date;
        System.out.println("time for seek of " + testInx + "times telegrams in HashMap: " + diff + " ms");

    }

    @Test
    public void pipeAddRemove() {
    }

    @Test
    public void try_command() {

        init();

        Transaction transaction;

        Controller cntr = Controller.getInstance();

        String message = "{\"info\":\"sldkf jslkfd jsldfk\"}";

        transaction = new RSend(sender, exLink, smartContract, (byte) 0, recipient1, 0, amount, "---",
                message.getBytes(), new byte[1], new byte[1],
                System.currentTimeMillis(), 0l);
        transaction.sign(sender, Transaction.FOR_NETWORK);

        Message telegram = MessageFactory.getInstance().createTelegramMessage(transaction);

        telegramer.add((TelegramMessage) telegram);

        assertEquals((int) telegramer.telegramCount(), 1);

        String signature = transaction.viewSignature();
        message = "{\"info\":\"sldkf jslkfd jsldfk\",\"__DELETE\":{\"list\":[\""
                + transaction.viewSignature() + "\"]}}";

        transaction = new RSend(sender, exLink, smartContract, (byte) 0, recipient1, 0, amount, "---",
                message.getBytes(), new byte[]{1}, new byte[1],
                System.currentTimeMillis(), 0l);
        transaction.sign(sender, Transaction.FOR_NETWORK);

        telegram = MessageFactory.getInstance().createTelegramMessage(transaction);

        telegramer.add((TelegramMessage) telegram);

        assertEquals((int) telegramer.telegramCount(), 1);

    }
}