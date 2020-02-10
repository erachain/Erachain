package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.AssetCls;

import javax.swing.*;

@SuppressWarnings("serial")
public class FundTokensComboBoxModel extends DefaultComboBoxModel<AssetCls> {

    public FundTokensComboBoxModel(boolean deposit) {

        AssetCls asset;
        if (BlockChain.TEST_MODE) {
            //this.addElement(Controller.getInstance().getAsset(1031));


            for (Long key : new Long[]{1077L, 1078L, 1079L, 2L}) {
                asset = Controller.getInstance().getAsset(key);
                if (asset == null)
                    continue;
                this.addElement(asset);
            }

            if (deposit) {
                this.addElement(Controller.getInstance().getAsset(1L));
            } else {
            }

        } else {
            for (Long key: new Long[]{12L, 2L}) {
                asset = Controller.getInstance().getAsset(key);
                if (asset == null)
                    continue;

                this.addElement(asset);
            }

            if (deposit) {
                this.addElement(Controller.getInstance().getAsset(1L));
            } else {
            }
            //this.addElement(Controller.getInstance().getAsset(14)); // ETH
            //this.addElement(Controller.getInstance().getAsset(92)); // RUB
            //this.addElement(Controller.getInstance().getAsset(95)); // USD
        }

    }

}
