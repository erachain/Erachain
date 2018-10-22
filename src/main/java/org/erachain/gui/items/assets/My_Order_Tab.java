package org.erachain.gui.items.assets;

import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.Split_Panel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.library.SetIntervalPanel;
import org.erachain.gui.models.WalletItemAssetsTableModel;
import org.erachain.gui.models.WalletOrdersTableModel;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.BigInteger;

public class My_Order_Tab extends Split_Panel {

    private static final long serialVersionUID = 1L;
    protected int row;
    /**
     *
     */
    WalletOrdersTableModel ordersModel;
    private SetIntervalPanel setIntervalPanel;

    @SuppressWarnings("rawtypes")
    public My_Order_Tab() {
        super("My_Order_Tab");
        this.setName(Lang.getInstance().translate("My Orders"));
        searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
        // not show buttons
        button1_ToolBar_LeftPanel.setVisible(false);
        button2_ToolBar_LeftPanel.setVisible(false);
        jButton1_jToolBar_RightPanel.setVisible(false);
        jButton2_jToolBar_RightPanel.setVisible(false);

        // set interval panel
        setIntervalPanel = new SetIntervalPanel(Transaction.CREATE_ORDER_TRANSACTION);
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
        // jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(table);
        // show
        this.jTable_jScrollPanel_LeftPanel = new MTable(ordersModel);
        // this.jTable_jScrollPanel_LeftPanel = table;
        jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());
        jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);

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
                row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
                if (row < 1) {
                    return;
                }

                row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);

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

                favorite_set(jTable_jScrollPanel_LeftPanel);

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

                row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
                row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);

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
        TableMenuPopupUtil.installContextMenu(this.jTable_jScrollPanel_LeftPanel, assetsMenu); // SELECT
                                                                                               // ROW
                                                                                               // ON
                                                                                               // WHICH
                                                                                               // CLICKED
                                                                                               // RIGHT
                                                                                               // BUTTON

        // MOUSE ADAPTER
        this.jTable_jScrollPanel_LeftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = jTable_jScrollPanel_LeftPanel.rowAtPoint(p);
                jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(row, row);
            }
        });

        this.jTable_jScrollPanel_LeftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = jTable_jScrollPanel_LeftPanel.rowAtPoint(p);
                jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(row, row);
                /*
                 * if(e.getClickCount() == 2) { row =
                 * table.convertRowIndexToModel(row); AssetCls asset =
                 * assetsModel.getAsset(row); new AssetFrame(asset); }
                 * if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1) {
                 * 
                 * if (table.getSelectedColumn() ==
                 * WalletItemAssetsTableModel.COLUMN_FAVORITE){ row =
                 * table.convertRowIndexToModel(row); AssetCls asset =
                 * orderModel.getAsset(row); favorite_set( table);
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
                if (jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0)
                    i = jTable_jScrollPanel_LeftPanel
                            .convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow());
                order = ordersModel.getOrder(i);
                if (order == null)
                    return;
                jScrollPane_jPanel_RightPanel.setViewportView(new Order_Info_Panel(order));
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
    public void delay_on_close() {
        ordersModel.removeObservers();
        setIntervalPanel.removeObservers();
        
    }
    

}
