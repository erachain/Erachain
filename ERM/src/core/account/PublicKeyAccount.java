package core.account;

import api.ApiErrorFactory;
import core.crypto.Base58;

//import java.math.BigDecimal;

//import org.mapdb.Fun.Tuple2;
//import org.mapdb.Fun.Tuple3;

import core.crypto.Crypto;
//import core.transaction.Transaction;
import core.item.statuses.StatusCls;
//import database.DBSet;
//import ntp.NTP;
import lang.Lang;
import utils.NameUtils;
import utils.Pair;
import utils.NameUtils.NameResult;

public class PublicKeyAccount extends Account {

	public static final long ALIVE_KEY = StatusCls.ALIVE_KEY;
	protected byte[] publicKey;
	
	public PublicKeyAccount(byte[] publicKey)
	{
		this.publicKey = publicKey;
		this.address = Crypto.getInstance().getAddress(publicKey);
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

	//CHECK IF IS VALID PUBLIC KEY and MAKE NEW
	public static Pair<Integer, PublicKeyAccount> isValidPublicKey(byte[] publicKey)
	{
		if (publicKey.length != Transaction.len)
		PublicKeyAccount account = new PublicKeyAccount(publicKey);
		if(!Crypto.getInstance().isValidAddress(account.getAddress()))
			return new Pair<Integer, PublicKeyAccount>(ApiErrorFactory.ERROR_INVALID_ADDRESS, null);
		
		return new Pair<Integer, PublicKeyAccount>(null, account);
	}
	public static Pair<Integer, PublicKeyAccount> isValidPublicKey(String publicKey)
	{
		return isValidPublicKey(Base58.decode(publicKey));
	}

}
