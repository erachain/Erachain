package org.erachain.gui.bank;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.*;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.WalletTableModel;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Observer;

@SuppressWarnings("serial")
// in list of org.erachain.records in wallet
public class PaymentOrdersTableModel extends WalletTableModel<Transaction> implements Observer {

    public static final int COLUMN_CONFIRMATIONS = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_TYPE = 2;
    public static final int COLUMN_CREATOR = 3;
    public static final int COLUMN_ITEM = 4;
    public static final int COLUMN_AMOUNT = 5;
    public static final int COLUMN_RECIPIENT = 6;
    public static final int COLUMN_FEE = 7;
    public static final int COLUMN_SIZE = 8;

    public PaymentOrdersTableModel() {
        super(Controller.getInstance().getWallet().dwSet.getTransactionMap(),
                new String[]{"Confirmation", "Timestamp", "Type", "Creator", "Item", "Amount", "Recipient", "Fee", "Size"},
                new Boolean[]{true, true, true, true, true, true, true, false, false}, true, 1000);

        logger = LoggerFactory.getLogger(PaymentOrdersTableModel.class);
    }

    @Override
    public Object getValueAt(int row, int column) {
        //try
        //{
        if (this.list == null || this.list.size() - 1 < row) {
            return null;
        }


        Transaction transaction = list.get(row);
        if (transaction == null)
            return null;
        //creator = transaction.getCreator();
        String itemName = "";
        if (transaction instanceof TransactionAmount && transaction.getAbsKey() > 0) {
            TransactionAmount transAmo = (TransactionAmount) transaction;
            //recipient = transAmo.getRecipient();
            ItemCls item = DCSet.getInstance().getItemAssetMap().get(transAmo.getAbsKey());
            if (item == null)
                return null;

            itemName = item.toString();
        } else if (transaction instanceof GenesisTransferAssetTransaction) {
            GenesisTransferAssetTransaction transGen = (GenesisTransferAssetTransaction) transaction;
            //recipient = transGen.getRecipient();
            ItemCls item = DCSet.getInstance().getItemAssetMap().get(transGen.getAbsKey());
            if (item == null)
                return null;

            itemName = item.toString();
        } else if (transaction instanceof IssueItemRecord) {
            IssueItemRecord transIssue = (IssueItemRecord) transaction;
            ItemCls item = transIssue.getItem();
            if (item == null)
                return null;

            itemName = item.getShort();
        } else if (transaction instanceof GenesisIssueItemRecord) {
            GenesisIssueItemRecord transIssue = (GenesisIssueItemRecord) transaction;
            ItemCls item = transIssue.getItem();
            if (item == null)
                return null;

            itemName = item.getShort();
        } else if (transaction instanceof RCertifyPubKeys) {
            RCertifyPubKeys certifyPK = (RCertifyPubKeys) transaction;
            //recipient = transAmo.getRecipient();
            ItemCls item = DCSet.getInstance().getItemPersonMap().get(certifyPK.getAbsKey());
            if (item == null)
                return null;

            itemName = item.toString();
        } else if (transaction.viewItemName() != null) {
            itemName = transaction.viewItemName();
        }

        switch (column) {
            case COLUMN_CONFIRMATIONS:

                return transaction.getConfirmations(DCSet.getInstance());

            case COLUMN_TIMESTAMP:


                return DateTimeFormat.timestamptoString(transaction.getTimestamp());//.viewTimestamp(); // + " " + transaction.getTimestamp() / 1000;

            case COLUMN_TYPE:

                return Lang.T(transaction.viewFullTypeName());

            case COLUMN_CREATOR:

                return transaction.viewCreator();

            case COLUMN_ITEM:
                return itemName;

            case COLUMN_AMOUNT:

                BigDecimal amo = transaction.getAmount();
                if (amo == null)
                    return BigDecimal.ZERO;
                return amo;

            case COLUMN_RECIPIENT:

                return transaction.viewRecipient();

            case COLUMN_FEE:

                return transaction.getFee();

            case COLUMN_SIZE:
                return transaction.viewSize(Transaction.FOR_NETWORK);
        }

        return null;

    }
}
