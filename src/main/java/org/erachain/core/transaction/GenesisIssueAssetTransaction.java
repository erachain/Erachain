package org.erachain.core.transaction;

import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetFactory;
import org.erachain.utils.NumberAsString;

import java.math.BigDecimal;
import java.util.Arrays;

// core.block.Block.isValid(DLSet) - check as false it
public class GenesisIssueAssetTransaction extends GenesisIssueItemRecord {

    private static final byte TYPE_ID = (byte) GENESIS_ISSUE_ASSET_TRANSACTION;
    private static final String NAME_ID = "GENESIS Issue Asset";
    private boolean involvedInWallet;

    public GenesisIssueAssetTransaction(AssetCls asset) {
        super(TYPE_ID, NAME_ID, asset);

        //this.generateSignature();

    }

    //GETTERS/SETTERS
    //public static String getName() { return "Genesis Issue Asset"; }

    //PARSE CONVERT
    public static Transaction Parse(byte[] data) throws Exception {
        //CHECK IF WE MATCH BLOCK LENGTH
        if (data.length < BASE_LENGTH) {
            throw new Exception("Data does not match block length");
        }

        // READ TYPE
        int position = SIMPLE_TYPE_LENGTH;

        //READ ASSET
        // read without reference
        AssetCls asset = AssetFactory.getInstance().parse(Transaction.FOR_NETWORK, Arrays.copyOfRange(data, position, data.length), false);
        //position += asset.getDataLength(false);

        return new GenesisIssueAssetTransaction(asset);
    }

    @Override
    public String viewAmount(Account account) {
        AssetCls asset = (AssetCls) this.getItem();
        return NumberAsString.formatAsString(asset.getQuantity());
    }

    @Override
    public String viewAmount(String address) {
        AssetCls asset = (AssetCls) this.getItem();
        return NumberAsString.formatAsString(asset.getQuantity());
    }

    @Override
    public boolean isInvolved(Account account) {
        if (!this.involvedInWallet) {
            // only one record to wallet for all accounts
            this.involvedInWallet = true;
            return true;
        }

        return false;

    }

    public void process(Block block, int forDeal) {

        AssetCls asset = (AssetCls) item;
        if (this.dcSet.getItemAssetMap().size() > 1
                && (!BlockChain.TEST_MODE || item.isNovaItem(dcSet) <= 0)
        )
            // SKIP all base TOKENS
            return;

        super.process(block, forDeal);

        long quantity = asset.getQuantity();
        if (quantity > 0L) {
            Account maker = item.getMaker();
            Long assetKey = item.getKey();
            // надо добавить баланс на счет
            maker.changeBalance(dcSet, false, false, assetKey,
                    new BigDecimal(quantity).setScale(0), false, false, false);

            // make HOLD balance
            maker.changeBalance(dcSet, false, true, assetKey,
                    new BigDecimal(-quantity).setScale(0), false, false, false);
        }
    }

}
