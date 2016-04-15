package database.wallet;
// 30/03 ++
import java.io.File;

import org.mapdb.Atomic.Var;
import org.mapdb.DB;
import org.mapdb.DBMaker;
//import org.mapdb.Serializer;

import database.IDB;
import qora.account.Account;
import qora.item.ItemCls;
import qora.item.assets.AssetCls;
import qora.item.notes.NoteCls;
import settings.Settings;

public class WalletDatabase implements IDB
{
	private static final File WALLET_FILE = new File(Settings.getInstance().getWalletDir(), "wallet.dat");
	
	private static final String VERSION = "version";
	private static final String LAST_BLOCK = "lastBlock";
	
	private DB database;	
	private AccountMap accountMap;
	private TransactionMap transactionMap;
	private BlockMap blockMap;
	private NameMap nameMap;
	private NameSaleMap nameSaleMap;
	private PollMap pollMap;
	private WItemAssetMap assetMap;
	private WItemNoteMap noteMap;
	private OrderMap orderMap;
	private FavoriteItemAsset assetFavoritesSet;
	private FavoriteItemNote noteFavoritesSet;
	
	public static boolean exists()
	{
		return WALLET_FILE.exists();
	}
	
	public WalletDatabase()
	{
		//OPEN WALLET
		WALLET_FILE.getParentFile().mkdirs();
		
		//DELETE TRANSACTIONS
		//File transactionFile = new File(Settings.getInstance().getWalletDir(), "wallet.dat.t");
		//transactionFile.delete();	
		
	    this.database = DBMaker.newFileDB(WALLET_FILE)
	    		.closeOnJvmShutdown()
	    		.cacheSize(2048)
	    		.checksumEnable()
	    		.mmapFileEnableIfSupported()
	            .make();
	    
	    this.accountMap = new AccountMap(this, this.database);
	    this.transactionMap = new TransactionMap(this, this.database);
	    this.blockMap = new BlockMap(this, this.database);
	    this.nameMap = new NameMap(this, this.database);
	    this.nameSaleMap = new NameSaleMap(this, this.database);
	    this.pollMap = new PollMap(this, this.database);
	    this.assetMap = new WItemAssetMap(this, this.database);
	    this.noteMap = new WItemNoteMap(this, this.database);
	    this.orderMap = new OrderMap(this, this.database);
	    this.assetFavoritesSet = new FavoriteItemAsset(this, this.database);
	    this.noteFavoritesSet = new FavoriteItemNote(this, this.database);
	}
	
	public void setVersion(int version)
	{
		this.database.getAtomicInteger(VERSION).set(version);
	}
	
	public int getVersion()
	{
		return this.database.getAtomicInteger(VERSION).intValue();
	}
	
	public void setLastBlockSignature(byte[] signature)
	{
		/*
		Var<byte[]> atomic;
		if(database.exists(LAST_BLOCK))
		{
			atomic = database.getAtomicVar(LAST_BLOCK);
		}
		else
		{
			atomic = database.createAtomicVar(LAST_BLOCK, new byte[0], Serializer.BYTE_ARRAY);
		}
		*/
		Var<byte[]> atomic = this.database.getAtomicVar(LAST_BLOCK);
		atomic.set(signature);
	}
	
	public byte[] getLastBlockSignature()
	{
		/*
		Var<byte[]> atomic;
		if(database.exists(LAST_BLOCK))
		{
			atomic = database.getAtomicVar(LAST_BLOCK);
		}
		else
		{
			atomic = database.createAtomicVar(LAST_BLOCK, new byte[0], Serializer.BYTE_ARRAY);
		}
		*/
		Var<byte[]> atomic = this.database.getAtomicVar(LAST_BLOCK);
		return atomic.get();
	}
	
	public AccountMap getAccountMap()
	{
		return this.accountMap;
	}
	
	public TransactionMap getTransactionMap()
	{
		return this.transactionMap;
	}
	
	public BlockMap getBlockMap()
	{
		return this.blockMap;
	}
	
	public NameMap getNameMap()
	{
		return this.nameMap;
	}
	
	public NameSaleMap getNameSaleMap()
	{
		return this.nameSaleMap;
	}
	
	public PollMap getPollMap()
	{
		return this.pollMap;
	}
	public WItemAssetMap getAssetMap()
	{
		return this.assetMap;
	}
	public WItemNoteMap getNoteMap()
	{
		return this.noteMap;
	}
	public WItem_Map getItemMap(ItemCls item)
	{
		if (item instanceof NoteCls) { 
			return (WItem_Map)this.noteMap;
		} else if (item instanceof AssetCls) { 
			return (WItem_Map)this.assetMap;
		} else {
			return null;
		}
	}
	public WItem_Map getItemMap(int type)
	{
		switch(type)
		{
			case ItemCls.ASSET_TYPE:
				return this.assetMap;
			case ItemCls.NOTE_TYPE:
				return this.noteMap;
		}
		return null;
	}

	public void addItemToFavorite(ItemCls item)
	{
		if (item instanceof AssetCls) { 
			this.assetFavoritesSet.add(item.getKey());
		} else if (item instanceof NoteCls) { 
			this.noteFavoritesSet.add(item.getKey());
		}
	}
	
	public void removeItemFromFavorite(ItemCls item) {
		if (item instanceof AssetCls) { 
			this.assetFavoritesSet.delete(item.getKey());
		} else if (item instanceof NoteCls) { 
			this.noteFavoritesSet.delete(item.getKey());
		}
	}
	public boolean isItemFavorite(ItemCls item) {
		if (item instanceof AssetCls) { 
			return this.assetFavoritesSet.contains(item.getKey());
		} else if (item instanceof NoteCls) { 
			return this.noteFavoritesSet.contains(item.getKey());
		}
		return false;
	}
	
	/*
	public boolean replace(ItemCls item)
	{
		//if(this.notesFavorites != null) {
		//	this.database.getNoteFavoritesSet().replace(this.notesFavorites.getKeys());	
		//}
		if (item instanceof NoteCls) { 
			return this.noteMap.replace(this.notesFavorites.getKeys();
		} else if (item instanceof AssetCls) { 
			return this.assetMap.replace(this.assetsFavorites.getKeys();
		} else {
			return false;
		}
	}
	*/


	public OrderMap getOrderMap()
	{
		return this.orderMap;
	}
	
	public FavoriteItemAsset getAssetFavoritesSet()
	{
		return this.assetFavoritesSet;
	}
	public FavoriteItemNote getNoteFavoritesSet()
	{
		return this.noteFavoritesSet;
	}
	public FavoriteItem getFavoriteItem(ItemCls item)
	{
		if (item instanceof NoteCls) { 
			return this.noteFavoritesSet;
		} else if (item instanceof AssetCls) { 
			return this.assetFavoritesSet;
		} else {
			return null;
		}
		
	}
	
	public void delete(Account account)
	{
		this.accountMap.delete(account);
		this.blockMap.delete(account);
		this.transactionMap.delete(account);
		this.nameMap.delete(account);
		this.nameSaleMap.delete(account);
		this.pollMap.delete(account);
		this.assetMap.delete(account);
		this.noteMap.delete(account);
		this.orderMap.delete(account);
	}
	
	public void commit()
	{
		this.database.commit();
	}
	
	public void close() 
	{
		if(this.database != null)
		{
			if(!this.database.isClosed())
			{
				this.database.commit();
				this.database.close();
			}
		}
	}
}