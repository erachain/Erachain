package org.erachain.gui.voting;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.CreatePollTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.voting.Poll;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MTable;
import org.erachain.gui.library.library;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.models.CreateOptionsTableModel;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

//import java.math.BigDecimal;
//import org.erachain.settings.Settings;

@SuppressWarnings("serial")
public class CreatePollFrame extends JFrame {
    private JComboBox<Account> cbxFrom;
    private JTextField txtFee;
    private JTextField txtName;
    private JTextArea txtareaDescription;
    private JButton createButton;
    private CreateOptionsTableModel optionsTableModel;
    private CreatePollFrame th;

    public CreatePollFrame() {
        super(Lang.getInstance().translate("Erachain.org") + " - " + Lang.getInstance().translate("Create Poll"));

        //CLOSE
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        th = this;

        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        //LAYOUT
        this.setLayout(new GridBagLayout());

        //PADDING
        ((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));

        //LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(5, 5, 5, 5);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.weightx = 0;
        labelGBC.gridx = 0;

        //COMBOBOX GBC
        GridBagConstraints cbxGBC = new GridBagConstraints();
        cbxGBC.insets = new Insets(5, 5, 5, 5);
        cbxGBC.fill = GridBagConstraints.NONE;
        cbxGBC.anchor = GridBagConstraints.NORTHWEST;
        cbxGBC.weightx = 0;
        cbxGBC.gridx = 1;

        //TEXTFIELD GBC
        GridBagConstraints txtGBC = new GridBagConstraints();
        txtGBC.insets = new Insets(5, 5, 5, 5);
        txtGBC.fill = GridBagConstraints.HORIZONTAL;
        txtGBC.anchor = GridBagConstraints.NORTHWEST;
        txtGBC.weightx = 1;
        txtGBC.gridwidth = 2;
        txtGBC.gridx = 1;

        //BUTTON GBC
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(5, 5, 5, 5);
        buttonGBC.fill = GridBagConstraints.NONE;
        buttonGBC.anchor = GridBagConstraints.NORTHWEST;
        buttonGBC.gridwidth = 2;
        buttonGBC.gridx = 0;

        //LABEL FROM
        labelGBC.gridy = 0;
        JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
        this.add(fromLabel, labelGBC);

        //COMBOBOX FROM
        txtGBC.gridy = 0;
        this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.cbxFrom, txtGBC);

        //LABEL NAME
        labelGBC.gridy = 1;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
        this.add(nameLabel, labelGBC);

        //TXT NAME
        txtGBC.gridy = 1;
        this.txtName = new JTextField();
        this.add(this.txtName, txtGBC);

        //LABEL NAME
        labelGBC.gridy = 2;
        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //TXTAREA NAME
        txtGBC.gridy = 2;
        this.txtareaDescription = new JTextArea();
        this.txtareaDescription.setRows(4);
        this.txtareaDescription.setBorder(this.txtName.getBorder());
        this.add(this.txtareaDescription, txtGBC);

        //LABEL OPTIONS
        labelGBC.gridy = 3;
        JLabel optionsLabel = new JLabel(Lang.getInstance().translate("Options") + ":");
        this.add(optionsLabel, labelGBC);

        //TABLE OPTIONS
        txtGBC.gridy = 3;
        this.optionsTableModel = new CreateOptionsTableModel(new Object[]{Lang.getInstance().translate("Name")}, 0);
        final JTable table = new MTable(optionsTableModel);

        this.add(new JScrollPane(table), txtGBC);

        //TABLE OPTIONS DELETE
        txtGBC.gridy = 4;
        JButton deleteButton = new JButton(Lang.getInstance().translate("Delete"));
        deleteButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (optionsTableModel.getRowCount() > 1) {
                    int selRow = table.getSelectedRow();
                    if (selRow != -1) {
                        ((DefaultTableModel) optionsTableModel).removeRow(selRow);
                    }
                }
            }
        });

        this.add(deleteButton, txtGBC);

        //LABEL FEE
        labelGBC.gridy = 5;
        JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee") + ":");
        this.add(feeLabel, labelGBC);

        //TXT FEE
        txtGBC.gridy = 5;
        this.txtFee = new JTextField();
        this.txtFee.setText("0");
        this.add(this.txtFee, txtGBC);

        //BUTTON Register
        buttonGBC.gridy = 6;
        createButton = new JButton(Lang.getInstance().translate("Create"));
        createButton.setPreferredSize(new Dimension(80, 25));
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onRegisterClick();
            }
        });
        this.add(createButton, buttonGBC);

        //PACK
        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void onRegisterClick() {
        //DISABLE
        this.createButton.setEnabled(false);

        //CHECK IF NETWORK OK
        if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            //NETWORK NOT OK
            JOptionPane.showMessageDialog(null, Lang.getInstance().translate("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

            //ENABLE
            this.createButton.setEnabled(true);

            return;
        }

        //CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            //ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (!Controller.getInstance().unlockWallet(password)) {
                //WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                this.createButton.setEnabled(true);

                return;
            }
        }

        //READ CREATOR
        Account sender = (Account) cbxFrom.getSelectedItem();

        try {
            //READ FEE POWER
            int feePow = Integer.parseInt(txtFee.getText());


            //CREATE POLL
            PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
            CreatePollTransaction issue_voiting = (CreatePollTransaction) Controller.getInstance().createPoll_old(creator, this.txtName.getText(), this.txtareaDescription.getText(), this.optionsTableModel.getOptions(), feePow);
            Poll poll = issue_voiting.getPoll();

            //Issue_Asset_Confirm_Dialog cont = new Issue_Asset_Confirm_Dialog(issueAssetTransaction);
            String text = "<HTML><body>";
            text += Lang.getInstance().translate("Confirmation Transaction Issue Asset") + "<br><br><br>";
            text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issue_voiting.getCreator() + "<br>";
            text += Lang.getInstance().translate("Name") + ":&nbsp;" + poll.getName() + "<br>";
            text += "<br>" + Lang.getInstance().translate("Description") + ":<br>" + library.to_HTML(poll.getDescription()) + "<br>";
            text += "<br>" + Lang.getInstance().translate("Options") + ":<br>";

            List<String> op = this.optionsTableModel.getOptions();

            int i;
            for (i = 0; i < op.size(); i++) {
                text += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + op.get(i);

            }
            text += "<br>    ";

            String Status_text = "";

            //	    UIManager.put("OptionPane.cancelButtonText", "Отмена");
            //	    UIManager.put("OptionPane.okButtonText", "Готово");

            //	int s = JOptionPane.showConfirmDialog(MainFrame.getInstance(), text, Lang.getInstance().translate("Issue Asset"),  JOptionPane.YES_NO_OPTION);

            IssueConfirmDialog dd = new IssueConfirmDialog(MainFrame.getInstance(), true, issue_voiting,
                    text, (int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2), Status_text, Lang.getInstance().translate("Confirmation Transaction"));
            dd.setLocationRelativeTo(th);
            dd.setVisible(true);

            //	JOptionPane.OK_OPTION
            if (!dd.isConfirm) { //s!= JOptionPane.OK_OPTION)	{

                this.createButton.setEnabled(true);

                return;
            }


            //VALIDATE AND PROCESS
            int result = Controller.getInstance().getTransactionCreator().afterCreate(issue_voiting, Transaction.FOR_NETWORK);

            //CHECK VALIDATE MESSAGE
            switch (result) {
                case Transaction.VALIDATE_OK:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Poll creation has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                    this.dispose();
                    break;

				/*
			case Transaction.NOT_YET_RELEASED:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Voting will be enabled at ") + DateTimeFormat.timestamptoString(Transaction.getVOTING_RELEASE()) + "!",  "Error", JOptionPane.ERROR_MESSAGE);
				break;
				*/
                case Transaction.NAME_NOT_LOWER_CASE:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Name must be lower case!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    this.txtName.setText(this.txtName.getText().toLowerCase());
                    break;

                case Transaction.NOT_ENOUGH_FEE:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Not enough %fee% balance!").replace("%fee%", AssetCls.FEE_NAME), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case Transaction.NO_BALANCE:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Not enough balance!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case Transaction.INVALID_NAME_LENGTH:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Name must be between 1 and 100 characters!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case Transaction.INVALID_DESCRIPTION_LENGTH:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Description must be between 1 and 1000 characters!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case Transaction.POLL_ALREADY_CREATED:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("A poll with that name already exists!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case Transaction.INVALID_OPTIONS_LENGTH:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("The amount of options must be between 1 and 100!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case Transaction.INVALID_OPTION_LENGTH:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("All options must be between 1 and 100 characters!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case Transaction.DUPLICATE_OPTION:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("All options must be unique!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                default:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Unknown error!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }

        //ENABLE
        this.createButton.setEnabled(true);
    }
}
