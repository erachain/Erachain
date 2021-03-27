package org.erachain.core.item.assets;

//import java.math.BigDecimal;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.datachain.DCSet;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AssetUnique extends AssetCls {

    private static final int TYPE_ID = UNIQUE;

    public AssetUnique(byte[] typeBytes, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, int assetType) {
        super(typeBytes, appData, maker, name, icon, image, description, assetType);
        parseAppData();
    }

    public AssetUnique(int props, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, int assetType) {
        this(new byte[]{(byte) TYPE_ID, (byte) props}, appData, maker, name, icon, image, description, assetType);
    }

    public AssetUnique(byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, int assetType) {
        this(new byte[]{(byte) TYPE_ID, (byte) 0}, appData, maker, name, icon, image, description, assetType);
    }

    protected int parseAppData() {
        int pos = super.parseAppData();
        //
        return pos;
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
    public boolean isUnique() {
        return true;
    }

    @Override
    public int getScale() {
        return 0;
    }

    @Override
    public boolean isUnlimited(Account address, boolean notAccounting) {
        return false;
    }

    @Override
    public BigDecimal getReleased(DCSet dcSet) {
        Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> bals = this.getMaker().getBalance(this.getKey(dcSet));
        return BigDecimal.ONE.subtract(bals.a.b).stripTrailingZeros();
    }

    @Override
    public BigDecimal getReleased() {
        return getReleased(DCSet.getInstance());
    }

    //PARSE
    // includeReference - TRUE only for store in local DB
    public static AssetUnique parse(byte[] data, boolean includeReference) throws Exception {

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        //READ CREATOR
        byte[] makerBytes = Arrays.copyOfRange(data, position, position + MAKER_LENGTH);
        PublicKeyAccount maker = new PublicKeyAccount(makerBytes);
        position += MAKER_LENGTH;

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
            throw new Exception("Invalid icon length" + name + ": " + iconLength);
        }

        byte[] icon = Arrays.copyOfRange(data, position, position + iconLength);
        position += iconLength;

        //READ IMAGE
        byte[] imageLengthBytes = Arrays.copyOfRange(data, position, position + IMAGE_SIZE_LENGTH);
        int imageLength = Ints.fromByteArray(imageLengthBytes);
        position += IMAGE_SIZE_LENGTH;

        // TEST APP DATA
        boolean hasAppData = (imageLength & APP_DATA_MASK) != 0;
        if (hasAppData)
            // RESET LEN
            imageLength *= -1;

        if (imageLength < 0 || imageLength > MAX_IMAGE_LENGTH) {
            throw new Exception("Invalid image length" + name + ": " + imageLength);
        }

        byte[] image = Arrays.copyOfRange(data, position, position + imageLength);
        position += imageLength;

        byte[] appData;
        if (hasAppData) {
            // READ APP DATA
            int appDataLen = Ints.fromByteArray(Arrays.copyOfRange(data, position, position + APP_DATA_LENGTH));
            position += APP_DATA_LENGTH;

            appData = Arrays.copyOfRange(data, position, position + appDataLen);
            position += imageLength;

        } else {
            appData = null;
        }

        //READ DESCRIPTION
        byte[] descriptionLengthBytes = Arrays.copyOfRange(data, position, position + DESCRIPTION_SIZE_LENGTH);
        int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
        position += DESCRIPTION_SIZE_LENGTH;

        if (descriptionLength > BlockChain.MAX_REC_DATA_BYTES) {
            throw new Exception("Invalid description length" + name + ": " + descriptionLength);
        }

        byte[] descriptionBytes = Arrays.copyOfRange(data, position, position + descriptionLength);
        String description = new String(descriptionBytes, StandardCharsets.UTF_8);
        position += descriptionLength;

        byte[] reference = null;
        long dbRef = 0;
        if (includeReference) {
            //READ REFERENCE
            reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
            position += REFERENCE_LENGTH;

            //READ SEQNO
            byte[] dbRefBytes = Arrays.copyOfRange(data, position, position + DBREF_LENGTH);
            dbRef = Longs.fromByteArray(dbRefBytes);
            position += DBREF_LENGTH;
        }

        //READ ASSET TYPE
        byte[] assetTypeBytes = Arrays.copyOfRange(data, position, position + ASSET_TYPE_LENGTH);
        position += ASSET_TYPE_LENGTH;

        //RETURN
        AssetUnique unique = new AssetUnique(typeBytes, appData, maker, name, icon, image, description, Byte.toUnsignedInt(assetTypeBytes[0]));
        if (includeReference) {
            unique.setReference(reference, dbRef);
        }

        return unique;
    }

    @Override
    public byte[] toBytes(boolean includeReference, boolean forMakerSign) {

        byte[] data = super.toBytes(includeReference, forMakerSign);

        //WRITE ASSET TYPE
        data = Bytes.concat(data, new byte[]{(byte) this.getAssetType()});

        return data;
    }

    //OTHER
    public String makeHTMLView() {

        String text = super.makeHTMLHeadView();
        text += super.makeHTMLFootView(true);

        return text;

    }

}
