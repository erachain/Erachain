package org.erachain.gui.transaction;

import org.erachain.core.transaction.IssueAssetSeriesTransaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class IssueAssetSeriesDetailsFrame extends RecDetailsFrame {
    public IssueAssetSeriesDetailsFrame(IssueAssetSeriesTransaction assetSeriesTX) {
        super(assetSeriesTX, true);

        assetSeriesTX.setDC(DCSet.getInstance(), true);

        //LABEL original REF
        ++labelGBC.gridy;
        this.add(new JLabel(Lang.T("Asset Reference") + ":"), labelGBC);
        //PRICE
        ++fieldGBC.gridy;
        JTextField origRef = new JTextField(assetSeriesTX.viewOrigAssetRef());
        origRef.setEditable(false);
        MenuPopupUtil.installContextMenu(origRef);
        this.add(origRef, fieldGBC);

        ++labelGBC.gridy;
        this.add(new JLabel(Lang.T("Asset") + ":"), labelGBC);
        //PRICE
        ++fieldGBC.gridy;
        JTextField origKey = new JTextField(assetSeriesTX.getAsset().toString());
        origKey.setEditable(false);
        MenuPopupUtil.installContextMenu(origKey);
        this.add(origKey, fieldGBC);

        //LABEL TOTAL
        ++labelGBC.gridy;
        this.add(new JLabel(Lang.T("Total") + ":"), labelGBC);

        //TOTAL
        ++fieldGBC.gridy;
        JTextField total = new JTextField(assetSeriesTX.getTotal());
        total.setEditable(false);
        MenuPopupUtil.installContextMenu(total);
        this.add(total, fieldGBC);

        //PACK
        //		this.pack();
        //        this.setResizable(false);
        //       this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
