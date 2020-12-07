package org.erachain.gui.items.templates;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.IssueTemplateRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.transaction.IssueTemplateDetailsFrame;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;

import javax.swing.*;

import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

@SuppressWarnings("serial")
public class IssueTemplatePanel extends IssueItemPanel {

    public static String NAME = "IssueTemplatePanel";
    public static String TITLE = "Issue Template";

    public IssueTemplatePanel() {
        super(NAME, TITLE);

        initComponents();
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
            // READ FEE POW
            feePow = Integer.parseInt((String) textFeePow.getSelectedItem());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            // ENABLE
            issueJButton.setEnabled(true);
            return;
        }

        byte[] icon = addLogoIconLabel.getImgBytes();
        byte[] image = addImageLabel.getImgBytes();

        // CREATE PLATE
        PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.getInstance().translate(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        IssueTemplateRecord issueTemplate = (IssueTemplateRecord) Controller.getInstance().issueTemplate(creator,
                exLink, textName.getText(), textAreaDescription.getText(), icon, image, feePow);

        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation transaction issue template") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issueTemplate.getCreator() + "<br>";
        text += Lang.getInstance().translate("Title") + ":&nbsp;" + issueTemplate.getItem().viewName() + "<br>";
        text += Lang.getInstance().translate("Description") + ":<br>"
                + Library.to_HTML(issueTemplate.getItem().getDescription()) + "<br>";
        String Status_text = "";

        IssueConfirmDialog issueConfirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, issueTemplate,
                text, (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation transaction issue template"));
        IssueTemplateDetailsFrame issueTemplateDetailsFrame = new IssueTemplateDetailsFrame(issueTemplate);
        issueConfirmDialog.jScrollPane1.setViewportView(issueTemplateDetailsFrame);
        issueConfirmDialog.setLocationRelativeTo(this);
        issueConfirmDialog.setVisible(true);
        // JOptionPane.OK_OPTION
        if (!issueConfirmDialog.isConfirm) {
            issueJButton.setEnabled(true);
            return;
        }
        // VALIDATE AND PROCESS
        int result = Controller.getInstance().getTransactionCreator().
                afterCreate(issueTemplate, Transaction.FOR_NETWORK);
        // CHECK VALIDATE MESSAGE
        switch (result) {
            case Transaction.VALIDATE_OK:
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Template issue has been sent") + "!",
                        Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                break;

            case Transaction.NOT_ENOUGH_FEE:
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate("Not enough %fee% balance!").
                                replace("%fee%", AssetCls.FEE_NAME),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                break;

            case Transaction.INVALID_NAME_LENGTH_MIN:
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate("Name must be more then %val characters!")
                                .replace("%val", "" + issueTemplate.getItem().getMinNameLen()),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                break;
            case Transaction.INVALID_NAME_LENGTH_MAX:
                JOptionPane.showMessageDialog(MainFrame.getInstance(),
                        Lang.getInstance().translate("Name must be less then %val characters!")
                                .replace("%val", "" + ItemCls.MAX_NAME_LENGTH),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                break;

            case Transaction.INVALID_DESCRIPTION_LENGTH_MAX:
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate("Description must be between 1 and 1000 characters!"),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                break;

            case Transaction.CREATOR_NOT_PERSONALIZED:
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate("Issuer account not personalized!"),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                break;
            default:
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate("Unknown error") + "[" + result + "]!",
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                break;
        }

        // ENABLE
        issueJButton.setEnabled(true);
    }

    protected void initComponents() {
    }

}