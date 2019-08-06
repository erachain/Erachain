package org.erachain.gui;

import org.erachain.core.BlockChain;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.BlockMap;
import org.erachain.datachain.TransactionMap;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.BlocksTableModel;
import org.erachain.gui.models.UnconfirmedTransactionsTableModel;
import org.erachain.gui.models.PeersTableModel;
import org.erachain.gui.models.SearchTransactionsTableModel;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;


public class DebugTabPane extends JTabbedPane {

    private static final long serialVersionUID = 2717571093561259483L;
    static Logger LOGGER = Logger.getLogger(DebugTabPane.class.getName());

    private PeersTableModel peersTableModel;
    private UnconfirmedTransactionsTableModel transactionsTableModel;
    private BlocksTableModel blocksTableModel;
    private LoggerTextArea loggerTextArea;
    private MTable transactionsTable;


    public DebugTabPane() {
        super();


        //ADD TABS
        if (Settings.getInstance().isGuiConsoleEnabled()) {
            this.addTab(Lang.getInstance().translate("Console"), new ConsolePanel());
        }

        this.peersTableModel = new PeersTableModel();
        //	this.addTab(Lang.getInstance().translate("Peers"), new JScrollPane(new MTable(this.peersTableModel)));

        //TRANSACTIONS TABLE MODEL
        this.transactionsTableModel = new UnconfirmedTransactionsTableModel();
        this.transactionsTable = new MTable(this.transactionsTableModel);

        //TRANSACTIONS SORTER
        Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
        indexes.put(SearchTransactionsTableModel.COLUMN_TIMESTAMP, TransactionMap.TIMESTAMP_INDEX);
        //CoreRowSorter sorter = new CoreRowSorter(transactionsTableModel, indexes);
        //transactionsTable.setRowSorter(sorter);

        //TRANSACTION DETAILS
        this.transactionsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    //GET ROW
                    int row = transactionsTable.getSelectedRow();
                    row = transactionsTable.convertRowIndexToModel(row);

                    //GET TRANSACTION
                    Transaction transaction = transactionsTableModel.getItem(row);

                    //SHOW DETAIL SCREEN OF TRANSACTION
                    TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
                }
            }
        });

        //ADD TRANSACTIONS TABLE
        //	this.addTab(Lang.getInstance().translate("Transactions"), new JScrollPane(this.transactionsTable));

        //BLOCKS TABLE MODEL
        this.blocksTableModel = new BlocksTableModel();
        JTable blocksTable = new MTable(this.blocksTableModel);

        //BLOCKS SORTER
        indexes = new TreeMap<Integer, Integer>();
        indexes.put(BlocksTableModel.COLUMN_HEIGHT, BlockMap.HEIGHT_INDEX);
        //sorter = new CoreRowSorter(blocksTableModel, indexes);
        //blocksTable.setRowSorter(sorter);

        //ADD BLOCK TABLE
        this.addTab(Lang.getInstance().translate("Blocks"), new JScrollPane(blocksTable));
        //
        if (BlockChain.DEVELOP_USE) {
            JPanel pppp = new JPanel();
            JButton bb = new JButton("OffRun");
            bb.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub

                }


            });
            pppp.add(bb);

            //		this.addTab(Lang.getInstance().translate("OffRun"), new JScrollPane(pppp))
        }

        loggerTextArea = new LoggerTextArea(LOGGER);
        JScrollPane scrollPane = new JScrollPane(loggerTextArea);
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
        //      this.addTab("Logger", scrollPane);
    }

    public void close() {
        //REMOVE OBSERVERS/HANLDERS
        this.peersTableModel.deleteObservers();

        this.transactionsTableModel.deleteObservers();

        this.blocksTableModel.deleteObservers();

        this.loggerTextArea.removeNotify();
    }

}
