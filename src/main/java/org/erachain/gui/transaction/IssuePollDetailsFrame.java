package org.erachain.gui.transaction;

import org.erachain.core.item.polls.PollCls;
import org.erachain.core.transaction.IssuePollRecord;
import org.erachain.gui.library.MTextPane;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class IssuePollDetailsFrame extends RecDetailsFrame {
    public IssuePollDetailsFrame(IssuePollRecord pollIssue) {
        super(pollIssue, false);

        PollCls poll = (PollCls) pollIssue.getItem();

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.T("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++fieldGBC.gridy;
        JTextField name = new JTextField(poll.viewName());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, fieldGBC);

        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.T("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        ++fieldGBC.gridy;
        MTextPane txtAreaDescription = new MTextPane(poll.getDescription());
        //txtAreaDescription.setRows(4);
        txtAreaDescription.setBorder(name.getBorder());
        //txtAreaDescription.setEditable(false);
        MenuPopupUtil.installContextMenu(txtAreaDescription);
        this.add(txtAreaDescription, fieldGBC);

        //LABEL OPTIONS
        ++labelGBC.gridy;
        JLabel optionLabel = new JLabel(Lang.T("Options") + ":");
        this.add(optionLabel, labelGBC);

        // OPTIONs
        ++fieldGBC.gridy;
        JTextArea txtAreaOption = new JTextArea(String.join("\n", poll.getOptions()));
        txtAreaOption.setRows(4);
        txtAreaOption.setBorder(name.getBorder());
        txtAreaOption.setEditable(false);
        MenuPopupUtil.installContextMenu(txtAreaOption);
        this.add(txtAreaOption, fieldGBC);


        //maker
        ++fieldGBC.gridy;
        JTextField maker = new JTextField(poll.getMaker().getAddress());
        maker.setEditable(false);
        this.add(maker, fieldGBC);

        //LABEL maker Public key
        ++labelGBC.gridy;
        JLabel maker_Public_keyLabel = new JLabel(Lang.T("Public key") + ":");
        this.add(maker_Public_keyLabel, labelGBC);

        //maker public key
        ++fieldGBC.gridy;
        JTextField maker_Public_Key = new JTextField(poll.getMaker().getBase58());
        maker_Public_Key.setEditable(false);
        this.add(maker_Public_Key, fieldGBC);

        //PACK
        //	this.pack();
        //    this.setResizable(false);
        //    this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
