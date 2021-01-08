package org.erachain.gui.items;

import org.erachain.gui.library.MDecimalFormatedTextField;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class SearchItemSplitPanel extends ItemSplitPanel {

    private static final long serialVersionUID = 2717571093561259483L;
    protected SearchItemsTableModel search_Table_Model;
    private MDecimalFormatedTextField key_Item;

    @SuppressWarnings("rawtypes")
    public SearchItemSplitPanel(SearchItemsTableModel search_Table_Model1, String gui_Name, String search_Label_Text) {

        super(search_Table_Model1, gui_Name, search_Label_Text);
        this.search_Table_Model = search_Table_Model1;
        searthLabelSearchToolBarLeftPanel.setText(Lang.T("Search") + ":  ");

        // CHECKBOX FOR FAVORITE
        TableColumn favorite_Column = jTableJScrollPanelLeftPanel.getColumnModel()
                .getColumn(search_Table_Model.COLUMN_FAVORITE);
        favorite_Column.setMaxWidth(150);

        // search Panel
        this.searchToolBar_LeftPanel.setVisible(true);
        this.toolBarLeftPanel.add(new JLabel(Lang.T("Find Key") + ":"));
        key_Item = new MDecimalFormatedTextField();
        key_Item.setMaskType(key_Item.maskLong);
        key_Item.setToolTipText("");
        key_Item.setAlignmentX(1.0F);
        key_Item.setMinimumSize(new java.awt.Dimension(100, 20));
        key_Item.setName(""); // NOI18N
        key_Item.setPreferredSize(new java.awt.Dimension(100, 20));
        key_Item.setMaximumSize(new java.awt.Dimension(2000, 20));

        MenuPopupUtil.installContextMenu(key_Item);

        this.toolBarLeftPanel.add(key_Item);
        key_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                searchTextFieldSearchToolBarLeftPanelDocument.setText("");
                Label_search_Info_Panel.setText(Lang.T("Waiting..."));
                jScrollPanelLeftPanel.setViewportView(search_Info_Panel);
                new Thread() {
                    @Override
                    public void run() {
                        search_Table_Model.findByKey(key_Item.getText());
                        if (search_Table_Model.getRowCount() > 0) {
                            jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);
                            jTableJScrollPanelLeftPanel. getSelectionModel().addSelectionInterval(0, 0);
                            return;
                        }
                        Label_search_Info_Panel.setText(Lang.T("Not Found"));
                        jScrollPanelLeftPanel.setViewportView(search_Info_Panel);
                        jScrollPaneJPanelRightPanel.setViewportView(null);
                    }
                }.start();
            }
        });


        // UPDATE FILTER ON TEXT CHANGE

        searchTextFieldSearchToolBarLeftPanelDocument.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                // GET VALUE
                String search = searchTextFieldSearchToolBarLeftPanelDocument.getText();
                if (search.equals("")) {
                    jScrollPaneJPanelRightPanel.setViewportView(null);
                    search_Table_Model.clear();
                    Label_search_Info_Panel.setText(Lang.T("Enter more  2 characters"));
                    jScrollPanelLeftPanel.setViewportView(search_Info_Panel);
                    return;
                }
                if (search.length() < 3) {
                    Label_search_Info_Panel.setText(Lang.T("Enter more  2 characters"));
                    jScrollPanelLeftPanel.setViewportView(search_Info_Panel);
                    return;
                }
                key_Item.setText("");

                Label_search_Info_Panel.setText(Lang.T("Waiting..."));
                jScrollPanelLeftPanel.setViewportView(search_Info_Panel);
                new Thread() {
                    @Override
                    public void run() {
                        search_Table_Model.findByName(search);
                        if (search_Table_Model.getRowCount() < 1) {
                            Label_search_Info_Panel.setText(Lang.T("Not Found"));
                            jScrollPanelLeftPanel.setViewportView(search_Info_Panel);
                            jScrollPaneJPanelRightPanel.setViewportView(null);
                            return;
                        }
                        jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);
                        jTableJScrollPanelLeftPanel. getSelectionModel().addSelectionInterval(0, 0);
                    }
                }.start();
            }

        });

    }

}
