package gui.items.templates;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import core.item.ItemCls;
import core.item.statuses.StatusCls;
import core.item.templates.TemplateCls;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.Split_Panel;
import gui.items.Item_Search_SplitPanel;
import gui.items.statuses.Status_Info;
import gui.library.MTable;
import gui.records.VouchRecordDialog;
import lang.Lang;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class Search_Templates_Tab extends Item_Search_SplitPanel {
	private static TableModelNotes tableModelNotes = new TableModelNotes();
	private Search_Templates_Tab th;
	
	
	public Search_Templates_Tab(){
		super (tableModelNotes,"Search_Templates_Tab", "Search_Templates_Tab");
		this.th = this;
		setName(Lang.getInstance().translate("Search Templates"));
		JMenuItem vouch_Item= new JMenuItem(Lang.getInstance().translate("Vouch"));
    
	vouch_Item.addActionListener(new ActionListener(){
	
		@Override
		public void actionPerformed(ActionEvent e) {
			
			 TemplateCls note =(TemplateCls) th.item_Menu ;
			if (note == null) return;
				Transaction trans = DCSet.getInstance().getTransactionFinalMap().getTransaction(note.getReference());
				new VouchRecordDialog(trans.getBlockHeight(DCSet.getInstance()),trans.getSeqNo(DCSet.getInstance()));
		}
	});
	this.menu_Table.add(vouch_Item);
	}


//show details
	@Override
	protected Component get_show(ItemCls item) {
		return  new Info_Notes((TemplateCls) item);

	}


}
