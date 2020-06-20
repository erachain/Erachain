package org.erachain.gui.transaction;

import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.gui.Gui;
import org.erachain.gui.models.PaymentsTableModel;
import org.erachain.lang.Lang;
import org.erachain.utils.BigDecimalStringComparator;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("serial")
public class ArbitraryTransactionDetailsFrame extends RecDetailsFrame {
    public ArbitraryTransactionDetailsFrame(ArbitraryTransaction arbitraryTransaction) {
        super(arbitraryTransaction, true);


        //LABEL SERVICE
        ++labelGBC.gridy;
        JLabel serviceLabel = new JLabel(Lang.getInstance().translate("Service ID") + ":");
        this.add(serviceLabel, labelGBC);

        //SERVICE
        ++detailGBC.gridy;
        JTextField service = new JTextField(String.valueOf(arbitraryTransaction.getService()));
        service.setEditable(false);
        MenuPopupUtil.installContextMenu(service);
        this.add(service, detailGBC);

        //LABEL DATA AS BASE58
        ++labelGBC.gridy;
        JLabel dataLabel = new JLabel(Lang.getInstance().translate("Data as Base58") + ":");
        this.add(dataLabel, labelGBC);

        //DATA AS BASE58
        ++detailGBC.gridy;
        JTextArea txtAreaData = new JTextArea(Base58.encode(arbitraryTransaction.getData()));
        txtAreaData.setRows(6);
        txtAreaData.setColumns(63);
        txtAreaData.setBorder(service.getBorder());
        txtAreaData.setEditable(false);
        txtAreaData.setLineWrap(true);
        MenuPopupUtil.installContextMenu(txtAreaData);

        JScrollPane AreaDataScroll = new JScrollPane(txtAreaData);
        AreaDataScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        AreaDataScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(AreaDataScroll, detailGBC);

        //LABEL DATA AS TEXT
        ++labelGBC.gridy;
        JLabel dataTextLabel = new JLabel(Lang.getInstance().translate("Data as Text") + ":");
        this.add(dataTextLabel, labelGBC);

        //DATA AS TEXT
        ++detailGBC.gridy;
        JTextArea txtAreaDataText = new JTextArea(new String(arbitraryTransaction.getData(), StandardCharsets.UTF_8));
        txtAreaDataText.setRows(6);
        txtAreaData.setColumns(63);
        txtAreaDataText.setBorder(service.getBorder());
        txtAreaDataText.setEditable(false);
        txtAreaDataText.setLineWrap(true);
        MenuPopupUtil.installContextMenu(txtAreaDataText);

        JScrollPane AreaDataTextScroll = new JScrollPane(txtAreaDataText);
        AreaDataTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        AreaDataTextScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(AreaDataTextScroll, detailGBC);

        if (!arbitraryTransaction.getPayments().isEmpty()) {

            //LABEL PAYMENTS
            ++labelGBC.gridy;
            JLabel paymentsLabel = new JLabel(Lang.getInstance().translate("Payments") + ":");
            this.add(paymentsLabel, labelGBC);

            //PAYMENTS
            ++detailGBC.gridy;
            PaymentsTableModel paymentsTableModel = new PaymentsTableModel(arbitraryTransaction.getPayments());
            JTable table = Gui.createSortableTable(paymentsTableModel, 1);

            @SuppressWarnings("unchecked")
            TableRowSorter<PaymentsTableModel> sorter = (TableRowSorter<PaymentsTableModel>) table.getRowSorter();
            sorter.setComparator(PaymentsTableModel.COLUMN_AMOUNT, new BigDecimalStringComparator());

            this.add(new JScrollPane(table), detailGBC);
        }

        //PACK
//		this.pack();
        //       this.setResizable(false);
//        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
