package org.erachain.gui.transaction;

import org.erachain.core.transaction.IssueTemplateRecord;
import org.erachain.gui.*;
import org.erachain.gui.library.library;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class IssueTemplateDetailsFrame extends Rec_DetailsFrame {
    public IssueTemplateDetailsFrame(IssueTemplateRecord templateIssue) {
        super(templateIssue);

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++detailGBC.gridy;
        JTextField name = new JTextField(templateIssue.getItem().viewName());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, detailGBC);

        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        ++detailGBC.gridy;
        String txt = "<HTML>" + library.to_HTML(templateIssue.getItem().getDescription());
        JLabel txtAreaDescription = new JLabel(txt);
        //txtAreaDescription.setRows(4);
        txtAreaDescription.setBorder(name.getBorder());
        //txtAreaDescription.setEditable(false);
        //MenuPopupUtil.installContextMenu(txtAreaDescription);
        this.add(txtAreaDescription, detailGBC);

        //PACK
//		this.pack();
        //      this.setResizable(false);
        //      this.setLocationRelativeTo(null);
        this.setVisible(true);

    }
}
