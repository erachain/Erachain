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
    // for saving in DB
    //private Atomic.Long fullWeightVar;
    //private Long fullWeight = 0L;
    //private int startedInForkHeight = 0;


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

    //public int getStartedInForkHeight() {
    //    return this.startedInForkHeight;
    //}

    public void recalcWeightFull(DCSet dcSet) {

        long weightFull = 0l;
        Iterator<Integer> iterator = this.getIndexIterator(DEFAULT_INDEX, true);
        while (iterator.hasNext()) {
            Integer key = iterator.next();
            Block.BlockHead item = this.get(key);
            weightFull += item.winValue;
        }

        //fullWeight = weightFull;
        //this.fullWeightVar.set(fullWeight);

    }

    public void putAndProcess(int height, Block.BlockHead item) {

        // get Win Value of block
        long weight = item.winValue;

        //fullWeight += weight;

        //if (this.fullWeightVar != null) {
        //    this.fullWeightVar.set(fullWeight);
        //}

        // INSERT WITH NEW KEY
        put(height, item);

    }

    public Block.BlockHead last() {
        return this.get(this.size());
    }

    public void deleteAndProcess(Integer key) {

        if (this.contains(key)) {
            // sub old value from FULL
            //Block.BlockHead value_old = this.get(key);
            //fullWeight -= value_old.winValue;

            //if (this.fullWeightVar != null) {
            //    this.fullWeightVar.set(fullWeight);
            //}
            delete(key);
        }

    }

    /**
     * Если откатить базу данных то нужно и локальные значения сбросить
     */
    //@Override
    //public void afterRollback() {
    //    this.fullWeight = fullWeightVar.get();
    //}

}