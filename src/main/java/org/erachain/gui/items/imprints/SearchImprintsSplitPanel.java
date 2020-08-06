package org.erachain.gui.items.imprints;

import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.WalletItemImprintsTableModel;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class SearchImprintsSplitPanel extends SplitPanel {

    private TableModelImprints tableModelImprints;

    public SearchImprintsSplitPanel() {
        super("SearchImprintsSplitPanel", title);

        setName(Lang.getInstance().translate("Search Imprints"));
        searthLabelSearchToolBarLeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
        searthLabelSearchToolBarLeftPanel.setVisible(true);
// not show buttons
        button1ToolBarLeftPanel.setVisible(false);
        button2ToolBarLeftPanel.setVisible(false);
        jButton1_jToolBar_RightPanel.setVisible(false);
        jButton2_jToolBar_RightPanel.setVisible(false);

//CREATE TABLE
        this.tableModelImprints = new TableModelImprints();
        final MTable imprintsTable = new MTable(this.tableModelImprints);

//CHECKBOX FOR FAVORITE
//	TableColumn favoriteColumn = imprintsTable.getColumnModel().getColumn(TableModelUnionsItemsTableModel.COLUMN_FAVORITE);
//	favoriteColumn.setCellRenderer(new RendererBoolean()); //unionsTable.getDefaultRenderer(Boolean.class));
//	favoriteColumn.setMinWidth(50);
//	favoriteColumn.setMaxWidth(50);
//	favoriteColumn.setPreferredWidth(50);//.setWidth(30);
// column #1
        TableColumn column1 = imprintsTable.getColumnModel().getColumn(WalletItemImprintsTableModel.COLUMN_KEY);//.COLUMN_CONFIRMED);
        column1.setMinWidth(1);
        column1.setMaxWidth(1000);
        column1.setPreferredWidth(50);
//Sorter
        RowSorter sorter = new TableRowSorter(this.tableModelImprints);
        imprintsTable.setRowSorter(sorter);
// UPDATE FILTER ON TEXT CHANGE
        searchTextFieldSearchToolBarLeftPanelDocument.getDocument().addDocumentListener(new DocumentListener() {
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
                String search = searchTextFieldSearchToolBarLeftPanelDocument.getText();

// SET FILTER
                tableModelImprints.fireTableDataChanged();
                RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
                ((DefaultRowSorter) sorter).setRowFilter(filter);
                tableModelImprints.fireTableDataChanged();

            }
        });

// set showvideo			
        jTableJScrollPanelLeftPanel.setModel(this.tableModelImprints);
        jTableJScrollPanelLeftPanel = imprintsTable;
        jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);

        // Event LISTENER
        jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                ImprintCls imprint = null;
                if (jTableJScrollPanelLeftPanel.getSelectedRow() >= 0)
                    imprint = tableModelImprints.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));


                //	info.show_001(person);

                //	search_Person_SplitPanel.jSplitPanel.setDividerLocation(search_Person_SplitPanel.jSplitPanel.getDividerLocation());
                //	search_Person_SplitPanel.searchTextFieldSearchToolBarLeftPanelDocument.setEnabled(true);
                ImprintsInfoPanel info_panel = new ImprintsInfoPanel(imprint);
                info_panel.setPreferredSize(new Dimension(jScrollPaneJPanelRightPanel.getSize().width - 50, jScrollPaneJPanelRightPanel.getSize().height - 50));
                jScrollPaneJPanelRightPanel.setViewportView(info_panel);
            }
        });
	
	
	
	
	
	/*
// MENU
	JPopupMenu nameSalesMenu = new JPopupMenu();
	JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
	details.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int row = imprintsTable.getSelectedRow();
			row = imprintsTable.convertRowIndexToModel(row);
			ImprintCls imprint = tableModelImprints.getImprint(row);
			new ImprintFrame(imprint);
		}
	});
	nameSalesMenu.add(details);
	imprintsTable.setComponentPopupMenu(nameSalesMenu);
	imprintsTable.addMouseListener(new MouseAdapter() {
	@Override
		public void mousePressed(MouseEvent e) {
			Point p = e.getPoint();
			int row = imprintsTable.rowAtPoint(p);
			imprintsTable.setRowSelectionInterval(row, row);
			if(e.getClickCount() == 2)
			{
				row = imprintsTable.convertRowIndexToModel(row);
				ImprintCls imprint = tableModelImprints.getImprint(row);
				new ImprintFrame(imprint);
			}
		}
	});
	*/

    }

    @Override
    public void onClose() {
        // delete observer left panel
        tableModelImprints.deleteObservers();
        // get component from right panel
        Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        if (c1 instanceof ImprintsInfoPanel) ((ImprintsInfoPanel) c1).delay_on_Close();

    }

}
