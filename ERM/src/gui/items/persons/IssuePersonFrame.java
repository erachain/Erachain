package gui.items.persons;

import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;
import lang.Lang;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

//import settings.Settings;
import utils.DateTimeFormat;
import utils.Pair;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.transaction.Transaction;

@SuppressWarnings("serial")
public class IssuePersonFrame extends JFrame
{
	private JComboBox<Account> cbxFrom;
	private JTextField txtScale;
	private JTextField txtFeePow;
	private JTextField txtName;
	private JTextArea txtareaDescription;
	private JTextField txtBirthday;
	private JTextField txtGender;
	private JTextField txtRace;
	private JTextField txtBirthLatitude;
	private JTextField txtBirthLongitude;
	private JTextField txtSkinColor;
	private JTextField txtEyeColor;
	private JTextField txtHairСolor;
	private JTextField txtHeight;
	private JButton issueButton;

	public IssuePersonFrame()
	{
		super(Lang.getInstance().translate("DATACHAINS.world") + " - " + Lang.getInstance().translate("Issue Person"));
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(5,5,5,5);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;
		
		//COMBOBOX GBC
		GridBagConstraints cbxGBC = new GridBagConstraints();
		cbxGBC.insets = new Insets(5,5,5,5);
		cbxGBC.fill = GridBagConstraints.NONE;  
		cbxGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxGBC.weightx = 0;	
		cbxGBC.gridx = 1;	
		
		//TEXTFIELD GBC
		GridBagConstraints txtGBC = new GridBagConstraints();
		txtGBC.insets = new Insets(5,5,5,5);
		txtGBC.fill = GridBagConstraints.HORIZONTAL;  
		txtGBC.anchor = GridBagConstraints.NORTHWEST;
		txtGBC.weightx = 1;	
		txtGBC.gridwidth = 2;
		txtGBC.gridx = 1;		
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5,5,5,5);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridwidth = 2;
		buttonGBC.gridx = 0;		
		
		int gridy = 0;
		//LABEL FROM
		labelGBC.gridy = gridy;
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
		this.add(fromLabel, labelGBC);
		
		//COMBOBOX FROM
		txtGBC.gridy = gridy++;
		this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.cbxFrom, txtGBC);
        
        //LABEL NAME
      	labelGBC.gridy = gridy;
      	JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
      	this.add(nameLabel, labelGBC);
      		
      	//TXT NAME
      	txtGBC.gridy = gridy++;
      	this.txtName = new JTextField();
        this.add(this.txtName, txtGBC);
        
        //LABEL DESCRIPTION
      	labelGBC.gridy = gridy;
      	JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
      	this.add(descriptionLabel, labelGBC);
      		
      	//TXTAREA DESCRIPTION
      	txtGBC.gridy = gridy++;
      	this.txtareaDescription = new JTextArea();
       	
      	this.txtareaDescription.setRows(6);
      	this.txtareaDescription.setColumns(20);
      	this.txtareaDescription.setBorder(this.txtName.getBorder());

      	JScrollPane scrollDescription = new JScrollPane(this.txtareaDescription);
      	scrollDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      	scrollDescription.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(scrollDescription, txtGBC);
      	      	
      	//LABEL GENDER
      	labelGBC.gridy = gridy;
      	JLabel genderLabel = new JLabel(Lang.getInstance().translate("Gender") + ":");
      	this.add(genderLabel, labelGBC);
      		
      	//TXT GENDER
      	txtGBC.gridy = gridy++;
      	this.txtGender = new JTextField();
      	this.txtGender.setText("1");
        this.add(this.txtGender, txtGBC);
        
        //LABEL Birthday
      	labelGBC.gridy = gridy;
      	JLabel birthdayLabel = new JLabel(Lang.getInstance().translate("Birthday") + ":");
      	this.add(birthdayLabel, labelGBC);
      		
      	//TXT Birthday
      	txtGBC.gridy = gridy++;
      	this.txtBirthday = new JTextField();
      	this.txtBirthday.setText("2");
        this.add(this.txtBirthday, txtGBC);

        //LABEL RACE
      	labelGBC.gridy = gridy;
      	JLabel raceLabel = new JLabel(Lang.getInstance().translate("Race") + ":");
      	this.add(raceLabel, labelGBC);
      		
      	//TXT RACE
      	txtGBC.gridy = gridy++;
      	this.txtRace = new JTextField();
      	this.txtRace.setText("-");
        this.add(this.txtRace, txtGBC);
      	
        //LABEL birthLatitude
      	labelGBC.gridy = gridy;
      	JLabel birthLatitudeLabel = new JLabel(Lang.getInstance().translate("Birth Latitude") + ":");
      	this.add(birthLatitudeLabel, labelGBC);
      		
      	//TXT birthLatitude
      	txtGBC.gridy = gridy++;
      	this.txtBirthLatitude = new JTextField();
      	this.txtBirthLatitude.setText("45.123");
        this.add(this.txtBirthLatitude, txtGBC);
      	
        //LABEL birthLongitude
      	labelGBC.gridy = gridy;
      	JLabel birthLongitudeLabel = new JLabel(Lang.getInstance().translate("Birth Longitude") + ":");
      	this.add(birthLongitudeLabel, labelGBC);
      		
      	//TXT birthLongitude
      	txtGBC.gridy = gridy++;
      	this.txtBirthLongitude = new JTextField();
      	this.txtBirthLongitude.setText("12.123");
        this.add(this.txtBirthLongitude, txtGBC);

        //LABEL skinColor
      	labelGBC.gridy = gridy;
      	JLabel skinColorLabel = new JLabel(Lang.getInstance().translate("Skin Color") + ":");
      	this.add(skinColorLabel, labelGBC);
      		
      	//TXT skinColor
      	txtGBC.gridy = gridy++;
      	this.txtSkinColor = new JTextField();
      	this.txtSkinColor.setText("");
        this.add(this.txtSkinColor, txtGBC);

        //LABEL eyeColor
      	labelGBC.gridy = gridy;
      	JLabel eyeColorLabel = new JLabel(Lang.getInstance().translate("Eye Color") + ":");
      	this.add(eyeColorLabel, labelGBC);
      		
      	//TXT eyeColor
      	txtGBC.gridy = gridy++;
      	this.txtEyeColor = new JTextField();
      	this.txtEyeColor.setText("");
        this.add(this.txtEyeColor, txtGBC);

        //LABEL hairСolor
      	labelGBC.gridy = gridy;
      	JLabel hairСolorLabel = new JLabel(Lang.getInstance().translate("Hair Сolor") + ":");
      	this.add(hairСolorLabel, labelGBC);
      		
      	//TXT hairСolor
      	txtGBC.gridy = gridy++;
      	this.txtHairСolor = new JTextField();
      	this.txtHairСolor.setText("");
        this.add(this.txtHairСolor, txtGBC);

        //LABEL height
      	labelGBC.gridy = gridy;
      	JLabel heightLabel = new JLabel(Lang.getInstance().translate("Height") + ":");
      	this.add(heightLabel, labelGBC);
      		
      	//TXT height
      	txtGBC.gridy = gridy++;
      	this.txtHeight = new JTextField();
      	this.txtHeight.setText("170");
        this.add(this.txtHeight, txtGBC);

        //LABEL FEE POW
      	labelGBC.gridy = gridy;
      	JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee Power") + ":");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT FEE
      	txtGBC.gridy = gridy++;
      	this.txtFeePow = new JTextField();
      	this.txtFeePow.setText("0");
        this.add(this.txtFeePow, txtGBC);
		           
        //BUTTON Register
        buttonGBC.gridy = gridy;
        this.issueButton = new JButton(Lang.getInstance().translate("Issue"));
        this.issueButton.setPreferredSize(new Dimension(100, 25));
        this.issueButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onIssueClick();
		    }
		});
    	this.add(this.issueButton, buttonGBC);
        
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
	
	public void onIssueClick()
	{
		//DISABLE
		this.issueButton.setEnabled(false);
	
		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, Lang.getInstance().translate("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.issueButton.setEnabled(true);
			
			return;
		}
		
		//CHECK IF WALLET UNLOCKED
		if(!Controller.getInstance().isWalletUnlocked())
		{
			//ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(); 
			if(!Controller.getInstance().unlockWallet(password))
			{
				//WRONG PASSWORD
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.issueButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ CREATOR
		Account sender = (Account) this.cbxFrom.getSelectedItem();

		long parse = 0;
		try
		{
			
			//READ FEE POW
			int feePow = Integer.parseInt(this.txtFeePow.getText());

			//READ GENDER
			byte gender = Byte.parseByte(this.txtGender.getText());
			long birthday = Long.parseLong(this.txtBirthday.getText());
			
			float birthLatitude = Float.parseFloat(this.txtBirthLatitude.getText());
			float birthLongitude = Float.parseFloat(this.txtBirthLongitude.getText());

			int height = Integer.parseInt(this.txtHeight.getText());
						
			//CREATE ASSET
			//PrivateKeyAccount creator, String fullName, int feePow, long birthday,
			//byte gender, String race, float birthLatitude, float birthLongitude,
			//String skinColor, String eyeColor, String hairСolor, int height, String description
			PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
			Pair<Transaction, Integer> result = Controller.getInstance().issuePerson(
					creator, this.txtName.getText(), feePow, birthday,
					gender, this.txtRace.getText(), birthLatitude, birthLongitude,
					this.txtSkinColor.getText(), this.txtEyeColor.getText(),
					this.txtHairСolor.getText(), height, this.txtareaDescription.getText()
					);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OK:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Person issue has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
				this.dispose();
				break;	
								
			case Transaction.NOT_ENOUGH_FEE:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Not enough OIL balance!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
												
			case Transaction.INVALID_NAME_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Name must be between 1 and 100 characters!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_DESCRIPTION_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Description must be between 1 and 1000 characters!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
								
			case Transaction.ACCOUNT_NOT_PERSON:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Issuer account not personalized!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	

			default:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Unknown error")
						+ "[" + result.getB() + "]!" , Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;		
				
			}
		}
		catch(Exception e)
		{
			if(parse == 0)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid quantity!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
		}
		
		//ENABLE
		this.issueButton.setEnabled(true);
	}
}
