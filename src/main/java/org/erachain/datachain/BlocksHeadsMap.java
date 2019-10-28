package org.erachain.datachain;


import org.erachain.core.block.Block;
import org.erachain.database.serializer.BlockHeadSerializer;
import org.mapdb.Atomic;
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
    private Atomic.Long fullWeightVar;
    private Long fullWeight = 0L;
    //private int startedInForkHeight = 0;


    public BlocksHeadsMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        this.fullWeightVar = database.getAtomicLong("fullWeight");
        this.fullWeight = this.fullWeightVar.get();
        if (this.fullWeight == null)
            this.fullWeight = 0L;

    }

    public BlocksHeadsMap(BlocksHeadsMap parent, DCSet dcSet) {
        super(parent, dcSet);

        this.fullWeight = parent.getFullWeight();

    }

    @Override
    protected void openMap() {
        //OPEN MAP
        map = database.createTreeMap(NAME)
                .keySerializer(BTreeKeySerializer.BASIC)
                .valueSerializer(new BlockHeadSerializer())
                /// in Signs ///.counterEnable() // used in datachain.DCSet.DCSet(org.mapdb.DB, boolean, boolean, boolean)
                .makeOrGet();
    }

	/*
	 *  NEED .counterEnable in MAP(non-Javadoc)
	 * @see datachain.DCMap#size()
	@Override
	public int size() {
		return this.key;
	}
	 */

    @Override
    protected void createIndexes() {
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<Integer, Block.BlockHead>();
    }

    @Override
    protected Block.BlockHead getDefaultValue() {
        return null;
    }

    @Override
    public int size() {
        return ((DCSet) databaseSet).getBlockSignsMap().size();
    }

    public Long getFullWeight() {
        return this.fullWeight;
    }

    //public int getStartedInForkHeight() {
    //    return this.startedInForkHeight;
    //}

    public void recalcWeightFull(DCSet dcSet) {

        long weightFull = 0l;
        Iterator<Integer> iterator = this.getIterator(DEFAULT_INDEX, true);
        while (iterator.hasNext()) {
            Integer key = iterator.next();
            Block.BlockHead item = this.get(key);
            weightFull += item.winValue;
        }

        fullWeight = weightFull;
        this.fullWeightVar.set(fullWeight);

    }

    public void putAndProcess(int height, Block.BlockHead item) {

        // get Win Value of block
        long weight = item.winValue;

        fullWeight += weight;

        if (this.fullWeightVar != null) {
            this.fullWeightVar.set(fullWeight);
        }

        // INSERT WITH NEW KEY
        put(height, item);

    }

    public Block.BlockHead last() {
        return this.get(this.size());
    }

    public void deleteAndProcess(Integer key) {

        if (this.contains(key)) {
            // sub old value from FULL
            Block.BlockHead value_old = this.get(key);
            fullWeight -= value_old.winValue;

            if (this.fullWeightVar != null) {
                this.fullWeightVar.set(fullWeight);
            }
            delete(key);
        }

    }

    @Override
    public void writeToParent() {
        super.writeToParent();
        ((BlocksHeadsMap) parent).fullWeightVar.set(this.fullWeight);
        ((BlocksHeadsMap) parent).fullWeight = this.fullWeight;
    }

}