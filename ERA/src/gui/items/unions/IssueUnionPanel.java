package gui.items.unions;

import gui.MainFrame;
import gui.PasswordPane;
import gui.library.Issue_Confirm_Dialog;
import gui.library.My_Add_Image_Panel;
import gui.library.library;
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
import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;

//import settings.Settings;
import utils.DateTimeFormat;
import utils.Pair;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.item.assets.AssetCls;
import core.item.unions.UnionCls;
import core.transaction.IssueUnionRecord;
import core.transaction.Transaction;
import gui.transaction.OnDealClick;

@SuppressWarnings("serial")
public class IssueUnionPanel extends JPanel
{
	private JComboBox<Account> cbxFrom;
	private JTextField txtFeePow;
	private JTextField txtName;
	private JTextArea txtareaDescription;
	private JTextField txtBirthday;
	private JTextField txtParent;
	private JButton issueButton;
	private IssueUnionPanel th;

	public IssueUnionPanel()
	{
//		super(Lang.getInstance().translate("Erachain.org") + " - " + Lang.getInstance().translate("Issue Union"));
		
		String colorText ="ff0000"; // цвет текста в форме
		th=this;
   	 this.issueButton = new JButton();
   	 txtFeePow = new JTextField(); 
	 txtName = new JTextField();
	  txtareaDescription = new JTextArea();
	txtBirthday = new JTextField();
	txtParent = new JTextField();
   	 
   	 
		initComponents();
		
		add_logo_panel.setPreferredSize(new Dimension(250,50));
      
		           
        //BUTTON Register
    
       
        this.issueButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onIssueClick();
		    }
		});
    	
       
    
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
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.issueButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ CREATOR
		Account sender = (Account) this.cbxFrom.getSelectedItem();

		int parse = 0;
		int feePow = 0;
		long birthday = 0;
		long parent = -1;
		try
		{
			
			//READ FEE POW
			feePow = Integer.parseInt(this.txtFeePow.getText());

			// READ BIRTHDAY
			parse++;
			//birthday = Long.parseLong(this.txtBirthday.getText());
			// 1970-08-12 03:05:07
			String bd = this.txtBirthday.getText();
			if (bd.length() < 11) bd = bd + " 12:12:12";// UTC";
			Timestamp ts = Timestamp.valueOf(bd);
			birthday = ts.getTime();

			//READ PARENT			
			parse++;
			parent = Integer.parseInt(this.txtParent.getText());
						
		}
		catch(Exception e)
		{
			String mess = "Invalid pars... " + parse;
			switch(parse)
			{
			case 0:
				mess = "Invalid fee power 0..6";
				break;
			case 1:
				mess = "Invalid birthday [YYYY-MM-DD]";
				break;
			case 2:
				mess = "Invalid parent";
				break;
			}
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(e + mess), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			
			this.issueButton.setEnabled(true);
			return;
		}
						
		byte[] icon = null;
		byte[] image = null;
		//CREATE ASSET
		//PrivateKeyAccount creator, String fullName, int feePow, long birthday,
		//byte gender, String race, float birthLatitude, float birthLongitude,
		//String skinColor, String eyeColor, String hairСolor, int height, String description
		PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
		IssueUnionRecord issue_Union = (IssueUnionRecord) Controller.getInstance().issueUnion(
				creator, this.txtName.getText(), birthday, parent, this.txtareaDescription.getText(),
				add_logo_panel.imgButes, image,
				feePow);
		//Issue_Asset_Confirm_Dialog cont = new Issue_Asset_Confirm_Dialog(issueAssetTransaction);
		 String text = "<HTML><body>";
		 	text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"  + Lang.getInstance().translate("Issue Union") + "<br><br><br>";
		    text += Lang.getInstance().translate("Creator") +":&nbsp;"  + issue_Union.getCreator() +"<br>";
		    text += Lang.getInstance().translate("Name") +":&nbsp;"+ issue_Union.getItem().getName() +"<br>";
		   text += Lang.getInstance().translate("Description")+":<br>"+ library.to_HTML(issue_Union.getItem().getDescription())+"<br>";
		   text += Lang.getInstance().translate("Date") +":&nbsp;"+ ((UnionCls)issue_Union.getItem()).getBirthday() +"<br>";
		   text += Lang.getInstance().translate("Parent") +":&nbsp;"+ ((UnionCls)issue_Union.getItem()).getParent() +"<br>";
		    String Status_text = "<HTML>"+ Lang.getInstance().translate("Size")+":&nbsp;"+ issue_Union.viewSize(false)+" Bytes, ";
		    Status_text += "<b>" +Lang.getInstance().translate("Fee")+":&nbsp;"+ issue_Union.getFee().toString()+" COMPU</b><br></body></HTML>";
		    
		
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
		int result = Controller.getInstance().getTransactionCreator().afterCreate(issue_Union, false);
		//CHECK VALIDATE MESSAGE
		if (result == Transaction.VALIDATE_OK) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Union issue has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
	//		this.dispose();
			clearPanel();
		} else {		
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
		}
		
		//ENABLE
		this.issueButton.setEnabled(true);
	}
	
	void clearPanel(){
		
	this.txtName.setText("");
	this.txtareaDescription.setText("");
	this.txtBirthday.setText("1970-12-08");
	this.txtParent.setText("-1");
	this.txtFeePow.setText("0");
	
	
	}
	   private void initComponents() {
	        java.awt.GridBagConstraints gridBagConstraints;

	        account_jLabel = new javax.swing.JLabel();
	        name_jLabel = new javax.swing.JLabel();
	        add_logo_panel = new  My_Add_Image_Panel(Lang.getInstance().translate("Add Logo"), 50, 50);
	        add_image_panel = new My_Add_Image_Panel((Lang.getInstance().translate("Add Image")+(" (max %1%kB)").replace("%1%", "1024")), 250,250);
	        jScrollPane1 = new javax.swing.JScrollPane();
	        title_jLabel = new javax.swing.JLabel();
	        description_jLabel = new javax.swing.JLabel();
	        birthday_jLabel = new javax.swing.JLabel();
	        parent_jLabel = new javax.swing.JLabel();
	        fee_jLabel = new javax.swing.JLabel();
	      
	        setLayout(new java.awt.GridBagLayout());

	        account_jLabel.setText(Lang.getInstance().translate("Account") + ":");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 1;
	        gridBagConstraints.gridy = 1;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 7);
	        add(account_jLabel, gridBagConstraints);

	        this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 1;
	        gridBagConstraints.gridwidth = 5;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.weightx = 0.1;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 8);
	        add(cbxFrom, gridBagConstraints);

	        name_jLabel.setText(Lang.getInstance().translate("Name") + ":");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 1;
	        gridBagConstraints.gridy = 2;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 7);
	        add(name_jLabel, gridBagConstraints);

	        txtName.setText("");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 2;
	        gridBagConstraints.gridwidth = 4;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.weightx = 0.1;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
	        add(txtName, gridBagConstraints);

	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 6;
	        gridBagConstraints.gridy = 2;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 8);
	        add(add_logo_panel, gridBagConstraints);

	        add_image_panel.setPreferredSize(new java.awt.Dimension(250, 350));
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 1;
	        gridBagConstraints.gridheight = 4;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.insets = new java.awt.Insets(0, 8, 8, 8);
	        add(add_image_panel, gridBagConstraints);

	        txtareaDescription.setColumns(20);
	        txtareaDescription.setRows(5);
	        jScrollPane1.setViewportView(txtareaDescription);

	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 3;
	        gridBagConstraints.gridwidth = 5;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	        gridBagConstraints.weightx = 0.3;
	        gridBagConstraints.weighty = 0.5;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 8);
	        add(jScrollPane1, gridBagConstraints);

	        title_jLabel.setText(Lang.getInstance().translate("Issue Union"));
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 0;
	        gridBagConstraints.gridwidth = 7;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.weightx = 0.1;
	        gridBagConstraints.insets = new java.awt.Insets(8, 6, 6, 9);
	        add(title_jLabel, gridBagConstraints);

	        description_jLabel.setText(Lang.getInstance().translate("Description") + ":");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 1;
	        gridBagConstraints.gridy = 3;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 7);
	        add(description_jLabel, gridBagConstraints);

	        birthday_jLabel.setText(Lang.getInstance().translate("Birthday") + ":");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 1;
	        gridBagConstraints.gridy = 5;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 7);
	        add(birthday_jLabel, gridBagConstraints);

//TXT Birthday
	        
	     
	      	// Маска ввода
	      	MaskFormatter mf1 = null;
	      	try {
				 mf1 = new MaskFormatter("####-##-##");
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	      	this.txtBirthday = new JFormattedTextField(mf1); 
	      	this.txtBirthday.setText("1970-12-08");
	      
	      
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 5;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.weightx = 0.1;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 10);
	        add(txtBirthday, gridBagConstraints);

	        parent_jLabel.setText(Lang.getInstance().translate("Parent") + ":");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 4;
	        gridBagConstraints.gridy = 5;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 7);
	        add(parent_jLabel, gridBagConstraints);

	        txtParent.setText("0");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 5;
	        gridBagConstraints.gridy = 5;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.weightx = 0.1;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
	        add(txtParent, gridBagConstraints);

	        fee_jLabel.setText(Lang.getInstance().translate("Fee Power") + ":");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 1;
	        gridBagConstraints.gridy = 6;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
	        add(fee_jLabel, gridBagConstraints);

	        txtFeePow.setText("0");
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 2;
	        gridBagConstraints.gridy = 6;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.weightx = 0.1;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
	        add(txtFeePow, gridBagConstraints);

	        issueButton.setText(Lang.getInstance().translate("Issue"));
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 5;
	        gridBagConstraints.gridy = 6;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
	        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
	        add(issueButton, gridBagConstraints);
	    }// </editor-fold>                        


	    // Variables declaration - do not modify                     
	    private javax.swing.JLabel title_jLabel;
	    private javax.swing.JLabel account_jLabel;
	    private javax.swing.JLabel description_jLabel;
	    private javax.swing.JLabel fee_jLabel;
	    private  My_Add_Image_Panel add_logo_panel;
	    private My_Add_Image_Panel add_image_panel;
	   
	    private javax.swing.JScrollPane jScrollPane1;
	    private javax.swing.JLabel name_jLabel;
	    private javax.swing.JLabel birthday_jLabel;
	    private javax.swing.JLabel parent_jLabel;
	 
	    // End of variables declaration   
	
}
