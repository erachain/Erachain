package gui.items.mails;

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
public class Mails_Main_Frame extends Main_Internal_Frame {
	
	Incoming_Mails_Panel incoming_Mails_Panel;
	Sent_Mails_Panel sent_Mails_Panel;
	
	public Mails_Main_Frame(){
		
	
		this.setTitle(Lang.getInstance().translate("Mails"));
		this.jButton2_jToolBar.setVisible(false);
		this.jButton3_jToolBar.setVisible(false);
		this.jButton1_jToolBar.setText(Lang.getInstance().translate("Create")+" "+Lang.getInstance().translate("New Mails"));
		
		
		
		this.jButton1_jToolBar.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	onNewClick();
		    }

			
		});	
		
		
		//this.jToolBar.setFloatable(true);
		this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with Mails"));
	// MY Accounts
		 incoming_Mails_Panel = new Incoming_Mails_Panel();
		this.jTabbedPane.add(incoming_Mails_Panel);
		
	// Search Accounts
		sent_Mails_Panel = new Sent_Mails_Panel();
		this.jTabbedPane.add(sent_Mails_Panel);
	
	
	
	
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
