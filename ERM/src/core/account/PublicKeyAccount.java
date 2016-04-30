package core.account;

import core.crypto.Base58;

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
	public PublicKeyAccount(String publicKey)
	{
		this(Base58.decode(publicKey));
	}
	
	protected PublicKeyAccount()
	{

	}
	
	public byte[] getPublicKey()
	{
		return publicKey;
	}

	public String getBase58()
	{
		return Base58.encode(publicKey);
	}

}
