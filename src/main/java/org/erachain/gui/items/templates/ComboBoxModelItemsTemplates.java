package org.erachain.gui.items.templates;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.gui.models.FavoriteComboBoxModel;

@SuppressWarnings("serial")
public class ComboBoxModelItemsTemplates extends FavoriteComboBoxModel {

    public ComboBoxModelItemsTemplates() {
        super(ItemCls.TEMPLATE_TYPE);
    }

    public void setObservable() {
        this.observable = Controller.getInstance().getWallet().database.getTemplateFavoritesSet();
    }

}
