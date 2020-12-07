package org.erachain.gui.items.statuses;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.transaction.IssueStatusRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;

import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

public class IssueStatusPanel extends IssueItemPanel {

    public static String NAME = "IssueStatusPanel";
    public static String TITLE = "Issue Status";

    private JCheckBox jcheckUnique;


    public IssueStatusPanel() {
        super(NAME, TITLE);

        setLayout(new GridBagLayout());
        String colorText = "FF0000";


        JLabel singleLabel = new JLabel(Lang.getInstance().translate("Single") + ":");
        GridBagConstraints gbcSingleLabel = new GridBagConstraints();
        gbcSingleLabel.gridx = 1;
        gbcSingleLabel.gridy = 7;
        gbcSingleLabel.anchor = GridBagConstraints.NORTHEAST;
        add(singleLabel, gbcSingleLabel);



        jcheckUnique = new JCheckBox();
        GridBagConstraints gbcJCheckUnique = new GridBagConstraints();
        gbcJCheckUnique.gridx = 2;
        gbcJCheckUnique.gridy = 7;
        gbcJCheckUnique.anchor = GridBagConstraints.NORTHEAST;
        add(jcheckUnique, gbcJCheckUnique);


        setVisible(true);
    }

    public void onIssueClick() {
        // DISABLE
        issueJButton.setEnabled(false);
        if (checkWalletUnlock(issueJButton)) {
            return;
        }

        // READ CREATOR
        Account sender = (Account) fromJComboBox.getSelectedItem();

        ExLink exLink = null;
        Long linkRef = Transaction.parseDBRef(exLinkText.getText());
        if (linkRef != null) {
            exLink = new ExLinkAppendix(linkRef);
        }

        int feePow;
        try {
            feePow = Integer.parseInt((String) this.textFeePow.getSelectedItem());
        } catch (Exception e) {
            String mess = "Invalid fee power 0..6";
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(e + mess),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            issueJButton.setEnabled(true);
            return;
        }
        byte[] icon = addLogoIconLabel.getImgBytes();
        byte[] image = addImageLabel.getImgBytes();
        boolean unique = jcheckUnique.isSelected();
        PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.getInstance().translate(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        IssueStatusRecord issueStatus = (IssueStatusRecord) Controller.getInstance().issueStatus(creator,
                exLink, textName.getText(), textAreaDescription.getText(), unique, icon, image, feePow);
        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                + Lang.getInstance().translate("Create Status") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issueStatus.getCreator() + "<br>";
        text += Lang.getInstance().translate("Name") + ":&nbsp;" + issueStatus.getItem().viewName() + "<br>";
        text += Lang.getInstance().translate("Description") + ":<br>"
                + Library.to_HTML(issueStatus.getItem().getDescription()) + "<br>";
        text += Lang.getInstance().translate("Unique") + ": " + ((StatusCls) issueStatus.getItem()).isUnique()
                + "<br>";
        String Status_text = "";
        IssueConfirmDialog issueConfirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, issueStatus,
                text,
                (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation Transaction"));
        issueConfirmDialog.setLocationRelativeTo(this);
        issueConfirmDialog.setVisible(true);
        if (!issueConfirmDialog.isConfirm) {
            issueJButton.setEnabled(true);
            return;
        }
        // VALIDATE AND PROCESS
        int result = Controller.getInstance().getTransactionCreator().afterCreate(issueStatus, Transaction.FOR_NETWORK);
        // CHECK VALIDATE MESSAGE
        if (result == Transaction.VALIDATE_OK) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Status issue has been sent") + "!",
                    Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
            clearPanel();
        } else {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }
        // ENABLE
        issueJButton.setEnabled(true);
    }

    private void clearPanel() {
        textName.setText("");
        textAreaDescription.setText("");
        textFeePow.setSelectedItem("0");
    }

}
