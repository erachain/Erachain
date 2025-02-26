package org.erachain.dbs.mapDB;

import com.google.common.primitives.Longs;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.database.IndexIterator;
import org.erachain.database.serializer.TransactionUncSerializer;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionSuit;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.ntp.NTP;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple2Comparator;
import org.slf4j.Logger;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

@Slf4j
public class TransactionSuitMapDB extends DBMapSuit<Long, Transaction> implements TransactionSuit {

    /**
     * Сколько трнзакций можно положить в таблицу до ее обновления (очистки). Иначе начинает тормозить,
     * надо удалитьтаблицу и переложить в нее оставшиеся неподтвержденные
     */
    int MAX_PUT_COUNT = 1000000;
    @Getter
    protected Atomic.Long totalAdded;

    @SuppressWarnings("rawtypes")
    public NavigableSet timestampIndex;

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
                .valueSerializer(new TransactionUncSerializer())
                .counterEnable() // разрешаем счет размера - это будет немного тормозить работу
                .makeOrGet();

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {

        totalAdded = database.getAtomicLong("utx_total_added");

        //////////// HERE PROTOCOL INDEX - for GENERATE BLOCL

        // TIMESTAMP INDEX
        Tuple2Comparator<Long, Long> comparator = new Tuple2Comparator<Long, Long>(Fun.COMPARATOR,
                //UnsignedBytes.lexicographicalComparator()
                Fun.COMPARATOR);
        timestampIndex = database.createTreeSet("transactions_index_timestamp")
                .comparator(comparator)
                .makeOrGet();

        Bind.secondaryKey((Bind.MapWithModificationListener) map, this.timestampIndex, new Fun.Function2<Long, Long, Transaction>() {
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
                    public String[] run(Long key, Transaction transaction) {
                        List<String> recps = new ArrayList<String>();

                        // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                        if (transaction.noDCSet()) {
                            transaction.setDC((DCSet) databaseSet, true);
                        }

                        for (Account acc : transaction.getRecipientAccounts()) {
                            // recps.add(acc.getAddress() + transaction.viewTimestamp()); уникальнось внутри Бинда делается
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
                    public Fun.Tuple3<String, Long, Integer>[] run(Long key, Transaction transaction) {
                        List<Fun.Tuple3<String, Long, Integer>> recps = new ArrayList<Fun.Tuple3<String, Long, Integer>>();
                        Integer type = transaction.getType();

                        // NEED set DCSet for calculate getRecipientAccounts in RVouch for example
                        if (transaction.noDCSet()) {
                            transaction.setDC((DCSet) databaseSet, true);
                        }

                        for (Account acc : transaction.getInvolvedAccounts()) {
                            recps.add(new Fun.Tuple3<String, Long, Integer>(acc.getAddress(), transaction.getTimestamp(), type));

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

    public IteratorCloseable<Long> typeIterator(String sender, Long timestamp, Integer type) {
        return new IteratorCloseableImpl(Fun.filter(typeKey,
                new Fun.Tuple3<String, Long, Integer>(sender, timestamp, type)).iterator());
    }

    public IteratorCloseable<Long> senderIterator(String sender) {
        return new IteratorCloseableImpl(Fun.filter(senderKey, sender).iterator());
    }

    public IteratorCloseable<Long> recipientIterator(String recipient) {
        return new IteratorCloseableImpl(Fun.filter(recipientKey, recipient).iterator());
    }

    @Override
    public IteratorCloseable<Long> getTimestampIterator(boolean descending) {
        if (descending)
            return IteratorCloseableImpl.make(new IndexIterator(timestampIndex.descendingSet()));

        return IteratorCloseableImpl.make(new IndexIterator(timestampIndex));
    }

    @Override
    public void put(Long key, Transaction value) {
        super.put(key, value);

        // Так как у MapDB замечены тормоза поле создания большого числа записей, даже если они удалялись
        // проведем чистку полную таблицы
        if (totalAdded.incrementAndGet() > DCSet.DELETIONS_BEFORE_COMPACT) {

            long ntpTimestamp = NTP.getTime();
            /////// CLEAR table and refresh uTx
            try {
                // уже пора чистить таблицу - удалим ее перенеся оставшиеся в новую чистую
                int size = size();
                int i = size;
                Transaction[] uTxs = values().toArray(new Transaction[i]);

                // ПОЛНОЕ УДАЛЕНИЕ
                clear();

                // Заносим обратно
                Transaction item;
                do {
                    item = uTxs[--i];
                    put(Longs.fromByteArray(item.getSignature()), item);
                } while (i > 0);
                logger.debug("ADDED UTXs: " + size + ", delay[s]: " + (NTP.getTime() - ntpTimestamp) / 1000);
            } catch (OutOfMemoryError e) {
                logger.error(e.getMessage(), e);
                Controller.getInstance().stopAndExit(456);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void clear() {
        totalAdded.set(0);
        super.clear();
    }
}
