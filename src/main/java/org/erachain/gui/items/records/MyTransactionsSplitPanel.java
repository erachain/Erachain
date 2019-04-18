package org.erachain.gui.items.records;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.library.SetIntervalPanel;
import org.erachain.gui.library.VoushLibraryPanel;
import org.erachain.gui.library.library;
import org.erachain.gui.models.WalletTransactionsTableModel;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.lang.Lang;
import org.mapdb.Fun.Tuple2;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;

public class MyTransactionsSplitPanel extends SplitPanel {

    private static final long serialVersionUID = 2717571093561259483L;

    private static MyTransactionsSplitPanel instance;
    public VoushLibraryPanel voush_Library_Panel;
    protected Tuple2<Long, Long> selectedTransactionKey;
    protected Transaction selectedTransaction;
    private JPanel records_Info_Panel;
    private JPopupMenu menu;
    private JMenuItem item_Delete;
    private JMenuItem item_Rebroadcast;
    public WalletTransactionsTableModel records_model;
    public SetIntervalPanel setIntervalPanel;

    private JMenuItem item_Save;

  

    private MyTransactionsSplitPanel() {
        super("MyTransactionsSplitPanel");
        this.leftPanel.setVisible(true);
       

        setName(Lang.getInstance().translate("My Records"));

        //CREATE TABLE
        this.records_model = new WalletTransactionsTableModel();
        this.jTableJScrollPanelLeftPanel = new MTable(this.records_model);
        this.jScrollPanelLeftPanel.setViewportView(this.jTableJScrollPanelLeftPanel);

        // not show buttons
        jToolBarRightPanel.setVisible(true);
        // toolBarLeftPanel.setVisible(false);
        jButton1_jToolBar_RightPanel.setText("<HTML><B> " + Lang.getInstance().translate("Record") + "</></> ");
        jButton1_jToolBar_RightPanel.setBorderPainted(true);
        jButton1_jToolBar_RightPanel.setFocusable(true);
        jButton1_jToolBar_RightPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)),
                javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        jButton1_jToolBar_RightPanel.setSize(120, 30);
        jButton1_jToolBar_RightPanel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClick();
            }
        });

        jButton2_jToolBar_RightPanel.setVisible(false);
        this.jToolBarRightPanel.setVisible(false);
        this.toolBarLeftPanel.setVisible(false);
        
     // set interval panel
        setIntervalPanel = new SetIntervalPanel(Controller.getInstance().wallet.database.getTransactionMap());
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

     // set interval
        setInterval();
        
        jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new search_listener());

        menu = new JPopupMenu();
        menu.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                // TODO Auto-generated method stub
                int row = jTableJScrollPanelLeftPanel.getSelectedRow();
                row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                if (row < 0) return;
                selectedTransaction = records_model.getItem(row).b;
                selectedTransactionKey = records_model.getPairItem(row).getA();
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                // TODO Auto-generated method stub
                // TODO Auto-generated method stub

            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                // TODO Auto-generated method stub
                int row = jTableJScrollPanelLeftPanel.getSelectedRow();
                row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                if (row < 0) return;
                selectedTransaction = records_model.getItem(row).b;
                selectedTransactionKey = records_model.getPairItem(row).getA();

            }


        });

        item_Rebroadcast = new JMenuItem(Lang.getInstance().translate("Rebroadcast"));

        item_Rebroadcast.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // code Rebroadcast

                if (selectedTransaction == null) return;
                // DLSet db = DLSet.getInstance();
                Controller.getInstance().broadcastTransaction(selectedTransaction);

            }
        });

        menu.add(item_Rebroadcast);

        item_Delete = new JMenuItem(Lang.getInstance().translate("Delete"));
        item_Delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (selectedTransaction == null)
                    return;

                // code delete
                Controller.getInstance().getWallet().database.getTransactionMap()
                        .delete(selectedTransactionKey);

            }
        });

        menu.add(item_Delete);
        
        item_Save = new JMenuItem(Lang.getInstance().translate("Save"));
        item_Save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (selectedTransaction == null) return;
                // save
                library.saveTransactionJSONtoFileSystem(getParent(), selectedTransaction);
            }

            
        });

        menu.add(item_Save);
        
        TableMenuPopupUtil.installContextMenu(jTableJScrollPanelLeftPanel, menu);
        menu.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                // TODO Auto-generated method stub
                //	int row = my_Records_Panel.records_Table.getSelectedRow();
                //	row = my_Records_Panel.records_Table.convertRowIndexToModel(row);
                //	Transaction trans = (Transaction) my_Records_Panel.records_model.getItem(row);
                if (selectedTransaction == null) return;
                if (selectedTransaction.isConfirmed(DCSet.getInstance())) {
                    item_Delete.setEnabled(false);
                    item_Rebroadcast.setEnabled(false);
                } else {
                    item_Delete.setEnabled(true);
                    item_Rebroadcast.setEnabled(true);
                }
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                // TODO Auto-generated method stub

            }


        });


    }

    public static MyTransactionsSplitPanel getInstance() {

        if (instance == null) {
            instance = new MyTransactionsSplitPanel();
        } else {
            // восстановим наблюдения
            instance.records_model.addObservers();
            instance.setIntervalPanel.addObservers();
        }


        return instance;


    }

    public void onClick() {
        // GET SELECTED OPTION
        int row = jTableJScrollPanelLeftPanel.getSelectedRow();
        if (row == -1) {
            row = 0;
        }
        row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);

        if (jTableJScrollPanelLeftPanel.getSelectedRow() >= 0) {
        }
    }

    //@Override
    public void onClose() {
        // delete observer left panel
        this.records_model.deleteObservers();
        this.setIntervalPanel.deleteObservers();
        // get component from right panel
        //	Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        //	  if (c1.getClass() == this.records_Info_Panel.getClass()) voush_Library_Panel.onClose();

    }

    // listener select row
    class search_listener implements ListSelectionListener {


        @Override
        public void valueChanged(ListSelectionEvent arg0) {
            Transaction trans = null;
            if (jTableJScrollPanelLeftPanel.getSelectedRow() >= 0 && jTableJScrollPanelLeftPanel.getSelectedRow() < records_model.getRowCount()) {
                trans = (Transaction) records_model
                        .getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;

                records_Info_Panel = new JPanel();
                records_Info_Panel.setLayout(new GridBagLayout());

                // TABLE GBC
                GridBagConstraints tableGBC = new GridBagConstraints();
                tableGBC.fill = GridBagConstraints.BOTH;
                tableGBC.anchor = GridBagConstraints.FIRST_LINE_START;
                tableGBC.weightx = 1;
                tableGBC.weighty = 1;
                tableGBC.gridx = 0;
                tableGBC.gridy = 0;
                records_Info_Panel.add(TransactionDetailsFactory.getInstance().createTransactionDetail(trans), tableGBC);

                Tuple2<BigDecimal, List<Long>> keys = DCSet.getInstance().getVouchRecordMap()
                        .get(Transaction.makeDBRef(trans.getBlockHeight(), trans.getSeqNo()));
                GridBagConstraints gridBagConstraints = null;
                if (keys != null) {

                    JLabel jLabelTitlt_Table_Sign = new JLabel(Lang.getInstance().translate("Signatures") + ":");
                    gridBagConstraints = new java.awt.GridBagConstraints();
                    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
                    gridBagConstraints.weightx = 0.1;
                    gridBagConstraints.insets = new java.awt.Insets(12, 11, 0, 11);
                    gridBagConstraints.gridx = 0;
                    gridBagConstraints.gridy = 1;
                    records_Info_Panel.add(jLabelTitlt_Table_Sign, gridBagConstraints);
                    gridBagConstraints = new java.awt.GridBagConstraints();
                    gridBagConstraints.gridx = 0;
                    gridBagConstraints.gridy = 2;
                    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
                    gridBagConstraints.weightx = 1.0;
                    gridBagConstraints.weighty = 1.0;
                    voush_Library_Panel = new VoushLibraryPanel(trans);
                    records_Info_Panel.add(voush_Library_Panel, gridBagConstraints);

                }

                jScrollPaneJPanelRightPanel.setViewportView(records_Info_Panel);

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
            this.records_model.setInterval(start, step);
            this.records_model.fireTableDataChanged();
        }
    }

}
