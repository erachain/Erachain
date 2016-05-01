package gui.items.persons;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import gui.MainFrame;
import gui.transaction.OnDealClick;
import lang.Lang;
import utils.NameUtils;
import utils.Pair;
import utils.NameUtils.NameResult;

public class PersonConfirm extends JDialog { // InternalFrame  {

	public PersonConfirm(JComponent  apers, PersonCls person) {
		super();
	
		final JTextField pubKey1Txt = new JTextField();
		final JTextField pubKey2Txt = new JTextField();
		final JTextField pubKey3Txt = new JTextField();
		final JTextField pubKey1Details = new JTextField();
		final JTextField pubKey2Details = new JTextField();
		final JTextField pubKey3Details = new JTextField();

		final JTextField toDate = new JTextField();
		
		
	//	this.setBorder(new EmptyBorder(10, 10, 10, 10));
		//	MainFrame mainFram = new MainFrame();
	
		setSize(400,300);
        setLocationRelativeTo(apers);
        setModalityType(ModalityType.TOOLKIT_MODAL);
        setDefaultCloseOperation(HIDE_ON_CLOSE);    	
		
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

	    // Create a constraints object, and specify some default values
	    GridBagConstraints c = new GridBagConstraints();
	    
	    c.insets = new Insets(0, 5, 5, 0);
		c.fill = GridBagConstraints.HORIZONTAL;   
		c.anchor = GridBagConstraints.NORTH;
	    //c.fill = GridBagConstraints.BOTH; // components grow in both dimensions
	    //c.insets = new Insets(0, 5, 5, 0); // 5-pixel margins on all sides

	    GridBagConstraints label = new GridBagConstraints();
	    
	    label.insets = new Insets(0, 5, 5, 0);
		label.fill = GridBagConstraints.HORIZONTAL;   
		label.anchor = GridBagConstraints.NORTH;
		label.gridx = 0;
		label.gridheight = 2;
	    //c.fill = GridBagConstraints.BOTH; // components grow in both dimensions
	    //c.insets = new Insets(0, 5, 5, 0); // 5-pixel margins on all sides

	    // Create and add a bunch of buttons, specifying different grid
	    // position, and size for each.
	    // Give the first button a resize weight of 1.0 and all others
	    // a weight of 0.0. The first button will get all extra space.
	    c.gridx = 0;
	    c.gridy = 0;
	    c.gridwidth = 5;
	    c.gridheight =1;
	    c.weightx = c.weighty = 0;
	    this.add(new JLabel(Lang.getInstance().translate("Public Keys of") + " " + person.getName() +":"), c);
	    
	    c.gridx = 0;
	    c.gridy = 1;
	    c.gridwidth = 5;
	    c.gridheight = 1;
	    c.weightx = c.weighty = 0;
	    this.add(pubKey1Txt, c);
	    
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

        //LABEL RECEIVER DETAILS
	    label.gridy = 2;
	    this.add(new JLabel(Lang.getInstance().translate("Details") +":"), label);
      		
      	//RECEIVER DETAILS 
      	c.gridy = 2;
	    c.gridx = 1;
      	pubKey1Details.setEditable(false);
        this.add(pubKey1Details, c);


	    c.gridx = 0;
	    c.gridy = 3;
	    c.gridwidth = 5;
	    c.gridheight = 1;
	    c.weightx = c.weighty = 0;
	    this.add(pubKey2Txt, c);
	    
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
	    
        //LABEL RECEIVER DETAILS
	    label.gridy = 4;
	    this.add(new JLabel(Lang.getInstance().translate("Details") +":"), label);
      		
      	//RECEIVER DETAILS 
      	c.gridy = 4;
	    c.gridx = 1;
      	pubKey2Details.setEditable(false);
        this.add(pubKey2Details, c);

	    c.gridx = 0;
	    c.gridy = 5;
	    c.gridwidth = 5;
	    c.gridheight = 1;
	    c.weightx = c.weighty = 0;
	    this.add(pubKey3Txt, c);

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

        //LABEL RECEIVER DETAILS
	    label.gridy = 6;
	    this.add(new JLabel(Lang.getInstance().translate("Details") +":"), label);
      		
      	//RECEIVER DETAILS 
      	c.gridy = 6;
	    c.gridx = 1;
      	pubKey3Details.setEditable(false);
        this.add(pubKey3Details, c);

	    c.gridx = 0;
	    c.gridy = 7;
	    c.gridwidth = 2;
	    c.gridheight = 1;
	    this.add(new JLabel(Lang.getInstance().translate("To date") +":"), c);

	    c.gridx = 2;
	    c.gridy = 7;
	    c.gridwidth = 3;
	    c.gridheight = 1;
	    this.add(toDate, c);

	    c.gridx = 2;
	    c.gridy = 9;
	    c.gridwidth = 1;
	    c.gridheight = 1;
	    JButton Button_Cancel = new JButton(Lang.getInstance().translate("Cancel"));
	    Button_Cancel.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		// программа обработки при нажатии cancel
		    }
		});
	    this.add( Button_Cancel, c);

	    c.gridx = 4;
	    c.gridy = 9;
	    c.gridwidth = 1;
	    c.gridheight = 1;
	    JButton Button_Confirm = new JButton(Lang.getInstance().translate("Confirm"));
	    this.add(Button_Confirm, c);
	    Button_Confirm.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	onGoClick(person, Button_Confirm, pubKey1Txt, pubKey1Txt, pubKey1Txt, toDate);
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
	    setPreferredSize(new Dimension(700, 500));
		//PACK
//		this.pack();
		//this.setSize(500, this.getHeight());
//		this.setResizable(true);
		this.setLocationRelativeTo(null);
	//	MainFrame.this.add(comp, constraints);//.setFocusable(false);
		this.setVisible(true);
	}
	
	private void refreshReceiverDetails(JTextField pubKeyTxt, JTextField pubKeyDetails)
	{
		String toValue = pubKeyTxt.getText();
		
		if(toValue.isEmpty())
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
		PublicKeyAccount account = new PublicKeyAccount(toValue);
		pubKeyDetails.setText(account.toString(Transaction.FEE_KEY));
		
		/*
		if(!Crypto.getInstance().isValidAddress(toValue))
		{
			Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(toValue);
					
			if(nameToAdress.getB() == NameResult.OK)
			{
				Account account = nameToAdress.getA();
				pubKeyDetails.setText(account.toString(Transaction.FEE_KEY));
			}
			else
			{
				pubKeyDetails.setText(nameToAdress.getB().getShortStatusMessage());
			}
		}else
		{
			Account account = new Account(toValue);
			pubKeyDetails.setText(account.toString(Transaction.FEE_KEY));
		}
		*/	
	}

	public void onGoClick(PersonCls person, JButton Button_Confirm,
			JTextField pubKey1Txt, JTextField pubKey2Txt, JTextField pubKey3Txt, JTextField toDate)
	{

    	if (!OnDealClick.proccess1(Button_Confirm)) return;

    	// программа обработки при нажатии confirm
    	String address = pubKey1Txt.getText();
    	int toDateVol = Integer.parseInt(toDate.getText());
    	int feePow = 0;
    	PublicKeyAccount userAccount1 = new PublicKeyAccount(Base58.decode(pubKey1Txt.getText()));
    	PublicKeyAccount userAccount2 = new PublicKeyAccount(Base58.decode(pubKey2Txt.getText()));
    	PublicKeyAccount userAccount3 = new PublicKeyAccount(Base58.decode(pubKey3Txt.getText()));
    	
		//Account authenticator =  new Account(address);
		PrivateKeyAccount authenticator = Controller.getInstance().getPrivateKeyAccountByAddress(address);
		int version = 5; // without user signs
		Pair<Transaction, Integer> result = Controller.getInstance().r_SertifyPerson(version, false, authenticator,
				feePow, person.getKey(), 
				userAccount1, userAccount2, userAccount3, toDateVol);
		
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
