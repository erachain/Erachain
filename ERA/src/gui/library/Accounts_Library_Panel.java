package gui.library;

import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableColumn;

import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import gui.models.PersonAccountsModel;
import gui.models.Renderer_Left;
import lang.Lang;
import utils.TableMenuPopupUtil;

public class Accounts_Library_Panel extends JPanel {

	private JTable jTable_Accounts;
	private JScrollPane jScrollPane_Tab_Accounts;
	private GridBagConstraints gridBagConstraints;
	public PersonAccountsModel person_Accounts_Model;
	private Account selected;

	public Accounts_Library_Panel(PersonCls person) {

		this.setName(Lang.getInstance().translate("Accounts"));
		
		person_Accounts_Model = new PersonAccountsModel(person.getKey());
		jTable_Accounts = new MTable(person_Accounts_Model);

	//	int row = jTable_Accounts.getSelectedRow();
	//	row = jTable_Accounts.convertRowIndexToModel(row);
	//	selected = person_Accounts_Model.getAccount(row);
		
		TableColumn to_Date_Column = jTable_Accounts.getColumnModel().getColumn(PersonAccountsModel.COLUMN_TO_DATE);
		int rr = (int) (getFontMetrics( UIManager.getFont("Table.font")).stringWidth("0022-22-2222"));	
		to_Date_Column.setMinWidth(rr+1);
		to_Date_Column.setMaxWidth(rr*10);
		to_Date_Column.setPreferredWidth(rr+5);//.setWidth(30);
		
		
		jScrollPane_Tab_Accounts = new JScrollPane();
		jScrollPane_Tab_Accounts.setViewportView(jTable_Accounts);
		this.setLayout(new java.awt.GridBagLayout());

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		this.add(jScrollPane_Tab_Accounts, gridBagConstraints);

		JPopupMenu menu = new JPopupMenu();

		JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Copy Account"));
		copyAddress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(selected.getAddress());
				clipboard.setContents(value, null);
			}
		});
		menu.add(copyAddress);

		JMenuItem menu_copyPublicKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key"));
		menu_copyPublicKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

				byte[] publick_Key = Controller.getInstance().getPublicKeyByAddress(selected.getAddress());
				PublicKeyAccount public_Account = new PublicKeyAccount(publick_Key);
				StringSelection value = new StringSelection(public_Account.getBase58());
				clipboard.setContents(value, null);
			}
		});
		menu.add(menu_copyPublicKey);

		JMenuItem menu_copyName = new JMenuItem(Lang.getInstance().translate("Copy Creator Name"));
		menu_copyName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				// StringSelection value = new
				// StringSelection(person.getCreator().getAddress().toString());
				int row = jTable_Accounts.getSelectedRow();
				row = jTable_Accounts.convertRowIndexToModel(row);
	
				@SuppressWarnings("static-access")
				StringSelection value = new StringSelection((String) person_Accounts_Model.getValueAt(row, person_Accounts_Model.COLUMN_CREATOR_NAME));
				clipboard.setContents(value, null);
				
			}
		});
		menu.add(menu_copyName);
		
		JMenuItem copy_Creator_Address = new JMenuItem(Lang.getInstance().translate("Copy Creator Account"));
		copy_Creator_Address.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = jTable_Accounts.getSelectedRow();
				row = jTable_Accounts.convertRowIndexToModel(row);

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(person_Accounts_Model.get_Creator_Account(row));
				clipboard.setContents(value, null);
			}
		});
		menu.add(copy_Creator_Address);

		JMenuItem menu_copy_Creator_PublicKey = new JMenuItem(Lang.getInstance().translate("Copy Creator Public Key"));
		menu_copy_Creator_PublicKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				// StringSelection value = new
				// StringSelection(person.getCreator().getAddress().toString());
				int row = jTable_Accounts.getSelectedRow();
				row = jTable_Accounts.convertRowIndexToModel(row);

				byte[] publick_Key = Controller.getInstance()
						.getPublicKeyByAddress(person_Accounts_Model.get_Creator_Account(row));
				PublicKeyAccount public_Account = new PublicKeyAccount(publick_Key);
				StringSelection value = new StringSelection(public_Account.getBase58());
				clipboard.setContents(value, null);
			}
		});
		menu.add(menu_copy_Creator_PublicKey);
		
		JMenuItem menu_copy_Block_PublicKey = new JMenuItem(Lang.getInstance().translate("Copy No.Transaction"));
		menu_copy_Block_PublicKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				// StringSelection value = new
				// StringSelection(person.getCreator().getAddress().toString());
				int row = jTable_Accounts.getSelectedRow();
				row = jTable_Accounts.convertRowIndexToModel(row);

				
				StringSelection value = new StringSelection(person_Accounts_Model.get_No_Trancaction(row));
				clipboard.setContents(value, null);
			}
		});
		menu.add(menu_copy_Block_PublicKey);



		JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.getInstance().translate("Send Asset"));
		Send_Coins_item_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int row = jTable_Accounts.getSelectedRow();
				row = jTable_Accounts.convertRowIndexToModel(row);
				Account account = person_Accounts_Model.getAccount(row);

				new Account_Send_Dialog(null, null, account, null);

			}
		});
		menu.add(Send_Coins_item_Menu);

		JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.getInstance().translate("Send Mail"));
		Send_Mail_item_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int row = jTable_Accounts.getSelectedRow();
				row = jTable_Accounts.convertRowIndexToModel(row);
				Account account = person_Accounts_Model.getAccount(row);

				new Mail_Send_Dialog(null, null, account, null);

			}
		});
		menu.add(Send_Mail_item_Menu);

		////////////////////
		TableMenuPopupUtil.installContextMenu(jTable_Accounts, menu); // SELECT
																		// ROW
																		// ON
																		// WHICH
																		// CLICKED
																		// RIGHT
																		// BUTTON

	}
	
	public void delay_on_close(){
		
		person_Accounts_Model.removeObservers();

	}

}
