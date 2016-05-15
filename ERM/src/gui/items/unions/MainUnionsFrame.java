package gui.items.unions;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultRowSorter;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.mapdb.Fun.Tuple3;

import core.item.unions.UnionCls;
import core.item.statuses.StatusCls;
import core.item.unions.UnionCls;
import database.DBSet;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.Split_Panel;
//import gui.items.unions.Union_Info;
//import gui.items.unions.IssueUnionDialog;
//import gui.items.unions.MainUnionsFrame;
//import gui.items.unions.UnionConfirmDialog;
//import gui.items.unions.UnionSetStatusDialog;
//import gui.items.unions.Union_Info;
//import gui.items.unions.TableModelUnions;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletItemUnionsTableModel;
import lang.Lang;

public class MainUnionsFrame extends Main_Internal_Frame{

private TableModelUnions tableModelUnions;

public MainUnionsFrame (){
	// not show buttons main Toolbar
		this.setTitle(Lang.getInstance().translate("Unions"));
		 this.jButton2_jToolBar.setVisible(false);
		 this.jButton3_jToolBar.setVisible(false);
	// buttun1
		 this.jButton1_jToolBar.setText(Lang.getInstance().translate("Issue Union"));
	// status panel
		 this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with unions"));
		 
		 this.jButton1_jToolBar.addActionListener(new ActionListener()
			{
			    public void actionPerformed(ActionEvent e)
			    {
			    //	 Menu.selectOrAdd( new IssueUnionFrame(), MainFrame.desktopPane.getAllFrames());
			    	 new IssueUnionDialog();
			    }

				
			});	

		// all unions 
			Split_Panel search_Union_SplitPanel = new Split_Panel();
			search_Union_SplitPanel.setName(Lang.getInstance().translate("Search Unions"));
			search_Union_SplitPanel.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
			// not show buttons
			search_Union_SplitPanel.button1_ToolBar_LeftPanel.setVisible(false);
			search_Union_SplitPanel.button2_ToolBar_LeftPanel.setVisible(false);
			search_Union_SplitPanel.jButton1_jToolBar_RightPanel.setVisible(false);
			search_Union_SplitPanel.jButton2_jToolBar_RightPanel.setVisible(false);
			
			
			//CREATE TABLE
				this.tableModelUnions = new TableModelUnions();
				final JTable unionsTable = new JTable(this.tableModelUnions);
				TableColumnModel columnModel = unionsTable.getColumnModel(); // read column model
				columnModel.getColumn(0).setMaxWidth((100));
			//Custom renderer for the String column;
				unionsTable.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
				unionsTable.setDefaultRenderer(String.class, new Renderer_Left()); // set renderer
			//CHECKBOX FOR FAVORITE
				TableColumn favoriteColumn = unionsTable.getColumnModel().getColumn(TableModelUnions.COLUMN_FAVORITE);
				
			

				
				
				favoriteColumn.setCellRenderer(new Renderer_Boolean()); //unionsTable.getDefaultRenderer(Boolean.class));
				favoriteColumn.setMinWidth(50);
				favoriteColumn.setMaxWidth(50);
				favoriteColumn.setPreferredWidth(50);//.setWidth(30);
			//	unionsTable.setAutoResizeMode(5);//.setAutoResizeMode(mode);.setAutoResizeMode(0);
			//Sorter
				RowSorter sorter =   new TableRowSorter(this.tableModelUnions);
				unionsTable.setRowSorter(sorter);	
				// UPDATE FILTER ON TEXT CHANGE
				
				search_Union_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new DocumentListener() {
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
								String search = search_Union_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getText();

							 	// SET FILTER
						//		tableModelUnions.getSortableList().setFilter(search);
								tableModelUnions.fireTableDataChanged();
								
								RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
								((DefaultRowSorter) sorter).setRowFilter(filter);
								
								tableModelUnions.fireTableDataChanged();
								
							}
						});
				
				
				
				

			// set video			
						//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setModel(this.tableModelUnions);
				search_Union_SplitPanel.jTable_jScrollPanel_LeftPanel.setModel(this.tableModelUnions);
						//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel = unionsTable;
				search_Union_SplitPanel.jTable_jScrollPanel_LeftPanel = unionsTable;
						//jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setViewportView(jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel); // unionsTable; 
				search_Union_SplitPanel.jScrollPanel_LeftPanel.setViewportView(search_Union_SplitPanel.jTable_jScrollPanel_LeftPanel);
			// select row table unions

				
			 
				JEditorPane info = new JEditorPane();
				info.setContentType("text/html");
				info.setText("<HTML>" + Lang.getInstance().translate("Select union")); // Document text is provided below.
				info.setBackground(new Color(255, 255, 255, 0));
		//		info.setFocusable(false);
				//		
			// обработка изменения положения курсора в таблице
						 //jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
						 search_Union_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
							 @SuppressWarnings("deprecation")
								@Override
								public void valueChanged(ListSelectionEvent arg0) {
									String dateAlive;
									String date_birthday;
									String message;
									// TODO Auto-generated method stub
									// устанавливаем формат даты
									SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
									//создаем объект персоны
									UnionCls union;
											
								
									if (unionsTable.getSelectedRow() >= 0 ){
										union = tableModelUnions.getUnion(unionsTable.convertRowIndexToModel(unionsTable.getSelectedRow()));
									

									if (union.isConfirmed()){
										date_birthday=  formatDate.format(new Date(Long.valueOf(union.getBirthday())));
										message ="<html><div>#" + "<b>" + union.getKey() + " : " + date_birthday + "</b>"
										+ "<br>" + union.getName().toString() + "</div>";

										message += "<h2>"+ "Statuses" +"</h2>";
										// GETT UNION STATUS for ALIVE
										Tuple3<Long, Integer, byte[]> t3Alive = null; //DBSet.getInstance().getUnionStatusMap().getItem(union.getKey());
								
										if (t3Alive != null){
											if (t3Alive.a == null) dateAlive = "active";
											else dateAlive = formatDate.format( new Date(t3Alive.a));
										} else
										{
											dateAlive = Lang.getInstance().translate("unknown");
										}
										message += "<div>" + Lang.getInstance().translate("ALIVE")+": <b>" + dateAlive +"</b></div>";

										// GETT UNION STATUS for DEAD
										Tuple3<Long, Integer, byte[]> t3Dead = DBSet.getInstance().getUnionStatusMap().getItem(union.getKey(), StatusCls.DEAD_KEY);
								
										if (t3Dead != null){
											if (t3Dead.a == null) dateAlive = "yes";
											else dateAlive = formatDate.format( new Date(t3Dead.a ));
										} else
										{
											dateAlive = Lang.getInstance().translate("unknown");
										}
										message += "<div>" + Lang.getInstance().translate("DEAD")+": <b>" + dateAlive +"</b></div>";

										// GET CERTIFIED ACCOUNTS
										message += "<h2>"+ "Accounts" +"</h2>";
										TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, byte[]>>> addresses= null; //DBSet.getInstance().getUnionAddressMap().getItems(union.getKey());
										if ( !addresses.isEmpty()){
											// for each account seek active date
											String active_date_str;
											for( Map.Entry<String, java.util.Stack<Tuple3<Integer, Integer, byte[]>>> e : addresses.entrySet())
											{
												Tuple3<Integer, Integer, byte[]> active_date = e.getValue().peek();
												if (active_date.a == 0) active_date_str = "active";
												else active_date_str = formatDate.format( new Date(active_date.a * (long)86400000));
												
												message += "<div><input type='text' size='33' value='"+ e.getKey() +"' disabled='disabled' class='disabled' onchange =''>"
														+ " -> <b>" + active_date_str +"</b></div>";
											}
										}
										else{
											message += "<p> " +  Lang.getInstance().translate("Account not found!")+ "</p";
										}					
									} else {
										message = "<html><p>"+ Lang.getInstance().translate("Not found!") +"</p>";	
									}
									message = message + "</html>";
									
									info.setText(message);	
									
							//		Address1.setText(message);
							//		PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());//.setPreferredSize(new Dimension(100,100));		
									search_Union_SplitPanel.jSplitPanel.setDividerLocation(search_Union_SplitPanel.jSplitPanel.getDividerLocation());	
									search_Union_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
									
									
									
									}
								}
							});
						 
				
							 search_Union_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info);
				
//				Union_Info info = new Union_Info();
//				info.show_001(null);
				//this.Panel_Right_Panel.add(info);
				 //this.jScrollPane_Panel_Right_Panel.setViewportView(info);
						
						// MENU
							
							JPopupMenu all_Unions_Table_menu = new JPopupMenu();
							JMenuItem confirm_Menu = new JMenuItem(Lang.getInstance().translate("Confirm"));
							confirm_Menu.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									/*
									int row = unionsTable.getSelectedRow();
									row = unionsTable.convertRowIndexToModel(row);

									UnionCls union = tableModelUnions.getUnion(row);
									new UnionFrame(union);
									*/
									// открываем диалоговое окно ввода данных для подтверждения персоны 
									UnionCls union = tableModelUnions.getUnion(unionsTable.getSelectedRow());

							    	UnionConfirmDialog fm = new UnionConfirmDialog(MainUnionsFrame.this, union);	
							    	// обрабатываем полученные данные от диалогового окна
							    	//if(fm.isOK()){
					                //    JOptionPane.showMessageDialog(Form1.this, "OK");
					                //}
								
								}
							});
							all_Unions_Table_menu.add(confirm_Menu);

							JMenuItem setStatus_Menu = new JMenuItem(Lang.getInstance().translate("Set Status"));
							setStatus_Menu.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {

									UnionCls union = tableModelUnions.getUnion(unionsTable.getSelectedRow());

									UnionSetStatusDialog fm = new UnionSetStatusDialog(MainUnionsFrame.this, union);	
							//		
									
							    	// обрабатываем полученные данные от диалогового окна
							    	//if(fm.isOK()){
					                //    JOptionPane.showMessageDialog(Form1.this, "OK");
					                //}
								
								}
							});
							all_Unions_Table_menu.add(setStatus_Menu);

							unionsTable.setComponentPopupMenu(all_Unions_Table_menu);
							
							unionsTable.addMouseListener(new MouseAdapter() {
								@Override
								public void mousePressed(MouseEvent e) {
									Point p = e.getPoint();
									int row = unionsTable.rowAtPoint(p);
									unionsTable.setRowSelectionInterval(row, row);
									
									if(e.getClickCount() == 2)
									{
										row = unionsTable.convertRowIndexToModel(row);
										UnionCls union = tableModelUnions.getUnion(row);
							//			new UnionFrame(union);
										
									}
								}
							});
						 
						 
						





			
			
// My unions		
							
							Split_Panel my_Union_SplitPanel = new Split_Panel();
							my_Union_SplitPanel.setName(Lang.getInstance().translate("My Unions"));
							my_Union_SplitPanel.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
							// not show buttons
							my_Union_SplitPanel.button1_ToolBar_LeftPanel.setVisible(false);
							my_Union_SplitPanel.button2_ToolBar_LeftPanel.setVisible(false);
							my_Union_SplitPanel.jButton1_jToolBar_RightPanel.setVisible(false);
							my_Union_SplitPanel.jButton2_jToolBar_RightPanel.setVisible(false);
							
							//TABLE
									final WalletItemUnionsTableModel unionsModel = new WalletItemUnionsTableModel();
									final JTable tableUnion = new JTable(unionsModel);
									
									columnModel = tableUnion.getColumnModel(); // read column model
										columnModel.getColumn(0).setMaxWidth((100));
									
									//Custom renderer for the String column;
									tableUnion.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
									tableUnion.setDefaultRenderer(String.class, new Renderer_Left()); // set renderer
									
									
									TableRowSorter sorter1 = new TableRowSorter(unionsModel);
									tableUnion.setRowSorter(sorter1);
									tableUnion.getRowSorter();
									unionsModel.fireTableDataChanged();
									
									//CHECKBOX FOR CONFIRMED
									TableColumn confirmedColumn = tableUnion.getColumnModel().getColumn(WalletItemUnionsTableModel.COLUMN_CONFIRMED);
									// confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
									confirmedColumn.setCellRenderer(new Renderer_Boolean()); //unionsTable.getDefaultRenderer(Boolean.class));
									confirmedColumn.setMinWidth(50);
									confirmedColumn.setMaxWidth(50);
									confirmedColumn.setPreferredWidth(50);//.setWidth(30);
									
									
									//CHECKBOX FOR FAVORITE
									favoriteColumn = tableUnion.getColumnModel().getColumn(WalletItemUnionsTableModel.COLUMN_FAVORITE);
									//favoriteColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
									favoriteColumn.setCellRenderer(new Renderer_Boolean()); //unionsTable.getDefaultRenderer(Boolean.class));
									favoriteColumn.setMinWidth(50);
									favoriteColumn.setMaxWidth(50);
									favoriteColumn.setPreferredWidth(50);//.setWidth(30);
									
								//	TableColumn keyColumn = table.getColumnModel().getColumn(WalletItemUnionsTableModel.COLUMN_KEY);
								//	keyColumn.setCellRenderer(new Renderer_Right());
									
								//	TableColumn nameColumn = table.getColumnModel().getColumn(WalletItemUnionsTableModel.COLUMN_NAME);
								//	nameColumn.setCellRenderer(new Renderer_Left());
									
								//	TableColumn addrColumn = table.getColumnModel().getColumn(WalletItemUnionsTableModel.COLUMN_ADDRESS);
								//	addrColumn.setCellRenderer(new Renderer_Left());
									
									
									
										
									
											//CREATE SEARCH FIELD
											final JTextField txtSearch = new JTextField();

											// UPDATE FILTER ON TEXT CHANGE
											txtSearch.getDocument().addDocumentListener(new DocumentListener() {
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
													String search = txtSearch.getText();

												 	// SET FILTER
											//		tableModelUnions.getSortableList().setFilter(search);
													unionsModel.fireTableDataChanged();
													
													RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
													((DefaultRowSorter) sorter1).setRowFilter(filter);
													
													unionsModel.fireTableDataChanged();
													
												}
											});
							
							

											// set video			
											//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setModel(this.tableModelUnions);
									my_Union_SplitPanel.jTable_jScrollPanel_LeftPanel.setModel(unionsModel);
											//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel = unionsTable;
									my_Union_SplitPanel.jTable_jScrollPanel_LeftPanel = tableUnion;
											//jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setViewportView(jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel); // unionsTable; 
									my_Union_SplitPanel.jScrollPanel_LeftPanel.setViewportView(my_Union_SplitPanel.jTable_jScrollPanel_LeftPanel);		
											
											
											
											// select row table unions
											
											//  JEditorPane info1 = new JEditorPane();
											// info1.setFocusable(false);
												JEditorPane Address1 = new JEditorPane();
												Address1.setContentType("text/html");
												Address1.setText("<HTML>" + Lang.getInstance().translate("Select union")); // Document text is provided below.
												Address1.setBackground(new Color(255, 255, 255, 0));
												
									//			 JSplitPane PersJSpline = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,new JScrollPane(tableUnion),new JScrollPane(Address1)); 
												 
												 
												// обработка изменения положения курсора в таблице
												tableUnion.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
													
													@SuppressWarnings("deprecation")
													@Override
													public void valueChanged(ListSelectionEvent arg0) {
														String Date_Acti;
														String Date_birs;
														String message;
												// TODO Auto-generated method stub
												// устанавливаем формат даты
														SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
												//создаем объект персоны
														UnionCls union;
														
														
															if (tableUnion.getSelectedRow() >= 0 ){
															union = unionsModel.getItem(tableUnion.convertRowIndexToModel(tableUnion.getSelectedRow()));
														
														
												//читаем таблицу персон.
														Tuple3<Integer, Integer, byte[]> t3 = null; //DBSet.getInstance().getUnionStatusMap().getItem(union.getKey()); //(Long) unionsTable.getValueAt(unionsTable.getSelectedRow(),0));
												// преобразование в дату
												
												
														if (t3 != null){
															if (t3.a == 0) Date_Acti = "+";
															else Date_Acti = formatDate.format( new Date(Long.valueOf(t3.a.toString())));
														} else
														{
															Date_Acti =Lang.getInstance().translate("Not found!");
														};
														if (union.isConfirmed()){
															Date_birs=  formatDate.format(new Date(Long.valueOf(union.getBirthday())));
															 message ="<html><div></div><div> <p><b>" + Lang.getInstance().translate("Key")+":"   + union.getKey()        			+ "</p>"
															+ "<p> <b> "  + Lang.getInstance().translate("Name")+":"       			  + union.getName().toString()		+ "</p>" 
													        + "<p> "  + Lang.getInstance().translate("Birthday")  +":"        	      + Date_birs			+"</p>"
													        + "<p>  "  + Lang.getInstance().translate("To Date")  +":"        		  + Date_Acti			+"</p>"
													        ;
															 // Читаем адреса клиента
															 TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, byte[]>>> Addresses= null; //DBSet.getInstance().getUnionAddressMap().getItems(union.getKey());
															 if ( !Addresses.isEmpty()){
																 message =message + "<p>"  + Lang.getInstance().translate("Account")  +":  <input type='text' size='40' value='"+ Addresses.lastKey() +"' id='iiii' name='nnnn' class= 'cccc' onchange =''><p></div>";
															 }
															 else{
																 message = message + "<p> " +  Lang.getInstance().translate("Account not found!")+ "</p";
																							 }
														}else{
															
															message = "<html><p>"+ Lang.getInstance().translate("Not found!") +"</></>";	
														}
														message = message + "</html>";
														
															
												Address1.setText(message);
												my_Union_SplitPanel.jSplitPanel.setDividerLocation(my_Union_SplitPanel.jSplitPanel.getDividerLocation());	
												my_Union_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
													
													}
													}
													
													});
												
												
												
												
							
							
											 my_Union_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(Address1);
							
							
							
							
							


			this.jTabbedPane.add(my_Union_SplitPanel);
			
			this.jTabbedPane.add(search_Union_SplitPanel);


			this.pack();
//			this.setSize(800,600);
			this.setMaximizable(true);
			
			this.setClosable(true);
			this.setResizable(true);
//			this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
			this.setLocation(20, 20);
//			this.setIconImages(icons);
			//CLOSE
			setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		    this.setResizable(true);
//		    splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
		    //my_union_panel.requestFocusInWindow();
		    this.setVisible(true);
		    Rectangle k = this.getNormalBounds();
		 //   this.setBounds(k);
		    Dimension size = MainFrame.desktopPane.getSize();
		    this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
		 // setDividerLocation(700)



			search_Union_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
		 	my_Union_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));


}
}
