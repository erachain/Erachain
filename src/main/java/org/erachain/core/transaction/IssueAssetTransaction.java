package org.erachain.core.transaction;

import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetFactory;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class IssueAssetTransaction extends IssueItemRecord {
    private static final byte TYPE_ID = (byte) ISSUE_ASSET_TRANSACTION;
    private static final String NAME_ID = "Issue Asset";
    public static final long START_KEY = 1000L;

    public IssueAssetTransaction(byte[] typeBytes, PublicKeyAccount creator, AssetCls asset, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, asset, feePow, timestamp, reference);
    }

    public IssueAssetTransaction(byte[] typeBytes, PublicKeyAccount creator, AssetCls asset, byte feePow, long timestamp, Long reference, byte[] signature) {
        super(typeBytes, NAME_ID, creator, asset, feePow, timestamp, reference, signature);
    }
    public IssueAssetTransaction(byte[] typeBytes, PublicKeyAccount creator, AssetCls asset, byte feePow,
                                 long timestamp, Long reference, byte[] signature, long feeLong) {
        super(typeBytes, NAME_ID, creator, asset, feePow, timestamp, reference, signature);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.AMOUNT_DEDAULT_SCALE);
    }

    // as pack
    public IssueAssetTransaction(byte[] typeBytes, PublicKeyAccount creator, AssetCls asset, byte[] signature) {
        super(typeBytes, NAME_ID, creator, asset, (byte) 0, 0l, null, signature);
    }

    public IssueAssetTransaction(PublicKeyAccount creator, AssetCls asset, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, asset, feePow, timestamp, reference, signature);
    }

    // as pack
    public IssueAssetTransaction(PublicKeyAccount creator, AssetCls asset, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, asset, (byte) 0, 0l, reference, signature);
    }

    public IssueAssetTransaction(PublicKeyAccount creator, AssetCls asset, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, asset, feePow, timestamp, reference);
    }

    public long getStartKey(int height) {
        if (height < BlockChain.VERS_4_11) {
            return 1000L;
        }
        return START_KEY;

    }

    public static Transaction Parse(byte[] data, int asDeal) throws Exception {
        int test_len;
        if (asDeal == Transaction.FOR_MYPACK) {
            test_len = BASE_LENGTH_AS_MYPACK;
        } else if (asDeal == Transaction.FOR_PACK) {
            test_len = BASE_LENGTH_AS_PACK;
        } else if (asDeal == Transaction.FOR_DB_RECORD) {
            test_len = BASE_LENGTH_AS_DBRECORD;
        } else {
            test_len = BASE_LENGTH;
        }
        if (data.length < test_len) {
            throw new Exception("Data does not match block length " + data.length);
        }
        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        long timestamp = 0;
        if (asDeal > Transaction.FOR_MYPACK) {
            //READ TIMESTAMP
            byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            timestamp = Longs.fromByteArray(timestampBytes);
            position += TIMESTAMP_LENGTH;
        }

        //READ REFERENCE
        byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
        Long reference = Longs.fromByteArray(referenceBytes);
        position += REFERENCE_LENGTH;

        //READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        byte feePow = 0;
        if (asDeal > Transaction.FOR_PACK) {
            //READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        //READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        long feeLong = 0;
        if (asDeal == FOR_DB_RECORD) {
            // READ FEE
            byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            feeLong = Longs.fromByteArray(feeBytes);
            position += FEE_LENGTH;
        }

        //READ ASSET
        // asset parse without reference - if is = signature
        AssetCls asset = AssetFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
        if (asDeal > Transaction.FOR_MYPACK) {
            return new IssueAssetTransaction(typeBytes, creator, asset, feePow, timestamp, reference, signatureBytes, feeLong);
        } else {
            return new IssueAssetTransaction(typeBytes, creator, asset, signatureBytes);
        }
    }

    public long getAssetKey(DCSet db) {
        return getItem().getKey(db);
    }

    @Override
    public BigDecimal getAmount() {
        return new BigDecimal(((AssetCls) getItem()).getQuantity());
    }

    @Override
    public BigDecimal getAmount(String address) {
        if (address.equals(creator.getAddress())) {
            return getAmount();
        }
        AssetCls asset = (AssetCls) item;
        return BigDecimal.ZERO.setScale(asset.getScale());
    }

    @Override
    public String viewAmount(String address) {
        return this.getAmount().toString();
    }

    //PARSE CONVERT

    @Override
    public int isValid(int asDeal, long flags) {

        int result = super.isValid(asDeal, flags);
        if (result != Transaction.VALIDATE_OK){
            return result;
        }
        //CHECK QUANTITY
        AssetCls asset = (AssetCls) getItem();
        long quantity = asset.getQuantity();
        if (quantity < -1) {
            return INVALID_QUANTITY;
        }
        return Transaction.VALIDATE_OK;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJson() {
        //GET BASE
        JSONObject transaction = super.toJson();
        AssetCls asset = (AssetCls) getItem();
        //ADD CREATOR/NAME/DISCRIPTION/QUANTITY/DIVISIBLE
        transaction.put("asset", asset.toJson());
        return transaction;
    }


    //PROCESS/ORPHAN

    //@Override
    @Override
    public void process(Block block, int asDeal) {
        //UPDATE CREATOR
        super.process(block, asDeal);
        //ADD ASSETS TO OWNER
        AssetCls asset = (AssetCls) this.getItem();
        long quantity = asset.getQuantity();
        if (quantity > 0) {
            creator.changeBalance(this.dcSet, false, asset.getKey(dcSet),
                    new BigDecimal(quantity).setScale(0), false);

            // make HOLD balance
            creator.changeBalance(this.dcSet, false, asset.getKey(dcSet),
                    new BigDecimal(-quantity).setScale(0), false);
                
        }

    }

    //@Override
    @Override
    public void orphan(Block block, int asDeal) {
        //UPDATE CREATOR
        super.orphan(block, asDeal);
        //REMOVE ASSETS FROM OWNER
        AssetCls asset = (AssetCls) this.getItem();
        long quantity = asset.getQuantity();
        if (quantity > 0) {
            creator.changeBalance(dcSet, true, asset.getKey(dcSet),
                    new BigDecimal(quantity).setScale(0), false);

            creator.changeBalance(dcSet, true, asset.getKey(dcSet),
                    new BigDecimal(-quantity).setScale(0), false);
        }
    }

    public Map<String, Map<Long, BigDecimal>> getAssetAmount() {
        Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
        assetAmount = subAssetAmount(assetAmount, creator.getAddress(), FEE_KEY, fee);
        AssetCls asset = (AssetCls) getItem();
        assetAmount = addAssetAmount(assetAmount, creator.getAddress(), asset.getKey(dcSet),
                new BigDecimal(asset.getQuantity()).setScale(0));
        return assetAmount;
    }

}
