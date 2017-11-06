package datachain;
import java.util.Arrays;
// 30/03
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;

import org.mapdb.Atomic.Var;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Function2;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple2Comparator;
import org.mapdb.HTreeMap;

import com.google.common.primitives.UnsignedBytes;
//import com.sun.media.jfxmedia.logging.Logger;

import core.account.PublicKeyAccount;
import core.block.Block;
import database.DBMap;
import database.serializer.BlockSerializer;
import settings.Settings;
import utils.Converter;
import utils.ObserverMessage;
import utils.ReverseComparator;

public class BlockMap extends DCMap<byte[], Block> 
{
	public static final int HEIGHT_INDEX = 1;

	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	private Var<byte[]> lastBlockVar;
	private byte[] lastBlockSignature;
		
	private Var<Long> feePoolVar;
	private long feePool; // POOL for OVER_FREE FEE
	
	private Var<Boolean> processingVar;
	private Boolean processing;
	NavigableSet<Tuple2<Integer, byte[]>> heightIndex;
	BTreeMap<byte[], byte[]> childIndex;
	//private List<Block> lastBlocksForTarget;

	private BTreeMap<Tuple2<String, String>, byte[]> generatorMap;
	
	public BlockMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);

		if (databaseSet.isWithObserver()) {
			//this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_BLOCK_TYPE);
			if (databaseSet.isDynamicGUI()) {
				//this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_BLOCK_TYPE);
				//this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_BLOCK_TYPE);
			}
			//this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_BLOCK_TYPE);
		}

		//LAST BLOCK
		this.lastBlockVar = database.getAtomicVar("lastBlock");
		this.lastBlockSignature = this.lastBlockVar.get();

		//POOL FEE
		//this.feePoolVar = database.getAtomicVar("feePool");
		//this.feePool = this.feePoolVar.get();

		//PROCESSING
		this.processingVar = database.getAtomicVar("processingBlock");
		this.processing = this.processingVar.get();
	}

	public BlockMap(BlockMap parent, DCSet dcSet) 
	{
		super(parent, dcSet);
				
		this.lastBlockSignature = parent.getLastBlockSignature();
		this.feePool = parent.getFeePool();
		this.processing = false; ///parent.isProcessing();
		
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void createIndexes(DB database)
	{
		//HEIGHT INDEX
		Tuple2Comparator<Integer, byte[]> comparator = new Fun.Tuple2Comparator<Integer, byte[]>(Fun.COMPARATOR, UnsignedBytes.lexicographicalComparator());
		heightIndex = database.createTreeSet("blocks_index_height")
				.comparator(comparator)
				.makeOrGet();
		
		NavigableSet<Tuple2<Integer, byte[]>> descendingHeightIndex = database.createTreeSet("blocks_index_height_descending")
				.comparator(new ReverseComparator(comparator))
				.makeOrGet();
		
		createIndex(HEIGHT_INDEX, heightIndex, descendingHeightIndex, new Fun.Function2<Integer, byte[], Block>() {
		   	@Override
		    public Integer run(byte[] key, Block value) {
		   		return value.getHeight(getDCSet());
		    }
		});
		
		//HEIGHT INDEX
				Tuple2Comparator<Integer, byte[]> comparator1 = new Fun.Tuple2Comparator<Integer, byte[]>(Fun.COMPARATOR, UnsignedBytes.lexicographicalComparator());
				 NavigableSet<Object> heightIndex1 = database.createTreeSet("blocks_index_height1")
						.comparator(comparator)
						.makeOrGet();
				
				NavigableSet<Tuple2<Integer, byte[]>> descendingHeightIndex1 = database.createTreeSet("blocks_index_height_descending1")
						.comparator(new ReverseComparator(comparator))
						.makeOrGet();
				
				createIndex(2, heightIndex1, descendingHeightIndex1, new Fun.Function2<Integer, byte[], Block>() {
				   	@Override
				    public Integer run(byte[] key, Block value) {
				   		return value.getHeight(getDCSet());
				    }
				});
				
				
		
		generatorMap = database.createTreeMap("generators_index").makeOrGet();
		
		Bind.secondaryKey((BTreeMap)this.map, generatorMap, new Fun.Function2<Tuple2<String, String>, byte[], Block>() {
			@Override
			public Tuple2<String, String> run(byte[] b, Block block) {
				return new Tuple2<String, String>(block.getCreator().getAddress(), Converter.toHex(block.getSignature()));
			}
		});
		
	//index parrent block signature - child block.signature
		childIndex = database.createTreeMap("children")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
		
		Bind.secondaryKey((BTreeMap)this.map, childIndex, new Fun.Function2<byte[], byte[], Block>() {
			@Override
			public byte[] run(byte[] b, Block block) {
				return block.getReference();
				
			}
		});
 /* secondary value map. Key - byte[], value  Tuple2<Integer, Integer>
		HTreeMap<byte[], Tuple2<Integer,Integer>> blockSignsMap = database.createHashMap("block_signs_map_Value").makeOrGet();
 		Bind.secondaryValue((BTreeMap)this.map, blockSignsMap, new Fun.Function2<Tuple2<Integer,Integer>, byte[], Block>() {
			@Override
			public Tuple2<Integer, Integer> run(byte[] a, Block b) {
				// TODO Auto-generated method stub
				return new  Tuple2(b.getHeight(db), b.getSignature();
			}
 		});
	*/
		/*// multi key
		NavigableSet<Tuple2<Integer, byte[]>> Set1 = database.createTreeSet("Set1").makeOrGet();
		 Bind.secondaryKeys((BTreeMap)this.map, Set1, new Fun.Function2<Integer[], byte[], Block>() {
				@Override
				public Integer[] run(byte[] b, Block block) {
					return new Integer[]{1};
					
				}
			});
		*/
	/*
		NavigableSet<Tuple2< byte[], Integer>> Set2 = database.createTreeSet("Set2").makeOrGet();
		Bind.secondaryValues((BTreeMap)this.map, Set2, new Fun.Function2<Integer[],byte[],  Block>(){
			@Override
			public Integer[] run(byte[] b, Block block) {
				return new Integer[]{1};
				
			}
		});
		*/
		
		
	}
	
	
	
	
	
	
	
	public Block getChildBlock(Block parent)
	{
		if(this.childIndex.containsKey(parent.getSignature()))
		{
			byte[] key = childIndex.get(parent.getSignature());
			if (this.map.containsKey(key))	return this.map.get(key);
		}
		
		return null;
	}
	
	public byte[] getChildBlock(byte[] parent)
	{
		if(this.contains(parent))
		{
			return childIndex.get(parent);
		}
		
		return null;
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

	private void setLastBlockSignature(byte[] signature) 
	{
		
		this.lastBlockSignature = signature;
		if(this.lastBlockVar != null)
		{
			this.lastBlockVar.set(this.lastBlockSignature);
		}

	}
	
	public Block getLastBlock()
	{
		return this.get(this.getLastBlockSignature());
	}
	
	public byte[] getLastBlockSignature()
	{
		return this.lastBlockSignature;
	}

	private void setFeePool(Long feePool) 
	{
		
		this.feePool = feePool;
		if(this.feePoolVar != null)
		{
			this.feePoolVar.set(this.feePool);
		}

	}
		
	public Long getFeePool()
	{
		return this.feePool;
	}

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
			if (DCSet.getInstance().isStoped()) {
				return;
			}
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
			
		DCSet dcSet = getDCSet();
		// calc before insert record
		int win_value = block.calcWinValueTargeted(dcSet);
		
	//	LOGGER.error(" %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%   block ref:"+ new String(block.getReference())+ "   Block sign:" + new String(block.getSignature()));
	//	System.out.println("##############   "+ childIndex);
		//long start = System.currentTimeMillis();
		//LOGGER.error("&&&&&&&&&&&&&&&&&&&&&&&&&&& 1000: " + (System.currentTimeMillis() - start)*0.001);		
		// THEN all other record add to DB

		if (block.getVersion() == 0) {
			// GENESIS block
			dcSet.getBlockSignsMap().set(signature,
					new Tuple2<Integer, Integer>(1, core.BlockChain.GENESIS_WIN_VALUE));
	//		dcSet.getBlockHeightsMap().add(signature);
		} else {
			Block parent = this.get(block.getReference());
	//		byte[] ss = parent.getSignature();
	//		byte[] s1 = block.getReference();
	//		System.out.println("/n ####################sign:" + new String(ss));
	//		System.out.println("/n ####################refe:" + new String(s1));
			int height = parent.getHeight(dcSet) + 1;
			//dcSet.getBlockMap().getChildMap().set(parent, block);
			dcSet.getBlockSignsMap().set(signature,
					new Tuple2<Integer, Integer>(height, win_value));
			//dcSet.getBlockHeightsMap().set((long)height, signature);
	//		long heightInMap = dcSet.getBlockHeightsMap().add(signature);
			
			//
			// PROCESS FORGING DATA
			PublicKeyAccount creator = block.getCreator();
			creator.setForgingData(dcSet, height);
		}
		//LOGGER.error("&&&&&&&&&&&&&&&&&&&&&&&&&&& 1200: " + (System.currentTimeMillis() - start)*0.001);
		this.setLastBlockSignature(signature);
		//LOGGER.error("&&&&&&&&&&&&&&&&&&&&&&&&&&& 1500: " + (System.currentTimeMillis() - start)*0.001);
		// TODO feePool
		//this.setFeePool(_feePool);
		boolean sss = super.set(signature, block); // мах time!!!!!!
		//LOGGER.error("&&&&&&&&&&&&&&&&&&&&&&&&&&& 1600: " + (System.currentTimeMillis() - start)*0.001);
		return sss;
		
	}

	// TODO make CHAIN deletes - only for LAST block!
	public void delete(Block block)
	{
		DCSet dcSet = getDCSet();

		if (!Arrays.equals(this.getLastBlockSignature(), block.getSignature())) {
			Long rr = null;
			rr +=1;
		}
		
		dcSet.getBlockSignsMap().delete(block.getSignature());

		byte[] parentSign = block.getReference();
		Block parent = this.get(parentSign);

		
		this.setLastBlockSignature(parentSign);
		
	//	dcSet.getChildMap().delete(parent.getSignature());
	
		// ORPHAN FORGING DATA
		int height = parent.getHeight(dcSet) + 1;
		if (height > 1) {
	//		dcSet.getBlockHeightsMap().delete(height);

			PublicKeyAccount creator = block.getCreator();
			// INITIAL forging DATA no need remove!
			creator.delForgingData(dcSet, height);
			int hh = creator.getLastForgingData(dcSet);
		}

		// use SUPER.class only!
		super.delete(block.getSignature());
	}

	public void delete(byte[] signature)
	{
		Block block = this.get(signature);
		this.delete(block);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<byte[]> getGeneratorBlocks(String address)
	{
		Collection<byte[]> blocks = ((BTreeMap)(this.generatorMap))
				.subMap(Fun.t2(address, null), Fun.t2(address,Fun.HI())).values();
		
		return blocks;
	}
	
	public int getHeight(byte[] signature){
		Iterator<Tuple2<Integer, byte[]>> it = heightIndex.iterator();
		while(it.hasNext()){
			 Tuple2<Integer, byte[]> ss = it.next();
			if (ss.b.equals(signature)){
				return ss.a;
			}
		}
		return -1;
	}
	public byte[] getSignByHeight(int  height){
		Iterator<Tuple2<Integer, byte[]>> it = heightIndex.iterator();
		while(it.hasNext()){
			 Tuple2<Integer, byte[]> ss = it.next();
			if (ss.a==height){
				return ss.b;
			}
		}
		return null;
	}
	public Block get(int height){
		return this.get(getSignByHeight(height));
		
		
	}
	public void notifyResetChain() {
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_RESET_BLOCK_TYPE, null));
	}
	public void notifyProcessChain(Block block) {
		LOGGER.error("&&&&&&&&&&&&& NOTEFY CHAIN_ADD_BLOCK_TYPE");
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_ADD_BLOCK_TYPE, block));
	}
	public void notifyOrphanChain(Block block) {
		LOGGER.error("&&&&&&&&&&&&& NOTEFY CHAIN_REMOVE_BLOCK_TYPE");
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE, block));
	}
	public void notifyListChain(List<Block> blocks) {
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_LIST_BLOCK_TYPE, blocks));
	}
	
}
