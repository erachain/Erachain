package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.erachain.core.transaction.Transaction;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.TreeMap;

public class DeployATMap extends DCUMap<byte[], Long> {

    public DeployATMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public DeployATMap(DeployATMap parent) {
        super(parent, null);
    }

    protected void createIndexes() {
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap("DeployATOrphanData")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<byte[], Long>(UnsignedBytes.lexicographicalComparator());
    }

    @Override
    public Long getDefaultValue() {
        return -1L;
    }

    public Long get(Transaction transaction) {
        return this.get(transaction.getSignature());
    }

    public void put(Transaction transaction, Long key) {
        this.put(transaction.getSignature(), key);
    }

    public void delete(Transaction transaction) {
        this.delete(transaction.getSignature());
    }
}
