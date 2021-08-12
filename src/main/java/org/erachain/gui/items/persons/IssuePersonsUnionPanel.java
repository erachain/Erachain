package org.erachain.gui.items.persons;

import com.toedter.calendar.JDateChooser;
import org.erachain.controller.Controller;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.persons.PersonsUnion;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.items.utils.GUIConstants;
import org.erachain.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.TimeZone;

@SuppressWarnings("serial")
public class IssuePersonsUnionPanel extends IssueItemPanel {

    public static String NAME = "IssuePersonsUnionPanel";
    public static String TITLE = "Issue Union";

    private static final Logger logger = LoggerFactory.getLogger(IssuePersonsUnionPanel.class);
    protected JDateChooser txtStartDay;
    protected JDateChooser txtStopDay;
    protected JComboBox<String> comboBoxType = new JComboBox<>();

    protected JLabel jLabelRegistrarAddress = new JLabel(Lang.T("Registrar") + ":");
    private JLabel jLabelBirthLatitudeLongtitude = new JLabel(Lang.T("Coordinates of Birth") + ":");
    private JLabel jLabelBirthday = new JLabel(Lang.T("Birthday") + ":");
    protected JLabel jLabelDead = new JLabel(Lang.T("Deathday") + ":");
    private JLabel jLabelEyeColor = new JLabel(Lang.T("Eye color") + ":");
    private JLabel jLabelGender = new JLabel(Lang.T("Gender") + ":");
    private JLabel jlabelhairColor = new JLabel(Lang.T("Hair color") + ":");
    private JLabel jLabelHeight = new JLabel(Lang.T("Growth") + ":");
    protected JPanel jPanelHead = new JPanel();
    protected JCheckBox aliveCheckBox = new JCheckBox(Lang.T("Alive"), true);

    public IssuePersonsUnionPanel() {
        this(NAME, TITLE);
    }

    public IssuePersonsUnionPanel(String name, String title) {
        super(name, title, null, null,
                false, GUIConstants.WIDTH_IMAGE, GUIConstants.HEIGHT_IMAGE, false, false);
        initComponents(true);
        initLabels();

    }

    public IssuePersonsUnionPanel(String name, String title, String issueMess) {
        super(name, title, "IssuePersonsUnionPanel.titleDescription", issueMess, false,
                GUIConstants.WIDTH_IMAGE, GUIConstants.HEIGHT_IMAGE, false, false);
    }

    protected void initLabels() {
        txtStopDay.setVisible(false);
        jLabelDead.setVisible(false);
        aliveCheckBox.addActionListener(arg0 -> {
            if (aliveCheckBox.isSelected()) {
                txtStopDay.setVisible(false);
                jLabelDead.setVisible(false);
            } else {
                txtStopDay.setVisible(true);
                jLabelDead.setVisible(true);
            }
        });

        String[] items = PersonCls.GENDERS_LIST;
        items = Lang.T(items);
        comboBoxType.setModel(new DefaultComboBoxModel<>(items));
        comboBoxType.setSelectedIndex(2);
        setVisible(true);

    }

    protected int initComponents(boolean andBottom) {
        super.initComponents();

        exLinkTextLabel.setVisible(!andBottom);
        exLinkText.setVisible(!andBottom);
        exLinkDescriptionLabel.setVisible(!andBottom);
        exLinkDescription.setVisible(!andBottom);
        jLabelRegistrarAddress.setVisible(andBottom);

        addImageLabel.setEditable(andBottom);
        addIconLabel.setEditable(andBottom);

        // вывод верхней панели
        int gridy = super.initTopArea(false);

        issueJButton.setText(Lang.T("Create and copy to clipboard"));
        //issueJButton.addActionListener(e -> onIssueClick());

        // SET ONE TIME ZONE for Birthday
        TimeZone tz = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            txtStartDay = new JDateChooser("yyyy-MM-dd HH:mm 'UTC'", "####-##-## ##:##", '_');
            Calendar calendar = Calendar.getInstance(tz);
            calendar.set(1990, Calendar.NOVEMBER, 11, 12, 13, 1);
            txtStartDay.setCalendar(calendar);
            txtStopDay = new JDateChooser("yyyy-MM-dd HH:mm 'UTC'", "####-##-## ##:##", '_');
        } finally {
            TimeZone.setDefault(tz);
        }

        txtStartDay.setFont(UIManager.getFont("TextField.font"));
        txtStopDay.setFont(UIManager.getFont("TextField.font"));

        int gridwidth = fieldGBC.gridwidth;
        fieldGBC.gridwidth = 2;

        // gender
        labelGBC.gridy = gridy;
        jPanelAdd.add(jLabelGender, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(comboBoxType, fieldGBC);

        // born
        labelGBC.gridy = gridy;
        jPanelAdd.add(jLabelBirthday, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(txtStartDay, fieldGBC);

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
        jPanelAdd.add(txtStopDay, fieldGBC);

        //BirthLatitude
        labelGBC.gridy = gridy;
        //jPanelAdd.add(jLabelBirthLatitudeLongtitude, labelGBC);
        fieldGBC.gridy = gridy++;
        //jPanelAdd.add(txtBirthLatitude, fieldGBC);

        //HairСolor
        labelGBC.gridy = gridy;
        //jPanelAdd.add(jlabelhairColor, labelGBC);
        fieldGBC.gridy = gridy++;
        //jPanelAdd.add(txtHairColor, fieldGBC);

        // EyeColor
        labelGBC.gridy = gridy;
        //jPanelAdd.add(jLabelEyeColor, labelGBC);
        fieldGBC.gridy = gridy++;
        //jPanelAdd.add(txtEyeColor, fieldGBC);

        // Height
        labelGBC.gridy = gridy;
        //jPanelAdd.add(jLabelHeight, labelGBC);
        fieldGBC.gridy = gridy++;
        //jPanelAdd.add(txtHeight, fieldGBC);

        // registrar address
        labelGBC.gridy = gridy;
        jPanelAdd.add(jLabelRegistrarAddress, labelGBC);

        JMenuItem jMenuItemCopy = new JMenuItem(Lang.T("Копировать"), KeyEvent.VK_C);
        jMenuItemCopy.setMnemonic(KeyEvent.VK_C);
        jMenuItemCopy.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, InputEvent.CTRL_MASK));

        JMenuItem jMenuItemPaste = new JMenuItem(Lang.T("Вставить"), KeyEvent.VK_V);
        jMenuItemPaste.setMnemonic(KeyEvent.VK_V);
        jMenuItemPaste.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_V, InputEvent.CTRL_MASK));

        fieldGBC.gridwidth = gridwidth;

        if (andBottom) {
            // вывод подвала
            super.initBottom(gridy);
        }

        return gridy;

    }


    protected void reset() {
        nameField.setText("");
        textAreaDescription.setText("");
        addImageLabel.reset();
    }

    byte unionType;
    Long startDay;
    Long endDay;

    protected boolean checkValues() {

        TimeZone tz = TimeZone.getDefault();
        try {

            int parse = 0;
            try {
                // READ GENDER
                parse++;
                unionType = (byte) (comboBoxType.getSelectedIndex());
                parse++;
                // SET TIMEZONE to UTC-0
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                startDay = txtStartDay.getCalendar().getTimeInMillis();
                parse++;
                // END DATE
                try {
                    endDay = txtStopDay.getCalendar().getTimeInMillis();
                } catch (Exception ed1) {
                    endDay = startDay - 1;
                }
                if (aliveCheckBox.isSelected()) {
                    endDay = startDay - 1;
                }

            } catch (Exception e) {
                String mess = "Invalid pars... " + parse;
                switch (parse) {
                    case 1:
                        mess = "Invalid type";
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
        } finally {
            TimeZone.setDefault(tz);
        }
    }


    @Override
    protected void makeTransaction() {

        PersonsUnion union = new PersonsUnion(itemAppData, creator, nameField.getText(),
                addIconLabel.getMediaBytes(), addImageLabel.getMediaBytes(), textAreaDescription.getText(), (byte) 0);

        transaction = Controller.getInstance().issuePerson(creator, exLink, feePow,
                union);
    }

}