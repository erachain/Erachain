package org.erachain.gui.items.persons;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.persons.PersonFactory;
import org.erachain.core.item.persons.PersonHuman;
import org.erachain.core.transaction.IssuePersonRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MButton;
import org.erachain.gui.transaction.IssuePersonDetailsFrame;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.TimeZone;

import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

public class InsertPersonPanel extends IssuePersonPanel {
    private static final Logger logger = LoggerFactory.getLogger(InsertPersonPanel.class);
    private static final long serialVersionUID = 1L;

    private JTextField txtSign = new JTextField();
    private JTextField txtPublicKey = new JTextField();
    private JTextField txtGenderTxt = new JTextField();
    private JTextField txtBirthdayTxt = new JTextField();
    private JTextField txtDeathdayTxt = new JTextField();

    private MButton transformButton;
    private JLabel labelSign = new JLabel();
    private JLabel labelPublicKey = new JLabel();
    protected JLabel iconLabel = new JLabel();
    protected MButton pasteButton;

    protected PersonHuman person;

    public InsertPersonPanel() {
        super();
        init();
        copyButton.setVisible(false);
        aliveCheckBox.setSelected(false);
        aliveCheckBox.setVisible(false);
    }

    public String getClipboardContents() {
        String result = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                logger.error("Clipboard error", e);
            }
        }
        return result;
    }

    private void init() {
        titleJLabel.setText(Lang.getInstance().translate("Enter Person"));
        txtBirthLatitude.setText("");
        txtBirthLongitudeLatitude.setText("");
        txtHeight.setText("");
        txtFeePow.setSelectedItem("0");
        txtName.setEditable(false);
        txtareaDescription.setEditable(false);
        txtBirthday.setVisible(false);
        txtDeathday.setVisible(false);
        txtBirthdayTxt.setEditable(false);
        txtDeathdayTxt.setVisible(false);
        txtDeathdayTxt.setEditable(false);

        addImageLabel.setVisible(false);
        comboBoxGender.setVisible(false);
        txtGenderTxt.setEditable(false);
        txtBirthLatitude.setEditable(false);
        txtBirthLongitudeLatitude.setEditable(false);
        txtSkinColor.setEditable(false);
        txtEyeColor.setEditable(false);
        txtHairColor.setEditable(false);
        txtHeight.setEditable(false);

        txtPublicKey.setEditable(false);

        labelSign.setText(Lang.getInstance().translate("Signature") + ":");
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 18, 0, 0);
        mainPanel.add(labelSign, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 1);
        txtSign.setEditable(false);
        mainPanel.add(txtSign, gridBagConstraints);

        labelPublicKey.setText(Lang.getInstance().translate("Public key") + ":");
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 18, 0, 0);
        mainPanel.add(labelPublicKey, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 1);
        mainPanel.add(txtPublicKey, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        mainPanel.add(txtGenderTxt, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        mainPanel.add(txtBirthdayTxt, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 16);
        mainPanel.add(txtDeathdayTxt, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.05;
        jPanelHead.add(iconLabel, gridBagConstraints);

        pasteButton = new MButton(Lang.getInstance().translate("Paste Person from clipboard"), 2);
        pasteButton.addActionListener(arg0 -> {
            person = null;
            reset();
            String base58str = getClipboardContents();
            byte[] dataPerson;
            try {
                dataPerson = Base58.decode(base58str);
                person = (PersonHuman) PersonFactory.getInstance().parse(dataPerson, false);
            } catch (Exception ee) {
                JOptionPane.showMessageDialog(null, ee.getMessage(), Lang.getInstance().translate("Error"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            txtName.setText(person.viewName());
            ImageIcon image = new ImageIcon(person.getImage());
            int x = image.getIconWidth();
            int y = image.getIconHeight();
            int x1 = 250;
            double k = ((double) x / (double) x1);
            y = (int) ((double) y / k);
            if (y != 0) {
                Image Im = image.getImage().getScaledInstance(x1, y, 1);
                iconLabel.setIcon(new ImageIcon(Im));
            }
            // SET ONE TIME ZONE for Birthday
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            txtBirthdayTxt.setText(person.getBirthdayStr());
            txtDeathdayTxt.setText(person.getDeathdayStr());
            txtDeathdayTxt.setVisible(false);
            jLabelDead.setVisible(false);
            if (!person.isAlive(0L)) {
                txtDeathdayTxt.setVisible(true);
                jLabelDead.setVisible(true);
            }
            TimeZone.setDefault(TimeZone.getDefault());

            txtareaDescription.setText(person.getDescription() == null ? "" : ("<html>" + Library.to_HTML(person.getDescription())));

            comboBoxGender.setSelectedIndex(person.getGender());
            txtGenderTxt.setText(comboBoxGender.getSelectedItem().toString());

            txtBirthLatitude.setText("" + person.getBirthLatitude() + ", " + person.getBirthLongitude());
            if (person.getSkinColor() != null) {
                txtSkinColor.setText(person.getSkinColor());
            }
            if (person.getEyeColor() != null) {
                txtEyeColor.setText(person.getEyeColor());
            }
            if (person.getHairColor() != null) {
                txtHairColor.setText(person.getHairColor());
            }
            txtHeight.setText("" + person.getHeight());

            txtSign.setText(
                    person.isSignatureValid(DCSet.getInstance()) ? Base58.encode(person.getOwnerSignature())
                            : Lang.getInstance().translate("Wrong signaryte for data owner"));
            txtPublicKey.setText(Base58.encode(person.getOwner().getPublicKey()));

        });

        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 4;
        gridBagConstraints1.gridy = 19;
        gridBagConstraints1.anchor = GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints1.insets = new Insets(20, 0, 0, 0);
        mainPanel.add(pasteButton, gridBagConstraints1);

        transformButton = new MButton(Lang.getInstance().translate("Check person and insert"), 2);

        transformButton.addActionListener(arg0 -> {
            if (person == null) {
                return;
            }
            if (checkWalletUnlock()) {
                return;
            }


            // READ CREATOR
            Account creatorAccount = (Account) cbxFrom.getSelectedItem();

            int feePow;
            try {
                // READ FEE POW
                feePow = Integer.parseInt((String) txtFeePow.getSelectedItem());
            } catch (Exception e) {
                String mess = "Invalid fee power 0..6";
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(mess),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            PrivateKeyAccount creator = Controller.getInstance()
                    .getPrivateKeyAccountByAddress(creatorAccount.getAddress());

            if (creator == null) {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            Pair<Transaction, Integer> result = Controller.getInstance().issuePersonHuman(creator, feePow, person);

            // CHECK VALIDATE MESSAGE
            if (result.getB() == Transaction.VALIDATE_OK) {
                String statusText = "";

                IssueConfirmDialog dd = new IssueConfirmDialog(MainFrame.getInstance(), true, result.getA(),
                        " ",
                        (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), statusText,
                        Lang.getInstance().translate("Confirmation transaction issue person"));

                IssuePersonDetailsFrame ww = new IssuePersonDetailsFrame((IssuePersonRecord) result.getA());
                dd.jScrollPane1.setViewportView(ww);
                dd.setLocationRelativeTo(this);
                dd.setVisible(true);
                if (dd.isConfirm) {
                    // VALIDATE AND PROCESS
                    Integer result1 = Controller.getInstance().getTransactionCreator().afterCreate(result.getA(),
                            Transaction.FOR_NETWORK);
                    if (result1 != Transaction.VALIDATE_OK) {
                        JOptionPane.showMessageDialog(new JFrame(),
                                Lang.getInstance().translate(OnDealClick.resultMess(result1)),
                                Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(new JFrame(),
                                Lang.getInstance().translate("Person issue has been sent!"),
                                Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                        person = null;
                        eraseFields();

                    }
                }
            } else {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate(OnDealClick.resultMess(result.getB())),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            }
        });

        gridBagConstraints1.gridx = 6;
        gridBagConstraints1.gridy = 19;
        gridBagConstraints1.anchor = GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints1.insets = new Insets(20, 0, 0, 16);
        mainPanel.add(transformButton, gridBagConstraints1);

    }


    private void eraseFields() {
        txtFeePow.setSelectedItem("0");
        txtName.setText("");
        txtareaDescription.setText("");
        txtGenderTxt.setText("");
        txtBirthLatitude.setText("");
        txtBirthLongitudeLatitude.setText("");
        txtSkinColor.setText("");
        txtEyeColor.setText("");
        txtHairColor.setText("");
        txtHeight.setText("");
        addImageLabel.reset();
        txtSign.setText("");
        txtPublicKey.setText("");
        txtBirthdayTxt.setText("");
        txtDeathdayTxt.setText("");
        txtGenderTxt.setText("");
        iconLabel.setIcon(null);

    }

}
