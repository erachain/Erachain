package database;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Atomic.Var;
import org.mapdb.Fun.Tuple2;

import com.google.common.primitives.UnsignedBytes;

import controller.Controller;
import core.block.Block;

// block.signature -> Height, (int)Weight
public class BlockSignsMap extends DBMap<byte[], Tuple2<Integer, Integer>> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
		
	// for saving in DB
	private Var<Long> fullWeightVar;
	private Long fullWeight = 0l;
	private int startedInForkHeight = 0;
	
	public BlockSignsMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.fullWeightVar = database.getAtomicVar("fullWeight");
		this.fullWeight = this.fullWeightVar.get();
		if (this.fullWeight == null)
			this.fullWeight = 0L;
		
		//startedInForkHeight = 0;
	}

	public BlockSignsMap(BlockSignsMap parent) 
	{
		super(parent, null);
		fullWeight = parent.getFullWeight();
		//startedInForkHeight
	}

	protected void createIndexes(DB database){}

	@Override
	protected Map<byte[], Tuple2<Integer, Integer>> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("height")
			.keySerializer(BTreeKeySerializer.BASIC)
			.comparator(UnsignedBytes.lexicographicalComparator())
			.makeOrGet();
	}

	@Override
	protected Map<byte[], Tuple2<Integer, Integer>> getMemoryMap() 
	{
		return new TreeMap<byte[], Tuple2<Integer, Integer>>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected Tuple2<Integer, Integer> getDefaultValue() 
	{
		return new Tuple2<Integer, Integer>(-1,-1);
	}
	
	public Long getFullWeight() {
		return this.fullWeight;
	}
	public void setFullWeight(long value) {
		
		fullWeight = value;
		if(this.fullWeightVar != null)
		{
			this.fullWeightVar.set(fullWeight);
		}
	}
	
	public int getStartedInForkHeight() {
		return this.startedInForkHeight;
	}
	
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public Tuple2<Integer, Integer> get(Block block)
	{
		return this.get(block.getSignature());
	}
	public Integer getHeight(Block block)
	{
		if (this.contains(block.getSignature()))
			return this.get(block.getSignature()).a;
		return -1;
	}
	public Integer getHeight(byte[] signature)
	{
		if (this.contains(signature)) {
			Tuple2<Integer, Integer> o = this.get(signature);
			if (o != null)
				return this.get(signature).a;
		}
		return -1;
	}
	public Integer getWeight(Block block)
	{
		if (this.contains(block.getSignature()))
			return this.get(block.getSignature()).b;
		return 0;
	}
	
	public boolean set(byte[] key, Tuple2<Integer, Integer> value)
	{
		if (this.contains(key)) {
			// sub old value from FULL
			Tuple2<Integer, Integer> value_old = this.get(key);
			fullWeight -= value_old.b;
		}
		
		if (startedInForkHeight == 0 && this.parent != null) {
			startedInForkHeight = value.a;
		}
		
		fullWeight += value.b;
		
		if(this.fullWeightVar != null)
		{
			this.fullWeightVar.set(fullWeight);
		}

		
		return super.set(key, value);
	}

	public void delete(byte[] key)
	{
		if (this.contains(key)) {
			// sub old value from FULL
			Tuple2<Integer, Integer> value_old = this.get(key);
			fullWeight -= value_old.b;
		}
		
		if(this.fullWeightVar != null)
		{
			this.fullWeightVar.set(fullWeight);
		}
		
		super.delete(key);
	}
}