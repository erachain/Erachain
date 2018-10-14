package datachain;

import com.google.common.primitives.UnsignedBytes;
import core.block.Block;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

// block.signature -> Height, Weight(Win Value)
public class BlockSignsMap extends DCMap<byte[], Integer> {
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    // for saving in DB
    //private Atomic.Long fullWeightVar;
    //private Long fullWeight = 0L;
    //private int startedInForkHeight = 0;

    public BlockSignsMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        //this.fullWeightVar = database.getAtomicLong("fullWeight");
        //this.fullWeight = this.fullWeightVar.getBySignature();
        //if (this.fullWeight == null)
        //	this.fullWeight = 0L;

        //startedInForkHeight = 0;
    }

    public BlockSignsMap(BlockSignsMap parent, DCSet dcSet) {
        super(parent, dcSet);
        //this.fullWeight = parent.getFullWeight();
        //startedInForkHeight
    }

    @Override
    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<byte[], Integer> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("height")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected Map<byte[], Integer> getMemoryMap() {
        return new TreeMap<byte[], Integer>(UnsignedBytes.lexicographicalComparator());
    }

    @Override
    protected Integer getDefaultValue() {
        //return new Tuple2<Integer, Long>(-1,-1L);
        return null;
    }

	/*
	private Long getFullWeight() {
		return this.fullWeight;
	}
	 */

	/*
	public void setFullWeight(long value) {

		fullWeight = value;
		if(this.fullWeightVar != null)
		{
			this.fullWeightVar.set(fullWeight);
		}
	}
	 */

	/*
	private int getStartedInForkHeight() {
		return this.startedInForkHeight;
	}
	 */


    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public Integer get(Block block) {
        return this.get(block.getSignature());
    }

    public Block getBlock(byte[] signature) {
        Integer key = this.get(signature);
        if (key != null && key > 0)
            return this.getDCSet().getBlockMap().get(key);

        return null;
    }

    public Integer getHeight(Block block) {
        if (this.contains(block.getSignature()))
            return this.get(block.getSignature());
        return -1;
    }

    public Integer getHeight(byte[] signature) {
        if (this.contains(signature)) {
            Integer o = this.get(signature);
            if (o != null)
                return this.get(signature);
        }
        return -1;
    }

	/*
	private int getWeight(Block block)
	{
		if (this.contains(block.getSignature()))
			return this.getBySignature(block.getSignature()).b;
		return 0;
	}
	 */

	/*
	private void recalcWeightFull(DCSet dcSet) {

		long weightFull = 0l;
		Iterator<byte[]> iterator = this.getIterator(0, true);
		while (iterator.hasNext()) {
			byte[] key = iterator.next();
			Tuple2<Integer, Integer> hw = this.getBySignature(key);
			weightFull += hw.b;
		}

		fullWeight = weightFull;
		this.fullWeightVar.set(fullWeight);

	}
	 */

	/*
	public boolean set(byte[] key, int height)//, int weight)
	{
		if (this.contains(key)) {
			// sub old value from FULL
			Tuple2<Integer, Integer> value_old = this.getBySignature(key);
			fullWeight -= value_old.b;
		}

		if (startedInForkHeight == 0 && this.parent != null) {
			startedInForkHeight = height;
		}

		fullWeight += 1; //weight;

		if(this.fullWeightVar != null)
		{
			this.fullWeightVar.set(fullWeight);
		}

		return super.set(key, new Tuple2<Integer, Integer>(height, 1)); //weight));
	}
	 */

	/*
	@Override
	public void delete(byte[] key)
	{
		if (this.contains(key)) {
			// sub old value from FULL
			Tuple2<Integer, Integer> value_old = this.getBySignature(key);
			fullWeight -= value_old.b;

			if(this.fullWeightVar != null)
			{
				this.fullWeightVar.set(fullWeight);
			}

			super.delete(key);
		}

	}
	 */

}