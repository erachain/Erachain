package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.wallet.FavoriteItemMap;
import org.erachain.gui.ObserverWaiter;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("serial")
public class FundTokensComboBoxModel extends DefaultComboBoxModel<AssetCls> {

    public FundTokensComboBoxModel() {
        if (BlockChain.DEVELOP_USE) {
            //this.addElement(Controller.getInstance().getAsset(1031));
            this.addElement(Controller.getInstance().getAsset(1077));
            this.addElement(Controller.getInstance().getAsset(1078));
            this.addElement(Controller.getInstance().getAsset(1079));
            this.addElement(Controller.getInstance().getAsset(2));
        } else {
            this.addElement(Controller.getInstance().getAsset(12)); // BTC
            //this.addElement(Controller.getInstance().getAsset(14)); // ETH
            //this.addElement(Controller.getInstance().getAsset(92)); // RUB
            //this.addElement(Controller.getInstance().getAsset(95)); // USD
        }

    }

}
