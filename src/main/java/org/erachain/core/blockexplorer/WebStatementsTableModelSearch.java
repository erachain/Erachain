package org.erachain.core.blockexplorer;

import org.erachain.controller.Controller;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple3;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

//import org.erachain.core.transaction.R_SignStatement_old;

public class WebStatementsTableModelSearch extends AbstractTableModel implements Observer {

    public static final int COLUMN_TIMESTAMP = 0;
    // public static final int COLUMN_TYPE = 1;
    public static final int COLUMN_CREATOR = 1;
    public static final int COLUMN_NOTE = 2;
    public static final int COLUMN_BODY = 3;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // public static final int COLUMN_SIGNATURE = 3;
    // public static final int COLUMN_FEE = 3;
    List<Transaction> transactions;

    // private SortableList<byte[], Transaction> transactions;

    private String[] columnNames = new String[]{"Timestamp", "Creator", "Template", "Statement"};// ,
    // AssetCls.FEE_NAME});
    private Boolean[] column_AutuHeight = new Boolean[]{true, true, true, false};
    // private Map<byte[], BlockingQueue<Block>> blocks;

    public WebStatementsTableModelSearch() {
        // transactions = new ArrayList<Transaction>();

        /*
         * for (Transaction transaction :
         * Controller.getInstance().getUnconfirmedTransactions()) {
         * if(transaction.getType() == Transaction.SIGN_NOTE_TRANSACTION); {
         * transactions.add(transaction); } }
         *
         * for (Account account : Controller.getInstance().getAccounts()) {
         * transactions.addAll(DLSet.getInstance().getTransactionFinalMap().
         * getTransactionsByTypeAndAddress(account.getAddress(),
         * Transaction.SIGN_NOTE_TRANSACTION,0));//.SEND_ASSET_TRANSACTION, 0));
         * }
         */
        // Pair<Block, List<Transaction>> result =
        // Controller.getInstance().scanTransactions(null, 0, 0, 0, 0, null);

        // GenesisBlock block = new GenesisBlock();

        // FOR ALL TRANSACTIONS IN BLOCK
        // List<Transaction> transactions = block.getTransactions();

        // BlockChain blockChain = new BlockChain(null);
        // Block lastBlock = blockChain.getLastBlock();

        // blockChain.getBlock(0)

        // private DLSet dcSet;

        // CREATE GENESIS BLOCK
        // genesisBlock = new GenesisBlock();
        // genesisTimestamp = genesisBlock.getTimestamp(null);
        transactions = new ArrayList<Transaction>();
        Controller.getInstance().addObserver(this);
        ///transactions = read_Statement();
        /*
         * // база данных DLSet dcSet = DLSet.getInstance(); // читаем все блоки
         * SortableList<byte[], Block> lists = dcSet.getBlocksHeadMap().getList(); //
         * проходим по блокам for(Pair<byte[], Block> list: lists) {
         *
         * // читаем транзакции из блока db_transactions =
         * (ArrayList<Transaction>) list.getB().getTransactions(); // проходим
         * по транзакциям for (Transaction transaction:db_transactions){ // если
         * ноте то пишем в transactions if(transaction.getType() ==
         * Transaction.SIGN_NOTE_TRANSACTION) transactions.add(transaction);
         *
         * } }
         */

        // this.blocks = new HashMap<byte[], BlockingQueue<Block>>();

        //

        // List<Pair<Account, Block>> blocks =
        // Controller.getInstance().getLastBlocks();
        // JSONArray array = new JSONArray();
        /*
         * for(Pair<Account, Block> block: blocks) { array.add(block.getB());
         * List<Transaction> transactions = block.getTransactions() }
         *
         *
         *
         * for (Transaction transaction :
         * Controller.getInstance().getUnconfirmedTransactions()) {
         * if(transaction.getType() == Transaction.SIGN_NOTE_TRANSACTION); {
         * transactions.add(transaction); } }
         *
         * for (Account account : Controller.getInstance().getAccounts()) {
         * transactions.addAll(DLSet.getInstance().getTransactionFinalMap().
         * getTransactionsByTypeAndAddress(null,
         * Transaction.SIGN_NOTE_TRANSACTION,0));//.SEND_ASSET_TRANSACTION, 0));
         * }
         *
         *
         */

        int a = 10;

    }

    // set class

    public Class<? extends Object> getColumnClass(int c) { // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] get_Column_AutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void set_get_Column_AutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }

    @Override
    public int getColumnCount() {
        // TODO Auto-generated method stub
        return this.columnNames.length;
    }

    public Transaction get_Statement(int row) {

        return transactions.get(row);
    }

    @Override
    public String getColumnName(int index) {
        return Lang.getInstance().translate(columnNames[index]);
    }

    public String getColumnNameNO_Translate(int index) {
        return columnNames[index];
    }

    @Override
    public int getRowCount() {
        // TODO Auto-generated method stub
        return transactions.size();
    }

    public String get_person_key(int row) {
        String str = "";
        RSignNote record = (RSignNote) this.transactions.get(row);
        PublicKeyAccount creator = record.getCreator();
        if (creator.isPerson()) {
            return creator.getPerson().b.getKey() + "";

        }

        return "";
    }

    @Override
    public Object getValueAt(int row, int column) {
        // TODO Auto-generated method stub
        try {
            if (this.transactions == null || this.transactions.size() - 1 < row) {
                return null;
            }

            // Transaction transaction = (RSignNote)this.transactions.get(row);

            RSignNote record = (RSignNote) this.transactions.get(row);

            switch (column) {
                case COLUMN_TIMESTAMP:

                    // return
                    // DateTimeFormat.timestamptoString(transaction.getTimestamp())
                    // + " " + transaction.getTimestamp();
                    return record.viewTimestamp(); // + " " +
                // transaction.getTimestamp() /
                // 1000;

                /*
                 * case COLUMN_TYPE:
                 *
                 * //return Lang.transactionTypes[transaction.getType()]; return
                 * Lang.getInstance().translate(transaction.viewTypeName());
                 */

                case COLUMN_NOTE:
                    ItemCls it = ItemCls.getItem(DCSet.getInstance(), ItemCls.TEMPLATE_TYPE, record.getKey());
                    if (it == null)
                        return "";
                    return it.toString();

                case COLUMN_BODY:

                    if (record.getVersion() == 2) {
                        Tuple3<String, String, JSONObject> a = record.parse_Data_V2_Without_Files();
                        return a.b;
                    }

                    String str = "";
                    try {
                        JSONObject data = (JSONObject) JSONValue
                                .parseWithException(new String(record.getData(), Charset.forName("UTF-8")));
                        str = (String) data.get("!!&_Title");
                        if (str == null)
                            str = (String) data.get("Title");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block

                        str = new String(record.getData(), Charset.forName("UTF-8"));
                    }
                    if (str == null)
                        return "??";
                    if (str.length() > 50)
                        return str.substring(0, 50) + "...";
                    return str;// transaction.viewReference();//.viewProperies();

                // case COLUMN_AMOUNT:

                // return
                // NumberAsString.getInstance().numberAsString(transaction.getAmount(transaction.getCreator()));

                // case COLUMN_FEE:

                // return
                // NumberAsString.getInstance().numberAsString(transaction.getFee());

                case COLUMN_CREATOR:

                    return record.getCreator().getPersonAsString();
            }

            return null;

        } catch (Exception e) {
            // logger.error(e.getMessage(),e);
            return null;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            // this.syncUpdate(o, arg);
        } catch (Exception e) {
            // GUI ERROR
        }
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;
        // System.out.println( message.getType());

        // CHECK IF NEW LIST
        /*
         * if(message.getType() == ObserverMessage.LIST_STATEMENT_TYPE) {
         * if(this.transactions == null) { // this.statuses = (TreeMap<Long,
         * Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>)
         * message.getValue(); // this.statusesMap .registerObserver();
         * //this.imprints.sort(PollMap.NAME_INDEX); transactions =
         * read_Statement(); }
         *
         * this.fireTableDataChanged(); }
         */

        // CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE) {
            // this.statuses = (TreeMap<Long, Stack<Tuple5<Long, Long, byte[],
            // Integer, Integer>>>) message.getValue();
            // transactions = read_Statement();
            Transaction trans = (Transaction) message.getValue();
            if (trans.getType() == Transaction.SIGN_NOTE_TRANSACTION)
                transactions.add(trans);
            this.fireTableDataChanged();
        }

        if (message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE) {
            Transaction trans = (Transaction) message.getValue();
            if (trans.getType() == Transaction.SIGN_NOTE_TRANSACTION && transactions.contains(trans)) {
                transactions.remove(trans);
                this.fireTableDataChanged();
            }

        }
    }

}
