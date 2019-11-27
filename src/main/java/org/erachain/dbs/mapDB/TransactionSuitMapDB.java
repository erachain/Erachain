package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionSuit;
import org.erachain.utils.ReverseComparator;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple2Comparator;
import org.mapdb.SerializerBase;
import org.slf4j.Logger;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;

@Slf4j
public class TransactionSuitMapDB extends DBMapSuit<Long, Transaction> implements TransactionSuit
{

    @SuppressWarnings("rawtypes")
    public NavigableSet senderKey;
    @SuppressWarnings("rawtypes")
    public NavigableSet recipientKey;
    @SuppressWarnings("rawtypes")
    public NavigableSet typeKey;

    public TransactionSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, true, null);
    }

    public TransactionSuitMapDB(DBASet databaseSet, DB database, Logger logger) {
        super(databaseSet, database, logger, true, null);
    }

    @Override
    public void openMap() {

        sizeEnable = true; // разрешаем счет размера - это будет немного тормозить работу

        // OPEN MAP
        map = database.createHashMap("transactions")
                .keySerializer(SerializerBase.LONG)
                .valueSerializer(new TransactionSerializer())
                .counterEnable()
                .makeOrGet();

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

        ///////////////
        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        this.senderKey = database.createTreeSet("sender_unc_txs").comparator(Fun.COMPARATOR)
                .makeOrGet();

        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.senderKey, new Fun.Function2<Tuple2<String, Long>, Long, Transaction>() {
            @Override
            public Tuple2<String, Long> run(Long key, Transaction val) {
                Account account = val.getCreator();
                return new Tuple2<String, Long>(account == null ? "genesis" : account.getAddress(), val.getTimestamp());
            }
        });

        this.recipientKey = database.createTreeSet("recipient_unc_txs").comparator(Fun.COMPARATOR)
                .makeOrGet();
        Bind.secondaryKeys((Bind.MapWithModificationListener) map, this.recipientKey,
                new Fun.Function2<String[], Long, Transaction>() {
                    @Override
                    public String[] run(Long key, Transaction val) {
                        List<String> recps = new ArrayList<String>();

                        // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                        val.setDC((DCSet) databaseSet);

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
                .makeOrGet();
        Bind.secondaryKeys((Bind.MapWithModificationListener) map, this.typeKey,
                new Fun.Function2<Fun.Tuple3<String, Long, Integer>[], Long, Transaction>() {
                    @Override
                    public Fun.Tuple3<String, Long, Integer>[] run(Long key, Transaction val) {
                        List<Fun.Tuple3<String, Long, Integer>> recps = new ArrayList<Fun.Tuple3<String, Long, Integer>>();
                        Integer type = val.getType();

                        // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                        val.setDC((DCSet) databaseSet);

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

    public Iterator typeIterator(String sender, Long timestamp, Integer type) {
        return Fun.filter(typeKey,
                new Fun.Tuple3<String, Long, Integer>(sender, timestamp, type)).iterator();
    }

    public Iterator senderIterator(String sender) {
        return Fun.filter(senderKey, sender).iterator();
    }

    public Iterator recipientIterator(String recipient) {
        return Fun.filter(recipientKey, recipient).iterator();
    }

    @Override
    public Iterator<Long> getTimestampIterator(boolean descending) {
        return getIterator(TIMESTAMP_INDEX, descending);
    }

    //@Override
    //public Iterator<Long> getCeatorIterator() {
    //    return ((TransactionSuitMapDB)map).senderKey.iterator();
    //}

}
