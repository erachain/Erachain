package org.erachain.gui.items;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.gui.MainFrame;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.RendererIcon;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;

public abstract class ItemSplitPanel extends SplitPanel {

    private static final long serialVersionUID = 2717571093561259483L;
    protected TimerTableModelCls tableModel;
    protected JMenuItem favoriteMenuItems;
    protected JPopupMenu menuTable;
    //protected ItemCls itemMenu;
    protected ItemCls itemTableSelected = null;
    protected static Logger logger = LoggerFactory.getLogger(ItemSplitPanel.class);


    @SuppressWarnings("rawtypes")
    public ItemSplitPanel(TimerTableModelCls tableModel, String guiName) {
        super(guiName);
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

        // иконку будем рисовать
        jTableJScrollPanelLeftPanel.getColumnModel().getColumn(tableModel.COLUMN_FOR_ICON)
                .setCellRenderer(new RendererIcon());

        TableColumnModel columnModel = jTableJScrollPanelLeftPanel.getColumnModel();
        columnModel.getColumn(0).setMaxWidth((100));

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
                logger.error(e.getMessage(),e);
                return;
            }
            if (itemTableSelected == null)  {
                return;
            }
            try {
                // TODO почемуто при выборе персоны сюда 2 раза прилетает и перерисовка дважды идет
                jScrollPaneJPanelRightPanel.setViewportView(getShow(itemTableSelected));
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                jScrollPaneJPanelRightPanel.setViewportView(null);

            }
            //	itemTableSelected = null;

        });

        // UPDATE FILTER ON TEXT CHANGE

        // mouse from favorine column
        jTableJScrollPanelLeftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point point = e.getPoint();
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
                    favoriteMenuItems.setText(Lang.getInstance().translate("Remove Favorite"));
                } else {
                    favoriteMenuItems.setText(Lang.getInstance().translate("Add Favorite"));
                }
            }

        });

        menuTable.add(favoriteMenuItems);

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
            int showConfirmDialog = JOptionPane.showConfirmDialog(MainFrame.getInstance(), Lang.getInstance().translate("Delete from favorite") + "?", Lang.getInstance().translate("Delete from favorite"), JOptionPane.OK_CANCEL_OPTION);
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
        Object item = ItemSplitPanel.this.tableModel.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(row));
        if (item instanceof Fun.Tuple2) {
            return (ItemCls) ((Fun.Tuple2)item).b;
        } else {
            return (ItemCls)item;
        }
    }

    protected void tableMouse2Click(ItemCls item) {
    }

}
