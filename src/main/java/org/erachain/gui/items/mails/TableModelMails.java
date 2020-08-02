package org.erachain.gui.items.mails;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.WalletTableModel;
import org.erachain.utils.DateTimeFormat;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

@SuppressWarnings("serial")
public class TableModelMails extends WalletTableModel<Transaction> {

    static Logger LOGGER = LoggerFactory.getLogger(TableModelMails.class.getName());

    public static final int COLUMN_CONFIRMATION = 0;
    public static final int COLUMN_DATA = 1;
    public static final int COLUMN_HEAD = 2;
    public static final int COLUMN_SENDER = 3;
    public static final int COLUMN_RECIEVER = 4;
    //	public static final int COLUMN_CONFIRM = 5;
    boolean incoming;
    DCSet dcSet;

    public TableModelMails(boolean incoming) {

        super(Controller.getInstance().wallet.database.getTransactionMap(),
                new String[]{"Confirmation", "Date", "Title", "Sender", "Reciever"},
                new Boolean[]{true, false, true, true, false}, true);
        this.incoming = incoming;
        this.dcSet = DCSet.getInstance();

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        RSend transaction = (RSend) this.list.get(row);

        switch (column) {
            case COLUMN_CONFIRMATION:

                return transaction.getConfirmations(dcSet);

            case COLUMN_DATA:

                return DateTimeFormat.timestamptoString(transaction.getTimestamp());

            case COLUMN_SENDER:

                return transaction.getCreator().viewPerson();

            case COLUMN_RECIEVER:

                return transaction.getRecipient().viewPerson();

            case COLUMN_HEAD:

                return transaction.getHead();

        }

        return null;
    }

    public void getInterval() {


        ArrayList<Transaction> all_transactions = new ArrayList<Transaction>();

        if (false) {
            for (Account account : Controller.getInstance().getWalletAccounts()) {
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
                if (!this.list.isEmpty()) {
                    for (Transaction message1 : this.list) {
                        if (Arrays.equals(messagetx.getSignature(), message1.getSignature())) {
                            is = true;
                            break;
                        }
                    }
                }
                if (!is) {

                    if (messagetx.getAssetKey() == 0) {
                        for (Account account1 : Controller.getInstance().getWalletAccounts()) {
                            RSend a = (RSend) messagetx;
                            if (a.getRecipient().getAddress().equals(account1.getAddress()) && incoming) {
                                this.list.add(a);
                            }

                            if (a.getCreator().getAddress().equals(account1.getAddress()) && !incoming) {
                                this.list.add(a);
                            }

                        }
                    }
                }
            }

            this.list.sort(new Comparator<Transaction>() {
                public int compare(Transaction o1, Transaction o2) {
                    // TODO Auto-generated method stub

                    return (int) (o2.getTimestamp() - o1.getTimestamp());
                }
            });

        } else {

            list = new ArrayList<Transaction>();

            Wallet wallet = Controller.getInstance().wallet;
            Iterator<Fun.Tuple2<Long, Integer>> iterator = ((WTransactionMap) map).getTypeIterator(
                    (byte) Transaction.SEND_ASSET_TRANSACTION, true);
            if (iterator == null) {
                return;
            }

            RSend rsend;
            boolean outcome;
            Fun.Tuple2<Long, Integer> key;
            while (iterator.hasNext()) {
                key = iterator.next();
                try {
                    rsend = (RSend) wallet.getTransaction(key);
                } catch (Exception e) {
                    continue;
                }

                if (rsend == null)
                    continue;
                if (rsend.hasAmount())
                    continue;

                // это исходящее письмо?
                // смотрим по совпадению ключа к котроому трнзакци прилипла и стоит ли он как создатель
                outcome = key.b.equals(rsend.getCreator().hashCode());

                if (incoming ^ outcome) {
                    rsend.setDC(dcSet, false);
                    list.add(rsend);
                }
            }
        }
    }
}
