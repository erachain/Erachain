package core.account;

import java.util.TreeSet;

import org.mapdb.Fun.Tuple3;

import api.ApiErrorFactory;
import core.crypto.Base58;

//import java.math.BigDecimal;

//import org.mapdb.Fun.Tuple2;
//import org.mapdb.Fun.Tuple3;

import core.crypto.Crypto;
//import core.transaction.Transaction;
import core.item.statuses.StatusCls;
import database.DBSet;
//import database.DBSet;
//import ntp.NTP;
import lang.Lang;
import utils.NameUtils;
import utils.Pair;
import utils.NameUtils.NameResult;

public class PublicKeyAccount extends Account {

	public static final int PUBLIC_KEY_LENGTH = Crypto.HASH_LENGTH;
	//public static final long ALIVE_KEY = StatusCls.ALIVE_KEY;
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
	public static boolean isValidPublicKey(byte[] publicKey)
	{
		if (publicKey.length != PUBLIC_KEY_LENGTH) return false;
		return true;
	}
	public static boolean isValidPublicKey(String publicKey)
	{
		return isValidPublicKey(Base58.decode(publicKey));
	}
	public boolean isValid()
	{
		return isValidPublicKey(this.publicKey);
	}
	

}
