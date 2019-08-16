package org.erachain.datachain;
// upd 09/03

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.block.Block;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.NameStorageMap;
import org.erachain.core.web.OrphanNameStorageHelperMap;
import org.erachain.core.web.OrphanNameStorageMap;
import org.erachain.core.web.SharedPostsMap;
import org.erachain.database.DBASet;
import org.erachain.settings.Settings;
import org.erachain.utils.UpdateUtil;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

/**
 * набор таблиц. Поидее тут нужно хранить список таблиц и ссылку на родителя при Форке базы.
 * Но почемуто парент хранится в каждой таблице - хотя там сразу ссылка на форкнутую таблицу есть
 * а в ней уже хранится объект набора DCSet
 */
public class DCSet extends DBASet implements Observer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DCSet.class);
    private static final int ACTIONS_BEFORE_COMMIT = BlockChain.MAX_BLOCK_SIZE_GEN << 2;
    private static final int MAX_ENGINE_BEFORE_COMMIT_KB = BlockChain.MAX_BLOCK_SIZE_BYTES_GEN >> 4 ;
    private static final long TIME_COMPACT_DB = 1L * 24L * 3600000L;

    private static final int CASH_SIZE = 1024 << Controller.HARD_WORK;

    private static boolean isStoped = false;
    private volatile static DCSet instance;
    private DCSet parent;

    // % и @ и # - пусть они будут служебные и по ним не делать разделения
    // так чтобы можно было найти @P указатель на персон например
    // % - это указатель на параметр например иак - %1
    public static String SPLIT_CHARS = "[!?/_., \\-~`+&^№*()<>\\\"\\'|\\[\\]{}=;:\\\\]";

    private boolean inMemory = false;

    private BlockChain bchain;

    private AddressForging addressForging;
    private CreditAddressesMap credit_AddressesMap;
    private ItemAssetBalanceMap assetBalanceMap;
    private AddressStatementRefs addressStatement_Refs;
    private ItemAssetBalanceMap assetBalanceAccountingMap;
    private KKAssetStatusMap kKAssetStatusMap;
    private KKPersonStatusMap kKPersonStatusMap;
    //private KKPollStatusMap kKPollStatusMap;
    private KKUnionStatusMap kKUnionStatusMap;
    private KKAssetUnionMap kKAssetUnionMap;
    private KKPersonUnionMap kKPersonUnionMap;
    private KKPollUnionMap kKPollUnionMap;
    private KKStatusUnionMap kKStatusUnionMap;
    private AddressPersonMap addressPersonMap;
    private PersonAddressMap personAddressMap;
    private KKKMapPersonStatusUnion kK_KPersonStatusUnionMapPersonStatusUnionTable;
    private VouchRecordMap vouchRecordMap;
    private HashesMap hashesMap;
    private HashesSignsMap hashesSignsMap;

    private BlockMap blockMap;
    //private BlockCreatorMap blockCreatorMap;
    private BlockSignsMap blockSignsMap;
    private BlocksHeadsMap blocksHeadsMap;
    private ReferenceMap referenceMap;
    private NameMap nameMap;
    private NameStorageMap nameStorageMap;
    private OrphanNameStorageMap orphanNameStorageMap;
    private OrphanNameStorageHelperMap orphanNameStorageHelperMap;
    private SharedPostsMap sharedPostsMap;
    private PostCommentMap postCommentMap;
    private CommentPostMap commentPostMap;
    private LocalDataMap localDataMap;
    private BlogPostMap blogPostMap;
    private HashtagPostMap hashtagPostMap;
    private NameExchangeMap nameExchangeMap;
    private UpdateNameMap updateNameMap;
    private CancelSellNameMap cancelSellNameMap;
    private PollMap pollMap;
    private VoteOnPollMap voteOnPollMap;
    private VoteOnItemPollMap voteOnItemPollMap;
    private ItemAssetMap itemAssetMap;
    private IssueAssetMap issueAssetMap;
    private OrderMap orderMap;
    private CompletedOrderMap completedOrderMap;
    private TradeMap tradeMap;
    private ItemStatusMap itemStatusMap;
    private IssueStatusMap issueStatusMap;
    private ItemImprintMap itemImprintMap;
    private IssueImprintMap issueImprintMap;
    private ItemPollMap itemPollMap;
    private IssuePollMap issuePollMap;
    private ItemTemplateMap itemTemplateMap;
    private IssueTemplateMap issueTemplateMap;
    private ItemStatementMap itemStatementMap;
    private IssueStatementMap issueStatementMap;
    private ItemPersonMap itemPersonMap;
    private IssuePersonMap issuePersonMap;
    private ItemUnionMap itemUnionMap;
    private IssueUnionMap issueUnionMap;
    private ATMap atMap;
    private ATStateMap atStateMap;
    private ATTransactionMap atTransactionMap;
    private TransactionFinalMap transactionFinalMap;
    private TransactionFinalCalculatedMap transactionFinalCalculatedMap;
    private TransactionFinalMapSigns transactionFinalMapSigns;
    private TransactionMap transactionMap;

    private long actions = (long) (Math.random() * (ACTIONS_BEFORE_COMMIT >> 1));

    public DCSet(File dbFile, DB database, boolean withObserver, boolean dynamicGUI, boolean inMemory) {
        super(dbFile, database, withObserver, dynamicGUI);

        LOGGER.info("UP SIZE BEFORE COMMIT [KB]: " + MAX_ENGINE_BEFORE_COMMIT_KB
                        + ", ACTIONS BEFORE COMMIT: " + ACTIONS_BEFORE_COMMIT);

        this.engineSize = getEngineSize();
        this.inMemory = inMemory;

        try {
            this.actions = 0L;

            this.blockMap = new BlockMap(this, database);
            //this.blockCreatorMap = new BlockCreatorMap(this, database);
            this.blockSignsMap = new BlockSignsMap(this, database);
            this.blocksHeadsMap = new BlocksHeadsMap(this, database);
            this.referenceMap = new ReferenceMap(this, database);
            this.addressForging = new AddressForging(this, database);
            this.credit_AddressesMap = new CreditAddressesMap(this, database);
            this.assetBalanceMap = new ItemAssetBalanceMap(this, database);
            this.addressStatement_Refs = new AddressStatementRefs(this, database);
            this.assetBalanceAccountingMap = new ItemAssetBalanceMap(this, database);

            this.kKAssetStatusMap = new KKAssetStatusMap(this, database);
            this.kKPersonStatusMap = new KKPersonStatusMap(this, database);
            //this.kKPollStatusMap = new KKPollStatusMap(this, database);
            this.kKUnionStatusMap = new KKUnionStatusMap(this, database);
            this.kKAssetUnionMap = new KKAssetUnionMap(this, database);
            this.kKPersonUnionMap = new KKPersonUnionMap(this, database);
            this.kKPollUnionMap = new KKPollUnionMap(this, database);
            this.kKStatusUnionMap = new KKStatusUnionMap(this, database);
            this.addressPersonMap = new AddressPersonMap(this, database);
            this.personAddressMap = new PersonAddressMap(this, database);
            this.kK_KPersonStatusUnionMapPersonStatusUnionTable = new KKKMapPersonStatusUnion(this, database);
            this.transactionFinalMap = new TransactionFinalMap(this, database);
            this.transactionFinalCalculatedMap = new TransactionFinalCalculatedMap(this, database);

            this.transactionFinalMapSigns = new TransactionFinalMapSigns(this, database);
            this.transactionMap = new TransactionMap(this, database);
            this.vouchRecordMap = new VouchRecordMap(this, database);
            this.hashesMap = new HashesMap(this, database);
            this.hashesSignsMap = new HashesSignsMap(this, database);
            this.nameMap = new NameMap(this, database);
            this.nameStorageMap = new NameStorageMap(this, database);
            this.orphanNameStorageMap = new OrphanNameStorageMap(this, database);
            this.orphanNameStorageHelperMap = new OrphanNameStorageHelperMap(this, database);

            this.sharedPostsMap = new SharedPostsMap(this, database);
            this.postCommentMap = new PostCommentMap(this, database);
            this.commentPostMap = new CommentPostMap(this, database);
            this.localDataMap = new LocalDataMap(this, database);
            this.blogPostMap = new BlogPostMap(this, database);
            this.hashtagPostMap = new HashtagPostMap(this, database);
            this.nameExchangeMap = new NameExchangeMap(this, database);
            this.updateNameMap = new UpdateNameMap(this, database);
            this.cancelSellNameMap = new CancelSellNameMap(this, database);
            this.pollMap = new PollMap(this, database);
            this.voteOnPollMap = new VoteOnPollMap(this, database);
            this.voteOnItemPollMap = new VoteOnItemPollMap(this, database);

            this.itemAssetMap = new ItemAssetMap(this, database);
            this.issueAssetMap = new IssueAssetMap(this, database);
            this.orderMap = new OrderMap(this, database);
            this.completedOrderMap = new CompletedOrderMap(this, database);
            this.tradeMap = new TradeMap(this, database);

            this.itemImprintMap = new ItemImprintMap(this, database);
            this.issueImprintMap = new IssueImprintMap(this, database);

            this.itemTemplateMap = new ItemTemplateMap(this, database);
            this.issueTemplateMap = new IssueTemplateMap(this, database);

            this.itemPersonMap = new ItemPersonMap(this, database);
            this.issuePersonMap = new IssuePersonMap(this, database);

            this.itemPollMap = new ItemPollMap(this, database);
            this.issuePollMap = new IssuePollMap(this, database);

            this.itemStatementMap = new ItemStatementMap(this, database);
            this.issueStatementMap = new IssueStatementMap(this, database);

            this.itemStatusMap = new ItemStatusMap(this, database);
            this.issueStatusMap = new IssueStatusMap(this, database);

            this.itemUnionMap = new ItemUnionMap(this, database);
            this.issueUnionMap = new IssueUnionMap(this, database);

            this.atMap = new ATMap(this, database);
            this.atStateMap = new ATStateMap(this, database);

            this.atTransactionMap = new ATTransactionMap(this, database);

        } catch (Throwable e) {
            this.close();
            throw e;
        }

        if (this.blockMap.size() != this.blocksHeadsMap.size()
                || this.blockSignsMap.size() != this.blocksHeadsMap.size()) {
            LOGGER.info("reset DATACHAIN on height error (blockMap, blockSignsMap, blocksHeadsMap: "
                    + this.blockMap.size() + " == " + this.blocksHeadsMap.size());

            this.close();
            this.actions = -1;

        }
        uses--;

    }

    /**
     * Make data set as Fork
     *
     * @param parent parent DCSet
     * @param idDatabase
     */
    protected DCSet(DCSet parent, DB idDatabase) {

        this.addUses();

        this.database = idDatabase;

        this.parent = parent;
        ///this.database = parent.database.snapshot();
        this.bchain = parent.bchain;

        this.addressForging = new AddressForging(parent.addressForging, this);
        this.credit_AddressesMap = new CreditAddressesMap(parent.credit_AddressesMap, this);
        this.assetBalanceMap = new ItemAssetBalanceMap(parent.assetBalanceMap, this);
        this.addressStatement_Refs = new AddressStatementRefs(parent.addressStatement_Refs, this);
        this.assetBalanceAccountingMap = new ItemAssetBalanceMap(parent.assetBalanceAccountingMap, this);
        this.kKAssetStatusMap = new KKAssetStatusMap(parent.kKAssetStatusMap, this);
        this.kKPersonStatusMap = new KKPersonStatusMap(parent.kKPersonStatusMap, this);
        this.kKUnionStatusMap = new KKUnionStatusMap(parent.kKUnionStatusMap, this);
        this.kKAssetUnionMap = new KKAssetUnionMap(parent.kKAssetUnionMap, this);
        this.kKPersonUnionMap = new KKPersonUnionMap(parent.kKPersonUnionMap, this);
        this.kKPollUnionMap = new KKPollUnionMap(parent.kKPollUnionMap, this);
        this.kKStatusUnionMap = new KKStatusUnionMap(parent.kKStatusUnionMap, this);

        this.addressPersonMap = new AddressPersonMap(parent.addressPersonMap, this);
        this.personAddressMap = new PersonAddressMap(parent.personAddressMap, this);
        this.kK_KPersonStatusUnionMapPersonStatusUnionTable = new KKKMapPersonStatusUnion(parent.kK_KPersonStatusUnionMapPersonStatusUnionTable, this);
        this.transactionFinalMap = new TransactionFinalMap(parent.transactionFinalMap, this);
        this.transactionFinalCalculatedMap = new TransactionFinalCalculatedMap(parent.transactionFinalCalculatedMap, this);
        this.transactionFinalMapSigns = new TransactionFinalMapSigns(parent.transactionFinalMapSigns, this);
        this.transactionMap = new TransactionMap(parent.transactionMap, this);
        this.vouchRecordMap = new VouchRecordMap(parent.vouchRecordMap, this);
        this.hashesMap = new HashesMap(parent.hashesMap, this);
        this.hashesSignsMap = new HashesSignsMap(parent.hashesSignsMap, this);

        this.blockMap = new BlockMap(parent.blockMap, this);
        this.blockSignsMap = new BlockSignsMap(parent.blockSignsMap, this);
        this.blocksHeadsMap = new BlocksHeadsMap(parent.blocksHeadsMap, this);
        this.referenceMap = new ReferenceMap(parent.referenceMap, this);
        //this.nameMap = new NameMap(parent.nameMap);
        //this.nameStorageMap = new NameStorageMap(parent.nameStorageMap);
        //this.orphanNameStorageMap = new OrphanNameStorageMap(parent.orphanNameStorageMap);
        //this.sharedPostsMap = new SharedPostsMap(parent.sharedPostsMap);

        //this.postCommentMap = new PostCommentMap(parent.postCommentMap);
        //this.commentPostMap = new CommentPostMap(parent.commentPostMap);
        //this.orphanNameStorageHelperMap = new OrphanNameStorageHelperMap(parent.orphanNameStorageHelperMap);
        //this.localDataMap = new LocalDataMap(parent.localDataMap);
        //this.blogPostMap = new BlogPostMap(parent.blogPostMap);
        //this.hashtagPostMap = new HashtagPostMap(parent.hashtagPostMap);
        //this.nameExchangeMap = new NameExchangeMap(parent.nameExchangeMap);
        //this.updateNameMap = new UpdateNameMap(parent.updateNameMap);
        //this.cancelSellNameMap = new CancelSellNameMap(parent.cancelSellNameMap);

        //this.pollMap = new PollMap(parent.pollMap);
        //this.voteOnPollMap = new VoteOnPollMap(parent.voteOnPollMap);

        this.voteOnItemPollMap = new VoteOnItemPollMap(parent.voteOnItemPollMap, this);

        this.itemAssetMap = new ItemAssetMap(parent.itemAssetMap, this);
        this.issueAssetMap = new IssueAssetMap(parent.getIssueAssetMap(), this);
        this.orderMap = new OrderMap(parent.orderMap, this);
        this.completedOrderMap = new CompletedOrderMap(parent.completedOrderMap, this);
        this.tradeMap = new TradeMap(parent.tradeMap, this);

        this.itemImprintMap = new ItemImprintMap(parent.itemImprintMap, this);
        this.issueImprintMap = new IssueImprintMap(parent.issueImprintMap, this);

        this.itemTemplateMap = new ItemTemplateMap(parent.itemTemplateMap, this);
        this.issueTemplateMap = new IssueTemplateMap(parent.getIssueTemplateMap(), this);

        this.itemStatementMap = new ItemStatementMap(parent.itemStatementMap, this);
        this.issueStatementMap = new IssueStatementMap(parent.issueStatementMap, this);

        this.itemPersonMap = new ItemPersonMap(parent.getItemPersonMap(), this);
        this.issuePersonMap = new IssuePersonMap(parent.getIssuePersonMap(), this);

        this.itemPollMap = new ItemPollMap(parent.itemPollMap, this);
        this.issuePollMap = new IssuePollMap(parent.issuePollMap, this);

        this.itemStatusMap = new ItemStatusMap(parent.itemStatusMap, this);
        this.issueStatusMap = new IssueStatusMap(parent.issueStatusMap, this);

        this.itemUnionMap = new ItemUnionMap(parent.itemUnionMap, this);
        this.issueUnionMap = new IssueUnionMap(parent.issueUnionMap, this);

        this.atMap = new ATMap(parent.atMap, this);
        this.atStateMap = new ATStateMap(parent.atStateMap, this);

        this.atTransactionMap = new ATTransactionMap(parent.atTransactionMap, this);

        this.outUses();
    }

    /**
     * Get instance of DCSet or create new
     *
     * @param withObserver [true] - for switch on GUI observers
     * @param dynamicGUI [true] - for switch on GUI observers fir dynamic interface

     * @return

     * @throws Exception
     */

    public static DCSet getInstance(boolean withObserver, boolean dynamicGUI, boolean inMemory) throws Exception {
        if (instance == null) {
            if (inMemory) {
                reCreateDBinMEmory(withObserver, dynamicGUI);
            } else {
                reCreateDB(withObserver, dynamicGUI);
            }
        }

        return instance;
    }

    /**
     *
     * @return
     */
    public static DCSet getInstance() {
        return instance;
    }

    /**
     * remake data set
     *
     * @param withObserver [true] - for switch on GUI observers
     * @param dynamicGUI [true] - for switch on GUI observers fir dynamic interface
     * @throws Exception
     */
    public static void reCreateDB(boolean withObserver, boolean dynamicGUI) throws Exception {

        //OPEN DB
        File dbFile = new File(Settings.getInstance().getDataDir(), "chain.dat");
        dbFile.getParentFile().mkdirs();

        /// https://jankotek.gitbooks.io/mapdb/performance/
        //CREATE DATABASE
        DB database = DBMaker.newFileDB(dbFile)
                // убрал .closeOnJvmShutdown() it closing not by my code and rise errors! closed before my closing

                //// иначе кеширует блок и если в нем удалить трнзакции или еще что то выдаст тут же такой блок с пустыми полями
                ///// добавил dcSet.clearCache(); --
                ///.cacheDisable()

                ////// ТУТ вряд ли нужно КЭШИРОВАТь при чтении что-либо
                //////
                // это чистит сама память если соталось 25% от кучи - так что она безопасная
                // у другого типа КЭША происходит утечка памяти
                //.cacheHardRefEnable()
                //.cacheLRUEnable()
                .cacheSoftRefEnable()
                ///.cacheWeakRefEnable()

                // количество точек в таблице которые хранятся в HashMap как в КЭШе
                // - начальное значени для всех UNBOUND и максимальное для КЭШ по умолчанию
                // WAL в кэш на старте закатывает все значения - ограничим для быстрого старта
                .cacheSize(10)

                .checksumEnable()
                .mmapFileEnableIfSupported() // ++ but -- error on asyncWriteEnable
                .commitFileSyncDisable() // ++

                //.snapshotEnable()
                //.asyncWriteEnable()
                //.asyncWriteFlushDelay(100)
                //.cacheHardRefEnable()

                // если при записи на диск блока процессор сильно нагружается - то уменьшить это
                .freeSpaceReclaimQ(7) // не нагружать процессор для поиска свободного места в базе данных

                //.compressionEnable()

                /*
                .cacheSize(CASH_SIZE)
                //.checksumEnable()
                .cacheHardRefEnable()
                .commitFileSyncDisable()
                //////.transactionDisable()
                //.asyncWriteEnable() ///
                //.asyncWriteFlushDelay(1000) //
                //.mmapFileEnableIfSupported()
                 */
                .make();

        if (Controller.getInstance().compactDConStart) {
            LOGGER.debug("try COMPACT");
            database.compact();
            LOGGER.debug("COMPACTED");
        }

        //CREATE INSTANCE
        instance = new DCSet(dbFile, database, withObserver, dynamicGUI, false);
        if (instance.actions < 0) {
            dbFile.delete();
            throw new Exception("error in DATACHAIN:" + instance.actions);
        }

        if (false && Controller.getInstance().compactDConStart) {
            // NEED LOT of MEMORY by .reset
            UpdateUtil.repopulateTransactionFinalMap(instance);
            LOGGER.debug("REPOPULATED");
        }


    }

    public static void reCreateDBinMEmory(boolean withObserver, boolean dynamicGUI) {
        DB database = DBMaker
                .newMemoryDB()
                //.newMemoryDirectDB()
                .make();

        instance = new DCSet(null, database, withObserver, dynamicGUI, true);

    }

        /**
         * make data set in memory. For tests
         *
         * @return
         */
    public static DCSet createEmptyDatabaseSet() {
        DB database = DBMaker
                .newMemoryDB()
                //.newMemoryDirectDB()
                .make();

        instance = new DCSet(null, database, false, false, true);
        return instance;
    }

    /**
     * create FORK of DB
     *
     * @return
     */
    public static DB createForkbase() {

        return getHardBase();
    }

    public static boolean isStoped() {
        return isStoped;
    }

    public boolean inMemory() {
        return this.inMemory || this.parent != null;
    }

    @Override
    public void addUses() {
        if (this.parent != null) {
            return;
        }
        this.uses++;
    }

    @Override
    public void outUses() {
        if (this.parent != null) {
            return;
        }
        this.uses--;
    }

    /**
     * reset all data set
     */
    public void reset() {

        this.addUses();

        this.addressForging.reset();
        this.credit_AddressesMap.reset();
        this.assetBalanceMap.reset();
        this.addressStatement_Refs.reset();
        this.assetBalanceAccountingMap.reset();
        this.kKAssetStatusMap.reset();
        this.kKPersonStatusMap.reset();
        this.kKUnionStatusMap.reset();
        this.kKAssetUnionMap.reset();
        this.kKPersonUnionMap.reset();
        this.kKPollUnionMap.reset();

        this.kKStatusUnionMap.reset();
        this.addressPersonMap.reset();
        this.personAddressMap.reset();
        this.kK_KPersonStatusUnionMapPersonStatusUnionTable.reset();
        this.vouchRecordMap.reset();
        this.hashesMap.reset();
        this.hashesSignsMap.reset();
        this.blockMap.reset();
        this.blockSignsMap.reset();
        this.blocksHeadsMap.reset();

        this.referenceMap.reset();
        this.transactionFinalMap.reset();
        this.transactionFinalCalculatedMap.reset();        
        this.transactionFinalMapSigns.reset();
        this.transactionMap.reset();
        this.nameMap.reset();
        this.nameStorageMap.reset();
        this.orphanNameStorageMap.reset();
        this.orphanNameStorageHelperMap.reset();
        this.sharedPostsMap.reset();
        this.commentPostMap.reset();

        this.postCommentMap.reset();
        this.localDataMap.reset();
        this.blogPostMap.reset();
        this.hashtagPostMap.reset();
        this.nameExchangeMap.reset();
        this.updateNameMap.reset();
        this.cancelSellNameMap.reset();
        this.pollMap.reset();
        this.voteOnPollMap.reset();
        this.voteOnItemPollMap.reset();

        this.tradeMap.reset();

        this.orderMap.reset();
        this.completedOrderMap.reset();
        this.issueAssetMap.reset();
        this.itemAssetMap.reset();
        this.issueImprintMap.reset();
        this.itemImprintMap.reset();
        this.issueTemplateMap.reset();
        this.itemStatementMap.reset();
        this.issueStatementMap.reset();
        this.itemTemplateMap.reset();

        this.issuePersonMap.reset();
        this.itemPersonMap.reset();
        this.issuePollMap.reset();
        this.itemPollMap.reset();
        this.issueStatusMap.reset();
        this.itemStatusMap.reset();
        this.issueUnionMap.reset();
        this.itemUnionMap.reset();
        this.atMap.reset();
        this.atStateMap.reset();
        this.atTransactionMap.reset();
        //this.blockCreatorMap.reset();

        this.outUses();
    }

    /**
     * Взять родительскую базу, с которой сделан форк. Используется в процессах транзакций
     * @return
     */
    public DCSet getParent() {
        return this.parent;
    }

    /**
     * всять объект цепочки для которой эта база сделана
     * @return BlockChain
     */
    public BlockChain getBlockChain() {
        return this.bchain;
    }

    public void setBlockChain(BlockChain bchain) {
        this.bchain = bchain;
    }

    /**
     * это форкнутая база?
     * @return
     */
    public boolean isFork() {
        return this.parent != null;
    }

    /**************************************************************************************************/

    /**
     * Хранит данные о сборке блока для данного счета - по номеру блока
     * если номер блока не задан - то это последнее значение.
     * При этом если номер блока не задана то хранится поледнее значение
     *  account.address + current block.Height ->
     *     previous making blockHeight + this ForgingH balance
     <hr>
     - not SAME with BLOCK HEADS - use point for not only forged blocks - with incoming ERA Volumes

     * @return
     */
    // TODO укротить до 20 байт адрес
    public AddressForging getAddressForging() {
        return this.addressForging;
    }

    /** Общая сумма переданных средств в кредит на другой счет
     * Используется для проверки сумм которые отдаются или забираются у заемщика<br><br>
     *
     * <b>Ключ:</b> account.address Creditor + asset key + account.address Debtor<br>
     *
     * <b>Значение:</b> сумма средств
     *
     */
    public CreditAddressesMap getCredit_AddressesMap() {
        return this.credit_AddressesMap;
    }

    /** Балансы для заданного адреса на данный актив. balances for all account in blockchain<br>
     * <b>Список балансов:</b> имущество, займы, хранение, производство, резерв<br>
     * Каждый баланс: Всего Пришло и Остаток<br><br>
     *
     * <b>Ключ:</b> account.address + asset key<br>
     *
     * <b>Значение:</b> Балансы. in_OWN, in_RENT, on_HOLD = in_USE (TOTAL on HAND)
     *
     */
// TODO SOFT HARD TRUE
    public ItemAssetBalanceMap getAssetBalanceMap() {
        return this.assetBalanceMap;
    }

    /**
     * Хранит для этого адреса и времени создания ссылки на транзакции типа Statement, см. супер класс
     * @return
     */
    public AddressStatementRefs getAddressStatement_Refs() {
        return this.addressStatement_Refs;
    }

    /** (пока не используется - по идее для бухгалтерских единиц отдельная таблица)
     * Балансы для заданного адреса на данный актив. balances for all account in blockchain<br>
     * <b>Список балансов:</b> имущество, займы, хранение, производство, резерв<br>
     * Каждый баланс: Всего Пришло и Остаток<br><br>
     *
     * <b>Ключ:</b> account.address + asset key<br>
     *
     * <b>Значение:</b> Балансы. in_OWN, in_RENT, on_HOLD = in_USE (TOTAL on HAND)
     *
     */
    // TODO SOFT HARD TRUE
    public ItemAssetBalanceMap getAssetBalanceAccountingMap() {
        return this.assetBalanceAccountingMap;
    }

    /**
     * Хранит Удостоверенные публичные ключи для персон.
     * Тут block.getHeight + transaction index  - это ссылка на транзакцию создавшую данную заметку<br>
     *
     * <b>Ключ:</b> person key<br>

     * <b>Значение:</b><br>
     TreeMap(<br>
     (String)address - публичный счет,<br>
     Stack((Integer)end_date - дата окончания действия удостоврения,<br>
     (Integer)block.getHeight - номер блока,<br>
     (Integer)transaction index - номер транзакции в блоке<br>
     ))
     */
// TODO: ссылку на ЛОНГ
    public PersonAddressMap getPersonAddressMap() {
        return this.personAddressMap;
    }

    /**
     * Хранит Удостоверения персон для заданного публичного ключа.
     * address -> Stack person + end_date + block.height + transaction.reference.
     * Тут block.getHeight + transaction index  - это ссылка на транзакцию создавшую данную заметку<br>
     *
     * <b>Ключ:</b> (String)publickKey<br>

     * <b>Значение:</b><br>
     Stack((Long)person key,
     (Integer)end_date - дата окончания действия удостоврения,<br>
     (Integer)block.getHeight - номер блока,<br>
     (Integer)transaction index - номер транзакции в блоке<br>
     ))
     */
// TODO укротить до 20 байт адрес и ссылку на Long
    public AddressPersonMap getAddressPersonMap() {
        return this.addressPersonMap;
    }

    /**
     * Назначает статус для актива. Использует схему карты Ключ + Ключ - Значение: KKMap,
     * в котрой по ключу ищем значение там карта по ключу еще и
     * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись

     * @return dcMap
     */
    public KKAssetStatusMap getAssetStatusMap() {
        return this.kKAssetStatusMap;
    }

    /**
     * Назначает статус для персоны. Использует схему карты Ключ + Ключ - Значение: KKMap,
     * в котрой по ключу ищем значение там карта по ключу еще и
     * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись.<br>
     *     <br>

     key: (Long)PERSON <br>
     Value:<br>
     TreeMap<(Long) STATUS
     Stack(Tuple5(
     (Long) beg_date,
     (Long)end_date,

     (byte[]) any additional data,

     Integer,
     Integer
     ))

     * @return dcMap
     */
    public KKPersonStatusMap getPersonStatusMap() {
        return this.kKPersonStatusMap;
    }

    /**
     * Назначает статус для актива. Использует схему карты Ключ + Ключ - Значение: KKMap,
     * в котрой по ключу ищем значение там карта по ключу еще и
     * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись

     * @return dcMap
     */
    public KKUnionStatusMap getUnionStatusMap() {
        return this.kKUnionStatusMap;
    }

    /**
     * Назначает актив для объединения. Использует схему карты Ключ + Ключ - Значение: KKMap,
     * в котрой по ключу ищем значение там карта по ключу еще и
     * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись

     * @return dcMap
     */
    public KKAssetUnionMap getAssetUnionMap() {
        return this.kKAssetUnionMap;
    }

    /**
     * Назначает персон для объединения. Использует схему карты Ключ + Ключ - Значение: KKMap,
     * в котрой по ключу ищем значение там карта по ключу еще и
     * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись

     * @return dcMap
     */
    public KKPersonUnionMap getPersonUnionMap() {
        return this.kKPersonUnionMap;
    }

    /**
     * Назначает голосования для объединения. Использует схему карты Ключ + Ключ - Значение: KKMap,
     * в котрой по ключу ищем значение там карта по ключу еще и
     * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись

     * @return dcMap
     */
    public KKPollUnionMap getPollUnionMap() {
        return this.kKPollUnionMap;
    }

    /**
     * Назначает статус для объединения. Использует схему карты Ключ + Ключ - Значение: KKMap,
     * в котрой по ключу ищем значение там карта по ключу еще и
     * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись

     * @return dcMap
     */
    public KKStatusUnionMap getStatusUnionMap() {
        return this.kKStatusUnionMap;
    }

    /**
     * Назначает статус для объединения. Использует схему карты Ключ + Ключ - Значение: KK_К_Map,
     * в котрой по ключу ищем значение там еще карта по ключу.
     * Результат это Стэк из значений Конец, Номер Блока, подпись транзакции

     * @return dcMap
     */
    public KKKMapPersonStatusUnion getPersonStatusUnionMap() {
        return this.kK_KPersonStatusUnionMapPersonStatusUnionTable;
    }

    /**
     * Заверение другой транзакции<br><br>
     * Ключ: ссылка на запись которую заверяем.<br>
     * Значение: Сумма ERA на момент заверения на счету заверителя + ссылка на запись заверения:<br>
     vouched record (BlockNo, RecNo) -> ERM balabce + List of vouchers records
     * @return dcMap
     */
    public VouchRecordMap getVouchRecordMap() {
        return this.vouchRecordMap;
    }

    /**
     * Для поиска по хешу в транзакции множества хешей - саму запись
     * // found by hash -> record signature
     *
     * Ключ: хэш пользователя
     * Значение: ссылка на запись
     *
     * @return
     */
    public HashesMap getHashesMap() {
        return this.hashesMap;
    }

    /** Набор хэшей - по хэшу поиск записи в котрой он участвует и
     * используется в транзакции org.erachain.core.transaction.RHashes
     hash[byte] -> Stack person + block.height + transaction.seqNo

     * Ключ: хэш<br>
     * Значение: список - номер персоны (Если это персона создала запись, ссылка на запись)<br>
     // TODO укротить до 20 байт адрес и ссылку на Long
     * @return
     */
    public HashesSignsMap getHashesSignsMap() {
        return this.hashesSignsMap;
    }

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
    public BlockMap getBlockMap() {
        return this.blockMap;
    }

    /**
     * ключ: подпись блока
     * занчение: номер блока (высота, height)<br>

     * TODO - убрать длинный индекс
     *
     * @return
     */
    public BlockSignsMap getBlockSignsMap() {
        return this.blockSignsMap;
    }

    /**
     *  Block Height -> Block.BlockHead - заголовок блока влючая все что вычислено <br>
     *
     *  + FACE - version, creator, signature, transactionsCount, transactionsHash<br>
     *  + parentSignature<br>
     *  + Forging Data - Forging Value, Win Value, Target Value<br>
     *
     *
     */
    public BlocksHeadsMap getBlocksHeadsMap() {
        return this.blocksHeadsMap;
    }

    /**
     * TODO: Надо подумать может она лишняя??
     * seek reference to tx_Parent by address+timestamp
     * account.address -> <tx2.parentTimestamp>
     *
     */
    public ReferenceMap getReferenceMap() {
        return this.referenceMap;
    }
    
    /**
     * Транзакции занесенные в цепочку
     *
     * block.id + tx.ID in this block -> transaction
     *
     * Вторичные ключи:
     * ++ sender_txs
     * ++ recipient_txs
     * ++ address_type_txs
     */
    public TransactionFinalMap getTransactionFinalMap() {
        return this.transactionFinalMap;
    }

    /**
     * Храним вычисленные транзакции - для отображения в отчетах - пока нигде не используется - на будущее
     *
     * Ключ: ссылка на запись Родитель + Номер Актива - хотя наверное по Активу это во вторичные ключи
     * Значение: Сама Вычисленная транзакция
     * block.id + tx.ID in this block -> transaction
     *
     * Вторичные ключи по:
     * ++ sender_txs
     * ++ recipient_txs
     * ++ address_type_txs
     */
    public TransactionFinalCalculatedMap getTransactionFinalCalculatedMap() {
        return this.transactionFinalCalculatedMap;
    }

    /**
     * Поиск по подписи ссылки на трнзакыию
     * signature -> <BlockHeoght, Record No>
     */
    public TransactionFinalMapSigns getTransactionFinalMapSigns() {
        return this.transactionFinalMapSigns;
    }

    /**
     * Храним неподтвержденные транзакции - memory pool for unconfirmed transaction
     *
     * Также хранит инфо каким пирам мы уже разослали транзакцию неподтвержденную так что бы при подключении делать автоматически broadcast
     *
     * signature -> Transaction
     * TODO: укоротить ключ до 8 байт
     *
     * ++ seek by TIMESTAMP
     */
    public TransactionMap getTransactionMap() {
        return this.transactionMap;
    }

    public NameMap getNameMap() {
        return this.nameMap;
    }

    public NameStorageMap getNameStorageMap() {
        return this.nameStorageMap;
    }

    public OrphanNameStorageMap getOrphanNameStorageMap() {
        return this.orphanNameStorageMap;
    }

    public SharedPostsMap getSharedPostsMap() {
        return this.sharedPostsMap;
    }

    public PostCommentMap getPostCommentMap() {
        return this.postCommentMap;
    }

    public CommentPostMap getCommentPostMap() {
        return this.commentPostMap;
    }

    public OrphanNameStorageHelperMap getOrphanNameStorageHelperMap() {
        return this.orphanNameStorageHelperMap;
    }

    /**
     * я так понял - это отслеживание версии базы данных - и если она новая то все удаляем и заново закачиваем/
     * Сейчас не используется вроде ни как
     */
    public LocalDataMap getLocalDataMap() {
        return this.localDataMap;
    }

    /**
     * для создания постов - не используется
     * @return
     */
    public BlogPostMap getBlogPostMap() {
        return this.blogPostMap;
    }

    /**
     * для создания постов - не используется
     * @return
     */
    public HashtagPostMap getHashtagPostMap() {
        return this.hashtagPostMap;
    }

    /**
     * для Имен - не используется в транзакциях сейчас
     */
    public NameExchangeMap getNameExchangeMap() {
        return this.nameExchangeMap;
    }

    public UpdateNameMap getUpdateNameMap() {
        return this.updateNameMap;
    }

    public CancelSellNameMap getCancelSellNameMap() {
        return this.cancelSellNameMap;
    }

    public PollMap getPollMap() {
        return this.pollMap;
    }


    public VoteOnPollMap getVoteOnPollMap() {
        return this.voteOnPollMap;
    }

    /**
     * Храним выбор голосующего по Сущности Голования
     * POLL KEY + OPTION KEY + ACCOUNT SHORT = result Transaction reference (BlockNo + SeqNo)
     * byte[] - un CORAMPABLE
     *
     * Ключ: Номер Голосвания + Номер выбора + Счет Короткий
     * Значение: СТЭК ссылок на трнзакцию голосвания
     *
     * TODO: передлать ссылку на запись на Лонг
     * TODO: передлать короткий Счет на байты
     */
    public VoteOnItemPollMap getVoteOnItemPollMap() {
        return this.voteOnItemPollMap;
    }

    /************************************** ITEMS *************************************/

    /**
     * Хранение активов.<br>
     * Ключ: номер (автоинкремент)<br>
     * Значение: Актив<br>
     */
    public ItemAssetMap getItemAssetMap() {
        return this.itemAssetMap;
    }

    /**
     * see datachain.IssueItemMap
     *
     * @return
     */
    public IssueAssetMap getIssueAssetMap() {
        return this.issueAssetMap;
    }

    /**
     * Хранение ордеров на бирже
     * Ключ: ссылка на запись создавшую заказ
     * Значение: Ордер
     *
     * @return
     */
    public OrderMap getOrderMap() {
        return this.orderMap;
    }

    /**
     * Хранит исполненные ордера, или отмененные - все что уже не активно<br>
     * <br>
     * Ключ: ссылка на запись создания заказа<br>
     * Значение: заказ<br>
     */
    public CompletedOrderMap getCompletedOrderMap() {
        return this.completedOrderMap;
    }

    /**
     * Хранит сделки на бирже
     * Ключ: ссылка на иницатора + ссылка на цель
     * Значение - Сделка
     Initiator DBRef (Long) + Target DBRef (Long) -> Trade
     */
    public TradeMap getTradeMap() {
        return this.tradeMap;
    }

    public ItemImprintMap getItemImprintMap() {
        return this.itemImprintMap;
    }

/**
 * see datachain.IssueItemMap
 *
 * @return
 */
    public IssueImprintMap getIssueImprintMap() {
        return this.issueImprintMap;
    }

    /**
     * Хранение активов.<br>
     * Ключ: номер (автоинкремент)<br>
     * Значение: Шаблон<br>
     */
    public ItemTemplateMap getItemTemplateMap() {
        return this.itemTemplateMap;
    }

    /**
     * see datachain.IssueItemMap
     *
     * @return
     */
    public IssueTemplateMap getIssueTemplateMap() {
        return this.issueTemplateMap;
    }

    public ItemStatementMap getItemStatementMap() {
        return this.itemStatementMap;
    }

    /**
     * see datachain.IssueItemMap
     *
     * @return
     */
    public IssueStatementMap getIssueStatementMap() {
        return this.issueStatementMap;
    }

    /**
     * see datachain.ItemMap
     *
     * @return
     */
    public ItemPersonMap getItemPersonMap() {
        return this.itemPersonMap;
    }

    /**
     * see datachain.IssueItemMap
     *
     * @return
     */
    public IssuePersonMap getIssuePersonMap() {
        return this.issuePersonMap;
    }

    /**
     * see datachain.ItemMap
     *
     * @return
     */
    public ItemPollMap getItemPollMap() {
        return this.itemPollMap;
    }

    /**
     * see datachain.IssueItemMap
     *
     * @return
     */
    public IssuePollMap getIssuePollMap() {
        return this.issuePollMap;
    }

    /**
     * see datachain.ItemMap
     *
     * @return
     */
    public ItemStatusMap getItemStatusMap() {
        return this.itemStatusMap;
    }

    /**
     * see datachain.IssueItemMap
     *
     * @return
     */
    public IssueStatusMap getIssueStatusMap() {
        return this.issueStatusMap;
    }

    /**
     * see datachain.ItemMap
     *
     * @return
     */
    public ItemUnionMap getItemUnionMap() {
        return this.itemUnionMap;
    }

    /**
     * see datachain.IssueItemMap
     *
     * @return
     */
    public IssueUnionMap getIssueUnionMap() {
        return this.issueUnionMap;
    }

    /**
     * Селектор таблицы по типу Сущности
     * @param type тип Сущности
     * @return
     */
    public ItemMap getItem_Map(int type) {

        switch (type) {
            case ItemCls.ASSET_TYPE: {
                return this.getItemAssetMap();
            }
            case ItemCls.IMPRINT_TYPE: {
                return this.getItemImprintMap();
            }
            case ItemCls.TEMPLATE_TYPE: {
                return this.getItemTemplateMap();
            }
            case ItemCls.PERSON_TYPE: {
                return this.getItemPersonMap();
            }
            case ItemCls.POLL_TYPE: {
                return this.getItemPollMap();
            }
            case ItemCls.STATUS_TYPE: {
                return this.getItemStatusMap();
            }
            case ItemCls.UNION_TYPE: {
                return this.getItemUnionMap();
            }
        }
        return null;
    }

    public DCMap getMap(Class type) {

        if(type == Transaction.class) {
            return this.getTransactionFinalMap();

        } else if(type == Block.class) {
            return this.getBlockMap();

        } else if(type == Block.BlockHead.class) {
            return this.getBlocksHeadsMap();

        } else if(type == AssetCls.class) {
            return this.getItemAssetMap();

        } else if (type == PersonCls.class) {
            return this.getItemPersonMap();

        } else if (type == PollCls.class) {
            return this.getItemPollMap();

        } else if (type == StatusCls.class) {
            return this.getItemStatusMap();

        } else if (type == ImprintCls.class) {
            return this.getItemImprintMap();

        } else if (type == UnionCls.class) {
            return this.getItemUnionMap();

        } else if (type == TemplateCls.class) {
            return this.getItemTemplateMap();
        }
        return null;
    }

    public ATMap getATMap() {
        return this.atMap;
    }

    public ATStateMap getATStateMap() {
        return this.atStateMap;
    }

    public ATTransactionMap getATTransactionMap() {
        return this.atTransactionMap;
    }

    static Random randFork = new Random();
    private static DB getHardBase() {
        //OPEN DB

        // найдем новый не созданный уже файл
        File dbFile;
        do {
            dbFile = new File(Settings.getInstance().getDataTempDir(), "fork" + randFork.nextInt() + ".dat");
        } while (dbFile.exists());

        dbFile.getParentFile().mkdirs();

        /// https://jankotek.gitbooks.io/mapdb/performance/
        //CREATE DATABASE
        DB database = DBMaker.newFileDB(dbFile)

                .deleteFilesAfterClose()
                .transactionDisable()

                ////// ТУТ вряд ли нужно КЭШИРОВАТь при чтении что-либо
                //////
                // это чистит сама память если соталось 25% от кучи - так что она безопасная
                // у другого типа КЭША происходит утечка памяти
                //.cacheHardRefEnable()
                //.cacheLRUEnable()
                ///.cacheSoftRefEnable()
                .cacheWeakRefEnable()

                // количество точек в таблице которые хранятся в HashMap как в КЭШе
                // - начальное значени для всех UNBOUND и максимальное для КЭШ по умолчанию
                // WAL в кэш на старте закатывает все значения - ограничим для быстрого старта
                .cacheSize(10)

                .checksumEnable()
                .mmapFileEnableIfSupported() // ++ but -- error on asyncWriteEnable
                .commitFileSyncDisable() // ++

                //.snapshotEnable()
                //.asyncWriteEnable()
                //.asyncWriteFlushDelay(100)
                //.cacheHardRefEnable()

                // если при записи на диск блока процессор сильно нагружается - то уменьшить это
                .freeSpaceReclaimQ(7) // не нагружать процессор для поиска свободного места в базе данных

                //.compressionEnable()

                /*
                .cacheSize(CASH_SIZE)
                //.checksumEnable()
                .cacheHardRefEnable()
                .commitFileSyncDisable()
                //.asyncWriteEnable() ///
                //.asyncWriteFlushDelay(1000) //
                //.mmapFileEnableIfSupported()
                 */
                .make();

        return database;
    }

    /**
     * создать форк
     * @return
     */
    public DCSet fork() {
        this.addUses();

        try {
            DCSet fork = new DCSet(this, getHardBase());

            this.outUses();
            return fork;

        } catch (java.lang.OutOfMemoryError e) {
            LOGGER.error(e.getMessage(), e);

            this.outUses();

            Controller.getInstance().stopAll(13);
            return null;
        }

    }

    @Override
    public void close() {
        if (this.database != null) {
            // THIS IS not FORK
            if (!this.database.isClosed()) {
                this.addUses();

                if (this.getBlockMap().isProcessing()) {
                    this.database.rollback();
                    // not need on close!
                    // getBlockMap().resetLastBlockSignature();
                } else {
                    this.database.commit();
                }

                this.database.close();

                this.uses = 0;
            }
        }
    }

    @Override
    public void commit() {
        this.actions += 100;
    }

    public void rollback() {
        this.addUses();
        this.database.rollback();
        getBlockMap().resetLastBlockSignature();
        this.actions = 0l;
        this.outUses();
    }

    private long poinFlush = System.currentTimeMillis();
    private long poinCompact = poinFlush;
    private long engineSize;
    public void flush(int size, boolean hardFlush) {

        if (parent != null)
            return;

        this.addUses();


        this.actions += size;
        if (hardFlush || this.actions > ACTIONS_BEFORE_COMMIT
                || getEngineSize() - engineSize > MAX_ENGINE_BEFORE_COMMIT_KB
                || System.currentTimeMillis() - poinFlush > 3600000) {
            long start = poinFlush = System.currentTimeMillis();
            LOGGER.debug("%%%%%%%%%%%%%%%  UP SIZE: " + (getEngineSize() - engineSize) + "   %%%%% actions: " + actions);

            this.database.getEngine().clearCache();

            this.database.commit();

            if (System.currentTimeMillis() - poinCompact > TIME_COMPACT_DB
                    || transactionMap.totalDeleted > 200000) {
                poinCompact = System.currentTimeMillis();
                LOGGER.debug("try COMPACT");
                // очень долго делает - лучше ключем при старте
                try {
                    this.database.compact();
                    transactionMap.totalDeleted = 0;
                    LOGGER.debug("COMPACTED");
                } catch (Exception e) {
                    transactionMap.totalDeleted >>= 1;
                    LOGGER.error(e.getMessage(), e);
                }
            }

            LOGGER.debug("%%%%%%%%%%%%%%%%%% " + DCSet.getInstance().getEngineSize() + "   %%%%%%  commit time: "
                    + (System.currentTimeMillis() - start) / 1000);

            this.actions = 0l;
            this.engineSize = getEngineSize();

        }

        this.outUses();
    }

    @Override
    public void update(Observable o, Object arg) {
    }

    public long getEngineSize() {

        return this.database.getEngine().preallocate();

    }
    
    public String toString() {
        return (this.isFork()? "forked " : "")  + super.toString();
    }

}
