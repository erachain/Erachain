package org.erachain.gui.items.statuses;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.database.wallet.FavoriteItemMap;
import org.erachain.gui.models.FavoriteComboBoxModel;
import org.erachain.utils.ObserverMessage;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("serial")
public class ComboBoxStatusesModel extends FavoriteComboBoxModel {

    public ComboBoxStatusesModel() {
        super(ItemCls.STATUS_TYPE);
    }

    public void setObservable() {
        this.observable = Controller.getInstance().wallet.database.getAssetFavoritesSet();
    }

}
