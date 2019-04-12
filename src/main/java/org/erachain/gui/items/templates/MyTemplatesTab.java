package org.erachain.gui.items.templates;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.WalletItemAssetsTableModel;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;

public class MyTemplatesTab extends SplitPanel {

    private static final long serialVersionUID = 1L;
    final MTable table;
    protected int row;
    /**
     *
     */
    WalletItemAssetsTableModel assetsModel;
    RowSorter<WalletItemAssetsTableModel> sorter;

    public MyTemplatesTab() {
        super("MyTemplatesTab");

        this.setName("My Template");
        searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
        // not show buttons
        button1ToolBarLeftPanel.setVisible(false);
        button2ToolBarLeftPanel.setVisible(false);
        jButton1_jToolBar_RightPanel.setVisible(false);
        jButton2_jToolBar_RightPanel.setVisible(false);

        //TABLE
        assetsModel = new WalletItemAssetsTableModel();
        table = new MTable(assetsModel);
        //assetsModel.getAsset(row)
        //POLLS SORTER
        sorter = new TableRowSorter<WalletItemAssetsTableModel>(assetsModel);
        table.setRowSorter(sorter);
        //	Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
        //	CoreRowSorter sorter = new CoreRowSorter(assetsModel, indexes);
        //	table.setRowSorter(sorter);

        //CHECKBOX FOR DIVISIBLE
        //	TableColumn divisibleColumn = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_DIVISIBLE);
        //	divisibleColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));

        //CHECKBOX FOR CONFIRMED
        //	TableColumn confirmedColumn = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_CONFIRMED);
        //	confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));

        //CHECKBOX FOR FAVORITE
        //	TableColumn favoriteColumn = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_FAVORITE);
        //	favoriteColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));


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
        TableColumn columnM = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_ASSET_TYPE);//.COLUMN_CONFIRMED);
        columnM.setMinWidth(50);
        columnM.setMaxWidth(1000);
        columnM.setPreferredWidth(50);
        // column #1
        TableColumn column4 = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_FAVORITE);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
        column4.setMinWidth(50);
        column4.setMaxWidth(1000);
        column4.setPreferredWidth(50);


        // add listener
        //		jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(table);
        // show
        this.jTableJScrollPanelLeftPanel.setModel(assetsModel);
        this.jTableJScrollPanelLeftPanel = table;
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

                // GET VALUE
                String search = searchTextField_SearchToolBar_LeftPanel.getText();

                // SET FILTER
                assetsModel.fireTableDataChanged();
                RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
                ((DefaultRowSorter) sorter).setRowFilter(filter);
                assetsModel.fireTableDataChanged();

            }
        });


        this.jTableJScrollPanelLeftPanel.addComponentListener(new ComponentListener() {

            @Override
            public void componentHidden(ComponentEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void componentMoved(ComponentEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void componentResized(ComponentEvent arg0) {
                // TODO Auto-generated method stub

                //	Table_Render("2", pair_Panel.jTableJScrollPanelLeftPanel);

                //		new Table_Formats().Table_Row_Auto_Height(table);

            }

            @Override
            public void componentShown(ComponentEvent arg0) {
                // TODO Auto-generated method stub

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
                    assetsMenu.disable();
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

        JMenuItem sell = new JMenuItem(Lang.getInstance().translate("To sell"));
        sell.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = assetsModel.getItem(row);
                //	String account = assetsModel..getAccount(row);
                //	AssetPairSelect a = new AssetPairSelect(asset.getKey(), "To sell", "");


            }
        });


        JMenuItem excahge = new JMenuItem(Lang.getInstance().translate("Exchange"));
        excahge.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = assetsModel.getItem(row);
                //		new AssetPairSelect(asset.getKey(), "","");
            }
        });
        assetsMenu.add(excahge);


        JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
        buy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = assetsModel.getItem(row);
                //		new AssetPairSelect(asset.getKey(), "Buy","");
            }
        });

        assetsMenu.addSeparator();
        assetsMenu.add(buy);

        assetsMenu.add(sell);
        assetsMenu.addSeparator();


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
                                                AssetCls asset = assetsModel.getItem(row);

                                                //IF ASSET CONFIRMED AND NOT ERM

                                                favorite.setVisible(true);
                                                //CHECK IF FAVORITES
                                                if (Controller.getInstance().isItemFavorite(asset)) {
                                                    favorite.setText(Lang.getInstance().translate("Remove Favorite"));
                                                } else {
                                                    favorite.setText(Lang.getInstance().translate("Add Favorite"));
                                                }
				/*
				//this.favoritesButton.setPreferredSize(new Dimension(200, 25));
				this.favoritesButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						onFavoriteClick();
					}
				});
				this.add(this.favoritesButton, labelGBC);
				 */


                                            }

                                        }

        );


        assetsMenu.add(favorite);


        JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
        details.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = assetsModel.getItem(row);
                //			new AssetFrame(asset);
            }
        });
        //	assetsMenu.add(details);
        JMenuItem dividend = new JMenuItem(Lang.getInstance().translate("Pay dividend"));
        dividend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = assetsModel.getItem(row);
                //		new PayDividendFrame(asset);
            }
        });
        assetsMenu.add(dividend);
     //   table.setComponentPopupMenu(assetsMenu);
        TableMenuPopupUtil.installContextMenu(table, assetsMenu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON


        //MOUSE ADAPTER
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                row = table.rowAtPoint(p);
                table.setRowSelectionInterval(row, row);
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                row = table.rowAtPoint(p);
                table.setRowSelectionInterval(row, row);

                if (e.getClickCount() == 2) {
                    row = table.convertRowIndexToModel(row);
                    AssetCls asset = assetsModel.getItem(row);
                    //				new AssetPairSelect(asset.getKey(), "","");
                    //		new AssetFrame(asset);
                }
                if (e.getClickCount() == 1 & e.getButton() == e.BUTTON1) {

                    if (table.getSelectedColumn() == WalletItemAssetsTableModel.COLUMN_FAVORITE) {
                        row = table.convertRowIndexToModel(row);
                        AssetCls asset = assetsModel.getItem(row);
                        favorite_set(table);


                    }


                }
            }
        });


    }

    public void onIssueClick() {
        //	new IssueAssetFrame();
    }

    public void onAllClick() {
        //	new AllAssetsFrame();
    }

    public void onMyOrdersClick() {
        //	new MyOrdersFrame();
    }

    public void favorite_set(JTable assetsTable) {


        AssetCls asset = assetsModel.getItem(row);
        //new AssetPairSelect(asset.getKey());

        if (asset.getKey() >= AssetCls.INITIAL_FAVORITES) {
            //CHECK IF FAVORITES
            if (Controller.getInstance().isItemFavorite(asset)) {

                Controller.getInstance().removeItemFavorite(asset);
            } else {

                Controller.getInstance().addItemFavorite(asset);
            }


            assetsTable.repaint();

        }
    }


}
