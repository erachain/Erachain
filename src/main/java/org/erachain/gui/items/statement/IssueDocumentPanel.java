package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.*;
import org.erachain.gui.exdata.ExDataPanel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MButton;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Саша
 */
public class IssueDocumentPanel extends IconPanel {

    public static String NAME = "IssueDocumentPanel";
    public static String TITLE = "Issue Document";

    private static final Logger LOGGER = LoggerFactory.getLogger(IssueDocumentPanel.class);

    private IssueDocumentPanel th;
    private ExDataPanel exData_Panel;
    private MButton jButton_Work_Cancel;
    private MButton jButton_Work_OK;
    private MButton jButton_Work_OK1;
    public javax.swing.JComboBox jComboBox_Account_Work;
    public JCheckBox encryptCheckBox;
    private javax.swing.JLabel jLabel_Account_Work;
    private javax.swing.JLabel jLabel_Fee_Work;
    private javax.swing.JPanel jPanel_Work;
    private javax.swing.JComboBox<String> txtFeePow;
    //private static IssueDocumentPanel instance;

    /**
     * Creates new form IssueDocumentPanel
     */
    public IssueDocumentPanel(Account creator, AssetCls actionAsset) {
        super(NAME, TITLE);
        th = this;
        initComponents();
        setChecks();

        encryptCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setChecks();

            }
        });

        txtFeePow.setModel(new DefaultComboBoxModel<>(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8"}));
        txtFeePow.setSelectedIndex(0);
        txtFeePow.setVisible(Gui.SHOW_FEE_POWER);

        jLabel_Account_Work.setText(Lang.getInstance().translate("Select account") + ":");
        jButton_Work_OK.setText(Lang.getInstance().translate("Sign and Send"));
        jButton_Work_OK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSendClick();
            }
        });
        jButton_Work_OK1.setText(Lang.getInstance().translate("Sign and Pack"));

        jComboBox_Account_Work.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exData_Panel.updateRecipients();
                exData_Panel.exPayoutsPanel.updateAction();
            }
        });
        if (creator != null) {
            jComboBox_Account_Work.setSelectedItem(creator);
        }
        if (actionAsset != null) {
            exData_Panel.exPayoutsPanel.jComboBoxPayoutAsset.setSelectedItem(actionAsset);
        }

        jLabel_Fee_Work.setText(Lang.getInstance().translate("Fee Power") + ":");
        this.jButton_Work_Cancel.setVisible(false);
    }

    public IssueDocumentPanel() {
        this(null, null);
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel_Work = new javax.swing.JPanel();
        jLabel_Account_Work = new javax.swing.JLabel(Lang.getInstance().translate("Account") + ": ");
        jComboBox_Account_Work = new JComboBox<Account>(new AccountsComboBoxModel());
        encryptCheckBox = new JCheckBox(Lang.getInstance().translate("Encrypt"));
        encryptCheckBox.setSelected(true);

        jLabel_Fee_Work = new javax.swing.JLabel(Lang.getInstance().translate("FeePow") + ": ");
        jLabel_Fee_Work.setVisible(Gui.SHOW_FEE_POWER);

        txtFeePow = new javax.swing.JComboBox();
        jButton_Work_Cancel = new MButton();
        jButton_Work_OK = new MButton();
        jButton_Work_OK1 = new MButton();

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[]{0, 0, 0};
        layout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
        setLayout(layout);

        exData_Panel = new ExDataPanel(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(16, 8, 0, 8);
        add(exData_Panel, gridBagConstraints);

        jPanel_Work.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jPanel_Work.add(jLabel_Account_Work, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Work.add(jComboBox_Account_Work, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jPanel_Work.add(encryptCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jPanel_Work.add(jLabel_Fee_Work, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        gridBagConstraints.gridwidth = 3;
        jPanel_Work.add(txtFeePow, gridBagConstraints);

        jButton_Work_Cancel.setText("Cancel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 16, 8, 16);
        jPanel_Work.add(jButton_Work_Cancel, gridBagConstraints);

        jButton_Work_OK.setText("OK");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 16, 8, 16);
        jPanel_Work.add(jButton_Work_OK, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(jPanel_Work, gridBagConstraints);
        jPanel_Work.getAccessibleContext().setAccessibleName("Work");
        jPanel_Work.getAccessibleContext().setAccessibleDescription("");
        this.setMinimumSize(new Dimension(0, 0));

    }// </editor-fold>

    public void makeDeal() {

        // CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            // ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (password.equals("")) {
                return;
            }
            if (!Controller.getInstance().unlockWallet(password)) {
                // WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
                        Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                return;
            }
        }


        // READ SENDER
        Account sender = (Account) this.jComboBox_Account_Work.getSelectedItem();
        int feePow = 0;
        byte[] messageBytes;
        long key = 0;
        int parsing = 0;
        Integer result = 0;
        try {

            // READ AMOUNT
            parsing = 1;

            // READ FEE
            parsing = 2;
            feePow = Integer.parseInt((String) this.txtFeePow.getSelectedItem());

        } catch (Exception e) {
            // CHECK WHERE PARSING ERROR HAPPENED
            switch (parsing) {
                case 1:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid amount!"),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case 2:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case 5:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Template not exist!"),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;
            }
            return;
        }

        Account[] recipients = exData_Panel.multipleRecipientsPanel.recipientsTableModel.getRecipients();
        if (recipients != null) {
            for (int i = 0; i < recipients.length; i++) {
                Account recipient = recipients[i];
                if (recipient == null) {
                    JOptionPane.showMessageDialog(new JFrame(), "Recipient[" + (i + 1) + "] is wrong",
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.getInstance().translate(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            messageBytes = exData_Panel.makeExData(creator, encryptCheckBox.isSelected());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(new JFrame(), " ERROR: " + e.getMessage(),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (messageBytes == null) {
            return;
        } else if (messageBytes.length > BlockChain.MAX_REC_DATA_BYTES) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.getInstance().translate("Message size exceeded %1 kB")
                            .replace("%1", "" + (BlockChain.MAX_REC_DATA_BYTES >> 10)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

            return;
        }

        parsing = 5;

        // CREATE TX MESSAGE
        byte version = (byte) 3;
        byte property1 = (byte) 0;
        byte property2 = (byte) 0;

        RSignNote issueDoc = (RSignNote) Controller.getInstance().r_SignNote(version, property1, property2,
                creator, feePow, key, messageBytes
        );

        // Issue_Asset_Confirm_Dialog cont = new
        // Issue_Asset_Confirm_Dialog(issueAssetTransaction);
        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                + Lang.getInstance().translate("Issue Note") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issueDoc.getCreator() + "<br>";
        // text += Lang.getInstance().translate("Name") +":&nbsp;"+
        // issueDoc.getItem().viewName() +"<br>";
        // text += Lang.getInstance().translate("Quantity") +":&nbsp;"+
        // ((AssetCls)issueAssetTransaction.getItem()).getQuantity().toString()+"<br>";
        // text += Lang.getInstance().translate("Movable") +":&nbsp;"+
        // Lang.getInstance().translate(((AssetCls)issueAssetTransaction.getItem()).isMovable()+"")+
        // "<br>";
        // text += Lang.getInstance().translate("Divisible") +":&nbsp;"+
        // Lang.getInstance().translate(((AssetCls)issueAssetTransaction.getItem()).isDivisible()+"")+
        // "<br>";
        // text += Lang.getInstance().translate("Scale") +":&nbsp;"+
        // ((AssetCls)issueAssetTransaction.getItem()).getScale()+ "<br>";
        // text += Lang.getInstance().translate("Description")+":<br>"+
        // Library.to_HTML(issueAssetTransaction.getItem().getDescription())+"<br>";
        String Status_text = "";

        // System.out.print("\n"+ text +"\n");
        // UIManager.put("OptionPane.cancelButtonText", "Отмена");
        // UIManager.put("OptionPane.okButtonText", "Готово");

        // int s = JOptionPane.showConfirmDialog(MainFrame.getInstance(), text,
        // Lang.getInstance().translate("Issue Asset"),
        // JOptionPane.YES_NO_OPTION);

        IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, issueDoc,
                text,
                (int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation transaction issue document"));

        // for calculate ExPays
        issueDoc.setDC(DCSet.getInstance());
        RNoteInfo rNoteInfo = new RNoteInfo(issueDoc);
        rNoteInfo.jPanel2.setVisible(false);
        confirmDialog.jScrollPane1.setViewportView(rNoteInfo);
        confirmDialog.setLocationRelativeTo(th);
        confirmDialog.setVisible(true);

        // JOptionPane.OK_OPTION
        if (confirmDialog.isConfirm > 0) {
            ResultDialog.make(this, issueDoc, confirmDialog.isConfirm == IssueConfirmDialog.TRY_FREE);
        }

        return;
    }

    public void onSendClick() {
        this.jButton_Work_OK.setEnabled(false);
        this.jButton_Work_OK1.setEnabled(false);
        makeDeal();
        this.jButton_Work_OK.setEnabled(true);
        this.jButton_Work_OK1.setEnabled(true);
    }

    public void setChecks() {

        boolean selected = !encryptCheckBox.isSelected();
        exData_Panel.checkBoxMakeHashAndCheckUniqueAttachedFiles.setEnabled(selected);
        exData_Panel.checkBoxMakeHashAndCheckUniqueHashes.setEnabled(selected);
        exData_Panel.checkBoxMakeHashAndCheckUniqueText.setEnabled(selected);
        exData_Panel.fill_Template_Panel.checkBoxMakeHashAndCheckUniqueTemplate.setEnabled(selected);

        exData_Panel.checkBoxMakeHashAndCheckUniqueAttachedFiles.setSelected(selected);
        exData_Panel.checkBoxMakeHashAndCheckUniqueHashes.setSelected(selected);
        exData_Panel.checkBoxMakeHashAndCheckUniqueText.setSelected(selected);
        exData_Panel.fill_Template_Panel.checkBoxMakeHashAndCheckUniqueTemplate.setSelected(selected);

    }
}