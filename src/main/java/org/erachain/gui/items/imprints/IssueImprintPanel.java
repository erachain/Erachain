package org.erachain.gui.items.imprints;

import org.erachain.controller.Controller;
import org.erachain.core.item.imprints.Imprint;
import org.erachain.core.transaction.IssueImprintRecord;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.items.utils.GUIConstants;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;

import javax.swing.*;


@SuppressWarnings("serial")
public class IssueImprintPanel extends IssueItemPanel {

    public static String NAME = "IssueImprintPanel";
    public static String TITLE = "Issue Unique Hash";

    private JTextField txtNumber;
    private JTextField txtDate;
    private JTextField txtDebitor;
    private JTextField txtCreditor;
    private JTextField txtAmount;

    public IssueImprintPanel() {
        super(NAME, TITLE, "Imprint issue has been sent!", true, GUIConstants.WIDTH_IMAGE, GUIConstants.WIDTH_IMAGE, true, true);

        initComponents();

        // вывод верхней панели
        int y = initTopArea();

        int gridy = y;

        nameJLabel.setVisible(false);
        nameField.setVisible(false);

        //LABEL NUMBER
        labelGBC.gridy = gridy;
        JLabel numberLabel = new JLabel(Lang.T("Number") + " (0..9/-.):");
        jPanelAdd.add(numberLabel, labelGBC);

        int gridwidth = fieldGBC.gridwidth;
        fieldGBC.gridwidth = 8;
        //TXT NUMBER
        fieldGBC.gridy = gridy++;
        this.txtNumber = new JTextField();
        jPanelAdd.add(this.txtNumber, fieldGBC);

        //LABEL DATE
        labelGBC.gridy = gridy;
        JLabel dateLabel = new JLabel(Lang.T("Date") + " (YY-MM-DD HH:MM):");
        jPanelAdd.add(dateLabel, labelGBC);

        //TXT DEBITOR
        fieldGBC.gridy = gridy++;
        this.txtDate = new JTextField();
        jPanelAdd.add(this.txtDate, fieldGBC);

        //LABEL DEBTOR
        labelGBC.gridy = gridy;
        JLabel debitorLabel = new JLabel(Lang.T("Debtor")
                + " (" + Lang.T("IBN-INN") + "):");
        jPanelAdd.add(debitorLabel, labelGBC);

        //TXT DEBTOR
        fieldGBC.gridy = gridy++;
        this.txtDebitor = new JTextField();
        jPanelAdd.add(this.txtDebitor, fieldGBC);

        //LABEL CREDITOR
        labelGBC.gridy = gridy;
        JLabel creditorLabel = new JLabel(Lang.T("Creditor")
                + " (" + Lang.T("IBN-INN") + "):");
        jPanelAdd.add(creditorLabel, labelGBC);

        //TXT CREDITOR
        fieldGBC.gridy = gridy++;
        this.txtCreditor = new JTextField();
        jPanelAdd.add(this.txtCreditor, fieldGBC);

        //LABEL TOTAL
        labelGBC.gridy = gridy;
        JLabel amountLabel = new JLabel(Lang.T("Total") + " (123.03):");
        jPanelAdd.add(amountLabel, labelGBC);

        //TXT TOTAL
        fieldGBC.gridy = gridy++;
        this.txtAmount = new JTextField();
        jPanelAdd.add(this.txtAmount, fieldGBC);

        fieldGBC.gridwidth = gridwidth;

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

        transaction = (IssueImprintRecord) Controller.getInstance().issueImprint1(itemAppData, creator, exLink, name_total,
                textAreaDescription.getText(),
                addIconLabel.getMediaBytes(), addImageLabel.getMediaBytes(),
                feePow);
    }

    protected String makeTransactionView() {

        String text = "<HTML><body>";
        text += Lang.T("Confirmation Transaction") + ":&nbsp;" + Lang.T("Issue Imprint") + "<br><br><br>"
                + makeHeadView("Hash");
        text += Library.to_HTML(transaction.getItem().getDescription()) + "<br>";

        return text;
    }
}
