package core.account;

import java.math.BigDecimal;

import core.crypto.Crypto;
import core.transaction.Transaction;
import database.DBSet;

public class PublicKeyAccount extends Account {

	protected byte[] publicKey;
	
	public PublicKeyAccount(byte[] publicKey)
	{
		this.publicKey = publicKey;
		this.address = Crypto.getInstance().getAddress(this.publicKey);
	}
	
	protected PublicKeyAccount()
	{

	}
	
	public byte[] getPublicKey() 
	{
		return publicKey;
	}
	
	public boolean isPerson(DBSet db) {
		
		BigDecimal vote = this.getConfirmedBalance(Transaction.LAEV_KEY, db);
		if (vote.compareTo(BigDecimal.ONE) < 1) return false;

		return true;
		
	}
	
}
