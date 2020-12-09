package org.erachain.gui.items;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.IssueItemRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.Gui;
import org.erachain.gui.IconPanel;
import org.erachain.gui.MainFrame;
import org.erachain.gui.library.AddImageLabel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static org.erachain.gui.items.utils.GUIConstants.*;
import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

/**
 * @author Саша
 *   insert item issue info
 *        use  cells[x,y] = [4,3]....[26,29]
 *
 */
public abstract class IssueItemPanel extends IconPanel {

    protected JLabel titleJLabel = new JLabel();
    protected JLabel accountJLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
    protected JLabel nameJLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
    protected JLabel descriptionJLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
    protected JLabel feeJLabel = new JLabel(Lang.getInstance().translate("Fee Power") + ":");
    protected JComboBox<String> textFeePow = new JComboBox<>();
    protected JComboBox<Account> fromJComboBox = new JComboBox<>(new AccountsComboBoxModel());
    protected JButton issueJButton = new JButton(Lang.getInstance().translate("Issue"));
    protected JScrollPane jScrollPane1 = new JScrollPane();
    protected JTextField textName = new JTextField("");
    protected JTextArea textAreaDescription = new JTextArea("");
    protected AddImageLabel addImageLabel;
    protected AddImageLabel addLogoIconLabel;
    protected JScrollPane jScrollPane2;
    protected JScrollPane jScrollPane3 = new JScrollPane();
    protected JScrollPane mainJScrollPane = new javax.swing.JScrollPane();
    protected JPanel jPanelMain = new javax.swing.JPanel();
    protected JPanel jPanelLeft = new javax.swing.JPanel();
    protected GridBagConstraints gridBagConstraints;
    protected GridBagConstraints labelGBC;
    protected JLabel exLinkTextLabel = new JLabel(Lang.getInstance().translate("Append to") + ":");
    protected JLabel exLinkDescriptionLabel = new JLabel(Lang.getInstance().translate("Parent") + ":");
    protected JTextField exLinkText = new JTextField();
    protected JTextField exLinkDescription = new JTextField();


    public IssueItemPanel(String name, String title) {
        super(name, title);

        jScrollPane2 = new JScrollPane();
        addImageLabel = new AddImageLabel(
                Lang.getInstance().translate("Add image"), WIDTH_IMAGE, HEIGHT_IMAGE, TypeOfImage.JPEG,
                0, ItemCls.MAX_IMAGE_LENGTH, WIDTH_IMAGE_INITIAL, HEIGHT_IMAGE_INITIAL);
        addImageLabel.setBorder(null);
        addLogoIconLabel = new AddImageLabel(Lang.getInstance().translate("Add Logo"),
                WIDTH_LOGO, HEIGHT_LOGO, TypeOfImage.GIF,
                0, ItemCls.MAX_ICON_LENGTH, WIDTH_LOGO_INITIAL, HEIGHT_LOGO_INITIAL);
        addLogoIconLabel.setBorder(null);
        addImageLabel.setImageHorizontalAlignment(SwingConstants.LEFT);
        addLogoIconLabel.setImageHorizontalAlignment(SwingConstants.LEFT);
        titleJLabel.setFont(FONT_TITLE);
        titleJLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleJLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        titleJLabel.setText(Lang.getInstance().translate(title));
        textAreaDescription.setLineWrap(true);
        textFeePow.setModel(new DefaultComboBoxModel<>(fillAndReceiveStringArray(9)));
        textFeePow.setSelectedItem("0");
        feeJLabel.setVisible(Gui.SHOW_FEE_POWER);
        textFeePow.setVisible(Gui.SHOW_FEE_POWER);
        issueJButton.addActionListener(arg0 -> onIssueClick());

        exLinkDescription.setEditable(false);
        exLinkText.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                viewLinkParent();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                viewLinkParent();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                viewLinkParent();
            }
        });

    }

    private void viewLinkParent() {
        String refStr = exLinkText.getText();
        Transaction parentTx = Controller.getInstance().getTransaction(refStr);
        if (parentTx == null) {
            exLinkDescription.setText(Lang.getInstance().translate("Not Found") + "!");
        } else {
            exLinkDescription.setText(parentTx.toStringShortAsCreator());
        }
    }

    protected void initComponents() {

        setLayout(new java.awt.BorderLayout());

        mainJScrollPane.setBorder(null);

        jPanelMain.setLayout(new java.awt.GridBagLayout());

        jPanelLeft.setLayout(new java.awt.GridBagLayout());

        labelGBC = new java.awt.GridBagConstraints();
        labelGBC.gridwidth = 3;
        labelGBC.anchor = java.awt.GridBagConstraints.EAST;
        labelGBC.insets = new java.awt.Insets(0, 0, 5, 0);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 0);
        jPanelLeft.add(addLogoIconLabel, gridBagConstraints);

        jScrollPane3.setBorder(null);

        jScrollPane3.setViewportView(addImageLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jPanelLeft.add(addImageLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 38;
        //   gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        //    gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        jPanelMain.add(jPanelLeft, gridBagConstraints);


        mainJScrollPane.setViewportView(jPanelMain);

        this.add(mainJScrollPane, java.awt.BorderLayout.CENTER);
    }

    protected String[] fillAndReceiveStringArray(int size) {
        String[] modelTextScale = new String[size];
        for (int i = 0; i < modelTextScale.length; i++) {
            modelTextScale[i] = i + "";
        }
        return modelTextScale;
    }

    protected abstract boolean checkValues();

    protected abstract void makeTransaction();

    protected abstract String makeTransactionView();

    protected PrivateKeyAccount creator;
    protected ExLink exLink = null;
    protected int feePow;
    protected IssueItemRecord transaction;
    protected String confirmMess;
    protected String issueMess;

    public void onIssueClick() {

        // DISABLE
        issueJButton.setEnabled(false);
        if (checkWalletUnlock(issueJButton)) {
            issueJButton.setEnabled(true);
            return;
        }

        // READ CREATOR
        Account creatorAccount = (Account) fromJComboBox.getSelectedItem();

        Long linkRef = null; //Transaction.parseDBRef(exLinkText.getText());
        if (linkRef != null) {
            exLink = new ExLinkAppendix(linkRef);
        }

        try {
            //READ FEE POW
            feePow = Integer.parseInt((String) this.textFeePow.getSelectedItem());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee Power!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            issueJButton.setEnabled(true);
            return;
        }

        if (checkValues()) {

            creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(creatorAccount.getAddress());
            if (creator == null) {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                issueJButton.setEnabled(true);
                return;
            }

            makeTransaction();

            IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                    makeTransactionView(), (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), "",
                    Lang.getInstance().translate(confirmMess));
            confirmDialog.setLocationRelativeTo(this);
            confirmDialog.setVisible(true);

            if (confirmDialog.isConfirm) {

                int result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);

                //CHECK VALIDATE MESSAGE
                if (result == Transaction.VALIDATE_OK) {
                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(issueMess),
                            Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(new JFrame(),
                            Lang.getInstance().translate(OnDealClick.resultMess(result)),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        //ENABLE
        this.issueJButton.setEnabled(true);
    }

    //
    // выводит верхние поля панели
    // возвращает номер сроки с которой можно продолжать вывод инфы на панель
    protected int initTopArea() {
        int y = 0;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = 27;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanelMain.add(titleJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(accountJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 8);
        jPanelMain.add(fromJComboBox, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(exLinkTextLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanelMain.add(exLinkText, gridBagConstraints);

        exLinkDescriptionLabel.setText(Lang.getInstance().translate("Parent") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(exLinkDescriptionLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 8);
        jPanelMain.add(exLinkDescription, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(nameJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 8);
        jPanelMain.add(textName, gridBagConstraints);


        return y;
    }

    // выводит нижние поля панели
    // принимает номер сроки с которой  продолжать вывод полей на нижнюю панель
    protected void initBottom(int y) {

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(descriptionJLabel, gridBagConstraints);

        textAreaDescription.setColumns(20);
        textAreaDescription.setRows(5);
        jScrollPane1.setViewportView(textAreaDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = 19;
        // gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 8);
        jPanelMain.add(jScrollPane1, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(feeJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        jPanelMain.add(textFeePow, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 23;
        //   gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        //   gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 5, 8);
        jPanelMain.add(issueJButton, gridBagConstraints);


    }

}
