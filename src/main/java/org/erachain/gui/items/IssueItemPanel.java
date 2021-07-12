package org.erachain.gui.items;

import com.toedter.calendar.JDateChooser;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.IssueItemRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.Gui;
import org.erachain.gui.IconPanel;
import org.erachain.gui.MainFrame;
import org.erachain.gui.ResultDialog;
import org.erachain.gui.library.AddImageLabel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

import static org.erachain.gui.items.utils.GUIConstants.*;
import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

/**
 * @author Саша
 *   insert item issue info
 *        use  cells[x,y] = [4,3]....[26,29]
 *
 */
public abstract class IssueItemPanel extends IconPanel {

    protected JLabel titleJLabel = new JLabel();
    protected JLabel accountJLabel = new JLabel(Lang.T("Account") + ":");
    protected JLabel nameJLabel = new JLabel(Lang.T("Name") + ":");
    protected JLabel tagsJLabel = new JLabel(Lang.T("Tags") + ":");
    protected JLabel descriptionJLabel = new JLabel(Lang.T("Description") + ":");
    protected JLabel feeJLabel = new JLabel(Lang.T("Fee Power") + ":");
    protected JComboBox<String> textFeePow = new JComboBox<>();
    protected JComboBox<Account> fromJComboBox = new JComboBox<>(new AccountsComboBoxModel());
    protected JButton issueJButton = new JButton(Lang.T("Issue"));
    protected JScrollPane jScrollPane1 = new JScrollPane();
    protected JTextField nameField = new JTextField("");
    protected JTextField tagsField = new JTextField("");
    protected JTextArea textAreaDescription = new JTextArea("");
    protected AddImageLabel addIconLabel;
    protected AddImageLabel addImageLabel;

    protected JCheckBox startCheckBox = new JCheckBox(Lang.T("Start"));
    protected JDateChooser startField;
    protected JCheckBox stopCheckBox = new JCheckBox(Lang.T("Stop"));
    protected JDateChooser stopField;
    protected JScrollPane jScrollPane2;
    protected JPanel jPanelMain = new javax.swing.JPanel();
    protected JPanel jPanelAdd = new javax.swing.JPanel();
    protected JPanel jPanelLeft = new javax.swing.JPanel();
    protected GridBagConstraints gridBagConstraints;
    protected GridBagConstraints labelGBC;
    protected GridBagConstraints fieldGBC;
    protected JLabel exLinkTextLabel = new JLabel(Lang.T("Append to") + ":");
    protected JLabel exLinkDescriptionLabel = new JLabel(Lang.T("Parent") + ":");
    protected JTextField exLinkText = new JTextField();
    protected JTextField exLinkDescription = new JTextField();
    boolean useIcon;

    protected byte[] itemAppData;

    public IssueItemPanel(String name, String title, String issueMess, boolean useIcon, int cropWidth, int cropHeight, boolean originalSize, boolean useExtURL) {
        super(name, title);

        this.useIcon = useIcon;
        this.issueMess = issueMess;
        this.confirmMess = "Confirmation Transaction";

        jScrollPane2 = new JScrollPane();

        addIconLabel = new AddImageLabel(Lang.T("Add Logo"),
                WIDTH_LOGO, HEIGHT_LOGO,
                0, ItemCls.MAX_ICON_LENGTH, WIDTH_LOGO_INITIAL, HEIGHT_LOGO_INITIAL, false, useExtURL,
                Toolkit.getDefaultToolkit().getImage("images/icons/add-media-logo.png"));
        addIconLabel.setBorder(null);
        addIconLabel.setImageHorizontalAlignment(SwingConstants.LEFT);

        addImageLabel = new AddImageLabel(
                Lang.T("Add image"), cropWidth, cropHeight,
                0, ItemCls.MAX_IMAGE_LENGTH, cropWidth >> 1, cropHeight >> 1, originalSize, useExtURL,
                Toolkit.getDefaultToolkit().getImage("images/icons/add-media.png"));
        addImageLabel.setBorder(null);
        addImageLabel.setImageHorizontalAlignment(SwingConstants.LEFT);

        titleJLabel.setFont(FONT_TITLE);
        titleJLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleJLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        titleJLabel.setText(Lang.T(title));

        textAreaDescription.setLineWrap(true);

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

        startCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startField.setEnabled(startCheckBox.isSelected());
            }
        });
        stopCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopField.setEnabled(stopCheckBox.isSelected());
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

        setLayout(new java.awt.BorderLayout());

        jPanelMain.setLayout(new java.awt.GridBagLayout());
        jPanelAdd.setLayout(new java.awt.GridBagLayout());

        jPanelLeft.setLayout(new java.awt.GridBagLayout());

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

        if (useIcon) {
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.insets = new java.awt.Insets(8, 8, 10, 10);
            jPanelLeft.add(addIconLabel, gridBagConstraints);
        }

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NORTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jPanelLeft.add(addImageLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 38;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
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

    protected void makeAppData() {
        itemAppData = ItemCls.makeAppData(0L,
                !addIconLabel.isInternalMedia(), addIconLabel.getMediaType(),
                !addImageLabel.isInternalMedia(), addImageLabel.getMediaType(),
                !startCheckBox.isSelected() ? null : startField.getCalendar().getTimeInMillis(),
                !stopCheckBox.isSelected() ? null : stopField.getCalendar().getTimeInMillis(),
                tagsField.getText());

    }

    protected abstract void makeTransaction();

    protected abstract String makeTransactionView();

    protected String makeHeadView(String nameLabel) {
        ItemCls item = transaction.getItem();
        String im = "";
        if (item.hasIconURL())
            im += "icon: " + ItemCls.viewMediaType(item.getIconType()) + ":" + item.getIconURL();
        if (item.hasImageURL()) {
            im += (im.isEmpty() ? "" : ", ") + "image: " + ItemCls.viewMediaType(item.getImageType()) + ":" + item.getImageURL();
        }

        if (!im.isEmpty())
            im += "<br>";

        String out = Lang.T("Creator") + ":&nbsp;<b>" + transaction.getCreator() + "</b><br>"
                + (exLink == null ? "" : Lang.T("Append to") + ":&nbsp;<b>" + exLink.viewRef() + "</b><br>")
                + "[" + item.getKey() + "]" + Lang.T(nameLabel) + ":&nbsp;" + item.viewName() + "<br>"
                + im;

        if (item.hasStartDate() || item.hasStopDate()) {
            out += Lang.T("Validity period") + ":"
                    + (item.hasStartDate() ? item.viewStartDate() : "-")
                    + " / "
                    + (item.hasStopDate() ? item.viewStopDate() : "-")
                    + "<br>";
        }

        return out;
    }

    protected PrivateKeyAccount creator;
    protected ExLink exLink = null;
    protected int feePow;
    protected IssueItemRecord transaction;
    protected String confirmMess;
    protected String issueMess;

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

            // соберем данные общего класса
            makeAppData();

            makeTransaction();

            IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                    makeTransactionView(), (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), "",
                    Lang.T(confirmMess));
            confirmDialog.setLocationRelativeTo(this);
            confirmDialog.setVisible(true);

            if (confirmDialog.isConfirm > 0) {
                ResultDialog.make(this, transaction, confirmDialog.isConfirm == IssueConfirmDialog.TRY_FREE);
            }
        }

        //ENABLE
        this.issueJButton.setEnabled(true);
    }

    //
    // выводит верхние поля панели
    // возвращает номер сроки с которой можно продолжать вывод инфы на панель
    protected int initTopArea(boolean useStartStop) {
        int y = 0;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = 27;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanelMain.add(titleJLabel, gridBagConstraints);


        labelGBC.gridy = y;
        jPanelMain.add(accountJLabel, labelGBC);

        fieldGBC.gridy = y++;
        jPanelMain.add(fromJComboBox, fieldGBC);

        labelGBC.gridy = y;
        jPanelMain.add(exLinkTextLabel, labelGBC);

        exLinkText.setToolTipText(Lang.T("IssueItemPanel.exLinkText"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = fieldGBC.gridx;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanelMain.add(exLinkText, gridBagConstraints);

        exLinkDescriptionLabel.setText(Lang.T("Parent") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(exLinkDescriptionLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 8);
        jPanelMain.add(exLinkDescription, gridBagConstraints);

        labelGBC.gridy = y;
        jPanelMain.add(nameJLabel, labelGBC);

        fieldGBC.gridy = y++;
        jPanelMain.add(nameField, fieldGBC);

        if (useStartStop) {
            // SET ONE TIME ZONE for Birthday
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            startField = new JDateChooser("yyyy-MM-dd HH:mm 'UTC'", "####-##-## ##:##", '_');
            startField.setCalendar(calendar);
            stopField = new JDateChooser("yyyy-MM-dd HH:mm 'UTC'", "####-##-## ##:##", '_');
            stopField.setCalendar(calendar);

            labelGBC.gridy = y;
            jPanelMain.add(new JLabel(Lang.T("Validity period") + ":"), labelGBC);
            JPanel startStop = new JPanel(new FlowLayout(FlowLayout.LEADING));
            startStop.add(startCheckBox);
            startField.setEnabled(false);
            startStop.add(startField);
            startStop.add(stopCheckBox);
            stopField.setEnabled(false);
            startStop.add(stopField);
            fieldGBC.gridy = y++;
            jPanelMain.add(startStop, fieldGBC);
        }

        labelGBC.gridy = y;
        jPanelMain.add(tagsJLabel, labelGBC);

        fieldGBC.gridy = y++;
        jPanelMain.add(tagsField, fieldGBC);
        tagsField.setToolTipText(Lang.T("Use ',' to separate the Tags"));

        fieldGBC.gridy = y++;
        jPanelMain.add(jPanelAdd, fieldGBC);


        return y;
    }

    // выводит нижние поля панели
    // принимает номер сроки с которой  продолжать вывод полей на нижнюю панель
    protected void initBottom(int y) {

        labelGBC.gridy = ++y;
        jPanelMain.add(descriptionJLabel, labelGBC);

        textAreaDescription.setColumns(20);
        textAreaDescription.setRows(5);
        jScrollPane1.setViewportView(textAreaDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.gridwidth = fieldGBC.gridwidth;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelMain.add(jScrollPane1, gridBagConstraints);

        fieldGBC.gridy = y;
        fieldGBC.insets = new java.awt.Insets(10, 5, 15, 5);
        jPanelMain.add(issueJButton, fieldGBC);

    }

}
