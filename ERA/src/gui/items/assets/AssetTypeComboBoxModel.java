package gui.items.assets;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import core.item.assets.AssetCls;
import lang.Lang;

public class AssetTypeComboBoxModel extends AbstractListModel implements ComboBoxModel {
    Integer[] list = { AssetCls.AS_OUTSIDE_GOODS, AssetCls.AS_OUTSIDE_IMMOVABLE, AssetCls.AS_OUTSIDE_CLAIM,
            AssetCls.AS_INSIDE_ASSETS, AssetCls.AS_ACCOUNTING };

    String selection = null;

    public AssetTypeComboBoxModel() {

    }

    public Object getElementAt(int index) {
        int asset_type = list[index];
        return Lang.getInstance().translate(AssetCls.viewAssetTypeCls(asset_type));
    }

    public int getSize() {
        return list.length;
    }

    public void setSelectedItem(Object anItem) {
        selection = (String) anItem;

    }

    // Methods implemented from the interface ComboBoxModel
    public Object getSelectedItem() {
        return selection; // to add the selection to the combo box
    }

    public Integer getSelectedType() {
        if (selection.equals(Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_GOODS))))
            return AssetCls.AS_OUTSIDE_GOODS;

        if (selection.equals(Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_IMMOVABLE))))
            return AssetCls.AS_OUTSIDE_IMMOVABLE;

        if (selection.equals(Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_CLAIM))))
            return AssetCls.AS_OUTSIDE_CLAIM;

        if (selection.equals(Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_ASSETS))))
            return AssetCls.AS_INSIDE_ASSETS;

        if (selection.equals(Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_ACCOUNTING))))
            return AssetCls.AS_ACCOUNTING;

        return -1;

    }
}