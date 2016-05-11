package gui.transaction;

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
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import core.crypto.Base58;
import core.transaction.CreateOrderTransaction;
import lang.Lang;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class CreateOrderDetailsFrame extends Rec_DetailsFrame
{
	public CreateOrderDetailsFrame(CreateOrderTransaction orderCreation)
	{
		super(orderCreation);
				
		//LABEL HAVE
		++labelGBC.gridy;
		JLabel haveLabel = new JLabel(Lang.getInstance().translate("Have") + ":");
		this.add(haveLabel, labelGBC);
		
		//HAVE
		++detailGBC.gridy;
		JTextField have = new JTextField(String.valueOf(orderCreation.getOrder().getHave()));
		have.setEditable(false);
		MenuPopupUtil.installContextMenu(have);
		this.add(have, detailGBC);
		
		//LABEL WANT
		++labelGBC.gridy;
		JLabel wantLabel = new JLabel(Lang.getInstance().translate("Want") + ":");
		this.add(wantLabel, labelGBC);
		
		//HAVE
		++detailGBC.gridy;
		JTextField want = new JTextField(String.valueOf(orderCreation.getOrder().getWant()));
		want.setEditable(false);
		MenuPopupUtil.installContextMenu(want);
		this.add(want, detailGBC);
		
		//LABEL AMOUNT
		++labelGBC.gridy;
		JLabel amountLabel = new JLabel(Lang.getInstance().translate("Amount") + ":");
		this.add(amountLabel, labelGBC);
				
		//AMOUNT
		++detailGBC.gridy;
		JTextField amount = new JTextField(orderCreation.getOrder().getAmount().toPlainString());
		amount.setEditable(false);
		MenuPopupUtil.installContextMenu(amount);
		this.add(amount, detailGBC);	
		
		//LABEL PRICE
		++labelGBC.gridy;
		JLabel priceLabel = new JLabel(Lang.getInstance().translate("Price") + ":");
		this.add(priceLabel, labelGBC);
				
		//PRICE
		++detailGBC.gridy;
		JTextField price = new JTextField(orderCreation.getOrder().getPrice().toPlainString());
		price.setEditable(false);
		MenuPopupUtil.installContextMenu(price);
		this.add(price, detailGBC);	
				           
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
