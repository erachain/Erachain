package org.erachain.database.wallet;
// 30/03 ++

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBASet;
import org.erachain.datachain.DCSet;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.json.simple.JSONObject;
import org.mapdb.Atomic.Var;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;

@Slf4j
public class DWSet extends DBASet {

    /**
     * New version will auto-rebase DCSet from empty db file
     */
    final static int CURRENT_VERSION = 531; // vers 5.3.02 (trade.type)

    private static final Logger LOGGER = LoggerFactory.getLogger(DWSet.class);

    private static final String LAST_BLOCK = "lastBlock";

    public final DCSet dcSet;

    private Var<Long> licenseKeyVar;
    private Long licenseKey;

    private AccountMap accountMap;
    private FavoriteAccountsMap favoriteAccountsMap;
    private WTransactionMap transactionMap;
    private BlocksHeadMap blocksHeadMap;
    private WItemAssetMap assetMap;
    private WItemImprintMap imprintMap;
    private WItemTemplateMap TemplateMap;
    private WItemPersonMap personMap;
    private WItemStatusMap statusMap;
    private WItemUnionMap unionMap;
    private WItemPollMap pollMap;
    private OrderMap orderMap;

    private FavoriteTransactionMap transactionFavoritesSet;

    private FavoriteItemMapAsset assetFavoritesSet;
    private FavoriteItemMapImprint imprintFavoritesSet;
    private FavoriteItemMapPerson personFavoritesSet;
    private FavoriteItemMapPoll pollFavoriteSet;
    private FavoriteItemMapStatus statusFavoritesSet;
    private FavoriteItemMapTemplate templateFavoritesSet;
    private FavoriteItemMapUnion unionFavoritesSet;

    private FavoriteDocument statementFavoritesSet;

    private TelegramsMap telegramsMap;

    public DWSet(DCSet dcSet, File dbFile, DB database, boolean withObserver, boolean dynamicGUI) {
        super(dbFile, database, withObserver, dynamicGUI);

        this.dcSet = dcSet;

        // LICENCE SIGNED
        licenseKeyVar = database.getAtomicVar("licenseKey");
        licenseKey = licenseKeyVar.get();

        this.accountMap = new AccountMap(this, this.database);
        this.favoriteAccountsMap = new FavoriteAccountsMap(this, this.database);
        this.transactionMap = new WTransactionMap(this, this.database);
        this.blocksHeadMap = new BlocksHeadMap(this, this.database);
        this.assetMap = new WItemAssetMap(this, this.database);
        this.imprintMap = new WItemImprintMap(this, this.database);
        this.TemplateMap = new WItemTemplateMap(this, this.database);
        this.personMap = new WItemPersonMap(this, this.database);
        this.statusMap = new WItemStatusMap(this, this.database);
        this.unionMap = new WItemUnionMap(this, this.database);
        this.pollMap = new WItemPollMap(this, this.database);
        this.orderMap = new OrderMap(this, this.database);

        this.transactionFavoritesSet = new FavoriteTransactionMap(this, this.database);
        this.assetFavoritesSet = new FavoriteItemMapAsset(this, this.database);
        this.templateFavoritesSet = new FavoriteItemMapTemplate(this, this.database);
        this.imprintFavoritesSet = new FavoriteItemMapImprint(this, this.database);
        this.pollFavoriteSet = new FavoriteItemMapPoll(this, this.database);
        this.personFavoritesSet = new FavoriteItemMapPerson(this, this.database);
        this.statusFavoritesSet = new FavoriteItemMapStatus(this, this.database);
        this.unionFavoritesSet = new FavoriteItemMapUnion(this, this.database);
        this.statementFavoritesSet = new FavoriteDocument(this, this.database);
        this.telegramsMap = new TelegramsMap(this, this.database);

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

        //DELETE TRANSACTIONS
        //File transactionFile = new File(Settings.getInstance().getWalletDir(), "wallet.dat.t");
        //transactionFile.delete();

        DB database = DBMaker.newFileDB(dbFile)
                // убрал .closeOnJvmShutdown() it closing not by my code and rise errors! closed before my closing
                //.cacheSize(2048)

                //// иначе кеширует блок и если в нем удалить транзакции или еще что то выдаст тут же такой блок с пустыми полями
                ///// добавил dcSet.clearCache(); --
                ///.cacheDisable()

                // это чистит сама память если соталось 25% от кучи - так что она безопасная
                // у другого типа КЭША происходит утечка памяти
                //.cacheHardRefEnable()
                //.cacheLRUEnable()
                ///.cacheSoftRefEnable()
                .cacheWeakRefEnable() // analog new WeakReference() - в случае нехватки ппамяти кеш сам чистится

                // количество точек в таблице которые хранятся в HashMap как в КЭШе
                .cacheSize(1 << 14)

                .checksumEnable()
                .mmapFileEnableIfSupported() // ++

                // вызывает java.io.IOError: java.io.IOException: Запрошенную операцию нельзя выполнить для файла с открытой пользователем сопоставленной секцией
                // на ситема с Виндой в момент синхронизации кошелька когда там многот транзакций для этого кошелька
                .commitFileSyncDisable() // ++

                //.asyncWriteFlushDelay(30000)

                // если при записи на диск блока процессор сильно нагружается - то уменьшить это
                .freeSpaceReclaimQ(10) // не нагружать процессор для поиска свободного места в базе данных

                .mmapFileEnablePartial()
                //.compressionEnable()

                .make();

        if (isNew)
            DBASet.setVersion(database, CURRENT_VERSION);

        return database;

    }

    public synchronized static DWSet reCreateDB(DCSet dcSet, boolean withObserver, boolean dynamicGUI) {

        //OPEN DB
        File dbFile = new File(Settings.getInstance().getDataWalletPath(), "wallet.dat");

        DB database = null;
        try {
            database = makeFileDB(dbFile);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
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

        return new DWSet(dcSet, dbFile, database, withObserver, dynamicGUI);

    }

    public Long getLicenseKey() {
        return this.licenseKey;
    }

    public void setLicenseKey(Long key) {

        this.licenseKey = key;
        this.licenseKeyVar.set(this.licenseKey);

    }

    public byte[] getLastBlockSignature() {
        this.uses++;
        Var<byte[]> atomic = this.database.getAtomicVar(LAST_BLOCK);
        byte[] u = atomic.get();
        this.uses--;
        return u;
    }

    public void setLastBlockSignature(byte[] signature) {
        this.uses++;
        Var<byte[]> atomic = this.database.getAtomicVar(LAST_BLOCK);
        atomic.set(signature);
        this.uses--;
    }

    public AccountMap getAccountMap() {
        return this.accountMap;
    }

    public FavoriteAccountsMap getFavoriteAccountsMap() {
        return this.favoriteAccountsMap;
    }

    /**
     * Транзакции относящиеся к моим счетам. Сюда же записываться должны и неподтвержденные<br>
     * А когда они подтверждаются они будут перезаписываться поверх.
     * Тогда неподтвержденные будут показывать что они не сиполнились.
     * И их пользователь сможет сам удалить вручную или командой - удалить все неподтвержденные
     * <hr>
     * Ключ: счет + подпись<br>
     * Значение: транзакция
     *
     * @return TransactionMap
     */
    public WTransactionMap getTransactionMap() {
        return this.transactionMap;
    }

    public BlocksHeadMap getBlocksHeadMap() {
        return this.blocksHeadMap;
    }

    public WItemAssetMap getAssetMap() {
        return this.assetMap;
    }

    public WItemImprintMap getImprintMap() {
        return this.imprintMap;
    }

    public WItemTemplateMap getTemplateMap() {
        return this.TemplateMap;
    }

    public WItemPersonMap getPersonMap() {
        return this.personMap;
    }

    public WItemStatusMap getStatusMap() {
        return this.statusMap;
    }

    public WItemUnionMap getUnionMap() {
        return this.unionMap;
    }

    public WItemPollMap getPollMap() {
        return this.pollMap;
    }

    public WItemMap getItemMap(ItemCls item) {
        if (item instanceof AssetCls) {
            return this.assetMap;
        } else if (item instanceof ImprintCls) {
            return this.imprintMap;
        } else if (item instanceof TemplateCls) {
            return this.TemplateMap;
        } else if (item instanceof PersonCls) {
            return this.personMap;
        } else if (item instanceof StatusCls) {
            return this.statusMap;
        } else if (item instanceof UnionCls) {
            return this.unionMap;
        } else if (item instanceof PollCls) {
            return this.pollMap;
        } else {
            return null;
        }
    }

    public void putItem(ItemCls item) {
        getItemMap(item).put(item.getKey(), item);
    }

    public void deleteItem(ItemCls item) {
        getItemMap(item).delete(item.getKey());
    }

    public WItemMap getItemMap(int type) {
        switch (type) {
            case ItemCls.ASSET_TYPE:
                return this.assetMap;
            case ItemCls.IMPRINT_TYPE:
                return this.imprintMap;
            case ItemCls.TEMPLATE_TYPE:
                return this.TemplateMap;
            case ItemCls.PERSON_TYPE:
                return this.personMap;
            case ItemCls.POLL_TYPE:
                return this.pollMap;
            case ItemCls.STATUS_TYPE:
                return this.statusMap;
            case ItemCls.UNION_TYPE:
                return this.unionMap;
        }
        return null;
    }

    public OrderMap getOrderMap() {
        return this.orderMap;
    }

    public FavoriteTransactionMap getTransactionFavoritesSet() {
        return this.transactionFavoritesSet;
    }

    public FavoriteItemMapAsset getAssetFavoritesSet() {
        return this.assetFavoritesSet;
    }

    public FavoriteItemMapTemplate getTemplateFavoritesSet() {
        return this.templateFavoritesSet;
    }

    public FavoriteItemMapImprint getImprintFavoritesSet() {
        return this.imprintFavoritesSet;
    }

    public FavoriteItemMapPoll getPollFavoritesSet() {
        return this.pollFavoriteSet;
    }


    public FavoriteItemMapPerson getPersonFavoritesSet() {
        return this.personFavoritesSet;
    }

    public FavoriteDocument getDocumentFavoritesSet() {
        return this.statementFavoritesSet;
    }

    public FavoriteItemMapStatus getStatusFavoritesSet() {
        return this.statusFavoritesSet;
    }

    public FavoriteItemMapUnion getUnionFavoritesSet() {
        return this.unionFavoritesSet;
    }

    public FavoriteItemMap getItemFavoritesSet(ItemCls item) {
        if (item instanceof AssetCls) {
            return assetFavoritesSet;
        } else if (item instanceof ImprintCls) {
            return imprintFavoritesSet;
        } else if (item instanceof PollCls) {
            return pollFavoriteSet;
        } else if (item instanceof TemplateCls) {
            return templateFavoritesSet;
        } else if (item instanceof PersonCls) {
            return personFavoritesSet;
        } else if (item instanceof StatusCls) {
            return statusFavoritesSet;
        } else if (item instanceof UnionCls) {
            return unionFavoritesSet;
        } else {
            return null;
        }
    }

    public void addItemFavorite(ItemCls item) {
        getItemFavoritesSet(item).add(item.getKey());
    }

    public void deleteItemFavorite(ItemCls item) {
        getItemFavoritesSet(item).delete(item.getKey());
    }

    //////////////// FAVORITES ///////////
    public void addDocumentToFavorite(Transaction transaction) {
        getDocumentFavoritesSet().add(transaction.getDBRef());
    }

    public void removeDocumentFromFavorite(Transaction transaction) {
        getDocumentFavoritesSet().delete(transaction.getDBRef());
    }

    public boolean isDocumentFavorite(Transaction transaction) {
        if (transaction.getDBRef() > 0) {
            return getDocumentFavoritesSet().contains(transaction.getDBRef());
        }
        return false;
    }

    public void addTransactionToFavorite(Transaction transaction) {
        getTransactionFavoritesSet().add(transaction.getDBRef());
    }

    public void removeTransactionFromFavorite(Transaction transaction) {
        getTransactionFavoritesSet().delete(transaction.getDBRef());
    }

    public boolean isTransactionFavorite(Transaction transaction) {
        if (transaction.getDBRef() > 0) {
            return getTransactionFavoritesSet().contains(transaction.getDBRef());
        }
        return false;
    }

    public void addAddressFavorite(String address, String pubKey, String name, String description) {
        if (getFavoriteAccountsMap().contains(address))
            return;

        JSONObject json = new JSONObject();
        json.put("description", description);

        getFavoriteAccountsMap().put(address, new Fun.Tuple3<>(pubKey, name, json.toJSONString()));
    }

    public void addItemToFavorite(ItemCls item) {
        getItemFavoritesSet(item).add(item.getKey());
    }

    public void removeItemFromFavorite(ItemCls item) {
        getItemFavoritesSet(item).delete(item.getKey());
    }

    public boolean isItemFavorite(ItemCls item) {
        return getItemFavoritesSet(item).contains(item.getKey());
    }

    public TelegramsMap getTelegramsMap() {
        return this.telegramsMap;
    }

    long commitPoint;

    public synchronized void hardFlush() {
        this.uses++;
        this.database.commit();
        this.uses--;

        commitPoint = System.currentTimeMillis();
    }

    @Override
    public void commit() {
        if (this.uses != 0
                || System.currentTimeMillis() - commitPoint < 10000
        )
            return;

        hardFlush();
    }

    /**
     * закрываем без коммита! - чтобы при запуске продолжитть?
     */
    @Override
    public void close() {

        if (this.database == null || this.database.isClosed())
            return;

        Controller.getInstance().getWallet().synchronizeBodyUsed = false;

        int step = 0;
        while (uses > 0 && ++step < 100) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }

        }

        this.uses++;
        this.database.commit();
        this.database.close();
        this.tables = null;
        this.uses--;

    }

}