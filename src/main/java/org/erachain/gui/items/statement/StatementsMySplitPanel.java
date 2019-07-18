package org.erachain.gui.items.statement;

import org.erachain.core.transaction.Transaction;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.TableMenuPopupUtil;
import org.erachain.utils.URLViewer;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class StatementsMySplitPanel extends SplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;


    // для прозрачности
    int alpha = 255;
    int alpha_int;
    StatementsTableModelMy my_Statements_Model;


    private TableRowSorter search_Sorter;


    public StatementsMySplitPanel() {
        super("StatementsMySplitPanel");

        this.setName(Lang.getInstance().translate("My Statements"));
        this.searthLabelSearchToolBarLeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
        // not show buttons
        this.button1ToolBarLeftPanel.setVisible(false);
        this.button2ToolBarLeftPanel.setVisible(false);
        this.jButton1_jToolBar_RightPanel.setVisible(false);
        this.jButton2_jToolBar_RightPanel.setVisible(false);


        // not show My filter
        this.searchMyJCheckBoxLeftPanel.setVisible(false);

        //TABLE


        my_Statements_Model = new StatementsTableModelMy();
        //	my_Statements_table = new JTable(my_Statements_Model);// new Statements_Table_Model();

        //	my_Statements_table.setTableHeader(null);
        //	my_Statements_table.setEditingColumn(0);
        //	my_Statements_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			
		/*	
			TableColumnModel columnModel = my_Statements_table.getColumnModel(); // read column model
			columnModel.getColumn(0).setMaxWidth((100));
			
			//Custom renderer for the String column;
			my_Statements_table.setDefaultRenderer(Long.class, new RendererRight()); // set renderer
			my_Statements_table.setDefaultRenderer(String.class, new RendererLeft()); // set renderer
					
					
			my_Sorter = new TableRowSorter(my_PersonsModel);
			my_Statements_table.setRowSorter(my_Sorter);
			my_Statements_table.getRowSorter();
			if (my_PersonsModel.getRowCount() > 0) my_PersonsModel.fireTableDataChanged();
			
			//CHECKBOX FOR CONFIRMED
			TableColumn confirmedColumn = my_Statements_table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_CONFIRMED);
			// confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
			confirmedColumn.setCellRenderer(new RendererBoolean());
			confirmedColumn.setMinWidth(50);
			confirmedColumn.setMaxWidth(50);
			confirmedColumn.setPreferredWidth(50);//.setWidth(30);
			
			
			//CHECKBOX FOR FAVORITE
			TableColumn favoriteColumn = my_Statements_table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_FAVORITE);
			//favoriteColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
			favoriteColumn.setCellRenderer(new RendererBoolean());
			favoriteColumn.setMinWidth(50);
			favoriteColumn.setMaxWidth(50);
			favoriteColumn.setPreferredWidth(50);//.setWidth(30);
	
			// UPDATE FILTER ON TEXT CHANGE
			this.searchTextFieldSearchToolBarLeftPanelDocument.getDocument().addDocumentListener(new My_Search());
			*/        // SET VIDEO
        //this.jTableJScrollPanelLeftPanel.setModel(my_PersonsModel);
        this.jTableJScrollPanelLeftPanel = new MTable(my_Statements_Model); //my_Statements_table;
        //this.jTableJScrollPanelLeftPanel.setTableHeader(null);
        // sorter
        search_Sorter = new TableRowSorter(my_Statements_Model);
        ArrayList<SortKey> keys = new ArrayList<RowSorter.SortKey>();
        keys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
        keys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
        search_Sorter.setSortKeys(keys);
        ((DefaultRowSorter<?, ?>) search_Sorter).setSortsOnUpdates(true);
        this.jTableJScrollPanelLeftPanel.setRowSorter(search_Sorter);
        this.jTableJScrollPanelLeftPanel.setEditingColumn(0);
        this.jTableJScrollPanelLeftPanel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.jTableJScrollPanelLeftPanel.setAutoCreateRowSorter(true);
        this.jScrollPanelLeftPanel.setViewportView(this.jTableJScrollPanelLeftPanel);
        // EVENTS on CURSOR
        this.jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new My_Tab_Listener());
//			 Dimension size = MainFrame.getInstance().desktopPane.getSize();
//			 this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
        // jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));

        JPopupMenu menu = new JPopupMenu();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) {
                    return;
                }

                Transaction transaction = my_Statements_Model.get_Statement(jTableJScrollPanelLeftPanel
                        .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
                if (transaction == null) {
                    return;
                }

                try {
                    URLViewer.openWebpage(new URL("http://" + Settings.getInstance().getBlockexplorerURL()
                            + ":" + Settings.getInstance().getWebPort() + "/index/blockexplorer.html"
                            + "?tx=" + transaction.viewHeightSeq()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });

        menu.add(setSeeInBlockexplorer);

        TableMenuPopupUtil.installContextMenu(jTableJScrollPanelLeftPanel, menu);

    }

    // set favorine My
    void favorite_my(JTable table) {
        int row = table.getSelectedRow();
        row = table.convertRowIndexToModel(row);
    }

    @Override
    public void onClose() {
        // delete observer left panel
        my_Statements_Model.removeObservers();
        // get component from right panel
        Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        if (c1 instanceof StatementInfo) ((StatementInfo) c1).delay_on_Close();

    }

    class My_Tab_Listener implements ListSelectionListener {

        //@SuppressWarnings("deprecation")
        @Override
        public void valueChanged(ListSelectionEvent arg0) {


            Transaction transaction = null;
            if (jTableJScrollPanelLeftPanel.getSelectedRow() >= 0)
                transaction = my_Statements_Model.get_Statement(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));

            if (transaction == null) return;

            JPanel info_panel = TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);

            info_panel.setPreferredSize(new Dimension(jScrollPaneJPanelRightPanel.getSize().width - 50, jScrollPaneJPanelRightPanel.getSize().height - 50));
            jScrollPaneJPanelRightPanel.setViewportView(info_panel);
            //	jSplitPanel.setRightComponent(info_panel);
        }

    }

}




