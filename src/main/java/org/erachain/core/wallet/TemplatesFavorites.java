package org.erachain.core.wallet;

import org.erachain.controller.Controller;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.gui.Gui;
import org.erachain.utils.ObserverMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class TemplatesFavorites implements Observer {

    private List<Long> favorites;

    public TemplatesFavorites() {
        this.favorites = new ArrayList<Long>();

        Controller.getInstance().addWalletObserver(this);
        Controller.getInstance().addObserver(this);
    }

    public List<Long> getKeys() {
        return this.favorites;
    }

    public List<TemplateCls> getTemplates() {
        List<TemplateCls> template = new ArrayList<TemplateCls>();
        for (Long key : this.favorites) {
            template.add(Controller.getInstance().getItemTemplate(key));
        }
        return template;
    }


    @Override
    // if some changed in wallet - reload favorites
    public void update(Observable o, Object arg) {

        if (!Gui.isGuiStarted()) {
            return;
        }

        ObserverMessage message = (ObserverMessage) arg;

        if ((message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK)
                || ((Controller.getInstance().getStatus() == Controller.STATUS_OK) &&
                (
                        message.getType() == ObserverMessage.ADD_ACCOUNT_TYPE
                                || message.getType() == ObserverMessage.REMOVE_ACCOUNT_TYPE
                ))) {
        }
    }
}