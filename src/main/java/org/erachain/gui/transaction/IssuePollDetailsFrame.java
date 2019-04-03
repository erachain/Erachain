package org.erachain.gui.transaction;

import org.erachain.core.item.polls.PollCls;
import org.erachain.core.transaction.IssuePollRecord;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class IssuePollDetailsFrame extends RecDetailsFrame {
    public IssuePollDetailsFrame(IssuePollRecord pollIssue) {
        super(pollIssue);

        PollCls poll = (PollCls) pollIssue.getItem();

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++detailGBC.gridy;
        JTextField name = new JTextField(poll.viewName());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, detailGBC);

        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        ++detailGBC.gridy;
        JTextArea txtAreaDescription = new JTextArea(poll.getDescription());
        txtAreaDescription.setRows(4);
        txtAreaDescription.setBorder(name.getBorder());
        txtAreaDescription.setEditable(false);
        MenuPopupUtil.installContextMenu(txtAreaDescription);
        this.add(txtAreaDescription, detailGBC);

        //LABEL OPTIONS
        ++labelGBC.gridy;
        JLabel optionLabel = new JLabel(Lang.getInstance().translate("Options") + ":");
        this.add(optionLabel, labelGBC);

        // OPTIONs
        ++detailGBC.gridy;
        JTextArea txtAreaOption = new JTextArea(String.join("\n", poll.getOptions()));
        txtAreaOption.setRows(4);
        txtAreaOption.setBorder(name.getBorder());
        txtAreaOption.setEditable(false);
        MenuPopupUtil.installContextMenu(txtAreaOption);
        this.add(txtAreaOption, detailGBC);


        //owner
        ++detailGBC.gridy;
        JTextField owner = new JTextField(poll.getOwner().getAddress());
        owner.setEditable(false);
        this.add(owner, detailGBC);

        //LABEL owner Public key
        ++labelGBC.gridy;
        JLabel owner_Public_keyLabel = new JLabel(Lang.getInstance().translate("Public key") + ":");
        this.add(owner_Public_keyLabel, labelGBC);

        //owner public key
        ++detailGBC.gridy;
        JTextField owner_Public_Key = new JTextField(poll.getOwner().getBase58());
        owner_Public_Key.setEditable(false);
        this.add(owner_Public_Key, detailGBC);

        //PACK
        //	this.pack();
        //    this.setResizable(false);
        //    this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
