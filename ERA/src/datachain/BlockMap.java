package datachain;

// 30/03

import core.BlockGenerator;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.crypto.Base58;
import database.serializer.BlockSerializer;
import org.apache.log4j.Logger;
import org.mapdb.*;
import org.mapdb.Atomic.Var;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import utils.Converter;
import utils.ObserverMessage;

import java.util.*;

//import com.sun.media.jfxmedia.logging.Logger;

/**
 * Хранит блоки полностью - с транзакциями
 *
 * ключ: номер блока (высота, height)<br>
 * занчение: Блок<br>
 *
 * Есть вторичный индекс, для отчетов (blockexplorer) - generatorMap
 * TODO - убрать длинный индек и вставить INT
 *
 * @return
 */
public class BlockMap extends DCMap<Integer, Block> {

    static Logger LOGGER = Logger.getLogger(BlockMap.class.getName());

    public static final int HEIGHT_INDEX = 1; // for GUI

    static boolean init1 = true;

    //	protected Atomic.Integer atomicKey;
    //	protected int key;
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
    // private Var<byte[]> lastBlockVar;
    private byte[] lastBlockSignature;
    private Var<Long> feePoolVar;
    private long feePool; // POOL for OVER_FREE FEE
    private Atomic.Boolean processingVar;
    // NavigableSet<Tuple2<Integer, byte[]>> heightIndex;
    // BTreeMap<byte[], byte[]> childIndex;
    // private List<Block> lastBlocksForTarget;
    private Boolean processing;
    private BTreeMap<Tuple2<String, String>, Integer> generatorMap;

    public BlockMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        //this.atomicKey = database.getAtomicInteger("block_map" + "_key");
        //this.key = this.atomicKey.get();

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
        //if (database.getCatalog().get(("lastBlock" + ".type")) == null) {
        //	database.createAtomicVar("lastBlock", new byte[0], null);
        //}
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

        //this.key = parent.size();

        this.lastBlockSignature = parent.getLastBlockSignature();
        this.feePool = parent.getFeePool();
        this.processing = false; /// parent.isProcessing();

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes(DB database) {
        generatorMap = database.createTreeMap("generators_index").makeOrGet();

        // TODO - убрать длинный индек и вставить INT
        Bind.secondaryKey((BTreeMap) this.map, generatorMap, new Fun.Function2<Tuple2<String, String>, Integer, Block>() {
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
        return database.createTreeMap("blocks")
                .keySerializer(BTreeKeySerializer.BASIC)
                // .comparator(UnsignedBytes.lexicographicalComparator())
                .valueSerializer(new BlockSerializer())
                .valuesOutsideNodesEnable()
                .counterEnable() // - auto increment atomicKey
                .makeOrGet();
    }

    @Override
    protected Map<Integer, Block> getMemoryMap() {
        // return new TreeMap<byte[],
        // Block>(UnsignedBytes.lexicographicalComparator());
        return new TreeMap<Integer, Block>();
    }
    // public Var<byte[]> getLastBlockVar() {
    // return this.lastBlockVar;
    // }

    @Override
    protected Block getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public Block last() {
        // return this.get(this.getLastBlockSignature());
        return this.get(this.size());
    }

    public byte[] getLastBlockSignature() {
        if (this.lastBlockSignature == null) {
            this.lastBlockSignature = getDCSet().getBlocksHeadsMap().get(this.size()).signature;
        }
        return this.lastBlockSignature;
    }

    private void setLastBlockSignature(byte[] signature) {

        this.lastBlockSignature = signature;
        // if(this.lastBlockVar != null)
        // {
        // this.lastBlockVar.set(this.lastBlockSignature);
        // }

    }

    public Long getFeePool() {
        return this.feePool;
    }

    private void setFeePool(Long feePool) {

        this.feePool = feePool;
        if (this.feePoolVar != null) {
            this.feePoolVar.set(this.feePool);
        }

    }

	/*
	@Override
	public int size() {
		// size from Map as .size() - .deleted() + parent.size() for numbered key is WRONG
		// if same key in .deleted() and in parent.map - поправил с помощью shiftSize
		// so use this KEY
		return this.key;
	}
	 */

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

    public Block getWithMind(int height) {

        Block block = this.get(height);
        if (block == null)
            return null;

        return block;

    }

    public Block get(Integer height) {

        Block block = super.get(height);
        if (block != null) {
            //block.setHeight(height);
            block.loadHeadMind(this.getDCSet());
        }
        return block;

    }

    public boolean add(Block block) {
        DCSet dcSet = getDCSet();

		/*
		if (init1) {
			init1 = false;
			Iterator<Integer> iterator = this.getIterator(0, true);
			while (iterator.hasNext()) {
				Integer key = iterator.next();
				Block itemBlock = this.get(key);
				byte[] pkb = itemBlock.getCreator().getPublicKey();
				PublicKeyAccount pk = new PublicKeyAccount(pkb);
				if (((Account)pk).equals("7DedW8f87pSDiRnDArq381DNn1FsTBa68Y")) {
					LOGGER.error(key + " - 7DedW8f87pSDiRnDArq381DNn1FsTBa68Y : " + Base58.encode(pkb));
				}

			}
		}
		 */

        byte[] signature = block.getSignature();
        if (dcSet.getBlockSignsMap().contains(signature)) {
            LOGGER.error("already EXIST : " + this.size()
                    + " SIGN: " + Base58.encode(signature));
            return true;
        }

        //int height = this.size() + 1;
        int height = block.getHeight();

        if (block.getVersion() == 0) {
            // GENESIS block
        } else {

            // PROCESS FORGING DATA
        }

        PublicKeyAccount creator = block.getCreator();
        creator.setForgingData(dcSet, height, block.getForgingValue());

        // LOGGER.error("&&&&&&&&&&&&&&&&&&&&&&&&&&& 1200: " +
        // (System.currentTimeMillis() - start)*0.001);

        dcSet.getBlockSignsMap().set(signature, height);
        if (height < 1) {
            Long error = null;
            ++error;
        }

        dcSet.getBlocksHeadsMap().set(block.blockHead);
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

	/*
	public boolean set(int height, Block block) {
		return false;
	}
	 */

    // TODO make CHAIN deletes - only for LAST block!
    public Block remove(byte[] signature, byte[] reference) {
        DCSet dcSet = getDCSet();

        int height = this.size();

        this.setLastBlockSignature(reference);
        dcSet.getBlockSignsMap().delete(signature);

        // ORPHAN FORGING DATA
        if (height > 1) {

            Block.BlockHead head = dcSet.getBlocksHeadsMap().remove();

            // INITIAL forging DATA no need remove!
            head.creator.delForgingData(dcSet, height);

        }

        // use SUPER.class only!
        return super.delete(height);

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Collection<Integer> getGeneratorBlocks(String address) {
        Collection<Integer> headers = ((BTreeMap) (this.generatorMap))
                .subMap(Fun.t2(address, null), Fun.t2(address, Fun.HI())).values();

        return headers;
    }


    public void notifyResetChain() {
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_RESET_BLOCK_TYPE, null));
    }

    public void notifyProcessChain(Block block) {
        LOGGER.debug("++++++ NOTEFY CHAIN_ADD_BLOCK_TYPE");
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_ADD_BLOCK_TYPE, block.blockHead));
        LOGGER.debug("++++++ NOTEFY CHAIN_ADD_BLOCK_TYPE END");
    }

    public void notifyOrphanChain(Block block) {
        LOGGER.debug("===== NOTEFY CHAIN_REMOVE_BLOCK_TYPE");
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE, block.blockHead));
        LOGGER.debug("===== NOTEFY CHAIN_REMOVE_BLOCK_TYPE END");
    }

}
