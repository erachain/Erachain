package gui.items.notes;

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
import core.item.assets.AssetCls;
import core.transaction.Transaction;

@SuppressWarnings("serial")
public class IssueNotePanel extends JPanel
{
	private JComboBox<Account> jComboBox_Account_Creator;
//	private JTextField txtScale;
//	private JTextField jTextField_Fee;
//	private JTextField jTextField_Title;
//	private JTextArea jTextArea_Content;
//	private JTextField txtQuantity;
//	private JCheckBox chkDivisible;
	//private JButton jButton_Create;

	public IssueNotePanel()
	{
//		super(Lang.getInstance().translate("ARONICLE.com") + " - " + Lang.getInstance().translate("Issue Note"));
		
		//CLOSE
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//ICON
		
		this.jComboBox_Account_Creator = new JComboBox<Account>(new AccountsComboBoxModel());
		initComponents();
/*		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
//		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
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
		
		//LABEL FROM
		labelGBC.gridy = 0;
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
		this.add(fromLabel, labelGBC);
		
		//COMBOBOX FROM
		txtGBC.gridy = 0;
		*/
		
      /* 
		this.add(this.jComboBox_Account_Creator, txtGBC);
        
        //LABEL NAME
      	labelGBC.gridy = 1;
      	JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
      	this.add(nameLabel, labelGBC);
      		
      	//TXT NAME
      	txtGBC.gridy = 1;
      	this.jTextField_Title = new JTextField();
        this.add(this.jTextField_Title, txtGBC);
        
        //LABEL DESCRIPTION
      	labelGBC.gridy = 2;
      	JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
      	this.add(descriptionLabel, labelGBC);
      		
      	//TXTAREA DESCRIPTION
      	txtGBC.gridy = 2;
      	this.jTextArea_Content = new JTextArea();
       	
      	this.jTextArea_Content.setRows(6);
      	this.jTextArea_Content.setColumns(20);
      	this.jTextArea_Content.setBorder(this.jTextField_Title.getBorder());

      	JScrollPane scrollDescription = new JScrollPane(this.jTextArea_Content);
      	scrollDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      	scrollDescription.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(scrollDescription, txtGBC);
      	
      	
      	//LABEL QUANTITY
      	labelGBC.gridy = 3;
      	JLabel quantityLabel = new JLabel(Lang.getInstance().translate("Quantity") + ":");
      	this.add(quantityLabel, labelGBC);
      		
      	//TXT QUANTITY
  
      	
        //LABEL FEE POW
      	labelGBC.gridy = 6;
      	JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee Power") + ":");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT FEE
      	txtGBC.gridy = 6;
      	this.jTextField_Fee = new JTextField();
      	this.jTextField_Fee.setText("0");
        this.add(this.jTextField_Fee, txtGBC);
		           
        //BUTTON Register
        buttonGBC.gridy = 7;
        this.jButton_Create = new JButton(Lang.getInstance().translate("Issue"));
        this.jButton_Create.setPreferredSize(new Dimension(100, 25));
       
 */       
        //PACK
//		this.pack();
 //       this.setResizable(false);
 //       this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
	
	public void onIssueClick()
	{
		//DISABLE
		this.jButton_Create.setEnabled(false);
	
		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, Lang.getInstance().translate("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.jButton_Create.setEnabled(true);
			
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
				this.jButton_Create.setEnabled(true);
				
				return;
			}
		}
		
		//READ CREATOR
		Account sender = (Account) this.jComboBox_Account_Creator.getSelectedItem();
		
		long parse = 0;
		int feePow = 0;
		try
		{
			
			//READ FEE POW
			feePow = Integer.parseInt(this.jTextField_Fee.getText());
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
			
			//ENABLE
			this.jButton_Create.setEnabled(true);
			return;

		}
						
			//CREATE NOTE
			PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
			Pair<Transaction, Integer> result = Controller.getInstance().issueNote(creator, this.jTextField_Title.getText(), this.jTextArea_Content.getText(), feePow);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OK:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Template issue has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
			//	this.dispose();
				
				
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
								
			case Transaction.CREATOR_NOT_PERSONALIZED:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Issuer account not personalized!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	

			default:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Unknown error")
						+ "[" + result.getB() + "]!" , Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;		
				
			}
		
		//ENABLE
		this.jButton_Create.setEnabled(true);
	}
	   private void initComponents() {
	        java.awt.GridBagConstraints gridBagConstraints;

	        jLabel_Title = new javax.swing.JLabel();
	        jLabel_Content = new javax.swing.JLabel();
	        jTextField_Title = new javax.swing.JTextField();
	        jScrollPane1 = new javax.swing.JScrollPane();
	        jTextArea_Content = new javax.swing.JTextArea();
	        jLabel_Fee = new javax.swing.JLabel();
	        jLabel_Account_Creator = new javax.swing.JLabel();
	    //    jComboBox_Account_Creator = new javax.swing.JComboBox<>();
	        jTextField_Fee = new javax.swing.JTextField();
	        jLabel_Template = new javax.swing.JLabel();
	        jLabel_Issue_Note = new javax.swing.JLabel();
	        jComboBox_Template = new javax.swing.JComboBox<>();
	        jCheckBox_Encrypted = new javax.swing.JCheckBox();
	        jButton_Create = new javax.swing.JButton();
	        jLabel_auto_saze = new javax.swing.JLabel();

	        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
	        layout.columnWidths = new int[] {0, 8, 0, 8, 0, 8, 0, 8, 0};
	        layout.rowHeights = new int[] {0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0};
	        setLayout(layout);

	        jLabel_Title.setText(Lang.getInstance().translate("Title")+":");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 4;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
	        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
	        add(jLabel_Title, gridBagConstraints);

	        jLabel_Content.setText(Lang.getInstance().translate("Content")+":");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 8;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
	        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
	        add(jLabel_Content, gridBagConstraints);

	        jTextField_Title.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                jTextField_TitleActionPerformed(evt);
	            }
	        });
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 4;
	        gridBagConstraints.gridwidth = 7;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.weightx = 0.1;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
	        add(jTextField_Title, gridBagConstraints);

	        jTextArea_Content.setColumns(20);
	  //      jTextArea_Content.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
	        jTextArea_Content.setLineWrap(true);
	        jTextArea_Content.setRows(18);
	        jTextArea_Content.setAlignmentY(1.0F);
	        jScrollPane1.setViewportView(jTextArea_Content);

	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 8;
	        gridBagConstraints.gridwidth = 7;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.weightx = 0.1;
	        gridBagConstraints.weighty = 0.7;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
	        add(jScrollPane1, gridBagConstraints);

	        jLabel_Fee.setText(Lang.getInstance().translate("Fee")+":");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 12;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
	        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
	        add(jLabel_Fee, gridBagConstraints);

	        jLabel_Account_Creator.setText(Lang.getInstance().translate("Account Creator")+":");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 2;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
	        add(jLabel_Account_Creator, gridBagConstraints);

	       
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 2;
	        gridBagConstraints.gridwidth = 7;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.weightx = 0.1;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
	        add(jComboBox_Account_Creator, gridBagConstraints);

	        jTextField_Fee.setText("0");
	        jTextField_Fee.setToolTipText("Level of FEE Power");
	        jTextField_Fee.setMaximumSize(new java.awt.Dimension(80, 20));
	        jTextField_Fee.setMinimumSize(new java.awt.Dimension(80, 20));
	        jTextField_Fee.setName(""); // NOI18N
	        jTextField_Fee.setPreferredSize(new java.awt.Dimension(80, 20));
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 12;
	        gridBagConstraints.gridwidth = 3;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
	        add(jTextField_Fee, gridBagConstraints);

	        jLabel_Template.setText(Lang.getInstance().translate("Template")+":");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 6;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
	        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
	 //       add(jLabel_Template, gridBagConstraints);

	 //       jLabel_Issue_Note.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
	        jLabel_Issue_Note.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
	        jLabel_Issue_Note.setText(Lang.getInstance().translate("Issue Template"));
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 0;
	        gridBagConstraints.gridwidth = 9;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.weightx = 0.1;
	        gridBagConstraints.insets = new java.awt.Insets(12, 15, 0, 15);
	        add(jLabel_Issue_Note, gridBagConstraints);

	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 6;
	        gridBagConstraints.gridwidth = 7;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.weightx = 0.1;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
	  //      add(jComboBox_Template, gridBagConstraints);

	        jCheckBox_Encrypted.setText(Lang.getInstance().translate("Encrypted"));
	        jCheckBox_Encrypted.setToolTipText("");
	        jCheckBox_Encrypted.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                jCheckBox_EncryptedActionPerformed(evt);
	            }
	        });
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 10;
	        gridBagConstraints.gridwidth = 5;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.weightx = 0.1;
	        add(jCheckBox_Encrypted, gridBagConstraints);

	        jButton_Create.setText(Lang.getInstance().translate("Create"));
	        jButton_Create.setPreferredSize(new java.awt.Dimension(90, 28));
	        jButton_Create.setRequestFocusEnabled(false);
	        jButton_Create.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	            	 onIssueClick();
	            }
	        });
	        
	        
	        
	        
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 6;
	        gridBagConstraints.gridy = 12;
	        gridBagConstraints.gridwidth = 3;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
	        add(jButton_Create, gridBagConstraints);
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 16;
	        gridBagConstraints.gridwidth = 5;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	        gridBagConstraints.weightx = 0.1;
	        gridBagConstraints.weighty = 0.3;
	        add(jLabel_auto_saze, gridBagConstraints);
	        this.setMinimumSize(new Dimension(0,0));
	    }// </editor-fold>                        

	    private void jComboBox_Account_CreatorActionPerformed(java.awt.event.ActionEvent evt) {                                                          
	        // TODO add your handling code here:
	    }                                                         

	    private void jTextField_TitleActionPerformed(java.awt.event.ActionEvent evt) {                                                 
	        // TODO add your handling code here:
	    }                                                

	    private void jButton_CreateActionPerformed(java.awt.event.ActionEvent evt) {                                               
	        // TODO add your handling code here:
	    }                                              

	    private void jCheckBox_EncryptedActionPerformed(java.awt.event.ActionEvent evt) {                                                    
	        // TODO add your handling code here:
	    }                                                   


	    // Variables declaration - do not modify                     
	    private javax.swing.JButton jButton_Create;
	    private javax.swing.JCheckBox jCheckBox_Encrypted;
	//    private javax.swing.JComboBox<String> jComboBox_Account_Creator;
	    private javax.swing.JComboBox<String> jComboBox_Template;
	    private javax.swing.JLabel jLabel_Account_Creator;
	    private javax.swing.JLabel jLabel_Content;
	    private javax.swing.JLabel jLabel_Fee;
	    private javax.swing.JLabel jLabel_Issue_Note;
	    private javax.swing.JLabel jLabel_Template;
	    private javax.swing.JLabel jLabel_Title;
	    private javax.swing.JLabel jLabel_auto_saze;
	    private javax.swing.JScrollPane jScrollPane1;
	    private javax.swing.JTextArea jTextArea_Content;
	    private javax.swing.JTextField jTextField_Fee;
	    private javax.swing.JTextField jTextField_Title;
}
