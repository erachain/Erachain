package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.DBMap;
import org.erachain.gui.ObserverWaiter;
import org.erachain.gui.models.WalletComboBoxModel;
import org.erachain.utils.ObserverMessage;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("serial")
public class AssetsComboBoxModel extends WalletComboBoxModel<AssetCls> {


    public AssetsComboBoxModel() {
    }

    public AssetCls getElementByEvent(Object key) {
        return Controller.getInstance().getAsset((long) key);
    }

    public void sortAndAdd() {
        //GET SELECTED ITEM
        AssetCls selected = (AssetCls) this.getSelectedItem();
        int selectedIndex = -1;

        //EMPTY LIST
        this.removeAllElements();

        //INSERT ALL ITEMS
        Set<Long> keys = ((DBMap)observable).getKeys();
        List<AssetCls> assets = new ArrayList<AssetCls>();
        int i = 0;
        for (Long key : keys) {
            if (key == 0
                    || key > 2 && key < 10
            )
                continue;

            //GET ASSET
            AssetCls asset = Controller.getInstance().getAsset(key);
            if (asset == null)
                continue;

            assets.add(asset);

            //ADD

            this.addElement(asset);

            if (selected != null && asset.getKey() == selected.getKey()) {
                selectedIndex = i;
                selected = asset; // need for SELECT as OBJECT
            }

            i++;
        }

        //RESET SELECTED ITEM
        if (this.getIndexOf(selected) != -1) {
            for (AssetCls asset : assets) {
                if (asset.getKey() == selected.getKey()) {
                    this.setSelectedItem(asset);
                    return;
                }
            }
        }

    }

    public void setObservable() {
        this.observable = Controller.getInstance().wallet.database.getAssetFavoritesSet();
    }

}
