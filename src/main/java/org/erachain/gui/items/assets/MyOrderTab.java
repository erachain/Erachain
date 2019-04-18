package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.library.SetIntervalPanel;
import org.erachain.gui.models.WalletOrdersTableModel;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MyOrderTab extends SplitPanel {

    private static final long serialVersionUID = 1L;
    protected int row;
    /**
     *
     */
    WalletOrdersTableModel ordersModel;
    private SetIntervalPanel setIntervalPanel;

    @SuppressWarnings("rawtypes")
    public MyOrderTab() {
        super("MyOrderTab");
        this.setName(Lang.getInstance().translate("My Orders"));
        searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
        // not show buttons
        button1ToolBarLeftPanel.setVisible(false);
        button2ToolBarLeftPanel.setVisible(false);
        jButton1_jToolBar_RightPanel.setVisible(false);
        jButton2_jToolBar_RightPanel.setVisible(false);

        // set interval panel
        setIntervalPanel = new SetIntervalPanel(Controller.getInstance().wallet.database.getOrderMap(), Transaction.CREATE_ORDER_TRANSACTION);
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        leftPanel.add(setIntervalPanel, gridBagConstraints);

        setIntervalPanel.jButtonSetInterval.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                setInterval();
            }
        });

        // TABLE
        ordersModel = new WalletOrdersTableModel();
        // set interval
        setInterval();
        // table = new MTable(ordersModel);

        // add listener
        // jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(table);
        // show
        this.jTableJScrollPanelLeftPanel = new MTable(ordersModel);
        // this.jTableJScrollPanelLeftPanel = table;
        jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new search_listener());
        jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);

        // UPDATE FILTER ON TEXT CHANGE
        searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                onChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onChange();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                onChange();
            }

            public void onChange() {

                // SET FILTER

            }
        });

        // MENU
        JPopupMenu assetsMenu = new JPopupMenu();
        assetsMenu.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent arg0) {
                // TODO Auto-generated method stub
                row = jTableJScrollPanelLeftPanel.getSelectedRow();
                if (row < 1) {
                    return;
                }

                row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);

            }

            @Override
            public void ancestorMoved(AncestorEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void ancestorRemoved(AncestorEvent arg0) {
                // TODO Auto-generated method stub

            }

        });

        JMenuItem favorite = new JMenuItem(Lang.getInstance().translate("Exchange"));
        favorite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                favorite_set(jTableJScrollPanelLeftPanel);

            }
        });

        assetsMenu.addPopupMenuListener(new PopupMenuListener() {

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

                row = jTableJScrollPanelLeftPanel.getSelectedRow();
                row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);

            }

        }

        );

        assetsMenu.add(favorite);

        JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
        details.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // AssetCls asset = assetsModel.getAsset(row);
                // new AssetFrame(asset);
            }
        });
        // assetsMenu.add(details);
        JMenuItem dividend = new JMenuItem(Lang.getInstance().translate("Pay dividend"));
        dividend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // AssetCls asset = assetsModel.getAsset(row);
                // new PayDividendFrame(asset);
            }
        });
        assetsMenu.add(dividend);
        // table.setComponentPopupMenu(assetsMenu);
        TableMenuPopupUtil.installContextMenu(this.jTableJScrollPanelLeftPanel, assetsMenu); // SELECT
                                                                                               // ROW
                                                                                               // ON
                                                                                               // WHICH
                                                                                               // CLICKED
                                                                                               // RIGHT
                                                                                               // BUTTON

        // MOUSE ADAPTER
        this.jTableJScrollPanelLeftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = jTableJScrollPanelLeftPanel.rowAtPoint(p);
                jTableJScrollPanelLeftPanel.setRowSelectionInterval(row, row);
            }
        });

        this.jTableJScrollPanelLeftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = jTableJScrollPanelLeftPanel.rowAtPoint(p);
                jTableJScrollPanelLeftPanel.setRowSelectionInterval(row, row);
                /*
                 * if(e.getClickCount() == 2) { row =
                 * table.convertRowIndexToModel(row); AssetCls asset =
                 * assetsModel.getAsset(row); new AssetFrame(asset); }
                 * if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1) {
                 * 
                 * if (table.getSelectedColumn() ==
                 * WalletItemAssetsTableModel.COLUMN_FAVORITE){ row =
                 * table.convertRowIndexToModel(row); AssetCls asset =
                 * orderModel.getAsset(row); favoriteSet( table);
                 * 
                 * 
                 * 
                 * }
                 * 
                 * 
                 * }
                 */
            }
        });

    }

    public void onIssueClick() {
        new IssueAssetFrame();
    }

    public void onAllClick() {
        new AllAssetsFrame();
    }

    public void onMyOrdersClick() {
        new MyOrdersFrame();
    }

    public void favorite_set(JTable assetsTable) {
    }

    // CreateOrderDetailsFrame
    // listener select row
    class search_listener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent arg0) {
            try {
                Order order = null;
                int i = 0;
                if (jTableJScrollPanelLeftPanel.getSelectedRow() >= 0)
                    i = jTableJScrollPanelLeftPanel
                            .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow());
                order = ordersModel.getItem(i).b;
                if (order == null)
                    return;
                jScrollPaneJPanelRightPanel.setViewportView(new OrderInfoPanel(order));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void setInterval() {
        Integer start = 0;
        try {
            start = Integer.valueOf(setIntervalPanel.jTextFieldStart.getText());
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            return;
        }
        Integer end = 0;
        try {
            end = Integer.valueOf(setIntervalPanel.jTextFieldEnd.getText());
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            return;
        }
        if (end > start) {
            int step = end - start;
            ordersModel.setInterval(start, step);
            ordersModel.fireTableDataChanged();
        }
    }
    
    @Override
    public void onClose() {
        ordersModel.deleteObservers();
        setIntervalPanel.deleteObservers();
        
    }
    

}
