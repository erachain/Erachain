package org.erachain.core.item.assets;

//import java.math.BigDecimal;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Shorts;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.ItemMap;
import org.erachain.lang.Lang;

import java.util.Arrays;

/**
 * копия - в ней нет данных при парсинге - наполняется только после loadExtData()
 */
public class AssetUniqueSeriesCopy extends AssetUnique {

    private static final int TYPE_ID = UNIQUE_COPY;

    private int index;
    private AssetUniqueSeries baseItem;

    public AssetUniqueSeriesCopy(byte[] typeBytes, int index) {
        super(typeBytes);

        this.index = index;

    }

    public AssetUniqueSeriesCopy(int index) {
        this(new byte[]{TYPE_ID, 0}, index);
    }

    public AssetUniqueSeriesCopy(AssetUniqueSeries baseItem, int index) {
        this(new byte[]{TYPE_ID, 0}, index);
        this.baseItem = baseItem;
    }

    // GETTERS/SETTERS
    @Override
    public String getItemSubType() {
        return "unique copy";
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int isValid() {
        if (index > baseItem.getTotal() || index < 1) {
            return Transaction.INVALID_ITEM_INDEX;
        }

        return Transaction.VALIDATE_OK;
    }

    /**
     *
     */
    public void loadExtData(ItemMap itemMap) {
        baseItem = (AssetUniqueSeries) itemMap.get(key - index + 1);
    }

    //PARSE
    // includeReference - TRUE only for store in local DB
    public static AssetUniqueSeriesCopy parse(int forDeal, byte[] data, boolean includeReference) throws Exception {

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        //READ INDEX
        byte[] indexBytes = Arrays.copyOfRange(data, position, position + Short.BYTES);
        int index = Shorts.fromByteArray(indexBytes);
        position += Short.BYTES;

        //RETURN
        AssetUniqueSeriesCopy unique = new AssetUniqueSeriesCopy(typeBytes, index);

        return unique;
    }

    @Override
    public byte[] toBytes(int forDeal, boolean includeReference, boolean forMakerSign) {

        byte[] data = this.typeBytes;

        data = Bytes.concat(data, Shorts.toByteArray((short) index));

        return data;
    }

    public int getDataLength(boolean includeReference) {
        return TYPE_LENGTH + Short.BYTES;
    }

    //OTHER

    public String makeHTMLView() {

        String text = super.makeHTMLHeadView();
        text += Lang.T("Series") + ":&nbsp;" + baseItem.getTotal() + ", "
                + Lang.T("Index") + ":&nbsp;" + getIndex() + "<br>";
        text += super.makeHTMLFootView(true);

        return text;

    }

}
