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
import core.item.persons.PersonCls;
import core.transaction.IssuePersonRecord;
import lang.Lang;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class IssuePersonDetailsFrame extends JFrame
{
	public IssuePersonDetailsFrame(IssuePersonRecord personIssue)
	{
		super(Lang.getInstance().translate("DATACHAINS.world") + " - " + Lang.getInstance().translate("Transaction Details"));
		
		PersonCls person = (PersonCls)personIssue.getItem();
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(0, 5, 5, 0);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;
		
		//DETAIL GBC
		GridBagConstraints detailGBC = new GridBagConstraints();
		detailGBC.insets = new Insets(0, 5, 5, 0);
		detailGBC.fill = GridBagConstraints.HORIZONTAL;  
		detailGBC.anchor = GridBagConstraints.NORTHWEST;
		detailGBC.weightx = 1;	
		detailGBC.gridwidth = 2;
		detailGBC.gridx = 1;		
		
		//LABEL TYPE
		labelGBC.gridy = 0;
		JLabel typeLabel = new JLabel(Lang.getInstance().translate("Type") + ":");
		this.add(typeLabel, labelGBC);
						
		//TYPE
		detailGBC.gridy = 0;
		JLabel type = new JLabel(Lang.getInstance().translate("Issue Asset Transaction"));
		this.add(type, detailGBC);
		
		//LABEL SIGNATURE
		labelGBC.gridy = 1;
		JLabel signatureLabel = new JLabel(Lang.getInstance().translate("Signature") + ":");
		this.add(signatureLabel, labelGBC);
				
		//SIGNATURE
		detailGBC.gridy = 1;
		JTextField signature = new JTextField(Base58.encode(personIssue.getSignature()));
		signature.setEditable(false);
		MenuPopupUtil.installContextMenu(signature);
		this.add(signature, detailGBC);
		
		//LABEL REFERENCE
		labelGBC.gridy = 2;
		JLabel referenceLabel = new JLabel(Lang.getInstance().translate("Reference") + ":");
		this.add(referenceLabel, labelGBC);
						
		//REFERENCE
		detailGBC.gridy = 2;
		JTextField reference = new JTextField(Base58.encode(personIssue.getReference()));
		reference.setEditable(false);
		MenuPopupUtil.installContextMenu(reference);
		this.add(reference, detailGBC);
		
		//LABEL TIMESTAMP
		labelGBC.gridy = 3;
		JLabel timestampLabel = new JLabel(Lang.getInstance().translate("Timestamp") + ":");
		this.add(timestampLabel, labelGBC);
						
		//TIMESTAMP
		detailGBC.gridy = 3;
		JTextField timestamp = new JTextField(DateTimeFormat.timestamptoString(personIssue.getTimestamp()));
		timestamp.setEditable(false);
		MenuPopupUtil.installContextMenu(timestamp);
		this.add(timestamp, detailGBC);
		
		//LABEL CREATOR
		labelGBC.gridy = 4;
		JLabel creatorLabel = new JLabel(Lang.getInstance().translate("Creator") + ":");
		this.add(creatorLabel, labelGBC);
		
		//CREATOR
		detailGBC.gridy = 4;
		JTextField creator = new JTextField(personIssue.getCreator().getAddress());
		creator.setEditable(false);
		MenuPopupUtil.installContextMenu(creator);
		this.add(creator, detailGBC);
				
		//LABEL NAME
		labelGBC.gridy = 6;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
		this.add(nameLabel, labelGBC);
		
		//NAME
		detailGBC.gridy = 6;
		JTextField name = new JTextField(personIssue.getItem().getName());
		name.setEditable(false);
		MenuPopupUtil.installContextMenu(name);
		this.add(name, detailGBC);		
		
		//LABEL DESCRIPTION
		labelGBC.gridy = 7;
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		this.add(descriptionLabel, labelGBC);
				
		//DESCRIPTION
		detailGBC.gridy = 7;
		JTextArea txtAreaDescription = new JTextArea(personIssue.getItem().getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(name.getBorder());
		txtAreaDescription.setEditable(false);
		MenuPopupUtil.installContextMenu(txtAreaDescription);
		this.add(txtAreaDescription, detailGBC);		
		
		//LABEL GENDER
		labelGBC.gridy = 8;
		JLabel genderLabel = new JLabel(Lang.getInstance().translate("Gender") + ":");
		this.add(genderLabel, labelGBC);
				
		//GENDER
		detailGBC.gridy = 8;
		JTextField gender = new JTextField(person.getGender());
		gender.setEditable(false);
		//MenuPopupUtil.installContextMenu(gender);
		this.add(gender, detailGBC);	
		
		//LABEL Birthday
		labelGBC.gridy = 9;
		JLabel birthdayLabel = new JLabel(Lang.getInstance().translate("Birthday") + ":");
		this.add(birthdayLabel, labelGBC);
				
		//Birthday
		detailGBC.gridy = 9;
		JTextField birtday = new JTextField(new Date(person.getBirthday()).toString());
		gender.setEditable(false);
		this.add(birtday, detailGBC);	
		
		//LABEL FEE
		labelGBC.gridy = 10;
		JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee") + ":");
		this.add(feeLabel, labelGBC);
						
		//FEE
		detailGBC.gridy = 10;
		JTextField fee = new JTextField("("+personIssue.getFeePow() + ")="+personIssue.getFee().toPlainString());
		fee.setEditable(false);
		MenuPopupUtil.installContextMenu(fee);
		this.add(fee, detailGBC);	
		
		//LABEL CONFIRMATIONS
		labelGBC.gridy = 11;
		JLabel confirmationsLabel = new JLabel(Lang.getInstance().translate("Confirmations") + ":");
		this.add(confirmationsLabel, labelGBC);
								
		//CONFIRMATIONS
		detailGBC.gridy = 11;
		JLabel confirmations = new JLabel(String.valueOf(personIssue.getConfirmations()));
		this.add(confirmations, detailGBC);	

		//LABEL SIZE
		labelGBC.gridy = 12;
		JLabel sizeLabel = new JLabel(Lang.getInstance().translate("Size") + ":");
		this.add(sizeLabel, labelGBC);
								
		//SIZE
		detailGBC.gridy = 12;
		JLabel size = new JLabel(String.valueOf(personIssue.getDataLength(false)));
		this.add(size, detailGBC);	
		           
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
