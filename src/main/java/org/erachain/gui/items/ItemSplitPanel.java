package org.erachain.gui.items;

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.WalletTableRenderer;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.gui.records.toSignRecordDialog;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.TableMenuPopupUtil;
import org.erachain.utils.URLViewer;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
//import java.util.TimerTask;

public abstract class ItemSplitPanel extends SplitPanel {

    private static final long serialVersionUID = 2717571093561259483L;
    protected TimerTableModelCls tableModel;
    protected JMenuItem favoriteMenuItems;
    protected JPopupMenu menuTable;
    //protected ItemCls itemMenu;
    protected ItemCls itemTableSelected;
    protected static Logger logger = LoggerFactory.getLogger(ItemSplitPanel.class);


    @SuppressWarnings("rawtypes")
    public ItemSplitPanel(TimerTableModelCls tableModel, String guiName, String title) {
        super(guiName, title);

        this.tableModel = tableModel;
        // not show buttons
        jToolBarRightPanel.setVisible(false);
        toolBarLeftPanel.setVisible(true);
        button1ToolBarLeftPanel.setVisible(false);
        button2ToolBarLeftPanel.setVisible(false);

        // not show My filter
        searchMyJCheckBoxLeftPanel.setVisible(false);
        searchFavoriteJCheckBoxLeftPanel.setVisible(false);

        // CREATE TABLE
        jTableJScrollPanelLeftPanel = new MTable(this.tableModel);
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Boolean.class, new WalletTableRenderer());
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Object.class, new WalletTableRenderer());
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Number.class, new WalletTableRenderer());

        TableColumnModel columnModel = jTableJScrollPanelLeftPanel.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth((100));
        columnModel.getColumn(0).setMaxWidth((150));
        columnModel.getColumn(tableModel.COLUMN_FAVORITE).setPreferredWidth(70);
        columnModel.getColumn(tableModel.COLUMN_FAVORITE).setMaxWidth(100);

        // hand cursor for Favorite column
        jTableJScrollPanelLeftPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                if (jTableJScrollPanelLeftPanel.columnAtPoint(e.getPoint()) == ItemSplitPanel.this.tableModel.COLUMN_FAVORITE) {
                    jTableJScrollPanelLeftPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    jTableJScrollPanelLeftPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        // select row
        jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(arg0 -> {

            if (jTableJScrollPanelLeftPanel == null || jTableJScrollPanelLeftPanel.getSelectedRow() < 0) {
                jScrollPaneJPanelRightPanel.setViewportView(null);
                return;
            }

            try {
                itemTableSelected = getItem(jTableJScrollPanelLeftPanel.getSelectedRow());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return;
            }
            if (itemTableSelected == null) {
                return;
            }
            try {
                // TODO почемуто при выборе персоны сюда 2 раза прилетает и перерисовка дважды идет
                jScrollPaneJPanelRightPanel.setViewportView(null);
                jScrollPaneJPanelRightPanel.setViewportView(getShow(itemTableSelected));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                try {
                    jScrollPaneJPanelRightPanel.setViewportView(null);
                    jScrollPaneJPanelRightPanel.setViewportView(getShow(itemTableSelected));
                } catch (Exception e1) {
                    jScrollPaneJPanelRightPanel.setViewportView(null);
                    jScrollPaneJPanelRightPanel.setViewportView(getShow(itemTableSelected));
                }

            }

        });

        // UPDATE FILTER ON TEXT CHANGE

        // mouse from favorine column
        jTableJScrollPanelLeftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                Point point = e.getPoint();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                    int row = jTableJScrollPanelLeftPanel.rowAtPoint(point);
                    jTableJScrollPanelLeftPanel.setRowSelectionInterval(row, row);

                    itemTableSelected = getItem(row);

                    if (e.getClickCount() == 2) {
                        tableMouse2Click(itemTableSelected);
                    }

                    if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
                        if (jTableJScrollPanelLeftPanel.getSelectedColumn() == ItemSplitPanel.this.tableModel.COLUMN_FAVORITE) {
                            favoriteSet(itemTableSelected);
                        }
                    }
                }}, 10);
            }
        });

        menuTable = new JPopupMenu();
        // favorite menu
        favoriteMenuItems = new JMenuItem();
        favoriteMenuItems.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = jTableJScrollPanelLeftPanel.getSelectedRow();
                favoriteSet(getItem(row));
            }
        });

        menuTable.addPopupMenuListener(new PopupMenuListener()
         {

            @Override
            public void popupMenuCanceled(PopupMenuEvent arg0) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
                itemTableSelected = getItem(jTableJScrollPanelLeftPanel.getSelectedRow());
                // IF ASSET CONFIRMED AND NOT ERM
                favoriteMenuItems.setVisible(true);
                // CHECK IF FAVORITES
                if (Controller.getInstance().isItemFavorite(itemTableSelected)) {
                    favoriteMenuItems.setText(Lang.T("Remove Favorite"));
                } else {
                    favoriteMenuItems.setText(Lang.T("Add Favorite"));
                }
            }

         });

        menuTable.add(favoriteMenuItems);

        JMenuItem vouchMenu = new JMenuItem(Lang.T("Sign / Vouch"));
        vouchMenu.addActionListener(e -> {
            DCSet db = DCSet.getInstance();
            Transaction transaction = db.getTransactionFinalMap().get(itemTableSelected.getReference());
            new toSignRecordDialog(transaction.getBlockHeight(), transaction.getSeqNo());

        });
        menuTable.add(vouchMenu);

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.T("Check in Blockexplorer"));
        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    URLViewer.openWebpage(new URL(Settings.getInstance().getBlockexplorerURL()
                            + "/index/blockexplorer.html"
                            + "?" + itemTableSelected.getItemTypeName() + "=" + itemTableSelected.getKey()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });

        menuTable.add(setSeeInBlockexplorer);

        JMenuItem byteCode = new JMenuItem(Lang.T("Get bytecode"));
        byteCode.addActionListener(e -> {
            String base58str = Base58.encode(itemTableSelected.toBytes(false, false));
            StringSelection stringSelection = new StringSelection(base58str);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Bytecode of the '%1' has been copy to buffer")
                            .replace("%1", itemTableSelected.getName())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuTable.add(byteCode);

        JMenuItem byteKey = new JMenuItem(Lang.T("Get Number"));
        byteKey.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection("" + itemTableSelected.getKey());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Number of the '%1' has been copy to buffer")
                            .replace("%1", itemTableSelected.getName())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuTable.add(byteKey);

        TableMenuPopupUtil.installContextMenu(jTableJScrollPanelLeftPanel, menuTable);
        jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);

    }

    @Override
    public void onClose() {
        tableModel.deleteObservers();
    }

    private void favoriteSet(ItemCls itemCls) {
        // CHECK IF FAVORITES
        if (Controller.getInstance().isItemFavorite(itemCls)) {
            int showConfirmDialog = JOptionPane.showConfirmDialog(MainFrame.getInstance(), Lang.T("Delete from favorite") + "?", Lang.T("Delete from favorite"), JOptionPane.OK_CANCEL_OPTION);
            if (showConfirmDialog == 0) {
                Controller.getInstance().removeItemFavorite(itemCls);
            }
        } else {
            Controller.getInstance().addItemFavorite(itemCls);
        }
        ((TimerTableModelCls) jTableJScrollPanelLeftPanel.getModel()).fireTableDataChanged();

    }

    abstract protected Component getShow(ItemCls item);


    private ItemCls getItem(int row) {
        int crow = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
        Object item = tableModel.getItem(crow);
        ItemCls itemCls;
        if (item instanceof Fun.Tuple2) {
            itemCls = (ItemCls) ((Fun.Tuple2) item).b;
        } else {
            itemCls = (ItemCls) item;
        }

        itemCls.getKey(DCSet.getInstance());
        return itemCls;

    }

    protected void tableMouse2Click(ItemCls item) {
    }

}
