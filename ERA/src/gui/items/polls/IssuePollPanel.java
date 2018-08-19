package gui.items.polls;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.transaction.IssuePollRecord;
import core.transaction.Transaction;
import gui.MainFrame;
import gui.PasswordPane;
import gui.library.Issue_Confirm_Dialog;
import gui.library.MTable;
import gui.library.My_Add_Image_Panel;
import gui.library.library;
import gui.models.AccountsComboBoxModel;
import gui.models.CreateOptionsTableModel;
import gui.transaction.OnDealClick;
import lang.Lang;
import utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

@SuppressWarnings("serial")
public class IssuePollPanel extends JPanel {
    protected int selRow;
    private JComboBox<Account> cbxFrom;
    private JComboBox<String> txtFee;
    private JTextField txtName;
    private JTextArea txtareaDescription;
    private JButton createButton;
    private CreateOptionsTableModel optionsTableModel;
    private IssuePollPanel th;
    private My_Add_Image_Panel add_Image_Panel;
    private My_Add_Image_Panel add_Logo_Icon_Panel;
    final MTable table;

    public IssuePollPanel() {

        // LAYOUT
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowHeights = new int[]{0, 0, 88, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0};
        this.setLayout(gridBagLayout);
        th = this;

        // COMBOBOX GBC
        GridBagConstraints cbxGBC = new GridBagConstraints();
        cbxGBC.insets = new Insets(5, 5, 5, 5);
        cbxGBC.fill = GridBagConstraints.NONE;
        cbxGBC.anchor = GridBagConstraints.NORTHWEST;
        cbxGBC.weightx = 0;
        cbxGBC.gridx = 1;

        // TEXTFIELD GBC
        GridBagConstraints txtGBC = new GridBagConstraints();
        txtGBC.insets = new Insets(5, 5, 5, 0);
        txtGBC.fill = GridBagConstraints.HORIZONTAL;
        txtGBC.anchor = GridBagConstraints.NORTHWEST;
        txtGBC.weightx = 1;
        txtGBC.gridwidth = 2;
        txtGBC.gridx = 2;

        // BUTTON GBC
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(5, 5, 0, 5);
        buttonGBC.fill = GridBagConstraints.NONE;
        buttonGBC.anchor = GridBagConstraints.NORTHWEST;
        buttonGBC.gridwidth = 2;
        buttonGBC.gridx = 0;
        JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
        GridBagConstraints gbc_fromLabel = new GridBagConstraints();
        gbc_fromLabel.insets = new Insets(0, 0, 5, 5);
        gbc_fromLabel.gridx = 1;
        gbc_fromLabel.gridy = 0;
        this.add(fromLabel, gbc_fromLabel);

        // COMBOBOX FROM
        txtGBC.gridy = 0;
        this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.cbxFrom, txtGBC);

        this.optionsTableModel = new CreateOptionsTableModel(new Object[]{Lang.getInstance().translate("Name")}, 0);

        // LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(5, 5, 5, 5);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.weightx = 0;
        labelGBC.gridx = 1;

        // ICON
        add_Image_Panel = new My_Add_Image_Panel(
                Lang.getInstance().translate("Add Image") + (" (max %1%kB)").replace("%1%", "1024"), 250, 250);

        GridBagConstraints gbc_add_Image_Panel = new GridBagConstraints();
        gbc_add_Image_Panel.anchor = GridBagConstraints.NORTH;
        gbc_add_Image_Panel.gridheight = 3;
        gbc_add_Image_Panel.insets = new Insets(0, 0, 5, 5);
        gbc_add_Image_Panel.gridx = 0;
        gbc_add_Image_Panel.gridy = 1;
        add(add_Image_Panel, gbc_add_Image_Panel);

        // LABEL NAME
        labelGBC.gridy = 1;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
        this.add(nameLabel, labelGBC);

        // LABEL FEE
        labelGBC.gridy = 5;
        labelGBC.weighty = 0;
        this.txtName = new JTextField();
        GridBagConstraints gbc_txtName = new GridBagConstraints();
        gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtName.insets = new Insets(0, 0, 5, 5);
        gbc_txtName.gridx = 2;
        gbc_txtName.gridy = 1;
        this.add(this.txtName, gbc_txtName);
        add_Logo_Icon_Panel = new My_Add_Image_Panel(Lang.getInstance().translate("Add Logo"), 50, 50);

        GridBagConstraints gbc_add_Logo_Icon_Panel = new GridBagConstraints();
        gbc_add_Logo_Icon_Panel.insets = new Insets(0, 0, 5, 0);
        gbc_add_Logo_Icon_Panel.gridx = 3;
        gbc_add_Logo_Icon_Panel.gridy = 1;
        add(add_Logo_Icon_Panel, gbc_add_Logo_Icon_Panel);

        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        GridBagConstraints gbc_descriptionLabel = new GridBagConstraints();
        gbc_descriptionLabel.anchor = GridBagConstraints.NORTH;
        gbc_descriptionLabel.insets = new Insets(0, 0, 5, 5);
        gbc_descriptionLabel.gridx = 1;
        gbc_descriptionLabel.gridy = 2;
        this.add(descriptionLabel, gbc_descriptionLabel);

        // TXTAREA NAME
        // TABLE OPTIONS
        GridBagConstraints txtGBC2 = new GridBagConstraints();
        txtGBC2.gridwidth = 2;
        txtGBC2.gridx = 2;
        txtGBC2.gridy = 2;
        txtGBC2.weighty = 0.3;
        txtGBC2.insets = new Insets(0, 5, 5, 0);
        txtGBC2.fill = GridBagConstraints.BOTH;
        txtGBC2.anchor = GridBagConstraints.NORTHWEST;
        JScrollPane ss = new JScrollPane();
        this.add(ss, txtGBC2);
        this.txtareaDescription = new JTextArea();
        ss.setViewportView(txtareaDescription);

        // BUTTON Register
        buttonGBC.gridy = 6;
        createButton = new JButton(Lang.getInstance().translate("Create"));
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onRegisterClick();
            }
        });

        JLabel optionsLabel = new JLabel(Lang.getInstance().translate("Options") + ":");
        GridBagConstraints gbc_optionsLabel = new GridBagConstraints();
        gbc_optionsLabel.anchor = GridBagConstraints.NORTH;
        gbc_optionsLabel.insets = new Insets(0, 0, 5, 5);
        gbc_optionsLabel.gridx = 1;
        gbc_optionsLabel.gridy = 3;
        this.add(optionsLabel, gbc_optionsLabel);

        // TABLE OPTIONS
        GridBagConstraints txtGBC1 = new GridBagConstraints();
        txtGBC1.gridwidth = 2;
        txtGBC1.insets = new Insets(0, 0, 5, 0);
        txtGBC1.gridx = 2;
        txtGBC1.gridy = 3;
        txtGBC1.weighty = 0.1;
        txtGBC1.fill = GridBagConstraints.BOTH;
        txtGBC1.anchor = GridBagConstraints.NORTHWEST;
        table = new MTable(optionsTableModel);
        
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(table);
        this.add(scroll, txtGBC1);
        JButton deleteButton = new JButton(Lang.getInstance().translate("Delete"));
        deleteButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                delrow();
            }
        });

        GridBagConstraints gbc_deleteButton = new GridBagConstraints();
        gbc_deleteButton.gridwidth = 2;
        gbc_deleteButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_deleteButton.insets = new Insets(0, 0, 5, 5);
        gbc_deleteButton.gridx = 2;
        gbc_deleteButton.gridy = 4;
        this.add(deleteButton, gbc_deleteButton);
        JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee") + ":");
        GridBagConstraints gbc_feeLabel = new GridBagConstraints();
        gbc_feeLabel.insets = new Insets(0, 0, 5, 5);
        gbc_feeLabel.gridx = 1;
        gbc_feeLabel.gridy = 5;
        this.add(feeLabel, gbc_feeLabel);
        txtFee = new JComboBox<String>();
        txtFee.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }));
        txtFee.setSelectedIndex(0);
        GridBagConstraints gbc_txtFee = new GridBagConstraints();
        gbc_txtFee.gridwidth = 2;
        gbc_txtFee.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtFee.insets = new Insets(0, 0, 5, 5);
        gbc_txtFee.gridx = 2;
        gbc_txtFee.gridy = 5;
        this.add(this.txtFee, gbc_txtFee);
        this.add(createButton, buttonGBC);

        JPopupMenu menu = new JPopupMenu();

        menu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuCanceled(PopupMenuEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
                // TODO Auto-generated method stub
                if (optionsTableModel.getRowCount() > 1) {
                    selRow = table.getSelectedRow();

                }

            }
        });

        JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Delete"));
        copyAddress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                delrow();
            }
        });
        menu.add(copyAddress);

     //   table.setComponentPopupMenu(menu);
        TableMenuPopupUtil.installContextMenu(table, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON


        // PACK
        // this.pack();
        // this.setResizable(false);
        // this.setLocationRelativeTo(null);
        this.setMinimumSize(new Dimension(0, 0));
        this.setVisible(true);
    }

    public void onRegisterClick() {
        // DISABLE
        this.createButton.setEnabled(false);

        // CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            // ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (!Controller.getInstance().unlockWallet(password)) {
                // WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
                        Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                // ENABLE
                this.createButton.setEnabled(true);
                return;
            }
        }

        // READ CREATOR
        Account sender = (Account) cbxFrom.getSelectedItem();
        int feePow = 0;

        try {
            // READ FEE POWER
            feePow =  Integer.parseInt((String)this.txtFee.getSelectedItem());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee Power!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            // ENABLE
            this.createButton.setEnabled(true);
            return;
        }

        if (optionsTableModel.getRowCount() < 1) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Null Options!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            // ENABLE
            this.createButton.setEnabled(true);
            return;

        }
        if (optionsTableModel.getRowCount() == 1 && optionsTableModel.getValueAt(0, 0).equals("")) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Null Options!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            // ENABLE
            this.createButton.setEnabled(true);
            return;

        }


        byte[] icon = add_Logo_Icon_Panel.imgButes;
        byte[] image = add_Image_Panel.imgButes;

        // CREATE POLL
        PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
        IssuePollRecord issuePoll = (IssuePollRecord) Controller.getInstance().issuePoll(creator,
                this.txtName.getText(), icon, image, this.txtareaDescription.getText(),
                this.optionsTableModel.getOptions(), feePow);

        // Issue_Asset_Confirm_Dialog(issueAssetTransaction);
        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                + Lang.getInstance().translate("Issue Voting") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issuePoll.getCreator() + "<br>";
        text += Lang.getInstance().translate("Name") + ":&nbsp;" + this.txtName.getText() + "<br>";
        text += "<br>" + Lang.getInstance().translate("Description") + ":<br>"
                + library.to_HTML(this.txtareaDescription.getText()) + "<br>";
        text += "<br>" + Lang.getInstance().translate("Options") + ":<br>";

        List<String> op = this.optionsTableModel.getOptions();

        int i;
        for (i = 0; i < op.size(); i++) {
            text += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + op.get(i);

        }
        text += "<br>    ";

        String Status_text = "";
        Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(MainFrame.getInstance(), true, issuePoll,
                text,
                (int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation Transaction"));
        dd.setLocationRelativeTo(th);
        dd.setVisible(true);

        // JOptionPane.OK_OPTION
        if (!dd.isConfirm) {
            this.createButton.setEnabled(true);
            return;
        }

        // VALIDATE AND PROCESS
        int result = Controller.getInstance().getTransactionCreator().afterCreate(issuePoll, Transaction.FOR_NETWORK);

        if (result == Transaction.VALIDATE_OK) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Poll issue has been sent!"),
                    Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);

        } else {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }

        // ENABLE
        this.createButton.setEnabled(true);
    }
    private void delrow(){
        if (optionsTableModel.getRowCount() > 1) {
            int selRow = table.getSelectedRow();
            if (selRow != -1) {
                ((DefaultTableModel) optionsTableModel).removeRow(selRow);
                
            }
        }
    }
}
