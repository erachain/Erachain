package org.erachain.core.item.assets;

//import java.math.BigDecimal;

import com.google.common.primitives.Shorts;
import org.erachain.datachain.ItemMap;
import org.erachain.lang.Lang;

import java.util.Arrays;

/**
 * копия - в ней нет данных при парсинге - наполняется только после loadExtData()
 */
public class AssetUniqueSeriesCopy extends AssetUnique {

    private static final int TYPE_ID = UNIQUE_COPY;

    protected static final int BASE_LENGTH = TYPE_LENGTH + 2 * Short.BYTES;

    private int total;
    private int index;
    private AssetUnique baseItem;

    public AssetUniqueSeriesCopy(byte[] typeBytes, int total, int index) {
        super(typeBytes);

        this.total = total;
        this.index = index;

    }

    public AssetUniqueSeriesCopy(AssetUnique baseItem, AssetCls prototypeAsset, int total, int index) {
        this(new byte[]{TYPE_ID, 0}, total, index);
        this.baseItem = baseItem;
    }

    // GETTERS/SETTERS
    @Override
    public String getItemSubType() {
        return "unique copy";
    }

    public int getTotal() {
        return total;
    }

    public int getIndex() {
        return index;
    }

    public void loadExtData(ItemMap itemMap) {
        baseItem = (AssetUnique) itemMap.get(key - index + 1);
        flags = baseItem.getFlags();
        name = baseItem.getName();
        appData = baseItem.getAppData();
        description = baseItem.getDescription();
        icon = baseItem.getIcon();
        iconAsURL = baseItem.hasIconURL();
        image = baseItem.getImage();
        imageAsURL = baseItem.hasImageURL();
        startDate = baseItem.getStartDate();
        startDate = baseItem.getStopDate();
        tags = baseItem.getTagsSelf();

    }

    //PARSE
    // includeReference - TRUE only for store in local DB
    public static AssetUniqueSeriesCopy parse(int forDeal, byte[] data, boolean includeReference) throws Exception {

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        //READ TOTAL
        byte[] totalBytes = Arrays.copyOfRange(data, position, position + Short.BYTES);
        int total = Shorts.fromByteArray(totalBytes);
        position += Short.BYTES;

        //READ INDEX
        byte[] indexBytes = Arrays.copyOfRange(data, position, position + Short.BYTES);
        int index = Shorts.fromByteArray(indexBytes);
        position += Short.BYTES;

        //RETURN
        AssetUniqueSeriesCopy unique = new AssetUniqueSeriesCopy(typeBytes, total, index);

        return unique;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean includeReference, boolean forMakerSign) {

        byte[] data = new byte[BASE_LENGTH];

        //WRITE ASSET TYPE
        data[0] = (byte) TYPE_ID;

        System.arraycopy(Shorts.toByteArray((short) total), 0, data, 2, Short.BYTES);
        System.arraycopy(Shorts.toByteArray((short) index), 0, data, 4, Short.BYTES);

        return data;
    }

    public int getDataLength(boolean includeReference) {
        return BASE_LENGTH;
    }

    //OTHER

    public String makeHTMLView() {

        String text = super.makeHTMLHeadView();
        text += Lang.T("Series") + ":&nbsp;" + total + ", "
                + Lang.T("Index") + ":&nbsp;" + index + "<br>";
        text += super.makeHTMLFootView(true);

        return text;

    }

}
