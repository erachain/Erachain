package org.erachain.gui.items.mails;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.WalletTableModel;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;

@SuppressWarnings("serial")
public class TableModelMails extends WalletTableModel<Transaction> {

    static Logger LOGGER = LoggerFactory.getLogger(TableModelMails.class.getName());

    public static final int COLUMN_SEQNO = 0;
    public static final int COLUMN_DATA = 1;
    public static final int COLUMN_HEAD = 2;
    public static final int COLUMN_AMOUNT = 3;
    public static final int COLUMN_ASSET = 4;
    public static final int COLUMN_SENDER = 5;
    public static final int COLUMN_RECIEVER = 6;

    boolean incoming;
    DCSet dcSet = DCSet.getInstance();

    public TableModelMails(boolean incoming) {

        super(Controller.getInstance().getWallet().dwSet.getTransactionMap(),
                new String[]{"№", "Date", "Title", "Amount", "Asset", "Sender", "Receiver"},
                new Boolean[]{false, true, true, true, true, true, false}, true, 1000);
        this.incoming = incoming;

    }

    @Override
    protected void updateMap() {
        map = Controller.getInstance().getWallet().dwSet.getTransactionMap();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        RSend transaction = (RSend) this.list.get(row);

        switch (column) {
            case COLUMN_IS_OUTCOME:
                return !incoming;

            case COLUMN_UN_VIEWED:
                return ((WTransactionMap) map).isUnViewed(transaction);

            case COLUMN_CONFIRMATIONS:
                return transaction.getConfirmations(dcSet);

            case COLUMN_SEQNO:
                return transaction.viewHeightSeq();

            case COLUMN_DATA:
                return DateTimeFormat.timestamptoString(transaction.getTimestamp());

            case COLUMN_SENDER:
                return transaction.getCreator().viewPerson();

            case COLUMN_RECIEVER:
                return transaction.getRecipient().viewPerson();

            case COLUMN_HEAD:
                return transaction.getTitle();

            case COLUMN_AMOUNT:
                if (transaction.hasAmount())
                    return transaction.viewAmount();
                return "";

            case COLUMN_ASSET:
                if (transaction.hasAmount())
                    return transaction.getAsset().toString();
                return "";

        }

        return null;
    }

    @Override
    public void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        if (message.getType() == ObserverMessage.WALLET_SYNC_STATUS) {
            needUpdate = true;

        } else if (message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK) {
            needUpdate = true;
        } else {
            super.syncUpdate(o, arg);
        }
    }

    @Override
    protected void repaintConfirms() {
        for (int i = 0; i < list.size(); i++) {
            setValueAt(i, COLUMN_CONFIRMATIONS, list.get(i).getConfirmations(dcSet));
        }
        fireTableDataChanged();
    }

    public void getInterval() {

        list = new ArrayList<Transaction>();

        Wallet wallet = Controller.getInstance().getWallet();
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
                LOGGER.error(e.getMessage(), e);
                continue;
            }

            if (rsend == null)
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
