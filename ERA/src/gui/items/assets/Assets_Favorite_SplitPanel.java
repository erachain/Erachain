	package gui.items.assets;

	import java.awt.Color;
	import java.awt.Component;
	import java.awt.Cursor;
	import java.awt.Dimension;
	import java.awt.GridLayout;
	import java.awt.Point;
	import java.awt.Rectangle;
	import java.awt.event.ActionEvent;
	import java.awt.event.ActionListener;
	import java.awt.event.FocusEvent;
	import java.awt.event.FocusListener;
	import java.awt.event.MouseAdapter;
	import java.awt.event.MouseEvent;
	import java.awt.event.MouseListener;
	import java.awt.event.MouseMotionListener;
	import java.awt.event.WindowEvent;
	import java.awt.event.WindowFocusListener;
	import java.awt.image.ColorModel;
	import javax.swing.Timer;
	import java.awt.*;

import javax.swing.AbstractButton;
import javax.swing.DefaultRowSorter;
	import javax.swing.JButton;
	import javax.swing.JDialog;
	import javax.swing.JFrame;
	import javax.swing.JInternalFrame;
	import javax.swing.JMenuItem;
	import javax.swing.JPanel;
	import javax.swing.JPopupMenu;
	import javax.swing.JScrollPane;
	import javax.swing.JTable;
	import javax.swing.JTextField;
	import javax.swing.RowFilter;
	import javax.swing.RowSorter;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
	import javax.swing.event.DocumentListener;
	import javax.swing.event.ListSelectionEvent;
	import javax.swing.event.ListSelectionListener;
	import javax.swing.event.PopupMenuEvent;
	import javax.swing.event.PopupMenuListener;
	import javax.swing.table.TableColumn;
	import javax.swing.table.TableColumnModel;
	import javax.swing.table.TableRowSorter;

	import controller.Controller;
import core.item.ItemCls;
import core.item.assets.AssetCls;
	import core.item.persons.PersonCls;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.MainFrame;
import gui.Main_Internal_Frame;
	import gui.Split_Panel;
import gui.items.Item_SplitPanel;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.assets.IssueAssetPanel;
	import gui.items.assets.TableModelItemAssets;
import gui.items.mails.Mail_Send_Dialog;
import gui.items.persons.Person_Info_002;
import gui.library.MTable;
import gui.models.Renderer_Boolean;
	import gui.models.Renderer_Left;
	import gui.models.Renderer_Right;
	import gui.models.WalletItemAssetsTableModel;
	import gui.models.WalletItemPersonsTableModel;
import gui.records.VouchRecordDialog;
import lang.Lang;
import utils.TableMenuPopupUtil;


	public class Assets_Favorite_SplitPanel extends Item_SplitPanel{
		private static final long serialVersionUID = 2717571093561259483L;
		private static TableModelItemAssetsFavorute table_Model = new TableModelItemAssetsFavorute();
		private Assets_Favorite_SplitPanel th;
		
	public Assets_Favorite_SplitPanel(){
		super(table_Model, "Assets_Favorite_SplitPanel");
		this.setName(Lang.getInstance().translate("Favorite Persons"));
		th=this;
				JMenuItem sell = new JMenuItem(Lang.getInstance().translate("To sell"));
				sell.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						new ExchangeFrame((AssetCls) th.item_Menu,null,  "To sell", "");	
					}
				});
				
				JMenuItem excahge = new JMenuItem(Lang.getInstance().translate("Exchange"));
				excahge.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						new ExchangeFrame((AssetCls) th.item_Menu,null,  "", "");	
					}
				});
					
				JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
				buy.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						new ExchangeFrame((AssetCls) th.item_Menu,null, "Buy", "");	
					}
				});
					
				JMenuItem vouch_menu= new JMenuItem(Lang.getInstance().translate("Vouch"));
				vouch_menu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						DCSet db = DCSet.getInstance();
						Transaction trans = db.getTransactionFinalMap().getTransaction(((AssetCls) th.item_Menu).getReference());
						new VouchRecordDialog(trans.getBlockHeight(db), trans.getSeqNo(db));
						
					}
				});
				
				

				th.menu_Table.addSeparator();
				th.menu_Table.add(excahge);
				th.menu_Table.addSeparator();
				th.menu_Table.add(buy);
				th.menu_Table.add(sell);
				th.menu_Table.addSeparator();
				th.menu_Table.add(vouch_menu);
				
				}
		
		
			// show details
				@Override
				public Component get_show(ItemCls item) {
					return new Asset_Info((AssetCls) item);
				}
				
				@Override
				protected void splitClose(){ 
					table_Model.removeObservers();
					
				}
			

		}
