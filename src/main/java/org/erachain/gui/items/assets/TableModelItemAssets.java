package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.gui.items.TableModelItemsSearch;
import org.erachain.lang.Lang;

import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class TableModelItemAssets extends TableModelItemsSearch {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_ASSET_TYPE = 3;
    public static final int COLUMN_AMOUNT = 4;
    public static final int COLUMN_FAVORITE = 5;
    public static final int COLUMN_I_OWNER = 6;

    private Boolean[] column_AutuHeight = new Boolean[]{false, true, true, false, false, false, false, false};
    private List<ItemCls> list;
    private String filter_Name = "";
    private long key_filter = 0;
    private ItemAssetMap db;

    public TableModelItemAssets() {
        super(new String[]{"Key", "Name", "Owner", "Type", "Quantity", "Favorite", "I Owner"});
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
        db = DCSet.getInstance().getItemAssetMap();
    }

    @Override
    public void findByName(String str) {
        filter_Name = str;
        list = db.get_By_Name(filter_Name, false);
        this.fireTableDataChanged();

    }

    @Override
    public void clear() {
        list = new ArrayList<ItemCls>();
        this.fireTableDataChanged();

    }

    // читаем колонки которые изменяем высоту
    public Boolean[] getColumnAutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void setColumnAutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }


    @Override
    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    public AssetCls getAsset(int row) {
        return (AssetCls) this.list.get(row);
    }

    @Override
    public ItemCls getItem(int row) {
        return this.list.get(row);
    }

    @Override
    public int getRowCount() {
        if (this.list == null)
            return 0;
        ;
        return this.list.size();

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        AssetCls asset = (AssetCls) this.list.get(row);

        switch (column) {
            case COLUMN_KEY:

                return asset.getKey();

            case COLUMN_NAME:

                return asset.viewName();

            case COLUMN_ASSET_TYPE:

                return Lang.getInstance().translate(asset.viewAssetType());

            case COLUMN_ADDRESS:

                return asset.getOwner().getPersonAsString();

            case COLUMN_AMOUNT:

                return asset.getTotalQuantity(DCSet.getInstance());

            case COLUMN_FAVORITE:

                return asset.isFavorite();

            case COLUMN_I_OWNER:

                if (Controller.getInstance().isAddressIsMine(asset.getOwner().getAddress()))
                    return true;
                return false;
        }

        return null;
    }


    @Override
    public void findByKey(String text) {
        // TODO Auto-generated method stub
        if (text.equals("") || text == null) return;
        if (!text.matches("[0-9]*")) return;
        key_filter = new Long(text);
        list = new ArrayList<ItemCls>();
        AssetCls pers = Controller.getInstance().getAsset(key_filter);
        if (pers == null) return;
        list.add(pers);
        this.fireTableDataChanged();

    }

    public void addObserversThis() {
    }

    public void removeObserversThis() {
    }

}
