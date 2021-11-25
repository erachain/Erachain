package org.erachain.gui;

import org.erachain.core.BlockChain;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.BlocksTableModel;
import org.erachain.gui.models.UnconfirmedTransactionsTableModel;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;


public class DebugTabPane extends JTabbedPane {

    private static final long serialVersionUID = 2717571093561259483L;
    static Logger LOGGER = Logger.getLogger(DebugTabPane.class.getName());

    private UnconfirmedTransactionsTableModel transactionsTableModel;
    private BlocksTableModel blocksTableModel;
    private LoggerTextArea loggerTextArea;
    private MTable transactionsTable;


    public DebugTabPane() {
        super();


        //ADD TABS
        if (Settings.getInstance().isGuiConsoleEnabled()) {
            this.addTab(Lang.T("Console"), new ConsolePanel());
        }

        //TRANSACTIONS TABLE MODEL
        this.transactionsTableModel = new UnconfirmedTransactionsTableModel();
        this.transactionsTable = new MTable(this.transactionsTableModel);

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
                    TransactionDetailsFactory.createTransactionDetail(transaction);
                }
            }
        });

        //BLOCKS TABLE MODEL
        this.blocksTableModel = new BlocksTableModel();
        JTable blocksTable = new MTable(this.blocksTableModel);

        //ADD BLOCK TABLE
        this.addTab(Lang.T("Blocks"), new JScrollPane(blocksTable));
        //
        if (true || BlockChain.TEST_MODE) {
            JPanel pppp = new JPanel();
            JButton bb = new JButton("OffRun");
            bb.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub

                }

            });
            pppp.add(bb);

        }

        loggerTextArea = new LoggerTextArea(LOGGER);
        JScrollPane scrollPane = new JScrollPane(loggerTextArea);
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());

    }

    public void close() {
        //REMOVE OBSERVERS/HANLDERS

        this.transactionsTableModel.deleteObservers();

        this.blocksTableModel.deleteObservers();

        this.loggerTextArea.removeNotify();
    }

}
