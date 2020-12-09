package org.erachain.gui.items.statuses;

import org.erachain.controller.Controller;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.transaction.IssueStatusRecord;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;

public class IssueStatusPanel extends IssueItemPanel {

    public static String NAME = "IssueStatusPanel";
    public static String TITLE = "Issue Status";

    private JCheckBox jcheckUnique;


    public IssueStatusPanel() {
        super(NAME, TITLE, "Status issue has been sent");

        initComponents();

        // вывод верхней панели
        int gridy = initTopArea();

        JLabel singleLabel = new JLabel(Lang.getInstance().translate("Single") + ":");
        GridBagConstraints gbcSingleLabel = new GridBagConstraints();
        gbcSingleLabel.gridx = 4;
        gbcSingleLabel.gridy = gridy;
        gbcSingleLabel.anchor = GridBagConstraints.NORTHEAST;
        jPanelMain.add(singleLabel, gbcSingleLabel);


        jcheckUnique = new JCheckBox();
        GridBagConstraints gbcJCheckUnique = new GridBagConstraints();
        gbcJCheckUnique.gridx = 8;
        gbcJCheckUnique.gridy = gridy++;
        gbcJCheckUnique.anchor = GridBagConstraints.NORTHEAST;
        jPanelMain.add(jcheckUnique, gbcJCheckUnique);

        // вывод подвала
        initBottom(gridy);

        setVisible(true);
    }

    boolean unique;

    protected boolean checkValues() {

        unique = jcheckUnique.isSelected();
        return true;
    }

    protected void makeTransaction() {

        transaction = (IssueStatusRecord) Controller.getInstance().issueStatus(creator,
                exLink, textName.getText(), textAreaDescription.getText(), unique,
                addLogoIconLabel.getImgBytes(), addImageLabel.getImgBytes(),
                feePow);

    }

    protected String makeTransactionView() {

        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                + Lang.getInstance().translate("Create Status") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + transaction.getCreator() + "<br>";
        text += Lang.getInstance().translate("Name") + ":&nbsp;" + transaction.getItem().viewName() + "<br>";
        text += Lang.getInstance().translate("Description") + ":<br>"
                + Library.to_HTML(transaction.getItem().getDescription()) + "<br>";
        text += Lang.getInstance().translate("Unique") + ": " + ((StatusCls) transaction.getItem()).isUnique()
                + "<br>";
        return text;

    }

}
