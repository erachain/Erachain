package gui.library;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.GenesisBlock;
import core.item.assets.AssetCls;
import datachain.DCSet;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import lang.Lang;

public class HyperLinkAccount  {

private Account account;
private String text;
private JPopupMenu account_Menu;

public  HyperLinkAccount(Account account){
	this.account = account;
	set_account(account);
}

public void set_account(Account account){
	if(account.isPerson()){
		this.text = account.getPersonAsString();
	}
	else if (GenesisBlock.CREATOR.equals(account)) this.text =("GENESIS");
	else
	{
		this.text=(account.getAddress());
	}

	// menu 
	account_Menu = new JPopupMenu();
	JMenuItem copy_Creator_Address1 = new JMenuItem(Lang.getInstance().translate("Copy Account"));
	copy_Creator_Address1.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection value = new StringSelection(account.getAddress());
			clipboard.setContents(value, null);
		}
	});
	account_Menu.add(copy_Creator_Address1);

	PublicKeyAccount public_Account;
	if (account instanceof PublicKeyAccount) {
		public_Account = (PublicKeyAccount)account;
	} else {
		byte[] publick_Key = Controller.getInstance().getPublicKeyByAddress(account.getAddress());
		public_Account = publick_Key==null?null:new PublicKeyAccount(publick_Key);
	}

	JMenuItem copyPublicKey;
	if (public_Account == null) {
		copyPublicKey = new JMenuItem(Lang.getInstance().translate("Public Key not Found"));
		copyPublicKey.setEnabled(false);
	} else {
		copyPublicKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key"));
		copyPublicKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				// StringSelection value = new
				// StringSelection(person.getCreator().getAddress().toString());
				StringSelection value = new StringSelection(public_Account.getBase58());
				clipboard.setContents(value, null);
			}
		});			
	}
	account_Menu.add(copyPublicKey);

	JMenuItem Send_Coins_Crator = new JMenuItem(Lang.getInstance().translate("Send Asset"));
	Send_Coins_Crator.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			new Account_Send_Dialog(null, null, account, null).show();
		}
	});
	account_Menu.add(Send_Coins_Crator);

	JMenuItem Send_Mail_Creator = new JMenuItem(Lang.getInstance().translate("Send Mail"));
	Send_Mail_Creator.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

			new Mail_Send_Dialog(null, null, account, null);
		}
	});
	account_Menu.add(Send_Mail_Creator);
//	this.setComponentPopupMenu(account_Menu);
		
}
	public String get_Text(){
		return this.text;
	}
	public JPopupMenu get_PopupMenu(){
		
		return this.account_Menu;
	}
	
}