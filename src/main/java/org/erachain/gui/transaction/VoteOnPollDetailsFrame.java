package org.erachain.gui.transaction;

import org.erachain.core.transaction.VoteOnPollTransaction;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class VoteOnPollDetailsFrame extends RecDetailsFrame {
    public VoteOnPollDetailsFrame(VoteOnPollTransaction pollVote) {
        super(pollVote, true);

        //NAME
        ++detailGBC.gridy;
        JTextField name = new JTextField(pollVote.getPoll());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, detailGBC);

        //LABEL OPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Option") + ":");
        this.add(descriptionLabel, labelGBC);

        //OPTION
        ++detailGBC.gridy;
        JTextField option = new JTextField(String.valueOf(pollVote.getOption()));
        option.setEditable(false);
        MenuPopupUtil.installContextMenu(option);
        this.add(option, detailGBC);

        //PACK
        //	this.pack();
        //    this.setResizable(false);
        //     this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
