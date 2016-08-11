package database;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import com.google.common.primitives.UnsignedBytes;

import core.block.Block;

// block.signature -> Height, (int)Weight
public class HeightMap extends DBMap<byte[], Tuple2<Integer, Integer>> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	private Map<Integer,byte[]> heightIndex;
	
	private long fullWeight;
	private int startedInForkHeight = 0;
	
	public HeightMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		fullWeight = 0;
		//startedInForkHeight = 0;
	}

	public HeightMap(HeightMap parent) 
	{
		super(parent);
		fullWeight = parent.getFullWeight();
		//startedInForkHeight
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createIndexes(DB database){
		this.heightIndex = database.createTreeMap("block_height_index").makeOrGet();
		
		Bind.secondaryKey((BTreeMap)this.map, heightIndex, new Fun.Function2<Integer, byte[], Tuple2<Integer, Integer>>() {
			@Override
			public Integer run(byte[] arg0, Tuple2<Integer, Integer> arg1) {
				// TODO Auto-generated method stub
				return arg1.a;
			}
		
		});
	}

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
		return null;
	}
	
	public long getFullWeight() {
		return this.fullWeight;
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
		return this.get(block.getSignature()).a;
	}
	public Integer getHeight(byte[] signature)
	{
		return this.get(signature).a;
	}
	public Integer getWeight(Block block)
	{
		return this.get(block.getSignature()).b;
	}
	
	public byte[] getBlockByHeight(int height)
	{
		return heightIndex.get(height);
	}
	
	public void set(Block block, int height, int weight)
	{
		this.set(block.getSignature(), new Tuple2<Integer, Integer>(height, weight));
	}
	
	public boolean set(byte[] key, Tuple2<Integer, Integer> value)
	{
		if (this.contains(key)) {
			// sub old value from FULL
			Tuple2<Integer, Integer> value_old = this.get(key);
			fullWeight -= value_old.b;
		}
		
		if (startedInForkHeight == 0) {
			startedInForkHeight = value.a;
		}
		
		fullWeight += value.b;
		
		return super.set(key, value);
	}
}