package org.erachain.core.item;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetUnique;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ItemClsTest {

    @Test
    public void insertToMap() {
        // see org.erachain.core.transaction.IssueAssetTransactionTest.test1
    }

    @Test
    public void deleteFromMap() {
        // see org.erachain.core.transaction.IssueAssetTransactionTest.test1
    }

    @Test
    public void parseAppData() {
        byte[] appData = AssetCls.makeAppData(1L, true, 7, true, 7);
        ItemCls item = new AssetUnique(appData, null, "String name", null, null, "String description", 3);

        assertEquals(item.getFlags(), 1L);
        assertEquals(item.hasIconURL(), true);
        assertEquals(item.hasImageURL(), true);
        assertEquals(item.iconType, 7);
        assertEquals(item.imageType, 7);

    }

    @Test
    public void makeAppData() {
        //System.out.println(Long.toString(1L << 63, 2));
        //System.out.println(Long.toString(1L << 62, 2));
        //System.out.println(Long.toString((4L + 2L + 1L) << 59, 2));
        //System.out.println(Long.toString((4L + 2L + 1L) << 56, 2));

        //System.out.println(Long.toUnsignedString(ItemCls.ITEM_HAS_ICON_URL_MASK, 2) + " - ITEM_HAS_ICON_URL_MASK");
        //System.out.println("0" + Long.toString(ItemCls.ITEM_HAS_IMAGE_URL_MASK, 2) + " - ITEM_HAS_IMAGE_URL_MASK");

        System.out.println("1234567812345678123456781234567812345678123456781234567812345678");
        byte[] appdata = AssetCls.makeAppData(0L, false, 0, false, 0);
        assertEquals(appdata, null);

        appdata = AssetCls.makeAppData(1L, true, 7, false, 7);
        assertEquals("10000000", Integer.toBinaryString(Byte.toUnsignedInt(appdata[0])));
        assertEquals("11111001", Integer.toBinaryString(Byte.toUnsignedInt(appdata[10])));
        assertEquals("111", Integer.toBinaryString(Byte.toUnsignedInt(appdata[11])));

        if (true) {
            // for see
            for (byte b : appdata) {
                System.out.print(Integer.toBinaryString(Byte.toUnsignedInt(b)) + " ");
            }
        }
    }
}