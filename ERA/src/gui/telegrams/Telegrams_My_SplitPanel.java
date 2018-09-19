package gui.telegrams;

import controller.Controller;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.Split_Panel;
import gui.library.MTable;
import gui.library.SetIntervalPanel;
import gui.library.Voush_Library_Panel;
import gui.library.library;
import gui.models.WalletTelegramsTableModel;
import gui.models.WalletTransactionsTableModel;
import gui.transaction.TransactionDetailsFactory;
import lang.Lang;
import org.mapdb.Fun.Tuple2;
import utils.TableMenuPopupUtil;

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

public class Telegrams_My_SplitPanel extends Split_Panel {

   
    private static final long serialVersionUID = 2717571093561259483L;

    private static Telegrams_My_SplitPanel instance;
    protected Transaction trans;
    private JPanel records_Info_Panel;
    private JPopupMenu menu;
    private JMenuItem item_Delete;
    private JMenuItem item_Rebroadcast;
    public WalletTelegramsTableModel records_model;
    public SetIntervalPanel setIntervalPanel;

    private JMenuItem item_Save;

  

    public Telegrams_My_SplitPanel() {
        super("Telegrams_My_SplitPanel");
        this.leftPanel.setVisible(true);
       

        setName(Lang.getInstance().translate("My Records"));

        //CREATE TABLE
        this.records_model = new WalletTelegramsTableModel();
        this.jTable_jScrollPanel_LeftPanel = new MTable(this.records_model);
        this.jScrollPanel_LeftPanel.setViewportView(this.jTable_jScrollPanel_LeftPanel);

        // not show buttons
        jToolBar_RightPanel.setVisible(true);
        // toolBar_LeftPanel.setVisible(false);
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
        this.jToolBar_RightPanel.setVisible(false);
        this.toolBar_LeftPanel.setVisible(false);
        
     // set interval panel
        setIntervalPanel = new SetIntervalPanel(Transaction.EXTENDED);
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        leftPanel.add(setIntervalPanel, gridBagConstraints);

    
        
        jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());

        menu = new JPopupMenu();
        menu.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                // TODO Auto-generated method stub
                int row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
                row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
                if (row < 0) return;
                trans = (Transaction) records_model.getItem(row);
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                // TODO Auto-generated method stub
                // TODO Auto-generated method stub

            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                // TODO Auto-generated method stub
                int row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
                row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
                if (row < 0) return;
                trans = (Transaction) records_model.getItem(row);

            }


        });

        item_Rebroadcast = new JMenuItem(Lang.getInstance().translate("Rebroadcast"));

        item_Rebroadcast.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // code Rebroadcast

                if (trans == null) return;
                // DBSet db = DBSet.getInstance();
                Controller.getInstance().broadcastTransaction(trans);

            }
        });

        menu.add(item_Rebroadcast);
        item_Delete = new JMenuItem(Lang.getInstance().translate("Delete"));
        item_Delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // code delete
                //int row = my_Records_Panel.records_Table.getSelectedRow();
                //row = my_Records_Panel.records_Table.convertRowIndexToModel(row);
                //Transaction trans = (Transaction) my_Records_Panel.records_model.getItem(row);
                if (trans == null) return;
                DCSet.getInstance().getTransactionMap().delete(trans);

            }
        });

        menu.add(item_Delete);
        
        item_Save = new JMenuItem(Lang.getInstance().translate("Save"));
        item_Save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // code delete
                //int row = my_Records_Panel.records_Table.getSelectedRow();
                //row = my_Records_Panel.records_Table.convertRowIndexToModel(row);
                //Transaction trans = (Transaction) my_Records_Panel.records_model.getItem(row);
                if (trans == null) return;
                // save
                library.saveTransactionJSONtoFileSystem(getParent(), trans);
            }

            
        });

        menu.add(item_Save);
        
        TableMenuPopupUtil.installContextMenu(jTable_jScrollPanel_LeftPanel, menu);
        menu.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                // TODO Auto-generated method stub
                //	int row = my_Records_Panel.records_Table.getSelectedRow();
                //	row = my_Records_Panel.records_Table.convertRowIndexToModel(row);
                //	Transaction trans = (Transaction) my_Records_Panel.records_model.getItem(row);
                if (trans == null) return;
                item_Delete.setEnabled(true);
                item_Rebroadcast.setEnabled(true);
                if (trans.isConfirmed(DCSet.getInstance())) {
                    item_Delete.setEnabled(false);
                    item_Rebroadcast.setEnabled(false);

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

    public static Telegrams_My_SplitPanel getInstance() {

        if (instance == null) {
            instance = new Telegrams_My_SplitPanel();
        }

        return instance;


    }

    public void onClick() {
        // GET SELECTED OPTION
        int row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
        if (row == -1) {
            row = 0;
        }
        row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);

        if (jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0) {
        }
    }

    @Override
    public void delay_on_close() {
        // delete observer left panel
        this.records_model.removeObservers();
        this.setIntervalPanel.removeObservers();
        // get component from right panel
        //	Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
        // if Person_Info 002 delay on close
        //	  if (c1.getClass() == this.records_Info_Panel.getClass()) voush_Library_Panel.delay_on_close();

    }

    // listener select row
    class search_listener implements ListSelectionListener {


        @Override
        public void valueChanged(ListSelectionEvent arg0) {
            Transaction trans = null;
            if (jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0) {
                trans = (Transaction) records_model.getItem(jTable_jScrollPanel_LeftPanel
                        .convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));

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

                Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> signs = DCSet.getInstance().getVouchRecordMap()
                        .get(trans.getBlockHeight(), trans.getSeqNo(DCSet.getInstance()));
                GridBagConstraints gridBagConstraints = null;
                if (signs != null) {

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
                   

                }

                jScrollPane_jPanel_RightPanel.setViewportView(records_Info_Panel);

            }
        }
    }
   
  

}
