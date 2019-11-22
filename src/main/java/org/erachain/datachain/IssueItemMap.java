package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.erachain.core.transaction.Transaction;
import org.mapdb.DB;

import java.util.TreeMap;

/**
 * Super Class for Issue Items
 *
 * Ключ: подпись создавшей класс записи - по идее надо поменять на ссылку
 * Значение - номер сущности
 *
 * Используется в org.erachain.core.transaction.IssueItemRecord#orphan(int)
 * TODO: поменять ссылку на запись с подписи на ссылку по номерам - и в таблицах ключ тоже на Лонг поменять - но проверку подписи хотябы 8 байт оставить
 * https://lab.erachain.org/erachain/Erachain/issues/465
 *
 */
public abstract class IssueItemMap extends DCUMap<byte[], Long> {

    public IssueItemMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public IssueItemMap(IssueItemMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    protected void createIndexes() {
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<>(UnsignedBytes.lexicographicalComparator());
    }

    @Override
    public Long getDefaultValue() {
        return 0L;
    }

    public Long get(Transaction transaction) {
        return get(transaction.getSignature());
    }

    public void put(Transaction transaction, Long key) {
        put(transaction.getSignature(), key);
    }

    public void delete(Transaction transaction) {
        delete(transaction.getSignature());
    }
}
