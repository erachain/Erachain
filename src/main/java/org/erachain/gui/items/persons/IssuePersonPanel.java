package org.erachain.gui.items.persons;

import com.toedter.calendar.JDateChooser;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonHuman;
import org.erachain.core.transaction.IssuePersonRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.TypeOfImage;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MButton;
import org.erachain.gui.library.AddImageLabel;
import org.erachain.gui.models.AccountsComboBoxModel;
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
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.util.Calendar;
import java.util.TimeZone;

import static org.erachain.gui.items.utils.GUIConstants.HEIGHT_IMAGE;
import static org.erachain.gui.items.utils.GUIConstants.WIDTH_IMAGE;
import static org.erachain.gui.items.utils.GUIConstants.FONT_TITLE;
import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

@SuppressWarnings("serial")
public class IssuePersonPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(IssuePersonPanel.class.getName());
    protected JLabel titleJLabel = new JLabel();
    protected JComboBox<Account> cbxFrom = new JComboBox<>();
    protected JComboBox<String> txtFeePow = new JComboBox<>();
    protected JTextField txtName = new JTextField("");
    protected JTextArea txtareaDescription = new JTextArea();
    protected JDateChooser txtBirthday;
    protected JDateChooser txtDeathday;
    protected JComboBox<String> comboBoxGender = new JComboBox<>();
    protected JTextField textPersonNumber = new JTextField();
    protected JTextField txtBirthLatitude = new JTextField();
    protected JTextField txtBirthLongitudeLatitude = new JTextField("0");
    protected JTextField txtSkinColor = new JTextField();
    protected JTextField txtEyeColor = new JTextField();
    protected JTextField txtHairColor = new JTextField();
    protected JTextField txtHeight = new JTextField("170");
    protected MButton copyButton;
    private JLabel jLabelFee = new JLabel(Lang.getInstance().translate("Fee Power") + ":");
    private JLabel jLabelAccount = new JLabel(Lang.getInstance().translate("Account") + ":");
    private JLabel jLabelBirthLatitudeLongtitude = new JLabel(Lang.getInstance().translate("Coordinates of Birth") + ":");
    private JLabel jLabelBirthday = new JLabel(Lang.getInstance().translate("Birthday") + ":");
    protected JLabel jLabelDead = new JLabel(Lang.getInstance().translate("Deathday") + ":");
    private JLabel jLabelDescription = new JLabel(Lang.getInstance().translate("Description") + ":");
    private JLabel jLabelEyeColor = new JLabel(Lang.getInstance().translate("Eye color") + ":");
    private JLabel jLabelGender = new JLabel(Lang.getInstance().translate("Gender") + ":");
    private JLabel jlabelhairColor = new JLabel(Lang.getInstance().translate("Hair color") + ":");
    private JLabel jLabelHeight = new JLabel(Lang.getInstance().translate("Growth") + ":");
    private JLabel jLabelName = new JLabel(Lang.getInstance().translate("Full name") + ":");
    private JLabel jLabelPersonNumber = new JLabel(Lang.getInstance().translate("Person number") + ":");
    protected JPanel jPanelHead = new JPanel();
    protected JScrollPane scrollPaneDescription = new JScrollPane();
    protected AddImageLabel addImageLabel;
    protected JCheckBox aliveCheckBox = new JCheckBox(Lang.getInstance().translate("Alive"), true);
    protected JPanel mainPanel = new JPanel();
    private JScrollPane mainScrollPane1 = new JScrollPane();

    public IssuePersonPanel() {
        initComponents();
        initLabels();
        titleJLabel.setFont(FONT_TITLE);
        titleJLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleJLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        titleJLabel.setText(Lang.getInstance().translate("Issue Person"));



        cbxFrom.setModel(new AccountsComboBoxModel());
        txtName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (txtName.getText().getBytes().length < 2) {
                    JOptionPane.showMessageDialog(null,
                            Lang.getInstance().translate("the name must be longer than 2 characters"),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    txtName.requestFocus();
                }
            }

        });

        String[] items = {Lang.getInstance().translate("Male"),
                Lang.getInstance().translate("Female"),
                Lang.getInstance().translate("-")};
        comboBoxGender.setModel(new DefaultComboBoxModel<>(items));
        comboBoxGender.setSelectedIndex(2);
        setVisible(true);
    }

    private void initLabels() {
        txtDeathday.setVisible(false);
        jLabelDead.setVisible(false);
        aliveCheckBox.addActionListener(arg0 -> {
            if (aliveCheckBox.isSelected()) {
                txtDeathday.setVisible(false);
                jLabelDead.setVisible(false);
            } else {
                txtDeathday.setVisible(true);
                jLabelDead.setVisible(true);
            }
        });
    }


    private void initComponents() {
        GridBagConstraints gridBagConstraints;
        setLayout(new BorderLayout());

        txtFeePow.setModel(new DefaultComboBoxModel<>(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8"}));
        txtFeePow.setSelectedIndex(0);

        copyButton = new MButton(Lang.getInstance().translate("Create Person and copy to clipboard"), 2);
        copyButton.addActionListener(e -> onIssueClick(false));

        addImageLabel = new AddImageLabel(Lang.getInstance().translate("Add image"),
                WIDTH_IMAGE, HEIGHT_IMAGE, TypeOfImage.JPEG);
        addImageLabel.setPreferredSize(new Dimension(WIDTH_IMAGE, HEIGHT_IMAGE));

        // SET ONE TIME ZONE for Birthday
        TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        txtBirthday = new JDateChooser("yyyy-MM-dd HH:mm 'UTC'", "####-##-## ##:##", '_');
        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(1990, Calendar.NOVEMBER, 11, 12, 13, 1);
        txtBirthday.setCalendar(calendar);
        txtDeathday = new JDateChooser("yyyy-MM-dd HH:mm 'UTC'", "####-##-## ##:##", '_');
        TimeZone.setDefault(tz);

        mainPanel.setLayout(new GridBagLayout());


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(titleJLabel, gridBagConstraints);


        composePanelHead();

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new Insets(10, 18, 0, 16);
        mainPanel.add(jPanelHead, gridBagConstraints);



        // born
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 18, 0, 0);
        mainPanel.add(jLabelBirthday, gridBagConstraints);


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 18, 0, 0);
        mainPanel.add(jLabelGender, gridBagConstraints);


        //HairСolor
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 18, 0, 0);
        mainPanel.add(jlabelhairColor, gridBagConstraints);

        //BirthLatitude
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 18, 0, 0);
        mainPanel.add(jLabelBirthLatitudeLongtitude, gridBagConstraints);

        //Fee
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 18, 0, 0);
        mainPanel.add(jLabelFee, gridBagConstraints);


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 16);
        mainPanel.add(copyButton, gridBagConstraints);


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        mainPanel.add(txtBirthday, gridBagConstraints);
        txtBirthday.setFont(UIManager.getFont("TextField.font"));

        // gender
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        mainPanel.add(comboBoxGender, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        mainPanel.add(txtHairColor, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        mainPanel.add(txtBirthLatitude, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 1);
        mainPanel.add(txtFeePow, gridBagConstraints);



        // dead
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 10, 0, 0);
        mainPanel.add(aliveCheckBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 10, 0, 0);
        mainPanel.add(jLabelDead, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 10, 0, 0);
        mainPanel.add(jLabelPersonNumber, gridBagConstraints);

        // EyeColor
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 10, 0, 0);
        mainPanel.add(jLabelEyeColor, gridBagConstraints);

        // Height
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 10, 0, 0);
        mainPanel.add(jLabelHeight, gridBagConstraints);





        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 16);
        mainPanel.add(txtDeathday, gridBagConstraints);
        txtDeathday.setFont(UIManager.getFont("TextField.font"));

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 16);
        mainPanel.add(textPersonNumber, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 16);
        mainPanel.add(txtEyeColor, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 16);
        mainPanel.add(txtHeight, gridBagConstraints);





        /* Added Copy, Paste in GEO (by Samartsev. 18.03.2019) */
        JPopupMenu popup = new JPopupMenu();
        txtBirthLatitude.add(popup);
        txtBirthLatitude.setComponentPopupMenu(popup);

        JMenuItem jMenuItemCopy = new JMenuItem(Lang.getInstance().translate("Копировать"), KeyEvent.VK_C);
        jMenuItemCopy.setMnemonic(KeyEvent.VK_C);
        jMenuItemCopy.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, ActionEvent.CTRL_MASK));

        JMenuItem jMenuItemPaste = new JMenuItem(Lang.getInstance().translate("Вставить"), KeyEvent.VK_P);
        jMenuItemPaste.setMnemonic(KeyEvent.VK_P);
        jMenuItemPaste.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_P, ActionEvent.CTRL_MASK));

        popup.add(jMenuItemCopy);
        popup.add(jMenuItemPaste);
        jMenuItemCopy.addActionListener(e -> {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Clipboard clipboard = toolkit.getSystemClipboard();
            StringSelection coordString = new StringSelection(txtBirthLatitude.getText());
            clipboard.setContents(coordString, null);
        });
        jMenuItemPaste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable transferable = clipboard.getContents(this);
                if (transferable == null) {
                    return;
                }
                try {
                    txtBirthLatitude.setText((String) transferable.getTransferData(DataFlavor.stringFlavor));
                } catch (Exception exception) {
                    logger.error("Error menu paste", exception);
                }
            }
        });
        mainScrollPane1.setViewportView(mainPanel);
        add(mainScrollPane1, BorderLayout.CENTER);
    }

    private void composePanelHead() {
        GridBagConstraints gridBagConstraints;
        jPanelHead.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.05;
        jPanelHead.add(addImageLabel, gridBagConstraints);

        txtareaDescription.setColumns(20);
        txtareaDescription.setRows(5);
        scrollPaneDescription.setViewportView(txtareaDescription);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.1;
        jPanelHead.add(scrollPaneDescription, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.1;
        jPanelHead.add(txtName, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 10, 0, 0);
        jPanelHead.add(jLabelName, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(10, 10, 0, 0);
        jPanelHead.add(jLabelAccount, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 10, 0, 0);
        jPanelHead.add(jLabelDescription, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        jPanelHead.add(cbxFrom, gridBagConstraints);
    }

    protected void reset() {
        txtName.setText("");
        txtareaDescription.setText("");
        addImageLabel.reset();
    }

    private void onIssueClick(boolean forIssue) {
        // DISABLE
        copyButton.setEnabled(false);

        // CHECK IF NETWORK OK
        if (false && forIssue && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            // NETWORK NOT OK
            JOptionPane.showMessageDialog(null,
                    Lang.getInstance().translate(
                            "You are unable to send a transaction while synchronizing or while having no connections!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

            // ENABLE
            copyButton.setEnabled(true);
            return;
        }
        if (checkWalletUnlock(copyButton)) {
            return;
        }

        // READ CREATOR
        Account sender = (Account) this.cbxFrom.getSelectedItem();

        int parse = 0;
        int feePow;
        byte gender;
        long birthday;
        long deathday;
        float birthLatitude;
        float birthLongitude;
        int height;
        try {
            // READ FEE POW
            feePow = Integer.parseInt((String) txtFeePow.getSelectedItem());
            // READ GENDER
            parse++;
            gender = (byte) (comboBoxGender.getSelectedIndex());
            parse++;
            // SET TIMEZONE to UTC-0
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            birthday = txtBirthday.getCalendar().getTimeInMillis();
            parse++;
            // END DATE
            try {
                deathday = txtDeathday.getCalendar().getTimeInMillis();
            } catch (Exception ed1) {
                deathday = birthday - 1;
            }
            if (aliveCheckBox.isSelected()) {
                deathday = birthday - 1;
            }
            parse++;
            String[] latitudeLongitude = txtBirthLatitude.getText().split(",");
            birthLatitude = Float.parseFloat(latitudeLongitude[0]);
            parse++;
            birthLongitude = Float.parseFloat(latitudeLongitude[1]);
            parse++;
            height = Integer.parseInt(txtHeight.getText());

        } catch (Exception e) {
            String mess = "Invalid pars... " + parse;
            switch (parse) {
                case 0:
                    mess = "Invalid fee power 0..6";
                    break;
                case 1:
                    mess = "Invalid gender";
                    break;
                case 2:
                    mess = "Invalid birthday [YYYY-MM-DD] or [YYYY-MM-DD hh:mm:ss]";
                    break;
                case 3:
                    mess = "Invalid deathday [YYYY-MM-DD] or [YYYY-MM-DD hh:mm:ss]";
                    break;
                case 4:
                    mess = "Invalid Coordinates of Birth, example: 43.123032, 131.917828";
                    break;
                case 5:
                    mess = "Invalid Coordinates of Birth, example: 43.123032, 131.917828";
                    break;
                case 6:
                    mess = "Invalid growth 10..255";
                    break;
            }
            JOptionPane.showMessageDialog(null, Lang.getInstance().translate(mess),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            copyButton.setEnabled(true);
            return;
        }
        PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
        Pair<Transaction, Integer> result = Controller.getInstance().issuePerson(forIssue, creator,
                txtName.getText(), feePow, birthday, deathday, gender, textPersonNumber.getText(), birthLatitude,
                birthLongitude, txtSkinColor.getText(), txtEyeColor.getText(), txtHairColor.getText(),
                height, null, addImageLabel.getImgBytes(), txtareaDescription.getText(),
                creator, null);

        IssuePersonRecord issuePersonRecord = (IssuePersonRecord) result.getA();

        // CHECK VALIDATE MESSAGE
        if (result.getB() == Transaction.VALIDATE_OK) {
            if (!forIssue) {
                PersonHuman personHuman = (PersonHuman) issuePersonRecord.getItem();
                // SIGN
                personHuman.sign(creator);
                String base58str = Base58.encode(personHuman.toBytes(false, false));
                // This method writes a string to the system clipboard.
                // otherwise it returns null.
                StringSelection stringSelection = new StringSelection(base58str);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate("Person issue has been copy to buffer") + "!",
                        Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                // ENABLE
                copyButton.setEnabled(true);
                return;
            }
            String statusText = "";
            IssueConfirmDialog issueConfirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, issuePersonRecord,
                    " ",
                    (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), statusText,
                    Lang.getInstance().translate("Confirmation transaction issue person"));

            IssuePersonDetailsFrame issuePersonDetailsFrame = new IssuePersonDetailsFrame(issuePersonRecord);
            issueConfirmDialog.jScrollPane1.setViewportView(issuePersonDetailsFrame);
            issueConfirmDialog.setLocationRelativeTo(this);
            issueConfirmDialog.setVisible(true);
            if (issueConfirmDialog.isConfirm) {
                // VALIDATE AND PROCESS
                Integer afterCreateResult = Controller.getInstance().getTransactionCreator().afterCreate(result.getA(), Transaction.FOR_NETWORK);
                if (afterCreateResult != Transaction.VALIDATE_OK) {
                    JOptionPane.showMessageDialog(new JFrame(),
                            Lang.getInstance().translate(OnDealClick.resultMess(afterCreateResult)),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(new JFrame(),
                            Lang.getInstance().translate("Person issue has been sent!"),
                            Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                }

            }
        } else if (result.getB() == Transaction.INVALID_NAME_LENGTH_MIN) {
            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                    Lang.getInstance().translate("Name must be more then %val characters!")
                            .replace("%val", "" + issuePersonRecord.getItem().getMinNameLen()),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        } else if (result.getB() == Transaction.INVALID_NAME_LENGTH_MAX) {
            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                    Lang.getInstance().translate("Name must be less then %val characters!")
                            .replace("%val", "" + ItemCls.MAX_NAME_LENGTH),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.getInstance().translate(OnDealClick.resultMess(result.getB())),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }
        // ENABLE
        copyButton.setEnabled(true);
    }


}



