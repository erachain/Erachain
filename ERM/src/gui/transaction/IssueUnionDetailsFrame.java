package gui.transaction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import core.crypto.Base58;
import core.item.unions.UnionCls;
import core.transaction.IssueUnionRecord;
import lang.Lang;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class IssueUnionDetailsFrame extends Rec_DetailsFrame
{
	public IssueUnionDetailsFrame(IssueUnionRecord unionIssue)
	{
		super(unionIssue);
				
		//LABEL NAME
		++labelGBC.gridy;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
		this.add(nameLabel, labelGBC);
		
		//NAME
		++detailGBC.gridy;
		JTextField name = new JTextField(unionIssue.getItem().getName());
		name.setEditable(false);
		MenuPopupUtil.installContextMenu(name);
		this.add(name, detailGBC);		
		
		UnionCls union = (UnionCls)unionIssue.getItem();
		
		//LABEL DESCRIPTION
		++labelGBC.gridy;
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		this.add(descriptionLabel, labelGBC);
				
		//DESCRIPTION
		++detailGBC.gridy;
		JTextArea txtAreaDescription = new JTextArea(unionIssue.getItem().getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(name.getBorder());
		txtAreaDescription.setEditable(false);
		MenuPopupUtil.installContextMenu(txtAreaDescription);
		this.add(txtAreaDescription, detailGBC);		
		
		//LABEL Birthday
		++labelGBC.gridy;
		JLabel birthdayLabel = new JLabel(Lang.getInstance().translate("Birthday") + ":");
		this.add(birthdayLabel, labelGBC);
				
		//Birthday
		++detailGBC.gridy;
		//JTextField birtday = new JTextField(new Date(union.getBirthday()).toString());
		JTextField birtday = new JTextField(union.getBirthdayStr());
		birtday.setEditable(false);
		this.add(birtday, detailGBC);	

		//LABEL PARENT
		++labelGBC.gridy;
		JLabel parentLabel = new JLabel(Lang.getInstance().translate("Parent") + ":");
		this.add(parentLabel, labelGBC);
				
		//PARENT
		++detailGBC.gridy;
		JTextField parent = new JTextField(String.valueOf(union.getParent()));
		parent.setEditable(false);
		//MenuPopupUtil.installContextMenu(gender);
		this.add(parent, detailGBC);	

        //PACK
//		this.pack();
  //      this.setResizable(false);
 //       this.setLocationRelativeTo(null);
        this.setVisible(true);
        
	}
}
