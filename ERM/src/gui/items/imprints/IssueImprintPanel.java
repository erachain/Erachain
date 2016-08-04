package gui.items.imprints;

import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;
import gui.transaction.OnDealClick;
import lang.Lang;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
//import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.imprints.Imprint;
import core.transaction.Transaction;

@SuppressWarnings("serial")
public class IssueImprintPanel extends JPanel
{
	private JComboBox<Account> cbxFrom;
	private JTextField txtFeePow;
	private JTextField txtNumber;
	private JTextField txtDate;
	private JTextField txtDebitor;
	private JTextField txtCreditor;
	private JTextField txtAmount;
	private JButton issueButton;

	public IssueImprintPanel()
	{
	//	super(Lang.getInstance().translate("DATACHAINS.world") + " - " + Lang.getInstance().translate("Issue Imprint"));
//		this.setTitle(Lang.getInstance().translate("DATACHAINS.world") + " - " + Lang.getInstance().translate("Issue Imprint"));
		//CLOSE
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
//		this.setIconImages(icons);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
//		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		//labelGBC.insets = new Insets(5,5,5,5);
		labelGBC.insets = new java.awt.Insets(5, 15, 5, 5);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;
		
		//COMBOBOX GBC
		GridBagConstraints cbxGBC = new GridBagConstraints();
		//cbxGBC.insets = new Insets(5,5,5,5);
		cbxGBC.insets = new java.awt.Insets(5, 3, 5, 15);
		cbxGBC.fill = GridBagConstraints.NONE;  
		cbxGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxGBC.weightx = 0;	
		cbxGBC.gridx = 1;	
		
		//TEXTFIELD GBC
		GridBagConstraints txtGBC = new GridBagConstraints();
		//txtGBC.insets = new Insets(5,5,5,5);
		txtGBC.insets = new java.awt.Insets(5, 3, 5, 15);
		txtGBC.fill = GridBagConstraints.HORIZONTAL;  
		txtGBC.anchor = GridBagConstraints.NORTHWEST;
		txtGBC.weightx = 1;	
		txtGBC.gridwidth = 2;
		txtGBC.gridx = 1;		
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5,5,5,5);
		buttonGBC.insets = new java.awt.Insets(5, 3, 5, 15);
		buttonGBC.fill = GridBagConstraints.NONE;  
		//buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		 buttonGBC.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
		buttonGBC.gridwidth = 2;
		
		buttonGBC.gridx = 2;		
		
		int gridy = 0;
		//LABEL FROM
		labelGBC.gridy = gridy++;
		
		labelGBC.gridwidth = 3;
		
		
		JLabel label1 = new JLabel("      " + Lang.getInstance().translate("Issue Imprint"));
		label1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
		label1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		label1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		
		
		
		
		
		
		this.add(label1, labelGBC);
		
		labelGBC.gridwidth =1;
		
		
		
		
		//LABEL FROM
		labelGBC.gridy = gridy;
				
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
		this.add(fromLabel, labelGBC);
		
		//COMBOBOX FROM
		txtGBC.gridy = gridy++;
		this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.cbxFrom, txtGBC);
        
        //LABEL NUMBER
      	labelGBC.gridy = gridy;
      	JLabel numberLabel = new JLabel(Lang.getInstance().translate("Number") + "(0..9/-.):");
      	this.add(numberLabel, labelGBC);
      		
      	//TXT NUMBER
      	txtGBC.gridy = gridy++;
      	this.txtNumber = new JTextField();
        this.add(this.txtNumber, txtGBC);

        //LABEL DATE
      	labelGBC.gridy = gridy;
      	JLabel dateLabel = new JLabel(Lang.getInstance().translate("Date") + "(YY-MM-DD HH:MM");
      	this.add(dateLabel, labelGBC);
      		
      	//TXT DEBITOR
      	txtGBC.gridy = gridy++;
      	this.txtDate = new JTextField();
        this.add(this.txtDate, txtGBC);
        
        //LABEL DEBITOR
      	labelGBC.gridy = gridy;
      	JLabel debitorLabel = new JLabel(Lang.getInstance().translate("Debitor INN") + ":");
      	this.add(debitorLabel, labelGBC);
      		
      	//TXT DEBITOR
      	txtGBC.gridy = gridy++;
      	this.txtDebitor = new JTextField();
        this.add(this.txtDebitor, txtGBC);

        //LABEL CREDITOR
      	labelGBC.gridy = gridy;
      	JLabel creditorLabel = new JLabel(Lang.getInstance().translate("Creditor INN") + ":");
      	this.add(creditorLabel, labelGBC);
      		
      	//TXT DEBITOR
      	txtGBC.gridy = gridy++;
      	this.txtCreditor = new JTextField();
        this.add(this.txtCreditor, txtGBC);

        //LABEL CREDITOR
      	labelGBC.gridy = gridy;
      	JLabel amountLabel = new JLabel(Lang.getInstance().translate("Amount") + "(123.03):");
      	this.add(amountLabel, labelGBC);
      		
      	//TXT DEBITOR
      	txtGBC.gridy = gridy++;
      	this.txtAmount = new JTextField();
        this.add(this.txtAmount, txtGBC);

        //this.txtareaDescription.setText("");
        /*
        //LABEL DESCRIPTION
      	labelGBC.gridy = gridy;
      	JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
      	this.add(descriptionLabel, labelGBC);
      		
      	//TXTAREA DESCRIPTION
      	txtGBC.gridy = gridy++;
      	this.txtareaDescription = new JTextArea();
       	
      	this.txtareaDescription.setRows(6);
      	this.txtareaDescription.setColumns(20);
      	this.txtareaDescription.setBorder(this.txtNumber.getBorder());

      	JScrollPane scrollDescription = new JScrollPane(this.txtareaDescription);
      	scrollDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      	scrollDescription.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(scrollDescription, txtGBC);

      	*/
      	      	
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
    	
    	
    	//BUTTON GBC
    			
    			
    			buttonGBC.fill = GridBagConstraints.NONE;  
    			buttonGBC.anchor = GridBagConstraints.NORTHWEST;
    			buttonGBC.gridwidth = 2;
    			buttonGBC.gridx = 0;
    			buttonGBC.weighty =1.0;
    	JLabel labBootom = new JLabel("");
    	this.add(labBootom, buttonGBC);
    	
    	
        
    	
 //   	this.setModal(true);
        //PACK
//		this.pack();
//        this.setResizable(false);
//        this.setLocationRelativeTo(null);
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
			// READ AMOUNT
			//float amount = Float.parseFloat(this.txtAmount.getText());
			
			// NAME TOTAL
			String name_total = this.txtNumber.getText().trim() + this.txtDate.getText().trim()
				+ this.txtDebitor.getText().trim() + this.txtCreditor.getText().trim() + this.txtAmount.getText().trim();

			// CUT BYTES LEN
			name_total = Imprint.hashNameToBase58(name_total);
			String description = ""; //this.txtareaDescription.getText();
						
			//CREATE IMPRINT
			PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
			Pair<Transaction, Integer> result = Controller.getInstance().issueImprint(creator, name_total, description, feePow);
			
			//CHECK VALIDATE MESSAGE
			if (result.getB() == Transaction.VALIDATE_OK) {
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Imprint issue has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
	//			this.dispose();
			
				this.txtAmount.setText("");
				this.txtCreditor.setText("");
				this.txtDate.setText("");
				this.txtFeePow.setText("");
				this.txtNumber.setText("");
				this.txtDebitor.setText("");
				this.cbxFrom.setSelectedIndex(0);
				
			
				
				
				
				
				
			}
			else {
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Unknown error")
						+ "[" + result.getB() + "]!" , Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
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
