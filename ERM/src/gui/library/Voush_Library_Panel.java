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
import javax.swing.table.TableColumnModel;

import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.transaction.Transaction;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import gui.items.statement.Statements_Vouch_Table_Model;
import gui.models.PersonStatusesModel;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import lang.Lang;
import utils.TableMenuPopupUtil;

public class Voush_Library_Panel extends JPanel {

	/**
	 * view VOUSH PANEL
	 */
	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane_Tab_Vouches;
	private GridBagConstraints gridBagConstraints;

	public Voush_Library_Panel(Transaction transaction) {

		this.setName(Lang.getInstance().translate("Certified"));
		Statements_Vouch_Table_Model model = new Statements_Vouch_Table_Model(transaction);
		JTable jTable_Vouches = new MTable(model);
		TableColumnModel column_mod = jTable_Vouches.getColumnModel();
		TableColumn col_data = column_mod.getColumn(model.COLUMN_TIMESTAMP);
		col_data.setMinWidth(50);
		col_data.setMaxWidth(200);
		col_data.setPreferredWidth(120);// .setWidth(30);

		jTable_Vouches.setDefaultRenderer(Account.class, new Renderer_Right());
		jTable_Vouches.setDefaultRenderer(String.class, new Renderer_Left(
		jTable_Vouches.getFontMetrics(jTable_Vouches.getFont()), model.get_Column_AutoHeight())); // set renderer

		TableColumn Date_Column = jTable_Vouches.getColumnModel().getColumn( model.COLUMN_TIMESTAMP);	
   		//favoriteColumn.setCellRenderer(new Renderer_Boolean()); //personsTable.getDefaultRenderer(Boolean.class));
   		int rr = (int) (getFontMetrics( UIManager.getFont("Table.font")).stringWidth("0022-22-2222"));	
   		Date_Column.setMinWidth(rr+1);
   		Date_Column.setMaxWidth(rr*10);
   		Date_Column.setPreferredWidth(rr+5);//.setWidth(30);
   		
		
		// jPanel_Tab_Vouch = new javax.swing.JPanel();
		jScrollPane_Tab_Vouches = new javax.swing.JScrollPane();

		this.setLayout(new java.awt.GridBagLayout());

		jScrollPane_Tab_Vouches.setViewportView(jTable_Vouches);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		this.add(jScrollPane_Tab_Vouches, gridBagConstraints);
		
		

		JPopupMenu menu = new JPopupMenu();

		JMenuItem copy_Creator_Address = new JMenuItem(Lang.getInstance().translate("Copy Creator Address"));
		copy_Creator_Address.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = jTable_Vouches.getSelectedRow();
				row = jTable_Vouches.convertRowIndexToModel(row);

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection( ((Account)jTable_Vouches.getValueAt(row, model.COLUMN_CREATOR)).getAddress());
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
				int row = jTable_Vouches.getSelectedRow();
				row = jTable_Vouches.convertRowIndexToModel(row);

				byte[] publick_Key = Controller.getInstance()
						.getPublicKeyByAddress(((Account)jTable_Vouches.getValueAt(row, model.COLUMN_CREATOR)).getAddress());
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
				int row = jTable_Vouches.getSelectedRow();
				row = jTable_Vouches.convertRowIndexToModel(row);

				
				StringSelection value = new StringSelection(model.get_No_Trancaction(row));
				clipboard.setContents(value, null);
			}
		});
		menu.add(menu_copy_Block_PublicKey);

		
		
		
		JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.getInstance().translate("Send Coins to Creator"));
		Send_Coins_item_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int row = jTable_Vouches.getSelectedRow();
				row = jTable_Vouches.convertRowIndexToModel(row);
				Account account = (Account)jTable_Vouches.getValueAt(row, model.COLUMN_CREATOR);

				new Account_Send_Dialog(null, null, account, null);

			}
		});
		menu.add(Send_Coins_item_Menu);

		JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.getInstance().translate("Send Mail to Creator"));
		Send_Mail_item_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int row = jTable_Vouches.getSelectedRow();
				row = jTable_Vouches.convertRowIndexToModel(row);
				Account account = (Account)jTable_Vouches.getValueAt(row, model.COLUMN_CREATOR);

			new Mail_Send_Dialog(null, null, account, null);

			}
		});
		menu.add(Send_Mail_item_Menu);

		
		
		
		////////////////////
		TableMenuPopupUtil.installContextMenu(jTable_Vouches, menu); // SELECT

		

	}

}
