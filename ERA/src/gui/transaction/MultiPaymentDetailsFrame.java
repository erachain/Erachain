package gui.transaction;

import gui.Gui;
import gui.models.PaymentsTableModel;
import lang.Lang;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

import core.crypto.Base58;
import core.transaction.MultiPaymentTransaction;
import utils.BigDecimalStringComparator;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class MultiPaymentDetailsFrame extends Rec_DetailsFrame
{
	@SuppressWarnings("unchecked")
	public MultiPaymentDetailsFrame(MultiPaymentTransaction multiPayment)
	{
		super(multiPayment);
			
		//LABEL PAYMENTS
		++labelGBC.gridy;
		JLabel paymentsLabel = new JLabel(Lang.getInstance().translate("Payments") + ":");
		this.add(paymentsLabel, labelGBC);
		
		//PAYMENTS
		++detailGBC.gridy;
		PaymentsTableModel paymentsTableModel = new PaymentsTableModel(multiPayment.getPayments());
		JTable table = Gui.createSortableTable(paymentsTableModel, 1);
		
		TableRowSorter<PaymentsTableModel> sorter =  (TableRowSorter<PaymentsTableModel>) table.getRowSorter();
		sorter.setComparator(PaymentsTableModel.COLUMN_AMOUNT, new BigDecimalStringComparator());
		
		this.add(new JScrollPane(table), detailGBC);
				           
        //PACK
//		this.pack();
 //       this.setResizable(false);
 //       this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
