package org.erachain.dbs.mapDB;

import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.datachain.TransactionSuit;
import org.erachain.datachain.TransactionTab;
import org.erachain.utils.ReverseComparator;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple2Comparator;
import org.mapdb.SerializerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.NavigableSet;

public class TransactionSuitMapDBFork extends DBMapSuitFork<Long, Transaction> implements TransactionSuit
{

    static Logger logger = LoggerFactory.getLogger(TransactionSuitMapDBFork.class.getSimpleName());

    public TransactionSuitMapDBFork(TransactionTab parent, DBASet databaseSet) {
        super(parent, databaseSet);
    }

    @Override
    public void getMap() {

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

        //////////// HERE PROTOCOL INDEX - for GENERATE BLOCK

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

    @Override
    public Iterator<Long> getTimestampIterator(boolean descending) {
        return getIterator(TIMESTAMP_INDEX, descending);
    }

    @Override
    public Iterator typeIterator(String sender, Long timestamp, Integer type) {
        return null;
    }

    @Override
    public Iterator senderIterator(String sender) {
        return null;
    }

    @Override
    public Iterator recipientIterator(String recipient) {
        return null;
    }

}
