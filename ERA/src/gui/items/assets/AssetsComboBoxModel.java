package gui.items.assets;

import controller.Controller;
import core.item.assets.AssetCls;
import utils.ObserverMessage;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("serial")
public class AssetsComboBoxModel extends DefaultComboBoxModel<AssetCls> implements Observer {
    Lock lock = new ReentrantLock();

    public AssetsComboBoxModel() {
        Controller.getInstance().wallet.database.getAssetFavoritesSet().addObserver(this);
    }

    public void deleteObserver() {

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

        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.LIST_ASSET_FAVORITES_TYPE
              
                
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
        if (message.getType() == ObserverMessage.ADD_ASSET_FAVORITES_TYPE) {
            this.addElement(Controller.getInstance().getAsset((long) message.getValue()));
           
        }
        if (message.getType() == ObserverMessage.DELETE_ASSET_FAVORITES_TYPE) {
            this.removeElement(Controller.getInstance().getAsset((long) message.getValue()));
            
        }
    }
}
