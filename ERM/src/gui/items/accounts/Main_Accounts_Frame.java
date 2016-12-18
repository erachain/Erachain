package gui.items.accounts;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import controller.Controller;
import core.account.Account;
import core.item.assets.AssetCls;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.PasswordPane;
import lang.Lang;
// фрейм в котором работаем с адресами
public class Main_Accounts_Frame extends Main_Internal_Frame {
	
	My_Accounts_SplitPanel my_Accounts_SplitPanel;
	Search_Accounts_SplitPanel search_Accounts;
	
	public Main_Accounts_Frame(){
		
	
		this.setTitle(Lang.getInstance().translate("Accounts"));
		this.jButton2_jToolBar.setVisible(false);
		this.jButton3_jToolBar.setVisible(false);
		this.jButton1_jToolBar.setText(Lang.getInstance().translate("Create")+" "+Lang.getInstance().translate("New Account"));
		
		
		
		this.jButton1_jToolBar.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	onNewClick();
		    }

			
		});	
		
		
		//this.jToolBar.setFloatable(true);
		this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with Accounts"));
	// MY Accounts
		my_Accounts_SplitPanel = new My_Accounts_SplitPanel();
		this.jTabbedPane.add(my_Accounts_SplitPanel);
		
	// Search Accounts
		search_Accounts = new Search_Accounts_SplitPanel();
		this.jTabbedPane.add(search_Accounts);
	
	
	
	
	this.pack();
	//	this.setSize(800,600);
	this.setMaximizable(true);
	
	this.setClosable(true);
	this.setResizable(true);
	this.setLocation(20, 20);
	//CLOSE
	setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
    this.setResizable(true);
    this.setVisible(true);
    Dimension size = MainFrame.desktopPane.getSize();
    this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
    my_Accounts_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
    search_Accounts.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
	
	
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
