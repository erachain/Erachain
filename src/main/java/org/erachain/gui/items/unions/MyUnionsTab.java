package org.erachain.gui.items.unions;

import org.erachain.core.item.unions.UnionCls;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.WalletItemUnionsTableModel;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.SimpleDateFormat;

public class MyUnionsTab extends SplitPanel {

    final WalletItemUnionsTableModel unionsModel;
    private TableColumnModel columnModel;
    private TableColumn favoriteColumn;

    public MyUnionsTab() {
        super("MyUnionsTab");
        // My unions

        setName(Lang.getInstance().translate("My Unions"));
        searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
        // not show buttons
        button1ToolBarLeftPanel.setVisible(false);
        button2ToolBarLeftPanel.setVisible(false);
        jButton1_jToolBar_RightPanel.setVisible(false);
        jButton2_jToolBar_RightPanel.setVisible(false);
        //TABLE
        unionsModel = new WalletItemUnionsTableModel();
        final MTable tableUnion = new MTable(unionsModel);
        columnModel = tableUnion.getColumnModel(); // read column model
        columnModel.getColumn(0).setMaxWidth((100));
        //Custom renderer for the String column;
        TableRowSorter<WalletItemUnionsTableModel> sorter1 = new TableRowSorter<WalletItemUnionsTableModel>(unionsModel);
        tableUnion.setRowSorter(sorter1);
        tableUnion.getRowSorter();
        //unionsModel.fireTableDataChanged();
        //CHECKBOX FOR CONFIRMED
        TableColumn confirmedColumn = tableUnion.getColumnModel().getColumn(WalletItemUnionsTableModel.COLUMN_CONFIRMED);
        confirmedColumn.setMinWidth(170);
        confirmedColumn.setMaxWidth(170);
        confirmedColumn.setPreferredWidth(50);
        //CHECKBOX FOR FAVORITE
        favoriteColumn = tableUnion.getColumnModel().getColumn(WalletItemUnionsTableModel.COLUMN_FAVORITE);
        //favoriteColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
        favoriteColumn.setMinWidth(150);
        favoriteColumn.setMaxWidth(150);
        favoriteColumn.setPreferredWidth(50);//.setWidth(30);

        //Sorter
        RowSorter sorter11 = new TableRowSorter(unionsModel);
        tableUnion.setRowSorter(sorter11);


        //CREATE SEARCH FIELD
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

            @SuppressWarnings("unchecked")
            public void onChange() {
                // GET VALUE
                String search = searchTextField_SearchToolBar_LeftPanel.getText();
                // SET FILTER
                unionsModel.fireTableDataChanged();
                @SuppressWarnings("rawtypes")
                RowFilter filter1 = RowFilter.regexFilter(".*" + search + ".*", 1);
                ((DefaultRowSorter<WalletItemUnionsTableModel, ?>) sorter11).setRowFilter(filter1);
                unionsModel.fireTableDataChanged();
            }
        });
        // set show
        jTableJScrollPanelLeftPanel.setModel(unionsModel);
        jTableJScrollPanelLeftPanel = tableUnion;
        jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);
        // new info panel
        UnionInfo info1 = new UnionInfo();
        // обработка изменения положения курсора в таблице
        tableUnion.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                // устанавливаем формат даты
                SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
                //создаем объект персоны
                UnionCls union;
                if (tableUnion.getSelectedRow() >= 0) {
                    // select person
                    union = unionsModel.getItem(tableUnion.convertRowIndexToModel(tableUnion.getSelectedRow())).b;
                    info1.show_Union_002(union);
                    jSplitPanel.setDividerLocation(jSplitPanel.getDividerLocation());
                    searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
                }
            }
        });
        jScrollPaneJPanelRightPanel.setViewportView(info1);
    }

    @Override
    public void onClose() {
        // delete observer left panel
        unionsModel.deleteObservers();
        // get component from right panel
        Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        //  if (c1 instanceof StatementInfo) ( (StatementInfo)c1).delay_on_Close();

    }
}