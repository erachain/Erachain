package org.erachain.gui.items.unions;

import org.erachain.controller.Controller;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.transaction.IssueUnionRecord;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.library.Library;
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
    //		super(Controller.getInstance().getApplicationName(false) + " - " + Lang.getInstance().translate("Issue Union"));

    // Variables declaration - do not modify
    private JLabel birthdayJLabel = new JLabel();
    private JLabel parentJLabel = new JLabel();

    public IssueUnionPanel() {
        super(NAME, TITLE, "Union issue has been sent!");

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

    protected void makeTransaction() {

        transaction = (IssueUnionRecord) Controller.getInstance().issueUnion(
                creator, exLink, this.textName.getText(), birthday, parent, textAreaDescription.getText(),
                addLogoIconLabel.getImgBytes(), addImageLabel.getImgBytes(),
                feePow);
    }

    protected String makeTransactionView() {

        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;" + Lang.getInstance().translate("Issue Union") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + transaction.getCreator() + "<br>";
        text += Lang.getInstance().translate("Name") + ":&nbsp;" + transaction.getItem().viewName() + "<br>";
        text += Lang.getInstance().translate("Description") + ":<br>" + Library.to_HTML(transaction.getItem().getDescription()) + "<br>";
        text += Lang.getInstance().translate("Date") + ":&nbsp;" + ((UnionCls) transaction.getItem()).getBirthday() + "<br>";
        text += Lang.getInstance().translate("Parent") + ":&nbsp;" + ((UnionCls) transaction.getItem()).getParent() + "<br>";

        return text;
    }

    protected void initComponents() {

        super.initComponents();

        int gridy = initTopArea();

        birthdayJLabel.setText(Lang.getInstance().translate("Birthday") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = gridy;
        jPanelMain.add(birthdayJLabel, gridBagConstraints);


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
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = gridy++;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        jPanelMain.add(txtBirthday, gridBagConstraints);

        parentJLabel.setText(Lang.getInstance().translate("Parent") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.insets = new Insets(0, 0, 7, 7);
        jPanelMain.add(parentJLabel, gridBagConstraints);

        txtParent.setText("0");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = gridy++;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        jPanelMain.add(txtParent, gridBagConstraints);

        initBottom(gridy);

    }
}
