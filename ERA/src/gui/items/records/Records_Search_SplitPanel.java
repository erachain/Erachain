package gui.items.records;

import core.item.unions.UnionCls;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.Split_Panel;
import gui.library.MTable;
import gui.library.Voush_Library_Panel;
import gui.models.TransactionsTableModel;
import gui.records.VouchRecordDialog;
import gui.transaction.TransactionDetailsFactory;
import lang.Lang;
import org.mapdb.Fun.Tuple2;
import utils.MenuPopupUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;


public class Records_Search_SplitPanel extends Split_Panel {

    public JPanel info_Panel;
    public Voush_Library_Panel voush_Library_Panel;
    TransactionsTableModel transactionsTableModel;
    JScrollPane jScrollPane4;
    private JTextField sender_address;

    public Records_Search_SplitPanel() {
        super("Records_Search_SplitPanel");

        this.searchToolBar_LeftPanel.setVisible(true);
        jScrollPane4 = new JScrollPane();

        this.setName(Lang.getInstance().translate("Search Records"));

        this.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Insert Height Block or Block-SeqNo") + ":");
        this.toolBar_LeftPanel.add(new JLabel(Lang.getInstance().translate("Insert Account") + ":"));
        sender_address = new JTextField();
        sender_address.setToolTipText("");
        sender_address.setAlignmentX(1.0F);
        sender_address.setMinimumSize(new java.awt.Dimension(350, 20));
        sender_address.setName(""); // NOI18N
        sender_address.setPreferredSize(new java.awt.Dimension(350, 20));
        sender_address.setMaximumSize(new java.awt.Dimension(2000, 20));

        MenuPopupUtil.installContextMenu(sender_address);

        this.toolBar_LeftPanel.add(sender_address);
        sender_address.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                searchTextField_SearchToolBar_LeftPanel.setText("");
                transactionsTableModel.Find_Transactions_from_Address(sender_address.getText());

            }

        });

        this.button1_ToolBar_LeftPanel.setVisible(false);
        this.button2_ToolBar_LeftPanel.setVisible(false);
        this.searth_Favorite_JCheckBox_LeftPanel.setVisible(false);
        this.searth_My_JCheckBox_LeftPanel.setVisible(false);
        this.jButton1_jToolBar_RightPanel.setVisible(false);
        this.jButton2_jToolBar_RightPanel.setVisible(false);


        // 	Records_Table_Model records_Model = new Records_Table_Model();
        // 	this.jTable_jScrollPanel_LeftPanel = new JTable(records_Model);

        MenuPopupUtil.installContextMenu(this.searchTextField_SearchToolBar_LeftPanel);
        this.searchTextField_SearchToolBar_LeftPanel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                sender_address.setText("");
                transactionsTableModel.setBlockNumber(searchTextField_SearchToolBar_LeftPanel.getText());

            }

        });

        //TRANSACTIONS TABLE MODEL
        this.transactionsTableModel = new TransactionsTableModel();
        this.jTable_jScrollPanel_LeftPanel = new MTable(this.transactionsTableModel);

        this.jTable_jScrollPanel_LeftPanel.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        // MENU
        JPopupMenu mainMenu = new JPopupMenu();

        JMenuItem vouch_menu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        vouch_menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
                row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
                Transaction trans = transactionsTableModel.getTransaction(row);
                DCSet db = DCSet.getInstance();
                new VouchRecordDialog(trans.getBlockHeight(db), trans.getSeqNo(db));

            }
        });

        mainMenu.add(vouch_menu);

        this.jTable_jScrollPanel_LeftPanel.setComponentPopupMenu(mainMenu);

        this.jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());

        //TRANSACTIONS SORTER
        //		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
        //		indexes.put(TransactionsTableModel.COLUMN_TIMESTAMP, TransactionMap.TIMESTAMP_INDEX);
        //		CoreRowSorter sorter = new CoreRowSorter(transactionsTableModel, indexes);
        //		this.jTable_jScrollPanel_LeftPanel.setRowSorter(sorter);

        //TRANSACTION DETAILS
        this.jTable_jScrollPanel_LeftPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    //GET ROW
                    int row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
                    row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);

                    //GET TRANSACTION
                    Transaction transaction = transactionsTableModel.getTransaction(row);

                    //SHOW DETAIL SCREEN OF TRANSACTION
                    TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
                }
            }
        });

        this.jScrollPanel_LeftPanel.setViewportView(this.jTable_jScrollPanel_LeftPanel);

    }

    @Override
    public void delay_on_close() {
        // delete observer left panel
        transactionsTableModel.removeObservers();
        // get component from right panel
        Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
        // if Person_Info 002 delay on close
        //		  if (c1.getClass() == this.info_Panel.getClass()) voush_Library_Panel.delay_on_close();

    }

    public void listener() {
        transactionsTableModel.setBlockNumber(searchTextField_SearchToolBar_LeftPanel.getText());
    }

    // listener select row
    class search_listener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent arg0) {
            String dateAlive;
            String date_birthday;
            String message;
            // устанавливаем формат даты
            SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
            //создаем объект персоны
            UnionCls union;
            Transaction voting = null;
            if (jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0) {
                voting = (Transaction) transactionsTableModel.getTransaction(jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));

                //	Person_info_panel_001 info_panel = new Person_info_panel_001(voting, false);

                //	votingDetailsPanel = new VotingDetailPanel(voting, (AssetCls)allVotingsPanel.cbxAssets.getSelectedItem());
                //	votingDetailsPanel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
                //jScrollPane_jPanel_RightPanel.setHorizontalScrollBar(null);
                //	jScrollPane_jPanel_RightPanel.setViewportView(votingDetailsPanel);
                //jSplitPanel.setRightComponent(votingDetailsPanel);


                //   TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);

                info_Panel = new JPanel();
                info_Panel.setLayout(new GridBagLayout());
                //  panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

                //TABLE GBC
                GridBagConstraints tableGBC = new GridBagConstraints();
                tableGBC.fill = GridBagConstraints.BOTH;
                tableGBC.anchor = GridBagConstraints.FIRST_LINE_START;
                tableGBC.weightx = 1;
                tableGBC.weighty = 1;
                tableGBC.gridx = 0;
                tableGBC.gridy = 0;
                //	JPanel a = TransactionDetailsFactory.getInstance().createTransactionDetail(voting);
                info_Panel.add(TransactionDetailsFactory.getInstance().createTransactionDetail(voting), tableGBC);

                Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> signs = DCSet.getInstance().getVouchRecordMap().get(voting.getBlockHeight(DCSet.getInstance()), voting.getSeqNo(DCSet.getInstance()));
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
                    info_Panel.add(jLabelTitlt_Table_Sign, gridBagConstraints);


                    gridBagConstraints = new java.awt.GridBagConstraints();
                    gridBagConstraints.gridx = 0;
                    gridBagConstraints.gridy = 2;
                    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
                    gridBagConstraints.weightx = 1.0;
                    gridBagConstraints.weighty = 1.0;
                    voush_Library_Panel = new Voush_Library_Panel(voting);
                    info_Panel.add(voush_Library_Panel, gridBagConstraints);

                }

                jScrollPane_jPanel_RightPanel.setViewportView(info_Panel);

            }
        }
    }
}