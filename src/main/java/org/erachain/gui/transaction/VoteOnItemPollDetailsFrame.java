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
        super(pollVote);

        PollCls poll = Controller.getInstance().getPoll(pollVote.getAbsKey());

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Poll") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++detailGBC.gridy;
        JTextField name = new JTextField(poll.toString(DCSet.getInstance()));
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, detailGBC);

        //LABEL OPTION
        ++labelGBC.gridy;
        JLabel optionLabel = new JLabel(Lang.getInstance().translate("Option") + ":");
        this.add(optionLabel, labelGBC);

        //OPTION
        ++detailGBC.gridy;
        JTextField option = new JTextField(poll.viewOption(pollVote.getOption()));
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
