package gui.transaction;

import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import core.item.persons.PersonCls;
import core.transaction.IssuePersonRecord;
import lang.Lang;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class IssuePersonDetailsFrame extends Rec_DetailsFrame
{
	public IssuePersonDetailsFrame(IssuePersonRecord personIssue)
	{
		super(personIssue);
		
		PersonCls person = (PersonCls)personIssue.getItem();
		
		//LABEL NAME
		++labelGBC.gridy;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
		this.add(nameLabel, labelGBC);
		
		//NAME
		++detailGBC.gridy;
		JTextField name = new JTextField(personIssue.getItem().getName());
		name.setEditable(false);
		MenuPopupUtil.installContextMenu(name);
		this.add(name, detailGBC);		
		
		//LABEL DESCRIPTION
		++labelGBC.gridy;
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		this.add(descriptionLabel, labelGBC);
				
		//DESCRIPTION
		++detailGBC.gridy;
		JTextArea txtAreaDescription = new JTextArea(personIssue.getItem().getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(name.getBorder());
		txtAreaDescription.setEditable(false);
		MenuPopupUtil.installContextMenu(txtAreaDescription);
		this.add(txtAreaDescription, detailGBC);		
		
		//LABEL GENDER
		++labelGBC.gridy;
		JLabel genderLabel = new JLabel(Lang.getInstance().translate("Gender") + ":");
		this.add(genderLabel, labelGBC);
				
		//GENDER
		++detailGBC.gridy;
		JTextField gender = new JTextField(person.getGender());
		gender.setEditable(false);
		//MenuPopupUtil.installContextMenu(gender);
		this.add(gender, detailGBC);	
		
		//LABEL Birthday
		++labelGBC.gridy;
		JLabel birthdayLabel = new JLabel(Lang.getInstance().translate("Birthday") + ":");
		this.add(birthdayLabel, labelGBC);
				
		//Birthday
		++detailGBC.gridy;
		JTextField birtday = new JTextField(new Date(person.getBirthday()).toString());
		gender.setEditable(false);
		this.add(birtday, detailGBC);	
				           
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
