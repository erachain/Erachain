package org.erachain.database.wallet;
// 30/03 ++

import org.erachain.controller.Controller;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.database.DBASet;
import org.erachain.settings.Settings;
import org.mapdb.Atomic.Var;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;

public class DWSet extends DBASet {

    private static final String LAST_BLOCK = "lastBlock";

    private Var<Long> licenseKeyVar;
    private Long licenseKey;

    private AccountMap accountMap;
    private AccountsPropertisMap accountsPropertisMap;
    private TransactionMap transactionMap;
    private BlocksHeadMap blocksHeadMap;
    private NameMap nameMap;
    private NameSaleMap nameSaleMap;
    private PollMap pollMap_old;
    private WItemAssetMap assetMap;
    private WItemImprintMap imprintMap;
    private WItemTemplateMap TemplateMap;
    private WItemPersonMap personMap;
    private WItemStatusMap statusMap;
    private WItemUnionMap unionMap;
    private WItemPollMap pollMap;
    private OrderMap orderMap;
    private FavoriteItemMapAsset assetFavoritesSet;
    private FavoriteItemMapTemplate templateFavoritesSet;
    private FavoriteItemMapImprint imprintFavoritesSet;
    private FavoriteItemMapPoll pollFavoriteSet;
    private TelegramsMap telegramsMap;

    private FavoriteItemMapPerson personFavoritesSet;
    private FavoriteItemMapStatus statusFavoritesSet;
    private FavoriteItemMapUnion unionFavoritesSet;

    private FavoriteDocument statementFavoritesSet;

    public DWSet(File dbFile, DB database, boolean withObserver, boolean dynamicGUI) {
        super(dbFile, database, withObserver,  dynamicGUI);

        // LICENCE SIGNED
        this.licenseKeyVar = database.getAtomicVar("licenseKey");
        this.licenseKey = this.licenseKeyVar.get();

        this.accountMap = new AccountMap(this, this.database);
        this.accountsPropertisMap = new AccountsPropertisMap(this, this.database);
        this.transactionMap = new TransactionMap(this, this.database);
        this.blocksHeadMap = new BlocksHeadMap(this, this.database);
        this.nameMap = new NameMap(this, this.database);
        this.nameSaleMap = new NameSaleMap(this, this.database);
        this.pollMap_old = new PollMap(this, this.database);
        this.assetMap = new WItemAssetMap(this, this.database);
        this.imprintMap = new WItemImprintMap(this, this.database);
        this.TemplateMap = new WItemTemplateMap(this, this.database);
        this.personMap = new WItemPersonMap(this, this.database);
        this.statusMap = new WItemStatusMap(this, this.database);
        this.unionMap = new WItemUnionMap(this, this.database);
        this.pollMap = new WItemPollMap(this, this.database);
        this.orderMap = new OrderMap(this, this.database);
        this.assetFavoritesSet = new FavoriteItemMapAsset(this, this.database);
        this.templateFavoritesSet = new FavoriteItemMapTemplate(this, this.database);
        this.imprintFavoritesSet = new FavoriteItemMapImprint(this, this.database);
        this.pollFavoriteSet = new FavoriteItemMapPoll(this, this.database);
        this.personFavoritesSet = new FavoriteItemMapPerson(this, this.database);
        this.statusFavoritesSet = new FavoriteItemMapStatus(this, this.database);
        this.unionFavoritesSet = new FavoriteItemMapUnion(this, this.database);
        this.statementFavoritesSet = new FavoriteDocument(this, this.database);
        this.telegramsMap = new TelegramsMap(this,this.database);

    }

    public static DWSet reCreateDB(boolean withObserver, boolean dynamicGUI) {

        //OPEN WALLET
        File dbFile = new File(Settings.getInstance().getDataWalletDir(), "wallet.dat");
        dbFile.getParentFile().mkdirs();

        //DELETE TRANSACTIONS
        //File transactionFile = new File(Settings.getInstance().getWalletDir(), "wallet.dat.t");
        //transactionFile.delete();

        DB database = DBMaker.newFileDB(dbFile)
                // убрал .closeOnJvmShutdown() it closing not by my code and rise errors! closed before my closing
                //.cacheSize(2048)

                //// иначе кеширует блок и если в нем удалить трнзакции или еще что то выдаст тут же такой блок с пустыми полями
                ///// добавил dcSet.clearCache(); --
                ///.cacheDisable()

                // это чистит сама память если соталось 25% от кучи - так что она безопасная
                // у другого типа КЭША происходит утечка памяти
                //.cacheHardRefEnable()
                //.cacheLRUEnable()
                ///.cacheSoftRefEnable()
                .cacheWeakRefEnable()

                // количество точек в таблице которые хранятся в HashMap как в КЭШе
                .cacheSize(10000)

                .checksumEnable()
                .mmapFileEnableIfSupported() // ++

                // вызывает java.io.IOError: java.io.IOException: Запрошенную операцию нельзя выполнить для файла с открытой пользователем сопоставленной секцией
                // на ситема с Виндой в момент синхронизации кошелька когда там многот транзакций для этого кошелька
                .commitFileSyncDisable() // ++

                //.asyncWriteFlushDelay(30000)

                // если при записи на диск блока процессор сильно нагружается - то уменьшить это
                .freeSpaceReclaimQ(7) // не нагружать процессор для поиска свободного места в базе данных

                //.mmapFileEnablePartial()
                //.compressionEnable()

                .make();

        return new DWSet(dbFile, database, withObserver, dynamicGUI);

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

    public AccountsPropertisMap getAccountsPropertisMap() {
        return this.accountsPropertisMap;
    }

    public TransactionMap getTransactionMap() {
        return this.transactionMap;
    }

    public BlocksHeadMap getBlocksHeadMap() {
        return this.blocksHeadMap;
    }

    public NameMap getNameMap() {
        return this.nameMap;
    }

    public NameSaleMap getNameSaleMap() {
        return this.nameSaleMap;
    }

    @Deprecated
    public PollMap getPollMap_old() {
        return this.pollMap_old;
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

    public WItemPollMap getpollMap() {
        return this.pollMap;
    }

    public WItem_Map getItemMap(ItemCls item) {
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

    public WItem_Map getItemMap(int type) {
        switch (type) {
            case ItemCls.ASSET_TYPE:
                return this.assetMap;
            case ItemCls.IMPRINT_TYPE:
                return this.imprintMap;
            case ItemCls.TEMPLATE_TYPE:
                return this.TemplateMap;
            case ItemCls.PERSON_TYPE:
                return this.personMap;
            case ItemCls.STATUS_TYPE:
                return this.statusMap;
            case ItemCls.UNION_TYPE:
                return this.unionMap;
            case ItemCls.POLL_TYPE:
                return this.pollMap;

        }
        return null;
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

    public OrderMap getOrderMap() {
        return this.orderMap;
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
            return this.assetFavoritesSet;
        } else if (item instanceof ImprintCls) {
            return this.imprintFavoritesSet;
        } else if (item instanceof PollCls) {
            return this.pollFavoriteSet;
        } else if (item instanceof TemplateCls) {
            return this.templateFavoritesSet;
        } else if (item instanceof PersonCls) {
            return this.personFavoritesSet;
        } else if (item instanceof StatusCls) {
            return this.statusFavoritesSet;
        } else if (item instanceof UnionCls) {
            return this.unionFavoritesSet;
        } else {
            return null;
        }
        
          
    }

    public TelegramsMap getTelegramsMap() {
        return this.telegramsMap;
    }
    
    public void delete(PublicKeyAccount account) {
        this.uses++;

        this.accountMap.delete(account);
        this.blocksHeadMap.delete(account);
        this.transactionMap.delete(account);
        this.nameMap.delete(account);
        this.nameSaleMap.delete(account);
        this.pollMap_old.delete(account);
        this.assetMap.delete(account);
        this.imprintMap.delete(account);
        this.TemplateMap.delete(account);
        this.unionMap.delete(account);
        this.pollMap.delete(account);
        this.personMap.delete(account);
        this.statusMap.delete(account);
        this.orderMap.delete(account);
        this.accountsPropertisMap.delete(account.getAddress());
        this.telegramsMap.deleteFromAccount(account);
        this.uses--;

    }

    long commitPoint;

    @Override
    public synchronized void commit() {
        if (this.uses != 0
                || System.currentTimeMillis() - commitPoint < 50000
        )
            return;

        this.uses++;
        this.database.commit();
        this.uses--;

        commitPoint = System.currentTimeMillis();

    }

    /**
     * закрываем без коммита! - чтобы при запуске продолжитть?
     */
    @Override
    public void close() {

        if (this.database == null || this.database.isClosed())
            return;

        int step = 0;
        if (Controller.getInstance().wallet.synchronizeStatus) {
            // STOP syncronize Wallet
            Controller.getInstance().wallet.synchronizeBodyStop = true;

            while (Controller.getInstance().wallet.synchronizeStatus && ++step < 500) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }

            }
        }

        step = 0;
        while (uses > 0 && ++step < 100) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }

        }

        this.uses++;
        this.database.rollback();
        this.database.close();
        this.uses--;

    }

}