package gui.library;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.GenesisBlock;
import database.DBSet;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import lang.Lang;

public class M_Accoutn_Text_Field  extends JTextField{
	public Account account;
		
	public M_Accoutn_Text_Field() {
		super();
		
	}
	
	public M_Accoutn_Text_Field(Account account){
		super();
		this.account = account;
		set_account(account);	
	}
	
	public void set_account(Account account){
		if(account.isPerson(DBSet.getInstance())){
			this.setText( account.viewPerson());
		}
		else if (GenesisBlock.CREATOR.equals(account)) this.setText("GENESIS");
		else
		{
			this.setText(account.getAddress());
		}
	
		// menu 
		JPopupMenu creator_Meny = new JPopupMenu();
		JMenuItem copy_Creator_Address1 = new JMenuItem(Lang.getInstance().translate("Copy Address"));
		copy_Creator_Address1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getAddress());
				clipboard.setContents(value, null);
			}
		});
		creator_Meny.add(copy_Creator_Address1);
	
		JMenuItem copyPublicKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key"));
		copyPublicKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				// StringSelection value = new
				// StringSelection(person.getCreator().getAddress().toString());
				byte[] publick_Key = Controller.getInstance().getPublicKeyByAddress(account.getAddress());
				PublicKeyAccount public_Account = new PublicKeyAccount(publick_Key);
				StringSelection value = new StringSelection(public_Account.getBase58());
				clipboard.setContents(value, null);
			}
		});
		creator_Meny.add(copyPublicKey);
	
		JMenuItem Send_Coins_Crator = new JMenuItem(Lang.getInstance().translate("Send Coins"));
		Send_Coins_Crator.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Account_Send_Dialog(null, null, account, null);
			}
		});
		creator_Meny.add(Send_Coins_Crator);
	
		JMenuItem Send_Mail_Creator = new JMenuItem(Lang.getInstance().translate("Send Mail"));
		Send_Mail_Creator.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	
				new Mail_Send_Dialog(null, null, account, null);
			}
		});
		creator_Meny.add(Send_Mail_Creator);
		this.setComponentPopupMenu(creator_Meny);
			
	}
	
}
