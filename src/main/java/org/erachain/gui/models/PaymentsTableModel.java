package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.payment.Payment;
import org.erachain.lang.Lang;
import org.erachain.utils.NumberAsString;

import javax.swing.table.AbstractTableModel;
import java.util.List;

@SuppressWarnings("serial")
public class PaymentsTableModel extends AbstractTableModel {
    public static final int COLUMN_ASSET = 1;
    public static final int COLUMN_AMOUNT = 2;
    private static final int COLUMN_ACCOUNT = 0;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Account", "Check", "Amount"});
    private List<Payment> payments;

    public PaymentsTableModel(List<Payment> payments) {
        this.payments = payments;
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
        return this.payments.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.payments == null || row > this.payments.size() - 1) {
            return null;
        }

        Payment payment = this.payments.get(row);

        switch (column) {
            case COLUMN_ACCOUNT:

                return payment.getRecipient().getPersonAsString();

            case COLUMN_ASSET:

                AssetCls asset = Controller.getInstance().getAsset(payment.getAsset());
                return asset.toString();

            case COLUMN_AMOUNT:

                return NumberAsString.formatAsString(payment.getAmount());

        }

        return null;
    }
}
