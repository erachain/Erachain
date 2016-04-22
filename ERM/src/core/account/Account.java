package core.account;
//04/01 +- 
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.mapdb.Fun.Tuple2;

//import com.google.common.primitives.Bytes;

import at.AT_Transaction;
import controller.Controller;
import core.BlockGenerator;
import core.block.Block;
import core.crypto.Base58;
import core.item.assets.AssetCls;
import core.transaction.Transaction;
import utils.NumberAsString;
import database.DBSet;

public class Account {
	
	public static final int ADDRESS_LENGTH = 25;

	protected String address;
	
	private byte[] lastBlockSignature;
	private BigDecimal generatingBalance;
	
	protected Account()
	{
		this.generatingBalance = BigDecimal.ZERO.setScale(8);
	}
	
	public Account(String address)
	{

		// test address
		assert(Base58.decode(address) instanceof byte[] );

		this.address = address;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	//BALANCE
	
	// GET
	public BigDecimal getUnconfirmedBalance()
	{
		return this.getUnconfirmedBalance(DBSet.getInstance());
	}
	
	public BigDecimal getUnconfirmedBalance(DBSet db)
	{
		return Controller.getInstance().getUnconfirmedBalance(this.getAddress());
	}
	
	public BigDecimal getConfirmedBalance()
	{
		return this.getConfirmedBalance(DBSet.getInstance());
	}
	
	public BigDecimal getConfirmedBalance(DBSet db)
	{
		return db.getAssetBalanceMap().get(getAddress());
	}
	
	public BigDecimal getConfirmedBalance(long key)
	{
		return this.getConfirmedBalance(key, DBSet.getInstance());
	}
	
	public BigDecimal getConfirmedBalance(long key, DBSet db)
	{
		return db.getAssetBalanceMap().get(getAddress(), key);
	}
	public Long getConfirmedStatus(long key, DBSet db)
	{
		return db.getStatusTimeMap().get(getAddress(), key).get(0);
	}

	// SET
	public void setConfirmedBalance(BigDecimal amount)
	{
		this.setConfirmedBalance(amount, DBSet.getInstance());
	}
	
	public void setConfirmedBalance(BigDecimal amount, DBSet db)
	{
		//UPDATE BALANCE IN DB
		db.getAssetBalanceMap().set(getAddress(), amount);
	}
		
	//
	public void setConfirmedBalance(long key, BigDecimal amount)
	{
		this.setConfirmedBalance(key, amount, DBSet.getInstance());
	}

	public void setConfirmedBalance(long key, BigDecimal amount, DBSet db)
	{
		//UPDATE BALANCE IN DB
		db.getAssetBalanceMap().set(getAddress(), key, amount);
	}

	// STATUS
	public void setConfirmedStatus(long key, Long  time)
	{
		this.setConfirmedStatus(key, time, DBSet.getInstance());
	}
	
	public void removeConfirmedStatus(long key, DBSet db)
	{
		//UPDATE TIMES LIST IN DB
		String address = getAddress();

		List<Long> times = db.getStatusTimeMap().get(getAddress(), key);
		
		if (times.isEmpty()) return;
		
		times.remove(0);
		db.getStatusTimeMap().set(address, key, times);
	}
	
	public void setConfirmedStatus(long key, Long time, DBSet db)
	{
		//UPDATE PRIMARY TIME IN DB
		String address = getAddress();

		List<Long> times = db.getStatusTimeMap().get(getAddress(), key);
		
		if (!times.isEmpty() & times.get(0) == time) return;
		
		times.add(time);
		db.getStatusTimeMap().set(address, key, times);
	}

	
	public BigDecimal getBalance(int confirmations)
	{
		return this.getBalance(confirmations, DBSet.getInstance());
	}
	
	public BigDecimal getBalance(int confirmations, DBSet db)
	{
		//CHECK IF UNCONFIRMED BALANCE
		if(confirmations <= 0)
		{
			return this.getUnconfirmedBalance(db);
		}
		
		//IF 1 CONFIRMATION
		if(confirmations == 1)
		{
			return this.getConfirmedBalance(db);
		}
		
		//GO TO PARENT BLOCK 10
		BigDecimal balance = this.getConfirmedBalance(db);
		Block block = db.getBlockMap().getLastBlock();
		
		for(int i=1; i<confirmations && block != null && block instanceof Block; i++)
		{
			for(Transaction transaction: block.getTransactions())
			{
				if(transaction.isInvolved(this))
				{
					balance = balance.subtract(transaction.viewAmount(this));
				}
			}
				
			block = block.getParent(db);
		}
		
		//RETURN
		return balance;
	}
	
	private void updateGeneratingBalance(DBSet db)
	{
		//CHECK IF WE NEED TO RECALCULATE
		if(this.lastBlockSignature == null)
		{
			this.lastBlockSignature = db.getBlockMap().getLastBlockSignature();
			calculateGeneratingBalance(db);
		}
		else
		{
			//CHECK IF WE NEED TO RECALCULATE
			if(!Arrays.equals(this.lastBlockSignature, db.getBlockMap().getLastBlockSignature()))
			{
				this.lastBlockSignature = db.getBlockMap().getLastBlockSignature();
				calculateGeneratingBalance(db);
			}
		}
	}
	
	public void calculateGeneratingBalance(DBSet db)
	{
		//CONFIRMED BALANCE + ALL NEGATIVE AMOUNTS IN LAST 9 BLOCKS
		BigDecimal balance = this.getConfirmedBalance(db);
		
		Block block = db.getBlockMap().getLastBlock();
		
		for(int i=1; i<BlockGenerator.RETARGET && block != null && block.getHeight(db) > 1; i++)
		{
			for(Transaction transaction: block.getTransactions())
			{
				if(transaction.isInvolved(this))
				{
					if(transaction.viewAmount(this).compareTo(BigDecimal.ZERO) == 1)
					{
						balance = balance.subtract(transaction.viewAmount(this));
					}
				}
			}
			LinkedHashMap<Tuple2<Integer,Integer>,AT_Transaction> atTxs = db.getATTransactionMap().getATTransactions(block.getHeight(db));
			Iterator<AT_Transaction> iter = atTxs.values().iterator(); 
			while ( iter.hasNext() )
			{
				AT_Transaction key = iter.next();
				if ( key.getRecipient().equals( this.getAddress() ) )
				{
					balance = balance.subtract( BigDecimal.valueOf(key.getAmount(), 8) );
				}
			}
				
			block = block.getParent(db);
		}
		
		//DO NOT GO BELOW 0
		if(balance.compareTo(BigDecimal.ZERO) == -1)
		{
			balance = BigDecimal.ZERO.setScale(8);
		}
		
		this.generatingBalance = balance;
	}
	
	public BigDecimal getGeneratingBalance()
	{
		return this.getGeneratingBalance(DBSet.getInstance());
	}
	
	public BigDecimal getGeneratingBalance(DBSet db)
	{	
		//UPDATE
		updateGeneratingBalance(db);
		
		//RETURN
		return this.generatingBalance;
	}
	
	//REFERENCE
	
	public byte[] getLastReference()
	{
		return this.getLastReference(DBSet.getInstance());
	}
	
	public byte[] getLastReference(DBSet db)
	{
		return db.getReferenceMap().get(this);
	}
	
	public void setLastReference(byte[] reference)
	{
		this.setLastReference(reference, DBSet.getInstance());
	}
	
	public void setLastReference(byte[] reference, DBSet db)
	{
		db.getReferenceMap().set(this, reference);
	}
	
	public void removeReference() 
	{
		this.removeReference(DBSet.getInstance());
	}
	
	public void removeReference(DBSet db) 
	{
		db.getReferenceMap().delete(this);
	}
	
	//TOSTRING
	
	@Override
	public String toString()
	{
		/*
		return NumberAsString.getInstance().numberAsString(this.getBalance(0))
				+ " {" + this.getConfirmedBalance(Transaction.FEE_KEY) + "}"
				+ " - " + this.getAddress();
				*/
		return this.getConfirmedBalance(AssetCls.DILE_KEY)
				+ " - " + this.getAddress();
	}
	
	public String toString(long key)
	{
		return NumberAsString.getInstance().numberAsString(this.getConfirmedBalance(key))
				+ " {" + NumberAsString.getInstance().numberAsString(this.getConfirmedBalance(AssetCls.DILE_KEY)) + "}"
				+ " - " + this.getAddress();
	}
	
	@Override
	public int hashCode()
	{
		return this.getAddress().hashCode();
	}
	
	//EQUALS
	@Override
	public boolean equals(Object b)
	{
		if(b instanceof Account)
		{
			return this.getAddress().equals(((Account) b).getAddress());
		}
		
		return false;	
	}
}
