package org.erachain.core.item.assets;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

//import java.math.BigDecimal;
//import com.google.common.primitives.Bytes;

public class AssetVenture extends AssetCls {

    protected static final int SCALE_LENGTH = 1;
    protected static final int QUANTITY_LENGTH = 8;

    private static final int TYPE_ID = VENTURE;

    /**
     * 0 - unlimited for maker.
     * If < 0 - all but not maker may sold it on exchange for maker - as Auction
     */
    protected long quantity;
    /**
     * +8 ... -23 = 32 диапазон. положительные - округляют целые числа
     */
    protected int scale;

    public AssetVenture(byte[] typeBytes, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, int asset_type, int scale, long quantity) {
        super(typeBytes, appData, maker, name, icon, image, description, asset_type);
        this.quantity = quantity;
        this.scale = (byte) scale;
    }

    public AssetVenture(int props, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, int asset_type, int scale, long quantity) {
        this(new byte[]{(byte) TYPE_ID, (byte) props}, appData, maker, name, icon, image, description, asset_type, scale, quantity);
    }

    public AssetVenture(byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, int asset_type, int scale, long quantity) {
        this(new byte[]{(byte) TYPE_ID, (byte) 0}, appData, maker, name, icon, image, description, asset_type, scale, quantity);
    }

    //GETTERS/SETTERS
    @Override
    public String getItemSubType() {
        return "common";
    }

    /**
     * 0 - unlimited for maker.
     * If < 0 - all but not maker may sold it on exchange for maker - as Auction.
     */
    @Override
    public long getQuantity() {
        return this.quantity;
    }

    public int getScale() {
        // TODO убрать это если будет новая цепочка с регулируемой точностью
        if (BlockChain.MAIN_MODE && this.key > 0 && this.key < 5 ||
                this.key > 1000 &&
                        this.key < BlockChain.AMOUNT_SCALE_FROM
        ) {
            //return this.assetType == 1? BlockChain.AMOUNT_DEDAULT_SCALE : 0;
            // IN ANY CASE
            return BlockChain.AMOUNT_DEDAULT_SCALE;
        }

        return this.scale;
    }

    @Override
    public BigDecimal getReleased(DCSet dcSet) {
        if (quantity > 0) {
            Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                    bals = this.getMaker().getBalance(this.getKey());
            return new BigDecimal(quantity).subtract(bals.a.b).stripTrailingZeros();
        } else {
            Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> bals = this.getMaker().getBalance(this.getKey());
            return bals.a.b.negate().stripTrailingZeros();
        }
    }

    @Override
    public BigDecimal getReleased() {
        return getReleased(DCSet.getInstance());
    }

    /**
     * Без ограничений - только если это счетная единица или сам владелец без ограничений
     *
     * @param address
     * @param notAccounting
     * @return
     */
    @Override
    public boolean isUnlimited(Account address, boolean notAccounting) {
        return !notAccounting && isAccounting() || getQuantity() == 0L && maker.equals(address);
    }

    @Override
    public boolean isUnique() {
        return quantity == 1L;
    }

    //PARSE
    // includeReference - TRUE only for store in local DB
    public static AssetVenture parse(int forDeal, byte[] data, boolean includeReference) throws Exception {

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        //READ CREATOR
        byte[] makerBytes = Arrays.copyOfRange(data, position, position + MAKER_LENGTH);
        PublicKeyAccount maker = new PublicKeyAccount(makerBytes);
        position += MAKER_LENGTH;

        //READ NAME
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

        //READ QUANTITY
        byte[] quantityBytes = Arrays.copyOfRange(data, position, position + QUANTITY_LENGTH);
        long quantity = Longs.fromByteArray(quantityBytes);
        position += QUANTITY_LENGTH;

        //READ SCALE
        byte[] scaleBytes = Arrays.copyOfRange(data, position, position + SCALE_LENGTH);
        byte scale = scaleBytes[0];
        position += SCALE_LENGTH;

        //READ ASSET TYPE
        int assetType = Byte.toUnsignedInt(Arrays.copyOfRange(data, position, position + ASSET_TYPE_LENGTH)[0]);
        position += ASSET_TYPE_LENGTH;

        //RETURN
        AssetVenture venture = new AssetVenture(typeBytes, appData, maker, name, icon, image, description, assetType, scale, quantity);
        if (includeReference) {
            venture.setReference(reference, dbRef);
        }

        return venture;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean includeReference, boolean onlyBody) {
        byte[] data = super.toBytes(forDeal, includeReference, onlyBody);

        //WRITE QUANTITY
        byte[] quantityBytes = Longs.toByteArray(this.quantity);
        data = Bytes.concat(data, quantityBytes);

        //WRITE SCALE_LENGTH
        byte[] scaleBytes = new byte[1];
        scaleBytes[0] = (byte) this.scale;
        data = Bytes.concat(data, scaleBytes);


        //WRITE ASSET TYPE
        byte[] assetTypeBytes = new byte[1];
        assetTypeBytes[0] = (byte) this.assetType;
        data = Bytes.concat(data, assetTypeBytes);

        return data;
    }

    public String makeHTMLView() {

        String text = super.makeHTMLHeadView();
        text += Lang.T("Quantity") + ":&nbsp;" + getQuantity() + ", "
                + Lang.T("Scale") + ":&nbsp;" + getScale() + "<br>";
        text += super.makeHTMLFootView(true);

        return text;

    }

    @Override
    public int getDataLength(boolean includeReference) {
        int len = super.getDataLength(includeReference);
        return len + SCALE_LENGTH + QUANTITY_LENGTH;
    }

    //OTHER

}
