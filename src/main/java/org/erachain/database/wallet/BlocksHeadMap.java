package org.erachain.database.wallet;

import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.database.serializer.BlockHeadSerializer;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DCUMapImpl;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *  Block Height ->
 *  BLOCK HEAD:
 *  + FACE - height, creator, signature, transactionsCount, Transactions Hash
 *  + Version, parentSignature, Fee as BigDecimal.toLong
 *  + Forging Data - Forging Value, Win Value, Target Value
 * maker
 */

public class BlocksHeadMap extends DCUMapImpl<Integer, Block.BlockHead> {

    static Logger logger = LoggerFactory.getLogger(BlocksHeadMap.class.getName());

    /**
     * для сортировки с SortableList задает по умолчанию клю по Высоте блока:<br>
     *     DEFAULT_INDEX = TIMESTAMP_INDEX
     *
     * @param dWSet
     * @param database
     */
    public BlocksHeadMap(DWSet dWSet, DB database) {
        super(dWSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.WALLET_RESET_BLOCK_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.WALLET_LIST_BLOCK_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.WALLET_ADD_BLOCK_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.WALLET_REMOVE_BLOCK_TYPE);
        }
    }

    @Override
    public void openMap() {
        HI = Integer.MAX_VALUE;
        LO = 0;

        //OPEN MAP
        map = database.createTreeMap("blocks")
                .valueSerializer(new BlockHeadSerializer())
                .valuesOutsideNodesEnable()
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<>();
    }

    public Block.BlockHead getLast() {
        return get(size());
    }

    // TODO - SORT by HEIGHT !!!
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Block.BlockHead> get(Account account, int limit) {
        List<Block.BlockHead> blocks = new ArrayList<>();

        try {
            Map<Integer, Block.BlockHead> accountBlocks = ((BTreeMap) this.map).subMap(
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
        List<Pair<Account, Block.BlockHead>> blocks = new ArrayList<>();

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
        Map<Integer, Block> accountBlocks = ((BTreeMap) this.map).subMap(
                Fun.t2(account.getAddress(), null),
                Fun.t2(account.getAddress(), Fun.HI()));

        //DELETE TRANSACTIONS
        for (Integer key : accountBlocks.keySet()) {
            delete(key);
        }
    }

    public void delete(Block.BlockHead block) {
        delete(block.heightBlock);
    }

    public void deleteAll(List<Account> accounts) {
        for (Account account : accounts) {
            delete(account);
        }
    }

    public boolean add(Block.BlockHead block) {
        return this.set(block.heightBlock, block);
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
