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

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import gui.MainFrame;
import gui.transaction.OnDealClick;
import lang.Lang;
import utils.Pair;

public class PersonConfirm extends JDialog { // InternalFrame  {

	public PersonConfirm(JComponent  apers, PersonCls person) {
		super();
	
		final JTextField addressTxt = new JTextField();
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

	    // Create and add a bunch of buttons, specifying different grid
	    // position, and size for each.
	    // Give the first button a resize weight of 1.0 and all others
	    // a weight of 0.0. The first button will get all extra space.
	    c.gridx = 0;
	    c.gridy = 0;
	    c.gridwidth = 1;
	    c.gridheight =1;
	    c.weightx = c.weighty = 0;
	    this.add(new JLabel(Lang.getInstance().translate("Address for ") + person.getName() +":"), c);
	    

	    c.gridx = 1;
	    c.gridy = 0;
	    c.gridwidth = 4;
	    c.gridheight = 1;
	    c.weightx = c.weighty = 0;
	    this.add(addressTxt, c);

	    c.gridx = 0;
	    c.gridy = 1;
	    c.gridwidth = 1;
	    c.gridheight = 1;
	    this.add(new JLabel(Lang.getInstance().translate("To do") +":"), c);

	    c.gridx = 1;
	    c.gridy = 1;
	    c.gridwidth = 4;
	    c.gridheight = 1;
	    this.add(toDate, c);

	    c.gridx = 2;
	    c.gridy = 2;
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
	    c.gridy = 2;
	    c.gridwidth = 1;
	    c.gridheight = 1;
	    JButton Button_Confirm = new JButton(Lang.getInstance().translate("Confirm"));
	    this.add(Button_Confirm, c);
	    Button_Confirm.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	onGoClick(person, Button_Confirm, addressTxt, toDate);
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
	    setPreferredSize(new Dimension(400, 200));
		//PACK
//		this.pack();
		//this.setSize(500, this.getHeight());
//		this.setResizable(true);
		this.setLocationRelativeTo(null);
	//	MainFrame.this.add(comp, constraints);//.setFocusable(false);
		this.setVisible(true);
	}
	
	public void onGoClick(PersonCls person, JButton Button_Confirm,
			JTextField pubKey1Txt, JTextField pubKey12Txt, JTextField pubKey13Txt, JTextField toDate)
	{

    	if (!OnDealClick.proccess1(Button_Confirm)) return;

    	// программа обработки при нажатии confirm
    	String address = addressTxt.getText();
    	int toDateVol = Integer.parseInt(toDate.getText());
    	int feePow = 0;
    	PublicKeyAccount userAccount1;
    	PublicKeyAccount userAccount2;
    	PublicKeyAccount userAccount3;
    	
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
