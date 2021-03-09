package org.erachain.gui.items.other;

import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.datachain.DCSet;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.library.WalletOrphanButton;
import org.erachain.gui.library.WalletSyncButton;
import org.erachain.gui.models.BlocksTableModel;
import org.erachain.gui.models.PeersTableModel;
import org.erachain.gui.models.WalletBlocksTableModel;
import org.erachain.lang.Lang;
import org.erachain.network.Peer;
import org.erachain.settings.Settings;
import org.erachain.utils.TableMenuPopupUtil;
import org.erachain.utils.URLViewer;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

public class OtherSplitPanel extends SplitPanel implements Observer {

    public static String NAME = "OtherSplitPanel";
    public static String TITLE = "Dashboard";

    private PeersTableModel peersTableModel = new PeersTableModel();
    private JPanel jPanel2 = new JPanel();
    private GridBagConstraints gridBagConstraints;
    private JLabel jLabelPeerTitle = new JLabel();
    private JScrollPane jScrollPanePeersTable = new JScrollPane();
    private BlocksTableModel allBlocksTableModel = new BlocksTableModel();
    private MTable jTableAllBlock;

    // TRANSACTIONS
    private WalletBlocksTableModel myBlocksModel = new WalletBlocksTableModel();
    private MTable jTableMyBlock;
    private JPopupMenu peersMenu = new JPopupMenu();
    private Peer itemPeerMenu;
    private WalletSyncButton syncButton = new WalletSyncButton();
    private WalletOrphanButton returnButton = new WalletOrphanButton();
    private JMenuItem connectItem;
    private final JLabel jLabelAllBlocksSum;

    public OtherSplitPanel() {
        super(NAME, TITLE);
        DCSet.getInstance().getBlockMap().addObserver(this);
        jTableJScrollPanelLeftPanel.setModel(peersTableModel);
        jTableJScrollPanelLeftPanel.setAutoCreateRowSorter(true);

        // hand cursor for Favorite column
        jTableJScrollPanelLeftPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {

                if (jTableJScrollPanelLeftPanel.columnAtPoint(e.getPoint()) == peersTableModel.COLUMN_ADDRESS) {
                    int row = jTableJScrollPanelLeftPanel.getSelectedRow();
                    if (row >= 0) {
                        int convertRowIndexToModel = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                        itemPeerMenu = peersTableModel.getItem(convertRowIndexToModel);
                        if (itemPeerMenu.getWEBPort() != null) {
                            jTableJScrollPanelLeftPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                            return;
                        }
                    }
                }
                jTableJScrollPanelLeftPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        jTableJScrollPanelLeftPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    //GET ROW
                    if (jTableJScrollPanelLeftPanel.columnAtPoint(e.getPoint()) == peersTableModel.COLUMN_ADDRESS) {
                        int row = jTableJScrollPanelLeftPanel.getSelectedRow();
                        if (row >= 0) {
                            int convertRowIndexToModel = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                            itemPeerMenu = peersTableModel.getItem(convertRowIndexToModel);
                            if (itemPeerMenu.getWEBPort() != null) {
                                try {
                                    String url = itemPeerMenu.getScheme()
                                            + "://" + itemPeerMenu.getHostName()
                                            + ":" + itemPeerMenu.getWEBPort()
                                            + "/index/blockexplorer.html";
                                    URLViewer.openWebpage(new URL(url));
                                } catch (MalformedURLException e1) {
                                    logger.error(e1.getMessage(), e1);
                                }
                            }
                        }
                    }
                }
            }
        });

        peersMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuCanceled(PopupMenuEvent arg0) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
                int row = jTableJScrollPanelLeftPanel.getSelectedRow();
                int convertRowIndexToModel = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                itemPeerMenu = peersTableModel.getItem(convertRowIndexToModel);
            }
        });

        connectItem = new JMenuItem(Lang.T("Connect"));
        connectItem.addActionListener(arg0 -> {
            // чтобы развязат задержку и не тормозить GUI
            new Thread(() -> {
                Controller.getInstance().network.addPeer(itemPeerMenu, 0); // reset BAN if exists
                itemPeerMenu.connect(null, Controller.getInstance().network,
                        "connected as recircled by USER!!! ");
            }).start();
        });

        peersMenu.add(connectItem);

        addMenuBan("Ban in 10 min.", 10);

        addMenuBan("Ban in 60 min.", 60);

        addMenuBan("Ban in 3 hours", 180);

        addMenuBan("Ban in 24 hours", 1440);

        addMenuBan("Ban always", Integer.MAX_VALUE);

        peersMenu.add(new JSeparator());

        TableMenuPopupUtil.installContextMenu(jTableJScrollPanelLeftPanel, peersMenu);

        toolBarLeftPanel.setVisible(false);
        jToolBarRightPanel.setVisible(false);

        jPanel2.setLayout(new GridBagLayout());

        jLabelPeerTitle.setText(Lang.T("Peers"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(9, 11, 0, 11);
        jPanel2.add(jLabelPeerTitle, gridBagConstraints);

        jScrollPanePeersTable.setBorder(null);

        jScrollPanePeersTable.setViewportView(jTableJScrollPanelLeftPanel);
        jTableJScrollPanelLeftPanel.setOpaque(false);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new Insets(7, 11, 11, 11);
        jPanel2.add(jScrollPanePeersTable, gridBagConstraints);

        leftPanel.removeAll();
        jSplitPanel.setLeftComponent(jPanel2);

        jTableAllBlock = new MTable(allBlocksTableModel);

        jTableMyBlock = new MTable(myBlocksModel);

        JPanel jPanel7 = new JPanel(new GridBagLayout());
        JLabel jLabelMyBlockTitle = new JLabel(Lang.T("My Generated Blocks"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(10, 11, 0, 11);
        jPanel7.add(jLabelMyBlockTitle, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new Insets(10, 11, 0, 11);
        jPanel7.add(syncButton, gridBagConstraints);

        JScrollPane jScrollPaneMyBlockTable = new JScrollPane();
        jScrollPaneMyBlockTable.setBorder(null);

        jScrollPaneMyBlockTable.setViewportView(jTableMyBlock);

        JPopupMenu menuMy = new JPopupMenu();

        JMenuItem setSeeInBlockexplorerMy = new JMenuItem(Lang.T("Check in Blockexplorer"));

        setSeeInBlockexplorerMy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Block.BlockHead blockHead = myBlocksModel.getItem(jTableMyBlock
                        .convertRowIndexToModel(jTableMyBlock.getSelectedRow()));
                if (blockHead == null) {
                    return;
                }

                try {
                    URLViewer.openWebpage(new URL(Settings.getInstance().getBlockexplorerURL()
                            + "/index/blockexplorer.html"
                            + "?block=" + blockHead.heightBlock));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });

        menuMy.add(setSeeInBlockexplorerMy);
        TableMenuPopupUtil.installContextMenu(jTableMyBlock, menuMy);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new Insets(5, 11, 11, 11);
        jPanel7.add(jScrollPaneMyBlockTable, gridBagConstraints);

        JSplitPane jSplitPane5 = new JSplitPane();
        jSplitPane5.setLeftComponent(jPanel7);
        JPanel jPanel8 = new JPanel(new GridBagLayout());

        JPanel jPanelLabelsAllBlocks = new JPanel(new BorderLayout());
        JLabel jLabelAllBlockCaption = new JLabel(Lang.T("Last 100 blocks") + ". " +
                Lang.T("Sum win value chain blocks:"));
        jLabelAllBlocksSum = new JLabel(String.valueOf(Controller.getInstance().getBlockChain().
                getFullWeight(DCSet.getInstance())));

        jPanelLabelsAllBlocks.add(jLabelAllBlockCaption, BorderLayout.CENTER);
        jPanelLabelsAllBlocks.add(jLabelAllBlocksSum, BorderLayout.EAST);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(11, 11, 0, 11);
        jPanel8.add(jPanelLabelsAllBlocks, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(11, 11, 0, 11);
        jPanel8.add(returnButton, gridBagConstraints);

        JScrollPane jScrollPaneAllBlockTable = new JScrollPane();
        jScrollPaneAllBlockTable.setBorder(null);
        jScrollPaneAllBlockTable.setViewportView(jTableAllBlock);

        JPopupMenu menuAll = new JPopupMenu();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.T("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Block.BlockHead blockHead = allBlocksTableModel.getItem(jTableAllBlock
                        .convertRowIndexToModel(jTableAllBlock.getSelectedRow()));
                if (blockHead == null) {
                    return;
                }

                try {
                    URLViewer.openWebpage(new URL(Settings.getInstance().getBlockexplorerURL()
                            + "/index/blockexplorer.html"
                            + "?block=" + blockHead.heightBlock));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });

        menuAll.add(setSeeInBlockexplorer);
        TableMenuPopupUtil.installContextMenu(jTableAllBlock, menuAll);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new Insets(6, 11, 11, 11);
        jPanel8.add(jScrollPaneAllBlockTable, gridBagConstraints);

        jSplitPane5.setRightComponent(jPanel8);
        jScrollPaneJPanelRightPanel.setViewportView(jSplitPane5);
        jSplitPane5.setDividerLocation(0.5);

    }

    private void addMenuBan(String message, int time) {
        JMenuItem banedTenMinItem = new JMenuItem(Lang.T(message));
        banedTenMinItem.addActionListener(arg0 -> {
            itemPeerMenu.ban(time, "banned by user");
        });
        peersMenu.add(banedTenMinItem);
    }

    @Override
    public void onClose() {
        myBlocksModel.deleteObservers();
        peersTableModel.deleteObservers();
        allBlocksTableModel.deleteObservers();
        Controller.getInstance().deleteObserver(syncButton);

    }

    @Override
    public void update(Observable o, Object arg) {
        if (jLabelAllBlocksSum != null)
            jLabelAllBlocksSum.setText(String.valueOf(Controller.getInstance().getBlockChain().
                    getFullWeight(DCSet.getInstance())));
    }
}