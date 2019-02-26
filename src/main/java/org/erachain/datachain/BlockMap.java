package org.erachain.datachain;

// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.database.serializer.BlockSerializer;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Atomic;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

;

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

    static Logger LOGGER = LoggerFactory.getLogger(BlockMap.class.getName());

    public static final int HEIGHT_INDEX = 1; // for GUI

    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
    private byte[] lastBlockSignature;
    private Atomic.Boolean processingVar;
    private Boolean processing;
    private BTreeMap<Tuple2<String, String>, Integer> generatorMap;

    public BlockMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);


        if (databaseSet.isWithObserver()) {
            if (databaseSet.isDynamicGUI()) {
            }
        }


        // PROCESSING
        this.processingVar = database.getAtomicBoolean("processingBlock");
        this.processing = this.processingVar.get();
    }

    public BlockMap(BlockMap parent, DCSet dcSet) {
        super(parent, dcSet);

        this.lastBlockSignature = parent.getLastBlockSignature();
        this.processing = false; /// parent.isProcessing();

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes(DB database) {


        generatorMap = database.createTreeMap("generators_index").makeOrGet();

        /*
        Bind.secondaryKey((BTreeMap) this.map, generatorMap, new Fun.Function2<Tuple2<String, String>, Integer, Block>() {
            @Override
            public Tuple2<String, String> run(Integer b, Block block) {
                return new Tuple2<String, String>(block.getCreator().getAddress(), Converter.toHex(block.getSignature()));
            }
        });
        */

        /*
         * secondary value map. Key - byte[], value Tuple2<Integer, Integer>
         * HTreeMap<byte[], Tuple2<Integer,Integer>> blockSignsMap =
         * database.createHashMap("block_signs_map_Value").makeOrGet();
         * Bind.secondaryValue((BTreeMap)this.map, blockSignsMap, new
         * Fun.Function2<Tuple2<Integer,Integer>, byte[], Block>() {
         *
         * @Override public Tuple2<Integer, Integer> run(byte[] a, Block b) { //
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
        if (BlockChain.DEVELOP_USE && creator.getLastForgingData(dcSet) == null) {
            // так как унас новые счета сами стартуют без инициализации - надо тут учеть начало
            creator.setForgingData(dcSet, height - BlockChain.DEVELOP_FORGING_START, block.getForgingValue());
        }
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
    public Block remove(byte[] signature, byte[] reference, PublicKeyAccount creator) {
        DCSet dcSet = getDCSet();

        int height = this.size();

        this.setLastBlockSignature(reference);
        dcSet.getBlockSignsMap().delete(signature);

        // ORPHAN FORGING DATA
        if (height > 1) {

            //Block.BlockHead head = dcSet.getBlocksHeadsMap().remove();
            dcSet.getBlocksHeadsMap().remove();

            if (creator.getAddress().equals("7CvpXXALviZPkZ9Yn27NncLVz6SkxMA8rh")
                    && height > 291000 && height < 291056) {
                Tuple2<String, Integer> key = new Tuple2<String, Integer>(creator.getAddress(), height);
                Tuple2<Integer, Integer> previous = dcSet.getAddressForging().get(key);
                int ii = 0;
            }

                // INITIAL forging DATA no need remove!
            Tuple2<String, Integer> key = new Tuple2<String, Integer>(creator.getAddress(), height);
            Tuple2<Integer, Integer> previous = dcSet.getAddressForging().get(key);
            if (previous != null) {
                // иногда бывавет что при откате в этом же блок и был собран блок
                // и была транзакция с ЭРА то два раза пытается откатить - сначала как у транзакции
                // а потом как у блока - то тут словим на второй раз NULL - и форжинг с него прекращается
                // однако удаление для прихода монет в ноль должно остаться
                creator.delForgingData(dcSet, height);
            }

        }

        // use SUPER.class only!
        return super.delete(height);

    }

    public void notifyResetChain() {
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_RESET_BLOCK_TYPE, null));
    }

    public void notifyProcessChain(Block block) {

        if (Controller.getInstance().isOnStopping()) {
            return;
        }

        LOGGER.debug("++++++ NOTIFY CHAIN_ADD_BLOCK_TYPE");
        this.setChanged();
        // NEED in BLOCK!
        this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_ADD_BLOCK_TYPE, block));

        LOGGER.debug("++++++ NOTIFY CHAIN_ADD_BLOCK_TYPE END");
    }

    public void notifyOrphanChain(Block block) {

        if (Controller.getInstance().isOnStopping()) {
            return;
        }

        LOGGER.debug("===== NOTIFY CHAIN_REMOVE_BLOCK_TYPE");
        this.setChanged();
        // NEED in BLOCK!
        this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE, block));

        LOGGER.debug("===== NOTIFY CHAIN_REMOVE_BLOCK_TYPE END");
    }

}
