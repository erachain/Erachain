package gui.transaction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

import core.crypto.Base58;
import core.transaction.ArbitraryTransaction;
import gui.Gui;
import gui.models.PaymentsTableModel;
import lang.Lang;
import utils.BigDecimalStringComparator;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class ArbitraryTransactionDetailsFrame extends Rec_DetailsFrame
{
	public ArbitraryTransactionDetailsFrame(ArbitraryTransaction arbitraryTransaction)
	{
		super(arbitraryTransaction);
		
		
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
		JTextArea txtAreaDataText = new JTextArea(new String(arbitraryTransaction.getData(), Charset.forName("UTF-8")));
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
		
		if(arbitraryTransaction.getPayments().size() > 0)
		{
			
			//LABEL PAYMENTS
			++labelGBC.gridy;
			JLabel paymentsLabel = new JLabel(Lang.getInstance().translate("Payments") + ":");
			this.add(paymentsLabel, labelGBC);
			
			//PAYMENTS
			++detailGBC.gridy;
			PaymentsTableModel paymentsTableModel = new PaymentsTableModel(arbitraryTransaction.getPayments());
			JTable table = Gui.createSortableTable(paymentsTableModel, 1);
			
			@SuppressWarnings("unchecked")
			TableRowSorter<PaymentsTableModel> sorter =  (TableRowSorter<PaymentsTableModel>) table.getRowSorter();
			sorter.setComparator(PaymentsTableModel.COLUMN_AMOUNT, new BigDecimalStringComparator());
			
			this.add(new JScrollPane(table), detailGBC);
		}
				           
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
