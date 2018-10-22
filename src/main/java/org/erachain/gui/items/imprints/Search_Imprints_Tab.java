package org.erachain.gui.items.imprints;

import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.gui.Split_Panel;
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

public class Search_Imprints_Tab extends Split_Panel {

    private TableModelImprints tableModelImprints;

    public Search_Imprints_Tab() {
        super("Search_Imprints_Tab");

        setName(Lang.getInstance().translate("Search Imprints"));
        searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
        searthLabel_SearchToolBar_LeftPanel.setVisible(true);
// not show buttons
        button1_ToolBar_LeftPanel.setVisible(false);
        button2_ToolBar_LeftPanel.setVisible(false);
        jButton1_jToolBar_RightPanel.setVisible(false);
        jButton2_jToolBar_RightPanel.setVisible(false);

//CREATE TABLE
        this.tableModelImprints = new TableModelImprints();
        final MTable imprintsTable = new MTable(this.tableModelImprints);

//CHECKBOX FOR FAVORITE
//	TableColumn favoriteColumn = imprintsTable.getColumnModel().getColumn(TableModelUnions.COLUMN_FAVORITE);
//	favoriteColumn.setCellRenderer(new Renderer_Boolean()); //unionsTable.getDefaultRenderer(Boolean.class));
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
        searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new DocumentListener() {
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
                tableModelImprints.fireTableDataChanged();
                RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
                ((DefaultRowSorter) sorter).setRowFilter(filter);
                tableModelImprints.fireTableDataChanged();

            }
        });

// set showvideo			
        jTable_jScrollPanel_LeftPanel.setModel(this.tableModelImprints);
        jTable_jScrollPanel_LeftPanel = imprintsTable;
        jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);

        // Event LISTENER
        jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                ImprintCls imprint = null;
                if (jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0)
                    imprint = tableModelImprints.getImprint(jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));


                //	info.show_001(person);

                //	search_Person_SplitPanel.jSplitPanel.setDividerLocation(search_Person_SplitPanel.jSplitPanel.getDividerLocation());
                //	search_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
                Imprints_Info_Panel info_panel = new Imprints_Info_Panel(imprint);
                info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width - 50, jScrollPane_jPanel_RightPanel.getSize().height - 50));
                jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
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
    public void delay_on_close() {
        // delete observer left panel
        tableModelImprints.removeObservers();
        // get component from right panel
        Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
        // if Person_Info 002 delay on close
        if (c1 instanceof Imprints_Info_Panel) ((Imprints_Info_Panel) c1).delay_on_Close();

    }

}
