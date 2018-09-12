package network;

import api.ApiErrorFactory;
import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.transaction.R_Send;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.transaction.OnDealClick;
import network.message.Message;
import network.message.MessageFactory;
import network.message.TelegramMessage;
import ntp.NTP;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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

        dcSet = DCSet.createEmptyDatabaseSet();
        telegramer = new TelegramManager(null);
        telegramer.start();
    }

    @Test
    public void delete() {
    }

    @Test
    public void deleteList() {

        init();

        Transaction transaction;

        Controller cntr = Controller.getInstance();
        for (int i=0; i < 100; ++i) {
            // CREATE TX MESSAGE

            //transaction = cntr.r_Send(
            //        sender, FEE_POWER, recipient1, 0l, amount,
            //        title + i, isText, data, encrypted);
            transaction = new R_Send(sender, FEE_POWER, recipient1, 0l, amount, title + i, message, isText, encrypted, timestamp, 0l);
            transaction.sign(sender, Transaction.FOR_NETWORK);


            // CREATE MESSAGE
            telegram = (TelegramMessage)MessageFactory.getInstance().createTelegramMessage(transaction);

            telegramer.pipeAddRemove(telegram, null, 0);

        }

        telegrams = telegramer.getTelegramsFromTimestamp(0l, null, null);
        assertEquals(telegrams.size(), 100);

        transaction = telegrams.get(10).getTransaction();
        String signanureStr = Base58.encode(transaction.getSignature());

        ///////////// DELETE ONE
        telegramer.delete(signanureStr, true, true);

        assertEquals((int)telegramer.telegramCount(), 100 - 1);

        telegrams = telegramer.getTelegramsFromTimestamp(0l, null, null);
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

        telegrams = telegramer.getTelegramsFromTimestamp(0l, null, null);
        assertEquals(telegrams.size(), 100 - 1 - signsList.size());

        telegrams = telegramer.getTelegramsForAddress(recipient1.getAddress(), 0, null);
        assertEquals(telegrams.size(), 100 - 1 - signsList.size());

    }

    @Test
    public void pipeAddRemove() {
    }
}