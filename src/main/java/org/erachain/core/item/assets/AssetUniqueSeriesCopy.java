package org.erachain.core.item.assets;

//import java.math.BigDecimal;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.transaction.IssueAssetSeriesTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.webserver.PreviewMaker;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * копия - в ней нет данных при парсинге - наполняется только после loadExtData()
 */
public class AssetUniqueSeriesCopy extends AssetUnique {

    private static final int TYPE_ID = UNIQUE_COPY;

    protected static final int BASE_LENGTH = Long.BYTES + 2 * Short.BYTES;

    private final long origKey;
    private final int total;
    private final int index;
    AssetCls original;

    public AssetUniqueSeriesCopy(byte[] typeBytes, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image,
                                 String description, int assetType, long origKey, int total, int index) {
        super(typeBytes, appData, maker, name, icon, image, description, assetType);

        this.origKey = origKey;
        this.total = total;
        this.index = index;

    }

    public AssetUniqueSeriesCopy(byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image,
                                 String description, long origKey, int total, int index) {
        this(new byte[]{TYPE_ID, 0}, appData, maker, name, icon, image, description, AssetCls.AS_NON_FUNGIBLE,
                origKey, total, index);

    }

    public AssetUniqueSeriesCopy(long origKey, AssetVenture templateAsset, int total, int index) {
        this(templateAsset.getAppData(),
                templateAsset.getMaker(),
                templateAsset.getName(),
                templateAsset.getIcon(),
                templateAsset.getImage(),
                templateAsset.getDescription(),
                origKey, total, index);
    }

    @Override
    public int hashCode() {
        return (reference == null ? 0 : Ints.fromByteArray(reference)) + index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssetUniqueSeriesCopy) {
            AssetUniqueSeriesCopy item = (AssetUniqueSeriesCopy) obj;
            if (this.reference != null && item.reference != null)
                if (Arrays.equals(this.reference, item.reference))
                    return item.index == index;
        }
        return false;
    }

    // GETTERS/SETTERS
    @Override
    public String getItemSubType() {
        return "series";
    }

    public long getOrigKey() {
        return origKey;
    }

    public AssetCls getOriginal(DCSet dcSet) {
        if (origKey == 0) return null;

        if (original == null) {
            original = dcSet.getItemAssetMap().get(origKey);
        }
        return original;
    }

    @Override
    public byte[] getIcon() {
        if (origKey == 0) return null;

        getOriginal(DCSet.getInstance());

        return original.getIcon();

    }

    @Override
    public byte[] getImage() {
        if (origKey == 0) return null;

        getOriginal(DCSet.getInstance());

        return original.getImage();

    }

    public boolean hasOriginal() {
        return origKey > 0;
    }

    public int getTotal() {
        return total;
    }

    public int getIndex() {
        return index;
    }

    /**
     * Первая копия - содержит обертку полностью, все остальные только номера/ Оригинал тут вообще не используем.
     *
     * @param templateAsset
     * @param origKey
     * @param total
     * @param index
     * @return
     */
    public static AssetUniqueSeriesCopy makeCopy(Transaction issueTX, AssetCls templateAsset, long origKey, int total, int index) {
        String copyName = templateAsset.getName() + " #" + index + "/" + total;

        AssetUniqueSeriesCopy copy;
        byte[] icon = templateAsset.getIcon();
        byte[] image = templateAsset.getImage();
        if (index == 1) {
            // Make template
            copy = new AssetUniqueSeriesCopy(templateAsset.getAppData(), templateAsset.getMaker(), copyName,
                    icon, image, templateAsset.getDescription(), origKey, total, index);

        } else {
            // Make COPY

            // SET ICON and IMAGE URL
            byte[] appDataOfCopy = templateAsset.getAppData();
            if (appDataOfCopy == null) {
                appDataOfCopy = AssetCls.makeEmptyAppData();
            } else {
                appDataOfCopy = appDataOfCopy.clone();
            }

            if (icon != null && icon.length > 0) {
                appDataOfCopy[10] = (byte) templateAsset.getIconType();
                appDataOfCopy[10] |= ITEM_HAS_URL_MASK;
                icon = templateAsset.getIconURL().getBytes();
            }
            if (image != null && image.length > 0) {
                appDataOfCopy[11] = (byte) templateAsset.getImageType();
                appDataOfCopy[11] |= ITEM_HAS_URL_MASK;
                image = templateAsset.getImageURL().getBytes();
            }

            copy = new AssetUniqueSeriesCopy(appDataOfCopy, templateAsset.getMaker(), copyName,
                    icon, image, templateAsset.getDescription(), origKey, total, index);
        }

        copy.setReference(issueTX.getSignature(), issueTX.getDBRef());

        return copy;
    }

    //PARSE
    // includeReference - TRUE only for store in local DB
    public static AssetUniqueSeriesCopy parse(int forDeal, byte[] data, boolean includeReference) throws Exception {

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

        //READ ASSET TYPE
        int assetType = Byte.toUnsignedInt(Arrays.copyOfRange(data, position, position + ASSET_TYPE_LENGTH)[0]);
        position += ASSET_TYPE_LENGTH;

        //READ ORIGINAL ASSET KEY
        byte[] origKeyBytes = Arrays.copyOfRange(data, position, position + Long.BYTES);
        long origKey = Longs.fromByteArray(origKeyBytes);
        position += Long.BYTES;

        //READ TOTAL
        byte[] totalBytes = Arrays.copyOfRange(data, position, position + Short.BYTES);
        int total = Shorts.fromByteArray(totalBytes);
        position += Short.BYTES;

        //READ INDEX
        byte[] indexBytes = Arrays.copyOfRange(data, position, position + Short.BYTES);
        int index = Shorts.fromByteArray(indexBytes);
        position += Short.BYTES;

        //RETURN
        AssetUniqueSeriesCopy uniqueCopy = new AssetUniqueSeriesCopy(typeBytes, appData, maker, name, icon, image,
                description, assetType, origKey, total, index);

        if (includeReference) {
            uniqueCopy.setReference(reference, dbRef);
        }

        return uniqueCopy;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean includeReference, boolean forMakerSign) {

        byte[] parentData = super.toBytes(forDeal, includeReference, forMakerSign);

        byte[] data = new byte[BASE_LENGTH];

        System.arraycopy(Longs.toByteArray(origKey), 0, data, 0, Long.BYTES);
        System.arraycopy(Shorts.toByteArray((short) total), 0, data, 8, Short.BYTES);
        System.arraycopy(Shorts.toByteArray((short) index), 0, data, 10, Short.BYTES);

        return Bytes.concat(parentData, data);
    }

    public int getDataLength(boolean includeReference) {
        return super.getDataLength(includeReference) + BASE_LENGTH;
    }

    //OTHER

    public JSONObject toJson() {

        // тут же referenceTx определяется
        JSONObject assetJSON = super.toJson();
        assetJSON.put("index", index);
        assetJSON.put("total", total);

        if (origKey > 0) {
            // ADD DATA of ORIGINAL
            JSONObject originalJson = new JSONObject();
            originalJson.put("key", origKey);

            IssueAssetSeriesTransaction issueTX = (IssueAssetSeriesTransaction) referenceTx;
            if (issueTX.getDCSet() == null) {
                issueTX.setDC(Controller.getInstance().getDCSet(), false);
            }
            AssetCls original = issueTX.getOrigAsset();


            String iconURL = original.getIconURL();
            if (iconURL != null) {
                originalJson.put("iconURL", iconURL);
                originalJson.put("iconType", original.getIconType());
                //originalJson.put("iconTypeName", original.getIconTypeName());
                originalJson.put("iconMediaType", original.getIconMediaType().toString());
            }

            String imageURL = original.getImageURL();
            if (imageURL != null) {
                originalJson.put("imageURL", imageURL);
                originalJson.put("imageType", original.getImageType());
                //originalJson.put("imageTypeName", original.getImageTypeName());
                originalJson.put("imageMediaType", original.getImageMediaType().toString());
                originalJson.put("imagePreviewMediaType", PreviewMaker.getPreviewType(original).toString());
            }

            assetJSON.put("original", originalJson);
        }

        return assetJSON;

    }

    @Override
    public JSONObject jsonForExplorerInfo(DCSet dcSet, JSONObject langObj, boolean forPrint) {
        JSONObject itemJson = super.jsonForExplorerInfo(dcSet, langObj, forPrint);
        itemJson.put("Label_Original_Asset", Lang.T("Original Asset", langObj));
        itemJson.put("Label_Series", Lang.T("Series", langObj));

        return itemJson;
    }

    public String makeHTMLView() {

        String text = super.makeHTMLHeadView();
        if (origKey > 0) {
            text += Lang.T("Original Asset") + ":&nbsp;" + origKey + "<br>";
        }
        text += Lang.T("Series") + ":&nbsp;" + total + ", "
                + Lang.T("Index") + ":&nbsp;" + index + "<br>";
        text += super.makeHTMLFootView(true);

        return text;

    }

}
