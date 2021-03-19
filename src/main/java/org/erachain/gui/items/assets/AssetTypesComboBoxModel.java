package org.erachain.gui.items.assets;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetType;

import javax.swing.*;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class AssetTypesComboBoxModel extends DefaultComboBoxModel<AssetType> {
    
    public AssetTypesComboBoxModel() {
        // INSERT ALL ACCOUNTS

        ArrayList<AssetType> list = new ArrayList<AssetType>();

        for (int type : AssetCls.assetTypes()) {
            this.addElement(new AssetType(type));
        }

    }
    
}
