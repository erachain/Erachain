package gui.items.persons;

import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableColumn;

import org.mapdb.Fun.Tuple3;

import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import database.DBSet;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import gui.library.MTable;
import gui.models.PersonAccountsModel;
import gui.models.Renderer_Left;
import lang.Lang;
import utils.TableMenuPopupUtil;

public class Person_Owner_Panel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable jTable_My_Persons;
	private JScrollPane jScrollPane_Tab_My_Persons;
	private GridBagConstraints gridBagConstraints;
	TableModelOwnerPersons person_Accounts_Model;

	@SuppressWarnings("rawtypes")
	public  Person_Owner_Panel(PersonCls person) {

		this.setName(Lang.getInstance().translate("Created person"));
		
		person_Accounts_Model = new TableModelOwnerPersons(person.getKey());
		jTable_My_Persons = new MTable(person_Accounts_Model);
	
		jScrollPane_Tab_My_Persons = new JScrollPane();
		jScrollPane_Tab_My_Persons.setViewportView(jTable_My_Persons);
		this.setLayout(new java.awt.GridBagLayout());

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		this.add(jScrollPane_Tab_My_Persons, gridBagConstraints);

		JPopupMenu menu = new JPopupMenu();

		JMenuItem copyKey = new JMenuItem(Lang.getInstance().translate("Copy Key"));
		copyKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = jTable_My_Persons.getSelectedRow();
				row = jTable_My_Persons.convertRowIndexToModel(row);

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				
				
				Object a = person_Accounts_Model.getValueAt(row, person_Accounts_Model.COLUMN_KEY).toString();
				StringSelection value = new StringSelection(person_Accounts_Model.getValueAt(row, person_Accounts_Model.COLUMN_KEY).toString());
				clipboard.setContents(value, null);
			}
		});
		menu.add(copyKey);

		JMenuItem menu_copyName = new JMenuItem(Lang.getInstance().translate("Copy Name"));
		menu_copyName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				// StringSelection value = new
				// StringSelection(person.getCreator().getAddress().toString());
				int row = jTable_My_Persons.getSelectedRow();
				row = jTable_My_Persons.convertRowIndexToModel(row);

	
				@SuppressWarnings("static-access")
				StringSelection value = new StringSelection((String) person_Accounts_Model.getValueAt(row, person_Accounts_Model.COLUMN_NAME));
				clipboard.setContents(value, null);
				
			}
		});
		menu.add(menu_copyName);

		
	
		
	

		JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.getInstance().translate("Send Asset"));
		Send_Coins_item_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int row = jTable_My_Persons.getSelectedRow();
				row = jTable_My_Persons.convertRowIndexToModel(row);
				new Account_Send_Dialog(null, null,null, person_Accounts_Model.getPerson(row));

			}
		});
		menu.add(Send_Coins_item_Menu);

		JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.getInstance().translate("Send Mail"));
		Send_Mail_item_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int row = jTable_My_Persons.getSelectedRow();
				row = jTable_My_Persons.convertRowIndexToModel(row);
	//			Account account = person_Accounts_Model.getAccount(row);

				new Mail_Send_Dialog(null, null, null, person_Accounts_Model.getPerson(row));

			}
		});
		menu.add(Send_Mail_item_Menu);

		////////////////////
		TableMenuPopupUtil.installContextMenu(jTable_My_Persons, menu); // SELECT
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
