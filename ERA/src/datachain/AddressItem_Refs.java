package datachain;

import core.crypto.Base58;
import database.DBMap;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class AddressItem_Refs extends DCMap<Tuple2<byte[], Long>, byte[]> {
    protected String name;
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    public AddressItem_Refs(DCSet databaseSet, DB database, String name,
                            int observeReset, int observeAdd, int observeRemove, int observeList
    ) {
        super(databaseSet, database);
        this.name = name;

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, observeReset);
            if (databaseSet.isDynamicGUI()) {
                this.observableData.put(DBMap.NOTIFY_ADD, observeAdd);
                this.observableData.put(DBMap.NOTIFY_REMOVE, observeRemove);
            }
            this.observableData.put(DBMap.NOTIFY_LIST, observeList);
        }

    }

    public AddressItem_Refs(AddressItem_Refs parent) {
        super(parent, null);
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<Tuple2<byte[], Long>, byte[]> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("address_" + this.name + "_refs")
                .keySerializer(BTreeKeySerializer.TUPLE2)
                //.comparator(UnsignedBytes.lexicographicalComparator())
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected Map<Tuple2<byte[], Long>, byte[]> getMemoryMap() {
        //return new TreeMap<Tuple2<byte[], Long>, byte[]>(UnsignedBytes.lexicographicalComparator());
        return new TreeMap<Tuple2<byte[], Long>, byte[]>();
    }

    @Override
    protected byte[] getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public byte[] get(String address, Long key) {
        return this.get(new Tuple2<byte[], Long>(Base58.decode(address), key));
    }

    public void set(String address, Long key, byte[] ref) {
        this.set(new Tuple2<byte[], Long>(Base58.decode(address), key), ref);
    }

    public void delete(String address, Long key) {
        this.delete(new Tuple2<byte[], Long>(Base58.decode(address), key));
    }
}
