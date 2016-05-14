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
import gui.items.statuses.ComboBoxModelItemsStatuses;
import gui.transaction.OnDealClick;
import lang.Lang;
import ntp.NTP;
import utils.NameUtils;
import utils.Pair;
import utils.NameUtils.NameResult;

//public class PersonConfirm extends JDialog { // InternalFrame  {
public class PersonSetStatusDialog extends JDialog {

	private JComboBox<Account> accountLBox;
	private JComboBox<StatusCls> statusLBox;

	public PersonSetStatusDialog(JComponent  apers, PersonCls person) {
		super();
	
this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		final JTextField pubKey1Txt = new JTextField();
		final JTextField pubKey2Txt = new JTextField();
		final JTextField pubKey3Txt = new JTextField();
		final JLabel pubKey1Details = new JLabel();
		final JLabel pubKey2Details = new JLabel();
		final JLabel pubKey3Details = new JLabel();

		final JTextField toDate = new JTextField();
		final JTextField feePow = new JTextField();
		
		
	//	this.setBorder(new EmptyBorder(10, 10, 10, 10));
		//	MainFrame mainFram = new MainFrame();
	
		setSize(400,300);
	
		this.setTitle(Lang.getInstance().translate("Set Status"));
		//	this.setClosable(true);
			this.setResizable(true);
			this.setModal(true);
		//CLOSE
		// setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
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
		JLabel accountLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
		this.add(accountLabel, label);
		
		//COMBOBOX FROM
		this.accountLBox = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.accountLBox, input);

		//LABEL STATUS
        label.gridy = ++gridy;
		JLabel statusLabel = new JLabel(Lang.getInstance().translate("Status") + ":");
		this.add(statusLabel, label);

		//COMBOBOX STATUS
		input.gridy = gridy;
		this.statusLBox =  new JComboBox<StatusCls>(new ComboBoxModelItemsStatuses());
        this.add((JComponent)this.statusLBox, input);
	          		
        input.gridx = 0;
	    input.gridy = ++gridy;
	    input.gridwidth = 5;
	    input.gridheight = 1;
	    this.add(new JLabel(Lang.getInstance().translate("To date (0 - is permanent)") +":"), input);

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
		    	dispose();	
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
		    	onGoClick(person, Button_Confirm, pubKey1Txt, pubKey1Txt, pubKey1Txt, toDate, feePow);
		    }
		});
	    
	    setPreferredSize(new Dimension(400, 400));
		//PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	    //MainFrame.this.add(comp, constraints).setFocusable(false);
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
	    if (pubKey1Txt.getText().length() == 30) {
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
