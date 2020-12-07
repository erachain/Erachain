package org.erachain.gui.items.unions;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.transaction.IssueUnionRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.ParseException;

import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

//import java.math.BigDecimal;
//import org.erachain.settings.Settings;

@SuppressWarnings("serial")
public class IssueUnionPanel extends IssueItemPanel {

    public static String NAME = "IssueUnionPanel";
    public static String TITLE = "Issue Union";

    private static Logger logger = LoggerFactory.getLogger(IssueUnionPanel.class);

    private JTextField txtBirthday = new JTextField();
    private JTextField txtParent = new JTextField();
    //		super(Controller.getInstance().getApplicationName(false) + " - " + Lang.getInstance().translate("Issue Union"));

    // Variables declaration - do not modify
    private JLabel birthdayJLabel = new JLabel();
    private JLabel parentJLabel = new JLabel();

    public IssueUnionPanel() {
        super(NAME, TITLE);

        initComponents();

        //BUTTON Register

        this.issueJButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onIssueClick();
            }
        });


        this.setMinimumSize(new Dimension(0, 0));
        this.setVisible(true);
    }

    public void onIssueClick() {
        //DISABLE
        issueJButton.setEnabled(false);

        //CHECK IF NETWORK OK
        if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            //NETWORK NOT OK
            JOptionPane.showMessageDialog(null,
                    Lang.getInstance().translate("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

            //ENABLE
            issueJButton.setEnabled(true);

            return;
        }

        if (checkWalletUnlock(issueJButton)) {
            return;
        }

        //READ CREATOR
        Account sender = (Account) this.fromJComboBox.getSelectedItem();
        ExLink exLink = null;
        Long linkRef = Transaction.parseDBRef(exLinkText.getText());
        if (linkRef != null) {
            exLink = new ExLinkAppendix(linkRef);
        }

        int parse = 0;
        int feePow;
        long birthday;
        long parent;
        try {

            //READ FEE POW
            feePow = Integer.parseInt((String) this.textFeePow.getSelectedItem());
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

            issueJButton.setEnabled(true);
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
                creator, exLink, this.textName.getText(), birthday, parent, textAreaDescription.getText(),
                addLogoIconLabel.getImgBytes(), image,
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
            issueJButton.setEnabled(true);
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
        issueJButton.setEnabled(true);
    }

    private void clearPanel() {
        textName.setText("");
        textAreaDescription.setText("");
        txtBirthday.setText("1970-12-08");
        txtParent.setText("-1");
        textFeePow.setSelectedItem("0");
    }

    protected void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints;

        birthdayJLabel.setText(Lang.getInstance().translate("Birthday") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        add(birthdayJLabel, gridBagConstraints);


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
}
