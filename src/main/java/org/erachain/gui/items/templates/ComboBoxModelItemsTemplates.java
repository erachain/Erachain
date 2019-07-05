package org.erachain.gui.items.templates;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.gui.items.ComboBoxModelItems;
import org.erachain.gui.models.FavoriteComboBoxModel;
import org.erachain.utils.ObserverMessage;

@SuppressWarnings("serial")
public class ComboBoxModelItemsTemplates extends FavoriteComboBoxModel {

    public ComboBoxModelItemsTemplates() {
        super(ItemCls.TEMPLATE_TYPE);
    }

    public void setObservable() {
        this.observable = Controller.getInstance().wallet.database.getTemplateFavoritesSet();
    }

}
