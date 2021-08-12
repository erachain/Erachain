package org.erachain.gui.items.unions;

import org.erachain.controller.Controller;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.items.utils.GUIConstants;
import org.erachain.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.sql.Timestamp;
import java.text.ParseException;

//import java.math.BigDecimal;
//import org.erachain.settings.Settings;

@SuppressWarnings("serial")
public class IssueUnionPanel extends IssueItemPanel {

    public static String NAME = "IssueUnionPanel";
    public static String TITLE = "Issue Union";

    private static Logger logger = LoggerFactory.getLogger(IssueUnionPanel.class);

    private JTextField txtBirthday = new JTextField();
    private JTextField txtParent = new JTextField();
    //		super(Controller.getInstance().getApplicationName(false) + " - " + Lang.T("Issue Union"));

    // Variables declaration - do not modify
    private JLabel birthdayJLabel = new JLabel();
    private JLabel parentJLabel = new JLabel();

    public IssueUnionPanel() {
        super(NAME, TITLE, null, null, true, GUIConstants.WIDTH_IMAGE, GUIConstants.WIDTH_IMAGE, true, true);

        initComponents();

        this.setMinimumSize(new Dimension(0, 0));
        this.setVisible(true);
    }

    long birthday;
    long parent;

    protected boolean checkValues() {
        int parseStep = 0;
        try {

            // READ BIRTHDAY
            String bd = txtBirthday.getText();
            if (bd.length() < 11) {
                bd = bd + " 12:12:12";
            }

            Timestamp ts = Timestamp.valueOf(bd);
            birthday = ts.getTime();

            //READ PARENT
            parseStep++;
            parent = Integer.parseInt(this.txtParent.getText());

        } catch (Exception e) {
            String mess = "Invalid pars... " + parseStep;
            switch (parseStep) {
                case 0:
                    mess = "Invalid birthday [YYYY-MM-DD]";
                    break;
                case 1:
                    mess = "Invalid parent";
                    break;
            }
            return false;
        }
        return true;
    }

    @Override
    protected void makeTransaction() {

        transaction = Controller.getInstance().issueUnion(
                itemAppData, creator, exLink, this.nameField.getText(), birthday, parent, textAreaDescription.getText(),
                addIconLabel.getMediaBytes(), addImageLabel.getMediaBytes(),
                feePow);
    }

    protected String makeBodyView() {

        String out = super.makeBodyView();

        out += Lang.T("Date") + ":&nbsp;" + ((UnionCls) item).getBirthday() + "<br>";
        out += Lang.T("Parent") + ":&nbsp;" + ((UnionCls) item).getParent() + "<br>";

        return out;
    }

    protected void initComponents() {

        super.initComponents();

        int gridyParent = initTopArea(true);
        int gridy = 1;

        int gridwidth = fieldGBC.gridwidth;

        birthdayJLabel.setText(Lang.T("Birthday") + ":");
        labelGBC.gridy = gridy;
        jPanelAdd.add(birthdayJLabel, labelGBC);

        // Маска ввода
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter("####-##-##");
        } catch (ParseException e) {
            logger.error("", e);
        }
        txtBirthday = new JFormattedTextField(formatter);
        txtBirthday.setText("1970-12-08");
        fieldGBC.gridy = gridy++;
        fieldGBC.gridwidth = 1;
        jPanelAdd.add(txtBirthday, fieldGBC);

        parentJLabel.setText(Lang.T("Parent") + ":");
        labelGBC.gridy = gridy;
        jPanelAdd.add(parentJLabel, labelGBC);
        fieldGBC.gridwidth = 2;
        fieldGBC.gridy = gridy++;
        jPanelAdd.add(txtParent, fieldGBC);
        txtParent.setText("0");

        fieldGBC.gridwidth = gridwidth;

        initBottom(gridyParent);

    }
}
