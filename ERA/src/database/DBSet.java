package database;
// 30/03 ++
import java.io.File;

import database.IDB;
import database.wallet.DWSet;
import datachain.TransactionMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
//import org.mapdb.Serializer;

import settings.Settings;

public class DBSet implements IDB
{
	private static final File DATA_FILE = new File(Settings.getInstance().getLocalDir(), "data.dat");
	
	private static final String VERSION = "version";
	
	private DB database;	
	private int uses;

	private PeerMap peerMap;
	
	public static boolean exists()
	{
		return DATA_FILE.exists();
	}
	
	public DBSet()
	{
		//OPEN WALLET
		DATA_FILE.getParentFile().mkdirs();
				
	    this.database = DBMaker.newFileDB(DATA_FILE)
	    		.closeOnJvmShutdown()
	    		.cacheSize(2048)
	    		//.checksumEnable()
	    		.mmapFileEnableIfSupported()
				/// ICREATOR
				////.commitFileSyncDisable()
				.transactionDisable()
	            .make();
	    
	    uses = 0;
	    
		this.peerMap = new PeerMap(this, this.database);
	    
	}	

	public PeerMap getPeerMap()
	{
		return this.peerMap;
	}
	
	public void setVersion(int version)
	{
		this.uses++;
		this.database.getAtomicInteger(VERSION).set(version);
		this.uses--;
	}
	
	public int getVersion()
	{
		this.uses++;
		int u = this.database.getAtomicInteger(VERSION).intValue();
		this.uses--;
		return u;
	}
	
	public void addUses()
	{
		this.uses++;
		
	}
	public void outUses()
	{
		this.uses--;
	}
	
	public boolean isBusy()
	{
		if (this.uses > 0) {
			return true;
		} else {
			return false;
		}
	}
		
		
	public void commit()
	{
		this.uses++;
		this.database.commit();
		this.uses--;

	}
	
	public void close() 
	{
		if(this.database != null)
		{
			if(!this.database.isClosed())
			{
				this.uses++;
				this.database.commit();
				this.database.close();
				this.uses--;

			}
		}
	}
}