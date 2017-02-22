package gui.naming;

import gui.PasswordPane;
import gui.models.NameComboBoxModel;
import gui.transaction.OnDealClick;
import lang.Lang;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import controller.Controller;
import core.account.PrivateKeyAccount;
import core.item.assets.AssetCls;
import core.naming.Name;
import core.transaction.Transaction;
//import settings.Settings;
import utils.Pair;

@SuppressWarnings("serial")
public class SellNameFrame extends JFrame
{
	static Logger LOGGER = Logger.getLogger(SellNameFrame.class.getName());

	private JComboBox<Name> cbxName;
	private JTextField txtOwner;
	private JTextField txtPrice;
	private JTextField txtFeePow;
	private JButton sellButton;
	
	public SellNameFrame(Name name)
	{
		super(Lang.getInstance().translate("ARONICLE.world") + " - " + Lang.getInstance().translate("Sell Name"));
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
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
		
		//LABEL NAME
      	labelGBC.gridy = 1;
      	JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
      	this.add(nameLabel, labelGBC);
      		
      	//TXT NAME
      	txtGBC.gridy = 1;
      	this.cbxName = new JComboBox<Name>(new NameComboBoxModel());
      	this.cbxName.addItemListener(new ItemListener()
      	{
      		@Override
      	    public void itemStateChanged(ItemEvent event) 
      		{
      			if (event.getStateChange() == ItemEvent.SELECTED) 
      			{
      				Name name = (Name) event.getItem();
      	 
      				txtOwner.setText(name.getOwner().getPersonAsString());
      			}
      	    }    
      	});
        this.add(this.cbxName, txtGBC);
        
        //LABEL OWNER
      	labelGBC.gridy = 2;
      	JLabel ownerLabel = new JLabel(Lang.getInstance().translate("Owner") + ":");
      	this.add(ownerLabel, labelGBC);
      		
      	//TXT OWNER
      	txtGBC.gridy = 2;
      	this.txtOwner = new JTextField();
      	this.txtOwner.setEditable(false);
      	this.add(this.txtOwner, txtGBC);
        
      	//LABEL PRICE
      	labelGBC.gridy = 3;
      	JLabel priceLabel = new JLabel(Lang.getInstance().translate("Price") + ":");
      	this.add(priceLabel, labelGBC);
      	
      	//TXT PRICE
      	txtGBC.gridy = 3;
      	this.txtPrice = new JTextField();
      	this.txtPrice.setText("1");
      	this.add(this.txtPrice, txtGBC);
      	
        //LABEL FEE
      	labelGBC.gridy = 4;
      	JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee") + ":");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT FEE
      	txtGBC.gridy = 4;
      	txtFeePow = new JTextField();
      	this.txtFeePow.setText("1");
        this.add(txtFeePow, txtGBC);
		           
        //BUTTON SELL
        buttonGBC.gridy = 5;
        sellButton = new JButton(Lang.getInstance().translate("Sell"));
        sellButton.setPreferredSize(new Dimension(80, 25));
        sellButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onSellClick();
		    }
		});
    	this.add(sellButton, buttonGBC);
        
    	//SET DEFAULT SELECTED ITEM
    	if(this.cbxName.getItemCount() > 0)
    	{
    		this.cbxName.setSelectedItem(name);
			this.txtOwner.setText(name.getOwner().getPersonAsString());
    	}
    	
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
	
	public void onSellClick()
	{
		//DISABLE
		this.sellButton.setEnabled(false);
		
		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, Lang.getInstance().translate("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.sellButton.setEnabled(true);
			
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
				this.sellButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ NAME
		Name name = (Name) this.cbxName.getSelectedItem();
		
		int parsing = 0;
		try
		{
			//READ PRICE
			parsing = 1;
			BigDecimal price= new BigDecimal(txtPrice.getText()).setScale(8);
						
			//READ FEE
			parsing = 2;
			int fee = Integer.parseInt(txtFeePow.getText());
			
			//CREATE NAME UPDATE
			PrivateKeyAccount owner = Controller.getInstance().getPrivateKeyAccountByAddress(name.getOwner().getAddress());
			Pair<Transaction, Integer> result = Controller.getInstance().sellName(owner, name.getName(), price, fee);
			
			//CHECK VALIDATE MESSAGE
			if (result.getB() == Transaction.VALIDATE_OK) {
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Name sale has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
				this.dispose();
			}
			else {
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result.getB())), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage(),e);
			
			//CHECK WHERE PARSING ERROR HAPPENED
			switch(parsing)
			{
			case 1:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid price!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
				
			case 2:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		
		//ENABLE
		this.sellButton.setEnabled(true);
	}
}
