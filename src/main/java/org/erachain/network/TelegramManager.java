package org.erachain.network;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.network.message.Message;
import org.erachain.network.message.TelegramMessage;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class TelegramManager extends Thread {
    /**
     * count telegrams
     */
    private static final int MAX_HANDLED_TELEGRAMS_SIZE = 1024 << (4 + Controller.HARD_WORK);

    /**
     * time to live telegram
     */
    private static final int KEEP_TIME = 60000 * 60 * (25 - 3 * Controller.HARD_WORK);
    static Logger LOGGER = LoggerFactory.getLogger(TelegramManager.class.getSimpleName());
    private Network network;
    private boolean run;
    // pool of messages
    private ConcurrentHashMap<String, TelegramMessage> handledTelegrams;
    // timestamp lists for clear
    private ConcurrentSkipListMap<Long, List<TelegramMessage>> telegramsForTime;
    // lists for RECIPIENTS only address
    private ConcurrentHashMap<String, List<TelegramMessage>> telegramsForAddress;
    // counts for CREATORs COUNT
    private ConcurrentHashMap<String, Integer> telegramsCounts;

    private static final int QUEUE_LENGTH = 512 << (Controller.HARD_WORK >> 1);
    BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<Message>(QUEUE_LENGTH);

    private Controller controller;
    private BlockChain blockChain;
    private DCSet dcSet;
    public long messageTimingAverage;

    public TelegramManager(Controller controller, BlockChain blockChain, DCSet dcSet, Network network) {

        this.controller = controller;
        this.blockChain = blockChain;
        this.dcSet = dcSet;

        this.network = network;
        this.handledTelegrams = new ConcurrentHashMap<String, TelegramMessage>();
        this.telegramsForTime = new ConcurrentSkipListMap<Long, List<TelegramMessage>>();
        this.telegramsForAddress = new ConcurrentHashMap<String, List<TelegramMessage>>();
        this.telegramsCounts = new ConcurrentHashMap<String, Integer>();

        this.setName("TelegramManager - " + this.getId());

        this.start();

    }

    /**
     * @param message
     */
    public boolean offerMessage(Message message) {

        boolean result = blockingQueue.offer(message);
        if (!result) {
            this.network.missedTelegrams.incrementAndGet();
        }
        return result;
    }

    private long onMessageProcessTiming;
    public void processMessage(Message message) {

        if (message == null)
            return;

        onMessageProcessTiming = System.nanoTime();

        if (add((TelegramMessage) message) != 0)
            return;

        onMessageProcessTiming = System.nanoTime() - onMessageProcessTiming;
        if (onMessageProcessTiming < 999999999999l) {
            // при переполнении может быть минус
            messageTimingAverage = ((messageTimingAverage << 5)
                    + onMessageProcessTiming - messageTimingAverage) >> 5;
        }

        // BROADCAST
        this.network.broadcast(message, false);

    }

    // GET telegram
    public TelegramMessage getTelegram(String signatureKey) {
        return handledTelegrams.get(signatureKey);
    }

    public Integer telegramCount() {
        return handledTelegrams.size();
    }

    public List<TelegramMessage> toList() {
        List<TelegramMessage> result = new ArrayList();
        result.addAll(handledTelegrams.values());
        return result;
    }


    // GET telegrams for RECIPIENT from TIME
    public List<TelegramMessage> getTelegramsFromTimestamp(long timestamp, String recipient, String filter, boolean outcomes) {
        List<TelegramMessage> telegrams = new ArrayList<TelegramMessage>();
        if (!controller.isOnStopping()) {

            boolean skip = false;
            SortedMap<Long, List<TelegramMessage>> subMap = telegramsForTime.tailMap(timestamp);
            for (Entry<Long, List<TelegramMessage>> item : subMap.entrySet()) {
                List<TelegramMessage> telegramsTimestamp = item.getValue();
                if (telegramsTimestamp != null) {
                    for (TelegramMessage telegram : telegramsTimestamp) {
                        Transaction transaction = telegram.getTransaction();
                        if (timestamp > 0 && telegram.getTransaction().getTimestamp() < timestamp)
                            continue;

                        if (filter != null && transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
                            String head = ((RSend) transaction).getTitle();
                            if (!filter.equals(head))
                                continue;
                        }

                        if (recipient != null) {
                            if (
                                // только входящие
                                    transaction.getType() == Transaction.SEND_ASSET_TRANSACTION
                                            && !((RSend) transaction).getRecipient().equals(recipient)
                                            // можно и исходящие
                                            && outcomes && !transaction.getCreator().equals(recipient)
                            ) {
                                continue;
                            }
                        }

                        telegrams.add(telegram);
                    }
                }
            }
        }

        // RETURN
        return telegrams;
    }

    // GET telegrams for RECIPIENT from TIME
    public List<TelegramMessage> getTelegramsForAddress(String recipient, long timestamp, String filter) {
        // TelegramMessage telegram;
        List<TelegramMessage> telegrams = new ArrayList<TelegramMessage>();
        // ASK DATABASE FOR A LIST OF PEERS
        if (!controller.isOnStopping()) {
            List<TelegramMessage> telegramsAddress = telegramsForAddress.get(recipient);
            if (telegramsAddress == null)
                return telegrams;
            for (TelegramMessage telegram : telegramsAddress) {
                Transaction transaction = telegram.getTransaction();
                if (timestamp > 0 && telegram.getTransaction().getTimestamp() < timestamp)
                    continue;

                if (filter != null && transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
                    String head = ((RSend) transaction).getTitle();
                    if (!filter.equals(head))
                        continue;
                }

                telegrams.add(telegram);
            }
        }

        // RETURN
        return telegrams;
    }

    /**
     * DELETE one telegram
     *
     * @param signatureStr
     * @param withTimestamp if TRUE delete from timestamp MAP too
     * @param withAddress   if TRUE delete from senders MAP too
     */
    public void delete(String signatureStr, boolean withTimestamp, boolean withAddress) {

        HashSet<Account> recipients;
        String address;
        TelegramMessage telegram;
        Transaction transaction;
        long timestamp;

        telegram = this.handledTelegrams.remove(signatureStr);
        if (telegram == null)
            return;

        transaction = telegram.getTransaction();
        byte[] signature = transaction.getSignature();

        if (withAddress) {
            recipients = transaction.getRecipientAccounts();
            if (recipients != null && !recipients.isEmpty()) {
                int i;
                for (Account recipient : recipients) {
                    address = recipient.getAddress();
                    List<TelegramMessage> addressTelegrams = this.telegramsForAddress.get(address);
                    if (addressTelegrams != null) {
                        i = 0;
                        for (TelegramMessage addressTelegram : addressTelegrams) {
                            if (Arrays.equals(addressTelegram.getTransaction().getSignature(), signature)) {
                                addressTelegrams.remove(i);
                                break;
                            }
                            i++;
                        }
                    }
                    // IF list is empty
                    if (addressTelegrams.isEmpty()) {
                        this.telegramsForAddress.remove(address);
                    } else {
                        this.telegramsForAddress.put(address, addressTelegrams);
                    }
                }
            }
        }

        // CREATOR counts
        address = transaction.getCreator().getAddress();
        Integer count = this.telegramsCounts.get(address);
        if (count != null) {
            if (count < 2) {
                this.telegramsCounts.remove(address);
            } else {
                this.telegramsCounts.put(address, count - 1);
            }
        }

        if (withTimestamp) {
            timestamp = transaction.getTimestamp();
            List<TelegramMessage> telegrams = this.telegramsForTime.get(timestamp);
            int i = 0;
            for (TelegramMessage telegram_item : telegrams) {
                if (Arrays.equals(telegram_item.getTransaction().getSignature(), signature)) {
                    telegrams.remove(i);
                    break;
                }
                i++;
            }

            if (telegrams.isEmpty())
                this.telegramsForTime.remove(timestamp);
            else
                this.telegramsForTime.put(timestamp, telegrams);
        }
    }

    //

    /**
     * delete all to this Timestamp and filter by Recipient address and Title
     *
     * @param toTimestamp timestamp (Long)
     * @param recipient   String
     * @param filter      String
     * @return - counter (long)
     */
    public long deleteToTimestamp(long toTimestamp, String recipient, String filter) {
        long counter = 0;

        SortedMap<Long, List<TelegramMessage>> subMap = telegramsForTime.headMap(toTimestamp);
        for (Long time_item : subMap.keySet()) {
            List<TelegramMessage> telegramsTimestamp = telegramsForTime.remove(time_item);
            if (telegramsTimestamp == null || telegramsTimestamp.isEmpty())
                continue;

            for (TelegramMessage telegram : telegramsTimestamp) {
                Transaction transaction = telegram.getTransaction();
                if (telegram.getTransaction().getTimestamp() > toTimestamp)
                    continue;

                if (filter != null && transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
                    String head = ((RSend) transaction).getTitle();
                    if (!filter.equals(head))
                        continue;
                }

                if (recipient != null && transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
                    Account account = ((RSend) transaction).getRecipient();
                    if (!account.equals(recipient))
                        continue;
                }

                counter++;
                delete(Base58.encode(transaction.getSignature()), false, true);
            }
        }

        return counter;
    }

    /**
     * delete all to this Recipient address and filter by Timestamp and Title
     *
     * @param recipient
     * @param toTimestamp if > 0 use filter to this value (delete all that less)
     * @param filter      if not null use it for filter
     * @return
     */
    public long deleteForRecipient(String recipient, long toTimestamp, String filter) {
        long counter = 0;

        List<TelegramMessage> telegrams = telegramsForAddress.remove(recipient);
        if (telegrams == null || telegrams.isEmpty())
            return 0;

        for (TelegramMessage telegram : telegrams) {
            Transaction transaction = telegram.getTransaction();
            if (toTimestamp > 0 && telegram.getTransaction().getTimestamp() > toTimestamp)
                continue;

            if (filter != null && transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
                String head = ((RSend) transaction).getTitle();
                if (!filter.equals(head))
                    continue;
            }

            counter++;
            delete(Base58.encode(transaction.getSignature()), true, false);
        }

        return counter;
    }

    /**
     * DELETE telegrams by signature list
     *
     * @param signatures
     * @return
     */
    public List<String> deleteList(List<String> signatures) {

        List<String> left = new ArrayList<>();
        HashSet<Account> recipients;
        String address;
        int i;
        TelegramMessage telegram;
        Transaction transaction;
        byte[] signature;
        long timestamp;
        for (String signatureStr : signatures) {
            telegram = this.handledTelegrams.remove(signatureStr);
            if (telegram == null) {
                left.add(signatureStr);
                continue;
            }

            transaction = telegram.getTransaction();
            signature = transaction.getSignature();
            recipients = transaction.getRecipientAccounts();
            if (recipients != null && !recipients.isEmpty()) {
                for (Account recipient : recipients) {
                    address = recipient.getAddress();
                    List<TelegramMessage> addressTelegrams = this.telegramsForAddress.get(address);
                    if (addressTelegrams != null) {
                        i = 0;
                        for (TelegramMessage addressTelegram : addressTelegrams) {
                            if (Arrays.equals(addressTelegram.getTransaction().getSignature(), signature)) {
                                addressTelegrams.remove(i);
                                break;
                            }
                            i++;
                        }
                    }
                    // IF list is empty
                    if (addressTelegrams.isEmpty()) {
                        this.telegramsForAddress.remove(address);
                    } else {
                        this.telegramsForAddress.put(address, addressTelegrams);
                    }
                }
            }

            // CREATOR counts
            address = transaction.getCreator().getAddress();
            Integer count = this.telegramsCounts.get(address);
            if (count != null) {
                if (count < 2) {
                    this.telegramsCounts.remove(address);
                } else {
                    this.telegramsCounts.put(address, count - 1);
                }
            }

            timestamp = transaction.getTimestamp();
            List<TelegramMessage> telegrams = this.telegramsForTime.get(timestamp);
            i = 0;
            for (TelegramMessage telegram_item : telegrams) {
                if (Arrays.equals(telegram_item.getTransaction().getSignature(), signature)) {
                    telegrams.remove(i);
                    break;
                }
                i++;
            }
            if (telegrams.isEmpty())
                this.telegramsForTime.remove(timestamp);
            else
                this.telegramsForTime.put(timestamp, telegrams);
        }

        return left;
    }

    /**
     * Do internal commands sent in transactions message as JSON.
     * ... "__DELETE": "list": ["BaseSignature",..], "toTime":TIMESTAMP, ...
     *
     * @param transactionCommand transaction from telegram
     * @return true if has LOAD and need to save local
     */
    protected boolean try_command(Transaction transactionCommand) {

        boolean hasLoad = true;

        if (transactionCommand.getType() == Transaction.SEND_ASSET_TRANSACTION) {
            RSend tx = (RSend) transactionCommand;
            if (tx.isEncrypted() || !tx.isText() || tx.getData() == null)
                return hasLoad;

            String message;
            try {
                message = new String(tx.getData(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return hasLoad;
            }

            if (message == null)
                return hasLoad;

            JSONObject jsonObject;
            try {
                jsonObject = (JSONObject) JSONValue.parse(message);
            } catch (Exception e) {
                return hasLoad;
            }

            if (jsonObject == null)
                return hasLoad;

            if (jsonObject.containsKey("__DELETE")) {

                // if THERE only ONE command DELETE - not broadcast if not delete
                JSONObject delete = (JSONObject) jsonObject.remove("__DELETE");

                PublicKeyAccount commander = transactionCommand.getCreator();
                if (delete.containsKey("list")) {

                    // TRY MAKE DELETION LIST
                    List<String> deleteList = new ArrayList<>();

                    for (Object sign58 : (JSONArray) delete.get("list")) {
                        TelegramMessage telegramToDelete = this.handledTelegrams.get((String) sign58);
                        if (telegramToDelete != null
                                && telegramToDelete.getTransaction().getCreator().equals(commander)) {
                            // IT FOUND and same CREATOR
                            deleteList.add((String) sign58);
                        }
                    }

                    // DELETE FOUNDED LIST
                    if (!deleteList.isEmpty()) {
                        deleteList(deleteList);
                    }

                }
                if (delete.containsKey("toTime")) {
                    long timestamp = (Long) delete.get("toTime");

                    // TRY MAKE DELETION LIST
                    List<String> deleteList = new ArrayList<>();

                    SortedMap<Long, List<TelegramMessage>> subMap = telegramsForTime.headMap(timestamp);
                    for (Entry<Long, List<TelegramMessage>> item : subMap.entrySet()) {
                        List<TelegramMessage> telegramsTimestamp = item.getValue();
                        if (telegramsTimestamp != null) {
                            for (TelegramMessage telegram : telegramsTimestamp) {
                                Transaction transaction = telegram.getTransaction();

                                if (commander.equals(transaction.getCreator())) {
                                    deleteList.add(transactionCommand.viewSignature());
                                }
                            }

                        }
                    }

                    // DELETE FOUNDED LIST
                    if (!deleteList.isEmpty()) {
                        deleteList(deleteList);
                    }
                }
            }

            hasLoad = !jsonObject.isEmpty();

        }

        return hasLoad;

    }

    /**
     * Заносит телеграмму в память Ноды
     *
     * @param telegram  телеграмма котрую надо добавить
     * @return TRUE if not added
     */
    public int add(TelegramMessage telegram) {

        Transaction transaction;

        transaction = telegram.getTransaction();

        // CHECK IF SIGNATURE IS VALID OR GENESIS TRANSACTION
        Account creator = transaction.getCreator();
        if (creator == null) {
            return Transaction.INVALID_CREATOR;
        }

        if(!transaction.isSignatureValid(DCSet.getInstance())) {
            return Transaction.INVALID_SIGNATURE;
        }

        long timestamp = transaction.getTimestamp();
        if (timestamp > NTP.getTime() + 120000) {
            return Transaction.INVALID_TIMESTAMP;
        } else if (200000 + timestamp < NTP.getTime()) {
            return Transaction.INVALID_TIMESTAMP;
        }

        // TRY DO COMMANDS
        if (!try_command(transaction)) {
            // go broadcast
            return 0;
        }

        String signatureKey;

        // signatureKey =
        // java.util.Base64.getEncoder().encodeToString(transaction.getSignature());
        signatureKey = Base58.encode(transaction.getSignature());

        if (this.handledTelegrams.containsKey(signatureKey))
            return Transaction.ITEM_DUPLICATE;

        // CHECK IF LIST IS FULL
        if (this.handledTelegrams.size() > MAX_HANDLED_TELEGRAMS_SIZE) {
            List<TelegramMessage> telegrams = this.telegramsForTime.remove(this.telegramsForTime.firstKey());
            remove(telegrams, 0);
        }

        this.handledTelegrams.put(signatureKey, telegram);

        if (Controller.getInstance().doesWalletKeysExists()
                && Settings.getInstance().getTelegramStoreUse() && Settings.getInstance().getTelegramStorePeriod() > 0) {
            // IF MY STORE is USED

            // save telegram to wallet DB
            if (controller.getWallet().accountExists(transaction.getCreator())) {
                // add as my OUTCOME
                controller.getWallet().dwSet.getTelegramsMap().add(signatureKey, transaction);
            } else {
                // TRY ADD as my INCOME
                controller.addTelegramToWallet(transaction, signatureKey);
            }

        }

        // MAP timestamps
        List<TelegramMessage> timestampTelegrams = this.telegramsForTime.get(timestamp);
        if (timestampTelegrams == null) {
            timestampTelegrams = new ArrayList<TelegramMessage>();
        }

        timestampTelegrams.add(telegram);
        this.telegramsForTime.put((Long) timestamp, timestampTelegrams);

        HashSet<Account> recipients;
        String address;

        // MAP addresses
        recipients = transaction.getRecipientAccounts();
        if (recipients != null) {
            for (Account recipient : recipients) {
                address = recipient.getAddress();
                List<TelegramMessage> addressTelegrams = this.telegramsForAddress.get(address);
                if (addressTelegrams == null) {
                    addressTelegrams = new ArrayList<TelegramMessage>();
                }
                addressTelegrams.add(telegram);
                this.telegramsForAddress.put(address, addressTelegrams);
            }
        }

        address = transaction.getCreator().getAddress();
        if (this.telegramsCounts.containsKey(address)) {
            this.telegramsCounts.put(address, this.telegramsCounts.get(address) + 1);
        } else {
            this.telegramsCounts.put(address, 1);
        }


        return 0;

    }

    public boolean remove(List<TelegramMessage> firstItem,
                          long timeKey) {

        Transaction transaction;
        TelegramMessage telegram;

        HashSet<Account> recipients;
        String address;
        int i;

        List<TelegramMessage> telegrams = firstItem;
        // for all signatures on this TIME
        for (TelegramMessage telegram_item : telegrams) {
            telegram = this.handledTelegrams.remove(Base58.encode(telegram_item.getTransaction().getSignature()));
            if (telegram != null) {
                transaction = telegram.getTransaction();
                byte[] signature = transaction.getSignature();
                recipients = transaction.getRecipientAccounts();
                if (recipients != null) {
                    for (Account recipient : recipients) {
                        address = recipient.getAddress();
                        List<TelegramMessage> addressTelegrams = this.telegramsForAddress.get(address);
                        if (addressTelegrams != null) {
                            i = 0;
                            for (TelegramMessage addressTelegram : addressTelegrams) {
                                if (Arrays.equals(addressTelegram.getTransaction().getSignature(), signature)) {
                                    addressTelegrams.remove(i);
                                    break;
                                }
                                i++;
                            }
                        }
                        // IF list is empty
                        if (addressTelegrams.isEmpty()) {
                            this.telegramsForAddress.remove(address);
                        } else {
                            this.telegramsForAddress.put(address, addressTelegrams);
                        }
                    }
                }

                // CREATOR counts
                address = transaction.getCreator().getAddress();
                Integer count = this.telegramsCounts.get(address);
                if (count != null) {
                    if (count < 2) {
                        this.telegramsCounts.remove(address);
                    } else {
                        this.telegramsCounts.put(address, count - 1);
                    }
                }
            }
        }

        // by TIME
        if (timeKey > 0)
            this.telegramsForTime.remove(timeKey);

        return false;

    }

    /**
     * endless cycle remove telegrams by time
     */

    public void run() {
        this.run = true;

        long timeWaiter = 0;
        long timestamp;
        while (this.run) {
            try {
                try {
                    processMessage(blockingQueue.poll(1000, TimeUnit.MILLISECONDS));
                } catch (java.lang.OutOfMemoryError e) {
                    LOGGER.error(e.getMessage(), e);
                    controller.stopAndExit(281);
                    break;
                } catch (java.lang.IllegalMonitorStateException e) {
                    break;
                } catch (java.lang.InterruptedException e) {
                    break;
                }

                timestamp = NTP.getTime();
                if (timestamp - timeWaiter > 1000) {

                    do {

                        timeWaiter = timestamp;

                        Entry<Long, List<TelegramMessage>> firstItem = this.telegramsForTime.firstEntry();
                        if (firstItem == null)
                            break;

                        long timeKey = firstItem.getKey();

                        if (timeKey + KEEP_TIME < timestamp) {
                            remove(firstItem.getValue(), timeKey);
                        } else {
                            break;
                        }
                    } while (true);
                }
            } catch (java.lang.OutOfMemoryError e) {
                LOGGER.error(e.getMessage(), e);
                controller.stopAndExit(282);
                break;
            }

        }

        LOGGER.info("Telegram Manager halted");

    }

    public void halt() {
        this.run = false;
    }

}
