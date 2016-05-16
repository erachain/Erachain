package gui.items.persons;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import api.ApiErrorFactory;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import core.transaction.Transaction;
import database.DBSet;
import gui.MainFrame;
import gui.models.AccountsComboBoxModel;
import gui.transaction.OnDealClick;
import lang.Lang;
import ntp.NTP;
import utils.NameUtils;
import utils.Pair;
import utils.NameUtils.NameResult;

//public class PersonConfirm extends JDialog { // InternalFrame  {
public class PersonConfirmFrame extends JFrame  {

	private JComboBox<Account> accountLBox;

	public PersonConfirmFrame(JComponent  apers, PersonCls person) {
		super();
	

		final JTextField pubKey1Txt = new JTextField();
		final JTextField pubKey2Txt = new JTextField();
		final JTextField pubKey3Txt = new JTextField();
		final JLabel pubKey1Details = new JLabel();
		final JLabel pubKey2Details = new JLabel();
		final JLabel pubKey3Details = new JLabel();

		final JTextField toDate = new JTextField("0");
		final JTextField feePow = new JTextField("0");
		
		
	//	this.setBorder(new EmptyBorder(10, 10, 10, 10));
		//	MainFrame mainFram = new MainFrame();
	
		setSize(400,300);
        //setLocationRelativeTo(apers);
        //setModalityType(ModalityType.TOOLKIT_MODAL);
        //setDefaultCloseOperation(HIDE_ON_CLOSE);    	
		
	//		this.setMaximizable(true);
	//		this.setTitle(Lang.getInstance().translate("Person confirm"));
	//		this.setClosable(true);
	//		this.setResizable(true);
	//		this.setModal(true);
	//		this.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);//.TOOLKIT_MODAL);//.APPLICATION_MODAL);
	//		this.setModalExclusionType (Dialog.ModalExclusionType.NO_EXCLUDE);
		
		//	 setLocationRelativeTo(owner);
	//	        setModalityType(ModalityType.TOOLKIT_MODAL);
	//	        setDefaultCloseOperation(HIDE_ON_CLOSE);     
		
			
		//	this.setLocation(50, 20);
		//	this.setIconImages(icons);
			
		//CLOSE
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
    
		//LAYOUT
		this.setLayout(new GridBagLayout());
		// Create and specify a layout manager
	    this.setLayout(new GridBagLayout());

	    int gridy = 0;
	    // Create a constraints object, and specify some default values
	    GridBagConstraints input = new GridBagConstraints();
	    input.insets = new Insets(0, 5, 5, 0); // 5-pixel margins on all sides
		input.fill = GridBagConstraints.HORIZONTAL;   
		input.anchor = GridBagConstraints.NORTHWEST;
	    input.gridwidth = 5;
	    input.gridheight = 1;

	    GridBagConstraints label = new GridBagConstraints();	    
	    label.insets = new Insets(0, 5, 5, 0);
		label.fill = GridBagConstraints.HORIZONTAL;   
		label.anchor = GridBagConstraints.NORTHWEST;
		label.gridx = 0;
		label.gridheight = 1;

	    GridBagConstraints detail = new GridBagConstraints();
	    detail.insets = new Insets(0, 5, 5, 0);
	    detail.fill = GridBagConstraints.BOTH; // components grow in both dimensions
	    detail.anchor = GridBagConstraints.NORTHWEST;
	    detail.gridx = 0;
	    detail.gridwidth = 5;
	    detail.gridheight = 2;
	    detail.weightx = -1;
	    detail.weighty = -1;

		//LABEL FROM
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
		this.add(fromLabel, label);
		
		//COMBOBOX FROM
		this.accountLBox = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.accountLBox, input);

	    input.gridx = 0;
	    this.add(new JLabel(Lang.getInstance().translate("Public Keys of") + " " + person.getName() +":"), input);
	    
	    input.gridy = ++gridy;
	    input.gridwidth = 5;
	    input.gridheight = 1;
	    //c.weightx = c.weighty = 0;
	    this.add(pubKey1Txt, input);
	    
	    pubKey1Txt.getDocument().addDocumentListener(new DocumentListener() {
            
			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				refreshReceiverDetails(pubKey1Txt, pubKey1Details);
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				refreshReceiverDetails(pubKey1Txt, pubKey1Details);
			}
        });
	          		
	    JPanel htmlPanel1 = new JPanel();
        htmlPanel1.setBorder(BorderFactory.createTitledBorder(Lang.getInstance().translate("Details")));
         
        String text = ""; //<html><h4>What is Google Labs?</h4>" +
                  //" well, just plain crazy.</html>";
         
        //font = new Font(null, Font.PLAIN, 10);
         
        pubKey1Details.setText(text);
        //pubKey1Details.
        htmlPanel1.add(pubKey1Details);        
        this.add(htmlPanel1, detail); // BorderLayout.SOUTH);
 
        gridy = 10;
	    input.gridx = 0;
	    input.gridy = ++gridy;
	    input.gridwidth = 5;
	    input.gridheight = 1;
	    //c.weightx = c.weighty = 0;
	    this.add(pubKey2Txt, input);
	    
	    pubKey2Txt.getDocument().addDocumentListener(new DocumentListener() {
            
			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				refreshReceiverDetails(pubKey2Txt, pubKey2Details);
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				refreshReceiverDetails(pubKey2Txt, pubKey2Details);
			}
        });
	    
	    JPanel htmlPanel2 = new JPanel();
        htmlPanel2.setBorder(BorderFactory.createTitledBorder(Lang.getInstance().translate("Details")));
        pubKey2Details.setText(text);
        htmlPanel2.add(pubKey2Details);        
        this.add(htmlPanel2, detail); // BorderLayout.SOUTH);

        gridy = 20;
        
	    input.gridx = 0;
	    input.gridy = ++gridy;
	    input.gridwidth = 5;
	    input.gridheight = 1;
	    //c.weightx = c.weighty = 0;
	    this.add(pubKey3Txt, input);

	    pubKey3Txt.getDocument().addDocumentListener(new DocumentListener() {
            
			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				refreshReceiverDetails(pubKey3Txt, pubKey3Details);
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				refreshReceiverDetails(pubKey3Txt, pubKey3Details);
			}
        });

	    JPanel htmlPanel3 = new JPanel();
        htmlPanel3.setBorder(BorderFactory.createTitledBorder(Lang.getInstance().translate("Details")));
         
        pubKey3Details.setText(text);
        htmlPanel3.add(pubKey3Details);        
        this.add(htmlPanel3, detail); // BorderLayout.SOUTH);

        gridy = 30;

        input.gridx = 0;
	    input.gridy = ++gridy;
	    input.gridwidth = 5;
	    input.gridheight = 1;
	    this.add(new JLabel(Lang.getInstance().translate("To date") +":"), input);

	    input.gridx = 2;
	    input.gridy = gridy;
	    input.gridwidth = 3;
	    input.gridheight = 1;
	    this.add(toDate, input);

	    // FEE POWER
        input.gridx = 0;
	    input.gridy = ++gridy;
	    input.gridwidth = 5;
	    input.gridheight = 1;
	    this.add(new JLabel(Lang.getInstance().translate("Fee Power") +":"), input);

	    input.gridx = 2;
	    input.gridy = gridy;
	    input.gridwidth = 3;
	    input.gridheight = 1;
      	feePow.setText("0");
	    this.add(feePow, input);

	    // BUTTONS
	    input.gridx = 2;
	    input.gridy = ++gridy;
	    input.gridwidth = 1;
	    input.gridheight = 1;
	    JButton Button_Cancel = new JButton(Lang.getInstance().translate("Cancel"));
	    Button_Cancel.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		// программа обработки при нажатии cancel
		    }
		});
	    this.add( Button_Cancel, input);

	    input.gridx = 4;
	    input.gridy = gridy;
	    input.gridwidth = 1;
	    input.gridheight = 1;
	    JButton Button_Confirm = new JButton(Lang.getInstance().translate("Confirm"));
	    this.add(Button_Confirm, input);
	    Button_Confirm.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	onGoClick(person, Button_Confirm, pubKey1Txt, pubKey2Txt, pubKey3Txt, toDate, feePow);
		    }
		});
	    
/*
	    c.gridx = 3;
	    c.gridy = 4;
	    c.gridwidth = 2;
	    c.gridheight = 1;
	    this.add(new JButton("Button #7"), c);

	    c.gridx = 1;
	    c.gridy = 5;
	    c.gridwidth = 1;
	    c.gridheight = 1;
	    this.add(new JButton("Button #8"), c);

	    c.gridx = 3;
	    c.gridy = 5;
	    c.gridwidth = 1;
	    c.gridheight = 1;
	    this.add(new JButton("Button #9"), c);
    
	    */
	    setPreferredSize(new Dimension(500, 600));
		//PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	    //MainFrame.this.add(comp, constraints).setFocusable(false);
	}
	
	private void refreshReceiverDetails(JTextField pubKeyTxt, JLabel pubKeyDetails)
	{
		String toValue = pubKeyTxt.getText();
		
		if(toValue.isEmpty() || toValue.length() < 30)
		{
			pubKeyDetails.setText("");
			return;
		}
		
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			pubKeyDetails.setText(Lang.getInstance().translate("Status must be OK to show public key details."));
			return;
		}
		
		//CHECK IF RECIPIENT IS VALID ADDRESS
		boolean isValid = false;
		try {
			isValid = PublicKeyAccount.isValidPublicKey(toValue);
		}
		catch(Exception e) {

		}
		if (!isValid) {
			// SHOW error message
			pubKeyDetails.setText(ApiErrorFactory.getInstance().messageError(ApiErrorFactory.ERROR_INVALID_ADDRESS));
		} else {
			PublicKeyAccount account = new PublicKeyAccount(toValue); 
			// SHOW account for FEE asset
			String personDetails;
			Tuple4<Long, Integer, Integer, Integer> addressDuration = account.getPersonDuration(DBSet.getInstance());
			
			if (addressDuration == null) personDetails = Lang.getInstance().translate("Not personalized yet");
			else
			{
				// TEST TIME and EXPIRE TIME
				long current_time = NTP.getTime();
				
				// TEST TIME and EXPIRE TIME
				int daysLeft = (int)((addressDuration.b - current_time) / (long)86400000);	
				if (daysLeft < 0 ) personDetails = Lang.getInstance().translate("Personalize ended %days% ago").replace("%days%", ""+daysLeft);
				else personDetails = Lang.getInstance().translate("Personalize is valid for %days% days").replace("%days%", ""+daysLeft);

				// IF PERSON ALIVE
				Long personKey = addressDuration.a;
				Tuple4<Long, Long, Integer, Integer> aliveDuration = DBSet.getInstance().getPersonStatusMap().getItem(personKey, StatusCls.ALIVE_KEY);
				daysLeft = (int)((aliveDuration.b - current_time) / (long)86400000);	
				if (daysLeft < 0 ) personDetails = personDetails + "<br>" + Lang.getInstance().translate("Person died %days% ago days ago").replace("%days%", ""+daysLeft);
				else personDetails = personDetails + "<br>" + Lang.getInstance().translate("Person is still alive %days%").replace("%days%", ""+daysLeft);
				
				//personDetails = personDetails; 
			}

			pubKeyDetails.setText("<html><h3>" + personDetails + "</h3>" + account.toString(Transaction.FEE_KEY) + "</html>");
			
		}
		
	}

	public void onGoClick(
			PersonCls person, JButton Button_Confirm,
			JTextField pubKey1Txt, JTextField pubKey2Txt, JTextField pubKey3Txt, JTextField toDateTxt, JTextField feePowTxt)
	{

    	if (!OnDealClick.proccess1(Button_Confirm)) return;

		Account creator = (Account) this.accountLBox.getSelectedItem();
    	//String address = pubKey1Txt.getText();
    	int toDate = 0;
    	int feePow = 0;
    	int parse = 0;
    	String toDateStr = toDateTxt.getText();
		try {

			//READ FEE POW
			feePow = Integer.parseInt(feePowTxt.getText());
			
			//READ to DAY
			parse++;
	    	if (toDateStr.length() > 0)
    			toDate = Integer.parseInt(toDateStr);
    		}
		catch(Exception e)
		{
			if(parse == 0)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid to Date"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
		}
    	
	    List<PublicKeyAccount> sertifiedPublicKeys = new ArrayList<PublicKeyAccount>();
	    if (pubKey1Txt.getText().length() > 30) {
	    	PublicKeyAccount userAccount1 = new PublicKeyAccount(Base58.decode(pubKey1Txt.getText()));
	    	if (userAccount1.isValid()) sertifiedPublicKeys.add(userAccount1);
	    }
	    if (pubKey2Txt.getText().length() > 30) {
	    	PublicKeyAccount userAccount2 = new PublicKeyAccount(Base58.decode(pubKey2Txt.getText()));
	    	if (userAccount2.isValid()) sertifiedPublicKeys.add(userAccount2);
	    }
	    if (pubKey3Txt.getText().length() > 30) {
	    	PublicKeyAccount userAccount3 = new PublicKeyAccount(Base58.decode(pubKey3Txt.getText()));
	    	if (userAccount3.isValid()) sertifiedPublicKeys.add(userAccount3);
	    }
    	
		//Account authenticator =  new Account(address);
		PrivateKeyAccount authenticator = Controller.getInstance().getPrivateKeyAccountByAddress(creator.getAddress());

		int version = 0; // without user signs
		
		Pair<Transaction, Integer> result = Controller.getInstance().r_SertifyPerson(version, false, authenticator,
				feePow, person.getKey(), 
				sertifiedPublicKeys, toDate);
		
		//CHECK VALIDATE MESSAGE
		if (result.getB() == Transaction.VALIDATE_OK) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Person has been authenticated!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
			this.dispose();
		} else {
		
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result.getB())), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
		}
		
		//ENABLE
		Button_Confirm.setEnabled(true);
		
	}
	
}
