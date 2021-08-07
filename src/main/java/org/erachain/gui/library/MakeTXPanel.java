package org.erachain.gui.library;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.Gui;
import org.erachain.gui.IconPanel;
import org.erachain.gui.MainFrame;
import org.erachain.gui.ResultDialog;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static org.erachain.gui.items.utils.GUIConstants.FONT_TITLE;
import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

/**
 * @author Саша
 * insert item issue info
 * use  cells[x,y] = [4,3]....[26,29]
 */
public abstract class MakeTXPanel extends IconPanel {

    protected JLabel titleJLabel = new JLabel();
    protected JLabel accountJLabel = new JLabel(Lang.T("Account") + ":");
    protected JLabel feeJLabel = new JLabel(Lang.T("Fee Power") + ":");
    protected JComboBox<String> textFeePow = new JComboBox<>();
    protected JComboBox<Account> fromJComboBox = new JComboBox<>(new AccountsComboBoxModel());
    protected JButton issueJButton = new JButton(Lang.T("Issue"));
    protected JScrollPane jScrollPane1 = new JScrollPane();

    protected JPanel jPanelMain = new JPanel();
    protected JPanel jPanelAdd = new JPanel();
    protected JPanel jPanelLeft = new JPanel();
    protected GridBagConstraints gridBagConstraints;
    protected GridBagConstraints labelGBC;
    protected GridBagConstraints fieldGBC;
    protected JLabel exLinkTextLabel = new JLabel(Lang.T("Append to") + ":");
    protected JLabel exLinkDescriptionLabel = new JLabel(Lang.T("Parent") + ":");
    protected JTextField exLinkText = new JTextField();
    protected JTextField exLinkDescription = new JTextField();

    protected PrivateKeyAccount creator;
    protected ExLink exLink = null;
    protected int feePow;
    protected Transaction transaction;
    protected String confirmMess;
    protected String issueMess;

    public MakeTXPanel(String name, String title, String issueMess, String confirmMess) {
        super(name, title);

        this.issueMess = issueMess;
        this.confirmMess = confirmMess;

        titleJLabel.setFont(FONT_TITLE);
        titleJLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleJLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        titleJLabel.setText(Lang.T(title));

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
            exLinkDescription.setText(Lang.T("Not Found") + "!");
        } else {
            exLinkDescription.setText(parentTx.toStringFullAndCreatorLang());
        }
    }

    protected void initComponents() {

        setLayout(new BorderLayout());

        jPanelMain.setLayout(new java.awt.GridBagLayout());
        jPanelAdd.setLayout(new java.awt.GridBagLayout());

        jPanelLeft.setLayout(new BoxLayout(jPanelLeft, BoxLayout.Y_AXIS));
        //jPanelLeft.setMinimumSize(new Dimension(200, 400));
        //jPanelLeft.setMaximumSize(new Dimension(600, 500));

        labelGBC = new java.awt.GridBagConstraints();
        labelGBC.gridwidth = 3;
        labelGBC.anchor = java.awt.GridBagConstraints.EAST;
        labelGBC.insets = new java.awt.Insets(0, 0, 5, 0);

        fieldGBC = new java.awt.GridBagConstraints();
        fieldGBC.gridx = 8;
        fieldGBC.gridwidth = 19;
        fieldGBC.fill = java.awt.GridBagConstraints.HORIZONTAL;
        fieldGBC.weightx = 0.4;
        fieldGBC.insets = new java.awt.Insets(0, 5, 5, 8);

        jPanelMain.setLayout(new GridBagLayout());
        jPanelAdd.setLayout(new GridBagLayout());

        jPanelLeft.setLayout(new BoxLayout(jPanelLeft, BoxLayout.Y_AXIS));

        labelGBC = new GridBagConstraints();
        labelGBC.gridwidth = 3;
        labelGBC.anchor = GridBagConstraints.EAST;
        labelGBC.insets = new Insets(0, 0, 5, 0);

        fieldGBC = new GridBagConstraints();
        fieldGBC.gridx = 8;
        fieldGBC.gridwidth = 19;
        fieldGBC.fill = GridBagConstraints.HORIZONTAL;
        fieldGBC.weightx = 0.4;
        fieldGBC.insets = new Insets(0, 5, 5, 8);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 18;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.7;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        jPanelMain.add(jPanelLeft, gridBagConstraints);

        add(jPanelMain);
    }

    protected String[] fillAndReceiveStringArray(int size) {
        String[] modelTextScale = new String[size];
        for (int i = 0; i < modelTextScale.length; i++) {
            modelTextScale[i] = i + "";
        }
        return modelTextScale;
    }

    protected abstract boolean checkValues();

    protected abstract void preMakeTransaction();

    protected abstract void makeTransaction();

    protected String makeBodyView() {
        return "";
    }

    protected String makeHeadView() {

        String out = "<h2>" + transaction.viewFullTypeName() + "</h2>"
                + Lang.T("Creator") + ":&nbsp;<b>" + transaction.getCreator() + "</b><br>"
                + (exLink == null ? "" : Lang.T("Append to") + ":&nbsp;<b>" + exLink.viewRef() + "</b><br>");

        return out;
    }

    private String makeTransactionView() {
        return "<HTML><body>" + makeHeadView() + makeBodyView();
    }

    public void onIssueClick() {

        // DISABLE
        issueJButton.setEnabled(false);
        if (checkWalletUnlock(issueJButton)) {
            issueJButton.setEnabled(true);
            return;
        }

        // READ CREATOR
        Account creatorAccount = (Account) fromJComboBox.getSelectedItem();

        Long linkRef = Transaction.parseDBRef(exLinkText.getText());
        if (linkRef != null) {
            exLink = new ExLinkAppendix(linkRef);
        }

        try {
            //READ FEE POW
            feePow = Integer.parseInt((String) this.textFeePow.getSelectedItem());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid fee Power!"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            issueJButton.setEnabled(true);
            return;
        }

        if (checkValues()) {

            creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(creatorAccount.getAddress());
            if (creator == null) {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.T(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                        Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                issueJButton.setEnabled(true);
                return;
            }

            preMakeTransaction();
            makeTransaction();

            IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                    makeTransactionView(), (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), "",
                    Lang.T(confirmMess));
            confirmDialog.setLocationRelativeTo(this);
            confirmDialog.setVisible(true);

            if (confirmDialog.isConfirm > 0) {
                ResultDialog.make(this, transaction, confirmDialog.isConfirm == IssueConfirmDialog.TRY_FREE, null);
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
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = 27;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        jPanelMain.add(titleJLabel, gridBagConstraints);


        labelGBC.gridy = y;
        jPanelMain.add(accountJLabel, labelGBC);

        fieldGBC.gridy = y++;
        jPanelMain.add(fromJComboBox, fieldGBC);

        labelGBC.gridy = y;
        jPanelMain.add(exLinkTextLabel, labelGBC);

        exLinkText.setToolTipText(Lang.T("IssueItemPanel.exLinkText"));

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = fieldGBC.gridx;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        jPanelMain.add(exLinkText, gridBagConstraints);

        exLinkDescriptionLabel.setText(Lang.T("Parent") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        jPanelMain.add(exLinkDescriptionLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = 12;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.insets = new Insets(0, 5, 5, 8);
        jPanelMain.add(exLinkDescription, gridBagConstraints);

        fieldGBC.gridy = y++;
        jPanelMain.add(jPanelAdd, fieldGBC);


        return y;
    }

    // выводит нижние поля панели
    // принимает номер сроки с которой  продолжать вывод полей на нижнюю панель
    protected void initBottom(int y) {

        labelGBC.gridy = ++y;

        fieldGBC.gridy = y;
        fieldGBC.insets = new Insets(10, 5, 15, 5);
        jPanelMain.add(issueJButton, fieldGBC);

    }

}
