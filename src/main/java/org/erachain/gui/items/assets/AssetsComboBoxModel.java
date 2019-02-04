package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.utils.ObserverMessage;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("serial")
public class AssetsComboBoxModel extends DefaultComboBoxModel<AssetCls> implements Observer {
    Lock lock = new ReentrantLock();

    public AssetsComboBoxModel() {
        if (Controller.getInstance().wallet.database != null)
            Controller.getInstance().wallet.database.getAssetFavoritesSet().addObserver(this);
    }

    public void deleteObserver() {
        if (Controller.getInstance().wallet.database != null)
        Controller.getInstance().wallet.database.getAssetFavoritesSet().deleteObserver(this);

    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            if (lock.tryLock()) {
                try {
                    this.syncUpdate(o, arg);
                } finally {
                    lock.unlock();
                }
            }

        } catch (Exception e) {
            //GUI ERROR
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        int type = message.getType();
        //CHECK IF LIST UPDATED
        if (type == ObserverMessage.LIST_ASSET_FAVORITES_TYPE
                ) {
            //GET SELECTED ITEM
            AssetCls selected = (AssetCls) this.getSelectedItem();
            int selectedIndex = -1;

            //EMPTY LIST
            this.removeAllElements();

            //INSERT ALL ITEMS
            Set<Long> keys = (Set<Long>) message.getValue();
            List<AssetCls> assets = new ArrayList<AssetCls>();
            int i = 0;
            for (Long key : keys) {
                if (key == 0) continue;

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
        } else if (type == ObserverMessage.ADD_ASSET_FAVORITES_TYPE) {
            this.addElement(Controller.getInstance().getAsset((long) message.getValue()));
           
        } else if (type == ObserverMessage.DELETE_ASSET_FAVORITES_TYPE) {
            this.removeElement(Controller.getInstance().getAsset((long) message.getValue()));
            
        }
    }
}
