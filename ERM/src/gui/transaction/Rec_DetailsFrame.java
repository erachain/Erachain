package gui.transaction;

import gui.PasswordPane;
import lang.Lang;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.bouncycastle.crypto.InvalidCipherTextException;

import utils.Converter;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.transaction.Transaction;
import database.DBSet;

@SuppressWarnings("serial")
public class Rec_DetailsFrame extends JPanel //JFrame
{

	public GridBagConstraints labelGBC = new GridBagConstraints();
	public GridBagConstraints detailGBC = new GridBagConstraints();
	
	public Rec_DetailsFrame(final Transaction record)
	{
//		super(Lang.getInstance().translate("ERMbase") + " - " + Lang.getInstance().translate(record.viewTypeName()));
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
//		this.setIconImages(icons);
		
		//CLOSE
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
//		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//LABEL GBC
		labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(0, 5, 5, 0);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.FIRST_LINE_START;//..NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;
		
		
		//DETAIL GBC
		detailGBC = new GridBagConstraints();
		detailGBC.insets = new Insets(0, 5, 5, 0);
		detailGBC.fill = GridBagConstraints.HORIZONTAL;  
		detailGBC.anchor = GridBagConstraints.FIRST_LINE_START;//.NORTHWEST;
		detailGBC.weightx = 1;	
		detailGBC.gridwidth = 3;
		detailGBC.gridx = 1;		
		
		
		int componentLevel = 0;
		
		/*
		//LABEL TYPE
		labelGBC.gridy = componentLevel;
		
		JLabel typeLabel = new JLabel(Lang.getInstance().translate("Type") + ":");
		this.add(typeLabel, labelGBC);
		
		//TYPE
		detailGBC.gridy = componentLevel;
		JLabel type = new JLabel(Lang.getInstance().translate("Message Transaction"));
		this.add(type, detailGBC);
		componentLevel ++;
		*/
		
		//LABEL Height + Seq
		labelGBC.gridy = componentLevel;
		JLabel heSeqLabel = new JLabel(Lang.getInstance().translate("Height / Seq.") + ":");
		this.add(heSeqLabel, labelGBC);
				
		//Height + Seq
		DBSet db = DBSet.getInstance();

		detailGBC.gridy = componentLevel++;
		JTextField heSeq = new JTextField(record.viewHeightSeq(db));
		heSeq.setEditable(false);
		MenuPopupUtil.installContextMenu(heSeq);
		this.add(heSeq, detailGBC);

		//LABEL SIGNATURE
		labelGBC.gridy = componentLevel;
		JLabel signatureLabel = new JLabel(Lang.getInstance().translate("Signature") + ":");
		this.add(signatureLabel, labelGBC);
				
		//SIGNATURE
		detailGBC.gridy = componentLevel;
		JTextField signature = new JTextField(Base58.encode(record.getSignature()));
		signature.setEditable(false);
		MenuPopupUtil.installContextMenu(signature);
		this.add(signature, detailGBC);
		
		//LABEL REFERENCE
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel referenceLabel = new JLabel(Lang.getInstance().translate("Reference") + ":");
		this.add(referenceLabel, labelGBC);
						
		//REFERENCE
		detailGBC.gridy = componentLevel;
		JTextField reference = new JTextField(""+record.getReference());
		reference.setEditable(false);
		MenuPopupUtil.installContextMenu(reference);
		this.add(reference, detailGBC);
		
		//LABEL TIMESTAMP
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel timestampLabel = new JLabel(Lang.getInstance().translate("Timestamp") + ":");
		this.add(timestampLabel, labelGBC);
						
		//TIMESTAMP
		detailGBC.gridy = componentLevel;
		JTextField timestamp = new JTextField(DateTimeFormat.timestamptoString(record.getTimestamp()));
		timestamp.setEditable(false);
		MenuPopupUtil.installContextMenu(timestamp);
		this.add(timestamp, detailGBC);
		
		//LABEL CREATOR
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel creatorLabel = new JLabel(Lang.getInstance().translate("Creator") + ":");
		this.add(creatorLabel, labelGBC);
		
		//CREATOR
		detailGBC.gridy = componentLevel;
		JTextField creator = new JTextField(record.getCreator().getAddress());
		creator.setEditable(false);
		MenuPopupUtil.installContextMenu(creator);
		this.add(creator, detailGBC);

		String personStr = record.getCreator().viewPerson();
		if (personStr.length()>0) {
			//LABEL PERSON
			componentLevel++;
			detailGBC.gridy = componentLevel;
			this.add(new JLabel(personStr), detailGBC);
		}

		//LABEL CREATOR PUBLIC KEY
				componentLevel ++;
				labelGBC.gridy = componentLevel;
				JLabel creator_Pub_keyLabel = new JLabel(Lang.getInstance().translate("Creator Publick Key") + ":");
				this.add(creator_Pub_keyLabel, labelGBC);
				
				//CREATOR
				detailGBC.gridy = componentLevel;
				
				byte[] publick_Key = Controller.getInstance().getPublicKeyByAddress(record.getCreator().getAddress());
  				PublicKeyAccount public_Account = new PublicKeyAccount(publick_Key);
  			//	  StringSelection value1 = new StringSelection(public_Account.getBase58());
  			//	String value = value1.toString();
				
				
				JTextField creator_Pub_key = new JTextField(public_Account.getBase58());
				creator_Pub_key.setEditable(false);
				MenuPopupUtil.installContextMenu(creator_Pub_key);
				this.add(creator_Pub_key, detailGBC);

				
		
		
		
		//LABEL FEE POWER
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel feePowLabel = new JLabel(Lang.getInstance().translate("Size & Fee") + ":");
		this.add(feePowLabel, labelGBC);
						
		//FEE POWER
		detailGBC.gridy = componentLevel;
		JTextField feePow = new JTextField(
				String.valueOf(record.getDataLength(false)) + "^" + String.valueOf(record.getFeePow()) + ": "
				+ record.getFee().toPlainString() + " " + core.item.assets.AssetCls.FEE_ABBREV);
		feePow.setEditable(false);
		MenuPopupUtil.installContextMenu(feePow);
		this.add(feePow, detailGBC);	
												
		//LABEL CONFIRMATIONS
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel confirmationsLabel = new JLabel(Lang.getInstance().translate("Confirmations") + ":");
		this.add(confirmationsLabel, labelGBC);
								
		//CONFIRMATIONS
		detailGBC.gridy = componentLevel;
		JLabel confirmations = new JLabel(String.valueOf(record.getConfirmations(DBSet.getInstance())));
		this.add(confirmations, detailGBC);	
		  
	}
}
