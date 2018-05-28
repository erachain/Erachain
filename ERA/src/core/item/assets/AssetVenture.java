package core.item.assets;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import core.BlockChain;
import core.account.PublicKeyAccount;
import datachain.DCSet;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

//import java.math.BigDecimal;
//import com.google.common.primitives.Bytes;

public class AssetVenture extends AssetCls {

    protected static final int QUANTITY_LENGTH = 8;

    private static final int TYPE_ID = AssetCls.VENTURE;

    protected long quantity;

    public AssetVenture(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, int asset_type, int scale, long quantity) {
        super(typeBytes, owner, name, icon, image, description, asset_type, scale);
        this.quantity = quantity;
    }

    public AssetVenture(int props, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, int asset_type, int scale, long quantity) {
        this(new byte[]{(byte) TYPE_ID, (byte) props}, owner, name, icon, image, description, asset_type, scale, quantity);
    }

    public AssetVenture(PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, int asset_type, int scale, long quantity) {
        //this(new byte[]{(byte)TYPE_ID, movable?(byte)1:(byte)0}, owner, name, asset_type, icon, image, description, quantity, scale);
        this(new byte[]{(byte) TYPE_ID, (byte) 0}, owner, name, icon, image, description, asset_type, scale, quantity);
    }

    //GETTERS/SETTERS

    //PARSE
    // includeReference - TRUE only for store in local DB
    public static AssetVenture parse(byte[] data, boolean includeReference) throws Exception {

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
            throw new Exception("Invalid name length: " + nameLength);
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
        if (includeReference) {
            //READ REFERENCE
            reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
            position += REFERENCE_LENGTH;
        }

        //READ QUANTITY
        byte[] quantityBytes = Arrays.copyOfRange(data, position, position + QUANTITY_LENGTH);
        long quantity = Longs.fromByteArray(quantityBytes);
        position += QUANTITY_LENGTH;

        //READ SCALE
        byte[] scaleBytes = Arrays.copyOfRange(data, position, position + SCALE_LENGTH);
        byte scale = scaleBytes[0];
        position += SCALE_LENGTH;

        //READ ASSET TYPE
        byte[] assetTypeBytes = Arrays.copyOfRange(data, position, position + ASSET_TYPE_LENGTH);
        //boolean divisible = divisibleBytes[0] == 1;
        position += ASSET_TYPE_LENGTH;

        //RETURN
        AssetVenture venture = new AssetVenture(typeBytes, owner, name, icon, image, description, assetTypeBytes[0], scale, quantity);
        if (includeReference) {
            venture.setReference(reference);
        }

        return venture;
    }

    @Override
    public String getItemSubType() {
        return "venture";
    }

    @Override
    public Long getQuantity() {
        return this.quantity;
    }

    @Override
    public Long getTotalQuantity(DCSet dcSet) {

        if (this.quantity == 0) {
            // IF UNLIMIT QIUNTITY
            Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> bals = this.getOwner().getBalance(this.getKey(dcSet));
            long bal = -bals.a.b.longValue();
            if (bal == 0) {
                bal = 1l;
            }
            return bal;
        } else {
            return this.quantity;
        }
    }

    @Override
    public byte[] toBytes(boolean includeReference, boolean onlyBody) {
        byte[] data = super.toBytes(includeReference, onlyBody);

        //WRITE QUANTITY
        byte[] quantityBytes = Longs.toByteArray(this.quantity);
        data = Bytes.concat(data, quantityBytes);

        //WRITE SCALE_LENGTH
        //byte[] scaleBytes = new byte[this.scale];
        byte[] scaleBytes = new byte[1];
        scaleBytes[0] = (byte) this.scale;
        data = Bytes.concat(data, scaleBytes);


        //WRITE ASSET TYPE
        byte[] assetTypeBytes = new byte[1];
        //assetTypeBytes[0] = (byte) (this.divisible == true ? 1 : 0);
        assetTypeBytes[0] = (byte) this.asset_type;
        data = Bytes.concat(data, assetTypeBytes);

        return data;
    }

    @Override
    public int getDataLength(boolean includeReference) {
        return super.getDataLength(includeReference)
                + SCALE_LENGTH + ASSET_TYPE_LENGTH + QUANTITY_LENGTH;
    }

    //OTHER
    @Override
    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        JSONObject assetJSON = super.toJson();

        // ADD DATA
        assetJSON.put("quantity", this.getQuantity());

        return assetJSON;
    }

}
