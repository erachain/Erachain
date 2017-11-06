package gui.transaction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import core.crypto.Base58;
import core.transaction.Transaction;
import datachain.DCSet;
import core.transaction.R_Hashes;
import core.transaction.R_Vouch;
import lang.Lang;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class HashesDetailsFrame extends Rec_DetailsFrame
{
	public HashesDetailsFrame(R_Hashes r_Hashes)
	{
		super(r_Hashes);
				
		//LABEL NAME
		++labelGBC.gridy;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("URL") + ":");
		this.add(nameLabel, labelGBC);
		
		//NAME
		++detailGBC.gridy;
		JTextField name = new JTextField(new String(r_Hashes.getURL(), Charset.forName("UTF-8")));
		name.setEditable(false);
		MenuPopupUtil.installContextMenu(name);
		this.add(name, detailGBC);		

		//LABEL DESCRIPTION
		++labelGBC.gridy;
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		this.add(descriptionLabel, labelGBC);
				
		//DESCRIPTION
		++detailGBC.gridy;
		JTextPane txtAreaDescription = new JTextPane();
		txtAreaDescription.setContentType("text/html");
	//	txtAreaDescription.setBackground(MainFrame.getFrames()[0].getBackground());

		txtAreaDescription.setText(new String(r_Hashes.getData(), Charset.forName("UTF-8")));
		/*
		txtAreaDescription.setRows(4);
		txtAreaDescription.setColumns(4);
		*/
		txtAreaDescription.setBorder(name.getBorder());
		txtAreaDescription.setEditable(false);
		//MenuPopupUtil.installContextMenu(txtAreaDescription);
		this.add(txtAreaDescription, detailGBC);

		//LABEL HASHES
		++labelGBC.gridy;
		JLabel hashesLabel = new JLabel(Lang.getInstance().translate("HASHES") + ":");
		this.add(hashesLabel, labelGBC);
				
		//HASHES
		++detailGBC.gridy;
		JTextPane txtAreaHashes = new JTextPane();
		txtAreaHashes.setContentType("text/html");
	//	txtAreaHashes.setBackground(MainFrame.getFrames()[0].getBackground());

		txtAreaHashes.setText("<html>" + String.join("<br />", r_Hashes.getHashesB58()) + "</html>");
		txtAreaHashes.setBorder(name.getBorder());
		txtAreaHashes.setEditable(false);
		//MenuPopupUtil.installContextMenu(txtAreaHashes);
		this.add(txtAreaHashes, detailGBC);

		//txtAreaHashes.setText(String.join(" ", r_Hashes.getHashesB58()));

        //PACK
//		this.pack();
//        this.setResizable(false);
//        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
	}
}
