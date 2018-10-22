package org.erachain.core.item.assets;

//import java.math.BigDecimal;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.datachain.DCSet;

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

        //READ SCALE
        byte[] scaleBytes = Arrays.copyOfRange(data, position, position + SCALE_LENGTH);
        byte scale = scaleBytes[0];
        position += SCALE_LENGTH;

        //READ ASSET TYPE
        byte[] assetTypeBytes = Arrays.copyOfRange(data, position, position + ASSET_TYPE_LENGTH);
        //boolean divisable = divisibleBytes[0] == 1;
        position += ASSET_TYPE_LENGTH;


        //RETURN
        AssetUnique statement = new AssetUnique(typeBytes, owner, name, icon, image, description, assetTypeBytes[0], scale);

        if (includeReference) {
            //READ REFERENCE
            byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
            statement.setReference(reference);
        }
        return statement;
    }

    //GETTERS/SETTERS
    @Override
    public String getItemSubType() {
        return "unique";
    }

    @Override
    public Long getQuantity() {
        return 1L;
    }

    @Override
    public Long getTotalQuantity(DCSet dc) {
        return 1L;
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

}
