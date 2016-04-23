package core.account;

import java.math.BigDecimal;

import core.crypto.Crypto;
//import core.transaction.Transaction;
import core.item.statuses.StatusCls;
import database.DBSet;
import ntp.NTP;

public class PublicKeyAccount extends Account {

	public static final long ALIVE_KEY = StatusCls.ALIVE_KEY;
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
		
		// IF NOT PERSON HAS THAT ADDRESS
		Long personKey = db.getAddressPersonMap().get(this.address);
		if (personKey < 0) return false;
		
		// IF PERSON NOT ALIVE
		Long timestamp = db.getPersonStatusMap().get(personKey, ALIVE_KEY);
		if (timestamp < 0 ) return false;
		if (timestamp == 0 ) return true;
		
		// TEST TIME and EXPIRE TIME
		long current_time = NTP.getTime();
		if (timestamp < current_time ) return false;

		return true;
		
	}
	
}
