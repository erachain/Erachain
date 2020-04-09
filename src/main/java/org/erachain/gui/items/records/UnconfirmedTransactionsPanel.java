package org.erachain.gui.items.records;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.erachain.controller.Controller;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.UnconfirmedTransactionsTableModel;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.TableMenuPopupUtil;
import org.erachain.utils.URLViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings("serial")
public class UnconfirmedTransactionsPanel extends JPanel

{
    protected Logger logger;
    private static String iconFile = Settings.getInstance().getPatnIcons() + "UnconfirmedTransactionsPanel.png";
    private static UnconfirmedTransactionsPanel instance;
    private UnconfirmedTransactionsTableModel transactionsModel;
    private MTable transactionsTable;

    public UnconfirmedTransactionsPanel() {
        setName(Lang.getInstance().translate("Unconfirmed Records"));
        // this.parent = parent;
        this.setLayout(new GridBagLayout());
        // this.setLayout(new ScrollPaneLayout());
        // ScrollPaneLayout

        logger = LoggerFactory.getLogger(getClass());


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
        record_stpit.toolBarLeftPanel.setVisible(false);
        record_stpit.jToolBarRightPanel.setVisible(false);
        record_stpit.searchToolBar_LeftPanel.setVisible(false);

        // Dimension size = MainFrame.getInstance().desktopPane.getSize();
        // this.setSize(new
        // Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
        // record_stpit.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));

        // show
        // record_stpit.jTableJScrollPanelLeftPanel.setModel(transactionsModel);
        record_stpit.jTableJScrollPanelLeftPanel = transactionsTable;
        record_stpit.jTableJScrollPanelLeftPanel.isFontSet();

        record_stpit.jScrollPanelLeftPanel.setViewportView(record_stpit.jTableJScrollPanelLeftPanel);

        // обработка изменения положения курсора в таблице
        record_stpit.jTableJScrollPanelLeftPanel.getSelectionModel()
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
                        if (record_stpit.jTableJScrollPanelLeftPanel.getSelectedRow() >= 0) {

                            // GET ROW
                            int row = record_stpit.jTableJScrollPanelLeftPanel.getSelectedRow();
                            try {
                                row = record_stpit.jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                            

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

                            record_stpit.jScrollPaneJPanelRightPanel.setViewportView(panel);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                logger.error(e.getMessage(), e);
                                record_stpit.jScrollPaneJPanelRightPanel.setViewportView(null);
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

                int row = record_stpit.jTableJScrollPanelLeftPanel.getSelectedRow();
                row = record_stpit.jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
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
                int row = record_stpit.jTableJScrollPanelLeftPanel.getSelectedRow();
                row = record_stpit.jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                Transaction trans = transactionsModel.getItem(row);
                DCSet.getInstance().getTransactionTab().delete(trans);

            }
        });

        menu.add(item_Delete);
        

        // save jsot transactions
        JMenuItem item_Save = new JMenuItem(Lang.getInstance().translate("Save"));
        item_Save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int row = record_stpit.jTableJScrollPanelLeftPanel.getSelectedRow();
                row = record_stpit.jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                Transaction trans = transactionsModel.getItem(row);
                if (trans == null) return;
                // save
                Library.saveTransactionJSONtoFileSystem(getParent(), trans);
            }

            
        });
        menu.add(item_Save);

        menu.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int row = record_stpit.jTableJScrollPanelLeftPanel.getSelectedRow();
                row = record_stpit.jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                Transaction trans = transactionsModel.getItem(row);

                try {
                    URLViewer.openWebpage(new URL("http://" + Settings.getInstance().getBlockexplorerURL()
                            + ":" + Settings.getInstance().getWebPort() + "/index/blockexplorer.html"
                            + "?tx=" + trans.viewSignature()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });
        menu.add(setSeeInBlockexplorer);

        TableMenuPopupUtil.installContextMenu(record_stpit.jTableJScrollPanelLeftPanel, menu);

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

    public static Image getIcon() {
        {
            try {
                return Toolkit.getDefaultToolkit().getImage(iconFile);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
