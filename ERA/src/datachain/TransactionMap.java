package datachain;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedBytes;
import controller.Controller;
import core.account.Account;
import core.transaction.Transaction;
import database.DBMap;
import database.serializer.TransactionSerializer;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple2Comparator;
import utils.ObserverMessage;
import utils.ReverseComparator;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Храним неподтвержденные транзакции - memory pool for unconfirmed transaction.
 * Signature (as Long) -> Transaction
 * <hr>
 * Здесь вторичные индексы создаются по несколько для одной записи путем создания массива ключей,
 * см. typeKey и recipientKey. Они используются для API RPC block explorer.
 * Нужно огрничивать размер выдаваемого списка чтобы не перегружать ноду.
 * <br>
 * Так же вторичный индекс по времени, который используется в ГУИ TIMESTAMP_INDEX = 0 (default)
 * - он оргнизыется внутри DCMap в списке индексов для сортировок в ГУИ
 *
 * Также хранит инфо каким пирам мы уже разослали транзакцию неподтвержденную так что бы при подключении делать автоматически broadcast
 *
 *  <hr>
 *  (!!!) для создания уникальных ключей НЕ нужно добавлять + val.viewTimestamp(), и так работант, а почему в Ордерах не работало?
 *  <br>в БИНДЕ внутри уникальные ключи создаются добавлением основного ключа
 */
public class TransactionMap extends DCMap<Long, Transaction> implements Observer {
    public static final int TIMESTAMP_INDEX = 0;
    public static final int MAX_MAP_SIZE = core.BlockChain.HARD_WORK ? 100000 : 5000;

    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    // PEERS for transaction signature
    private Map<Long, List<byte[]>> peersBroadcasted = new HashMap<Long, List<byte[]>>();

    @SuppressWarnings("rawtypes")
    private NavigableSet senderKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet recipientKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet typeKey;

    public TransactionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            if (databaseSet.isDynamicGUI()) {
                this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_UNC_TRANSACTION_TYPE);
                this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_UNC_TRANSACTION_TYPE);
                this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE);
                this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_UNC_TRANSACTION_TYPE);
            } else {
                this.observableData.put(DBMap.NOTIFY_COUNT, ObserverMessage.COUNT_UNC_TRANSACTION_TYPE);
            }
        }

    }

    public TransactionMap(TransactionMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes(DB database) {
        // TIMESTAMP INDEX
        Tuple2Comparator<Long, Long> comparator = new Fun.Tuple2Comparator<Long, Long>(Fun.COMPARATOR,
                //UnsignedBytes.lexicographicalComparator()
                Fun.COMPARATOR);
        NavigableSet<Tuple2<Long, Long>> heightIndex = database
                .createTreeSet("transactions_index_timestamp")
                .comparator(comparator)
                .makeOrGet();

        NavigableSet<Tuple2<Long, Long>> descendingHeightIndex = database
                .createTreeSet("transactions_index_timestamp_descending")
                .comparator(new ReverseComparator(comparator))
                .makeOrGet();

        createIndex(TIMESTAMP_INDEX, heightIndex, descendingHeightIndex,
                new Fun.Function2<Long, Long, Transaction>() {
                    @Override
                    public Long run(Long key, Transaction value) {
                        return value.getTimestamp();
                    }
                });

    }

    public Integer deleteObservableData(int index) {
        return this.observableData.remove(index);
    }

    public Integer setObservableData(int index, Integer data) {
        return this.observableData.put(index, data);
    }

    @Override
    protected Map<Long, Transaction> getMap(DB database) {

        // OPEN MAP
        BTreeMap<Long, Transaction> map = database.createTreeMap("transactions")
                .keySerializer(BTreeKeySerializer.BASIC)
                //.comparator(UnsignedBytes.lexicographicalComparator())
                .valueSerializer(new TransactionSerializer())
                .counterEnable().makeOrGet();

        this.senderKey = database.createTreeSet("sender_unc_txs").comparator(Fun.COMPARATOR).makeOrGet();
        Bind.secondaryKey(map, this.senderKey, new Fun.Function2<Tuple2<String, Long>, Long, Transaction>() {
            @Override
            public Tuple2<String, Long> run(Long key, Transaction val) {
                Account account = val.getCreator();
                return new Tuple2<String, Long>(account == null ? "genesis" : account.getAddress(), val.getTimestamp());
            }
        });

        this.recipientKey = database.createTreeSet("recipient_unc_txs").comparator(Fun.COMPARATOR).makeOrGet();
        Bind.secondaryKeys(map, this.recipientKey,
                new Fun.Function2<String[], Long, Transaction>() {
                    @Override
                    public String[] run(Long key, Transaction val) {
                        List<String> recps = new ArrayList<String>();

                        val.setDC(getDCSet());

                        for (Account acc : val.getRecipientAccounts()) {
                            // recps.add(acc.getAddress() + val.viewTimestamp()); уникальнось внутри Бинда делается
                            recps.add(acc.getAddress());
                        }
                        String[] ret = new String[recps.size()];
                        ret = recps.toArray(ret);
                        return ret;
                    }
                });

        this.typeKey = database.createTreeSet("address_type_unc_txs").comparator(Fun.COMPARATOR).makeOrGet();
        Bind.secondaryKeys(map, this.typeKey,
                new Fun.Function2<Fun.Tuple3<String, Long, Integer>[], Long, Transaction>() {
                    @Override
                    public Fun.Tuple3<String, Long, Integer>[] run(Long key, Transaction val) {
                        List<Fun.Tuple3<String, Long, Integer>> recps = new ArrayList<Fun.Tuple3<String, Long, Integer>>();
                        Integer type = val.getType();

                        val.setDC(getDCSet());

                        for (Account acc : val.getInvolvedAccounts()) {
                            recps.add(new Fun.Tuple3<String, Long, Integer>(acc.getAddress(), val.getTimestamp(), type));

                        }
                        // Tuple2<Integer, String>[] ret = (Tuple2<Integer,
                        // String>[]) new Object[ recps.size() ];
                        Fun.Tuple3<String, Long, Integer>[] ret = (Fun.Tuple3<String, Long, Integer>[])
                                Array.newInstance(Fun.Tuple3.class, recps.size());

                        ret = recps.toArray(ret);

                        return ret;
                    }
                });

        return map;
    }

    @Override
    protected Map<Long, Transaction> getMemoryMap() {
        return new TreeMap<Long, Transaction>(
                //UnsignedBytes.lexicographicalComparator()
                );
    }

    @Override
    protected Transaction getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public List<Transaction> getSubSet(long timestamp, boolean notSetDCSet) {

        List<Transaction> values = new ArrayList<Transaction>();
        Iterator<Long> iterator = this.getIterator(TIMESTAMP_INDEX, false);
        Transaction transaction;
        int count = 0;
        int bytesTotal = 0;
        Long key;
        while (iterator.hasNext()) {
            key = iterator.next();
            transaction = this.map.get(key);
            if (transaction.getTimestamp() > timestamp)
                continue;
            if (transaction.getDeadline() < timestamp) {
                // мы используем отсортированный индекс
                break;
            }

            bytesTotal += transaction.getDataLength(Transaction.FOR_NETWORK, true);
            if (bytesTotal > core.BlockGenerator.MAX_BLOCK_SIZE_BYTE + (core.BlockGenerator.MAX_BLOCK_SIZE_BYTE >> 3)) {
                break;
            }

            if (count++ > 25222)
                break;

            if (!notSetDCSet)
                transaction.setDC(this.getDCSet());

            values.add(transaction);

        }

        return values;
    }

    public void clear(long timestamp) {

        Iterator<Long> iterator = this.getIterator(0, false);
        Transaction transaction;
        Long key;
        while (iterator.hasNext()) {
            key = iterator.next();
            transaction = this.map.get(key);
            if (transaction.getDeadline() < timestamp) {
                this.delete(key);
            }

        }

    }

    @Override
    public void update(Observable o, Object arg) {

        if (true)
            return;

        ObserverMessage message = (ObserverMessage) arg;

        // ON NEW BLOCK
        if (message.getType() == ObserverMessage.CHAIN_ADD_BLOCK_TYPE) {

            long dTime = Controller.getInstance().getBlockChain().getTimestamp(DCSet.getInstance());

            Transaction item;
            long start = System.currentTimeMillis();

            int i = 0;

            Iterator<Long> iterator = this.getIterator(0, false);
            // CLEAN UP
            while (iterator.hasNext()) {

                Long key = iterator.next();
                item = this.get(key);

                // CHECK IF DEADLINE PASSED
                if (i > MAX_MAP_SIZE || item.getDeadline() < dTime) {
                    iterator.remove();
                    continue;
                }

                i++;

            }
            iterator = null;
            long tickets = System.currentTimeMillis() - start;
            LOGGER.debug("update CLEAR DEADLINE time " + tickets);

        }
    }

    public boolean set(byte[] signature, Transaction transaction) {

        if (this.size() > MAX_MAP_SIZE) {
            Iterator<Long> iterator = this.getIterator(0, false);
            Transaction item;
            long dTime = Controller.getInstance().getBlockChain().getTimestamp(DCSet.getInstance());

            do {
                Long key = iterator.next();
                item = this.get(key);
                this.delete(key);

            } while (item.getDeadline() < dTime && iterator.hasNext());
        }

        Long key = Longs.fromByteArray(signature);

        if (this.map.containsKey(key)) {
            return true;
        }

        this.getDCSet().updateUncTxCounter(1);

        return this.set(key, transaction);

    }

    public boolean add(Transaction transaction) {

        return this.set(transaction.getSignature(), transaction);

    }

    // ADD broadcasted PEER
    public void addBroadcastedPeer(Transaction transaction, byte[] peer) {

        byte[] signature = transaction.getSignature();
        Long key = Longs.fromByteArray(signature);

        List<byte[]> peers;
        if (!this.peersBroadcasted.containsKey(key)) {
            peers = new ArrayList<byte[]>();
        } else {
            peers = this.peersBroadcasted.get(key);
            if (peers == null)
                peers = new ArrayList<byte[]>();
        }

        if (peers.add(peer))
            this.peersBroadcasted.put(key, peers);
    }

    public boolean needBroadcasting(Transaction transaction, byte[] peerBYtes) {

        byte[] signature = transaction.getSignature();
        Long key = Longs.fromByteArray(signature);

        List<byte[]> peers = this.peersBroadcasted.get(key);
        if (peers == null || peers.isEmpty() || (!peers.contains(peerBYtes) && peers.size() < 4)) {
            return true;
        }

        return false;

    }

    // HOW many PEERS broadcasted by this TRANSACTION
    public int getBroadcasts(Transaction transaction) {

        byte[] signature = transaction.getSignature();
        Long key = Longs.fromByteArray(signature);

        List<byte[]> peers = this.peersBroadcasted.get(key);
        if (peers == null || peers.isEmpty())
            return 0;

        return peers.size();

    }

    // HOW many PEERS broadcasted by this TRANSACTION
    public List<byte[]> getBroadcastedPeers(Transaction transaction) {

        byte[] signature = transaction.getSignature();
        Long key = Longs.fromByteArray(signature);

        if (!this.peersBroadcasted.containsKey(key)) {
            return new ArrayList<byte[]>();
        } else {
            return this.peersBroadcasted.get(key);
        }

    }

    // is this TRANSACTION is broadcasted to this PEER
    public boolean isBroadcastedToPeer(Transaction transaction, byte[] peer) {

        byte[] signature = transaction.getSignature();
        Long key = Longs.fromByteArray(signature);

        if (!this.peersBroadcasted.containsKey(key)) {
            return false;
        } else {
            return this.peersBroadcasted.get(key).contains(peer);
        }

    }

    public Transaction delete(Long key) {

        // delete BROADCASTS
        this.peersBroadcasted.remove(key);

        if (this.contains(key))
            this.getDCSet().updateUncTxCounter(-1);

        return super.delete(key);
    }


    public void delete(Transaction transaction) {
        this.delete(transaction.getSignature());
    }

    public Transaction delete(byte[] signature) {
        return this.delete(Longs.fromByteArray(signature));
    }
    public boolean contains(byte[] signature) {
        return this.contains(Longs.fromByteArray(signature));
    }

    public boolean contains(Transaction transaction) {
        return this.contains(transaction.getSignature());
    }

    public Transaction get(byte[] signature) {
        return this.get(Longs.fromByteArray(signature));
    }

    /**
     * Find all unconfirmed transaction by address, sender or recipient.
     * Need set only one parameter(address, sender,recipient)
     *
     * @param address   - address
     * @param sender    - sender
     * @param recipient - recipient
     * @param type      - type transaction
     * @param desc      - order by transaction
     * @param offset    -
     * @param limit     - count transaction
     * @return Key transactions
     */
    @SuppressWarnings({"rawtypes", "unchecked"})

    public Iterable findTransactionsKeys(String address, String sender, String recipient,
                                         int type, boolean desc, int offset, int limit, long timestamp) {
        Iterable senderKeys = null;
        Iterable recipientKeys = null;
        TreeSet<Object> treeKeys = new TreeSet<>();

        if (address != null) {
            sender = address;
            recipient = address;
        }

        if (sender == null && recipient == null) {
            return treeKeys;
        }
        //  timestamp = null;
        if (sender != null) {
            if (type > 0)
                senderKeys = Fun.filter(this.typeKey, new Fun.Tuple3<String, Long, Integer>(sender, timestamp, type));
            else
                senderKeys = Fun.filter(this.senderKey, sender);
        }

        if (recipient != null) {
            if (type > 0)
                recipientKeys = Fun.filter(this.typeKey, new Fun.Tuple3<String, Long, Integer>(recipient, timestamp, type));
            else
                recipientKeys = Fun.filter(this.recipientKey, recipient);
        }

        if (address != null) {
            treeKeys.addAll(Sets.newTreeSet(senderKeys));
            treeKeys.addAll(Sets.newTreeSet(recipientKeys));
        } else if (sender != null && recipient != null) {
            treeKeys.addAll(Sets.newTreeSet(senderKeys));
            treeKeys.retainAll(Sets.newTreeSet(recipientKeys));
        } else if (sender != null) {
            treeKeys.addAll(Sets.newTreeSet(senderKeys));
        } else if (recipient != null) {
            treeKeys.addAll(Sets.newTreeSet(recipientKeys));
        }

        Iterable keys;
        if (desc) {
            keys = ((TreeSet) treeKeys).descendingSet();
        } else {
            keys = treeKeys;
        }

        limit = (limit == 0) ? Iterables.size(keys) : limit;

        // зачем это тут ?
        //// Iterable k = Iterables.limit(Iterables.skip(keys, offset), limit);
        //// getUnconfirmedTransaction(k);
        //// return Iterables.limit(Iterables.skip(keys, offset), limit);

        return Iterables.limit(Iterables.skip(keys, offset), limit);

    }

    public List<Transaction> findTransactions(String address, String sender, String recipient,
                                         int type, boolean desc, int offset, int limit, long timestamp) {

        Iterable keys = findTransactionsKeys(address, sender, recipient,
                type, desc, offset, limit, timestamp);
        return getUnconfirmedTransaction(keys);

    }


        public List<Transaction> getUnconfirmedTransaction(Iterable keys) {
        Iterator iter = keys.iterator();
        List<Transaction> transactions = new ArrayList<>();
        Transaction item;
        Long key;

        while (iter.hasNext()) {
            key = (Long) iter.next();
            item = this.map.get(key);
            transactions.add(item);
        }
        return transactions;
    }

    public List<Transaction> getTransactionsByAddressFast100(String address) {

        Iterable senderKeys = null;
        Iterable recipientKeys = null;
        TreeSet<Object> treeKeys = new TreeSet<>();

        senderKeys = Iterables.limit(Fun.filter(this.senderKey, address), 100);
        recipientKeys = Iterables.limit(Fun.filter(this.recipientKey, address), 100);

        treeKeys.addAll(Sets.newTreeSet(senderKeys));
        treeKeys.addAll(Sets.newTreeSet(recipientKeys));

        return getUnconfirmedTransaction(Iterables.limit(treeKeys, 100));

    }

    // slow?? without index
    public List<Transaction> getTransactionsByAddress(String address) {

        ArrayList<Transaction> values = new ArrayList<Transaction>();
        Iterator<Long> iterator = this.getIterator(0, false);
        Account account = new Account(address);

        Transaction transaction;
        boolean ok = false;

        int i = 0;
        while (iterator.hasNext()) {

            transaction = map.get(iterator.next());
            if (transaction.getCreator().equals(address))
                ok = true;
            else
                ok = false;

            if (!ok) {
                transaction.setDC(this.getDCSet());
                HashSet<Account> recipients = transaction.getRecipientAccounts();

                if (recipients == null || recipients.isEmpty() || !recipients.contains(account)) {
                    continue;
                }

            }

            // SET LIMIT
            if (++i > 100)
                break;

            values.add(transaction);

        }
        return values;
    }

    public List<Transaction> getTransactions(int from, int count, boolean descending) {

        ArrayList<Transaction> values = new ArrayList<Transaction>();
        Iterator<Long> iterator = this.getIterator(from, descending);

        Transaction transaction;
        for (int i = 0; i < count; i++) {
            if (!iterator.hasNext())
                break;

            transaction = this.get(iterator.next());
            transaction.setDC(this.getDCSet());
            values.add(transaction);
        }
        iterator = null;
        return values;
    }

    public List<Transaction> getIncomedTransactions(String address, int type, long timestamp, int count, boolean descending) {

        ArrayList<Transaction> values = new ArrayList<>();
        Iterator<Long> iterator = this.getIterator(0, descending);
        Account account = new Account(address);

        int i = 0;
        Transaction transaction;
        while (iterator.hasNext()) {
            transaction = map.get(iterator.next());
            if (type != 0 && type != transaction.getType())
                continue;

            transaction.setDC(this.getDCSet());
            HashSet<Account> recipients = transaction.getRecipientAccounts();
            if (recipients == null || recipients.isEmpty())
                continue;
            if (recipients.contains(account) && transaction.getTimestamp() >= timestamp) {
                values.add(transaction);
                i++;
                if (count > 0 && i > count)
                    break;
            }
        }
        return values;
    }

}
