package org.erachain.gui.transaction;

import org.erachain.core.transaction.IssueStatusRecord;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

//import org.erachain.core.item.statuses.StatusCls;

@SuppressWarnings("serial")
public class IssueStatusDetailsFrame extends Rec_DetailsFrame {
    public IssueStatusDetailsFrame(IssueStatusRecord statusIssue) {
        super(statusIssue);

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++detailGBC.gridy;
        JTextField name = new JTextField(statusIssue.getItem().viewName());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, detailGBC);

        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        ++detailGBC.gridy;
        JTextArea txtAreaDescription = new JTextArea(statusIssue.getItem().getDescription());
        txtAreaDescription.setRows(4);
        txtAreaDescription.setBorder(name.getBorder());
        txtAreaDescription.setEditable(false);
        MenuPopupUtil.installContextMenu(txtAreaDescription);
        this.add(txtAreaDescription, detailGBC);

        //PACK
        //	this.pack();
        //     this.setResizable(false);
        //     this.setLocationRelativeTo(null);
        this.setVisible(true);

    }
}
