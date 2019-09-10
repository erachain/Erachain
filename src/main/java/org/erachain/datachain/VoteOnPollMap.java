package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.erachain.core.transaction.Transaction;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Deprecated
public class VoteOnPollMap extends DCMap<byte[], Integer> {

    public VoteOnPollMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public VoteOnPollMap(VoteOnPollMap parent) {
        super(parent, null);
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected void getMap(DB database) {
        //OPEN MAP
        map = database.createTreeMap("voteOnPollOrphanData")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<byte[], Integer>(UnsignedBytes.lexicographicalComparator());
    }

    @Override
    protected Integer getDefaultValue() {
        return -1;
    }

    public Integer get(Transaction transaction) {
        return this.get(transaction.getSignature());
    }

    public void set(Transaction transaction, Integer value) {
        this.set(transaction.getSignature(), value);
    }

    public void delete(Transaction transaction) {
        this.delete(transaction.getSignature());
    }
}
