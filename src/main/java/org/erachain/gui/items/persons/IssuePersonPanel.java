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
import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

@SuppressWarnings("serial")
public class IssuePersonPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(IssuePersonPanel.class.getName());

    protected JComboBox<Account> cbxFrom = new JComboBox<>();
    protected JComboBox<String> txtFeePow = new JComboBox<>();
    protected JTextField txtName = new JTextField();
    protected JTextArea txtareaDescription = new JTextArea();
    protected JDateChooser txtBirthday;
    protected JDateChooser txtDeathday;
    protected JComboBox txtGender = new JComboBox<>();
    protected JTextField textPersonNumber = new JTextField();
    protected JTextField txtBirthLatitude = new JTextField();
    protected JTextField txtBirthLongitude = new JTextField();
    protected JTextField txtSkinColor = new JTextField();
    protected JTextField txtEyeColor = new JTextField();
    protected JTextField txtHairColor = new JTextField();
    protected JTextField txtHeight = new JTextField();
    protected MButton copyButton;
    private JLabel jLabelFee = new JLabel();
    private JLabel jLabelAccount = new JLabel();
    private JLabel jLabelBirthLatitudeLongtitude = new JLabel();
    private JLabel jLabelBirthday = new JLabel();
    protected JLabel jLabelDead = new JLabel();
    private JLabel jLabelDescription = new JLabel();
    private JLabel jLabelEyeColor = new JLabel();
    private JLabel jLabelGender = new JLabel();
    private JLabel jlabelhairColor = new JLabel();
    private JLabel jLabelHeight = new JLabel();
    private JLabel jLabelName = new JLabel();
    private JLabel jLabelPeronNumber = new JLabel();
    private JLabel jLabelTitle = new JLabel();
    protected JPanel jPanelHead = new JPanel();
    protected JScrollPane scrollPaneDescription = new JScrollPane();
    protected AddImageLabel addImageLabel;
    protected JCheckBox aliveCheckBox = new JCheckBox();
    protected JPanel mainPanel = new JPanel();
    private JScrollPane mainScrollPane1 = new JScrollPane();

    public IssuePersonPanel() {
        initComponents();
        initLabelsText();
        cbxFrom.setModel(new AccountsComboBoxModel());
        txtName.setText("");
        // проверка на код
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
        String[] items = {Lang.getInstance().translate("-"), Lang.getInstance().translate("Male"),
                Lang.getInstance().translate("Female")};
        txtGender.setModel(new DefaultComboBoxModel<>(items));
        txtBirthLongitude.setText("0");
        txtHeight.setText("170");
        txtFeePow.setSelectedItem("0");
        setVisible(true);

    }

    private void initLabelsText() {
        jLabelTitle.setText("Create person");
        jLabelAccount.setText(Lang.getInstance().translate("Account") + ":");
        jLabelName.setText(Lang.getInstance().translate("Full name") + ":");
        jLabelDescription.setText(Lang.getInstance().translate("Description") + ":");
        jLabelGender.setText(Lang.getInstance().translate("Gender") + ":");
        jLabelBirthday.setText(Lang.getInstance().translate("Birthday") + ":");
        jLabelEyeColor.setText(Lang.getInstance().translate("Eye color") + ":");
        jlabelhairColor.setText(Lang.getInstance().translate("Hair color") + ":");
        jLabelHeight.setText(Lang.getInstance().translate("Growth") + ":");
        jLabelFee.setText(Lang.getInstance().translate("Fee Power") + ":");
        jLabelBirthLatitudeLongtitude.setText(Lang.getInstance().translate("Coordinates of Birth") + ":");
        jLabelPeronNumber.setText(Lang.getInstance().translate("Person number") + ":");
        jLabelDead.setText(Lang.getInstance().translate("Deathday") + ":");
        aliveCheckBox.setText(Lang.getInstance().translate("Alive"));
        aliveCheckBox.setSelected(true);
        aliveCheckBox.addActionListener(arg0 -> {
            if (aliveCheckBox.isSelected()) {
                txtDeathday.setVisible(false);
                jLabelDead.setVisible(false);

            } else {
                txtDeathday.setVisible(true);
                jLabelDead.setVisible(true);
            }
        });
        txtDeathday.setVisible(false);
        jLabelDead.setVisible(false);

    }


    private void initComponents() {
        GridBagConstraints gridBagConstraints;
        setLayout(new BorderLayout());

        txtFeePow.setModel(new DefaultComboBoxModel<>(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8"}));
        txtFeePow.setSelectedIndex(0);
        copyButton = new MButton(Lang.getInstance().translate("Create Person and copy to clipboard"), 2);
        addImageLabel = new AddImageLabel(Lang.getInstance().translate("Add image"),
                WIDTH_IMAGE, HEIGHT_IMAGE, TypeOfImage.JPEG);

        copyButton.addActionListener(e -> onIssueClick(false));

        // SET ONE TIME ZONE for Birthday
        TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        txtBirthday = new JDateChooser("yyyy-MM-dd HH:mm 'UTC'", "####-##-## ##:##", '_');
        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(1990, Calendar.NOVEMBER, 11, 12, 13, 1);
        txtBirthday.setCalendar(calendar);
        txtDeathday = new JDateChooser("yyyy-MM-dd HH:mm 'UTC'", "####-##-## ##:##", '_');
        TimeZone.setDefault(tz);

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        gridBagLayout.rowHeights = new int[]{0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0};
        mainPanel.setLayout(gridBagLayout);

        // born
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 18, 0, 0);
        mainPanel.add(jLabelBirthday, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        mainPanel.add(txtBirthday, gridBagConstraints);
        txtBirthday.setFont(UIManager.getFont("TextField.font"));

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
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 16);
        mainPanel.add(txtDeathday, gridBagConstraints);
        txtDeathday.setFont(UIManager.getFont("TextField.font"));
        // gender
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 18, 0, 0);
        mainPanel.add(jLabelGender, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        mainPanel.add(txtGender, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 10, 0, 0);
        mainPanel.add(jLabelPeronNumber, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 16);
        mainPanel.add(textPersonNumber, gridBagConstraints);
        // EyeColor
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 10, 0, 0);
        mainPanel.add(jLabelEyeColor, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 16);
        mainPanel.add(txtEyeColor, gridBagConstraints);
        // HairСolor
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 18, 0, 0);
        mainPanel.add(jlabelhairColor, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        mainPanel.add(txtHairColor, gridBagConstraints);
        // Height
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 10, 0, 0);
        mainPanel.add(jLabelHeight, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 16);
        mainPanel.add(txtHeight, gridBagConstraints);
        // BirthLatitude
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 18, 0, 0);
        mainPanel.add(jLabelBirthLatitudeLongtitude, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        mainPanel.add(txtBirthLatitude, gridBagConstraints);

        // Fee

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 18, 0, 0);
        mainPanel.add(jLabelFee, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 0, 0, 1);
        mainPanel.add(txtFeePow, gridBagConstraints);

        GridBagLayout headLayout = new GridBagLayout();
        headLayout.columnWidths = new int[]{0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        headLayout.rowHeights = new int[]{0, 4, 0, 4, 0};
        jPanelHead.setLayout(headLayout);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.05;
        addImageLabel.setPreferredSize(new Dimension(WIDTH_IMAGE, HEIGHT_IMAGE));
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

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new Insets(10, 18, 0, 16);
        mainPanel.add(jPanelHead, gridBagConstraints);


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new Insets(0, 0, 0, 16);
        mainPanel.add(copyButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(10, 10, 10, 0);
        mainPanel.add(jLabelTitle, gridBagConstraints);

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
        if (checkWalletUnlock(copyButton)){
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
            gender = (byte) (txtGender.getSelectedIndex());
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
        } else if (result.getB() == Transaction.INVALID_NAME_LENGTH) {
            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                    Lang.getInstance().translate("Name must be between %m and %M characters!")
                            .replace("%m", "" + issuePersonRecord.getItem().getMinNameLen())
                            .replace("%M", "" + ItemCls.MAX_NAME_LENGTH),
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



