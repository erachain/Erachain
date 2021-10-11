package org.erachain.dbs.rocksDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.ArrayIndexDB;
import org.erachain.dbs.rocksDB.indexes.ListIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.indexes.indexByteables.IndexByteableLong;
import org.erachain.dbs.rocksDB.indexes.indexByteables.IndexByteableTuple3StringLongInteger;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.erachain.dbs.rocksDB.transformation.toBytesStringLongInteger;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
public class TransactionSuitRocksDB extends DBMapSuit<Long, Transaction> implements TransactionSuit
{

    private final String NAME_TABLE = "TRANSACTIONS_UNCONFIRMED_TABLE";
    private final String timestampIndexName = "timestamp_unc_txs";
    private final String senderIndexName = "sender_unc_txs";
    private final String addressTypeIndexName = "address_type_unc_txs";

    private SimpleIndexDB<Long, Transaction, Long> timestampIndex;
    private SimpleIndexDB<Long, Transaction, Fun.Tuple2<String, Long>> senderIndex;
    private ArrayIndexDB<Long, Transaction, String> recipientsIndex;
    ListIndexDB<Long, Transaction, Fun.Tuple3<String, Long, Integer>> addressTypeIndex;

    public TransactionSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, true);
    }

    @Override
    public void openMap() {

        map = new DBRocksDBTableDBCommitedAsBath<>(new ByteableLong(), new ByteableTransaction(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(),
                databaseSet, sizeEnable);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {

        // USE counter index
        indexes = new ArrayList<>();

        timestampIndex = new SimpleIndexDB<>(timestampIndexName,
                new BiFunction<Long, Transaction, Long>() {
                    @Override
                    public Long apply(Long aLong, Transaction transaction) {
                        return transaction.getTimestamp();
                    }
                    //}, (result, key) ->new ByteableLong().toBytesObject(result)); // создает Класс на лету и переопределяет его метод
                }, new IndexByteableLong()); // а тут мы уже создали заранее Класс

        indexes.add(timestampIndex);


        if (Controller.getInstance().onlyProtocolIndexing) {
            return;
        }

        senderIndex = new SimpleIndexDB<>(senderIndexName,
                new BiFunction<Long, Transaction, Fun.Tuple2<String, Long>>() {
                    @Override
                    public Fun.Tuple2<String, Long> apply(Long aLong, Transaction transaction) {
                        Account account = transaction.getCreator();
                        return new Fun.Tuple2<>(account == null ? "genesis" : account.getAddress(), transaction.getTimestamp());
                    }
                }, (result) -> org.bouncycastle.util.Arrays.concatenate(
                new ByteableString().toBytesObject(result.a),
                new ByteableLong().toBytesObject(result.b)));

        String recipientsIndexName = "recipient_unc_txs";
        recipientsIndex = new ArrayIndexDB<>(recipientsIndexName,
                (aLong, transaction) -> {
                    if (transaction.noDCSet()) {
                        transaction.setDC((DCSet) databaseSet, true);
                    }
                    return transaction.getRecipientAccounts().stream().map(Account::getAddress).toArray(String[]::new);
                },
                (result) -> new ByteableString().toBytesObject(result));

        addressTypeIndex
                = new ListIndexDB<>(addressTypeIndexName,
                (aLong, transaction) -> {
                    // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                    if (transaction.noDCSet()) {
                        transaction.setDC((DCSet) databaseSet, true);
                    }
                    Integer type = transaction.getType();
                    return transaction.getInvolvedAccounts().stream().map(
                            (account) -> (new Fun.Tuple3<>(account.getAddress(), transaction.getTimestamp(), type))).collect(Collectors.toList());
                }, new IndexByteableTuple3StringLongInteger());

        indexes.add(addressTypeIndex);
        indexes.add(senderIndex);
        indexes.add(recipientsIndex);

    }

    @Override
    public IteratorCloseable<Long> typeIterator(String sender, Long timestamp, Integer type) {
        return map.getIndexIteratorFilter(addressTypeIndex.getColumnFamilyHandle(),
                toBytesStringLongInteger.toBytes(sender, timestamp, type), false, true);
    }

    @Override
    public IteratorCloseable<Long> senderIterator(String sender) {
        return map.getIndexIteratorFilter(senderIndex.getColumnFamilyHandle(), sender.getBytes(), false, true);
    }

    @Override
    public IteratorCloseable<Long> recipientIterator(String recipient) {
        return map.getIndexIteratorFilter(recipientsIndex.getColumnFamilyHandle(), recipient.getBytes(), false, true);
    }

    @Override
    public IteratorCloseable<Long> getTimestampIterator(boolean descending) {
        return map.getIndexIterator(timestampIndex.getColumnFamilyHandle(), descending, true);
    }

}
