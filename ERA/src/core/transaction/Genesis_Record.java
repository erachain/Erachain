package core.transaction;

import java.math.BigDecimal;
//import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 import org.apache.log4j.Logger;

//import ntp.NTP;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.ItemFactory;
import datachain.DCSet;
import datachain.Item_Map;

public class Genesis_Record extends Transaction 
{
	
	protected static final int BASE_LENGTH = SIMPLE_TYPE_LENGTH;
	
	public Genesis_Record(byte type, String NAME_ID) 
	{
		super(type, NAME_ID);
	}

	//GETTERS/SETTERS

	/*
	@Override
	public byte[] getReference()
	{
		return this.signature;
	}
	*/			

	public boolean hasPublicText() {
		return false;
	}

	public void generateSignature() {
		
		//return generateSignature1(this.recipient, this.amount, this.timestamp);
		byte[] data = this.toBytes( false, null );

		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);
		digest = Bytes.concat(digest, digest);
				
		this.signature = digest;

	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();

		return transaction;	
	}

	//PARSE CONVERT
	//public abstract Transaction Parse(byte[] data);
	
	@Override
	public byte[] toBytes(boolean withSign, Long releaserReference) 
	{
		
		//WRITE TYPE in typeBytes[0]
		byte[] data = new byte[]{this.typeBytes[0]};
		
		// SIGNATURE not need - its calculated on fly
						
		return data;
	}
	
	@Override
	public int getDataLength(boolean asPack)
	{
		return BASE_LENGTH;
	}
	
	//VALIDATE
	
	public boolean isSignatureValid()
	{
		return Arrays.equals(this.signature, this.getSignature());
	}	

	@Override
	public HashSet<Account> getInvolvedAccounts()
	{
		return this.getRecipientAccounts();
	}

	@Override
	public HashSet<Account> getRecipientAccounts()
	{
		HashSet<Account> accounts = new HashSet<>();
		return accounts;
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{	
		return false;	
	}

	@Override
	public int calcBaseFee() {
		return 0;
	}
}
