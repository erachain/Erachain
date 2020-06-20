package org.erachain.gui.items.unions;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.transaction.IssueUnionRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.TypeOfImage;
import org.erachain.gui.library.AddImageLabel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.ParseException;

import static org.erachain.gui.items.utils.GUIConstants.*;
import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

//import java.math.BigDecimal;
//import org.erachain.settings.Settings;

@SuppressWarnings("serial")
public class IssueUnionPanel extends JPanel {

    private static String iconFile = Settings.getInstance().getPatnIcons() + "IssueUnionPanel.png";
    private static Logger logger = LoggerFactory.getLogger(IssueUnionPanel.class);

    private JComboBox<Account> cbxFrom;
    private JComboBox<String> txtFeePow = new JComboBox<String>();
    private JTextField txtName = new JTextField();
    private JTextArea txtareaDescription = new JTextArea();
    private JTextField txtBirthday = new JTextField();
    private JTextField txtParent = new JTextField();
    //		super(Controller.getInstance().getApplicationName(false) + " - " + Lang.getInstance().translate("Issue Union"));

    private JButton issueButton = new JButton();
    // Variables declaration - do not modify
    private JLabel titleJLabel = new JLabel();
    private JLabel accountJLabel = new JLabel();
    private JLabel descriptionJLabel = new JLabel();
    private JLabel feeJLabel = new JLabel();
    private AddImageLabel addLogoLabel;
    private AddImageLabel addImageLabel;
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JLabel nameJLabel = new JLabel();
    private JLabel birthdayJLabel = new JLabel();
    private JLabel parentJLabel = new JLabel();

    public IssueUnionPanel() {
        initComponents();


        //BUTTON Register


        this.issueButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onIssueClick();
            }
        });


        this.setMinimumSize(new Dimension(0, 0));
        this.setVisible(true);
    }

    public void onIssueClick() {
        //DISABLE
        issueButton.setEnabled(false);

        //CHECK IF NETWORK OK
        if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            //NETWORK NOT OK
            JOptionPane.showMessageDialog(null,
                    Lang.getInstance().translate("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

            //ENABLE
            issueButton.setEnabled(true);

            return;
        }

        if (checkWalletUnlock(issueButton)) {
            return;
        }

        //READ CREATOR
        Account sender = (Account) this.cbxFrom.getSelectedItem();

        int parse = 0;
        int feePow;
        long birthday;
        long parent;
        try {

            //READ FEE POW
            feePow = Integer.parseInt((String) this.txtFeePow.getSelectedItem());
            // READ BIRTHDAY
            parse++;
            String bd = txtBirthday.getText();
            if (bd.length() < 11) {
                bd = bd + " 12:12:12";
            }
            Timestamp ts = Timestamp.valueOf(bd);
            birthday = ts.getTime();

            //READ PARENT
            parse++;
            parent = Integer.parseInt(this.txtParent.getText());

        } catch (Exception e) {
            String mess = "Invalid pars... " + parse;
            switch (parse) {
                case 0:
                    mess = "Invalid fee power 0..6";
                    break;
                case 1:
                    mess = "Invalid birthday [YYYY-MM-DD]";
                    break;
                case 2:
                    mess = "Invalid parent";
                    break;
            }
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(e + mess), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

            issueButton.setEnabled(true);
            return;
        }

        byte[] image = null;
        PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.getInstance().translate(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        IssueUnionRecord issue_Union = (IssueUnionRecord) Controller.getInstance().issueUnion(
                creator, this.txtName.getText(), birthday, parent, txtareaDescription.getText(),
                addLogoLabel.getImgBytes(), image,
                feePow);
        //Issue_Asset_Confirm_Dialog cont = new Issue_Asset_Confirm_Dialog(issueAssetTransaction);
        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;" + Lang.getInstance().translate("Issue Union") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issue_Union.getCreator() + "<br>";
        text += Lang.getInstance().translate("Name") + ":&nbsp;" + issue_Union.getItem().viewName() + "<br>";
        text += Lang.getInstance().translate("Description") + ":<br>" + Library.to_HTML(issue_Union.getItem().getDescription()) + "<br>";
        text += Lang.getInstance().translate("Date") + ":&nbsp;" + ((UnionCls) issue_Union.getItem()).getBirthday() + "<br>";
        text += Lang.getInstance().translate("Parent") + ":&nbsp;" + ((UnionCls) issue_Union.getItem()).getParent() + "<br>";

        String Status_text = "";
        IssueConfirmDialog issueConfirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, issue_Union,
                text, (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), Status_text, Lang.getInstance().translate("Confirmation Transaction"));
        issueConfirmDialog.setLocationRelativeTo(this);
        issueConfirmDialog.setVisible(true);
        //	JOptionPane.OK_OPTION
        if (!issueConfirmDialog.isConfirm) { //s!= JOptionPane.OK_OPTION)	{
            issueButton.setEnabled(true);
            return;
        }
        //VALIDATE AND PROCESS
        int result = Controller.getInstance().getTransactionCreator().afterCreate(issue_Union, Transaction.FOR_NETWORK);
        //CHECK VALIDATE MESSAGE
        if (result == Transaction.VALIDATE_OK) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Union issue has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
            //		this.dispose();
            clearPanel();
        } else {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }

        //ENABLE
        issueButton.setEnabled(true);
    }

    private void clearPanel() {
        txtName.setText("");
        txtareaDescription.setText("");
        txtBirthday.setText("1970-12-08");
        txtParent.setText("-1");
        txtFeePow.setSelectedItem("0");
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints;

        addImageLabel = new AddImageLabel((Lang.getInstance().translate("Add image")),
                WIDTH_IMAGE, HEIGHT_IMAGE, TypeOfImage.JPEG,
                0, ItemCls.MAX_IMAGE_LENGTH, WIDTH_IMAGE_INITIAL, HEIGHT_IMAGE_INITIAL);

        addLogoLabel = new AddImageLabel(Lang.getInstance().translate("Add Logo"),
                WIDTH_LOGO, HEIGHT_LOGO, TypeOfImage.GIF,
                0, ItemCls.MAX_ICON_LENGTH, WIDTH_LOGO_INITIAL, HEIGHT_LOGO_INITIAL);

        titleJLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleJLabel.setText(Lang.getInstance().translate("Issue Union"));
        titleJLabel.setFont(FONT_TITLE); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(titleJLabel, gridBagConstraints);


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.insets = new Insets(0, 12, 8, 8);
        add(addImageLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        add(addLogoLabel, gridBagConstraints);

        issueButton.setText(Lang.getInstance().translate("Issue"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        add(issueButton, gridBagConstraints);

        accountJLabel.setText(Lang.getInstance().translate("Account") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        add(accountJLabel, gridBagConstraints);

        nameJLabel.setText(Lang.getInstance().translate("Name") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        add(nameJLabel, gridBagConstraints);

        descriptionJLabel.setText(Lang.getInstance().translate("Description") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        add(descriptionJLabel, gridBagConstraints);

        birthdayJLabel.setText(Lang.getInstance().translate("Birthday") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        add(birthdayJLabel, gridBagConstraints);

        feeJLabel.setText(Lang.getInstance().translate("Fee Power") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        add(feeJLabel, gridBagConstraints);


        cbxFrom = new JComboBox<>(new AccountsComboBoxModel(TransactionAmount.ACTION_SEND));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        add(cbxFrom, gridBagConstraints);


        txtName.setText("");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        add(txtName, gridBagConstraints);

        txtareaDescription.setColumns(20);
        txtareaDescription.setRows(5);
        jScrollPane1.setViewportView(txtareaDescription);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(jScrollPane1, gridBagConstraints);

        // Маска ввода
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter("####-##-##");
        } catch (ParseException e) {
            logger.error("", e);
        }
        txtBirthday = new JFormattedTextField(formatter);
        txtBirthday.setText("1970-12-08");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        add(txtBirthday, gridBagConstraints);

        txtFeePow.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8"}));
        txtFeePow.setSelectedIndex(0);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(0, 0, 10, 10);
        add(txtFeePow, gridBagConstraints);

        parentJLabel.setText(Lang.getInstance().translate("Parent") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new Insets(0, 0, 7, 7);
        add(parentJLabel, gridBagConstraints);

        txtParent.setText("0");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        add(txtParent, gridBagConstraints);


    }

    public static Image getIcon() {
        {
            try {
                return Toolkit.getDefaultToolkit().getImage(iconFile);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
