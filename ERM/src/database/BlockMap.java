package database;
// 30/03
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;

import org.mapdb.Atomic.Var;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple2Comparator;

import com.google.common.primitives.UnsignedBytes;

import core.block.Block;
import database.serializer.BlockSerializer;
import settings.Settings;
import utils.Converter;
import utils.ObserverMessage;
import utils.ReverseComparator;

public class BlockMap extends DBMap<byte[], Block> 
{
	public static final int HEIGHT_INDEX = 1;
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	private Var<byte[]> lastBlockVar;
	private byte[] lastBlockSignature;
	
	private Var<Boolean> processingVar;
	private Boolean processing;
	
	private BTreeMap<Tuple2<String, String>, byte[]> generatorMap;
	
	public BlockMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_BLOCK_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_BLOCK_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_BLOCK_TYPE);
		
		//LAST BLOCK
		this.lastBlockVar = database.getAtomicVar("lastBlock");
		this.lastBlockSignature = this.lastBlockVar.get();
		
		//PROCESSING
		this.processingVar = database.getAtomicVar("processingBlock");
		this.processing = this.processingVar.get();
	}

	public BlockMap(BlockMap parent) 
	{
		super(parent);
		
		//this.lastBlockVar = parent.getLastBlockVar();

		this.lastBlockSignature = parent.getLastBlockSignature();
		this.processing = parent.isProcessing();
		
		//this.lastTrueBlockHeight = parent.getLastTrueBlockHeight();
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void createIndexes(DB database)
	{
		//HEIGHT INDEX
		Tuple2Comparator<Integer, byte[]> comparator = new Fun.Tuple2Comparator<Integer, byte[]>(Fun.COMPARATOR, UnsignedBytes.lexicographicalComparator());
		NavigableSet<Tuple2<Integer, byte[]>> heightIndex = database.createTreeSet("blocks_index_height")
				.comparator(comparator)
				.makeOrGet();
		
		NavigableSet<Tuple2<Integer, byte[]>> descendingHeightIndex = database.createTreeSet("blocks_index_height_descending")
				.comparator(new ReverseComparator(comparator))
				.makeOrGet();
		
		createIndex(HEIGHT_INDEX, heightIndex, descendingHeightIndex, new Fun.Function2<Integer, byte[], Block>() {
		   	@Override
		    public Integer run(byte[] key, Block value) {
		   		return value.getHeight((DBSet)BlockMap.this.databaseSet);
		    }
		});
		
		generatorMap = database.createTreeMap("generators_index").makeOrGet();
		
		Bind.secondaryKey((BTreeMap)this.map, generatorMap, new Fun.Function2<Tuple2<String, String>, byte[], Block>() {
			@Override
			public Tuple2<String, String> run(byte[] b, Block block) {
				return new Tuple2<String, String>(block.getCreator().getAddress(), Converter.toHex(block.getSignature()));
			}
		});
	}

	@Override
	protected Map<byte[], Block> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("blocks")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.valueSerializer(new BlockSerializer())
				.valuesOutsideNodesEnable()
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<byte[], Block> getMemoryMap() 
	{
		return new TreeMap<byte[], Block>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected Block getDefaultValue() 
	{
		return null;
	}
	//public Var<byte[]> getLastBlockVar() {
	//	return this.lastBlockVar;
	//}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public void setLastBlock(Block block) 
	{
		
		this.lastBlockSignature = block.getSignature();
		if(this.lastBlockVar != null)
		{
			this.lastBlockVar.set(this.lastBlockSignature);
		}
		
		return;
		
		/*
		// SET TRUE LAST BLOCK
		//byte[] b = dbSet.getHeightMap().getBlockByHeight(block.getHeight(dbSet) - Settings.CONFIRMS_TRUE);
		//Block trueBlock = dbSet.getBlockMap().get(b);
		DBSet dbSet = (DBSet)this.databaseSet;
		// TAKE getHeight from PARENT, !child getHeight = null
		Block parent = block.getParent(dbSet);
		if (parent == null)
			return;

		int th = parent.getHeight(dbSet) - Settings.CONFIRMS_TRUE;

		if (this.lastTrueBlockHeight < th)
			this.lastTrueBlockHeight = th;

		if(this.lastTrueBlockHeightVar != null)
		{
			this.lastBlockVar.set(block.getSignature());
		}
		*/
		
	}
	
	public Block getLastBlock()
	{
		return this.get(this.getLastBlockSignature());
	}
	
	public byte[] getLastBlockSignature()
	{
		return this.lastBlockSignature;
	}
	
	/*
	// checkpoint
	public int getLastTrueBlockHeight()
	{
		return this.lastTrueBlockHeight;
	}
	*/
	
	public boolean isProcessing() 
	{
		if(this.processing != null)
		{
			return this.processing.booleanValue();
		}
		
		return false;
	}
	
	public void setProcessing(boolean processing)
	{
		if(this.processingVar != null)
		{
			this.processingVar.set(processing);
		}
		
		this.processing = processing;
	}
		
	public void set(Block block)
	{
		this.set(block.getSignature(), block);
	}
	public boolean set(byte[] signature, Block block)
	{
		
		DBSet dbSet = (DBSet)this.databaseSet;
		
		Block parent = block.getParent(dbSet);
		if (parent != null) {
			dbSet.getChildMap().set(parent, block);
			dbSet.getHeightMap().set(signature,
					new Tuple2<Integer, Integer>(block.getHeight(dbSet), block.getWinValue(dbSet)));
		} else {
			dbSet.getHeightMap().set(signature,
					new Tuple2<Integer, Integer>(1, 0));
			
		}

		
		return super.set(signature, block);
	}

	public void delete(Block block)
	{
		this.delete(block.getSignature());
	}
	public void delete(byte[] signature)
	{
		DBSet dbSet = (DBSet)this.databaseSet;
		
		dbSet.getHeightMap().delete(signature);
		dbSet.getChildMap().delete(signature);

		super.delete(signature);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<byte[]> getGeneratorBlocks(String address)
	{
		Collection<byte[]> blocks = ((BTreeMap)(this.generatorMap))
				.subMap(Fun.t2(address, null), Fun.t2(address,Fun.HI())).values();
		
		return blocks;
	}
	
}
