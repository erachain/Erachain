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
import core.transaction.BuyNameTransaction;
import lang.Lang;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class BuyNameDetailsFrame extends Rec_DetailsFrame
{
	public BuyNameDetailsFrame(BuyNameTransaction namePurchase)
	{
		super(namePurchase);
		
		
		//LABEL SELLER
		++labelGBC.gridy;
		JLabel sellerLabel = new JLabel(Lang.getInstance().translate("Seller") + ":");
		this.add(sellerLabel, labelGBC);
		
		//SELLER
		++detailGBC.gridy;
		JTextField seller = new JTextField(namePurchase.getSeller().getAddress());
		seller.setEditable(false);
		MenuPopupUtil.installContextMenu(seller);
		this.add(seller, detailGBC);

		String personStr = namePurchase.getSeller().viewPerson();
		if (personStr.length()>0) {
			//LABEL PERSON
			++labelGBC.gridy;
			++detailGBC.gridy;
			this.add(new JLabel(personStr), detailGBC);
		}
		
		//LABEL NAME
		++labelGBC.gridy;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
		this.add(nameLabel, labelGBC);
		
		//NAME
		++detailGBC.gridy;
		JTextField name = new JTextField(namePurchase.getNameSale().getKey());
		name.setEditable(false);
		MenuPopupUtil.installContextMenu(name);
		this.add(name, detailGBC);		
		
		//LABEL PRICE
		++labelGBC.gridy;
		JLabel priceLabel = new JLabel(Lang.getInstance().translate("Price") + ":");
		this.add(priceLabel, labelGBC);
				
		//PRICE
		++detailGBC.gridy;
		JTextField price = new JTextField(namePurchase.getNameSale().getAmount().toPlainString());
		price.setEditable(false);
		MenuPopupUtil.installContextMenu(price);
		this.add(price, detailGBC);		
				           
        //PACK
//		this.pack();
//        this.setResizable(false);
//        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
