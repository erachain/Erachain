package org.erachain.gui.items.mails;

import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.util.*;

@SuppressWarnings("serial")
public class TableModelMails extends AbstractTableModel implements Observer {

    static Logger LOGGER = LoggerFactory.getLogger(TableModelMails.class.getName());

    public static final int COLUMN_CONFIRMATION = 0;
    public static final int COLUMN_DATA = 1;
    public static final int COLUMN_SENDER = 3;
    public static final int COLUMN_RECIEVER = 4;
    public static final int COLUMN_HEAD = 2;
    //	public static final int COLUMN_CONFIRM = 5;
    boolean incoming;
    private ArrayList<RSend> transactions;
    private String[] columnNames = Lang.getInstance()
            .translate(new String[]{"Confirmation", "Date", "Title", "Sender", "Reciever"});//, "Confirm" });
    private Boolean[] column_AutuHeight = new Boolean[]{true, false, true, true, false};

    DCSet dcSet;

    public TableModelMails(boolean incoming) {

        dcSet = DCSet.getInstance();
        this.incoming = incoming;
        transactions = new ArrayList<RSend>();
        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getTransactionMap().addObserver(this);

    }

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

    public Transaction getTransaction(int row) {
        return this.transactions.get(row);
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    @Override
    public String getColumnName(int index) {
        return this.columnNames[index];
    }

    @Override
    public int getRowCount() {
        return this.transactions.size();

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.transactions == null || row > this.transactions.size() - 1) {
            return null;
        }

        RSend tran = this.transactions.get(row);

        switch (column) {
            case COLUMN_CONFIRMATION:

                return tran.getConfirmations(dcSet);

            case COLUMN_DATA:


                return DateTimeFormat.timestamptoString(tran.getTimestamp());

            //	case COLUMN_CONFIRM:

            //		return tran.isConfirmed(DLSet.getInstance());

            case COLUMN_SENDER:

                return tran.getCreator().viewPerson();

            case COLUMN_RECIEVER:

                return tran.getRecipient().viewPerson();

            case COLUMN_HEAD:

                return tran.getHead();

        }

        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            // GUI ERROR
            LOGGER.error(e.getMessage(), e);
        }
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        // CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.WALLET_LIST_TRANSACTION_TYPE) {
            filter(message);
            this.fireTableDataChanged();
        }

    }

    public void removeObservers() {

        if (Controller.getInstance().doesWalletDatabaseExists())
            dcSet.getTransactionTab().deleteObserver(this);
    }

    public void filter(ObserverMessage message) {

        ArrayList<Transaction> all_transactions = new ArrayList<Transaction>();


        if (false) {
            for (Account account : Controller.getInstance().getAccounts()) {
                all_transactions.addAll(dcSet.getTransactionFinalMap()
                        .getTransactionsByAddressAndType(account.getShortAddressBytes(), Transaction.SEND_ASSET_TRANSACTION, 0, 0));
            }

            for (Transaction transaction : Controller.getInstance().getUnconfirmedTransactions(300, true)) {
                if (transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
                    all_transactions.add(transaction);
                }
            }


            for (Transaction messagetx : all_transactions) {
                boolean is = false;
                if (!this.transactions.isEmpty()) {
                    for (RSend message1 : this.transactions) {
                        if (Arrays.equals(messagetx.getSignature(), message1.getSignature())) {
                            is = true;
                            break;
                        }
                    }
                }
                if (!is) {

                    if (messagetx.getAssetKey() == 0) {
                        for (Account account1 : Controller.getInstance().getAccounts()) {
                            RSend a = (RSend) messagetx;
                            if (a.getRecipient().getAddress().equals(account1.getAddress()) && incoming) {
                                this.transactions.add(a);
                            }

                            if (a.getCreator().getAddress().equals(account1.getAddress()) && !incoming) {
                                this.transactions.add(a);
                            }

                        }
                    }
                }
            }


            this.transactions.sort(new Comparator<Transaction>() {


                public int compare(Transaction o1, Transaction o2) {
                    // TODO Auto-generated method stub

                    return (int) (o2.getTimestamp() - o1.getTimestamp());
                }
            });

        } else {
            Wallet wallet = Controller.getInstance().wallet;
            Iterator<Fun.Tuple2<Long, Long>> iterator = wallet.getTransactionsIteratorByType(Transaction.SEND_ASSET_TRANSACTION, true);
            if (iterator == null) {
                transactions = new ArrayList<RSend>();
                return;
            }

            RSend rsend;
            boolean outcome;
            Fun.Tuple2<Long, Long> key;
            while (iterator.hasNext()) {
                key = iterator.next();
                rsend = (RSend) wallet.getTransaction(key).b;
                if (rsend == null)
                    continue;
                if (rsend.hasAmount())
                    continue;

                // это исходящее письмо?
                outcome = key.a.equals(Longs.fromByteArray(rsend.getCreator().getShortAddressBytes()));

                if (incoming ^ outcome) {
                    if (rsend.getSignature() != null)
                        rsend.setDC_HeightSeq(dcSet);
                    transactions.add(rsend);
                }
            }
        }
    }
}
