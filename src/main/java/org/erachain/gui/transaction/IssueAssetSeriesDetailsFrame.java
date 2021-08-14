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

        if (assetSeriesTX.hasOriginal()) {
            //LABEL original REF
            ++labelGBC.gridy;
            this.add(new JLabel(Lang.T("Original Reference") + ":"), labelGBC);
            ++fieldGBC.gridy;
            JTextField origRef = new JTextField(assetSeriesTX.viewOrigAssetRef());
            origRef.setEditable(false);
            MenuPopupUtil.installContextMenu(origRef);
            this.add(origRef, fieldGBC);

            ++labelGBC.gridy;
            this.add(new JLabel(Lang.T("Original Asset") + ":"), labelGBC);
            ++fieldGBC.gridy;
            JTextField origAsset = new JTextField(assetSeriesTX.getOrigAsset().toString());
            origAsset.setEditable(false);
            this.add(origAsset, fieldGBC);
        }

        ++labelGBC.gridy;
        this.add(new JLabel(Lang.T("Series") + ":"), labelGBC);
        //PRICE
        ++fieldGBC.gridy;
        JTextField origKey = new JTextField(assetSeriesTX.getAsset().toString() + " #1/" + assetSeriesTX.getTotal());
        origKey.setEditable(false);
        MenuPopupUtil.installContextMenu(origKey);
        this.add(origKey, fieldGBC);

        //PACK
        //		this.pack();
        //        this.setResizable(false);
        //       this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
