package gui.transaction;

import javax.swing.JLabel;
import javax.swing.JTextField;

import controller.Controller;
import core.item.polls.PollCls;
import core.transaction.VoteOnItemPollTransaction;
import datachain.DCSet;
import lang.Lang;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class VoteOnItemPollDetailsFrame extends Rec_DetailsFrame
{
	public VoteOnItemPollDetailsFrame(VoteOnItemPollTransaction pollVote)
	{
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
