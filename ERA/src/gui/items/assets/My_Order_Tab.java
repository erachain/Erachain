package gui.items.assets;

import gui.Split_Panel;
import gui.library.MTable;
import gui.models.WalletItemAssetsTableModel;
import gui.models.WalletOrdersTableModel;
import lang.Lang;
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
    @SuppressWarnings("rawtypes")
    final MTable table;
    protected int row;
    /**
     *
     */
    WalletOrdersTableModel ordersModel;

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

        //TABLE
        ordersModel = new WalletOrdersTableModel();
        table = new MTable(ordersModel);


        // column #1
        TableColumn column1 = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_KEY);//.COLUMN_CONFIRMED);
        column1.setMinWidth(1);
        column1.setMaxWidth(1000);
        column1.setPreferredWidth(50);
        // column #1
        TableColumn column2 = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_CONFIRMED);//.COLUMN_CONFIRMED);
        column2.setMinWidth(50);
        column2.setMaxWidth(1000);
        column2.setPreferredWidth(50);
        // column #1
        TableColumn column3 = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_ASSET_TYPE);//.COLUMN_CONFIRMED);
        column3.setMinWidth(50);
        column3.setMaxWidth(1000);
        column3.setPreferredWidth(50);
        // column #1
        TableColumn column4 = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_FAVORITE);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
        column4.setMinWidth(50);
        column4.setMaxWidth(1000);
        column4.setPreferredWidth(50);


        // add listener
        //		jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(table);
        // show
        this.jTable_jScrollPanel_LeftPanel.setModel(ordersModel);
        this.jTable_jScrollPanel_LeftPanel = table;
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


        //MENU
        JPopupMenu assetsMenu = new JPopupMenu();
        assetsMenu.addAncestorListener(new AncestorListener() {


            @Override
            public void ancestorAdded(AncestorEvent arg0) {
                // TODO Auto-generated method stub
                row = table.getSelectedRow();
                if (row < 1) {
                    return;
                }

                row = table.convertRowIndexToModel(row);


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

                favorite_set(table);

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

                                                row = table.getSelectedRow();
                                                row = table.convertRowIndexToModel(row);

                                            }

                                        }

        );


        assetsMenu.add(favorite);


        JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
        details.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //		AssetCls asset = assetsModel.getAsset(row);
                //			new AssetFrame(asset);
            }
        });
        //	assetsMenu.add(details);
        JMenuItem dividend = new JMenuItem(Lang.getInstance().translate("Pay dividend"));
        dividend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //		AssetCls asset = assetsModel.getAsset(row);
                //		new PayDividendFrame(asset);
            }
        });
        assetsMenu.add(dividend);
        table.setComponentPopupMenu(assetsMenu);

        //MOUSE ADAPTER
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                table.setRowSelectionInterval(row, row);
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                table.setRowSelectionInterval(row, row);
				/*
			if(e.getClickCount() == 2)
			{
				row = table.convertRowIndexToModel(row);
				AssetCls asset = assetsModel.getAsset(row);
				new AssetFrame(asset);
			}
			if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1)
			{

				if (table.getSelectedColumn() == WalletItemAssetsTableModel.COLUMN_FAVORITE){
					row = table.convertRowIndexToModel(row);
					AssetCls asset = orderModel.getAsset(row);
					favorite_set( table);



				}


			}
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

    //CreateOrderDetailsFrame
    //listener select row
    class search_listener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent arg0) {
            Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
                    Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = null;
            if (table.getSelectedRow() >= 0)
                order = ordersModel.getOrder(table.convertRowIndexToModel(table.getSelectedRow()));
            if (order == null) return;
            jScrollPane_jPanel_RightPanel.setViewportView(new Order_Info_Panel(order));
        }
    }

}
