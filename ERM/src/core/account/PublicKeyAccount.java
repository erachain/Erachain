package core.account;

//import java.math.BigDecimal;

//import org.mapdb.Fun.Tuple2;
//import org.mapdb.Fun.Tuple3;

import core.crypto.Crypto;
//import core.transaction.Transaction;
import core.item.statuses.StatusCls;
//import database.DBSet;
//import ntp.NTP;

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
	
}
