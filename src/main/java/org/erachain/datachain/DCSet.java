package org.erachain.datachain;
// upd 09/03

import lombok.extern.slf4j.Slf4j;
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
import org.erachain.dbs.DBTab;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOError;
import java.nio.file.Files;
import java.util.Random;

/**
 * набор таблиц. Поидее тут нужно хранить список таблиц и ссылку на родителя при Форке базы.
 * Но почемуто парент хранится в каждой таблице - хотя там сразу ссылка на форкнутую таблицу есть
 * а в ней уже хранится объект набора DCSet
 */
@Slf4j
public class DCSet extends DBASet implements Closeable {

    final static int CURRENT_VERSION = 535;

    private static final Logger LOGGER = LoggerFactory.getLogger(DCSet.class);
    /**
     * Используется для отладки - где незакрытый набор таблиц остался.
     * Делаем дамн КУЧИ в VisualVM и там в параметрах смотрим откуда этот объект был создан
     */
    public String makedIn = "--";

    private static final int ACTIONS_BEFORE_COMMIT = BlockChain.MAX_BLOCK_SIZE_GEN
            << (Controller.getInstance().databaseSystem == DBS_MAP_DB ? 1 : 3);
    // если все на Рокс перевели то меньше надо ставить
    private static final long MAX_ENGINE_BEFORE_COMMIT_KB = 999999999999999L; ///BlockChain.MAX_BLOCK_SIZE_BYTES_GEN >> 5;
    private static final long TIME_COMPACT_DB = 1L * 24L * 3600000L;
    public static final long DELETIONS_BEFORE_COMPACT = (long) ACTIONS_BEFORE_COMMIT;

    /**
     * Включает подсчет количество в основной таблице трнзакций или в Таблице с подписями
     */
    static private boolean SIZE_ENABLE_IN_FINAL = true;

    // эти настройки по умолчанию при ФАСТ режиме пойдут

    /**
     * DBS_MAP_DB - fast, DBS_ROCK_DB - slow
     */
    public static final int BLOCKS_MAP = DBS_ROCK_DB;
    public static final int BLOCKS_MAP_FORK = DBS_NATIVE_MAP;
    /**
     * DBS_MAP_DB - slow then DBS_ROCK_DB
     */
    public static final int FINAL_TX_MAP = DBS_ROCK_DB;
    public static final int FINAL_TX_MAP_FORK = DBS_NATIVE_MAP;

    /**
     * DBS_MAP_DB - fast, DBS_ROCK_DB - slow
     */
    public static final int FINAL_TX_SIGNS_MAP = DBS_MAP_DB;
    public static final int FINAL_TX_SIGNS_MAP_FORK = DBS_MAP_DB;

    /**
     * DBS_MAP_DB - slow, DBS_ROCK_DB - crash, DBS_MAP_DB_IN_MEM - fast
     * нельзя делать DBS_NATIVE_MAP !!! - так как он не удаляет транзакции по вторичному индексу
     * И трнзакции копятся пока полностью не будут удалены скопом при FLUSH что тормозит время
     * блока на проверке и исполнении
     */
    public static final int UNCONF_TX_MAP = DBS_MAP_DB_IN_MEM;;
    public static final int UNCONF_TX_MAP_FORK = DBS_MAP_DB_IN_MEM;

    /**
     * DBS_MAP_DB - good, DBS_ROCK_DB - very SLOW потому что BigDecimal 20 байт - хотя с -opi это не делаем
     */
    public static final int ACCOUNT_BALANCES = DBS_MAP_DB;
    public static final int ACCOUNT_BALANCES_FORK = DBS_NATIVE_MAP;

    /**
     * DBS_MAP_DB - fast, DBS_ROCK_DB - slow
     */
    public static final int ACCOUNTS_REFERENCES = DBS_MAP_DB;

    public static final int ORDERS_MAP = DBS_MAP_DB;
    public static final int COMPLETED_ORDERS_MAP = DBS_ROCK_DB;
    public static final int TRADES_MAP = DBS_MAP_DB;
    public static final int PAIRS_MAP = DBS_MAP_DB;

    /**
     * если задано то выбран такой КЭШ который нужно самим чистить иначе реперолнение будет
     */
    private static final boolean needClearCache = false;

    private static boolean isStoped = false;
    private volatile static DCSet instance;
    private DCSet parent;

    private boolean inMemory = false;

    private BlockChain bchain;

    private AddressForging addressForging;
    private TimeRoyaltyMap timeRoyaltyMap;

    private CreditAddressesMap credit_AddressesMap;
    private ItemAssetBalanceMap assetBalanceMap;
    private AddressStatementRefs addressStatement_Refs;
    private KKAssetStatusMap kKAssetStatusMap;
    private KKPersonStatusMap kKPersonStatusMap;
    //private KKPollStatusMap kKPollStatusMap;
    private KKUnionStatusMap kKUnionStatusMap;
    private KKAssetUnionMap kKAssetUnionMap;
    private KKPersonUnionMap kKPersonUnionMap;
    private KKPollUnionMap kKPollUnionMap;
    private KKStatusUnionMap kKStatusUnionMap;
    private AddressPersonMapImpl addressPersonMap;
    private PersonAddressMap personAddressMap;
    private KKKMapPersonStatusUnion kK_KPersonStatusUnionMapPersonStatusUnionTable;

    private VouchRecordMap vouchRecordMap;
    private ExLinksMap exLinksMap;
    private HashesMap hashesMap;
    private HashesSignsMap hashesSignsMap;

    private BlocksMapImpl blockMap;
    private BlockSignsMap blockSignsMap;
    private BlocksHeadsMap blocksHeadsMap;
    private ReferenceMapImpl referenceMap;
    private NameStorageMap nameStorageMap;
    private OrphanNameStorageMap orphanNameStorageMap;
    private OrphanNameStorageHelperMap orphanNameStorageHelperMap;
    private SharedPostsMap sharedPostsMap;
    private PostCommentMap postCommentMap;
    private CommentPostMap commentPostMap;
    private LocalDataMap localDataMap;
    private BlogPostMap blogPostMap;
    private HashtagPostMap hashtagPostMap;
    private VoteOnItemPollMap voteOnItemPollMap;
    private ItemAssetMap itemAssetMap;
    private IssueAssetMap issueAssetMap;
    private OrderMapImpl orderMap;
    private CompletedOrderMap completedOrderMap;
    private TradeMapImpl tradeMap;
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
    private TransactionFinalMapImpl transactionFinalMap;
    private TransactionFinalCalculatedMap transactionFinalCalculatedMap;
    private TransactionFinalMapSigns transactionFinalMapSigns;
    private TransactionMapImpl transactionTab;

    private long actions = (long) (Math.random() * (ACTIONS_BEFORE_COMMIT >> 1));

    /**
     *
     * @param dbFile
     * @param database общая база данных для данного набора - вообще надо ее в набор свтавить и все.
     *               У каждой таблицы внутри может своя база данных открытьваться.
     *               А команды базы данных типа close commit должны из таблицы передаваться в свою.
     *               Если в общей базе таблица, то не нужно обработка так как она делается в наборе наверху
     * @param withObserver
     * @param dynamicGUI
     * @param inMemory
     * @param defaultDBS
     */
    public DCSet(File dbFile, DB database, boolean withObserver, boolean dynamicGUI, boolean inMemory, int defaultDBS) {
        super(dbFile, database, withObserver, dynamicGUI);

        LOGGER.info("UP SIZE BEFORE COMMIT [KB]: " + MAX_ENGINE_BEFORE_COMMIT_KB
                + ", ACTIONS BEFORE COMMIT: " + ACTIONS_BEFORE_COMMIT
                + ", DELETIONS BEFORE COMPACT: " + DELETIONS_BEFORE_COMPACT);

        this.engineSize = getEngineSize();
        this.inMemory = inMemory;

        try {
            // переделанные таблицы
            this.assetBalanceMap = new ItemAssetBalanceMapImpl(defaultDBS != DBS_FAST ? defaultDBS :
                    ACCOUNT_BALANCES
                    , this, database);

            this.transactionFinalMap = new TransactionFinalMapImpl(defaultDBS != DBS_FAST ? defaultDBS :
                    FINAL_TX_MAP
                    , this, database, SIZE_ENABLE_IN_FINAL);

            this.transactionTab = new TransactionMapImpl(UNCONF_TX_MAP, this, database);

            this.referenceMap = new ReferenceMapImpl(defaultDBS != DBS_FAST ? defaultDBS :
                    ACCOUNTS_REFERENCES
                    , this, database);

            this.blockMap = new BlocksMapImpl(defaultDBS != DBS_FAST ? defaultDBS :
                    BLOCKS_MAP
                    , this, database);

            this.transactionFinalMapSigns = new TransactionFinalMapSignsImpl(defaultDBS != DBS_FAST ? defaultDBS :
                    FINAL_TX_SIGNS_MAP
                    , this, database, !SIZE_ENABLE_IN_FINAL);

            this.orderMap = new OrderMapImpl(defaultDBS != DBS_FAST ? defaultDBS :
                    ORDERS_MAP
                    , this, database);

            this.completedOrderMap = new CompletedOrderMapImpl(defaultDBS != DBS_FAST ? defaultDBS :
                    COMPLETED_ORDERS_MAP
                    , this, database);

            this.tradeMap = new TradeMapImpl(defaultDBS != DBS_FAST ? defaultDBS :
                    TRADES_MAP
                    , this, database);

            this.addressPersonMap = new AddressPersonMapImpl(defaultDBS != DBS_FAST ? defaultDBS : DBS_MAP_DB, this, database);

            this.actions = 0L;

            this.blockSignsMap = new BlockSignsMap(this, database);
            this.blocksHeadsMap = new BlocksHeadsMap(this, database);

            this.addressForging = new AddressForging(this, database);
            this.timeRoyaltyMap = new TimeRoyaltyMap(this, database);

            this.credit_AddressesMap = new CreditAddressesMap(this, database);
            this.addressStatement_Refs = new AddressStatementRefs(this, database);

            this.kKAssetStatusMap = new KKAssetStatusMap(this, database);
            this.kKPersonStatusMap = new KKPersonStatusMap(this, database);
            //this.kKPollStatusMap = new KKPollStatusMap(this, database);
            this.kKUnionStatusMap = new KKUnionStatusMap(this, database);
            this.kKAssetUnionMap = new KKAssetUnionMap(this, database);
            this.kKPersonUnionMap = new KKPersonUnionMap(this, database);
            this.kKPollUnionMap = new KKPollUnionMap(this, database);
            this.kKStatusUnionMap = new KKStatusUnionMap(this, database);
            this.personAddressMap = new PersonAddressMap(this, database);
            this.kK_KPersonStatusUnionMapPersonStatusUnionTable = new KKKMapPersonStatusUnion(this, database);
            this.transactionFinalCalculatedMap = new TransactionFinalCalculatedMap(this, database);

            this.vouchRecordMap = new VouchRecordMap(this, database);
            this.exLinksMap = new ExLinksMap(this, database);
            this.hashesMap = new HashesMap(this, database);
            this.hashesSignsMap = new HashesSignsMap(this, database);
            this.nameStorageMap = new NameStorageMap(this, database);
            this.orphanNameStorageMap = new OrphanNameStorageMap(this, database);
            this.orphanNameStorageHelperMap = new OrphanNameStorageHelperMap(this, database);

            this.sharedPostsMap = new SharedPostsMap(this, database);
            this.postCommentMap = new PostCommentMap(this, database);
            this.commentPostMap = new CommentPostMap(this, database);
            this.localDataMap = new LocalDataMap(this, database);
            this.blogPostMap = new BlogPostMap(this, database);
            this.hashtagPostMap = new HashtagPostMap(this, database);
            this.voteOnItemPollMap = new VoteOnItemPollMap(this, database);

            this.itemAssetMap = new ItemAssetMap(this, database);
            this.issueAssetMap = new IssueAssetMap(this, database);

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
            LOGGER.error(e.getMessage(), e);
            this.close();
            throw e;
        }

        if (false // теперь отклучаем счетчики для усклрения работы - отсвили только в Подписи
                &&this.blockMap.size() != this.blocksHeadsMap.size()
                || this.blockSignsMap.size() != this.blocksHeadsMap.size()) {
            LOGGER.info("reset DATACHAIN on height error (blockMap, blockSignsMap, blocksHeadsMap: "
                    + this.blockMap.size() + " != "
                    + this.blockSignsMap.size() + " != " + this.blocksHeadsMap.size());

            this.close();
            this.actions = -1;

        }
        uses--;

    }

    /**
     * Make data set as Fork
     *
     * @param parent     parent DCSet
     * @param idDatabase
     */
    protected DCSet(DCSet parent, DB idDatabase) {

        if (Runtime.getRuntime().maxMemory() == Runtime.getRuntime().totalMemory()) {
            // System.out.println("########################### Free Memory:"
            // + Runtime.getRuntime().freeMemory());
            if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() >> 10)
                    + (Controller.MIN_MEMORY_TAIL)) {
                // у родителя чистим - у себя нет, так как только создали
                parent.clearCache();
                System.gc();
                if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() >> 10)
                        + (Controller.MIN_MEMORY_TAIL << 1)) {
                    logger.error("Heap Memory Overflow");
                    Controller.getInstance().stopAll(1091);
                    return;
                }
            }
        }

        this.addUses();

        this.database = idDatabase;
        this.parent = parent;
        ///this.database = parent.database.snapshot();
        this.bchain = parent.bchain;

        // переделанные поновой таблицы
        this.assetBalanceMap = new ItemAssetBalanceMapImpl(
                ACCOUNT_BALANCES_FORK
                , parent.assetBalanceMap, this);
        this.transactionTab = new TransactionMapImpl(
                UNCONF_TX_MAP_FORK
                , parent.transactionTab, this);
        this.transactionFinalMap = new TransactionFinalMapImpl(
                FINAL_TX_MAP_FORK
                , parent.transactionFinalMap, this);
        this.referenceMap = new ReferenceMapImpl(
                DBS_NATIVE_MAP
                , parent.referenceMap, this);

        this.blockMap = new BlocksMapImpl(
                BLOCKS_MAP_FORK
                , parent.blockMap, this);

        this.transactionFinalMapSigns = new TransactionFinalMapSignsImpl(
                FINAL_TX_SIGNS_MAP_FORK
                , parent.transactionFinalMapSigns, this, true);

        this.orderMap = new OrderMapImpl(
                DBS_MAP_DB
                //DBS_ROCK_DB
                //DBS_NATIVE_MAP
                , parent.orderMap, this);
        this.completedOrderMap = new CompletedOrderMapImpl(
                DBS_MAP_DB
                //DBS_ROCK_DB
                //DBS_NATIVE_MAP
                , parent.completedOrderMap, this);
        this.tradeMap = new TradeMapImpl(
                DBS_MAP_DB
                //DBS_ROCK_DB
                //DBS_NATIVE_MAP
                , parent.tradeMap, this);

        this.addressPersonMap = new AddressPersonMapImpl(DBS_MAP_DB, parent.addressPersonMap, this);


        this.addressForging = new AddressForging(parent.addressForging, this);
        this.timeRoyaltyMap = new TimeRoyaltyMap(parent.timeRoyaltyMap, this);

        this.credit_AddressesMap = new CreditAddressesMap(parent.credit_AddressesMap, this);
        this.addressStatement_Refs = new AddressStatementRefs(parent.addressStatement_Refs, this);
        this.kKAssetStatusMap = new KKAssetStatusMap(parent.kKAssetStatusMap, this);
        this.kKPersonStatusMap = new KKPersonStatusMap(parent.kKPersonStatusMap, this);
        this.kKUnionStatusMap = new KKUnionStatusMap(parent.kKUnionStatusMap, this);
        this.kKAssetUnionMap = new KKAssetUnionMap(parent.kKAssetUnionMap, this);
        this.kKPersonUnionMap = new KKPersonUnionMap(parent.kKPersonUnionMap, this);
        this.kKPollUnionMap = new KKPollUnionMap(parent.kKPollUnionMap, this);
        this.kKStatusUnionMap = new KKStatusUnionMap(parent.kKStatusUnionMap, this);

        this.personAddressMap = new PersonAddressMap(parent.personAddressMap, this);
        this.kK_KPersonStatusUnionMapPersonStatusUnionTable = new KKKMapPersonStatusUnion(parent.kK_KPersonStatusUnionMapPersonStatusUnionTable, this);
        this.transactionFinalCalculatedMap = new TransactionFinalCalculatedMap(parent.transactionFinalCalculatedMap, this);
        this.vouchRecordMap = new VouchRecordMap(parent.vouchRecordMap, this);
        this.exLinksMap = new ExLinksMap(parent.exLinksMap, this);
        this.hashesMap = new HashesMap(parent.hashesMap, this);
        this.hashesSignsMap = new HashesSignsMap(parent.hashesSignsMap, this);

        this.blockSignsMap = new BlockSignsMap(parent.blockSignsMap, this);
        this.blocksHeadsMap = new BlocksHeadsMap(parent.blocksHeadsMap, this);
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

        //this.pollMap = new PollMap(parent.pollMap);
        //this.voteOnPollMap = new VoteOnPollMap(parent.voteOnPollMap);

        this.voteOnItemPollMap = new VoteOnItemPollMap(parent.voteOnItemPollMap, this);

        this.itemAssetMap = new ItemAssetMap(parent.itemAssetMap, this);
        this.issueAssetMap = new IssueAssetMap(parent.getIssueAssetMap(), this);

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
     * @param dynamicGUI   [true] - for switch on GUI observers fir dynamic interface
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
     * @return
     */
    public static DCSet getInstance() {
        return instance;
    }

    /**
     * Создание файла для основной базы данных
     *
     * @param dbFile
     * @return
     */
    public static DB makeFileDB(File dbFile) {

        boolean isNew = !dbFile.exists();
        if (isNew) {
            dbFile.getParentFile().mkdirs();
        }

        /// https://jankotek.gitbooks.io/mapdb/performance/
        DBMaker databaseStruc = DBMaker.newFileDB(dbFile)
                // убрал .closeOnJvmShutdown() it closing not by my code and rise errors! closed before my closing

                .checksumEnable()
                .mmapFileEnableIfSupported() // ++ but -- error on asyncWriteEnable

                // тормозит сильно но возможно когда файл большеой не падает скорость сильно
                // вдобавок не сохраняет на диск даже Транзакционный файл и КРАХ теряет данные
                // НЕ ВКЛЮЧАТЬ!
                // .mmapFileEnablePartial()

                // Если этот отключить (закомментировать) то файлы на лету не обновляются на диске а только в момент Флуша
                // типа быстрее работают но по факту с Флушем нет и в описании предупрежджение - что
                // при крахе системы в момент флуша можно потерять данные - так как в Транзакционный фал изменения
                // катаются так же в момент флуша - а не при изменении данных
                // так что этот ключ тут полезный
                .commitFileSyncDisable() // ++

                //.snapshotEnable()
                //.asyncWriteEnable() - крах при коммитах и откатах тразакций - возможно надо asyncWriteFlushDelay больше задавать
                .asyncWriteFlushDelay(2)

                // если при записи на диск блока процессор сильно нагружается - то уменьшить это
                .freeSpaceReclaimQ(BlockChain.TEST_DB > 0 ? 3 : 7)// не нагружать процессор для поиска свободного места в базе данных

                //.compressionEnable()
                ;

        /**
         * если не задавать вид КЭШа то берется стандартный - и его размер 10 очень мал и скорость
         * решения боков в 2-5 раза меньше. Однако если разделить таблицы по разным базам так чтобы блоки особо не кэшировать.
         * Тогда возможно этот вид КЭШа будет приемлем для дранзакций
         * == количество точек в таблице которые хранятся в HashMap как в КЭШе
         * - начальное значени для всех UNBOUND и максимальное для КЭШ по умолчанию
         * WAL в кэш на старте закатывает все значения - ограничим для быстрого старта
         */

        if (true) {
            // USE CACHE
            if (BLOCKS_MAP != DBS_MAP_DB) {
                // если блоки не сохраняются в общей базе данных
                databaseStruc
                        .cacheSize(1024 << (5 + Controller.HARD_WORK));
            } else {
                if (needClearCache) {
                    //// иначе кеширует блок и если в нем удалить трнзакции или еще что то выдаст тут же такой блок с пустыми полями
                    ///// добавил dcSet.clearCache(); --
                    databaseStruc
                            .cacheSize(32 + 32 << Controller.HARD_WORK)
                    ;

                } else if (BlockChain.TEST_DB > 0) {
                    databaseStruc

                            // при норм размере и досточной памяти скорость не хуже чем у остальных
                            //.cacheLRUEnable() // скорость зависит от памяти и настроек -
                            //.cacheSize(2048 + 64 << Controller.HARD_WORK)

                            // это чистит сама память если соталось 25% от кучи - так что она безопасная
                            // у другого типа КЭША происходит утечка памяти
                            .cacheHardRefEnable() // самый быстрый

                    ///.cacheSoftRefEnable()
                    ///.cacheSize(32 << Controller.HARD_WORK)

                    // analog new WeakReference() - в случае нехватки ппамяти кеш сам чистится
                    ///.cacheWeakRefEnable() // new WeakReference()
                    ///.cacheSize(32 << Controller.HARD_WORK)
                    ;

                } else {
                    databaseStruc

                            // при норм размере и досточной памяти скорость не хуже чем у остальных
                            //.cacheLRUEnable() // скорость зависит от памяти и настроек -
                            //.cacheSize(2048 + 64 << Controller.HARD_WORK)

                            // это чистит сама память если соталось 25% от кучи - так что она безопасная
                            // у другого типа КЭША происходит утечка памяти
                            ///.cacheHardRefEnable()

                            ///.cacheSoftRefEnable()
                            ///.cacheSize(32 << Controller.HARD_WORK)

                            // analog new WeakReference() - в случае нехватки ппамяти кеш сам чистится
                            .cacheWeakRefEnable() // new WeakReference()
                    ///.cacheSize(32 << Controller.HARD_WORK)
                    ;

                }
            }
        } else {
            databaseStruc.cacheDisable();
        }

        DB database = databaseStruc.make();
        if (isNew)
            DBASet.setVersion(database, CURRENT_VERSION);

        return database;

    }

    /**
     * Для проверкт одного блока в памяти - при добавлении в цепочку или в буфер ожидания
     *
     * @return
     */
    public static boolean needResetUTXPoolMap = false;
    public static DB makeDBinMemory() {

        // лучше для памяти ставить наилучшее сжатие чтобы память не кушать лишний раз
        int freeSpaceReclaimQ = 10;
        needResetUTXPoolMap = freeSpaceReclaimQ < 3;
        return DBMaker
                .newMemoryDB()
                .transactionDisable()
                .deleteFilesAfterClose()
                .asyncWriteEnable() // улучшает чуток и не падает так как нет транзакционности

                // это время добавляется к ожиданию конца - и если больше 100 то тормоз лишний
                // но 1..10 - увеличивает скорость валидации трнзакций!
                .asyncWriteFlushDelay(2)
                // тут не влияет .commitFileSyncDisable()

                //.cacheHardRefEnable()
                //.cacheLRUEnable()
                .cacheWeakRefEnable() // new WeakReference()
                //.cacheDisable()


                // если задано мене чем 3 то очитска записей при их удалении вобще не происходит - поэтому база раздувается в памяти без огрничений
                // в этом случае нужно ее закрывать удалять и заново открывать
                .freeSpaceReclaimQ(freeSpaceReclaimQ) // как-то слабо влияет в памяти
                //.compressionEnable() // как-то не влияет в памяти

                //
                //.newMemoryDirectDB()
                .make();
    }

    /**
     * remake data set
     *
     * @param withObserver [true] - for switch on GUI observers
     * @param dynamicGUI   [true] - for switch on GUI observers fir dynamic interface
     * @throws Exception
     */
    public static void reCreateDB(boolean withObserver, boolean dynamicGUI) throws Exception {

        //OPEN DB
        File dbFile = new File(Settings.getInstance().getDataChainPath(), "chain.dat");

        DB database = null;
        try {
            database = makeFileDB(dbFile);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            try {
                Files.walkFileTree(dbFile.getParentFile().toPath(),
                        new SimpleFileVisitorForRecursiveFolderDeletion());
            } catch (Throwable e1) {
                logger.error(e1.getMessage(), e1);
            }
            database = makeFileDB(dbFile);
        }

        if (DBASet.getVersion(database) < CURRENT_VERSION) {
            database.close();
            logger.warn("New Version: " + CURRENT_VERSION + ". Try remake datachain Set " + dbFile.getParentFile().toPath());
            try {
                Files.walkFileTree(dbFile.getParentFile().toPath(),
                        new SimpleFileVisitorForRecursiveFolderDeletion());
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
            database = makeFileDB(dbFile);

        }

        //CREATE INSTANCE
        instance = new DCSet(dbFile, database, withObserver, dynamicGUI, false, Controller.getInstance().databaseSystem);
        if (instance.actions < 0) {
            for (DBTab tab : instance.tables) {
                tab.clear();
            }
            database.close();
            dbFile.delete();
            throw new Exception("error in DATACHAIN:" + instance.actions);
        }

        // очистим полностью перед компактом
        if (Controller.getInstance().compactDConStart) {
            instance.getTransactionTab().clear();
            instance.database.commit();
            LOGGER.debug("try COMPACT");
            database.compact();
            LOGGER.debug("COMPACTED");
        }

    }

    public static void reCreateDBinMEmory(boolean withObserver, boolean dynamicGUI) {
        DB database = makeDBinMemory();

        instance = new DCSet(null, database, withObserver, dynamicGUI, true, Controller.getInstance().databaseSystem);

    }

        /**
         * make data set in memory. For tests
         *
         * @return
         * @param defaultDBS
         */
    public static DCSet createEmptyDatabaseSet(int defaultDBS) {
        DB database = DCSet.makeDBinMemory();

        instance = new DCSet(null, database, false, false, true, defaultDBS);
        return instance;
    }

    public static DCSet createEmptyHardDatabaseSet(DB database, int defaultDBS) {
        instance = new DCSet(null, database, false, false, true, defaultDBS);
        return instance;
    }

    public static DCSet createEmptyHardDatabaseSet(int defaultDBS) {
        instance = new DCSet(null, getHardBaseForFork(), false, false, true, defaultDBS);
        return instance;
    }

    public static DCSet createEmptyHardDatabaseSetWithFlush(String path, int defaultDBS) {
        // найдем новый не созданный уже файл
        File dbFile;
        do {
            dbFile = new File(path == null? Settings.getInstance().getDataTempDir() : path, "fork" + randFork.nextInt() + ".dat");
        } while (dbFile.exists());

        dbFile.getParentFile().mkdirs();

        instance = new DCSet(dbFile, makeFileDB(dbFile), false, false, true, defaultDBS);
        return instance;
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

        this.addressForging.clear();
        this.timeRoyaltyMap.clear();

        this.credit_AddressesMap.clear();
        this.assetBalanceMap.clear();
        this.addressStatement_Refs.clear();
        //this.assetBalanceAccountingMap.clear();
        this.kKAssetStatusMap.clear();
        this.kKPersonStatusMap.clear();
        this.kKUnionStatusMap.clear();
        this.kKAssetUnionMap.clear();
        this.kKPersonUnionMap.clear();
        this.kKPollUnionMap.clear();

        this.kKStatusUnionMap.clear();
        this.addressPersonMap.clear();
        this.personAddressMap.clear();
        this.kK_KPersonStatusUnionMapPersonStatusUnionTable.clear();
        this.vouchRecordMap.clear();
        this.exLinksMap.clear();
        this.hashesMap.clear();
        this.hashesSignsMap.clear();
        this.blockMap.clear();
        this.blockSignsMap.clear();
        this.blocksHeadsMap.clear();

        this.referenceMap.clear();
        this.transactionFinalMap.clear();
        this.transactionFinalCalculatedMap.clear();
        this.transactionFinalMapSigns.clear();
        this.transactionTab.clear();
        this.nameStorageMap.clear();
        this.orphanNameStorageMap.clear();
        this.orphanNameStorageHelperMap.clear();
        this.sharedPostsMap.clear();
        this.commentPostMap.clear();

        this.postCommentMap.clear();
        this.localDataMap.clear();
        this.blogPostMap.clear();
        this.hashtagPostMap.clear();
        this.voteOnItemPollMap.clear();

        this.tradeMap.clear();

        this.orderMap.clear();
        this.completedOrderMap.clear();
        this.issueAssetMap.clear();
        this.itemAssetMap.clear();
        this.issueImprintMap.clear();
        this.itemImprintMap.clear();
        this.issueTemplateMap.clear();
        this.itemStatementMap.clear();
        this.issueStatementMap.clear();
        this.itemTemplateMap.clear();

        this.issuePersonMap.clear();
        this.itemPersonMap.clear();
        this.issuePollMap.clear();
        this.itemPollMap.clear();
        this.issueStatusMap.clear();
        this.itemStatusMap.clear();
        this.issueUnionMap.clear();
        this.itemUnionMap.clear();
        this.atMap.clear();
        this.atStateMap.clear();
        this.atTransactionMap.clear();

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

    /**
     * Хранит данные о наградах за время
     * если номер Транзакции не задан - то это последнее значение.
     * Person.key + seqNo ->
     * previous making : seqNoPrev + previous Royalty Balance + this Royalty Balance
     * <hr>
     * Если точка первая то предыдущее в ней значение Высоты = 0, то есть указывает что ниже нету,
     * но текущей баланс уже есть для Форжинга
     *
     * @return
     */
    // TODO укротить до 20 байт адрес
    public TimeRoyaltyMap getTimeRoyaltyMap() {
        return this.timeRoyaltyMap;
    }

    /**
     * Общая сумма переданных средств в кредит на другой счет
     * Используется для проверки сумм которые отдаются или забираются у заемщика<br><br>
     *
     * <b>Ключ:</b> account.address Creditor + asset key + account.address Debtor<br>
     *
     * <b>Значение:</b> сумма средств
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
     * Ссылка на другую транзакцию<br><br>
     * Ключ: ссылка на эту транзакцию (с ссылкой).<br>
     * Значение: ExLink
     *
     * @return dcMap
     */
    public ExLinksMap getExLinksMap() {
        return this.exLinksMap;
    }


    /**
     * Для поиска по хешу в транзакции множества хешей - саму запись
     * // found by hash -> record signature
     * <p>
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
    public BlocksMapImpl getBlockMap() {
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
    public ReferenceMapImpl getReferenceMap() {
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
    public TransactionFinalMapImpl getTransactionFinalMap() {
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
    public TransactionMapImpl getTransactionTab() {
        return this.transactionTab;
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
    public OrderMapImpl getOrderMap() {
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
     * Initiator DBRef (Long) + Target DBRef (Long) -> Trade
     */
    public TradeMapImpl getTradeMap() {
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

    public DBTab getMap(Class type) {

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

    /**
     * Эта база используется для откатов, возможно глубоких
     *
     * @param dbFile
     * @return
     */
    public static DB getHardBaseForFork(File dbFile) {

        dbFile.getParentFile().mkdirs();

        /// https://jankotek.gitbooks.io/mapdb/performance/
        //CREATE DATABASE
        DB database = DBMaker.newFileDB(dbFile)

                // включим самоудаление после закрытия
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
                .cacheSize(2048)

                //.checksumEnable()
                .mmapFileEnableIfSupported() // ++ but -- error on asyncWriteEnable

                // тормозит сильно но возможно когда файл большеой не падает скорость сильно
                // вдобавок не сохраняет на диск даже Транзакционный файл и КРАХ теряет данные
                // НЕ ВКЛЮЧАТЬ!
                // .mmapFileEnablePartial()

                .commitFileSyncDisable() // ++

                //.snapshotEnable()
                .asyncWriteEnable() // тут нет Коммитов поэтому должно работать
                .asyncWriteFlushDelay(2)

                // если при записи на диск блока процессор сильно нагружается - то уменьшить это
                .freeSpaceReclaimQ(5) // не нагружать процессор для поиска свободного места в базе данных

                .make();

        return database;
    }

    public static DB getHardBaseForFork() {
        //OPEN DB

        // найдем новый не созданный уже файл
        File dbFile;
        do {
            dbFile = new File(Settings.getInstance().getDataTempDir(), "fork" + randFork.nextInt() + ".dat");
        } while (dbFile.exists());

        dbFile.getParentFile().mkdirs();

        return getHardBaseForFork(dbFile);
    }

    /**
     * создать форк
     *
     * @return
     */
    public DCSet fork(DB database, String maker) {
        this.addUses();

        try {
            DCSet fork = new DCSet(this, database);
            fork.makedIn = maker;

            this.outUses();
            return fork;

        } catch (java.lang.OutOfMemoryError e) {
            LOGGER.error(e.getMessage(), e);

            this.outUses();

            Controller.getInstance().stopAll(1113);
            return null;
        }

    }

    /**
     * USe inMemory MapDB Database
     *
     * @param maker
     * @return
     */
    public DCSet fork(String maker) {
        return fork(makeDBinMemory(), maker);
    }

    /**
     * Нужно незабыть переменные внктри каждой таблицы тоже в Родителя скинуть
     */
    @Override
    public synchronized void writeToParent() {

        // проверим сначала тут память чтобы посередине не вылететь
        if (Runtime.getRuntime().maxMemory() == Runtime.getRuntime().totalMemory()) {
            // System.out.println("########################### Free Memory:"
            // + Runtime.getRuntime().freeMemory());
            if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() >> 10)
                    + (Controller.MIN_MEMORY_TAIL)) {
                // у родителя чистим - у себя нет, так как только создали
                parent.clearCache();
                System.gc();
                if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() >> 10)
                        + (Controller.MIN_MEMORY_TAIL << 1)) {
                    logger.error("Heap Memory Overflow before commit");
                    Controller.getInstance().stopAll(9618);
                    return;
                }
            }
        }

        try {
            // до сброса обновим - там по Разсеру таблицы - чтобы не влияло новой в Родителе и а Форке
            // иначе размер больше будет в форке и не то значение
            ((BlockMap) blockMap.getParent()).setLastBlockSignature(blockMap.getLastBlockSignature());

            for (DBTab table : tables) {
                table.writeToParent();
            }
            // теперь нужно все общие переменные переопределить
        } catch (Exception e) {

            LOGGER.error(e.getMessage(), e);

            // база битая - выходим!! Хотя rollback должен сработать
            Controller.getInstance().stopAll(9613);
            return;
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);

            // база битая - выходим!! Хотя rollback должен сработать
            Controller.getInstance().stopAll(9615);
        }

    }

    @Override
    public synchronized void close() {

        if (this.database != null) {
            // THIS IS not FORK
            if (!this.database.isClosed()) {
                this.addUses();

                // если основная база то с откатом
                if (parent == null) {
                    if (this.getBlockMap().isProcessing()) {
                        LOGGER.debug("TRY ROLLBACK");
                        for (DBTab tab : tables) {
                            try {
                                tab.rollback();
                            } catch (IOError e) {
                                LOGGER.error(e.getMessage(), e);
                            }
                        }

                        try {
                            this.database.rollback();
                        } catch (IOError e) {
                            LOGGER.error(e.getMessage(), e);
                        }

                        // not need on close!
                        // getBlockMap().resetLastBlockSignature();
                    } else {
                        for (DBTab tab : tables) {
                            try {
                                tab.commit();
                            } catch (IOError e) {
                                LOGGER.error(tab.toString() + ": " + e.getMessage(), e);
                            }
                        }

                        try {
                            this.database.commit();
                        } catch (IOError e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }

                for (DBTab tab : tables) {
                    try {
                        tab.close();
                    } catch (IOError e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                // улучшает работу финализера
                tables = null;
                try {
                    this.database.close();
                } catch (IOError e) {
                    LOGGER.error(e.getMessage(), e);
                }
                // улучшает работу финализера
                this.database = null;

                this.uses = 0;
            }

            logger.info("closed " + (parent == null ? "Main" : "parent " + toString()));
        }

    }

    @Override
    protected void finalize() throws Throwable {
        close();
        if (BlockChain.CHECK_BUGS > 5) {
            LOGGER.debug("DCSet is FINALIZED: " + this.toString());
        }
        super.finalize();
    }

    @Override
    public void commit() {
        this.actions += 100;
    }

    public void rollback() {
        this.addUses();
        for (DBTab tab : tables) {
            tab.rollback();
        }

        this.database.rollback();

        getBlockMap().resetLastBlockSignature();

        for (DBTab tab : tables) {
            tab.afterRollback();
        }

        this.actions = 0l;
        this.outUses();
    }

    public void clearCache() {
        for (DBTab tab : tables) {
            tab.clearCache();
        }
        super.clearCache();
    }

    private long poinFlush = System.currentTimeMillis();
    private long poinCompact = poinFlush;
    private long engineSize;
    private long poinClear;
    private boolean clearGC = false;
    public void flush(int size, boolean hardFlush, boolean doOrphan) {

        if (parent != null)
            return;

        this.addUses();

        boolean needRepopulateUTX = hardFlush
                || System.currentTimeMillis() - poinClear - 1000 >
                BlockChain.GENERATING_MIN_BLOCK_TIME_MS(BlockChain.VERS_30SEC + 1) << 3;
        // try repopulate UTX table
        if (needRepopulateUTX && Controller.getInstance().transactionsPool != null) {
            Controller.getInstance().transactionsPool.needClear(doOrphan);

            poinClear = System.currentTimeMillis();

        }

        this.actions += size;
        long diffSizeEngine = getEngineSize() - engineSize;
        if (diffSizeEngine < 0)
            diffSizeEngine = -diffSizeEngine;

        if (hardFlush || this.actions > ACTIONS_BEFORE_COMMIT
                || diffSizeEngine > MAX_ENGINE_BEFORE_COMMIT_KB
                || System.currentTimeMillis() - poinFlush > BlockChain.GENERATING_MIN_BLOCK_TIME_MS(BlockChain.VERS_30SEC + 1) << 8
        ) {

            long start = System.currentTimeMillis();

            LOGGER.debug("%%%%%%%%%%%%%%%  UP SIZE: " + (getEngineSize() - engineSize) + "   %%%%% actions: " + actions
                    + (this.actions > ACTIONS_BEFORE_COMMIT ? "by Actions: " + this.actions :
                    (diffSizeEngine > MAX_ENGINE_BEFORE_COMMIT_KB ? "by diff Size Engine: " + diffSizeEngine : "by time"))
                );

            for (DBTab tab : tables) {
                tab.commit();
            }

            this.database.commit();

            if (false && Controller.getInstance().compactDConStart && System.currentTimeMillis() - poinCompact > 9999999) {
                // очень долго делает - лучше ключем при старте
                poinCompact = System.currentTimeMillis();

                LOGGER.debug("try COMPACT");
                // очень долго делает - лучше ключем при старте
                try {
                    this.database.compact();
                    transactionTab.setTotalDeleted(0);
                    LOGGER.debug("COMPACTED");
                } catch (Exception e) {
                    transactionTab.setTotalDeleted(transactionTab.getTotalDeleted() >> 1);
                    LOGGER.error(e.getMessage(), e);
                }
            }

            if (true) {
                // нельзя папку с базами форков чистить между записями блоков
                // так как еще другие процессы с форками есть - например создание своих трнзакций или своего блока
                // они же тоже тут создаются
                // а хотя тогда они и не удалятся - так как они не закрытые и сотанутся в папке... значит можно тут удалять - нужные не удлятся
                try {

                    // там же лежит и он
                    ///transactionTab.close();

                    // удалим все в папке Temp
                    File tempDir = new File(Settings.getInstance().getDataTempDir());
                    if (tempDir.exists()) {
                        Files.walkFileTree(tempDir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
                    }
                } catch (Throwable e) {
                    ///LOGGER.error(e.getMessage(), e);
                }

            }

            clearGC = !clearGC;
            if (clearGC) {
                if (true || needClearCache || clearGC) {
                    LOGGER.debug("CLEAR ENGINE CACHE...");
                    clearCache();
                }
                LOGGER.debug("CLEAR GC");
                System.gc();
            }

            LOGGER.debug("%%%%%%%%%%%%%%%%%% TOTAL: " + getEngineSize() + "   %%%%%%  commit time: "
                    + (System.currentTimeMillis() - start) + " ms");

            poinFlush = System.currentTimeMillis();
            this.actions = 0l;
            this.engineSize = getEngineSize();

        }

        this.outUses();
    }

    public long getEngineSize() {
        return this.database.getEngine().preallocate();
    }
    
    public String toString() {
        return (this.isFork() ? "forked in " + makedIn : "") + super.toString();
    }

}
