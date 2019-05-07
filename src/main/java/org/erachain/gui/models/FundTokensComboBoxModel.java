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
        this.addElement(Controller.getInstance().getAsset(BlockChain.DEVELOP_USE? 1031 : 12));
        this.addElement(Controller.getInstance().getAsset(BlockChain.DEVELOP_USE? 1077 : 95));
        this.addElement(Controller.getInstance().getAsset(2));

    }

}
