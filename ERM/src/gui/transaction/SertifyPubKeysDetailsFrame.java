package gui.transaction;
// 30/03
import gui.PasswordPane;
import lang.Lang;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;

import utils.Converter;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.transaction.R_SertifyPubKeys;

@SuppressWarnings("serial")
public class SertifyPubKeysDetailsFrame extends Rec_DetailsFrame
{
	private JTextField messageText;
	
	private static final Logger LOGGER = Logger.getLogger(SertifyPubKeysDetailsFrame.class);
	
	public SertifyPubKeysDetailsFrame(final R_SertifyPubKeys sertifyPubKeysRecord)
	{
		super(sertifyPubKeysRecord);				
        
		//LABEL PERSON
		++labelGBC.gridy;
		JLabel personLabel = new JLabel(Lang.getInstance().translate("Person") + ":");
		this.add(personLabel, labelGBC);

		// PERSON
		++detailGBC.gridy;
		JTextField person = new JTextField(Controller.getInstance().getPerson( sertifyPubKeysRecord.getKey()).toString());
		person.setEditable(false);
		MenuPopupUtil.installContextMenu(person);
		this.add(person, detailGBC);	

		//LABEL to DAYS
		++labelGBC.gridy;
		JLabel amountLabel = new JLabel(Lang.getInstance().translate("End Days") + ":");
		this.add(amountLabel, labelGBC);
				
		//ens DAYS
		++detailGBC.gridy;
		JTextField amount = new JTextField("" + sertifyPubKeysRecord.getAddDay());
		amount.setEditable(false);
		MenuPopupUtil.installContextMenu(amount);
		this.add(amount, detailGBC);	

		int i = 0;
		for (String address: sertifyPubKeysRecord.getSertifiedPublicKeysB58())
		{
			++labelGBC.gridy;
			JLabel lbl = new JLabel( ++i + " :");
			this.add(lbl, labelGBC);
			
			++detailGBC.gridy;
			JTextField txt = new JTextField(address);
			txt.setEditable(false);
			MenuPopupUtil.installContextMenu(txt);
			this.add(txt, detailGBC);	
			
		}
        //PACK
//		this.pack();
 //       this.setResizable(false);
//        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
