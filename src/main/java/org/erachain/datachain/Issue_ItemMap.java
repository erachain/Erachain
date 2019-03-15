package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.erachain.core.transaction.Transaction;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Super Class for Issue Items
 *
 * Ключ: подпись создавшей класс записи - поидее надо поменять на ссылку
 * Значение - номер сущности
 *
 * Используется в org.erachain.core.transaction.Issue_ItemRecord#orphan(int)
 * TODO: поменять ссылку на запись с подписи на ссылку по номерам - и в таблицах ключ тоже на Лонг поменять
 * https://lab.erachain.org/erachain/Erachain/issues/465
 *
 */
public abstract class Issue_ItemMap extends DCMap<byte[], Long> {

    public Issue_ItemMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public Issue_ItemMap(Issue_ItemMap parent) {
        super(parent, null);
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<byte[], Long> getMemoryMap() {
        return new TreeMap<byte[], Long>(UnsignedBytes.lexicographicalComparator());
    }

    @Override
    protected Long getDefaultValue() {
        return 0l;
    }

    public Long get(Transaction transaction) {
        return this.get(transaction.getSignature());
    }

    public void set(Transaction transaction, Long key) {
        this.set(transaction.getSignature(), key);
    }

    public void delete(Transaction transaction) {
        this.delete(transaction.getSignature());
    }
}
