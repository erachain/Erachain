package org.erachain.gui.items.statuses;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.gui.models.FavoriteComboBoxModel;

@SuppressWarnings("serial")
public class ComboBoxStatusesModel extends FavoriteComboBoxModel {

    public ComboBoxStatusesModel() {
        super(ItemCls.STATUS_TYPE);
    }

    public void setObservable() {
        this.observable = Controller.getInstance().getWallet().database.getStatusFavoritesSet();
    }

}
