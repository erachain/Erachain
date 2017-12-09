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

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.assets.AssetFactory;
import datachain.DCSet;
import datachain.ItemAssetMap;
import utils.NumberAsString;

// core.block.Block.isValid(DBSet) - check as false it
public class GenesisIssueAssetTransaction extends GenesisIssue_ItemRecord 
{
	
	private boolean involvedInWallet;
	private static final byte TYPE_ID = (byte)GENESIS_ISSUE_ASSET_TRANSACTION;
	private static final String NAME_ID = "GENESIS Issue Asset";
	
	public GenesisIssueAssetTransaction(AssetCls asset) 
	{
		super(TYPE_ID, NAME_ID, asset);

		//this.generateSignature();

	}

	//GETTERS/SETTERS
	//public static String getName() { return "Genesis Issue Asset"; }
	
	@Override
	public String viewAmount(Account account) {
		AssetCls asset = (AssetCls)this.getItem();
		return NumberAsString.getInstance().numberAsString(asset.getQuantity(DCSet.getInstance()));
	}
	@Override
	public String viewAmount(String address) {
		AssetCls asset = (AssetCls)this.getItem();
		return NumberAsString.getInstance().numberAsString(asset.getQuantity(DCSet.getInstance()));
	}

	
	//PARSE CONVERT
	public static Transaction Parse(byte[] data) throws Exception
	{	
		//CHECK IF WE MATCH BLOCK LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length");
		}
		
		// READ TYPE
		int position = SIMPLE_TYPE_LENGTH;
	
		//READ ASSET
		// read without reference
		AssetCls asset = AssetFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
		//position += asset.getDataLength(false);
						
		return new GenesisIssueAssetTransaction(asset);
	}	

	@Override
	public boolean isInvolved(Account account)
	{
		if (!this.involvedInWallet)
		{
			// only one record to wallet for all accounts
			this.involvedInWallet = true;
			return true;
		}
		
		return false;
		
	}

}
