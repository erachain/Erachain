package org.erachain.gui.items.records;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.erachain.controller.Controller;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.library.library;
import org.erachain.gui.models.UnconfirmedTransactionsTableModel;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;


@SuppressWarnings("serial")
public class UnconfirmedTransactionsPanel extends JPanel

{

    private static UnconfirmedTransactionsPanel instance;
    private UnconfirmedTransactionsTableModel transactionsModel;
    private MTable transactionsTable;

    public UnconfirmedTransactionsPanel() {
        setName(Lang.getInstance().translate("Unconfirmed Records"));
        // this.parent = parent;
        this.setLayout(new GridBagLayout());
        // this.setLayout(new ScrollPaneLayout());
        // ScrollPaneLayout

        // PADDING
        // this.setBorder(new EmptyBorder(10, 10, 10, 10));
        // this.setSize(500, 500);
        // this.setLocation(20, 20);
        // this.setMaximizable(true);
        // this.setTitle(Lang.getInstance().translate("Accounts"));
        // this.setClosable(true);
        // this.setResizable(true);
        // this.setBorder(true);

        // TABLE GBC
        GridBagConstraints tableGBC = new GridBagConstraints();
        tableGBC.fill = GridBagConstraints.BOTH;
        tableGBC.anchor = GridBagConstraints.NORTHWEST;
        tableGBC.weightx = 1;
        tableGBC.weighty = 1;
        tableGBC.gridx = 1;
        tableGBC.gridy = 1;

        // TRANSACTIONS
        this.transactionsModel = new UnconfirmedTransactionsTableModel();
        this.transactionsTable = new MTable(this.transactionsModel);
        // TRANSACTION DETAILS
        this.transactionsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // GET ROW
                    // int row = transactionsTable.getSelectedRow();
                    // row = transactionsTable.convertRowIndexToModel(row);

                    // GET TRANSACTION
                    // Transaction transaction =
                    // transactionsModel.get(row);

                    // SHOW DETAIL SCREEN OF TRANSACTION
                    // TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
                }
            }
        });

        SplitPanel record_stpit = new SplitPanel("");
        record_stpit.toolBar_LeftPanel.setVisible(false);
        record_stpit.jToolBar_RightPanel.setVisible(false);
        record_stpit.searchToolBar_LeftPanel.setVisible(false);

        // Dimension size = MainFrame.getInstance().desktopPane.getSize();
        // this.setSize(new
        // Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
        // record_stpit.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));

        // show
        // record_stpit.jTable_jScrollPanel_LeftPanel.setModel(transactionsModel);
        record_stpit.jTable_jScrollPanel_LeftPanel = transactionsTable;
        record_stpit.jTable_jScrollPanel_LeftPanel.isFontSet();

        record_stpit.jScrollPanel_LeftPanel.setViewportView(record_stpit.jTable_jScrollPanel_LeftPanel);

        // обработка изменения положения курсора в таблице
        record_stpit.jTable_jScrollPanel_LeftPanel.getSelectionModel()
                .addListSelectionListener(new ListSelectionListener() {
                    @SuppressWarnings({"unused"})
                    @Override
                    public void valueChanged(ListSelectionEvent arg0) {
                        String dateAlive;
                        String date_birthday;
                        String message;
                        // устанавливаем формат даты
                        SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
                        // создаем объект персоны
                        UnionCls union;
                        if (record_stpit.jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0) {

                            // GET ROW
                            int row = record_stpit.jTable_jScrollPanel_LeftPanel.getSelectedRow();
                            try {
                                row = record_stpit.jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
                            

                            // GET TRANSACTION
                            Transaction transaction = transactionsModel.getItem(row);
                            // SHOW DETAIL SCREEN OF TRANSACTION
                            // TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);

                            JPanel panel = new JPanel();
                            panel.setLayout(new GridBagLayout());
                            // panel.setBorder(javax.swing.BorderFactory.createLineBorder(new
                            // java.awt.Color(0, 0, 0)));

                            // TABLE GBC
                            GridBagConstraints tableGBC = new GridBagConstraints();
                            tableGBC.fill = GridBagConstraints.BOTH;
                            tableGBC.anchor = GridBagConstraints.FIRST_LINE_START;
                            tableGBC.weightx = 1;
                            tableGBC.weighty = 1;
                            tableGBC.gridx = 0;
                            tableGBC.gridy = 0;
                            panel.add(TransactionDetailsFactory.getInstance().createTransactionDetail(transaction),
                                    tableGBC);
                            JLabel jLabel9 = new JLabel();
                            jLabel9.setText("");
                            GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
                            gridBagConstraints.gridx = 0;
                            gridBagConstraints.gridy = 1;
                            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                            gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
                            gridBagConstraints.weightx = 1.0;
                            gridBagConstraints.weighty = 1.0;
                            panel.add(jLabel9, gridBagConstraints);

                            record_stpit.jScrollPane_jPanel_RightPanel.setViewportView(panel);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                record_stpit.jScrollPane_jPanel_RightPanel.setViewportView(null);
                            }
                        }
                    }
                });

        this.add(record_stpit, tableGBC);

        JPopupMenu menu = new JPopupMenu();

        JMenuItem item_Rebroadcast = new JMenuItem(Lang.getInstance().translate("Rebroadcast"));

        item_Rebroadcast.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // code Rebroadcast

                int row = record_stpit.jTable_jScrollPanel_LeftPanel.getSelectedRow();
                row = record_stpit.jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
                Transaction trans = transactionsModel.getItem(row);
                DCSet dcSet = DCSet.getInstance();
                trans.setDC(dcSet, Transaction.FOR_NETWORK, DCSet.getInstance().getBlockMap().size() + 1, 1);
                if (trans.isValid(Transaction.FOR_NETWORK, 0) == Transaction.VALIDATE_OK)
                    Controller.getInstance().broadcastTransaction(trans);

            }
        });

        menu.add(item_Rebroadcast);
        JMenuItem item_Delete = new JMenuItem(Lang.getInstance().translate("Delete"));
        item_Delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // code delete
                int row = record_stpit.jTable_jScrollPanel_LeftPanel.getSelectedRow();
                row = record_stpit.jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
                Transaction trans = transactionsModel.getItem(row);
                DCSet.getInstance().getTransactionMap().delete(trans);

            }
        });

        menu.add(item_Delete);
        

        // save jsot transactions
        JMenuItem item_Save = new JMenuItem(Lang.getInstance().translate("Save"));
        item_Save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int row = record_stpit.jTable_jScrollPanel_LeftPanel.getSelectedRow();
                row = record_stpit.jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
                Transaction trans = transactionsModel.getItem(row);
                if (trans == null) return;
                // save
                library.saveTransactionJSONtoFileSystem(getParent(), trans);
            }

            
        });
        menu.add(item_Save);
        
        TableMenuPopupUtil.installContextMenu(record_stpit.jTable_jScrollPanel_LeftPanel, menu);

        // this.add(this.transactionsTable);

        transactionsModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent arg0) {
                // TODO Auto-generated method stub
                setName(Lang.getInstance().translate("Unconfirmed Records:" + transactionsModel.getRowCount()));
            }

        });

    }

    public static UnconfirmedTransactionsPanel getInstance() {

        if (instance == null) {
            instance = new UnconfirmedTransactionsPanel();
        } else {
            instance.transactionsModel.addObservers();
        }

        return instance;

    }

}
