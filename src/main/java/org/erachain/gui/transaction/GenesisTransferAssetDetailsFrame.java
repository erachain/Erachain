package org.erachain.gui.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.GenesisTransferAssetTransaction;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class GenesisTransferAssetDetailsFrame extends RecGenesis_DetailsFrame {
    public GenesisTransferAssetDetailsFrame(GenesisTransferAssetTransaction assetTransfer) {
        super(assetTransfer);

        boolean isCredit = false;
        if (assetTransfer.getCreator() != null) {
            isCredit = true;
            //LABEL OWNER
            ++labelGBC.gridy;
            JLabel makerLabel = new JLabel(Lang.T("Creditor") + ":");
            this.add(makerLabel, labelGBC);

            //RECIPIENT
            ++detailGBC.gridy;
            JTextField makerFld = new JTextField(assetTransfer.getCreator().getAddress());
            makerFld.setEditable(false);
            MenuPopupUtil.installContextMenu(makerFld);
            this.add(makerFld, detailGBC);

            String personMakerStr = assetTransfer.getCreator().viewPerson();
            if (personMakerStr.length() > 0) {
                ++labelGBC.gridy;
                ++detailGBC.gridy;
                this.add(new JLabel(personMakerStr), detailGBC);
            }
        }

        //LABEL RECIPIENT
        ++labelGBC.gridy;
        JLabel recipientLabel = new JLabel(Lang.T("Recipient") + ":");
        this.add(recipientLabel, labelGBC);

        //RECIPIENT
        ++detailGBC.gridy;
        JTextField recipient = new JTextField(assetTransfer.getRecipient().getAddress());
        recipient.setEditable(false);
        MenuPopupUtil.installContextMenu(recipient);
        this.add(recipient, detailGBC);

        String personStr = assetTransfer.getRecipient().viewPerson();
        if (personStr.length() > 0) {
            ++labelGBC.gridy;
            ++detailGBC.gridy;
            this.add(new JLabel(personStr), detailGBC);
        }

        //LABEL ASSET
        ++labelGBC.gridy;
        JLabel assetLabel = new JLabel(Lang.T("Asset") + ":");
        this.add(assetLabel, labelGBC);

        //ASSET
        ++detailGBC.gridy;
        JTextField asset = new JTextField(String.valueOf(Controller.getInstance()
                .getAsset(assetTransfer.getAbsKey()).toString()));
        asset.setEditable(false);
        MenuPopupUtil.installContextMenu(asset);
        this.add(asset, detailGBC);

        //LABEL AMOUNT
        ++labelGBC.gridy;
        JLabel amountLabel = new JLabel(Lang.T(isCredit ? "Credit" : "Amount") + ":");
        this.add(amountLabel, labelGBC);

        //AMOUNT
        ++detailGBC.gridy;
        JTextField amount = new JTextField(assetTransfer.getAmount().toPlainString());
        amount.setEditable(false);
        MenuPopupUtil.installContextMenu(amount);
        this.add(amount, detailGBC);

        //PACK
        //	this.pack();
        //      this.setResizable(false);
        //       this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
