package gui.transaction;

import controller.Controller;
import core.transaction.GenesisTransferAssetTransaction;
import lang.Lang;
import utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class GenesisTransferAssetDetailsFrame extends RecGenesis_DetailsFrame {
    public GenesisTransferAssetDetailsFrame(GenesisTransferAssetTransaction assetTransfer) {
        super(assetTransfer);

        boolean isCredit = false;
        if (assetTransfer.getOwner() != null) {
            isCredit = true;
            //LABEL OWNER
            ++labelGBC.gridy;
            JLabel ownerLabel = new JLabel(Lang.getInstance().translate("Creditor") + ":");
            this.add(ownerLabel, labelGBC);

            //RECIPIENT
            ++detailGBC.gridy;
            JTextField ownerFld = new JTextField(assetTransfer.getOwner().getAddress());
            ownerFld.setEditable(false);
            MenuPopupUtil.installContextMenu(ownerFld);
            this.add(ownerFld, detailGBC);

            String personOwnerStr = assetTransfer.getOwner().viewPerson();
            if (personOwnerStr.length() > 0) {
                ++labelGBC.gridy;
                ++detailGBC.gridy;
                this.add(new JLabel(personOwnerStr), detailGBC);
            }
        }

        //LABEL RECIPIENT
        ++labelGBC.gridy;
        JLabel recipientLabel = new JLabel(Lang.getInstance().translate("Recipient") + ":");
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
        JLabel assetLabel = new JLabel(Lang.getInstance().translate("Asset") + ":");
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
        JLabel amountLabel = new JLabel(Lang.getInstance().translate(isCredit ? "Credit" : "Amount") + ":");
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
