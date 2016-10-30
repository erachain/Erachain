	package gui.items.records;

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
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Timer;
	import java.awt.*;

	import javax.swing.DefaultRowSorter;
	import javax.swing.JButton;
	import javax.swing.JDialog;
	import javax.swing.JFrame;
	import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
	import javax.swing.JPanel;
	import javax.swing.JPopupMenu;
	import javax.swing.JScrollPane;
	import javax.swing.JTable;
	import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
	import javax.swing.RowSorter;
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
	import core.item.assets.AssetCls;
	import core.item.persons.PersonCls;
import core.item.unions.UnionCls;
import core.transaction.Transaction;
import database.wallet.TransactionMap;
import gui.CoreRowSorter;
import gui.MainFrame;
	import gui.Main_Internal_Frame;
	import gui.RunMenu;
	import gui.Split_Panel;
	import gui.items.assets.IssueAssetPanel;
	import gui.items.assets.TableModelItemAssets;
	import gui.models.Renderer_Boolean;
	import gui.models.Renderer_Left;
	import gui.models.Renderer_Right;
	import gui.models.WalletItemAssetsTableModel;
	import gui.models.WalletItemPersonsTableModel;
import gui.models.WalletTransactionsTableModel;
import gui.transaction.TransactionDetailsFactory;
import lang.Lang;


	public class Records_My_SplitPanel extends Split_Panel{
		private static final long serialVersionUID = 2717571093561259483L;

		
		private WalletTransactionsTableModel my_Records_Model;
		private JTable my_Person_table;
		private TableRowSorter my_Sorter;
		private RunMenu my_run_menu;
	// для прозрачности
	     int alpha =255;
	     int alpha_int;
		
		
	public Records_My_SplitPanel(){
	
		this.setName(Lang.getInstance().translate("My Records"));
			this.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
			// not show buttons
			this.button1_ToolBar_LeftPanel.setVisible(false);
			this.button2_ToolBar_LeftPanel.setVisible(false);
			this.jButton1_jToolBar_RightPanel.setVisible(false);
			this.jButton2_jToolBar_RightPanel.setVisible(false);
			

			// not show My filter
			this.searth_My_JCheckBox_LeftPanel.setVisible(false);
			
		
			
			//TRANSACTIONS
			my_Records_Model = new WalletTransactionsTableModel();
			my_Person_table = new JTable(my_Records_Model);
			
			
			//TRANSACTIONS SORTER
			Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
			indexes.put(WalletTransactionsTableModel.COLUMN_CONFIRMATIONS, TransactionMap.TIMESTAMP_INDEX);
			indexes.put(WalletTransactionsTableModel.COLUMN_TIMESTAMP, TransactionMap.TIMESTAMP_INDEX);
			indexes.put(WalletTransactionsTableModel.COLUMN_CREATOR, TransactionMap.ADDRESS_INDEX);
			indexes.put(WalletTransactionsTableModel.COLUMN_AMOUNT, TransactionMap.AMOUNT_INDEX);
			CoreRowSorter sorter = new CoreRowSorter(my_Records_Model, indexes);
			my_Person_table.setRowSorter(sorter);
			
			//Custom renderer for the String column;
			//RenderingHints.
			my_Person_table.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
			my_Person_table.setDefaultRenderer(String.class, new Renderer_Left(my_Person_table.getFontMetrics(my_Person_table.getFont()),my_Records_Model.get_Column_AutoHeight())); // set renderer
			my_Person_table.setDefaultRenderer(Boolean.class, new Renderer_Boolean()); // set renderer
			my_Person_table.setDefaultRenderer(Double.class, new Renderer_Right()); // set renderer
			my_Person_table.setDefaultRenderer(Integer.class, new Renderer_Right()); // set renderer
			
			my_Person_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );
			
			TableColumn column_Size = my_Person_table.getColumnModel().getColumn(WalletTransactionsTableModel.COLUMN_SIZE);
			column_Size.setMinWidth(50);
			column_Size.setMaxWidth(1000);
			column_Size.setPreferredWidth(70);
			
			TableColumn column_Confirm = my_Person_table.getColumnModel().getColumn(WalletTransactionsTableModel.COLUMN_CONFIRMATIONS);//.COLUMN_SIZE);
			column_Confirm.setMinWidth(50);
			column_Confirm.setMaxWidth(1000);
			column_Confirm.setPreferredWidth(70);
			
			TableColumn column_Fee = my_Person_table.getColumnModel().getColumn(WalletTransactionsTableModel.COLUMN_FEE);//.COLUMN_SIZE);
			column_Fee.setMinWidth(80);
			column_Fee.setMaxWidth(1000);
			column_Fee.setPreferredWidth(80);
			
			TableColumn column_Date = my_Person_table.getColumnModel().getColumn(WalletTransactionsTableModel.COLUMN_TIMESTAMP);//.COLUMN_FEE);//.COLUMN_SIZE);
			column_Date.setMinWidth(120);
			column_Date.setMaxWidth(1000);
			column_Date.setPreferredWidth(120);
			
			my_Person_table.addMouseListener(new My_Mouse());
			my_run_menu  = new RunMenu();
			Dimension dim1 = new Dimension(180,25);
			my_run_menu.setSize(dim1);
			my_run_menu.setPreferredSize(dim1);
			my_run_menu.setVisible(false);
			my_run_menu.jButton1.setFocusPainted(true);
			my_run_menu.jButton1.setFocusCycleRoot(true);
			my_run_menu.jButton1.addActionListener(new My_run_menu_Button1_Action());
			my_run_menu.addWindowFocusListener(new My_run_Menu_Focus_Listener());

			 Dimension size = MainFrame.desktopPane.getSize();
			 this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
			 jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
			my_run_menu.pack();
		  
			
			this.jTable_jScrollPanel_LeftPanel = my_Person_table;
			this.jTable_jScrollPanel_LeftPanel.isFontSet();
			
			this.jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
				
			
			// обработка изменения положения курсора в таблице
			this.jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
						@SuppressWarnings({ "unused" })
						@Override
							public void valueChanged(ListSelectionEvent arg0) {
								String dateAlive;
								String date_birthday;
								String message; 
				// устанавливаем формат даты
								SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
				//создаем объект персоны
								UnionCls union;
								if (jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0 ){
									 
									//GET ROW
								        int row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
								        row =jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
								        
								        //GET TRANSACTION
								        Transaction transaction =  (Transaction) my_Records_Model.getItem(row);
								      //SHOW DETAIL SCREEN OF TRANSACTION
								     //   TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
									  
									 JPanel panel = new JPanel();
								        panel.setLayout(new GridBagLayout());
								      //  panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
										
										//TABLE GBC
										GridBagConstraints tableGBC = new GridBagConstraints();
										tableGBC.fill = GridBagConstraints.BOTH; 
										tableGBC.anchor = GridBagConstraints.FIRST_LINE_START;
										tableGBC.weightx = 1;
										tableGBC.weighty = 1;
										tableGBC.gridx = 0;	
										tableGBC.gridy= 0;	
										JPanel a = TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
										panel.add(TransactionDetailsFactory.getInstance().createTransactionDetail(transaction),tableGBC);
										  JLabel jLabel9 = new JLabel();
											jLabel9.setText("");
									        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
									        gridBagConstraints.gridx = 0;
									        gridBagConstraints.gridy = 1;
									        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
									        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
									        gridBagConstraints.weightx = 1.0;
									        gridBagConstraints.weighty = 1.0;
									        panel. add(jLabel9, gridBagConstraints);
										
										
										
										
									
								        jScrollPane_jPanel_RightPanel.setViewportView( panel);
									
									 
								}
							}
						});				
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
		    
		}
	
	// set favorine My
		void favorite_my(JTable table){
			int row = table.getSelectedRow();
			row = table.convertRowIndexToModel(row);

			PersonCls person = (PersonCls) my_Records_Model.getItem(row);
			//new AssetPairSelect(asset.getKey());

			
				//CHECK IF FAVORITES
				if(Controller.getInstance().isItemFavorite(person))
				{
					
					Controller.getInstance().removeItemFavorite(person);
				}
				else
				{
					
					Controller.getInstance().addItemFavorite(person);
				}
					

				table.repaint();

		}

		
	
	
	// listener search_tab run menu focus
		class My_run_Menu_Focus_Listener implements WindowFocusListener{
			@Override
			public void windowGainedFocus(WindowEvent arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void windowLostFocus(WindowEvent arg0) {
				// TODO Auto-generated method stub
				my_run_menu.setVisible(false);
			}
		};

		class My_run_menu_Button1_Action implements ActionListener{
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				favorite_my(my_Person_table);
				int row = my_Person_table.getSelectedRow();
				row = my_Person_table.convertRowIndexToModel(row);
				PersonCls person = (PersonCls) my_Records_Model.getItem(row);
				if(Controller.getInstance().isItemFavorite(person))
				{
					my_run_menu.jButton1.setText(Lang.getInstance().translate("Remove Favorite"));
				}
				else
				{
					my_run_menu.jButton1.setText(Lang.getInstance().translate("Add Favorite"));
				}
			
			
			}
		
		};

		class My_Mouse extends MouseAdapter {
			@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				int row = my_Person_table.rowAtPoint(p);
				row = my_Person_table.convertRowIndexToModel(row);
				PersonCls person = (PersonCls) my_Records_Model.getItem(row);
				if(e.getClickCount() == 2)
				{
					
				}
			
				if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1)
				{
					if(Controller.getInstance().isItemFavorite(person))
					{
						my_run_menu.jButton1.setText(Lang.getInstance().translate("Remove Favorite"));
					}
					else
					{
						my_run_menu.jButton1.setText(Lang.getInstance().translate("Add Favorite"));
					}
					my_run_menu.setLocation(e.getXOnScreen(), e.getYOnScreen());
					my_run_menu.setVisible(true);	
				}
			}
		}

	
		class My_Search implements DocumentListener {
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}
		
			public void removeUpdate(DocumentEvent e) {
				onChange();
			}
		
			public void insertUpdate(DocumentEvent e) {
				onChange();
			}
		
			public void onChange() {
				// GET VALUE
				String search = searchTextField_SearchToolBar_LeftPanel.getText();
				// SET FILTER
				my_Records_Model.fireTableDataChanged();
			
				RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
				((DefaultRowSorter)  my_Sorter).setRowFilter(filter);
					
				my_Records_Model.fireTableDataChanged();

			}
		}
		
		
	
	}




