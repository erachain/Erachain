package gui.items.unions;

import core.item.unions.UnionCls;
import gui.Split_Panel;
import gui.library.MTable;
import gui.models.WalletItemUnionsTableModel;
import lang.Lang;

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

public class My_Unions_Tab extends Split_Panel {

    final WalletItemUnionsTableModel unionsModel;
    private TableColumnModel columnModel;
    private TableColumn favoriteColumn;

    public My_Unions_Tab() {
        super("My_Unions_Tab");
        // My unions

        setName(Lang.getInstance().translate("My Unions"));
        searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
        // not show buttons
        button1_ToolBar_LeftPanel.setVisible(false);
        button2_ToolBar_LeftPanel.setVisible(false);
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
        jTable_jScrollPanel_LeftPanel.setModel(unionsModel);
        jTable_jScrollPanel_LeftPanel = tableUnion;
        jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
        // new info panel
        Union_Info info1 = new Union_Info();
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
                    union = unionsModel.getItem(tableUnion.convertRowIndexToModel(tableUnion.getSelectedRow()));
                    info1.show_Union_002(union);
                    jSplitPanel.setDividerLocation(jSplitPanel.getDividerLocation());
                    searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
                }
            }
        });
        jScrollPane_jPanel_RightPanel.setViewportView(info1);
    }

    @Override
    public void delay_on_close() {
        // delete observer left panel
        unionsModel.removeObservers();
        // get component from right panel
        Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
        // if Person_Info 002 delay on close
        //  if (c1 instanceof Statement_Info) ( (Statement_Info)c1).delay_on_Close();

    }
}