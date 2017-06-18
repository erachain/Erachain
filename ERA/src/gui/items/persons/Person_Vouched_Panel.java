package gui.items.persons;

import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.item.persons.PersonCls;
import core.transaction.R_SertifyPubKeys;
import core.transaction.Transaction;
import database.DBSet;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import gui.items.statement.Statements_Vouch_Table_Model;
import gui.library.MTable;
import gui.models.PersonStatusesModel;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import lang.Lang;
import utils.TableMenuPopupUtil;

public class Person_Vouched_Panel extends JPanel {

	/**
	 * view VOUSH PANEL
	 */
	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane_Tab_Vouches;
	private GridBagConstraints gridBagConstraints;
	Person_Vouch_From_Table_Model model;
	protected int row;

	public Person_Vouched_Panel(PersonCls person) {

		this.setName(Lang.getInstance().translate("Vouched for"));
		  model = new Person_Vouch_From_Table_Model(person);
		JTable jTable_Vouches = new MTable(model);
		TableColumnModel column_mod = jTable_Vouches.getColumnModel();
		TableColumn col_data = column_mod.getColumn(Statements_Vouch_Table_Model.COLUMN_TIMESTAMP);
		col_data.setMinWidth(50);
		col_data.setMaxWidth(200);
		col_data.setPreferredWidth(120);// .setWidth(30);


		TableColumn Date_Column = jTable_Vouches.getColumnModel().getColumn( Statements_Vouch_Table_Model.COLUMN_TIMESTAMP);	
   		//favoriteColumn.setCellRenderer(new Renderer_Boolean()); //personsTable.getDefaultRenderer(Boolean.class));
   		int rr = (int) (getFontMetrics( UIManager.getFont("Table.font")).stringWidth("0022-22-2222"));	
   		Date_Column.setMinWidth(rr+1);
   		Date_Column.setMaxWidth(rr*10);
   		Date_Column.setPreferredWidth(rr+5);//.setWidth(30);
   		
   		TableColumn height_Column = jTable_Vouches.getColumnModel().getColumn( Statements_Vouch_Table_Model.COLUMN_HEIGHT);	
   		//favoriteColumn.setCellRenderer(new Renderer_Boolean()); //personsTable.getDefaultRenderer(Boolean.class));
   		rr = (int) (getFontMetrics( UIManager.getFont("Table.font")).stringWidth("002222222222"));	
   		height_Column.setMinWidth(rr+1);
   		height_Column.setMaxWidth(rr*10);
   		height_Column.setPreferredWidth(rr+5);//.setWidth(30);
   		
   		jTable_Vouches.setAutoCreateRowSorter(true);
   		
   		TableRowSorter sorter=new TableRowSorter(model); //Создаем сортировщик
        //sorter.setSortable(0, true); //Указываем, что сортировать будем в первой колонке
        //sorter.setSortable(1, true); // а в других нет
        //sorter.setSortable(2, true);
        //ArrayList<SortKey> keys=new ArrayList<SortKey>(); // создаем коллецию ключей сортировки
        //keys.add(new SortKey(0, SortOrder.DESCENDING));  //Записываем два ключа !!! (если задать
        //keys.add(new SortKey(0, SortOrder.DESCENDING));  //один раз, то сортировщик по-умолчанию
                                                                              //DefaultRowSorter от которого происходит
                                                                              //TableRowSorter автоматически добавит
                                                                             //SortOrder.ASCENDING
        //sorter.setSortKeys(keys);                                   //Добавляем ключи к сортировщику
   		if (model.getRowCount() > 0 && sorter.isSortable(Statements_Vouch_Table_Model.COLUMN_TIMESTAMP)) {
   	        sorter.toggleSortOrder(Statements_Vouch_Table_Model.COLUMN_TIMESTAMP); //Сортируем первую колонку
   		}
        sorter.setSortsOnUpdates(true);                         //Указываем автоматически сортировать
                                                                            //при изменении модели данных
        jTable_Vouches.setRowSorter(sorter);   
   		
		
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
menu.addAncestorListener(new AncestorListener(){

			

			@Override
			public void ancestorAdded(AncestorEvent arg0) {
				// TODO Auto-generated method stub
				row = jTable_Vouches.getSelectedRow();
				if (row < 1 ) {
				menu.disable();
			}
			
			row = jTable_Vouches.convertRowIndexToModel(row);
				
				
			}

			@Override
			public void ancestorMoved(AncestorEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void ancestorRemoved(AncestorEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			
			
		});
		/*
		JMenuItem menu_copyName = new JMenuItem(Lang.getInstance().translate("Copy Creator Name"));
		menu_copyName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				// StringSelection value = new
				// StringSelection(person.getCreator().getAddress().toString());
				int row = jTable_Vouches.getSelectedRow();
				row = jTable_Vouches.convertRowIndexToModel(row);


				@SuppressWarnings("static-access")
				StringSelection value = new StringSelection((String) model.getValueAt(row, model.COLUMN_CREATOR_NAME));
				clipboard.setContents(value, null);
				
			}
		});
		menu.add(menu_copyName);
		*/

		

		JMenuItem copy_Creator_Address = new JMenuItem(Lang.getInstance().translate("Copy Account"));
		copy_Creator_Address.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(model.get_Public_Account(row).getAddress());
				clipboard.setContents(value, null);
			}
		});
		menu.add(copy_Creator_Address);

		JMenuItem menu_copy_Creator_PublicKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key"));
		menu_copy_Creator_PublicKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				PublicKeyAccount public_Account = model.get_Public_Account(row);
				StringSelection value = new StringSelection(public_Account.getBase58());
				clipboard.setContents(value, null);
			}
		});
		menu.add(menu_copy_Creator_PublicKey);


		
		JMenuItem menu_copy_Block_PublicKey = new JMenuItem(Lang.getInstance().translate("Copy No.Transaction"));
		menu_copy_Block_PublicKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(model.get_No_Trancaction(row));
				clipboard.setContents(value, null);
			}
		});
		menu.add(menu_copy_Block_PublicKey);

		
		
		
		JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.getInstance().translate("Send Asset to Person"));
		Send_Coins_item_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Account account =(Account) model.get_Public_Account(row);
				new Account_Send_Dialog(null, null, account, null);

			}
		});
		menu.add(Send_Coins_item_Menu);

		JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.getInstance().translate("Send Mail to Person"));
		Send_Mail_item_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Account account =(Account) model.get_Public_Account(row);

			new Mail_Send_Dialog(null, null, account, null);

			}
		});
		menu.add(Send_Mail_item_Menu);

		
		
		
		////////////////////
		TableMenuPopupUtil.installContextMenu(jTable_Vouches, menu); // SELECT

		

	}
	public void delay_on_close(){
		
		model.removeObservers();
			
			
			
		}


}
