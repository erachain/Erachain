package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.datachain.TransactionMap;
import org.erachain.datachain.TransactionSuit;
import org.erachain.dbs.IteratorCloseable;
import org.mapdb.SerializerBase;

import java.util.Iterator;

@Slf4j
public class TransactionSuitMapDBFork extends DBMapSuitFork<Long, Transaction> implements TransactionSuit {

    public TransactionSuitMapDBFork(TransactionMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger);
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
    public IteratorCloseable<Long> getTimestampIterator(boolean descending) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> typeIterator(String sender, Long timestamp, Integer type) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> senderIterator(String sender) {
        return null;
    }

    @Override
    public IteratorCloseable<Long> recipientIterator(String recipient) {
        return null;
    }

    @Override
    public boolean writeToParent() {

        boolean updated = false;

        Iterator<Long> iterator = this.map.keySet().iterator();

        while (iterator.hasNext()) {
            Long key = iterator.next();
            // тут через Очередь сработает - без ошибок от закрытия
            parent.put(key, this.map.get(key));
            updated = true;
        }

        if (deleted != null) {
            Iterator<Long> iteratorDeleted = this.deleted.keySet().iterator();
            while (iteratorDeleted.hasNext()) {
                // тут через Очередь сработает - без ошибок от закрытия
                parent.delete(iteratorDeleted.next());
                updated = true;
            }
        }

        return updated;
    }

}
