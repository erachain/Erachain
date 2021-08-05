package org.erachain.core.item.assets;

//import java.math.BigDecimal;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.transaction.Transaction;
import org.erachain.lang.Lang;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AssetUniqueSeries extends AssetUnique {

    private static final int TYPE_ID = UNIQUE_SERIES;

    private int total;

    public AssetUniqueSeries(byte[] typeBytes, byte[] appData, PublicKeyAccount maker, String name, byte[] icon,
                             byte[] image, String description, int assetType, int total) {
        super(typeBytes, appData, maker, name, icon, image, description, assetType);

        this.total = total;

    }

    public AssetUniqueSeries(int props, byte[] appData, PublicKeyAccount maker, String name, byte[] icon,
                             byte[] image, String description, int assetType, int total, int index) {
        this(new byte[]{(byte) TYPE_ID, (byte) props}, appData, maker, name, icon, image, description, assetType, total);
    }

    public AssetUniqueSeries(byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image,
                             String description, int assetType, int total, int index) {
        this(new byte[]{(byte) TYPE_ID, (byte) 0}, appData, maker, name, icon, image, description, assetType, total);
    }

    // GETTERS/SETTERS
    @Override
    public String getItemSubType() {
        return "unique series";
    }

    public int getTotal() {
        return total;
    }

    @Override
    public int isValid() {
        if (total > 500) {
            return Transaction.INVALID_MAX_COUNT;
        }

        return super.isValid();
    }

    //PARSE
    // includeReference - TRUE only for store in local DB
    public static AssetUniqueSeries parse(int forDeal, byte[] data, boolean includeReference) throws Exception {

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
            imageLength &= ~APP_DATA_MASK;

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
            position += appDataLen;

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

        //READ TOTAL
        byte[] totalBytes = Arrays.copyOfRange(data, position, position + Short.BYTES);
        int total = Shorts.fromByteArray(totalBytes);
        position += Short.BYTES;

        //RETURN
        AssetUniqueSeries unique = new AssetUniqueSeries(typeBytes, appData, maker, name, icon,
                image, description, Byte.toUnsignedInt(assetTypeBytes[0]), total);

        if (includeReference) {
            unique.setReference(reference, dbRef);
        }

        return unique;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean includeReference, boolean forMakerSign) {

        byte[] data = super.toBytes(forDeal, includeReference, forMakerSign);

        byte[] addData = new byte[3];

        //WRITE ASSET TYPE
        addData[0] = (byte) this.getAssetType();

        System.arraycopy(Shorts.toByteArray((short) total), 0, addData, 1, Short.BYTES);

        data = Bytes.concat(data, appData);

        return data;
    }

    public int getDataLength(boolean includeReference) {
        return super.getDataLength(includeReference) + 2;
    }

    //OTHER
    public byte[] remakeAppdata() {
        return appData;
    }

    /**
     * Make new indexed AssetUniqueSeries
     *
     * @return
     */
    public AssetUniqueSeriesCopy copy(int index) {
        return new AssetUniqueSeriesCopy(this, index);
    }

    public String makeHTMLView() {

        String text = super.makeHTMLHeadView();
        text += Lang.T("Series") + ":&nbsp;" + getTotal() + ", "
                + "<br>";
        text += super.makeHTMLFootView(true);

        return text;

    }

}
