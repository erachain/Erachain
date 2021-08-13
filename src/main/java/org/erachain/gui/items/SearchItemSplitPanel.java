package org.erachain.gui.items;

import org.erachain.gui.library.MDecimalFormatedTextField;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class SearchItemSplitPanel extends ItemSplitPanel {

    private static final long serialVersionUID = 2717571093561259483L;
    protected SearchItemsTableModel search_Table_Model;
    public MDecimalFormatedTextField itemKey;
    public JButton buttonGetLasts = new JButton(Lang.T("Get Last"));


    @SuppressWarnings("rawtypes")
    public SearchItemSplitPanel(SearchItemsTableModel search_Table_Model1, String gui_Name, String search_Label_Text) {

        super(search_Table_Model1, gui_Name, search_Label_Text);
        this.search_Table_Model = search_Table_Model1;

        // CHECKBOX FOR FAVORITE
        TableColumn favorite_Column = jTableJScrollPanelLeftPanel.getColumnModel()
                .getColumn(search_Table_Model.COLUMN_FAVORITE);
        favorite_Column.setMaxWidth(150);

        // search Panel
        searchToolBar_LeftPanel.setVisible(true);
        searchToolBar_LeftPanel.add(new JLabel("  " + Lang.T("Find Key") + ":"));
        itemKey = new MDecimalFormatedTextField();
        itemKey.setToolTipText("");
        itemKey.setAlignmentX(1.0F);
        itemKey.setText("");
        itemKey.setMinimumSize(new Dimension(100, (int) (UIManager.getFont("Label.font").getSize() * 1.4)));
        itemKey.setMinimumSize(new Dimension(100, (int) (UIManager.getFont("Label.font").getSize() * 1.4)));
        itemKey.setPreferredSize(new Dimension(100, (int) (UIManager.getFont("Label.font").getSize() * 1.4)));

        MenuPopupUtil.installContextMenu(itemKey);

        itemKey.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                startSearchKey();
            }
        });

        searchToolBar_LeftPanel.add(itemKey, gridBagConstraints);

        ////toolBarLeftPanel.add(buttonGetLasts, gridBagConstraints);
        searchToolBar_LeftPanel.add(buttonGetLasts);
        buttonGetLasts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                startGetLast();
            }
        });


        // UPDATE FILTER ON TEXT CHANGE

        searchTextField2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                // GET VALUE
                startSearchName();
            }

        });

    }

    public void startSearchName() {
        String search = searchTextField2.getText();
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
        itemKey.setText("");

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
                jTableJScrollPanelLeftPanel.getSelectionModel().addSelectionInterval(0, 0);
            }
        }.start();
    }

    public void startSearchKey() {
        searchTextField2.setText("");
        Label_search_Info_Panel.setText(Lang.T("Waiting..."));
        jScrollPanelLeftPanel.setViewportView(search_Info_Panel);
        new Thread() {
            @Override
            public void run() {
                String seqNo = itemKey.getText();
                if (seqNo == null || seqNo.isEmpty()) {
                    search_Table_Model.getLast();
                } else {
                    search_Table_Model.findByKey(seqNo);
                }
                if (search_Table_Model.getRowCount() > 0) {
                    jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);
                    jTableJScrollPanelLeftPanel.getSelectionModel().addSelectionInterval(0, 0);
                    return;
                }
                Label_search_Info_Panel.setText(Lang.T("Not Found"));
                jScrollPanelLeftPanel.setViewportView(search_Info_Panel);
                jScrollPaneJPanelRightPanel.setViewportView(null);
            }
        }.start();
    }

    public void startGetLast() {
        Label_search_Info_Panel.setText(Lang.T("Waiting..."));
        jScrollPanelLeftPanel.setViewportView(search_Info_Panel);
        new Thread() {
            @Override
            public void run() {
                search_Table_Model.getLast();
                if (search_Table_Model.getRowCount() > 0) {
                    jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);
                    jTableJScrollPanelLeftPanel.getSelectionModel().addSelectionInterval(0, 0);
                    return;
                }
                Label_search_Info_Panel.setText(Lang.T("Not Found"));
                jScrollPanelLeftPanel.setViewportView(search_Info_Panel);
                jScrollPaneJPanelRightPanel.setViewportView(null);
            }
        }.start();
    }

}
