package org.erachain.gui.items.statuses;

import org.erachain.controller.Controller;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.items.utils.GUIConstants;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;

public class IssueStatusPanel extends IssueItemPanel {

    public static String NAME = "IssueStatusPanel";
    public static String TITLE = "Issue Status";

    private JCheckBox jcheckUnique;


    public IssueStatusPanel() {
        super(NAME, TITLE, "Status issue has been sent", true, GUIConstants.WIDTH_IMAGE, GUIConstants.WIDTH_IMAGE, true, true);

        initComponents();

        // вывод верхней панели
        int gridy = initTopArea(true);

        JLabel singleLabel = new JLabel(Lang.T("Single") + ":");
        labelGBC.gridy = gridy;
        jPanelAdd.add(singleLabel, labelGBC);

        jcheckUnique = new JCheckBox();
        GridBagConstraints gbcJCheckUnique = new GridBagConstraints();
        gbcJCheckUnique.gridx = 8;
        //gbcJCheckUnique.gridy = gridy++;
        gbcJCheckUnique.anchor = GridBagConstraints.NORTHEAST;

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(jcheckUnique, fieldGBC);

        // вывод подвала
        initBottom(gridy);

        setVisible(true);
    }

    boolean unique;

    protected boolean checkValues() {

        unique = jcheckUnique.isSelected();
        return true;
    }

    @Override
    protected void makeTransaction() {

        transaction = Controller.getInstance().issueStatus(itemAppData, creator,
                exLink, nameField.getText(), textAreaDescription.getText(), unique,
                addIconLabel.getMediaBytes(), addImageLabel.getMediaBytes(),
                feePow);

    }

    protected String makeTransactionView() {

        String text = "<HTML><body>";
        text += Lang.T("Confirmation Transaction") + ":&nbsp;"
                + Lang.T("Create Status") + "<br><br><br>"
                + makeHeadView("Name");
        text += Lang.T("Description") + ":<br>"
                + Library.to_HTML(transaction.getItem().getDescription()) + "<br>";
        text += Lang.T("Unique") + ": " + ((StatusCls) transaction.getItem()).isUnique()
                + "<br>";
        return text;

    }

}
