package org.erachain.core.item.assets;

//import java.math.BigDecimal;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.datachain.DCSet;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

//import com.google.common.primitives.Bytes;

public class AssetUnique extends AssetCls {

    private static final int TYPE_ID = UNIQUE;

    public AssetUnique(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, int asset_type, int scale) {
        super(typeBytes, owner, name, icon, image, description, asset_type, scale);
    }

    public AssetUnique(int props, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, int asset_type, int scale) {
        this(new byte[]{(byte) TYPE_ID, (byte) props}, owner, name, icon, image, description, asset_type, scale);
    }

    public AssetUnique(PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, int asset_type, int scale) {
        this(new byte[]{(byte) TYPE_ID, (byte) 0}, owner, name, icon, image, description, asset_type, scale);
    }

    //PARSE
    // includeReference - TRUE only for store in local DB
    public static AssetUnique parse(byte[] data, boolean includeReference) throws Exception {

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        //READ CREATOR
        byte[] ownerBytes = Arrays.copyOfRange(data, position, position + OWNER_LENGTH);
        PublicKeyAccount owner = new PublicKeyAccount(ownerBytes);
        position += OWNER_LENGTH;

        //READ NAME
        //byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
        //int nameLength = Ints.fromByteArray(nameLengthBytes);
        //position += NAME_SIZE_LENGTH;
        int nameLength = Byte.toUnsignedInt(data[position]);
        position++;

        if (nameLength < 1 || nameLength > MAX_NAME_LENGTH) {
            throw new Exception("Invalid name length");
        }

        byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
        String name = new String(nameBytes, StandardCharsets.UTF_8);
        position += nameLength;

        //READ ICON
        byte[] iconLengthBytes = Arrays.copyOfRange(data, position, position + ICON_SIZE_LENGTH);
        int iconLength = Ints.fromBytes((byte) 0, (byte) 0, iconLengthBytes[0], iconLengthBytes[1]);
        position += ICON_SIZE_LENGTH;

        if (iconLength < 0 || iconLength > MAX_ICON_LENGTH) {
            throw new Exception("Invalid icon length");
        }

        byte[] icon = Arrays.copyOfRange(data, position, position + iconLength);
        position += iconLength;

        //READ IMAGE
        byte[] imageLengthBytes = Arrays.copyOfRange(data, position, position + IMAGE_SIZE_LENGTH);
        int imageLength = Ints.fromByteArray(imageLengthBytes);
        position += IMAGE_SIZE_LENGTH;

        if (imageLength < 0 || imageLength > MAX_IMAGE_LENGTH) {
            throw new Exception("Invalid image length");
        }

        byte[] image = Arrays.copyOfRange(data, position, position + imageLength);
        position += imageLength;

        //READ DESCRIPTION
        byte[] descriptionLengthBytes = Arrays.copyOfRange(data, position, position + DESCRIPTION_SIZE_LENGTH);
        int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
        position += DESCRIPTION_SIZE_LENGTH;

        if (descriptionLength > BlockChain.MAX_REC_DATA_BYTES) {
            throw new Exception("Invalid description length");
        }

        byte[] descriptionBytes = Arrays.copyOfRange(data, position, position + descriptionLength);
        String description = new String(descriptionBytes, StandardCharsets.UTF_8);
        position += descriptionLength;

        byte[] reference = null;
        long seqNo = 0;
        if (includeReference) {
            //READ REFERENCE
            reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
            position += REFERENCE_LENGTH;

            //READ SEQNO
            byte[] seqNoBytes = Arrays.copyOfRange(data, position, position + SEQNO_LENGTH);
            seqNo = Longs.fromByteArray(seqNoBytes);
            position += SEQNO_LENGTH;
        }

        //READ SCALE
        byte[] scaleBytes = Arrays.copyOfRange(data, position, position + SCALE_LENGTH);
        byte scale = scaleBytes[0];
        position += SCALE_LENGTH;

        //READ ASSET TYPE
        byte[] assetTypeBytes = Arrays.copyOfRange(data, position, position + ASSET_TYPE_LENGTH);
        position += ASSET_TYPE_LENGTH;


        //RETURN
        AssetUnique unique = new AssetUnique(typeBytes, owner, name, icon, image, description, assetTypeBytes[0], scale);
        if (includeReference) {
            unique.setReference(reference, seqNo);
        }

        return unique;
    }

    //GETTERS/SETTERS
    @Override
    public String getItemSubType() {
        return "unique";
    }

    @Override
    public long getQuantity() {
        return 1L;
    }

    @Override
    public BigDecimal getReleased(DCSet dcSet) {
        Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> bals = this.getOwner().getBalance(this.getKey(dcSet));
        return BigDecimal.ONE.subtract(bals.a.b).stripTrailingZeros();
    }

    public BigDecimal getReleased() {
        return getReleased(DCSet.getInstance());
    }

    //OTHER
    @Override
    public byte[] toBytes(boolean includeReference, boolean forOwnerSign) {

        byte[] data = super.toBytes(includeReference, forOwnerSign);

        //WRITE SCALE
        data = Bytes.concat(data, new byte[]{(byte) this.getScale()});

        //WRITE ASSET TYPE
        data = Bytes.concat(data, new byte[]{(byte) this.getAssetType()});

        return data;
    }

    @Override
    public int getDataLength(boolean includeReference) {
        return super.getDataLength(includeReference)
                + SCALE_LENGTH + ASSET_TYPE_LENGTH;
    }

    //OTHER

    @Override
    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        JSONObject assetJSON = super.toJson();

        // ADD DATA
        assetJSON.put("quantity", 1);
        assetJSON.put("released", this.getReleased());

        return assetJSON;
    }

}
