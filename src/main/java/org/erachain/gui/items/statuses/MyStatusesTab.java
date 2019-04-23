package org.erachain.gui.items.statuses;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.WalletItemStatusesTableModel;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MyStatusesTab extends SplitPanel {

    public ItemCls itemAll;
    public ItemCls itemMy;
    private WalletItemStatusesTableModel statusesModel;
    private MTable table;
    private TableColumn favoriteColumn;
    private MyStatusesTab tSP;
    //	private  StatusesItemsTableModel tableModelItemStatuses;
    private StatusInfo info1;
    // My statuses
    public MyStatusesTab() {
        super("MyStatusesTab");
        tSP = this;
        setName(Lang.getInstance().translate("My Statuses"));
        searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
        // not show buttons
        button1ToolBarLeftPanel.setVisible(false);
        button2ToolBarLeftPanel.setVisible(false);
        jButton1_jToolBar_RightPanel.setVisible(false);
        jButton2_jToolBar_RightPanel.setVisible(false);

        //TABLE
        statusesModel = new WalletItemStatusesTableModel();
        table = new MTable(statusesModel);

        TableColumnModel columnModel = table.getColumnModel(); // read column model
        columnModel.getColumn(0).setMaxWidth((100));


        TableRowSorter sorter1 = new TableRowSorter(statusesModel);
        table.setRowSorter(sorter1);
        table.getRowSorter();
        if (statusesModel.getRowCount() > 0) statusesModel.fireTableDataChanged();

        //CHECKBOX FOR CONFIRMED
        TableColumn confirmedColumn = table.getColumnModel().getColumn(WalletItemStatusesTableModel.COLUMN_CONFIRMED);
        // confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
        //		confirmedColumn.setCellRenderer(new RendererBoolean()); //statusesTable.getDefaultRenderer(Boolean.class));
        confirmedColumn.setMinWidth(50);
        confirmedColumn.setMaxWidth(90);
        confirmedColumn.setPreferredWidth(90);//.setWidth(30);

        TableColumn isUniqueColumn1 = table.getColumnModel().getColumn(WalletItemStatusesTableModel.COLUMN_UNIQUE);
        // confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
        //		confirmedColumn.setCellRenderer(new RendererBoolean()); //statusesTable.getDefaultRenderer(Boolean.class));
        isUniqueColumn1.setMinWidth(50);
        isUniqueColumn1.setMaxWidth(90);
        isUniqueColumn1.setPreferredWidth(90);//.setWidth(30);

        //CHECKBOX FOR FAVORITE
        favoriteColumn = table.getColumnModel().getColumn(WalletItemStatusesTableModel.COLUMN_FAVORITE);
        favoriteColumn.setMinWidth(50);
        favoriteColumn.setMaxWidth(90);
        favoriteColumn.setPreferredWidth(90);//.setWidth(30);
        searchFavoriteJCheckBoxLeftPanel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                onChange(tSP, sorter1);

            }

        });


        //CREATE SEARCH FIELD
        final JTextField txtSearch = new JTextField();

        // UPDATE FILTER ON TEXT CHANGE
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                onChange(tSP, sorter1);
            }

            public void removeUpdate(DocumentEvent e) {
                onChange(tSP, sorter1);
            }

            public void insertUpdate(DocumentEvent e) {
                onChange(tSP, sorter1);
            }


        });


        // set video
        //jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setModel(this.tableModelStatuses);
        jTableJScrollPanelLeftPanel.setModel(statusesModel);
        //jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel = statusesTable;
        jTableJScrollPanelLeftPanel = table;
        //jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setViewportView(jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel); // statusesTable;
        jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);


        // select row table statuses

        info1 = new StatusInfo();
        info1.setFocusable(false);

        // обработка изменения положения курсора в таблице
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                StatusCls status = null;
                if (table.getSelectedRow() >= 0)
                    status = statusesModel.getItem(table.convertRowIndexToModel(table.getSelectedRow())).b;
                if (status == null) return;
                info1.show_001(status);
                //	MainStatusesFrame.itemMy = status;

                //	PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());
                tSP.jSplitPanel.setDividerLocation(tSP.jSplitPanel.getDividerLocation());
                tSP.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
            }
        });
        tSP.jScrollPaneJPanelRightPanel.setViewportView(info1);

        // MENU

        JPopupMenu my_Statuses_Table_menu = new JPopupMenu();

        JMenuItem my_favorite = new JMenuItem(Lang.getInstance().translate(""));
        my_favorite.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                favorite_set(table);

            }
        });

        my_Statuses_Table_menu.add(my_favorite);

        my_Statuses_Table_menu.addPopupMenuListener(new PopupMenuListener() {

                                                        @Override
                                                        public void popupMenuCanceled(PopupMenuEvent arg0) {
                                                            // TODO Auto-generated method stub

                                                        }

                                                        @Override
                                                        public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
                                                            // TODO Auto-generated method stub

                                                        }

                                                        @Override
                                                        public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
                                                            // TODO Auto-generated method stub

                                                            int row = table.getSelectedRow();
                                                            row = table.convertRowIndexToModel(row);
                                                            StatusCls status = statusesModel.getItem(row).b;

                                                            my_favorite.setVisible(true);
                                                            //CHECK IF FAVORITES
                                                            if (Controller.getInstance().isItemFavorite(status)) {
                                                                my_favorite.setText(Lang.getInstance().translate("Remove Favorite"));
                                                            } else {
                                                                my_favorite.setText(Lang.getInstance().translate("Add Favorite"));
                                                            }

                                                        }

                                                    }

        );
   //     jTableJScrollPanelLeftPanel.setComponentPopupMenu(my_Statuses_Table_menu);
        TableMenuPopupUtil.installContextMenu(this.jTableJScrollPanelLeftPanel, my_Statuses_Table_menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON


    }

    public void favorite_set(JTable assetsTable) {


        int row = assetsTable.getSelectedRow();
        row = assetsTable.convertRowIndexToModel(row);

        StatusCls status = statusesModel.getItem(row).b;
        //new AssetPairSelect(asset.getKey());

        if (status.getKey() >= StatusCls.INITIAL_FAVORITES) {
            //CHECK IF FAVORITES
            if (Controller.getInstance().isItemFavorite(status)) {

                Controller.getInstance().removeItemFavorite(status);
            } else {

                Controller.getInstance().addItemFavorite(status);
            }


            assetsTable.repaint();

        }
    }

    public void onChange(SplitPanel search_Status_SplitPanel, RowSorter sorter) {
        // filter
        // GET VALUE
        String search = search_Status_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getText();

        RowFilter<Object, Object> fooBarFilter;
        statusesModel.fireTableDataChanged();

        if (search_Status_SplitPanel.searchFavoriteJCheckBoxLeftPanel.isSelected()) {

            ArrayList<RowFilter<Object, Object>> filters = new ArrayList<RowFilter<Object, Object>>(2);
            filters.add(RowFilter.regexFilter(".*" + search + ".*", statusesModel.COLUMN_NAME));
            filters.add(RowFilter.regexFilter(".*true*", statusesModel.COLUMN_FAVORITE));
            fooBarFilter = RowFilter.andFilter(filters);

        } else {

            fooBarFilter = RowFilter.regexFilter(".*" + search + ".*", statusesModel.COLUMN_NAME);
        }


        ((DefaultRowSorter) sorter).setRowFilter(fooBarFilter);

        statusesModel.fireTableDataChanged();
        //	String a = search_Status_SplitPanel.searchFavoriteJCheckBoxLeftPanel.isSelected().get.getText();
        //	a = a+ " ";
    }

    @Override
    public void onClose() {
        // delete observer left panel
        statusesModel.deleteObservers();
        // get component from right panel
        Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        if (c1 instanceof StatusInfo) ((StatusInfo) c1).delay_on_Close();

    }

}
