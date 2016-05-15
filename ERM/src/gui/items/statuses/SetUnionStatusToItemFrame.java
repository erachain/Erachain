package gui.items.statuses;

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
import core.item.ItemCls;
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import core.item.unions.UnionCls;
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

// set union Status to Item (Unit)
// for example: set status DIRECTOR of union MY_FIRM to person ERMOLAEV
public class SetUnionStatusToItemFrame extends JInternalFrame  {

	private JComboBox<Account> accountLBox;
	static StatusCls status;
	static UnionCls union;
	static ItemCls item;

	public SetUnionStatusToItemFrame(JFrame parent) {
		super(Lang.getInstance().translate("Union Status to Item (Unit)"));
	

		final JTextField statusKeyTxt = new JTextField();
		final JLabel statusDetails = new JLabel();

		final JTextField unionKeyTxt = new JTextField();
		final JLabel unionDetails = new JLabel();

		final JTextField itemKeyTxt = new JTextField();
		final JLabel itemDetails = new JLabel();

		final JTextField endDate = new JTextField(".");
		final JTextField feePow = new JTextField("0");
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(10, 15, 15, 10));

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

		//LABEL SIGNER
		++label.gridy;
		this.add(new JLabel(Lang.getInstance().translate("Account") + ":"), label);
		
		//COMBOBOX SIGNER
		++input.gridy;
		this.accountLBox = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.accountLBox, input);
	    
		++label.gridy;
		this.add(new JLabel(Lang.getInstance().translate("Status Key") + ":"), label);

		++input.gridy;
	    this.add(statusKeyTxt, input);
	    
	    statusKeyTxt.getDocument().addDocumentListener(new DocumentListener() {
            
			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				SetUnionStatusToItemFrame.status = (StatusCls)refreshStatusDetails(statusKeyTxt, statusDetails);
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				SetUnionStatusToItemFrame.status = (StatusCls)refreshStatusDetails(statusKeyTxt, statusDetails);
			}
        });

	    ++input.gridy;
	    ++label.gridy;
	    JPanel htmlPanel = new JPanel();
        htmlPanel.setBorder(BorderFactory.createTitledBorder(Lang.getInstance().translate("Details")));
         
        String text = "";         
        //font = new Font(null, Font.PLAIN, 10);
        
	    GridBagConstraints detail = new GridBagConstraints();
	    detail.insets = new Insets(0, 5, 5, 0);
	    detail.fill = GridBagConstraints.BOTH; // components grow in both dimensions
	    detail.anchor = GridBagConstraints.NORTHWEST;
	    detail.gridx = 0;
	    detail.gridy = 0;
	    detail.gridwidth = 5;
	    detail.gridheight = 3;
	    detail.weightx = -5;
	    detail.weighty = -2;
        itemDetails.setText(text);
        htmlPanel.add(itemDetails, detail);      

	    input.gridx = 0;
	    input.gridwidth = 5;
	    input.gridheight = 2;
        this.add(htmlPanel, input); // BorderLayout.SOUTH);

	    ++label.gridy;
		this.add(new JLabel(Lang.getInstance().translate("Union Key") + ":"), label);

		++input.gridy;
	    this.add(unionKeyTxt, input);
	    
	    
	    unionKeyTxt.getDocument().addDocumentListener(new DocumentListener() {
            
			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				SetUnionStatusToItemFrame.union = refreshUnionDetails(unionKeyTxt, unionDetails);
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				SetUnionStatusToItemFrame.union = refreshUnionDetails(unionKeyTxt, unionDetails);
			}
        });
	          		
	    ++input.gridy;
	    ++label.gridy;
	    JPanel unionDetailsPanel = new JPanel();
	    unionDetailsPanel.setBorder(BorderFactory.createTitledBorder(Lang.getInstance().translate("Details")));

        ++label.gridy;
		this.add(new JLabel(Lang.getInstance().translate("Person Key") + ":"), label);

		++input.gridy;
	    this.add(itemKeyTxt, input);
	    
	    
	    itemKeyTxt.getDocument().addDocumentListener(new DocumentListener() {
            
			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				SetUnionStatusToItemFrame.item = refreshItemDetails(itemKeyTxt, itemDetails);
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				SetUnionStatusToItemFrame.item = refreshItemDetails(itemKeyTxt, itemDetails);
			}
        });
	          		
	    ++input.gridy;
	    ++label.gridy;
	    JPanel detailsPanel = new JPanel();
        htmlPanel.setBorder(BorderFactory.createTitledBorder(Lang.getInstance().translate("Details")));
         
        //String text = "";         
        //font = new Font(null, Font.PLAIN, 10);
        
	    //GridBagConstraints detail = new GridBagConstraints();
	    detail.insets = new Insets(0, 5, 5, 0);
	    detail.fill = GridBagConstraints.BOTH; // components grow in both dimensions
	    detail.anchor = GridBagConstraints.NORTHWEST;
	    detail.gridx = 0;
	    detail.gridy = 0;
	    detail.gridwidth = 5;
	    detail.gridheight = 3;
	    detail.weightx = -5;
	    detail.weighty = -2;
        itemDetails.setText(text);
        htmlPanel.add(itemDetails, detail);      

	    input.gridx = 0;
	    input.gridwidth = 5;
	    input.gridheight = 2;
        this.add(htmlPanel, input); // BorderLayout.SOUTH);
 
	    ++label.gridy;
	    this.add(new JLabel(Lang.getInstance().translate("Date ('.'=today)") +":"), label);

	    input.gridy = label.gridy;
	    input.gridx = 1;
	    input.gridwidth = 5;
	    input.gridheight = 1;
	    this.add(endDate, input);

	    // FEE POWER
	    ++label.gridy;
	    this.add(new JLabel(Lang.getInstance().translate("Fee Power") +":"), label);

	    ++input.gridy;
      	feePow.setText("0");
	    this.add(feePow, input);

	    // BUTTONS
	    ++input.gridy;
	    input.gridx = 1;
	    input.gridwidth = 2;
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
	    input.gridwidth = 2;
	    input.gridheight = 1;
	    JButton Button_Confirm = new JButton(Lang.getInstance().translate("Confirm"));
	    this.add(Button_Confirm, input);
	    Button_Confirm.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	onGoClick(Button_Confirm, feePow, item, endDate);
		    }
		});
	    
       //SHOW FRAME
        this.pack();
        this.setMaximizable(true);
		//this.setTitle(Lang.getInstance().translate("Persons"));
	    //setPreferredSize(new Dimension(500, 600));
	    this.setSize(new Dimension(500, 300));
		this.setClosable(true);
		this.setResizable(true);
		//this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
		this.setLocation(30, 20);

		//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        this.setResizable(true);
        this.setVisible(true);
	}
	
	private StatusCls refreshStatusDetails(JTextField fieldTxt, JLabel itemDetails)
	{
		String toValue = fieldTxt.getText();
		
		if(toValue.isEmpty())
		{
			itemDetails.setText("");
			return null;
		}
		
		long key = Long.parseLong(fieldTxt.getText());
		
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			itemDetails.setText(Lang.getInstance().translate("Status must be OK to show public key details."));
			return null;
		}
		
		//CHECK IF ITEM IS VALID ADDRESS
		StatusCls status = Controller.getInstance().getItemStatus(key);

		if (status == null) {
			// SHOW error message
			itemDetails.setText(OnDealClick.resultMess(Transaction.ITEM_STATUS_NOT_EXIST));
		} else {
			// SHOW account for FEE asset
			String statusDetails = status.toString() + "<br>";
			itemDetails.setText("<html>" + statusDetails + "</html>");
			
		}
		
		return status;
	}

	private UnionCls refreshUnionDetails(JTextField fieldTxt, JLabel itemDetails)
	{
		String toValue = fieldTxt.getText();
		
		if(toValue.isEmpty())
		{
			itemDetails.setText("");
			return null;
		}
		
		long key = Long.parseLong(fieldTxt.getText());
		
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			itemDetails.setText(Lang.getInstance().translate("Status must be OK to show public key details."));
			return null;
		}
		
		//CHECK IF ITEM IS VALID ADDRESS
		UnionCls union = Controller.getInstance().getItemUnion(key);

		if (union == null) {
			// SHOW error message
			itemDetails.setText(OnDealClick.resultMess(Transaction.ITEM_UNION_NOT_EXIST));
		} else {
			// SHOW account for FEE asset
			String statusDetails = union.toString() + "<br>";
			itemDetails.setText("<html>" + statusDetails + "</html>");
			
		}
		
		return union;
	}

	private PersonCls refreshItemDetails(JTextField itemTxt, JLabel itemDetails)
	{
		String toValue = itemTxt.getText();
		
		if(toValue.isEmpty())
		{
			itemDetails.setText("");
			return null;
		}
		
		long personKey = Long.parseLong(itemTxt.getText());
		
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			itemDetails.setText(Lang.getInstance().translate("Status must be OK to show public key details."));
			return null;
		}
		
		//CHECK IF RECIPIENT IS VALID ADDRESS
		PersonCls person = Controller.getInstance().getItemPerson(personKey);

		if (person == null) {
			// SHOW error message
			itemDetails.setText(OnDealClick.resultMess(Transaction.ITEM_DOES_NOT_EXIST));
		} else {
			// SHOW account for FEE asset
			String personDetails = item.toString() + "<br>";
			
			itemDetails.setText("<html>" + personDetails + "</html>");
			
		}
		
		return person;
	}

	public void onGoClick(JButton Button_Confirm, JTextField feePowTxt,
			ItemCls item, JTextField toDateTxt)
	{

    	if (!OnDealClick.proccess1(Button_Confirm)) return;

		Account creator = (Account) this.accountLBox.getSelectedItem();
    	//String address = pubKey1Txt.getText();
    	Long endDate = null;
    	int feePow = 0;
    	int parse = 0;
    	String toDateStr = toDateTxt.getText();
		try {

			//READ FEE POW
			feePow = Integer.parseInt(feePowTxt.getText());
			
		}
		catch(Exception e)
		{
			if(parse == 0)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
		}
    	
		Pair<Integer, Long> endDateRes = ItemCls.resolveEndDateFromStr(toDateStr, NTP.getTime());
		if (endDateRes.getA() == -1)
		{
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid Date value"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			Button_Confirm.setEnabled(true);
			return;
		}
		else
			endDate = endDateRes.getB();

		//Account authenticator =  new Account(address);
		PrivateKeyAccount authenticator = Controller.getInstance().getPrivateKeyAccountByAddress(creator.getAddress());

		int version = 0; // without user signs
		
		//Pair<Transaction, Integer> result = new Pair<Transaction, Integer>(null, 52);
		Pair<Transaction, Integer> result = Controller.getInstance().r_SetStatusToItem(version, false, authenticator,
				feePow, StatusCls.DEAD_KEY, item, endDate);
		//CHECK VALIDATE MESSAGE
		if (result.getB() == Transaction.VALIDATE_OK) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Person listed as dead"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
			this.dispose();
		} else {
		
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result.getB())), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
		}
		
		//ENABLE
		Button_Confirm.setEnabled(true);
		
	}
	
}
