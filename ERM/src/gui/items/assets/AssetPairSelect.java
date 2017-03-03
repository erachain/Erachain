package gui.items.assets;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import lang.Lang;

import javax.swing.DefaultRowSorter;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import gui.MainFrame;
import gui.Table_Formats;
import gui.library.MTable;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletItemAssetsTableModel;

@SuppressWarnings("serial")
public class AssetPairSelect extends JFrame{
	
	public AssetPairSelectTableModel assetPairSelectTableModel;
	 AssetsPairSelect_Panel pair_Panel = new AssetsPairSelect_Panel();
	 RowSorter sorter;

	public AssetPairSelect(long key, String action, String account) {
		
		super(Lang.getInstance().translate("ARONICLE.com") + " - " + Controller.getInstance().getAsset(key).toString() + " - " + Lang.getInstance().translate("Select pair"));
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//this.setSize(800, 600);
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		
		//Label GBC
				GridBagConstraints labelGBC = new GridBagConstraints();
				labelGBC.insets = new Insets(0, 5, 5, 0);
				labelGBC.fill = GridBagConstraints.BOTH;  
				labelGBC.anchor = GridBagConstraints.NORTHWEST;
				labelGBC.weightx = 1;	
				labelGBC.weighty = 1;	
				labelGBC.gridwidth = 2;
				labelGBC.gridx = 0;	
				labelGBC.gridy = 0;	
		
				JLabel label = new JLabel("Выберите пару");
				
				if (action == "Buy") label.setText("Укажите актив на который хотите купить " +  Controller.getInstance().getAsset(key).toString() );
				if (action == "To sell") label.setText("Укажите актив за который хотите продать " +  Controller.getInstance().getAsset(key).toString());
		//TABLE GBC
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.insets = new Insets(0, 5, 5, 0);
		tableGBC.fill = GridBagConstraints.BOTH;  
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;	
		tableGBC.weighty = 1;	
		tableGBC.gridwidth = 2;
		tableGBC.gridx = 0;	
		tableGBC.gridy = 1;	
		
		
		pair_Panel.button1_ToolBar_LeftPanel.setVisible(false);
		pair_Panel.button2_ToolBar_LeftPanel.setVisible(false);
		
		
		assetPairSelectTableModel = new AssetPairSelectTableModel(key, action);
				
		final MTable assetsPairTable = new MTable(assetPairSelectTableModel);
		
		sorter = new TableRowSorter(assetPairSelectTableModel);
		assetsPairTable.setRowSorter(sorter);	
		
		
		
		pair_Panel.jTable_jScrollPanel_LeftPanel.setModel(assetPairSelectTableModel);
	    pair_Panel.jTable_jScrollPanel_LeftPanel = assetsPairTable;
		
	    pair_Panel.jTable_jScrollPanel_LeftPanel .setIntercellSpacing(new java.awt.Dimension(2, 2));

	    pair_Panel.jTable_jScrollPanel_LeftPanel .setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	  //Custom renderer for the String column;
	    pair_Panel.jTable_jScrollPanel_LeftPanel.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
	    pair_Panel.jTable_jScrollPanel_LeftPanel.setDefaultRenderer(Integer.class, new Renderer_Right()); // set renderer
	    pair_Panel.jTable_jScrollPanel_LeftPanel.setDefaultRenderer(String.class, new Renderer_Left(pair_Panel.jTable_jScrollPanel_LeftPanel.getFontMetrics(pair_Panel.jTable_jScrollPanel_LeftPanel.getFont()),assetPairSelectTableModel.get_Column_AutoHeight() )); // set renderer
	    pair_Panel.jTable_jScrollPanel_LeftPanel.setDefaultRenderer(Boolean.class, new Renderer_Boolean()); // set renderer

	    
		 // column #1
			TableColumn column1 = pair_Panel.jTable_jScrollPanel_LeftPanel.getColumnModel().getColumn(AssetPairSelectTableModel.COLUMN_KEY);//.COLUMN_CONFIRMED);
			column1.setMinWidth(1);
			column1.setMaxWidth(1000);
			column1.setPreferredWidth(50);
		// column #1
			TableColumn column2 = pair_Panel.jTable_jScrollPanel_LeftPanel.getColumnModel().getColumn(AssetPairSelectTableModel.COLUMN_NAME);//.COLUMN_CONFIRMED);
			column2.setMinWidth(50);
			column2.setMaxWidth(1000);
			column2.setPreferredWidth(200);
				// column #1
				TableColumn column3 = pair_Panel.jTable_jScrollPanel_LeftPanel.getColumnModel().getColumn(AssetPairSelectTableModel.COLUMN_ORDERS_COUNT);//.COLUMN_CONFIRMED);
				column3.setMinWidth(50);
				column3.setMaxWidth(1000);
				column3.setPreferredWidth(50);
				// column #1
				TableColumn column4 = pair_Panel.jTable_jScrollPanel_LeftPanel.getColumnModel().getColumn(AssetPairSelectTableModel.COLUMN_ORDERS_VOLUME);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
				column4.setMinWidth(50);
				column4.setMaxWidth(1000);
				column4.setPreferredWidth(200);
		    
				TableColumn column5 = pair_Panel.jTable_jScrollPanel_LeftPanel.getColumnModel().getColumn(AssetPairSelectTableModel.COLUMN_TRADES_COUNT);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
				column5.setMinWidth(50);
				column5.setMaxWidth(1000);
				column5.setPreferredWidth(50);
				
				TableColumn column6 = pair_Panel.jTable_jScrollPanel_LeftPanel.getColumnModel().getColumn(AssetPairSelectTableModel.COLUMN_TRADES_VOLUME);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
				column6.setMinWidth(50);
				column6.setMaxWidth(1000);
				column6.setPreferredWidth(200);
	    
	    
				// изменение высоты строки при изменении ширины  
				
		//		pair_Panel.setRowHeightFormat(true);
	    

	    pair_Panel.jTable_jScrollPanel_LeftPanel .getTableHeader().setPreferredSize(new Dimension(10, (int)(pair_Panel.jTable_jScrollPanel_LeftPanel .getTableHeader().getPreferredSize().getHeight()+6)));

	    pair_Panel.jTable_jScrollPanel_LeftPanel .setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
	    
	    pair_Panel.jTable_jScrollPanel_LeftPanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable target = (JTable)e.getSource();
					int row = target.getSelectedRow();

					if(row < assetPairSelectTableModel.assets.size())
					{
						new ExchangeFrame(
								(AssetCls)Controller.getInstance().getItem(ItemCls.ASSET_TYPE, assetPairSelectTableModel.key), 
								(AssetCls) assetPairSelectTableModel.assets.get(row), action, account);
						((JFrame) (pair_Panel.getTopLevelAncestor())).dispose();
					}
				}
			}
		});
	    
		//this.add(new JScrollPane(assetsPairTable), tableGBC);
		//this.add(label, labelGBC);
		
	    pair_Panel.jScrollPanel_LeftPanel.setViewportView(pair_Panel.jTable_jScrollPanel_LeftPanel);
	    pair_Panel.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
	    
	 // UPDATE FILTER ON TEXT CHANGE
	    pair_Panel.searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new DocumentListener() {
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
	 					String search = pair_Panel.searchTextField_SearchToolBar_LeftPanel.getText();

	 		// SET FILTER
	 					assetPairSelectTableModel.fireTableDataChanged();
	 					RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
	 					((DefaultRowSorter) sorter).setRowFilter(filter);
	 					assetPairSelectTableModel.fireTableDataChanged();
	 									
	 				}
	 			});
	 	
	 	
	 	
	    
	    
	    
	    
	    Dimension size = MainFrame.desktopPane.getSize();
	    pair_Panel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
	    this.add(pair_Panel, labelGBC);
		//PACK
		this.pack();
		this.setSize(1000, this.getHeight());
		this.setResizable(true);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	

}
