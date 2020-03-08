package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.library.MainPanelInterface;
import org.erachain.gui.models.RendererIcon;
import org.erachain.gui.models.WalletItemAssetsTableModel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.TableMenuPopupUtil;
import org.erachain.utils.URLViewer;
import org.mapdb.Fun;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;

public class MyAssetsTab extends SplitPanel implements MainPanelInterface {

    private String iconFile = "images/pageicons/MyAssetsTab.png";
    private static final long serialVersionUID = 1L;
    final MTable table;
    protected int row;
    /**
     *
     */
    WalletItemAssetsTableModel assetsModel;
    RowSorter<WalletItemAssetsTableModel> sorter;
    //private MyAssetsTab th;

    public MyAssetsTab() {
        super("MyAssetsTab");
        //th = this;
        this.setName(Lang.getInstance().translate("My Assets"));
        searthLabelSearchToolBarLeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
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

        // иконку будем рисовать
        table.getColumnModel().getColumn(assetsModel.COLUMN_FOR_ICON)
                .setCellRenderer(new RendererIcon());

        // add listener
        //		jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(table);
        // show
        /// this.jTableJScrollPanelLeftPanel.setModel(assetsModel);

        this.jTableJScrollPanelLeftPanel = table;

        jTableJScrollPanelLeftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point point = e.getPoint();
                int row = jTableJScrollPanelLeftPanel.rowAtPoint(point);
                jTableJScrollPanelLeftPanel.setRowSelectionInterval(row, row);

                Fun.Tuple2<Long, AssetCls> itemTableSelected = assetsModel.getItem(row);

                if (e.getClickCount() == 2) {
                    tableMouse2Click(itemTableSelected.b);
                }

            }
        });

        jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);

        jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new search_listener());

        // UPDATE FILTER ON TEXT CHANGE
        searchTextFieldSearchToolBarLeftPanelDocument.getDocument().addDocumentListener(new DocumentListener() {

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
                String search = searchTextFieldSearchToolBarLeftPanelDocument.getText();

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

                //	new Table_Formats().Table_Row_Auto_Height(table);

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
                AssetCls asset = assetsModel.getItem(row).b;
                MainPanel.getInstance().insertTab(new ExchangePanel(asset, null, "To sell", ""));

            }


        });

        JMenuItem excahge = new JMenuItem(Lang.getInstance().translate("Exchange"));
        excahge.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = assetsModel.getItem(row).b;
                MainPanel.getInstance().insertTab(new ExchangePanel(asset, null, "", ""));
            }
        });
        assetsMenu.add(excahge);

        JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
        buy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = assetsModel.getItem(row).b;
                MainPanel.getInstance().insertTab(new ExchangePanel(asset, null, "Buy", ""));

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

                                                int row = table.getSelectedRow();
                                                row = table.convertRowIndexToModel(row);
                                                AssetCls asset = assetsModel.getItem(row).b;

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
                AssetCls asset = assetsModel.getItem(row).b;
                //			new AssetFrame(asset);
            }
        });
        //	assetsMenu.add(details);
        JMenuItem dividend = new JMenuItem(Lang.getInstance().translate("Pay dividend"));
        dividend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = assetsModel.getItem(row).b;
                new PayDividendFrame(asset);
            }
        });
        assetsMenu.add(dividend);

        assetsMenu.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetCls asset = assetsModel.getItem(row).b;

                try {
                    URLViewer.openWebpage(new URL("http://" + Settings.getInstance().getBlockexplorerURL()
                            + ":" + Settings.getInstance().getWebPort() + "/index/blockexplorer.html"
                            + "?asset=" + asset.getKey()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });

        assetsMenu.add(setSeeInBlockexplorer);

        //     table.setComponentPopupMenu(assetsMenu);
        TableMenuPopupUtil.installContextMenu(table, assetsMenu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON


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
                row = table.rowAtPoint(p);
                table.setRowSelectionInterval(row, row);

                if (e.getClickCount() == 2) {
                    row = table.convertRowIndexToModel(row);
                    AssetCls asset = assetsModel.getItem(row).b;
                    MainPanel.getInstance().insertTab(new ExchangePanel(asset, null, "", ""));

                    //		new AssetFrame(asset);
                }
                if (e.getClickCount() == 1 & e.getButton() == e.BUTTON1) {

                    if (table.getSelectedColumn() == WalletItemAssetsTableModel.COLUMN_FAVORITE) {
                        row = table.convertRowIndexToModel(row);
                        AssetCls asset = assetsModel.getItem(row).b;
                        favorite_set(table);


                    }


                }
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


        AssetCls asset = assetsModel.getItem(row).b;
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

    @Override
    public void onClose() {
        // delete observer left panel
        assetsModel.deleteObservers();
        // get component from right panel
        Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        if (c1 instanceof AssetInfo) ((AssetInfo) c1).delay_on_Close();

    }

    //listener select row
    class search_listener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent arg0) {
            AssetCls asset = null;
            if (table.getSelectedRow() >= 0)
                asset = assetsModel.getItem(table.convertRowIndexToModel(table.getSelectedRow())).b;
            if (asset == null) return;
            //AssetDetailsPanel001 info_panel = new AssetDetailsPanel001(asset);
            //info_panel.setPreferredSize(new Dimension(jScrollPaneJPanelRightPanel.getSize().width-50,jScrollPaneJPanelRightPanel.getSize().height-50));
            int div = jSplitPanel.getDividerLocation();
            int or = jSplitPanel.getOrientation();
            AssetInfo info_panel = new AssetInfo(asset, false);
            //info_panel.setPreferredSize(new Dimension(jScrollPaneJPanelRightPanel.getSize().width-50,jScrollPaneJPanelRightPanel.getSize().height-50));
            jScrollPaneJPanelRightPanel.setViewportView(info_panel);
            //jSplitPanel.setRightComponent(info_panel);
            jSplitPanel.setDividerLocation(div);
            jSplitPanel.setOrientation(or);

        }
    }

    protected void tableMouse2Click(ItemCls item) {

        AssetCls asset = (AssetCls) item;
        AssetCls assetSell = Settings.getInstance().getDefaultPairAsset();
        String action = null;
        ExchangePanel panel = new ExchangePanel(asset, assetSell, action, "");
        panel.setName(asset.getTickerName() + "/" + assetSell.getTickerName());
        MainPanel.getInstance().insertTab(panel);
    }
    @Override
    public Image getIcon() {
        {
            try {
                return Toolkit.getDefaultToolkit().getImage(iconFile);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
