package org.erachain.gui.items.imprints;

import org.erachain.controller.Controller;
import org.erachain.core.item.imprints.Imprint;
import org.erachain.core.transaction.IssueImprintRecord;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;


@SuppressWarnings("serial")
public class IssueImprintPanel extends IssueItemPanel {

    public static String NAME = "IssueImprintPanel";
    public static String TITLE = "Issue Unique Hash";

    private JTextField txtNumber;
    private JTextField txtDate;
    private JTextField txtDebitor;
    private JTextField txtCreditor;
    private JTextField txtAmount;
    private JButton issueButton;
    private JTextArea txtDescription;

    public IssueImprintPanel() {
        super(NAME, TITLE, null, "Imprint issue has been sent!");

        initComponents();

        // вывод верхней панели
        int y = initTopArea();

        //LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        //labelGBC.insets = new Insets(5,5,5,5);
        labelGBC.insets = new java.awt.Insets(5, 15, 5, 5);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.weightx = 0;
        labelGBC.gridx = 4;

        //COMBOBOX GBC
        GridBagConstraints cbxGBC = new GridBagConstraints();
        //cbxGBC.insets = new Insets(5,5,5,5);
        cbxGBC.insets = new java.awt.Insets(5, 3, 5, 15);
        cbxGBC.fill = GridBagConstraints.NONE;
        cbxGBC.anchor = GridBagConstraints.NORTHWEST;
        cbxGBC.weightx = 0;
        cbxGBC.gridx = 8;

        //TEXTFIELD GBC
        GridBagConstraints txtGBC = new GridBagConstraints();
        //txtGBC.insets = new Insets(5,5,5,5);
        txtGBC.insets = new java.awt.Insets(5, 3, 5, 15);
        txtGBC.fill = GridBagConstraints.HORIZONTAL;
        txtGBC.anchor = GridBagConstraints.NORTHWEST;
        txtGBC.weightx = 1;
        txtGBC.gridwidth = 2;
        txtGBC.gridx = 8;

        //BUTTON GBC
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(5, 5, 5, 5);
        buttonGBC.insets = new java.awt.Insets(5, 3, 5, 15);
        buttonGBC.fill = GridBagConstraints.NONE;
        //buttonGBC.anchor = GridBagConstraints.NORTHWEST;
        buttonGBC.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        buttonGBC.gridwidth = 2;

        buttonGBC.gridx = 12;

        int gridy = y;

        //LABEL NUMBER
        labelGBC.gridy = gridy;
        JLabel numberLabel = new JLabel(Lang.getInstance().translate("Number") + " (0..9/-.):");
        jPanelMain.add(numberLabel, labelGBC);

        //TXT NUMBER
        txtGBC.gridy = gridy++;
        this.txtNumber = new JTextField();
        jPanelMain.add(this.txtNumber, txtGBC);

        //LABEL DATE
        labelGBC.gridy = gridy;
        JLabel dateLabel = new JLabel(Lang.getInstance().translate("Date") + " (YY-MM-DD HH:MM):");
        jPanelMain.add(dateLabel, labelGBC);

        //TXT DEBITOR
        txtGBC.gridy = gridy++;
        this.txtDate = new JTextField();
        jPanelMain.add(this.txtDate, txtGBC);

        //LABEL DEBTOR
        labelGBC.gridy = gridy;
        JLabel debitorLabel = new JLabel(Lang.getInstance().translate("Debtor")
                + " (" + Lang.getInstance().translate("IBN-INN") + "):");
        jPanelMain.add(debitorLabel, labelGBC);

        //TXT DEBTOR
        txtGBC.gridy = gridy++;
        this.txtDebitor = new JTextField();
        jPanelMain.add(this.txtDebitor, txtGBC);

        //LABEL CREDITOR
        labelGBC.gridy = gridy;
        JLabel creditorLabel = new JLabel(Lang.getInstance().translate("Creditor")
                + " (" + Lang.getInstance().translate("IBN-INN") + "):");
        jPanelMain.add(creditorLabel, labelGBC);

        //TXT CREDITOR
        txtGBC.gridy = gridy++;
        this.txtCreditor = new JTextField();
        jPanelMain.add(this.txtCreditor, txtGBC);

        //LABEL TOTAL
        labelGBC.gridy = gridy;
        JLabel amountLabel = new JLabel(Lang.getInstance().translate("Total") + " (123.03):");
        jPanelMain.add(amountLabel, labelGBC);

        //TXT TOTAL
        txtGBC.gridy = gridy++;
        this.txtAmount = new JTextField();
        jPanelMain.add(this.txtAmount, txtGBC);

        // вывод подвала
        initBottom(gridy);

        this.setVisible(true);
    }

    String name_total;
    protected boolean checkValues() {

        // NAME TOTAL
        name_total = this.txtNumber.getText().trim() + this.txtDate.getText().trim()
                + this.txtDebitor.getText().trim() + this.txtCreditor.getText().trim() + this.txtAmount.getText().trim();
        // CUT BYTES LEN
        name_total = Imprint.hashNameToBase58(name_total);
        return true;
    }

    protected void makeTransaction() {

        transaction = (IssueImprintRecord) Controller.getInstance().issueImprint1(creator, exLink, name_total,
                textAreaDescription.getText(),
                addLogoIconLabel.getImgBytes(), addImageLabel.getImgBytes(),
                feePow);
    }

    protected String makeTransactionView() {

        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;" + Lang.getInstance().translate("Issue Imprint") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + transaction.getCreator() + "<br>";
        text += Library.to_HTML(transaction.getItem().getDescription()) + "<br>";

        return text;
    }
}
