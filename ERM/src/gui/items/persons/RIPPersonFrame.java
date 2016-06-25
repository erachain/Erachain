package gui.items.persons;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.mapdb.Fun.Tuple5;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.item.ItemCls;
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import core.transaction.Transaction;
import database.DBSet;
import gui.models.AccountsComboBoxModel;
import gui.transaction.OnDealClick;
import lang.Lang;
import ntp.NTP;
import utils.Pair;

//public class PersonConfirm extends JDialog { // JInternalFrame  {
public class RIPPersonFrame extends JInternalFrame  {

	/**
	 * 
	 */
	//private static final long serialVersionUID = 1L;
	private static final long serialVersionUID = 2717571093561259483L;

	private JComboBox<Account> accountLBox;
	static PersonCls person;

	public RIPPersonFrame(JFrame parent) {
		super(Lang.getInstance().translate("R.I.P Person"));
	

		final JTextField personKeyTxt = new JTextField();
		final JLabel personDetails = new JLabel();

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

		//LABEL FROM
		++label.gridy;
		this.add(new JLabel(Lang.getInstance().translate("Account") + ":"), label);
		
		//COMBOBOX FROM
		++input.gridy;
		this.accountLBox = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.accountLBox, input);
	    
		++label.gridy;
		this.add(new JLabel(Lang.getInstance().translate("Person Key") + ":"), label);

		++input.gridy;
	    this.add(personKeyTxt, input);
	    
	    
	    personKeyTxt.getDocument().addDocumentListener(new DocumentListener() {
            
			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				RIPPersonFrame.person = refreshReceiverDetails(personKeyTxt, personDetails);
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				RIPPersonFrame.person = refreshReceiverDetails(personKeyTxt, personDetails);
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
        personDetails.setText(text);
        htmlPanel.add(personDetails, detail);      

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
		    	onGoClick(Button_Confirm, feePow, person, endDate);
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
	
	private PersonCls refreshReceiverDetails(JTextField pubKeyTxt, JLabel pubKeyDetails)
	{
		String toValue = pubKeyTxt.getText();
		
		if(toValue.isEmpty())
		{
			pubKeyDetails.setText("");
			return null;
		}
		
		long personKey = Long.parseLong(pubKeyTxt.getText());
		
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			pubKeyDetails.setText(Lang.getInstance().translate("Status must be OK to show public key details."));
			return null;
		}
		
		//CHECK IF RECIPIENT IS VALID ADDRESS
		PersonCls person = Controller.getInstance().getItemPerson(personKey);

		if (person == null) {
			// SHOW error message
			pubKeyDetails.setText(OnDealClick.resultMess(Transaction.ITEM_PERSON_NOT_EXIST));
		} else {
			// SHOW account for FEE asset
			String personDetails = person.toString() + "<br>";
			personDetails += person.getSkinColor() + ":" + person.getEyeColor() + ":" + person.getHairСolor() + "<br>";
			personDetails += person.getHeight() + ":" + person.getBirthLatitude() + ":" + person.getBirthLongitude() + "<br>";

			// IF PERSON DEAD
			Tuple5<Long, Long, byte[], Integer, Integer> deadDay = DBSet.getInstance().getPersonStatusMap().getItem(person.getKey(), StatusCls.DEAD_KEY);
			if (deadDay != null)
			{
				if (false & deadDay.b == Long.MIN_VALUE)
					personDetails += "<br>Dead";
				else {
					long current_time = NTP.getTime();
					int daysLeft = (int)((deadDay.a - current_time) / 86400000);
					//personDetails += "<br>" + Lang.getInstance().translate("Date of death %days%").replace("%days%", ""+daysLeft);
					personDetails += "<br>" + Lang.getInstance().translate("Died %days% days ago").replace("%days%", ""+daysLeft);
				}
			} else {
				// IF PERSON ALIVE
				Tuple5<Long, Long, byte[], Integer, Integer> aliveDay = DBSet.getInstance().getPersonStatusMap().getItem(person.getKey(), StatusCls.ALIVE_KEY);
				if (aliveDay == null)
				{} else {
					if (aliveDay.b == null || aliveDay.b == Long.MAX_VALUE)
						personDetails += "<br>Alive";
					else {
						long current_time = NTP.getTime();
						int daysLeft = (int)((aliveDay.a - current_time) / 86400000);
						if (daysLeft < 0 ) personDetails += "<br>" + Lang.getInstance().translate("Person died %days% ago days ago").replace("%days%", ""+daysLeft);
						else personDetails += "<br>" + Lang.getInstance().translate("Person is still alive %days%").replace("%days%", ""+daysLeft);
					}
				}
			}
			
			pubKeyDetails.setText("<html>" + personDetails + "</html>");
			
		}
		
		return person;
	}

	public void onGoClick(JButton Button_Confirm, JTextField feePowTxt,
			PersonCls person, JTextField toDateTxt)
	{

    	if (!OnDealClick.proccess1(Button_Confirm)) return;

		Account creator = (Account) this.accountLBox.getSelectedItem();
    	//String address = pubKey1Txt.getText();
    	//Long begDate = null;
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
    	
		Pair<Integer, Long> endDateRes = ItemCls.resolveDateFromStr(toDateStr, NTP.getTime());
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
				feePow, StatusCls.DEAD_KEY, person, endDate, Long.MAX_VALUE);
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
