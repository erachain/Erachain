package org.erachain.datachain;

import org.erachain.at.ATTransaction;
import org.erachain.controller.Controller;
import org.erachain.dbs.DBTab;
import org.erachain.database.serializer.ATTransactionSerializer;
import org.erachain.utils.BlExpUnit;
import org.erachain.utils.ObserverMessage;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;

import java.util.*;


public class ATTransactionMap extends DCUMap<Tuple2<Integer, Integer>, ATTransaction> {

    @SuppressWarnings("rawtypes")
    private NavigableSet senderKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet recipientKey;

    public ATTransactionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_AT_TX_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_AT_TXS);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_AT_TX_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_AT_TX);
        }
    }

    public ATTransactionMap(ATTransactionMap parent, DCSet dcSet) {
        super(parent, dcSet);

    }

    protected void createIndexes() {
    }

    @Override
    protected void getMap() {
        //OPEN MAP
        map = this.openMap(database);
    }

    @Override
    protected void getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        //OPEN MAP
        map = this.openMap(database);
    }

    @SuppressWarnings("unchecked")
    private Map<Tuple2<Integer, Integer>, ATTransaction> openMap(DB database) {
        //OPEN MAP
        BTreeMap<Tuple2<Integer, Integer>, ATTransaction> map = database.createTreeMap("at_txs")
                .valueSerializer(new ATTransactionSerializer())
                .makeOrGet();

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return map;


        this.senderKey = database.createTreeSet("sender_at_txs")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        Bind.secondaryKey(map, this.senderKey, new Fun.Function2<String, Tuple2<Integer, Integer>, ATTransaction>() {
            @Override
            public String run(Tuple2<Integer, Integer> key, ATTransaction val) {
                // TODO Auto-generated method stub
                return val.getSender();
            }
        });

        this.recipientKey = database.createTreeSet("recipient_at_txs")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        Bind.secondaryKey(map, this.recipientKey, new Fun.Function2<String, Tuple2<Integer, Integer>, ATTransaction>() {
            @Override
            public String run(Tuple2<Integer, Integer> key, ATTransaction val) {
                return val.getRecipient();
            }
        });

        //RETURN
        return map;
    }


    @Override
    protected ATTransaction getDefaultValue() {
        return null;
    }

    public boolean add(Integer blockHeight, int seq, ATTransaction atTx) {
        atTx.setBlockHeight(blockHeight);
        atTx.setSeq(seq);
        return this.set(new Tuple2<Integer, Integer>(blockHeight, seq), atTx);
    }

    public DBTab<Tuple2<Integer, Integer>, ATTransaction> getParent() {
        return this.parent;
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Integer height) {
        BTreeMap map = (BTreeMap) this.map;

        //FILTER ALL ATS
        Collection<Tuple2> keys = ((BTreeMap<Tuple2, ATTransaction>) map).subMap(
                Fun.t2(height, null),
                Fun.t2(height, Fun.HI())).keySet();

        //DELETE
        for (Tuple2 key : keys) {
            this.remove(key);
        }

        if (this.parent != null)
            ((DCSet)this.parent.getDBSet()).getATTransactionMap().delete(height);

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void deleteAllAfterHeight(Integer height) {
        BTreeMap map = (BTreeMap) this.map;

        //FILTER ALL ATS
        Collection<Tuple2> keys = ((BTreeMap<Tuple2, ATTransaction>) map).subMap(
                Fun.t2(height, null),
                Fun.t2(Fun.HI(), Fun.HI())).keySet();

        //DELETE
        for (Tuple2 key : keys) {
            this.remove(key);
        }

        if (this.parent != null)
            ((DCSet)this.parent.getDBSet()).getATTransactionMap().deleteAllAfterHeight(height);

    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public LinkedHashMap<Tuple2<Integer, Integer>, ATTransaction> getATTransactions(Integer height) {
        LinkedHashMap<Tuple2<Integer, Integer>, ATTransaction> txs = new LinkedHashMap<>();
        BTreeMap map = (BTreeMap) this.map;

        //FILTER ALL ATS
        Collection<Tuple2> keys = ((BTreeMap<Tuple2, ATTransaction>) map).subMap(
                Fun.t2(height, null),
                Fun.t2(height, Fun.HI())).keySet();


        for (Tuple2 key : keys) {
            txs.put(key, this.map.get(key));
        }

        if (this.parent != null)
            txs.putAll(((DCSet)this.parent.getDBSet()).getATTransactionMap().getATTransactions(height));

        return txs;

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<ATTransaction> getATTransactionsBySender(String sender) {
        Iterable keys = Fun.filter(this.senderKey, sender);
        Iterator iter = keys.iterator();

        List<ATTransaction> ats = new ArrayList<>();
        while (iter.hasNext()) {
            ats.add(this.map.get(iter.next()));
        }

        if (this.parent != null)
            ats.addAll(((DCSet)this.parent.getDBSet()).getATTransactionMap().getATTransactionsBySender(sender));

        return ats;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set<BlExpUnit> getBlExpATTransactionsBySender(String sender) {
        Iterable keys = Fun.filter(this.senderKey, sender);
        Iterator iter = keys.iterator();

        Set<BlExpUnit> ats = new TreeSet<>();
        while (iter.hasNext()) {
            ATTransaction aTtx = this.map.get(iter.next());
            ats.add(new BlExpUnit(aTtx.getBlockHeight(), aTtx.getSeq(), aTtx));
        }

        if (this.parent != null)
            ats.addAll(((DCSet)this.parent.getDBSet()).getATTransactionMap().getBlExpATTransactionsBySender(sender));

        return ats;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<ATTransaction> getATTransactionsByRecipient(String recipient) {
        Iterable keys = Fun.filter(this.recipientKey, recipient);
        Iterator iter = keys.iterator();

        List<ATTransaction> ats = new ArrayList<>();
        while (iter.hasNext()) {
            ats.add(this.map.get(iter.next()));
        }

        if (this.parent != null)
            ats.addAll(((DCSet)this.parent.getDBSet()).getATTransactionMap().getATTransactionsByRecipient(recipient));

        return ats;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set<BlExpUnit> getBlExpATTransactionsByRecipient(String recipient) {
        Iterable keys = Fun.filter(this.recipientKey, recipient);
        Iterator iter = keys.iterator();

        Set<BlExpUnit> ats = new TreeSet<>();
        while (iter.hasNext()) {
            ATTransaction aTtx = this.map.get(iter.next());
            ats.add(new BlExpUnit(aTtx.getBlockHeight(), aTtx.getSeq(), aTtx));
        }

        if (this.parent != null)
            ats.addAll(((DCSet)this.parent.getDBSet()).getATTransactionMap().getBlExpATTransactionsByRecipient(recipient));

        return ats;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Tuple2<Integer, Integer> getNextATTransaction(Integer height, Integer seq, String recipient) {

        // TODO - ERROR - FORK not used!
        Iterable keys = Fun.filter(this.recipientKey, recipient);
        Iterator iter = keys.iterator();
        int prevKey = height;
        while (iter.hasNext()) {
            Tuple2<Integer, Integer> key = (Tuple2<Integer, Integer>) iter.next();
            if (key.a >= height) {
                if (key.a != prevKey) {
                    seq = 0;
                }
                prevKey = key.a;
                if (key.b >= seq)
                    return key;
            }
        }
        return null;
    }

}
