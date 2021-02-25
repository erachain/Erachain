package org.erachain.gui.transaction;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.IssueAssetTransaction;
import org.erachain.gui.items.assets.AssetInfo;
import org.erachain.lang.Lang;

import javax.swing.*;

@SuppressWarnings("serial")
public class IssueAssetDetailsFrame extends RecDetailsFrame {
    public IssueAssetDetailsFrame(IssueAssetTransaction assetIssue) {
        super(assetIssue, false);

        AssetInfo as_info = new AssetInfo((AssetCls) assetIssue.getItem(), false);
        //LABEL NAME
        ++labelGBC.gridy;
        labelGBC.gridwidth = 4;
        labelGBC.fill = labelGBC.BOTH;
        labelGBC.weightx = 0.1;
        labelGBC.weightx = 0.1;
        JLabel nameLabel = new JLabel(Lang.T("Name") + ":");
        this.add(as_info, labelGBC);

        //PACK
        //	this.pack();
        //    this.setResizable(false);
        //    this.setLocationRelativeTo(null);
        this.setVisible(true);

    }
}
