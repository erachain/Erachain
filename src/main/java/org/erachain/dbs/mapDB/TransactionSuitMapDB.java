package org.erachain.dbs.mapDB;

import com.google.common.collect.Iterables;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionSuit;
import org.erachain.datachain.TransactionTab;
import org.erachain.utils.ReverseComparator;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple2Comparator;
import org.mapdb.SerializerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.*;

public class TransactionSuitMapDB extends DBMapSuit<Long, Transaction> implements TransactionSuit
{

    static Logger logger = LoggerFactory.getLogger(TransactionSuitMapDB.class.getSimpleName());

    @SuppressWarnings("rawtypes")
    public NavigableSet senderKey;
    @SuppressWarnings("rawtypes")
    public NavigableSet recipientKey;
    @SuppressWarnings("rawtypes")
    public NavigableSet typeKey;

    public TransactionSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database);
    }

    @Override
    protected void getMap() {

        // OPEN MAP
        map = database.createHashMap("transactions")
                .keySerializer(SerializerBase.BASIC)
                .valueSerializer(new TransactionSerializer())
                .counterEnable()
                .makeOrGet();


        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        this.senderKey = database.createTreeSet("sender_unc_txs").comparator(Fun.COMPARATOR)
                .counterEnable()
                .makeOrGet();

        Bind.secondaryKey((Bind.MapWithModificationListener)map, this.senderKey, new Fun.Function2<Tuple2<String, Long>, Long, Transaction>() {
            @Override
            public Tuple2<String, Long> run(Long key, Transaction val) {
                Account account = val.getCreator();
                return new Tuple2<String, Long>(account == null ? "genesis" : account.getAddress(), val.getTimestamp());
            }
        });

        this.recipientKey = database.createTreeSet("recipient_unc_txs").comparator(Fun.COMPARATOR)
                .counterEnable()
                .makeOrGet();
        Bind.secondaryKeys((Bind.MapWithModificationListener)map, this.recipientKey,
                new Fun.Function2<String[], Long, Transaction>() {
                    @Override
                    public String[] run(Long key, Transaction val) {
                        List<String> recps = new ArrayList<String>();

                        val.setDC((DCSet)databaseSet);

                        for (Account acc : val.getRecipientAccounts()) {
                            // recps.add(acc.getAddress() + val.viewTimestamp()); уникальнось внутри Бинда делается
                            recps.add(acc.getAddress());
                        }
                        String[] ret = new String[recps.size()];
                        ret = recps.toArray(ret);
                        return ret;
                    }
                });

        this.typeKey = database.createTreeSet("address_type_unc_txs").comparator(Fun.COMPARATOR)
                .counterEnable()
                .makeOrGet();
        Bind.secondaryKeys((Bind.MapWithModificationListener)map, this.typeKey,
                new Fun.Function2<Fun.Tuple3<String, Long, Integer>[], Long, Transaction>() {
                    @Override
                    public Fun.Tuple3<String, Long, Integer>[] run(Long key, Transaction val) {
                        List<Fun.Tuple3<String, Long, Integer>> recps = new ArrayList<Fun.Tuple3<String, Long, Integer>>();
                        Integer type = val.getType();

                        val.setDC((DCSet)databaseSet);

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

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {

        //////////// HERE PROTOCOL INDEX - for GENERATE BLOCL

        // TIMESTAMP INDEX
        Tuple2Comparator<Long, Long> comparator = new Tuple2Comparator<Long, Long>(Fun.COMPARATOR,
                //UnsignedBytes.lexicographicalComparator()
                Fun.COMPARATOR);
        NavigableSet<Tuple2<Long, Long>> heightIndex = database
                .createTreeSet("transactions_index_timestamp")
                .comparator(comparator)
                .counterEnable()
                .makeOrGet();

        NavigableSet<Tuple2<Long, Long>> descendingHeightIndex = database
                .createTreeSet("transactions_index_timestamp_descending")
                .comparator(new ReverseComparator(comparator))
                .counterEnable()
                .makeOrGet();

        createIndex(TIMESTAMP_INDEX, heightIndex, descendingHeightIndex,
                new Fun.Function2<Long, Long, Transaction>() {
                    @Override
                    public Long run(Long key, Transaction value) {
                        return value.getTimestamp();
                    }
                });

    }

    @Override
    public Transaction getDefaultValue() {
        return DEFAULT_VALUE;
    }

    public Iterable typeKeys(String sender, Long timestamp, Integer type) {
        return Fun.filter(((TransactionSuitMapDB)map).typeKey,
                new Fun.Tuple3<String, Long, Integer>(sender, timestamp, type));
    }
    public Iterable senderKeys(String sender) {
        return Fun.filter(((TransactionSuitMapDB)map).senderKey, sender);
    }
    public Iterable recipientKeys(String recipient) {
        return Fun.filter(((TransactionSuitMapDB)map).recipientKey, recipient);
    }

    @Override
    public Iterator<Long> getTimestampIterator() {
        return getIterator(TIMESTAMP_INDEX, false);
    }

    @Override
    public Collection<Long> getFromToKeys(long fromKey, long toKey) {

        List<Long> treeKeys = new ArrayList<Long>();

        //NavigableMap set = new NavigableMap<Long, Transaction>();
        // NodeIterator


        // DESCENDING + 1000
        Iterable iterable = this.indexes.get(TransactionSuit.TIMESTAMP_INDEX + DESCENDING_SHIFT_INDEX);
        Iterable iterableLimit = Iterables.limit(Iterables.skip(iterable, (int) fromKey), (int) (toKey - fromKey));

        Iterator<Tuple2<Long, Long>> iterator = iterableLimit.iterator();
        while (iterator.hasNext()) {
            treeKeys.add(iterator.next().b);
        }

        return treeKeys;

    }

    //@Override
    //public Iterator<Long> getCeatorIterator() {
    //    return ((TransactionSuitMapDB)map).senderKey.iterator();
    //}

}
