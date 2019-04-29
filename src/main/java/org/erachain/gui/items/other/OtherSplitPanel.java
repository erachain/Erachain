package org.erachain.gui.items.other;

import org.erachain.controller.Controller;
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
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class OtherSplitPanel extends SplitPanel implements Observer {

    private PeersTableModel peersTableModel = new PeersTableModel();

    private JPanel jPanel2 = new JPanel();
    private GridBagConstraints gridBagConstraints;
    private JLabel jLabelPeerTitle = new JLabel();
    private JScrollPane jScrollPanePeersTable = new JScrollPane();
    private BlocksTableModel allBlocksTableModel = new BlocksTableModel();
    private MTable jTableAllBlock;

    // TRANSACTIONS
    private WalletBlocksTableModel blocksModel = new WalletBlocksTableModel();
    private MTable jTableMyBlock;
    private JPopupMenu peersMenu = new JPopupMenu();
    private Peer itemPeerMenu;
    private WalletSyncButton syncButton = new WalletSyncButton();
    private WalletOrphanButton returnButton = new WalletOrphanButton();
    private JMenuItem connectItem;
    private final JLabel jLabelAllBlocksSum;

    public OtherSplitPanel() {
        super("OtherSplitPanel");
        DCSet.getInstance().getBlockMap().addObserver(this);
        jTableJScrollPanelLeftPanel.setModel(peersTableModel);
        jTableJScrollPanelLeftPanel.setAutoCreateRowSorter(true);

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

        connectItem = new JMenuItem(Lang.getInstance().translate("Connect"));
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

        addMenuBan("Ban always", 999999);

        peersMenu.add(new JSeparator());

        TableMenuPopupUtil.installContextMenu(jTableJScrollPanelLeftPanel, peersMenu);

        toolBarLeftPanel.setVisible(false);
        jToolBarRightPanel.setVisible(false);

        jPanel2.setLayout(new GridBagLayout());

        jLabelPeerTitle.setText(Lang.getInstance().translate("Peers"));
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

        jTableMyBlock = new MTable(blocksModel);

        JPanel jPanel7 = new JPanel(new GridBagLayout());
        JLabel jLabelMyBlockTitle = new JLabel(Lang.getInstance().translate("My Generated Blocks"));
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
        JLabel jLabelAllBlockCaption = new JLabel(Lang.getInstance().translate("Last 100 blocks") + ". " +
                Lang.getInstance().translate("Sum win value chain blocks:"));
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
        JMenuItem banedTenMinItem = new JMenuItem(Lang.getInstance().translate(message));
        banedTenMinItem.addActionListener(arg0 -> {
            itemPeerMenu.ban(time, "banned by user");
        });
        peersMenu.add(banedTenMinItem);
    }

    @Override
    public void onClose() {
        blocksModel.deleteObservers();
        peersTableModel.deleteObservers();
        allBlocksTableModel.removeObservers();
        Controller.getInstance().deleteObserver(syncButton);

    }

    @Override
    public void update(Observable o, Object arg) {
         jLabelAllBlocksSum.setText(String.valueOf(Controller.getInstance().getBlockChain().
                 getFullWeight(DCSet.getInstance())));
    }
}
