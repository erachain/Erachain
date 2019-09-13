package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.erachain.core.transaction.CancelSellNameTransaction;
import org.erachain.dbs.DBMap;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

import java.math.BigDecimal;
import java.util.TreeMap;

public class CancelSellNameMap extends DCMap<byte[], BigDecimal> {

    public CancelSellNameMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_NAME_SALE_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_NAME_SALE_TYPE);
            this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_NAME_SALE_TYPE);
            this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_NAME_SALE_TYPE);
        }
    }

    public CancelSellNameMap(CancelSellNameMap parent) {
        super(parent, null);
    }

    protected void createIndexes() {
    }

    @Override
    protected void getMap() {
        //OPEN MAP
        map = database.createTreeMap("cancelNameOrphanData")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<byte[], BigDecimal>(UnsignedBytes.lexicographicalComparator());
    }

    @Override
    protected BigDecimal getDefaultValue() {
        return null;
    }

    public void delete(CancelSellNameTransaction transaction) {
        this.delete(transaction.getSignature());
    }

    public BigDecimal get(CancelSellNameTransaction transaction) {
        return this.get(transaction.getSignature());
    }

    public void set(CancelSellNameTransaction transaction, BigDecimal value) {
        this.set(transaction.getSignature(), value);
    }
}
