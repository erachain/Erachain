package org.erachain.gui.items.imprints;

import org.erachain.controller.Controller;
import org.erachain.core.item.imprints.Imprint;
import org.erachain.core.transaction.IssueImprintRecord;
import org.erachain.gui.items.IssueItemPanel;
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
        super(NAME, TITLE, "Imprint issue has been sent!");

        initComponents();

        // вывод верхней панели
        int y = initTopArea();

        int gridy = y;

        nameJLabel.setVisible(false);
        textName.setVisible(false);

        //LABEL NUMBER
        labelGBC.gridy = gridy;
        JLabel numberLabel = new JLabel(Lang.getInstance().translate("Number") + " (0..9/-.):");
        jPanelMain.add(numberLabel, labelGBC);

        int gridwidth = fieldGBC.gridwidth;
        fieldGBC.gridwidth = 8;
        //TXT NUMBER
        fieldGBC.gridy = gridy++;
        this.txtNumber = new JTextField();
        jPanelMain.add(this.txtNumber, fieldGBC);

        //LABEL DATE
        labelGBC.gridy = gridy;
        JLabel dateLabel = new JLabel(Lang.getInstance().translate("Date") + " (YY-MM-DD HH:MM):");
        jPanelMain.add(dateLabel, labelGBC);

        //TXT DEBITOR
        fieldGBC.gridy = gridy++;
        this.txtDate = new JTextField();
        jPanelMain.add(this.txtDate, fieldGBC);

        //LABEL DEBTOR
        labelGBC.gridy = gridy;
        JLabel debitorLabel = new JLabel(Lang.getInstance().translate("Debtor")
                + " (" + Lang.getInstance().translate("IBN-INN") + "):");
        jPanelMain.add(debitorLabel, labelGBC);

        //TXT DEBTOR
        fieldGBC.gridy = gridy++;
        this.txtDebitor = new JTextField();
        jPanelMain.add(this.txtDebitor, fieldGBC);

        //LABEL CREDITOR
        labelGBC.gridy = gridy;
        JLabel creditorLabel = new JLabel(Lang.getInstance().translate("Creditor")
                + " (" + Lang.getInstance().translate("IBN-INN") + "):");
        jPanelMain.add(creditorLabel, labelGBC);

        //TXT CREDITOR
        fieldGBC.gridy = gridy++;
        this.txtCreditor = new JTextField();
        jPanelMain.add(this.txtCreditor, fieldGBC);

        //LABEL TOTAL
        labelGBC.gridy = gridy;
        JLabel amountLabel = new JLabel(Lang.getInstance().translate("Total") + " (123.03):");
        jPanelMain.add(amountLabel, labelGBC);

        //TXT TOTAL
        fieldGBC.gridy = gridy++;
        this.txtAmount = new JTextField();
        jPanelMain.add(this.txtAmount, fieldGBC);

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

        transaction = (IssueImprintRecord) Controller.getInstance().issueImprint1(creator, exLink, name_total,
                textAreaDescription.getText(),
                addLogoIconLabel.getImgBytes(), addImageLabel.getImgBytes(),
                feePow);
    }

    protected String makeTransactionView() {

        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;" + Lang.getInstance().translate("Issue Imprint") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + transaction.getCreator() + "<br>"
                + (exLink == null ? "" : Lang.getInstance().translate("Append to") + ":&nbsp;<b>" + exLink.viewRef() + "</b><br>");
        text += Lang.getInstance().translate("Hash") + ":&nbsp;" + name_total + "<br>";
        text += Library.to_HTML(transaction.getItem().getDescription()) + "<br>";

        return text;
    }
}
