package org.erachain.datachain;


import org.erachain.core.block.Block;
import org.erachain.database.serializer.BlockHeadSerializer;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;

/**
 *  Block Height -> Block.BlockHead:
 *  + FACE - version, creator, signature, transactionsCount, transactionsHash
 *  + parentSignature
 *  + Forging Data - Forging Value, Win Value, Target Value
 *
 */
public class BlocksHeadsMap extends DCUMap<Integer, Block.BlockHead> {

    static final String NAME = "blocks_heads";
    static Logger LOGGER = LoggerFactory.getLogger(BlocksHeadsMap.class.getName());


    public BlocksHeadsMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public BlocksHeadsMap(BlocksHeadsMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap(NAME)
                .keySerializer(BTreeKeySerializer.BASIC)
                .valueSerializer(new BlockHeadSerializer())
                //.comparator(Fun.COMPARATOR) // USE Fun.HI value for map.sub
                .makeOrGet();

        HI = Integer.MAX_VALUE;
        LO = 0;
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<Integer, Block.BlockHead>();
    }

    @Override
    public int size() {
        return ((DCSet) databaseSet).getBlockSignsMap().size();
    }

    public Long getFullWeight() {
        return get(size()).totalWinValue;
    }

    public void recalcWeightFull(DCSet dcSet) {

        long weightFull = 0L;
        Iterator<Integer> iterator = this.getIterator();
        while (iterator.hasNext()) {
            Integer key = iterator.next();
            Block.BlockHead item = this.get(key);
            weightFull += item.winValue;
        }

    }

    public void putAndProcess(int height, Block.BlockHead item) {

        // INSERT WITH NEW KEY
        put(height, item);

    }

    public Block.BlockHead last() {
        return this.get(this.size());
    }

    public void deleteAndProcess(Integer key) {

        if (this.contains(key)) {
            delete(key);
        }

    }

}