package gui.transaction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import core.crypto.Base58;
import core.transaction.VoteOnPollTransaction;
import lang.Lang;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class VoteOnPollDetailsFrame extends Rec_DetailsFrame
{
	public VoteOnPollDetailsFrame(VoteOnPollTransaction pollVote)
	{
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
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
