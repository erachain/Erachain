package org.erachain.datachain;

import com.google.common.primitives.Longs;
import org.erachain.core.block.Block;
import org.mapdb.DB;
import org.mapdb.SerializerBase;

import java.util.TreeMap;

/**
 * block.signature[0..7] (as Long) -> Height (as Integer)<br>
 * Here used createHashMap - its is more quick
 * <hr>
 * ключ: подпись блока<br>
 * занчение: номер блока (высота, height)<br>
 */
public class BlockSignsMap extends DCUMap<Long, Integer> {

    static final boolean SIZE_ENABLE = true;

    public BlockSignsMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, SIZE_ENABLE);
    }

    public BlockSignsMap(BlockSignsMap parent, DCSet dcSet) {
        super(parent, dcSet, SIZE_ENABLE);
    }

    @Override
    public void openMap() {

        sizeEnable = true; // разрешаем счет размера - это будет немного тормозить работу

        //OPEN HASH MAP
        //
        map = database.createHashMap("height")
                .keySerializer(SerializerBase.LONG)
                .valueSerializer(SerializerBase.INTEGER)

                // .comparator(UnsignedBytes.lexicographicalComparator()) // for byte[] KEYS
                // or from MapDB .comparator(Fun.BYTE_ARRAY_COMPARATOR)

                .counterEnable() // used for count blocks in Chain
                .makeOrGet();
    }

    protected void getMemoryMap() {
        map = new TreeMap<Long, Integer>();
    }

    public Block getBlock(byte[] signature) {
        Long key = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        Integer value = this.get(key);
        if (value == null)
            return null;

        return ((DCSet)this.databaseSet).getBlockMap().getAndProcess(value);

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

    public Integer remove(byte[] signature) {
        Long key = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        return this.remove(key);
    }

    public void delete(byte[] signature) {
        Long key = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        this.delete(key);
    }

    public boolean set(byte[] signature, Integer height) {
        Long key = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        return this.set(key, height);

    }

    public void put(byte[] signature, Integer height) {
        Long key = Longs.fromBytes(signature[0], signature[1], signature[2], signature[3],
                signature[4], signature[5], signature[6], signature[7]);
        this.put(key, height);

    }

}