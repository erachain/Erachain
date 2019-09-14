package org.erachain.dbs.rocksDB;

import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.dbs.mapDB.DBMapSuit;
import org.erachain.dbs.rocksDB.common.RocksDbSettings;
import org.erachain.dbs.rocksDB.indexes.ArrayIndexDB;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.indexes.ListIndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.indexes.indexByteables.IndexByteableTuple3StringLongInteger;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.integration.InnerDBTable;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.erachain.dbs.rocksDB.transformation.ByteableTransaction;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

public class TransactionRocksDBMap extends DBMapSuit<Long, Transaction>
{

    static Logger logger = LoggerFactory.getLogger(TransactionRocksDBMap.class.getSimpleName());

    private final String NAME_TABLE = "TRANSACTIONS_UNCONFIRMED_TABLE";
    private final String senderUnconfirmedTransactionIndexName = "sender_unc_txs";
    private final String recipientUnconfirmedTransactionIndexName = "recipient_unc_txs";
    private final String addressTypeUnconfirmedTransactionIndexName = "address_type_unc_txs";

    private InnerDBTable<Long, Transaction> map;
    private List<IndexDB> indexes;
    private IndexByteableTuple3StringLongInteger indexByteableTuple3StringLongInteger;
    private SimpleIndexDB<Long, Transaction, Fun.Tuple2<String, Long>> senderUnconfirmedTransactionIndex;


    public TransactionRocksDBMap(DBASet databaseSet, DB database) {
        super(databaseSet, database);
    }

    @Override
    protected void getMap() {
        map = new DBRocksDBTable<>(new ByteableLong(), new ByteableTransaction(), NAME_TABLE, indexes,
                RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),
                ROCKS_DB_FOLDER);
    }

    @Override
    protected void createIndexes() {
        senderUnconfirmedTransactionIndex = new SimpleIndexDB<>(senderUnconfirmedTransactionIndexName,
                new BiFunction<Long, Transaction, Fun.Tuple2<String, Long>>() {
                    @Override
                    public Fun.Tuple2<String, Long> apply(Long aLong, Transaction transaction) {
                        Account account = transaction.getCreator();
                        return new Fun.Tuple2<>(account == null ? "genesis" : account.getAddress(), transaction.getTimestamp());
                    }
                }, (result, key) -> org.bouncycastle.util.Arrays.concatenate(
                new ByteableString().toBytesObject(result.a),
                new ByteableLong().toBytesObject(result.b)));


        ArrayIndexDB<Long, Transaction, String> recipientsUnconfirmedTransactionIndex = new ArrayIndexDB<>(recipientUnconfirmedTransactionIndexName,
                (aLong, transaction) -> transaction.getRecipientAccounts().stream().map(Account::getAddress).toArray(String[]::new),
                (result, key) -> new ByteableString().toBytesObject(result));

        indexByteableTuple3StringLongInteger = new IndexByteableTuple3StringLongInteger();
        ListIndexDB<Long, Transaction, Fun.Tuple3<String, Long, Integer>> addressTypeUnconfirmedTransactionIndex
                = new ListIndexDB<>(addressTypeUnconfirmedTransactionIndexName,
                (aLong, transaction) -> {
                    Integer type = transaction.getType();
                    return transaction.getInvolvedAccounts().stream().map(
                            (account) -> (new Fun.Tuple3<>(account.getAddress(), transaction.getTimestamp(), type))).collect(Collectors.toList());
                }, indexByteableTuple3StringLongInteger);

        indexes = new ArrayList<>();
        indexes.add(senderUnconfirmedTransactionIndex);
        indexes.add(recipientsUnconfirmedTransactionIndex);
        indexes.add(addressTypeUnconfirmedTransactionIndex);
    }

    public IndexDB getSenderIndex() {
        return indexes.get(0);
    }
    public IndexDB getRecientIndex() {
        return indexes.get(1);
    }
    public IndexDB getAddresTypeIndex() {
        return indexes.get(2);
    }

    protected Transaction getDefaultValue() {
        return null;
    }

}
