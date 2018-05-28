package gui.transaction;

import core.transaction.VoteOnPollTransaction;
import lang.Lang;
import utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class VoteOnPollDetailsFrame extends Rec_DetailsFrame {
    public VoteOnPollDetailsFrame(VoteOnPollTransaction pollVote) {
        super(pollVote);

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
