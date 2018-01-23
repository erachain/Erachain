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

import org.mapdb.Atomic;
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
import org.apache.log4j.Logger;

import core.account.PublicKeyAccount;
import core.block.Block;
import core.transaction.Transaction;
import database.serializer.BlockSerializer;
import settings.Settings;
import utils.Converter;
import utils.ObserverMessage;

public class BlockMap extends DCMap<Integer, Block> {
	
	public static final int HEIGHT_INDEX = 1; // for GUI

	private static final Logger LOGGER = Logger.getLogger(BlockMap.class);

	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	// private Var<byte[]> lastBlockVar;
	private byte[] lastBlockSignature;

	private Var<Long> feePoolVar;
	private long feePool; // POOL for OVER_FREE FEE

	private Atomic.Boolean processingVar;
	private Boolean processing;
	// NavigableSet<Tuple2<Integer, byte[]>> heightIndex;
	// BTreeMap<byte[], byte[]> childIndex;
	// private List<Block> lastBlocksForTarget;

	private BTreeMap<Tuple2<String, String>, Integer> generatorMap;

	public BlockMap(DCSet databaseSet, DB database) {
		super(databaseSet, database);

		if (databaseSet.isWithObserver()) {
			// this.observableData.put(DBMap.NOTIFY_RESET,
			// ObserverMessage.RESET_BLOCK_TYPE);
			if (databaseSet.isDynamicGUI()) {
				// this.observableData.put(DBMap.NOTIFY_ADD,
				// ObserverMessage.ADD_BLOCK_TYPE);
				// this.observableData.put(DBMap.NOTIFY_REMOVE,
				// ObserverMessage.REMOVE_BLOCK_TYPE);
			}
			// this.observableData.put(DBMap.NOTIFY_LIST,
			// ObserverMessage.LIST_BLOCK_TYPE);
		}

		// LAST BLOCK
		if (database.getCatalog().get(("lastBlock" + ".type")) == null) {
			database.createAtomicVar("lastBlock", new byte[0], null);
		}
		// this.lastBlockVar = database.getAtomicVar("lastBlock");
		// this.lastBlockSignature = this.lastBlockVar.get();

		// POOL FEE
		// this.feePoolVar = database.getAtomicVar("feePool");
		// this.feePool = this.feePoolVar.get();

		// PROCESSING
		this.processingVar = database.getAtomicBoolean("processingBlock");
		this.processing = this.processingVar.get();
	}

	public BlockMap(BlockMap parent, DCSet dcSet) {
		super(parent, dcSet);

		this.lastBlockSignature = parent.getLastBlockSignature();
		this.feePool = parent.getFeePool();
		this.processing = false; /// parent.isProcessing();

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createIndexes(DB database) {
		generatorMap = database.createTreeMap("generators_index").makeOrGet();
		
		Bind.secondaryKey((BTreeMap)this.map, generatorMap, new Fun.Function2<Tuple2<String, String>, Integer, Block>() {
			@Override
			public Tuple2<String, String> run(Integer b, Block block) {
				return new Tuple2<String, String>(block.getCreator().getAddress(), Converter.toHex(block.getSignature()));
			}
		});

		/*
		 * secondary value map. Key - byte[], value Tuple2<Integer, Integer>
		 * HTreeMap<byte[], Tuple2<Integer,Integer>> blockSignsMap =
		 * database.createHashMap("block_signs_map_Value").makeOrGet();
		 * Bind.secondaryValue((BTreeMap)this.map, blockSignsMap, new
		 * Fun.Function2<Tuple2<Integer,Integer>, byte[], Block>() {
		 * 
		 * @Override public Tuple2<Integer, Integer> run(byte[] a, Block b) { //
		 * TODO Auto-generated method stub return new Tuple2(b.getHeight(db),
		 * b.getSignature(); } });
		 */
		/*
		 * // multi key NavigableSet<Tuple2<Integer, byte[]>> Set1 =
		 * database.createTreeSet("Set1").makeOrGet();
		 * Bind.secondaryKeys((BTreeMap)this.map, Set1, new
		 * Fun.Function2<Integer[], byte[], Block>() {
		 * 
		 * @Override public Integer[] run(byte[] b, Block block) { return new
		 * Integer[]{1};
		 * 
		 * } });
		 */
		/*
		 * NavigableSet<Tuple2< byte[], Integer>> Set2 =
		 * database.createTreeSet("Set2").makeOrGet();
		 * Bind.secondaryValues((BTreeMap)this.map, Set2, new
		 * Fun.Function2<Integer[],byte[], Block>(){
		 * 
		 * @Override public Integer[] run(byte[] b, Block block) { return new
		 * Integer[]{1};
		 * 
		 * } });
		 */

	}

	@Override
	protected Map<Integer, Block> getMap(DB database) {
		// OPEN MAP
		return database.createTreeMap("blocks").keySerializer(BTreeKeySerializer.BASIC)
				// .comparator(UnsignedBytes.lexicographicalComparator())
				.valueSerializer(new BlockSerializer()).valuesOutsideNodesEnable().counterEnable().makeOrGet();
	}

	@Override
	protected Map<Integer, Block> getMemoryMap() {
		// return new TreeMap<byte[],
		// Block>(UnsignedBytes.lexicographicalComparator());
		return new TreeMap<Integer, Block>();
	}

	@Override
	protected Block getDefaultValue() {
		return null;
	}
	// public Var<byte[]> getLastBlockVar() {
	// return this.lastBlockVar;
	// }

	@Override
	protected Map<Integer, Integer> getObservableData() {
		return this.observableData;
	}

	private void setLastBlockSignature(byte[] signature) {

		this.lastBlockSignature = signature;
		// if(this.lastBlockVar != null)
		// {
		// this.lastBlockVar.set(this.lastBlockSignature);
		// }

	}

	public Block last() {
		// return this.get(this.getLastBlockSignature());
		return this.get(this.size());
	}

	public byte[] getLastBlockSignature() {
		if (this.lastBlockSignature == null) {
			this.lastBlockSignature = getDCSet().getBlockHeightsMap().get(this.size());
		}
		return this.lastBlockSignature;
	}

	private void setFeePool(Long feePool) {

		this.feePool = feePool;
		if (this.feePoolVar != null) {
			this.feePoolVar.set(this.feePool);
		}

	}

	public Long getFeePool() {
		return this.feePool;
	}

	public boolean isProcessing() {
		if (this.processing != null) {
			return this.processing.booleanValue();
		}

		return false;
	}

	public void setProcessing(boolean processing) {
		if (this.processingVar != null) {
			if (DCSet.isStoped()) {
				return;
			}
			this.processingVar.set(processing);
		}

		this.processing = processing;
	}

	public boolean add(Block block) {
		DCSet dcSet = getDCSet();

		byte[] signature = block.getSignature();
		// calc before insert record
		int win_value = block.calcWinValueTargeted(dcSet);

		int height = this.size() + 1;

		if (block.getVersion() == 0) {
			// GENESIS block
			dcSet.getBlockSignsMap().set(signature, 1, core.BlockChain.GENESIS_WIN_VALUE);
			dcSet.getBlockHeightsMap().add(signature);
		} else {
			dcSet.getBlockSignsMap().set(signature, height, win_value);
			dcSet.getBlockHeightsMap().set(height, signature);

			PublicKeyAccount creator = block.getCreator();
			dcSet.getBlockCreatorMap().set(height, creator.getPublicKey());
			// PROCESS FORGING DATA
			creator.setForgingData(dcSet, height);

		}
		// LOGGER.error("&&&&&&&&&&&&&&&&&&&&&&&&&&& 1200: " +
		// (System.currentTimeMillis() - start)*0.001);
		this.setLastBlockSignature(signature);
		// LOGGER.error("&&&&&&&&&&&&&&&&&&&&&&&&&&& 1500: " +
		// (System.currentTimeMillis() - start)*0.001);

		// TODO feePool
		// this.setFeePool(_feePool);
		boolean sss = super.set(height, block);
		// LOGGER.error("&&&&&&&&&&&&&&&&&&&&&&&&&&& 1600: " +
		// (System.currentTimeMillis() - start)*0.001);
		return sss;

	}

	// TODO make CHAIN deletes - only for LAST block!
	public void remove() {
		DCSet dcSet = getDCSet();

		int height = this.size();
		// Block block = this.get(height);
		// this.setLastBlockSignature(block.getReference());

		// ORPHAN FORGING DATA
		if (height > 1) {
			dcSet.getBlockHeightsMap().delete(height);

			byte[] creatorByte = dcSet.getBlockCreatorMap().get(height);
			PublicKeyAccount creator = new PublicKeyAccount(creatorByte);
			// INITIAL forging DATA no need remove!
			creator.delForgingData(dcSet, height);

		}

		dcSet.getBlockSignsMap().delete(this.getLastBlockSignature());

		// use SUPER.class only!
		super.delete(height);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<Integer> getGeneratorBlocks(String address)
	{
		Collection<Integer> headers = ((BTreeMap)(this.generatorMap))
				.subMap(Fun.t2(address, null), Fun.t2(address,Fun.HI())).values();
		
		return headers;
	}


	public void notifyResetChain() {
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_RESET_BLOCK_TYPE, null));
	}

	public void notifyProcessChain(Block block) {
		LOGGER.debug("++++++ NOTEFY CHAIN_ADD_BLOCK_TYPE");
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_ADD_BLOCK_TYPE, block));
		LOGGER.debug("++++++ NOTEFY CHAIN_ADD_BLOCK_TYPE END");
	}

	public void notifyOrphanChain(Block block) {
		LOGGER.debug("===== NOTEFY CHAIN_REMOVE_BLOCK_TYPE");
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE, block));
		LOGGER.debug("===== NOTEFY CHAIN_REMOVE_BLOCK_TYPE END");
	}

	public void notifyListChain(List<Block> blocks) {
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_LIST_BLOCK_TYPE, blocks));
	}

}
