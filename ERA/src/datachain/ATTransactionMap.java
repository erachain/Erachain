package datachain;

import at.AT_Transaction;
import database.DBMap;
import database.serializer.ATTransactionSerializer;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import utils.BlExpUnit;
import utils.ObserverMessage;

import java.util.*;


public class ATTransactionMap extends DCMap<Tuple2<Integer, Integer>, AT_Transaction> {

    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
    @SuppressWarnings("rawtypes")
    private NavigableSet senderKey;
    @SuppressWarnings("rawtypes")
    private NavigableSet recipientKey;

    public ATTransactionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_AT_TX_TYPE);
            if (databaseSet.isDynamicGUI()) {
                this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_AT_TX_TYPE);
                this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_AT_TX);
            }
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_AT_TXS);
        }
    }

    public ATTransactionMap(ATTransactionMap parent) {
        super(parent, null);

    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<Tuple2<Integer, Integer>, AT_Transaction> getMap(DB database) {
        //OPEN MAP
        return this.openMap(database);
    }

    @Override
    protected Map<Tuple2<Integer, Integer>, AT_Transaction> getMemoryMap() {
        DB database = DBMaker.newMemoryDB().make();

        //OPEN MAP
        return this.openMap(database);
    }

    @SuppressWarnings("unchecked")
    private Map<Tuple2<Integer, Integer>, AT_Transaction> openMap(DB database) {
        //OPEN MAP
        BTreeMap<Tuple2<Integer, Integer>, AT_Transaction> map = database.createTreeMap("at_txs")
                .valueSerializer(new ATTransactionSerializer())
                .makeOrGet();

        this.senderKey = database.createTreeSet("sender_at_txs")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        Bind.secondaryKey(map, this.senderKey, new Fun.Function2<String, Tuple2<Integer, Integer>, AT_Transaction>() {
            @Override
            public String run(Tuple2<Integer, Integer> key, AT_Transaction val) {
                // TODO Auto-generated method stub
                return val.getSender();
            }
        });

        this.recipientKey = database.createTreeSet("recipient_at_txs")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        Bind.secondaryKey(map, this.recipientKey, new Fun.Function2<String, Tuple2<Integer, Integer>, AT_Transaction>() {
            @Override
            public String run(Tuple2<Integer, Integer> key, AT_Transaction val) {
                return val.getRecipient();
            }
        });

        //RETURN
        return map;
    }


    @Override
    protected AT_Transaction getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public boolean add(Integer blockHeight, int seq, AT_Transaction atTx) {
        atTx.setBlockHeight(blockHeight);
        atTx.setSeq(seq);
        return this.set(new Tuple2<Integer, Integer>(blockHeight, seq), atTx);
    }

    public DCMap<Tuple2<Integer, Integer>, AT_Transaction> getParent() {
        return this.parent;
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Integer height) {
        BTreeMap map = (BTreeMap) this.map;

        //FILTER ALL ATS
        Collection<Tuple2> keys = ((BTreeMap<Tuple2, AT_Transaction>) map).subMap(
                Fun.t2(height, null),
                Fun.t2(height, Fun.HI())).keySet();

        //DELETE
        for (Tuple2 key : keys) {
            this.delete(key);
        }

        if (this.parent != null)
            this.parent.getDCSet().getATTransactionMap().delete(height);

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void deleteAllAfterHeight(Integer height) {
        BTreeMap map = (BTreeMap) this.map;

        //FILTER ALL ATS
        Collection<Tuple2> keys = ((BTreeMap<Tuple2, AT_Transaction>) map).subMap(
                Fun.t2(height, null),
                Fun.t2(Fun.HI(), Fun.HI())).keySet();

        //DELETE
        for (Tuple2 key : keys) {
            this.delete(key);
        }

        if (this.parent != null)
            this.parent.getDCSet().getATTransactionMap().deleteAllAfterHeight(height);

    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public LinkedHashMap<Tuple2<Integer, Integer>, AT_Transaction> getATTransactions(Integer height) {
        LinkedHashMap<Tuple2<Integer, Integer>, AT_Transaction> txs = new LinkedHashMap<>();
        BTreeMap map = (BTreeMap) this.map;

        //FILTER ALL ATS
        Collection<Tuple2> keys = ((BTreeMap<Tuple2, AT_Transaction>) map).subMap(
                Fun.t2(height, null),
                Fun.t2(height, Fun.HI())).keySet();


        for (Tuple2 key : keys) {
            txs.put(key, this.map.get(key));
        }

        if (this.parent != null)
            txs.putAll(this.parent.getDCSet().getATTransactionMap().getATTransactions(height));

        return txs;

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<AT_Transaction> getATTransactionsBySender(String sender) {
        Iterable keys = Fun.filter(this.senderKey, sender);
        Iterator iter = keys.iterator();

        List<AT_Transaction> ats = new ArrayList<>();
        while (iter.hasNext()) {
            ats.add(this.map.get(iter.next()));
        }

        if (this.parent != null)
            ats.addAll(this.parent.getDCSet().getATTransactionMap().getATTransactionsBySender(sender));

        return ats;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set<BlExpUnit> getBlExpATTransactionsBySender(String sender) {
        Iterable keys = Fun.filter(this.senderKey, sender);
        Iterator iter = keys.iterator();

        Set<BlExpUnit> ats = new TreeSet<>();
        while (iter.hasNext()) {
            AT_Transaction aTtx = this.map.get(iter.next());
            ats.add(new BlExpUnit(aTtx.getBlockHeight(), aTtx.getSeq(), aTtx));
        }

        if (this.parent != null)
            ats.addAll(this.parent.getDCSet().getATTransactionMap().getBlExpATTransactionsBySender(sender));

        return ats;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<AT_Transaction> getATTransactionsByRecipient(String recipient) {
        Iterable keys = Fun.filter(this.recipientKey, recipient);
        Iterator iter = keys.iterator();

        List<AT_Transaction> ats = new ArrayList<>();
        while (iter.hasNext()) {
            ats.add(this.map.get(iter.next()));
        }

        if (this.parent != null)
            ats.addAll(this.parent.getDCSet().getATTransactionMap().getATTransactionsByRecipient(recipient));

        return ats;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set<BlExpUnit> getBlExpATTransactionsByRecipient(String recipient) {
        Iterable keys = Fun.filter(this.recipientKey, recipient);
        Iterator iter = keys.iterator();

        Set<BlExpUnit> ats = new TreeSet<>();
        while (iter.hasNext()) {
            AT_Transaction aTtx = this.map.get(iter.next());
            ats.add(new BlExpUnit(aTtx.getBlockHeight(), aTtx.getSeq(), aTtx));
        }

        if (this.parent != null)
            ats.addAll(this.parent.getDCSet().getATTransactionMap().getBlExpATTransactionsByRecipient(recipient));

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
