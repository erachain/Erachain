package org.erachain.gui.items.persons;

import com.toedter.calendar.JDateChooser;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.persons.PersonHuman;
import org.erachain.core.transaction.IssuePersonRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.items.utils.GUIConstants;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.RecipientAddress;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.TimeZone;

import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

@SuppressWarnings("serial")
public class IssuePersonPanel extends IssueItemPanel implements RecipientAddress.RecipientAddressInterface {

    public static String NAME = "IssuePersonPanel";
    public static String TITLE = "Issue Person";

    private static final Logger logger = LoggerFactory.getLogger(IssuePersonPanel.class);
    protected JDateChooser txtBirthday;
    protected RecipientAddress registrarAddress;
    protected JLabel registrarAddressDesc = new JLabel();
    protected JDateChooser txtDeathDay;
    protected JComboBox<String> comboBoxGender = new JComboBox<>();
    //protected JTextField textPersonNumber = new JTextField();
    protected JTextField txtBirthLatitude = new JTextField("0.0, 0.0");
    protected JTextField txtBirthLongitudeLatitude = new JTextField("0");
    protected JTextField txtSkinColor = new JTextField();
    protected JTextField txtEyeColor = new JTextField();
    protected JTextField txtHairColor = new JTextField();
    protected JTextField txtHeight = new JTextField("170");

    protected JLabel jLabelRegistrarAddress = new JLabel(Lang.T("Registrar") + ":");
    private JLabel jLabelBirthLatitudeLongtitude = new JLabel(Lang.T("Coordinates of Birth") + ":");
    private JLabel jLabelBirthday = new JLabel(Lang.T("Birthday") + ":");
    protected JLabel jLabelDead = new JLabel(Lang.T("Deathday") + ":");
    private JLabel jLabelEyeColor = new JLabel(Lang.T("Eye color") + ":");
    private JLabel jLabelGender = new JLabel(Lang.T("Gender") + ":");
    private JLabel jlabelhairColor = new JLabel(Lang.T("Hair color") + ":");
    private JLabel jLabelHeight = new JLabel(Lang.T("Growth") + ":");
    //private JLabel jLabelPersonNumber = new JLabel(Lang.T("Person number") + ":");
    protected JPanel jPanelHead = new JPanel();
    protected JCheckBox aliveCheckBox = new JCheckBox(Lang.T("Alive"), true);

    public IssuePersonPanel() {
        this(NAME, TITLE);
    }

    public IssuePersonPanel(String name, String title) {
        super(name, title, "Person issue has been sent!", false, GUIConstants.WIDTH_IMAGE, GUIConstants.HEIGHT_IMAGE);
        initComponents(true);
        initLabels();

    }

    public IssuePersonPanel(String name, String title, String issueMess) {
        super(name, title, issueMess, false, GUIConstants.WIDTH_IMAGE, GUIConstants.HEIGHT_IMAGE);
    }

    protected void initLabels() {
        txtDeathDay.setVisible(false);
        jLabelDead.setVisible(false);
        aliveCheckBox.addActionListener(arg0 -> {
            if (aliveCheckBox.isSelected()) {
                txtDeathDay.setVisible(false);
                jLabelDead.setVisible(false);
            } else {
                txtDeathDay.setVisible(true);
                jLabelDead.setVisible(true);
            }
        });

        String[] items = PersonCls.GENDERS_LIST;
        items = Lang.T(items);
        comboBoxGender.setModel(new DefaultComboBoxModel<>(items));
        comboBoxGender.setSelectedIndex(2);
        setVisible(true);

    }

    protected int initComponents(boolean andBottom) {
        super.initComponents();

        registrarAddress = new RecipientAddress(this);

        exLinkTextLabel.setVisible(!andBottom);
        exLinkText.setVisible(!andBottom);
        exLinkDescriptionLabel.setVisible(!andBottom);
        exLinkDescription.setVisible(!andBottom);
        registrarAddressDesc.setVisible(andBottom);
        registrarAddress.setVisible(andBottom);
        jLabelRegistrarAddress.setVisible(andBottom);

        addImageLabel.setEditable(andBottom);
        addLogoIconLabel.setEditable(andBottom);

        // вывод верхней панели
        int gridy = super.initTopArea();

        issueJButton.setText(Lang.T("Create and copy to clipboard"));
        //issueJButton.addActionListener(e -> onIssueClick());

        // SET ONE TIME ZONE for Birthday
        TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        txtBirthday = new JDateChooser("yyyy-MM-dd HH:mm 'UTC'", "####-##-## ##:##", '_');
        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(1990, Calendar.NOVEMBER, 11, 12, 13, 1);
        txtBirthday.setCalendar(calendar);
        txtDeathDay = new JDateChooser("yyyy-MM-dd HH:mm 'UTC'", "####-##-## ##:##", '_');
        TimeZone.setDefault(tz);

        txtBirthday.setFont(UIManager.getFont("TextField.font"));
        txtDeathDay.setFont(UIManager.getFont("TextField.font"));

        int gridwidth = fieldGBC.gridwidth;
        fieldGBC.gridwidth = 2;

        // gender
        labelGBC.gridy = gridy;
        jPanelAdd.add(jLabelGender, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(comboBoxGender, fieldGBC);

        // born
        labelGBC.gridy = gridy;
        jPanelAdd.add(jLabelBirthday, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(txtBirthday, fieldGBC);

        // dead
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = gridy++;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 10, 0, 0);
        jPanelAdd.add(aliveCheckBox, gridBagConstraints);

        labelGBC.gridy = gridy;
        jPanelAdd.add(jLabelDead, labelGBC);
        fieldGBC.gridy = gridy++;
        jPanelAdd.add(txtDeathDay, fieldGBC);

        //BirthLatitude
        labelGBC.gridy = gridy;
        jPanelAdd.add(jLabelBirthLatitudeLongtitude, labelGBC);
        fieldGBC.gridy = gridy++;
        jPanelAdd.add(txtBirthLatitude, fieldGBC);

        //HairСolor
        labelGBC.gridy = gridy;
        jPanelAdd.add(jlabelhairColor, labelGBC);
        fieldGBC.gridy = gridy++;
        jPanelAdd.add(txtHairColor, fieldGBC);

        // EyeColor
        labelGBC.gridy = gridy;
        jPanelAdd.add(jLabelEyeColor, labelGBC);
        fieldGBC.gridy = gridy++;
        jPanelAdd.add(txtEyeColor, fieldGBC);

        // Height
        labelGBC.gridy = gridy;
        jPanelAdd.add(jLabelHeight, labelGBC);
        fieldGBC.gridy = gridy++;
        jPanelAdd.add(txtHeight, fieldGBC);

        // registrar address
        labelGBC.gridy = gridy;
        jPanelAdd.add(jLabelRegistrarAddress, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(registrarAddress, fieldGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(registrarAddressDesc, fieldGBC);

        /* Added Copy, Paste in GEO (by Samartsev. 18.03.2019) */
        JPopupMenu popup = new JPopupMenu();
        txtBirthLatitude.add(popup);
        txtBirthLatitude.setComponentPopupMenu(popup);

        JMenuItem jMenuItemCopy = new JMenuItem(Lang.T("Копировать"), KeyEvent.VK_C);
        jMenuItemCopy.setMnemonic(KeyEvent.VK_C);
        jMenuItemCopy.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, InputEvent.CTRL_MASK));

        JMenuItem jMenuItemPaste = new JMenuItem(Lang.T("Вставить"), KeyEvent.VK_V);
        jMenuItemPaste.setMnemonic(KeyEvent.VK_V);
        jMenuItemPaste.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_V, InputEvent.CTRL_MASK));

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
                    String dataBase58 = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                    dataBase58 = dataBase58.trim();
                    dataBase58 = dataBase58.replaceAll("\n", "");
                    txtBirthLatitude.setText(dataBase58);
                } catch (Exception exception) {
                    logger.error("Error menu paste", exception);
                }
            }
        });

       /* // set acoount TO
        this.registrarAddress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshReceiverDetails();
            }
        });*/

        fieldGBC.gridwidth = gridwidth;

        if (andBottom) {
            // вывод подвала
            super.initBottom(gridy);
        }

        return gridy;

    }


    protected void reset() {
        textName.setText("");
        textAreaDescription.setText("");
        addImageLabel.reset();
    }

    boolean forIssue = false;
    byte gender;
    long birthday;
    long deathday;
    float birthLatitude;
    float birthLongitude;
    int height;

    protected boolean checkValues() {

        int parse = 0;
        try {
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
                deathday = txtDeathDay.getCalendar().getTimeInMillis();
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
            JOptionPane.showMessageDialog(new JFrame(), Lang.T(mess),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }


    protected void makeTransaction() {
    }

    @Override
    protected String makeTransactionView() {
        return null;
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
            JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid fee Power!"),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
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

            Pair<Transaction, Integer> result = Controller.getInstance().issuePerson(forIssue, creator,
                    exLink, textName.getText(), feePow, birthday, deathday, gender,
                    "", //textPersonNumber.getText(),
                    birthLatitude,
                    birthLongitude, txtSkinColor.getText(), txtEyeColor.getText(), txtHairColor.getText(),
                    height, addLogoIconLabel.getImgBytes(), addImageLabel.getImgBytes(), textAreaDescription.getText(),
                    creator, null);

            transaction = (IssuePersonRecord) result.getA();

            // CHECK VALIDATE MESSAGE
            if (result.getB() == Transaction.VALIDATE_OK) {
                if (!forIssue) {
                    PersonHuman personHuman = (PersonHuman) transaction.getItem();
                    // SIGN
                    personHuman.sign(creator);
                    byte[] issueBytes = personHuman.toBytes(false, false);
                    String base58str = Base58.encode(issueBytes);
                    if (registrar == null) {
                        // copy to clipBoard

                        // This method writes a string to the system clipboard.
                        // otherwise it returns null.
                        StringSelection stringSelection = new StringSelection(base58str);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                        JOptionPane.showMessageDialog(new JFrame(),
                                Lang.T("Person bytecode has been copy to buffer") + "!",
                                Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // send telegram
                        byte[] encryptedBytes = AEScrypto.dataEncrypt(issueBytes, creator.getPrivateKey(), registrar.getPublicKey());

                        Transaction transaction = Controller.getInstance().r_Send(
                                creator, null, feePow, registrar, 0L,
                                null, "Person bytecode", encryptedBytes,
                                new byte[1], new byte[]{1}, 0);

                        Controller.getInstance().broadcastTelegram(transaction, true);
                        JOptionPane.showMessageDialog(new JFrame(),
                                Lang.T("Person bytecode has been send to Registrar") + "!",
                                Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

                    }

                    // ENABLE
                    issueJButton.setEnabled(true);
                    return;
                }
                String statusText = "";
                IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                        " ",
                        (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), statusText,
                        Lang.T("Confirmation transaction issue person"));

                IssuePersonDetailsFrame issuePersonDetailsFrame = new IssuePersonDetailsFrame((IssuePersonRecord) transaction);
                confirmDialog.jScrollPane1.setViewportView(issuePersonDetailsFrame);
                confirmDialog.setLocationRelativeTo(this);
                confirmDialog.setVisible(true);
                if (confirmDialog.isConfirm > 0) {
                    // VALIDATE AND PROCESS
                    Integer afterCreateResult = Controller.getInstance().getTransactionCreator().afterCreate(result.getA(),
                            Transaction.FOR_NETWORK, confirmDialog.isConfirm == IssueConfirmDialog.TRY_FREE, false);
                    if (afterCreateResult != Transaction.VALIDATE_OK) {
                        JOptionPane.showMessageDialog(new JFrame(),
                                Lang.T(OnDealClick.resultMess(afterCreateResult)),
                                Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(new JFrame(),
                                Lang.T("Person issue has been sent!"),
                                Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);
                    }

                }
            } else {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.T(OnDealClick.resultMess(result.getB())
                                + (transaction != null && transaction.getErrorValue() != null ? "\n" + transaction.getErrorValue() : "")),
                        Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            }

        }

        // ENABLE
        issueJButton.setEnabled(true);
    }

    PublicKeyAccount registrar;

    private void refreshReceiverDetails(String registrarStr) {

        //Account
        this.registrarAddressDesc.setText(Lang.T(
                Account.getDetailsForEncrypt(registrarStr, AssetCls.FEE_KEY, true, true)));

        registrar = null;
        if (registrarStr != null && !registrarStr.isEmpty()) {
            if (Crypto.getInstance().isValidAddress(registrarStr)) {
                byte[] pubKey = Controller.getInstance().getPublicKeyByAddress(registrarStr);
                if (pubKey == null) {
                    registrar = null;
                } else {
                    registrar = new PublicKeyAccount(pubKey);
                }
            } else {
                if (PublicKeyAccount.isValidPublicKey(registrarStr)) {
                    registrar = new PublicKeyAccount(registrarStr);
                }
            }
        }

        if (registrar == null) {
            issueJButton.setText(Lang.T("Create and copy to clipboard"));
        } else {
            issueJButton.setText(Lang.T("Create and send to Registrar"));
        }
    }

    // выполняемая процедура при изменении адреса получателя
    @Override
    public void recipientAddressWorker(String e) {
        refreshReceiverDetails(e);
    }
}