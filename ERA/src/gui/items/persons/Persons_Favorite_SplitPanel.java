package gui.items.persons;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import core.item.ItemCls;
import core.item.persons.PersonCls;
import gui.items.Item_SplitPanel;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import lang.Lang;

public class Persons_Favorite_SplitPanel extends Item_SplitPanel {
	private static final long serialVersionUID = 2717571093561259483L;
	private static Persons_Favorite_TableModel search_Table_Model = new Persons_Favorite_TableModel();
	private Persons_Favorite_SplitPanel th;

	public Persons_Favorite_SplitPanel() {
		super(search_Table_Model, "Persons_Favorite_SplitPanel");
		this.setName(Lang.getInstance().translate("Favorite Persons"));
		th = this;
		JMenuItem vsend_Coins_Item = new JMenuItem(Lang.getInstance().translate("Send Asset"));

		vsend_Coins_Item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Account_Send_Dialog(null, null, null, (PersonCls) th.item_Menu);
			}
		});

		th.menu_Table.add(vsend_Coins_Item);
		JMenuItem send_Mail_Item = new JMenuItem(Lang.getInstance().translate("Send Mail"));
		send_Mail_Item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Mail_Send_Dialog(null, null, null, (PersonCls) th.item_Menu);
			}
		});

		th.menu_Table.add(send_Mail_Item);
	}

	// show details
	@Override
	public Component get_show(ItemCls item) {
		return new Person_Info_002((PersonCls) item, true);
	}
	
	@Override
	public void delay_on_close() {
		search_Table_Model.removeObservers();
		jTable_jScrollPanel_LeftPanel = null;
		table_Model = null;
		favorite_menu_items = null;
		menu_Table= null;
		item_Menu =null;
		item_Table_Selected = null;
	}
}
