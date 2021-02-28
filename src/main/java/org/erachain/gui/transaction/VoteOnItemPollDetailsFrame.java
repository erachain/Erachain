package org.erachain.gui.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.transaction.VoteOnItemPollTransaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class VoteOnItemPollDetailsFrame extends RecDetailsFrame {
    public VoteOnItemPollDetailsFrame(VoteOnItemPollTransaction pollVote) {
        super(pollVote, true);

        PollCls poll = Controller.getInstance().getPoll(pollVote.getAbsKey());

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.T("Poll") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++fieldGBC.gridy;
        JTextField name = new JTextField(poll.toString(DCSet.getInstance()));
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, fieldGBC);

        //LABEL OPTION
        ++labelGBC.gridy;
        JLabel optionLabel = new JLabel(Lang.T("Option") + ":");
        this.add(optionLabel, labelGBC);

        //OPTION
        ++fieldGBC.gridy;
        JTextField option = new JTextField(pollVote.viewOption());
        option.setEditable(false);
        MenuPopupUtil.installContextMenu(option);
        this.add(option, fieldGBC);

        //PACK
        //	this.pack();
        //    this.setResizable(false);
        //     this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
