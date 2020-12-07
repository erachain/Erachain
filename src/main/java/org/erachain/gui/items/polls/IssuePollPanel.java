package org.erachain.gui.items.polls;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.transaction.IssuePollRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.CreateOptionsTableModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

public class IssuePollPanel extends IssueItemPanel {

    public static String NAME = "IssuePollPanel";
    public static String TITLE = "Issue Poll";

    private CreateOptionsTableModel optionsTableModel;
    private final MTable table;

    public IssuePollPanel() {
        super(NAME, TITLE);

        initComponents();


        JLabel optionsLabel = new JLabel(Lang.getInstance().translate("Options") + ":");
        GridBagConstraints gbcOptionsLabel = new GridBagConstraints();
        gbcOptionsLabel.gridx = 1;
        gbcOptionsLabel.gridy = 4;
        gbcOptionsLabel.anchor = GridBagConstraints.NORTHEAST;
        add(optionsLabel, gbcOptionsLabel);

        // TABLE OPTIONS
        GridBagConstraints gbcOptionalTable = new GridBagConstraints();
        gbcOptionalTable.gridx = 2;
        gbcOptionalTable.gridy = 4;
        gbcOptionalTable.weighty = 0.1;
        gbcOptionalTable.gridwidth = 2;
        gbcOptionalTable.fill = GridBagConstraints.BOTH;
        gbcOptionalTable.anchor = GridBagConstraints.CENTER;
        table = new MTable(optionsTableModel);
        JScrollPane scrollPaneOptionalTable = new JScrollPane();
        scrollPaneOptionalTable.setViewportView(table);
        add(scrollPaneOptionalTable, gbcOptionalTable);

        JButton deleteButton = new JButton(Lang.getInstance().translate("Delete"));
        deleteButton.addActionListener(e -> deleteRow());
        GridBagConstraints gbcDeleteButton = new GridBagConstraints();
        gbcDeleteButton.gridx = 2;
        gbcDeleteButton.gridy = 5;
        gbcDeleteButton.fill = GridBagConstraints.HORIZONTAL;
        gbcDeleteButton.gridwidth = 2;
        add(deleteButton, gbcDeleteButton);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Delete"));
        copyAddress.addActionListener(e -> deleteRow());
        menu.add(copyAddress);
        TableMenuPopupUtil.installContextMenu(table, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON
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
            // READ FEE POWER
            feePow = Integer.parseInt((String) textFeePow.getSelectedItem());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee Power!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            // ENABLE
            issueJButton.setEnabled(true);
            return;
        }

        if (optionsTableModel.getRowCount() < 1) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Null Options!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            // ENABLE
            issueJButton.setEnabled(true);
            return;
        }
        if (optionsTableModel.getRowCount() == 1 && optionsTableModel.getValueAt(0, 0).equals("")) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Null Options!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            // ENABLE
            issueJButton.setEnabled(true);
            return;
        }
        byte[] icon = addLogoIconLabel.getImgBytes();
        byte[] image = addImageLabel.getImgBytes();

        // CREATE POLL
        PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.getInstance().translate(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        IssuePollRecord issuePoll = (IssuePollRecord) Controller.getInstance().issuePoll(creator,
                exLink, textName.getText(), textAreaDescription.getText(),
                optionsTableModel.getOptions(), icon, image, feePow);

        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                + Lang.getInstance().translate("Issue Voting") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issuePoll.getCreator() + "<br>";
        text += Lang.getInstance().translate("Name") + ":&nbsp;" + this.textName.getText() + "<br>";
        text += "<br>" + Lang.getInstance().translate("Description") + ":<br>"
                + Library.to_HTML(this.textAreaDescription.getText()) + "<br>";
        text += "<br>" + Lang.getInstance().translate("Options") + ":<br>";

        List<String> options = optionsTableModel.getOptions();

        for (int i = 0; i < options.size(); i++) {
            text += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + options.get(i);

        }
        text += "<br>    ";
        String statusText = "";
        IssueConfirmDialog issueConfirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, issuePoll,
                text, (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), statusText,
                Lang.getInstance().translate("Confirmation Transaction"));
        issueConfirmDialog.setLocationRelativeTo(this);
        issueConfirmDialog.setVisible(true);

        // JOptionPane.OK_OPTION
        if (!issueConfirmDialog.isConfirm) {
            issueJButton.setEnabled(true);
            return;
        }

        // VALIDATE AND PROCESS
        int result = Controller.getInstance().getTransactionCreator().afterCreate(issuePoll, Transaction.FOR_NETWORK);
        if (result == Transaction.VALIDATE_OK) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Poll issue has been sent") + "!",
                    Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);

        } else {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }

        // ENABLE
        issueJButton.setEnabled(true);
    }

    private void deleteRow() {
        if (optionsTableModel.getRowCount() > 1) {
            int selRow = table.getSelectedRow();
            if (selRow != -1) {
                optionsTableModel.removeRow(selRow);
            }
        }
    }
}
