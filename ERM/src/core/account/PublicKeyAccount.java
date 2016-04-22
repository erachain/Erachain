package core.account;

import java.math.BigDecimal;

import core.crypto.Crypto;
//import core.transaction.Transaction;
import core.item.statuses.StatusCls;
import database.DBSet;
import ntp.NTP;

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
		
		Long timestamp = this.getConfirmedStatus(StatusCls.ALIVE_KEY, db);
		if (timestamp < 0 ) return false;
		if (timestamp == 0 ) return true;
		
		// TEST TIME and EXPIRE TIME
		long time = NTP.getTime();
		if (timestamp < time ) return false;

		return true;
		
	}
	
}
