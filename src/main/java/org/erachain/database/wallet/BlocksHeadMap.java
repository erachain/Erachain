package org.erachain.database.wallet;

import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.database.DBMap;
import org.erachain.database.serializer.BlockHeadSerializer;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.erachain.utils.ReverseComparator;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

/*
 *  Block Height ->
 *  BLOCK HEAD:
 *  + FACE - height, creator, signature, transactionsCount, Transactions Hash
 *  + Version, parentSignature, Fee as BigDecimal.toLong
 *  + Forging Data - Forging Value, Win Value, Target Value
 * maker
 */

public class BlocksHeadMap extends DBMap<Tuple2<String, String>, Block.BlockHead> {
    // нужно сделать так: public class BlocksHeadMap extends DCMap<Integer, Block.BlockHead> {
    public static final int TIMESTAMP_INDEX = 1;
    public static final int GENERATOR_INDEX = 2;
    public static final int BALANCE_INDEX = 3;
    public static final int TRANSACTIONS_INDEX = 4;
    public static final int FEE_INDEX = 5;
    static Logger logger = LoggerFactory.getLogger(BlocksHeadMap.class.getName());
    private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

    public BlocksHeadMap(DWSet dWSet, DB database) {
        super(dWSet, database);

        observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.WALLET_RESET_BLOCK_TYPE);
        observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.WALLET_ADD_BLOCK_TYPE);
        observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.WALLET_REMOVE_BLOCK_TYPE);
        observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.WALLET_LIST_BLOCK_TYPE);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes(DB database) {

        //TIMESTAMP INDEX
        NavigableSet<Tuple2<Long, Tuple2<String, String>>> timestampIndex = database.createTreeSet("blocks_index_timestamp")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<Long, Tuple2<String, String>>> descendingTimestampIndex = database.createTreeSet("blocks_index_timestamp_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(TIMESTAMP_INDEX, timestampIndex, descendingTimestampIndex, new Fun.Function2<Integer, Tuple2<String, String>,
                Block.BlockHead>() {
            @Override
            public Integer run(Tuple2<String, String> key, Block.BlockHead value) {
                return value.heightBlock;
            }
        });

        //GENERATOR INDEX
        NavigableSet<Tuple2<String, Tuple2<String, String>>> generatorIndex = database.createTreeSet("blocks_index_generator")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingGeneratorIndex = database.createTreeSet("blocks_index_generator_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(GENERATOR_INDEX, generatorIndex, descendingGeneratorIndex, new Fun.Function2<String, Tuple2<String, String>, Block.BlockHead>() {
            @Override
            public String run(Tuple2<String, String> key, Block.BlockHead value) {
                return key.a;
            }
        });

        //BALANCE INDEX
        NavigableSet<Tuple2<Integer, Tuple2<String, String>>> balanceIndex = database.createTreeSet("blocks_index_balance")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<Integer, Tuple2<String, String>>> descendingBalanceIndex = database.createTreeSet("blocks_index_balance_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(BALANCE_INDEX, balanceIndex, descendingBalanceIndex, new Fun.Function2<Integer, Tuple2<String, String>, Block.BlockHead>() {
            @Override
            public Integer run(Tuple2<String, String> key, Block.BlockHead value) {
                return value.forgingValue;
            }
        });

        //TRANSACTIONS INDEX
        NavigableSet<Tuple2<Integer, Tuple2<String, String>>> transactionsIndex = database.createTreeSet("blocks_index_transactions")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<Integer, Tuple2<String, String>>> descendingTransactionsIndex = database.createTreeSet("blocks_index_transactions_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(TRANSACTIONS_INDEX, transactionsIndex, descendingTransactionsIndex, new Fun.Function2<Integer, Tuple2<String, String>, Block.BlockHead>() {
            @Override
            public Integer run(Tuple2<String, String> key, Block.BlockHead value) {
                return value.transactionsCount;
            }
        });

        //FEE INDEX
        NavigableSet<Tuple2<BigDecimal, Tuple2<String, String>>> feeIndex = database.createTreeSet("blocks_index_fee")
                .comparator(Fun.COMPARATOR)
                .makeOrGet();

        NavigableSet<Tuple2<BigDecimal, Tuple2<String, String>>> descendingFeeIndex = database.createTreeSet("blocks_index_fee_descending")
                .comparator(new ReverseComparator(Fun.COMPARATOR))
                .makeOrGet();

        createIndex(FEE_INDEX, feeIndex, descendingFeeIndex, new Fun.Function2<Long, Tuple2<String, String>, Block.BlockHead>() {
            @Override
            public Long run(Tuple2<String, String> key, Block.BlockHead value) {
                return value.totalFee;
            }
        });
    }

    @Override
    protected Map<Tuple2<String, String>, Block.BlockHead> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("blocks")
                .keySerializer(BTreeKeySerializer.TUPLE2) /// ТУТ тоже переделать на стандартный серилиазотор
                .valueSerializer(new BlockHeadSerializer())
                .valuesOutsideNodesEnable()
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected Map<Tuple2<String, String>, Block.BlockHead> getMemoryMap() {
        return new TreeMap<Tuple2<String, String>, Block.BlockHead>(Fun.TUPLE2_COMPARATOR);
    }

    @Override
    protected Block.BlockHead getDefaultValue() {
        return null;
    }

    @Override
    protected Map<Integer, Integer> getObservableData() {
        return this.observableData;
    }

    public Block.BlockHead getLast() {

        List<Pair<Account, Block.BlockHead>> blocks = new ArrayList<Pair<Account, Block.BlockHead>>();

        Iterator<Tuple2<String, String>> iterator = this.getIterator(1, true);
        if (!iterator.hasNext())
            return null;

        return this.get(iterator.next());
    }

    // TODO - SORT by HEIGHT !!!
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Block.BlockHead> get(Account account, int limit) {
        List<Block.BlockHead> blocks = new ArrayList<Block.BlockHead>();

        try {
            Map<Tuple2<String, String>, Block.BlockHead> accountBlocks = ((BTreeMap) this.map).subMap(
                    Fun.t2(account.getAddress(), null),
                    Fun.t2(account.getAddress(), Fun.HI()));

            //GET ITERATOR
            Iterator<Block.BlockHead> iterator = accountBlocks.values().iterator();

            int i = 0;
            while (iterator.hasNext() && i < limit) {
                i++;
                blocks.add(iterator.next());
            }
        } catch (Exception e) {
            //ERROR
            logger.error(e.getMessage(), e);
        }

        return blocks;
    }

    public List<Pair<Account, Block.BlockHead>> get(List<Account> accounts, int limit) {
        List<Pair<Account, Block.BlockHead>> blocks = new ArrayList<Pair<Account, Block.BlockHead>>();

        try {
            //FOR EACH ACCOUNTS
            synchronized (accounts) {
                for (Account account : accounts) {
                    List<Block.BlockHead> accountBlocks = get(account, limit);
                    for (Block.BlockHead block : accountBlocks) {
                        blocks.add(new Pair<Account, Block.BlockHead>(account, block));
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return blocks;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Account account) {
        //GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
        Map<Tuple2<String, String>, Block> accountBlocks = ((BTreeMap) this.map).subMap(
                Fun.t2(account.getAddress(), null),
                Fun.t2(account.getAddress(), Fun.HI()));

        //DELETE TRANSACTIONS
        for (Tuple2<String, String> key : accountBlocks.keySet()) {
            delete(key);
            databaseSet.commit();
        }
    }

    public void delete(Block.BlockHead block) {
        delete(new Tuple2<String, String>(block.creator.getAddress(), new String(block.signature)));
        databaseSet.commit();
    }

    public void deleteAll(List<Account> accounts) {
        for (Account account : accounts) {
            delete(account);
        }
    }

    public boolean add(Block.BlockHead block) {
        boolean result = this.set(new Tuple2<String, String>(block.creator.getAddress(),
                new String(block.signature)), block);
        databaseSet.commit();
        return result;
    }

    public void addAll(Map<Account, List<Block.BlockHead>> blocks) {
        //FOR EACH ACCOUNT
        for (Account account : blocks.keySet()) {
            //FOR EACH TRANSACTION
            for (Block.BlockHead block : blocks.get(account)) {
                add(block);
            }
        }
    }
}
