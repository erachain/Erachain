package org.erachain.datachain;


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.erachain.core.block.Block;
import org.erachain.database.serializer.BlockHeadSerializer;
import org.mapdb.Atomic;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *  Block Height -> Block.BlockHead:
 *  + FACE - version, creator, signature, transactionsCount, transactionsHash
 *  + parentSignature
 *  + Forging Data - Forging Value, Win Value, Target Value
 *
 */
public class BlocksHeadsMap extends DCMap<Integer, Block.BlockHead> {

    static final String NAME = "blocks_heads";
    static Logger LOGGER = LoggerFactory.getLogger(BlocksHeadsMap.class.getName());
    // for saving in DB
    private Atomic.Long fullWeightVar;
    private Long fullWeight = 0L;
    private int startedInForkHeight = 0;


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
    protected Map<Integer, Block.BlockHead> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap(NAME)
                .keySerializer(BTreeKeySerializer.BASIC)
                .valueSerializer(new BlockHeadSerializer())
                .counterEnable() // used in datachain.DCSet.DCSet(org.mapdb.DB, boolean, boolean, boolean)
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
    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<Integer, Block.BlockHead> getMemoryMap() {
        return new HashMap<Integer, Block.BlockHead>();
    }

    @Override
    protected Block.BlockHead getDefaultValue() {
        return null;
    }

    public Long getFullWeight() {
        return this.fullWeight;
    }

    public int getStartedInForkHeight() {
        return this.startedInForkHeight;
    }

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

    public boolean set(int height, Block.BlockHead item) {

        //int key = this.size() + 1;
        if (height == 86549 || item.heightBlock <= 0) {
            int ttt = 1;
        }

        // get Win Value of block
        long weight = item.winValue;

        if (startedInForkHeight == 0 && this.parent != null) {
            startedInForkHeight = height;
        }

        fullWeight += weight;

        if (this.fullWeightVar != null) {
            this.fullWeightVar.set(fullWeight);
        }

        // INSERT WITH NEW KEY
        return super.set(height, item);

    }

    public boolean set(Block.BlockHead item) {
        return this.set(item.heightBlock, item);
    }

    /*
    public int add(Block.BlockHead item) {

        int key = this.size() + 1;

        // INSERT WITH NEW KEY
        this.set(key, item);

        // RETURN KEY
        return key;
    }
    */

    public Block.BlockHead last() {
        return this.get(this.size());
    }

    public Block.BlockHead remove() {

        int key = this.size();
        if (this.contains(key)) {
            // sub old value from FULL
            Block.BlockHead value_old = this.get(key);
            fullWeight -= value_old.winValue;

            if (this.fullWeightVar != null) {
                this.fullWeightVar.set(fullWeight);
            }
            return super.delete(key);
        }

        return null;
    }


}