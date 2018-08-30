package database.wallet;
// 30/03 ++

import core.account.PublicKeyAccount;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.imprints.ImprintCls;
import core.item.persons.PersonCls;
import core.item.polls.PollCls;
import core.item.statuses.StatusCls;
import core.item.templates.TemplateCls;
import core.item.unions.UnionCls;
import datachain.IDB;
import org.mapdb.Atomic.Var;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import settings.Settings;

import java.io.File;

//import org.mapdb.Serializer;

public class DWSet implements IDB {
    private final File WALLET_FILE;

    private static final String VERSION = "version";
    private static final String LAST_BLOCK = "lastBlock";

    private DB database;
    private int uses;

    private AccountMap accountMap;
    private AccountsPropertisMap accountsPropertisMap;
    private TransactionMap transactionMap;
    private BlocksHeadMap blocksHeadMap;
    private NameMap nameMap;
    private NameSaleMap nameSaleMap;
    private PollMap pollMap;
    private WItemAssetMap assetMap;
    private WItemImprintMap imprintMap;
    private WItemTemplateMap TemplateMap;
    private WItemPersonMap personMap;
    private WItemStatusMap statusMap;
    private WItemUnionMap unionMap;
    private WItemPollMap wPollMap;
    private OrderMap orderMap;
    private FavoriteItemAsset assetFavoritesSet;
    private FavoriteItemTemplate templateFavoritesSet;
    private FavoriteItemImprint imprintFavoritesSet;
    private FavoriteItemPoll pollFavoriteSet;

    private FavoriteItemPerson personFavoritesSet;
    private FavoriteItemStatus statusFavoritesSet;
    private FavoriteItemUnion unionFavoritesSet;

    private FavoriteDocument statementFavoritesSet;

    public DWSet() {
        //OPEN WALLET
    	WALLET_FILE = new File(Settings.getInstance().getWalletDir(), "wallet.dat");
        WALLET_FILE.getParentFile().mkdirs();

        //DELETE TRANSACTIONS
        //File transactionFile = new File(Settings.getInstance().getWalletDir(), "wallet.dat.t");
        //transactionFile.delete();

        this.database = DBMaker.newFileDB(WALLET_FILE)
                .closeOnJvmShutdown()
                //.cacheSize(2048)
                //.cacheDisable()
                .checksumEnable()
                .mmapFileEnableIfSupported()
                /// ICREATOR
                .commitFileSyncDisable()
                .make();

        uses = 0;

        this.accountMap = new AccountMap(this, this.database);
        this.accountsPropertisMap = new AccountsPropertisMap(this, this.database);
        this.transactionMap = new TransactionMap(this, this.database);
        this.blocksHeadMap = new BlocksHeadMap(this, this.database);
        this.nameMap = new NameMap(this, this.database);
        this.nameSaleMap = new NameSaleMap(this, this.database);
        this.pollMap = new PollMap(this, this.database);
        this.assetMap = new WItemAssetMap(this, this.database);
        this.imprintMap = new WItemImprintMap(this, this.database);
        this.TemplateMap = new WItemTemplateMap(this, this.database);
        this.personMap = new WItemPersonMap(this, this.database);
        this.statusMap = new WItemStatusMap(this, this.database);
        this.unionMap = new WItemUnionMap(this, this.database);
        this.wPollMap = new WItemPollMap(this, this.database);
        this.orderMap = new OrderMap(this, this.database);
        this.assetFavoritesSet = new FavoriteItemAsset(this, this.database);
        this.templateFavoritesSet = new FavoriteItemTemplate(this, this.database);
        this.imprintFavoritesSet = new FavoriteItemImprint(this, this.database);
        this.pollFavoriteSet = new FavoriteItemPoll(this, this.database);
        this.personFavoritesSet = new FavoriteItemPerson(this, this.database);
        this.statusFavoritesSet = new FavoriteItemStatus(this, this.database);
        this.unionFavoritesSet = new FavoriteItemUnion(this, this.database);
        this.statementFavoritesSet = new FavoriteDocument(this, this.database);

    }

    public boolean exists() {
        return WALLET_FILE.exists();
    }

    public int getVersion() {
        this.uses++;
        int u = this.database.getAtomicInteger(VERSION).intValue();
        this.uses--;
        return u;
    }

    public void setVersion(int version) {
        this.uses++;
        this.database.getAtomicInteger(VERSION).set(version);
        this.uses--;
    }

    public void addUses() {
        this.uses++;

    }

    public void outUses() {
        this.uses--;
    }

    public boolean isBusy() {
        if (this.uses > 0) {
            return true;
        } else {
            return false;
        }
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

    public PollMap getPollMap() {
        return this.pollMap;
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

    public WItemPollMap wPollMap() {
        return this.wPollMap;
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
            return this.wPollMap;
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
                return this.wPollMap;

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

    public FavoriteItemAsset getAssetFavoritesSet() {
        return this.assetFavoritesSet;
    }

    public FavoriteItemTemplate getTemplateFavoritesSet() {
        return this.templateFavoritesSet;
    }

    public FavoriteItemImprint getImprintFavoritesSet() {
        return this.imprintFavoritesSet;
    }

    public FavoriteItemPoll getPollFavoritesSet() {
        return this.pollFavoriteSet;
    }


    public FavoriteItemPerson getPersonFavoritesSet() {
        return this.personFavoritesSet;
    }

    public FavoriteDocument getDocumentFavoritesSet() {
        return this.statementFavoritesSet;
    }

    public FavoriteItemStatus getStatusFavoritesSet() {
        return this.statusFavoritesSet;
    }

    public FavoriteItemUnion getUnionFavoritesSet() {
        return this.unionFavoritesSet;
    }

    public FavoriteItem getItemFavoritesSet(ItemCls item) {
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

    public void delete(PublicKeyAccount account) {
        this.uses++;

        this.accountMap.delete(account);
        this.blocksHeadMap.delete(account);
        this.transactionMap.delete(account);
        this.nameMap.delete(account);
        this.nameSaleMap.delete(account);
        this.pollMap.delete(account);
        this.assetMap.delete(account);
        this.imprintMap.delete(account);
        this.TemplateMap.delete(account);
        this.unionMap.delete(account);
        this.wPollMap.delete(account);
        this.personMap.delete(account);
        this.statusMap.delete(account);
        this.orderMap.delete(account);
        this.accountsPropertisMap.delete(account.getAddress());

        this.uses--;

    }

    public void commit() {
        this.uses++;
        this.database.commit();
        this.uses--;

    }

    public void close() {
        if (this.database != null) {
            if (!this.database.isClosed()) {
                this.uses++;
                //this.database.rollback();
                this.database.commit();
                this.database.close();
                this.uses--;

            }
        }
    }
}