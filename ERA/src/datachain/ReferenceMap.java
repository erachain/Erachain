package datachain;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedBytes;
import core.crypto.Base58;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

//import org.mapdb.Fun.Tuple2;
//import core.account.Account;
//import core.block.Block;

/**
 * TODO: Надо подумать может она лишняя??
 * seek reference to tx_Parent by address+timestamp
 * account.address -> <tx2.parentTimestamp>
 *
 */
public class ReferenceMap extends DCMap<byte[], Long> {
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    public ReferenceMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public ReferenceMap(ReferenceMap parent) {
        super(parent, null);
    }


    @Override
    protected Map<byte[], Long> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("references")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                //.valueSerializer(new BlockSerializer())
                .valuesOutsideNodesEnable()
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected Map<byte[], Long> getMemoryMap() {
        return new TreeMap<byte[], Long>(UnsignedBytes.lexicographicalComparator());
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Long getDefaultValue() {
        // NEED for toByte for not referenced accounts
        return 0l;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public Long get(String address, Long timestamp) {
        byte[] key = Bytes.concat(Base58.decode(address), Longs.toByteArray(timestamp));
        return this.get(key);
    }

    public void set(String address, Long timestamp, Long reference) {
        byte[] key = Bytes.concat(Base58.decode(address), Longs.toByteArray(timestamp));
        this.set(key, reference);
    }

    public void delete(String address, Long timestamp) {
        byte[] key = Bytes.concat(Base58.decode(address), Longs.toByteArray(timestamp));
        this.delete(key);
    }

    public Long getLast(String address) {
        byte[] key = Base58.decode(address);
        return this.get(key);
    }

}