package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.datachain.TransactionMap;
import org.erachain.datachain.TransactionSuit;
import org.mapdb.SerializerBase;

import java.util.Iterator;

@Slf4j
public class TransactionSuitMapDBFork extends DBMapSuitFork<Long, Transaction> implements TransactionSuit
{

    public TransactionSuitMapDBFork(TransactionMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger, null);
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
    }

    @Override
    public Iterator<Long> getTimestampIterator(boolean descending) {
        return null;
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
