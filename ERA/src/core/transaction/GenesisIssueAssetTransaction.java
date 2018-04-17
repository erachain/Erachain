package core.transaction;

import java.util.Arrays;

import core.account.Account;
import core.item.assets.AssetCls;
import core.item.assets.AssetFactory;
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
		return NumberAsString.getInstance().numberAsString(asset.getQuantity());
	}
	@Override
	public String viewAmount(String address) {
		AssetCls asset = (AssetCls)this.getItem();
		return NumberAsString.getInstance().numberAsString(asset.getQuantity());
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
