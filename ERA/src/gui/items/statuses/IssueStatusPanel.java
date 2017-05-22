package gui.items.statuses;

import gui.MainFrame;
import gui.PasswordPane;
import gui.library.Issue_Confirm_Dialog;
import gui.library.library;
import gui.models.AccountsComboBoxModel;
import lang.Lang;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import utils.Pair;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.item.assets.AssetCls;
import core.item.statuses.StatusCls;
import core.transaction.IssueStatusRecord;
import core.transaction.Transaction;
import gui.transaction.OnDealClick;

@SuppressWarnings("serial")
public class IssueStatusPanel extends JPanel
{
	private JComboBox<Account> cbxFrom;
	private JTextField txtFeePow;
	private JTextField txtName;
	private JTextArea txtareaDescription;
	private JButton issueButton;
	private JCheckBox jCheck_Unique;
	private IssueStatusPanel th;

	//@SuppressWarnings({ "unchecked", "rawtypes" })
	public IssueStatusPanel()
	{
//		this.setTitle(Lang.getInstance().translate("ARONICLE.com") + " - " + Lang.getInstance().translate("Issue Status"));
		
		String colorText ="ff0000"; // цвет текста в форме
		th = this;
		//CLOSE
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		/*
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		*/
		
	//	this.setVisible(true);
//		this.setModal(true);
	//	this.setMaximizable(true);
//		this.setTitle(Lang.getInstance().translate("Issue Status"));
	//	this.setClosable(true);
//		this.setResizable(true);
	//	setPreferredSize(new Dimension(800, 600));
	//	this.setLocation(50, 20);
		
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
	//	((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
	//	labelGBC.insets = new Insets(5,5,5,5);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;
		labelGBC.insets = new java.awt.Insets(5, 15, 5, 5);
		
		//COMBOBOX GBC
		GridBagConstraints cbxGBC = new GridBagConstraints();
		//cbxGBC.insets = new Insets(5,5,5,5);
		cbxGBC.fill = GridBagConstraints.NONE;  
		cbxGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxGBC.weightx = 0;	
		cbxGBC.gridx = 1;	
		cbxGBC.insets = new java.awt.Insets(5, 3, 5, 15);
		
		//TEXTFIELD GBC
		GridBagConstraints txtGBC = new GridBagConstraints();
		//txtGBC.insets = new Insets(5,5,5,5);
		txtGBC.fill = GridBagConstraints.HORIZONTAL;  
		txtGBC.anchor = GridBagConstraints.NORTHWEST;
		txtGBC.weightx = 1;	
		txtGBC.gridwidth = 2;
		txtGBC.gridx = 1;	
		txtGBC.insets = new java.awt.Insets(5, 3, 5, 15);
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new java.awt.Insets(8, 5, 5, 15);
		buttonGBC.fill = GridBagConstraints.NONE;  
		//buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		 buttonGBC.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
		buttonGBC.gridwidth = 2;
		buttonGBC.gridx = 0;		
		
		int gridy = 0;
		
	//	JLabel info_Label = new JLabel();
	//	info_Label.setText(Lang.getInstance().translate("Issue Status"));
	//	labelGBC.gridy = gridy;
		
		JLabel jLabel1 = new JLabel();
		jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(Lang.getInstance().translate("Create Status"));
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.3;
        gridBagConstraints.insets = new java.awt.Insets(8, 15, 8, 15);
        add(jLabel1, gridBagConstraints);
	
		
		
		
		
		
//		this.add(info_Label, labelGBC);
		gridy++;
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
      	JLabel nameLabel = new JLabel("<HTML><p style=':#" + colorText +"'>" +Lang.getInstance().translate("Name") + ": </p></html>");
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
      	      	
        //LABEL FEE POW
      	labelGBC.gridy = gridy;
      	JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee Power")+" (0..6)" + ":");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT FEE POW
      	txtGBC.gridy = gridy++;
      	this.txtFeePow = new JTextField();
      	this.txtFeePow.setText("0");
        this.add(this.txtFeePow, txtGBC);
		
        // JCheckBox jCheck_Unique;
        labelGBC.gridy = gridy;
        JLabel unoqueLabel = new JLabel(Lang.getInstance().translate("Unique") + ":");
      	this.add(unoqueLabel, labelGBC);
      	
      	txtGBC.gridy = gridy++;
      	 jCheck_Unique = new JCheckBox();
         this.add(this.jCheck_Unique, txtGBC);
        
        
        //BUTTON Register
        buttonGBC.gridy = gridy++;
        buttonGBC.gridx = 1;
        
       
        
        
        
        this.issueButton = new JButton(Lang.getInstance().translate("Issue"));
        this.issueButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onIssueClick();
		    }
		});
    	this.add(this.issueButton, buttonGBC);
    	
    	labelGBC.gridy = gridy;
    	labelGBC.weighty = 1.0;
    	labelGBC.fill = labelGBC.NORTH;
    	JLabel label_bottom = new JLabel();
    	
    	this.add(label_bottom, labelGBC);
    	
        
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

		int parse = 0;
		int feePow = 0;
		try
		{
			
			//READ FEE POW
			feePow = Integer.parseInt(this.txtFeePow.getText());
			
		}
		catch(Exception e)
		{
			String mess = "Invalid pars... " + parse;
			switch(parse)
			{
			case 0:
				mess = "Invalid fee power 0..6";
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
		boolean unique = jCheck_Unique.isSelected();
		PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
		IssueStatusRecord issue_Status = (IssueStatusRecord) Controller.getInstance().issueStatus(
				creator, this.txtName.getText(), this.txtareaDescription.getText(),
				unique,
				icon, image,
				feePow);
		
		//Issue_Asset_Confirm_Dialog cont = new Issue_Asset_Confirm_Dialog(issueAssetTransaction);
		 String text = "<HTML><body>";
		 	text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"  + Lang.getInstance().translate("Issue Asset") + "<br><br><br>";
		    text += Lang.getInstance().translate("Creator") +":&nbsp;"  + issue_Status.getCreator() +"<br>";
		    text += Lang.getInstance().translate("Name") +":&nbsp;"+ issue_Status.getItem().getName() +"<br>";
		    text += Lang.getInstance().translate("Description")+":<br>"+ library.to_HTML(issue_Status.getItem().getDescription())+"<br>";
		   
		    
		    text += Lang.getInstance().translate("Unique")+": "+ ((StatusCls)issue_Status.getItem()).isUnique() + "<br>";
		    String Status_text = "<HTML>"+ Lang.getInstance().translate("Size")+":&nbsp;"+ issue_Status.viewSize(true)+" Bytes, ";
		    Status_text += "<b>" +Lang.getInstance().translate("Fee")+":&nbsp;"+ issue_Status.getFee().toString()+" COMPU</b><br></body></HTML>";
		    
		  System.out.print("\n"+ text +"\n");
	//	    UIManager.put("OptionPane.cancelButtonText", "Отмена");
	//	    UIManager.put("OptionPane.okButtonText", "Готово");
		
	//	int s = JOptionPane.showConfirmDialog(MainFrame.getInstance(), text, Lang.getInstance().translate("Issue Asset"),  JOptionPane.YES_NO_OPTION);
		
		Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(MainFrame.getInstance(), true,text, (int) (th.getWidth()/1.2), (int) (th.getHeight()/1.2),Status_text);
		dd.setLocationRelativeTo(th);
		dd.setVisible(true);
		
	//	JOptionPane.OK_OPTION
		if (!dd.isConfirm){ //s!= JOptionPane.OK_OPTION)	{
			
			this.issueButton.setEnabled(true);
			
			return;
		}
		
				
		//VALIDATE AND PROCESS
		int result = Controller.getInstance().getTransactionCreator().afterCreate(issue_Status, false);
		
		
		
		
		
		//CHECK VALIDATE MESSAGE
		if (result == Transaction.VALIDATE_OK) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Status issue has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
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
		this.txtFeePow.setText("0");
		
		
	}
	
	
}
