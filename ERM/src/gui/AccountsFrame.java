package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JToolBar;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import controller.Controller;
import core.item.assets.AssetCls;
import lang.Lang;
import utils.NumberAsString;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;

import gui.AccountsPanel;
//import gui.items.persons.IssuePersonFrame;
//import gui.items.persons.MyPersonsPanel;
//import gui.items.persons.PersonFrame;
import javax.swing.JTextField;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.BoxLayout;

public class AccountsFrame extends JInternalFrame{


	/**
	 * 
	 */
	//private static final long serialVersionUID = 1L;
	private static final long serialVersionUID = 2717571093561259483L;


	public AccountsFrame (JFrame parent)
	{
	
		// tool bar
		JToolBar tb2 = new JToolBar(Lang.getInstance().translate("Toolbar accounts"));
	
		JButton issueButton = new JButton(Lang.getInstance().translate("New Account"));
				tb2.add(issueButton);
				issueButton.addActionListener(new ActionListener()
				{
				    public void actionPerformed(ActionEvent e)
				    {
				    	onNewClick();
				    }
	
					
				});	
				
		getContentPane().add(tb2, BorderLayout.NORTH);
		
	    AccountsPanel accountsPanel = new AccountsPanel();
        getContentPane().add(accountsPanel, BorderLayout.CENTER);
		accountsPanel.setSelectionFavoriteItem();  

	 
	    //JTabbedPane main_jTabbedPane = new JTabbedPane();
        //main_jTabbedPane.addTab(Lang.getInstance().translate("Search account"), null, allPersonsFrame, "");
        //main_jTabbedPane.addTab(Lang.getInstance().translate("My Persons"), null, my_person_panel, "");
        //main_jTabbedPane.getAccessibleContext().setAccessibleName("");
        //main_jTabbedPane.getAccessibleContext().setAccessibleDescription("");
        
        //getContentPane().add(main_jTabbedPane, BorderLayout.CENTER);
        
       //SHOW FRAME
        this.pack();
        this.setMaximizable(true);
		this.setTitle(Lang.getInstance().translate("Accounts"));
		this.setClosable(true);
		this.setResizable(true);
		this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
		this.setLocation(20, 20);
	//	this.setIconImages(icons);
		//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        this.setResizable(true);
//        splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
        this.setVisible(true);
	
	}

	public void onNewClick()
	{
		//CHECK IF WALLET UNLOCKED
		if(!Controller.getInstance().isWalletUnlocked())
		{
			//ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(); 
			if(!Controller.getInstance().unlockWallet(password))
			{
				//WRONG PASSWORD
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		//GENERATE NEW ACCOUNT
		Controller.getInstance().generateNewAccount();
	}

}