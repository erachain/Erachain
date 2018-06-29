package gui.items.other;

import controller.Controller;
import gui.Split_Panel;
import gui.library.MTable;
import gui.library.Wallet_Orphan_Button;
import gui.library.Wallet_Sync_Button;
import gui.models.BlocksTableModel;
import gui.models.PeersTableModel;
import gui.models.WalletBlocksTableModel;
import lang.Lang;
import network.Peer;
import utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicInteger;

public class Other_Split_Panel extends Split_Panel {

    private PeersTableModel peersTableModel;
   
    private JPanel jPanel2;
    private GridBagConstraints gridBagConstraints;
    private JLabel jLabel_Peer_Title;
    private JScrollPane jScrollPane_Peers_Table;
    private BlocksTableModel All_Blocks_TableModel;
    private MTable jTable_All_Block;
    private WalletBlocksTableModel blocksModel;
    private MTable jTable_My_Block;
    private JPopupMenu peers_Menu;
    private Peer item_Peer_Menu;
    private Wallet_Sync_Button sync_Button;
    private Wallet_Orphan_Button return_Button;
    private JMenuItem connect_Item;
    // final AtomicInteger selectedCol=new AtomicInteger(-1);

    public Other_Split_Panel() {
        super("Other_Split_Panel");
        // TODO Auto-generated constructor stub
        jLabel_Peer_Title = new JLabel();
        this.peersTableModel = new PeersTableModel();
        this.jTable_jScrollPanel_LeftPanel.setModel(this.peersTableModel);
        this.jTable_jScrollPanel_LeftPanel.setAutoCreateRowSorter(true);


        peers_Menu = new JPopupMenu();

        peers_Menu.addPopupMenuListener(new PopupMenuListener() {

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

                // TODO Auto-generated method stub
                item_Peer_Menu = peersTableModel.get_Peers(jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));

            }
            
       

        });

       
        connect_Item = new JMenuItem(Lang.getInstance().translate("Connect"));
        connect_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub

                //	item_Peer_Menu.ban(10, "banned by user");
                item_Peer_Menu.connect(item_Peer_Menu.callback);

            }

        });
        peers_Menu.add(connect_Item);

        JMenuItem Baned_2_Min_Item = new JMenuItem(Lang.getInstance().translate("Ban in 10 min."));
        Baned_2_Min_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub

                item_Peer_Menu.ban(10, "banned by user");

            }

        });
        peers_Menu.add(Baned_2_Min_Item);
        JMenuItem Baned_10_Min_Item = new JMenuItem(Lang.getInstance().translate("Ban in 60 min."));
        Baned_10_Min_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                item_Peer_Menu.ban(60, "banned by user");
            }

        });
        peers_Menu.add(Baned_10_Min_Item);
        JMenuItem Baned_60_Min_Item = new JMenuItem(Lang.getInstance().translate("Ban in 3 hours"));
        Baned_60_Min_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                item_Peer_Menu.ban(180, "banned by user");
            }

        });
        peers_Menu.add(Baned_60_Min_Item);
        JMenuItem Baned_24_Hours_Item = new JMenuItem(Lang.getInstance().translate("Ban in 24 hours"));
        Baned_24_Hours_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                item_Peer_Menu.ban(1440, "banned by user");
            }

        });
        peers_Menu.add(Baned_24_Hours_Item);

        JMenuItem Baned_Allways_Item = new JMenuItem(Lang.getInstance().translate("Ban Allways"));
        Baned_Allways_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                item_Peer_Menu.ban(999999, "banned by user");
            }

        });
        peers_Menu.add(Baned_Allways_Item);

        peers_Menu.add(new JSeparator());
        JMenuItem allow_Item = new JMenuItem(Lang.getInstance().translate("Allow"));
        allow_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub

            }

        });
        //  peers_Menu.add(allow_Item);

        TableMenuPopupUtil.installContextMenu(this.jTable_jScrollPanel_LeftPanel, peers_Menu);


        this.toolBar_LeftPanel.setVisible(false);
        this.jToolBar_RightPanel.setVisible(false);
        //  this.jTable_Peers.setEnabled(false);

        jPanel2 = new JPanel();

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel_Peer_Title.setText(Lang.getInstance().translate("Peers"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(9, 11, 0, 11);
        jPanel2.add(jLabel_Peer_Title, gridBagConstraints);

        jScrollPane_Peers_Table = new JScrollPane();
        jScrollPane_Peers_Table.setBorder(null);


        jScrollPane_Peers_Table.setViewportView(jTable_jScrollPanel_LeftPanel);
        jScrollPane_Peers_Table.setMinimumSize(new Dimension(0, 0));
        jTable_jScrollPanel_LeftPanel.setMinimumSize(new Dimension(0, 0));
        //   jTable_Peers.setPreferredSize(jTable_Peers.getPreferredSize());
        jPanel2.setMinimumSize(new Dimension(0, 0));
        jTable_jScrollPanel_LeftPanel.setOpaque(false);
    
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(7, 11, 11, 11);
        jPanel2.add(jScrollPane_Peers_Table, gridBagConstraints);

        // jScrollPanel_LeftPanel.setViewportView(jPanel2);
        leftPanel.removeAll();
        jSplitPanel.setLeftComponent(jPanel2);


        this.All_Blocks_TableModel = new BlocksTableModel(true);
        this.jTable_All_Block = new MTable(this.All_Blocks_TableModel);
        //this.jTable_All_Block.setEnabled(false);

        //TRANSACTIONS
        this.blocksModel = new WalletBlocksTableModel();
        this.jTable_My_Block = new MTable(blocksModel);


        JPanel jPanel7 = new JPanel();
        jPanel7.setLayout(new java.awt.GridBagLayout());
        JLabel jLabel_My_Block_Title = new JLabel();
        jLabel_My_Block_Title.setText(Lang.getInstance().translate("My Generated Blocks"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 11, 0, 11);
        jPanel7.add(jLabel_My_Block_Title, gridBagConstraints);

        sync_Button = new Wallet_Sync_Button();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 11, 0, 11);
        jPanel7.add(sync_Button, gridBagConstraints);


        JScrollPane jScrollPane_My_Block_Table = new JScrollPane();
        jScrollPane_My_Block_Table.setBorder(null);

        jScrollPane_My_Block_Table.setViewportView(jTable_My_Block);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 11, 11);
        jPanel7.add(jScrollPane_My_Block_Table, gridBagConstraints);

        JSplitPane jSplitPane5 = new JSplitPane();
        jSplitPane5.setLeftComponent(jPanel7);
        JPanel jPanel8 = new JPanel();
        jPanel8.setLayout(new java.awt.GridBagLayout());

        JLabel jLabel_All_Block = new JLabel();
        jLabel_All_Block.setText(Lang.getInstance().translate("Last 100 blocks"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        jPanel8.add(jLabel_All_Block, gridBagConstraints);

        return_Button = new Wallet_Orphan_Button();

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        jPanel8.add(return_Button, gridBagConstraints);


        JScrollPane jScrollPane_All_Block_Table = new JScrollPane();
        jScrollPane_All_Block_Table.setBorder(null);


        jScrollPane_All_Block_Table.setViewportView(jTable_All_Block);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(6, 11, 11, 11);
        jPanel8.add(jScrollPane_All_Block_Table, gridBagConstraints);


        jSplitPane5.setMinimumSize(new Dimension(0, 0));
        jSplitPane5.setRightComponent(jPanel8);
        //    jSplitPane5.setRightComponent(jScrollPane_My_Block_Table);

        jScrollPane_jPanel_RightPanel.setViewportView(jSplitPane5);
        jSplitPane5.setDividerLocation(0.5);
        
        

    }

    @Override
    public void delay_on_close() {

        blocksModel.deleteObserver();
        peersTableModel.deleteObserver();
        All_Blocks_TableModel.removeObservers();
        Controller.getInstance().addObserver(sync_Button);

    }


}
