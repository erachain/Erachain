package gui.transaction;

import core.transaction.MultiPaymentTransaction;
import gui.Gui;
import gui.models.PaymentsTableModel;
import lang.Lang;
import utils.BigDecimalStringComparator;

import javax.swing.*;
import javax.swing.table.TableRowSorter;

@SuppressWarnings("serial")
public class MultiPaymentDetailsFrame extends Rec_DetailsFrame {
    @SuppressWarnings("unchecked")
    public MultiPaymentDetailsFrame(MultiPaymentTransaction multiPayment) {
        super(multiPayment);

        //LABEL PAYMENTS
        ++labelGBC.gridy;
        JLabel paymentsLabel = new JLabel(Lang.getInstance().translate("Payments") + ":");
        this.add(paymentsLabel, labelGBC);

        //PAYMENTS
        ++detailGBC.gridy;
        PaymentsTableModel paymentsTableModel = new PaymentsTableModel(multiPayment.getPayments());
        JTable table = Gui.createSortableTable(paymentsTableModel, 1);

        TableRowSorter<PaymentsTableModel> sorter = (TableRowSorter<PaymentsTableModel>) table.getRowSorter();
        sorter.setComparator(PaymentsTableModel.COLUMN_AMOUNT, new BigDecimalStringComparator());

        this.add(new JScrollPane(table), detailGBC);

        //PACK
//		this.pack();
        //       this.setResizable(false);
        //       this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
