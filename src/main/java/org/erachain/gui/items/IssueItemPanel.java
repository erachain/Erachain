package org.erachain.gui.items;

import com.toedter.calendar.JDateChooser;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.ResultDialog;
import org.erachain.gui.library.AddImageLabel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MakeTXPanel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

import static org.erachain.gui.items.utils.GUIConstants.*;
import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

/**
 * @author Саша
 * insert item issue info
 * use  cells[x,y] = [4,3]....[26,29]
 */
public abstract class IssueItemPanel extends MakeTXPanel {

    protected JLabel nameJLabel = new JLabel(Lang.T("Name") + ":");
    protected JLabel tagsJLabel = new JLabel(Lang.T("Tags") + ":");
    protected JLabel descriptionJLabel = new JLabel(Lang.T("Description") + ":");
    protected JTextField nameField = new JTextField("");
    protected JTextField tagsField = new JTextField("");
    protected JTextArea textAreaDescription = new JTextArea("");
    protected AddImageLabel addIconLabel;
    protected AddImageLabel addImageLabel;

    protected JCheckBox startCheckBox = new JCheckBox(Lang.T("Start"));
    protected JDateChooser startField;
    protected JCheckBox stopCheckBox = new JCheckBox(Lang.T("Stop"));
    protected JDateChooser stopField;
    protected JLabel exLinkTextLabel = new JLabel(Lang.T("Append to") + ":");
    protected JLabel exLinkDescriptionLabel = new JLabel(Lang.T("Parent") + ":");
    protected JTextField exLinkText = new JTextField();
    protected JTextField exLinkDescription = new JTextField();
    boolean useIcon;

    protected byte[] itemAppData;

    public IssueItemPanel(String name, String title, String issueMess, boolean useIcon, int cropWidth, int cropHeight, boolean originalSize, boolean useExtURL) {
        super(name, title, issueMess, "Confirmation Transaction");

        this.useIcon = useIcon;

        addIconLabel = new AddImageLabel(Lang.T("Add Logo"),
                WIDTH_LOGO, HEIGHT_LOGO,
                0, ItemCls.MAX_ICON_LENGTH, WIDTH_LOGO_INITIAL, HEIGHT_LOGO_INITIAL, false, useExtURL,
                Toolkit.getDefaultToolkit().getImage("images/icons/add-media-logo.png"));
        //addIconLabel.setBorder(null);
        //addIconLabel.setImageHorizontalAlignment(SwingConstants.LEFT);
        //addIconLabel.setMaximumSize(new Dimension(400, 500));

        addImageLabel = new AddImageLabel(
                Lang.T("Add image"), cropWidth, cropHeight,
                0, ItemCls.MAX_IMAGE_LENGTH, cropWidth >> 1, cropHeight >> 1, originalSize, useExtURL,
                Toolkit.getDefaultToolkit().getImage("images/icons/add-media.png"));
        //addImageLabel.setBorder(null);
        //addImageLabel.setImageHorizontalAlignment(SwingConstants.LEFT);
        //addImageLabel.setMaximumSize(new Dimension(400, 500));

        textAreaDescription.setLineWrap(true);

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

    protected void initComponents() {

        super.initComponents();

        if (useIcon) {
            jPanelLeft.add(addIconLabel);
        }
        jPanelLeft.add(addImageLabel);

    }

    protected void makeAppData() {
        itemAppData = ItemCls.makeAppData(0L,
                !addIconLabel.isInternalMedia(), addIconLabel.getMediaType(),
                !addImageLabel.isInternalMedia(), addImageLabel.getMediaType(),
                !startCheckBox.isSelected() ? null : startField.getCalendar().getTimeInMillis(),
                !stopCheckBox.isSelected() ? null : stopField.getCalendar().getTimeInMillis(),
                tagsField.getText());

    }

    protected void makeTransaction() {
        // соберем данные общего класса
        makeAppData();

    }

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
    protected int initTopArea(boolean useStartStop) {

        int y = super.initTopArea();

        labelGBC.gridy = y;
        jPanelMain.add(nameJLabel, labelGBC);

        fieldGBC.gridy = y++;
        jPanelMain.add(nameField, fieldGBC);

        if (useStartStop) {
            Calendar calendar = Calendar.getInstance();
            //TimeZone tz = TimeZone.getDefault();
            //TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            startField = new JDateChooser(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm z");
            startField.setCalendar(calendar);
            stopField = new JDateChooser(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm z");
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

        labelGBC.gridy = y;
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

        super.initBottom(y);

    }

}
