package datachain;

import com.google.common.primitives.UnsignedBytes;
import core.transaction.Transaction;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public abstract class Issue_ItemMap extends DCMap<byte[], Long> {
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

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

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
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
