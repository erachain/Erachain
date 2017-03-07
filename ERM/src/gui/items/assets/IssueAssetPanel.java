package gui.items.assets;

import gui.PasswordPane;
import gui.library.MButton;
import gui.models.AccountsComboBoxModel;
import gui.transaction.TransactionDetailsFactory;
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

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

//import settings.Settings;
import utils.DateTimeFormat;
import utils.Pair;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.item.assets.AssetCls;
import core.transaction.IssueAssetTransaction;
import core.transaction.Transaction;

@SuppressWarnings("serial")
public class IssueAssetPanel extends JPanel
{
	private JComboBox<Account> cbxFrom;
	private JTextField txtScale;
	private JTextField txtFeePow;
	private JTextField txtName;
	private JTextArea txtareaDescription;
	private JCheckBox chkMovable;
	private JTextField txtQuantity;
	private JCheckBox chkDivisible;
	private MButton issueButton;

	public IssueAssetPanel()
	{
//		super(Lang.getInstance().translate("ARONICLE.com") + " - " + Lang.getInstance().translate("Issue Asset"));
		
		//CLOSE
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		int gridy = 0;
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
//		this.setIconImages(icons);
		
		//LAYOUT
		  java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
	        layout.columnWidths = new int[] {0, 7, 0};
	        layout.rowHeights = new int[] {0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0};
	        setLayout(layout);

	        JLabel jLabel1 = new JLabel();
			jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
	        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
	        jLabel1.setText(Lang.getInstance().translate("Issue Asset"));
	        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
	        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        //gridBagConstraints.gridy = gridy;
	        gridBagConstraints.gridwidth = 3;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.weightx = 1.3;
	        gridBagConstraints.insets = new java.awt.Insets(8, 15, 8, 15);
	        add(jLabel1, gridBagConstraints);
		
		//PADDING
//		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
	/*	
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
*/		
		//LABEL FROM
		//labelGBC.gridy = 0;
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
		
		gridy += 2;
		gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        //gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(fromLabel, gridBagConstraints);
		
	
		
		//COMBOBOX FROM
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        //gridBagConstraints.gridy = gridy;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 15);
     
		this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.cbxFrom,  gridBagConstraints);
        
        //LABEL NAME
      
      	
      	JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
      	gridy += 2;
      	 gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         //gridBagConstraints.gridy = gridy;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
         add(nameLabel, gridBagConstraints);
      	
      	
      	
     
      		
      	//TXT NAME
      	
      	this.txtName = new JTextField();
      	gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        //gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 15);
    
        this.add(this.txtName, gridBagConstraints);
        
        //LABEL DESCRIPTION
  //    	labelGBC.gridy = 2;
      	JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
      	
      	gridy += 2;
      	descriptionLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
      	descriptionLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        //gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(descriptionLabel, gridBagConstraints);
      	
      	
     // 	this.add(descriptionLabel, labelGBC);
      		
      	//TXTAREA DESCRIPTION
   //   	txtGBC.gridy = 2;
      	this.txtareaDescription = new JTextArea();
      	
      
       	
  //    	this.txtareaDescription.setRows(6);
  //    	this.txtareaDescription.setColumns(20);
  //    	this.txtareaDescription.setBorder(this.txtName.getBorder());

      	JScrollPane scrollDescription = new JScrollPane(this.txtareaDescription);
  //    	scrollDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
  //    	scrollDescription.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	
      	
      	this.txtareaDescription.setColumns(20);
      	this.txtareaDescription.setLineWrap(true);
      	this.txtareaDescription.setRows(5);
      	scrollDescription.setViewportView(this.txtareaDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        //gridBagConstraints.gridy;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 15);
        add(scrollDescription, gridBagConstraints);
      	
      	
      	
      	
    //  	this.add(scrollDescription, txtGBC);
      	
      	

        //LABEL MOVABLE
   //   	labelGBC.gridy = 5;
      	gridy++;
      	JLabel movableLabel = new JLabel(Lang.getInstance().translate("Movable") + ":");
      	gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        //gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(movableLabel, gridBagConstraints);
      	
      	
     // 	this.add(divisibleLabel, labelGBC);
      		
      	//CHECKBOX MOVABLE
    //  	txtGBC.gridy = 5;
      	this.chkMovable = new JCheckBox();
      	this.chkMovable.setSelected(true);
      	
      	 gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
         add(this.chkMovable, gridBagConstraints);


        
      	//LABEL QUANTITY
      //	labelGBC.gridy = 3;
      	JLabel quantityLabel = new JLabel(Lang.getInstance().translate("Quantity") + ":");

      	gridy++;
      	gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        //gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(quantityLabel, gridBagConstraints);
      	
      	
   //   	this.add(quantityLabel, labelGBC);
      		
      	//TXT QUANTITY
    //  	txtGBC.gridy = 3;
      	this.txtQuantity = new JTextField();
      	this.txtQuantity.setText("0");
      	
      	 gridBagConstraints.gridx = 2;
         //gridBagConstraints.gridy = 8;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 15);
         add(this.txtQuantity, gridBagConstraints);
      	
      	
     //   this.add(this.txtQuantity, txtGBC);
        
        //LABEL SCALE
    //  	labelGBC.gridy = 4;
      	JLabel scaleLabel = new JLabel(Lang.getInstance().translate("Scale") + ":");
      	
      	gridy +=2;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        //gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(scaleLabel, gridBagConstraints);
      	
      	
   //   	this.add(scaleLabel, labelGBC);
      		
      	//TXT SCALE
  //    	txtGBC.gridy = 4;
      	this.txtScale = new JTextField();
      	this.txtScale.setText("0");
      	
      	 gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         //gridBagConstraints.gridy = 10;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 15);
         add(this.txtScale, gridBagConstraints);
      

        //LABEL DIVISIBLE
   //   	labelGBC.gridy = 5;
       	gridy +=2;
      	JLabel divisibleLabel = new JLabel(Lang.getInstance().translate("Divisible") + ":");
      	gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        //gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(divisibleLabel, gridBagConstraints);
      	
      	
     // 	this.add(divisibleLabel, labelGBC);
      		
      	//CHECKBOX DIVISIBLE
    //  	txtGBC.gridy = 5;
      	this.chkDivisible = new JCheckBox();
      	this.chkDivisible.setSelected(true);
      	
      	 gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         //gridBagConstraints.gridy = 12;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
         add(this.chkDivisible, gridBagConstraints);

      	
     // 	this.add(this.chkDivisible, txtGBC);
      	
        //LABEL FEE POW
    //  	labelGBC.gridy = 6;
       	gridy +=2;
      	JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee Power") + ":");
      	 gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         //gridBagConstraints.gridy = gridy;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
    //     add(feeLabel, gridBagConstraints);
      	
  ;
      		
      	//TXT FEE
  //    	txtGBC.gridy = 6;
      	this.txtFeePow = new JTextField();
      	this.txtFeePow.setText("0");
      	 gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         //gridBagConstraints.gridy = 14;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 15);
   //      add(this.txtFeePow, gridBagConstraints);
 
		           
        //BUTTON Register
        
        this.issueButton = new MButton(Lang.getInstance().translate("Issue"),2);
    //    this.issueButton.setPreferredSize(new Dimension(100, 25));
        this.issueButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onIssueClick();
		    }
		});
    	
      	gridy +=2;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        //gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 15);
        add(this.issueButton, gridBagConstraints);
        
        
        
       JLabel jLabel9 = new JLabel();
     	gridy +=2;
		jLabel9.setText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        //gridBagConstraints.gridy = gridy;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jLabel9, gridBagConstraints);
     //   this.add(this.issueButton, buttonGBC);
        
        //PACK
	//	this.pack();
   //     this.setResizable(false);
   //     this.setLocationRelativeTo(null);
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
			//READ SCALSE
			byte scale = Byte.parseByte(this.txtScale.getText());
			
			//READ FEE POW
			int feePow = Integer.parseInt(this.txtFeePow.getText());
			
			//READ QUANTITY
			parse = 1;
			long quantity = Long.parseLong(this.txtQuantity.getText());
			boolean asPack = false;
			
			//CREATE ASSET
			PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
			IssueAssetTransaction issueAssetTransaction = (IssueAssetTransaction)Controller.getInstance().issueAsset(creator, this.txtName.getText(), this.txtareaDescription.getText(), this.chkMovable.isSelected(), quantity, scale, this.chkDivisible.isSelected(), feePow);			

			//Issue_Asset_Confirm_Dialog cont = new Issue_Asset_Confirm_Dialog(issueAssetTransaction);
			 String text = "<HTML>";
			    text += "&nbsp;&nbsp;"+ Lang.getInstance().translate("Creator") +":&nbsp;"  + issueAssetTransaction.getCreator()+"<br>";
			    text += "&nbsp;&nbsp;" +Lang.getInstance().translate("Name") +":&nbsp;"+ issueAssetTransaction.viewItemName()+"<br>";
			    text += "&nbsp;&nbsp;" +Lang.getInstance().translate("Quantity") +":&nbsp;"+ ((AssetCls)issueAssetTransaction.getItem()).getQuantity().toString()+"<br>";
			    text += "&nbsp;&nbsp;" +Lang.getInstance().translate("Description")+":&nbsp;"+ issueAssetTransaction.getItem().getDescription()+"<br>";
			    text += "&nbsp;&nbsp;"+ Lang.getInstance().translate("Size")+":&nbsp;"+ issueAssetTransaction.viewSize(true)+"<br>";
			    text += "&nbsp;&nbsp; <b>" +Lang.getInstance().translate("Fee")+":&nbsp;"+ issueAssetTransaction.viewFee()+"</b><br>";
			    
			    UIManager.put("OptionPane.yesButtonText", Lang.getInstance().translate("Confirm"));
			    UIManager.put("OptionPane.noButtonText", Lang.getInstance().translate("Cancel"));
		//	    UIManager.put("OptionPane.cancelButtonText", "Отмена");
		//	    UIManager.put("OptionPane.okButtonText", "Готово");
			
			int s = JOptionPane.showConfirmDialog(new JFrame(), text, Lang.getInstance().translate("Issue Asset"),  JOptionPane.YES_NO_OPTION);
			
		//	JOptionPane.OK_OPTION
			if (s!= JOptionPane.OK_OPTION)	{
				
				this.issueButton.setEnabled(true);
				
				return;
			}
			
					
			//VALIDATE AND PROCESS
			int result = Controller.getInstance().getTransactionCreator().afterCreate(issueAssetTransaction, asPack);
			
			

			//CHECK VALIDATE MESSAGE
			switch(result)
			{
			case Transaction.VALIDATE_OK:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Asset issue has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
	
				this.txtName.setText("");
				this.txtareaDescription.setText("");
				this.txtQuantity.setText("");
				this.txtScale.setText("");
				this.txtFeePow.setText("");
				
				break;	
				
			case Transaction.INVALID_QUANTITY:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid quantity!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.NOT_ENOUGH_FEE:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Not enough %fee% balance!").replace("%fee%", AssetCls.FEE_NAME), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
								
			case Transaction.INVALID_NAME_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Name must be between 1 and 100 characters!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_DESCRIPTION_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Description must be between 1 and 1000 characters!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_PAYMENTS_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid quantity!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.CREATOR_NOT_PERSONALIZED:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Issuer account not personalized!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	

			default:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Unknown error")
						+ "[" + result + "]!" , Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
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
