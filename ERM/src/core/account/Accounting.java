package core.account;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.mapdb.Fun.Tuple2;

//import com.google.common.primitives.Bytes;

import at.AT_Transaction;
import controller.Controller;
import core.BlockGenerator;
import core.block.Block;
import core.crypto.Base58;
import core.transaction.Transaction;
import utils.NumberAsString;
import database.DBSet;

public class Accounting extends Account {
	
	public static final int ADDRESS_LENGTH = 25;
	private static final long FEE_KEY = Transaction.FEE_KEY;
		
	public Accounting(String address)
	{

		// test address
		assert(Base58.decode(address) instanceof byte[] );

		this.address = address;
	}
		
	//BALANCE
	
	public BigDecimal getUnconfirmedBalance()
	{
		return this.getUnconfirmedBalance(DBSet.getInstance());
	}
	
	public BigDecimal getUnconfirmedBalance(DBSet db)
	{
		return Controller.getInstance().getUnconfirmedBalance(this.getAddress(), FEE_KEY);
	}
	
	public BigDecimal getConfirmedBalance()
	{
		return this.getConfirmedBalance(FEE_KEY, DBSet.getInstance());
	}
	
	/*
	public BigDecimal getConfirmedBalance(DBSet db)
	{
		return db.getAssetBalanceAccountingMap().get(getAddress());
	}
	*/
	
	public BigDecimal getConfirmedBalance(long key)
	{
		return this.getConfirmedBalance(key, DBSet.getInstance());
	}
	
	public BigDecimal getConfirmedBalance(long key, DBSet db)
	{
		return db.getAssetBalanceAccountingMap().get(getAddress(), key);
	}
	public BigDecimal getConfirmedBalance(byte[] hkey)
	{
		return this.getConfirmedBalance(hkey, DBSet.getInstance());
	}
	
	public BigDecimal getConfirmedBalance(byte[] hkey, DBSet db)
	{
		return db.getAssetBalanceAccountingMap().get(getAddress(), 2l);
	}

	public void setConfirmedBalance(BigDecimal amount)
	{
		this.setConfirmedBalance(FEE_KEY, amount, DBSet.getInstance());
	}
	
	/*
	public void setConfirmedBalance(BigDecimal amount, DBSet db)
	{
		//UPDATE BALANCE IN DB
		db.getAssetBalanceAccountingMap().set(getAddress(), amount);
	}
	*/
	public void setConfirmedBalance(long key, BigDecimal amount)
	{
		this.setConfirmedBalance(key, amount, DBSet.getInstance());
	}
	
	public void setConfirmedBalance(byte[] hkey, BigDecimal amount, DBSet db)
	{
		//UPDATE BALANCE IN DB
		// TODO db.getBalanceMapHKey().set(getAddress(), hkey, amount);
		this.setConfirmedBalance(2l, amount, DBSet.getInstance());
	}

	public void setConfirmedBalance(byte[] hkey, BigDecimal amount)
	{
		this.setConfirmedBalance(hkey, amount, DBSet.getInstance());
	}
	
	public void setConfirmedBalance(long key, BigDecimal amount, DBSet db)
	{
		//UPDATE BALANCE IN DB
		db.getAssetBalanceAccountingMap().set(getAddress(), key, amount);
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
			return this.getUnconfirmedBalance(FEE_KEY);
		}
		
		//IF 1 CONFIRMATION
		if(confirmations == 1)
		{
			return this.getConfirmedBalance(FEE_KEY, db);
		}
		
		//GO TO PARENT BLOCK 10
		BigDecimal balance = this.getConfirmedBalance(FEE_KEY, db);
		Block block = db.getBlockMap().getLastBlock();
		
		for(int i=1; i<confirmations && block != null && block instanceof Block; i++)
		{
			for(Transaction transaction: block.getTransactions())
			{
				if(transaction.isInvolved(this))
				{
					balance = balance.subtract(transaction.getAmount(this));
				}
			}
				
			block = block.getParent(db);
		}
		
		//RETURN
		return balance;
	}
		
	//EQUALS
	@Override
	public boolean equals(Object b)
	{
		if(b instanceof Accounting)
		{
			return this.getAddress().equals(((Accounting) b).getAddress());
		}
		
		return false;	
	}
}
