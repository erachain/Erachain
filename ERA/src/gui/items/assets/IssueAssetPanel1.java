package gui.items.assets;

import gui.MainFrame;
import gui.PasswordPane;
import gui.library.Issue_Confirm_Dialog;
import gui.library.MButton;
import gui.library.library;
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
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.transaction.IssueAssetTransaction;
import core.transaction.Transaction;
import datachain.DCSet;

@SuppressWarnings("serial")
public class IssueAssetPanel1 extends JPanel
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
	private IssueAssetPanel1 th;
	private JPanel panel1 ;
	

	public IssueAssetPanel1()
	{
//		super(Lang.getInstance().translate("Erachain.org") + " - " + Lang.getInstance().translate("Issue Asset"));
		
		//CLOSE
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		th = this;
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
		
	        panel1 = new JPanel();
	        panel1.setLayout(new java.awt.GridBagLayout());
	        
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
      	
      	
   //   	this.txtareaDescription.setColumns(20);
      	this.txtareaDescription.setLineWrap(true);
    //  	this.txtareaDescription.setRows(5);
      	scrollDescription.setViewportView(this.txtareaDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        //gridBagConstraints.gridy;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.9;
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
      	this.txtScale.setText("8");
      	
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
         add(feeLabel, gridBagConstraints);
      	
  
      		
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
         add(this.txtFeePow, gridBagConstraints);
 
		           
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
     //   add(jLabel9, gridBagConstraints);
     //   this.add(this.issueButton, buttonGBC);
        
        //PACK
	//	this.pack();
   //     this.setResizable(false);
   //     this.setLocationRelativeTo(null);
        this.setMinimumSize(new Dimension(0,0));
        this.setVisible(true);
	}
	
	public void onIssueClick()
	{
		//DISABLE
		this.issueButton.setEnabled(false);
	
		//CHECK IF NETWORK OK
		if(false && Controller.getInstance().getStatus() != Controller.STATUS_OK)
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
			String password = PasswordPane.showUnlockWalletDialog(this); 
			if(!Controller.getInstance().unlockWallet(password))
			{
				//WRONG PASSWORD
				JOptionPane.showMessageDialog(MainFrame.getInstance(), Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.issueButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ CREATOR
		Account sender = (Account) this.cbxFrom.getSelectedItem();
		
		int parsestep = 0;
		try
		{
			
			//READ FEE POW
			int feePow = Integer.parseInt(this.txtFeePow.getText());
			
			//READ SCALSE
			parsestep++;
			byte scale = Byte.parseByte(this.txtScale.getText());

			//READ QUANTITY
			parsestep++;
			long quantity = Long.parseLong(this.txtQuantity.getText());
			boolean asPack = false;
			
			//CREATE ASSET
			parsestep++;
			PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
			IssueAssetTransaction issueAssetTransaction = (IssueAssetTransaction)Controller.getInstance().issueAsset(creator, this.txtName.getText(), this.txtareaDescription.getText(), null, null, this.chkMovable.isSelected(), quantity, scale, this.chkDivisible.isSelected(),
					 feePow);			

			//Issue_Asset_Confirm_Dialog cont = new Issue_Asset_Confirm_Dialog(issueAssetTransaction);
			 String text = "<HTML><body>";
			 	text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"  + Lang.getInstance().translate("Issue Asset") + "<br><br><br>";
			    text += Lang.getInstance().translate("Creator") +":&nbsp;"  + issueAssetTransaction.getCreator() +"<br>";
			    text += Lang.getInstance().translate("Name") +":&nbsp;"+ issueAssetTransaction.getItem().getName() +"<br>";
			    text += Lang.getInstance().translate("Quantity") +":&nbsp;"+ ((AssetCls)issueAssetTransaction.getItem()).getQuantity(DCSet.getInstance()).toString()+"<br>";
			    text += Lang.getInstance().translate("Movable") +":&nbsp;"+ Lang.getInstance().translate(((AssetCls)issueAssetTransaction.getItem()).isMovable()+"")+ "<br>";
			    text += Lang.getInstance().translate("Divisible") +":&nbsp;"+ Lang.getInstance().translate(((AssetCls)issueAssetTransaction.getItem()).isDivisible()+"")+ "<br>";
			    text += Lang.getInstance().translate("Scale") +":&nbsp;"+ ((AssetCls)issueAssetTransaction.getItem()).getScale()+ "<br>";
			    text += Lang.getInstance().translate("Description")+":<br>"+ library.to_HTML(issueAssetTransaction.getItem().getDescription())+"<br>";
			    String Status_text = "<HTML>"+ Lang.getInstance().translate("Size")+":&nbsp;"+ issueAssetTransaction.viewSize(false)+" Bytes, ";
			    Status_text += "<b>" +Lang.getInstance().translate("Fee")+":&nbsp;"+ issueAssetTransaction.getFee().toString()+" COMPU</b><br></body></HTML>";
			    
			  System.out.print("\n"+ text +"\n");
		//	    UIManager.put("OptionPane.cancelButtonText", "Отмена");
		//	    UIManager.put("OptionPane.okButtonText", "Готово");
			
		//	int s = JOptionPane.showConfirmDialog(MainFrame.getInstance(), text, Lang.getInstance().translate("Issue Asset"),  JOptionPane.YES_NO_OPTION);
			
			Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(MainFrame.getInstance(), true,text, (int) (th.getWidth()/1.2), (int) (th.getHeight()/1.2),Status_text, Lang.getInstance().translate("Confirmation Transaction"));
			dd.setLocationRelativeTo(th);
			dd.setVisible(true);
			
		//	JOptionPane.OK_OPTION
			if (!dd.isConfirm){ //s!= JOptionPane.OK_OPTION)	{
				
				this.issueButton.setEnabled(true);
				
				return;
			}
			
					
			//VALIDATE AND PROCESS
			int result = Controller.getInstance().getTransactionCreator().afterCreate(issueAssetTransaction, asPack);
			
			

			//CHECK VALIDATE MESSAGE
			switch(result)
			{
			case Transaction.VALIDATE_OK:
				
				JOptionPane.showMessageDialog(MainFrame.getInstance(), Lang.getInstance().translate("Asset issue has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
					
				break;	
				
			case Transaction.INVALID_QUANTITY:
				
				JOptionPane.showMessageDialog(MainFrame.getInstance(), Lang.getInstance().translate("Invalid quantity!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.NOT_ENOUGH_FEE:
				
				JOptionPane.showMessageDialog(MainFrame.getInstance(), Lang.getInstance().translate("Not enough %fee% balance!").replace("%fee%", AssetCls.FEE_NAME), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
								
			case Transaction.INVALID_NAME_LENGTH:
				
				JOptionPane.showMessageDialog(MainFrame.getInstance(), Lang.getInstance()
						.translate("Name must be between %m and %M characters!")
						.replace("%m", ""+ issueAssetTransaction.getItem().getMinNameLen())
						.replace("%M", ""+ItemCls.MAX_NAME_LENGTH),
						Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_DESCRIPTION_LENGTH:
				
				JOptionPane.showMessageDialog(MainFrame.getInstance(), Lang.getInstance().translate("Description must be between 1 and 1000 characters!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_PAYMENTS_LENGTH:
				
				JOptionPane.showMessageDialog(MainFrame.getInstance(), Lang.getInstance().translate("Invalid quantity!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.CREATOR_NOT_PERSONALIZED:
				
				JOptionPane.showMessageDialog(MainFrame.getInstance(), Lang.getInstance().translate("Issuer account not personalized!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	

			default:
				
				JOptionPane.showMessageDialog(MainFrame.getInstance(), Lang.getInstance().translate("Unknown error")
						+ "[" + result + "]!" , Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;		
				
			}
		}
		catch(Exception e)
		{
			switch(parsestep)
			{
			case 0:				
				JOptionPane.showMessageDialog(MainFrame.getInstance(), Lang.getInstance().translate("Invalid Fee Power!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
			case 1:
				JOptionPane.showMessageDialog(MainFrame.getInstance(), Lang.getInstance().translate("Invalid Scale!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
			case 2:
				JOptionPane.showMessageDialog(MainFrame.getInstance(), Lang.getInstance().translate("Invalid Quantity!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
				
			default:
				JOptionPane.showMessageDialog(MainFrame.getInstance(), Lang.getInstance().translate("Invalid Asset!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		
		//ENABLE
		this.issueButton.setEnabled(true);
	}
}
