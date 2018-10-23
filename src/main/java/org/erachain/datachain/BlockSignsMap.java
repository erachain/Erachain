package org.erachain.datachain;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedBytes;
import org.erachain.core.block.Block;
import org.mapdb.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * block.signature[0..7] (as Long) -> Height (as Integer)<br>
 * Here used createHashMap - its is more quick
 * <hr>
 * ключ: подпись блока<br>
 * занчение: номер блока (высота, height)<br>
 */
public class BlockSignsMap extends DCMap<Long, Integer> {
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    public BlockSignsMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

    }

    public BlockSignsMap(BlockSignsMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<Long, Integer> getMap(DB database) {
        //OPEN HASH MAP
        //
        return database.createHashMap("height")
                .keySerializer(SerializerBase.LONG)
                .valueSerializer(SerializerBase.INTEGER)

                // .comparator(UnsignedBytes.lexicographicalComparator()) // for byte[] KEYS
                // or from MapDB .comparator(Fun.BYTE_ARRAY_COMPARATOR)

                .counterEnable() // used in datachain.DCSet.DCSet(org.mapdb.DB, boolean, boolean, boolean)
                .makeOrGet();
    }

    @Override
    protected Map<Long, Integer> getMemoryMap() {
        //return new TreeMap<long[], Integer>(UnsignedBytes.lexicographicalComparator()); // for byte[] KEYS
        return new TreeMap<Long, Integer>();
    }

    @Override
    protected Integer getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public boolean contains(byte[] signature) {
        Long key = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        return this.contains(key);
    }

    public Integer get(Block block) {
        byte[] signature = block.getSignature();
        Long key = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        return this.get(key);
    }

    public Integer get(byte[] signature) {
        Long key = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        return this.get(key);
    }

    public void delete(byte[] signature) {
        Long key = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        this.delete(key);
    }

    public Block getBlock(byte[] signature) {
        Long key = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        Integer value = this.get(key);
        if (value == null)
            return null;

        return this.getDCSet().getBlockMap().get(value);

    }

    public boolean set(byte[] signature, Integer height) {
        Long key = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        return this.set(key, height);

    }

}