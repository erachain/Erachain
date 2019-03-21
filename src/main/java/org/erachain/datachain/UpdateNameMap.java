package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.erachain.core.naming.Name;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.serializer.NameSerializer;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Deprecated
public class UpdateNameMap extends DCMap<byte[], Name> {

    public UpdateNameMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public UpdateNameMap(UpdateNameMap parent) {
        super(parent, null);
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<byte[], Name> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("updateNameOrphanData")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .valueSerializer(new NameSerializer())
                .makeOrGet();
    }

    @Override
    protected Map<byte[], Name> getMemoryMap() {
        return new TreeMap<byte[], Name>(UnsignedBytes.lexicographicalComparator());
    }

    @Override
    protected Name getDefaultValue() {
        return null;
    }

    public Name get(Transaction transaction) {
        return this.get(transaction.getSignature());
    }

    public void set(Transaction transaction, Name name) {
        this.set(transaction.getSignature(), name);
    }

    public void delete(Transaction transaction) {
        this.delete(transaction.getSignature());
    }
}
