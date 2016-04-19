package gui.transaction;

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

import org.bouncycastle.crypto.InvalidCipherTextException;

import utils.Converter;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.transaction.R_SignNote;

@SuppressWarnings("serial")
public class RecStatementDetailsFrame extends JFrame
{
	private JTextField messageText;
	
	public RecStatementDetailsFrame(final R_SignNote recStatement)
	{
		super(Lang.getInstance().translate("DATACHAINS.world") + " - " + Lang.getInstance().translate("Statement Details"));
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(0, 5, 5, 0);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;
		
		
		//DETAIL GBC
		GridBagConstraints detailGBC = new GridBagConstraints();
		detailGBC.insets = new Insets(0, 5, 5, 0);
		detailGBC.fill = GridBagConstraints.HORIZONTAL;  
		detailGBC.anchor = GridBagConstraints.NORTHWEST;
		detailGBC.weightx = 1;	
		detailGBC.gridwidth = 3;
		detailGBC.gridx = 1;		
		
		
		int componentLevel = 0;
		//LABEL TYPE
		labelGBC.gridy = componentLevel;
		
		JLabel typeLabel = new JLabel(Lang.getInstance().translate("Type") + ":");
		this.add(typeLabel, labelGBC);
		
		//TYPE
		detailGBC.gridy = componentLevel;
		JLabel type = new JLabel(Lang.getInstance().translate("Message Transaction"));
		this.add(type, detailGBC);
		
		//LABEL SIGNATURE
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel signatureLabel = new JLabel(Lang.getInstance().translate("Signature") + ":");
		this.add(signatureLabel, labelGBC);
				
		//SIGNATURE
		detailGBC.gridy = componentLevel;
		JTextField signature = new JTextField(Base58.encode(recStatement.getSignature()));
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
		JTextField reference = new JTextField(Base58.encode(recStatement.getReference()));
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
		JTextField timestamp = new JTextField(DateTimeFormat.timestamptoString(recStatement.getTimestamp()));
		timestamp.setEditable(false);
		MenuPopupUtil.installContextMenu(timestamp);
		this.add(timestamp, detailGBC);
		
		//LABEL SENDER
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel senderLabel = new JLabel(Lang.getInstance().translate("Creator") + ":");
		this.add(senderLabel, labelGBC);
		
		//SENDER
		detailGBC.gridy = componentLevel;
		JTextField sender = new JTextField(recStatement.getCreator().getAddress());
		sender.setEditable(false);
		MenuPopupUtil.installContextMenu(sender);
		this.add(sender, detailGBC);
		
		//LABEL SERVICE
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel serviceLabel = new JLabel(Lang.getInstance().translate("Message") + ":");
		this.add(serviceLabel, labelGBC);
		
		//ISTEXT
		detailGBC.gridy = componentLevel;
		detailGBC.gridwidth = 2;
		messageText = new JTextField( ( recStatement.isText() ) ? new String(recStatement.getData(), Charset.forName("UTF-8")) : Base58.encode(recStatement.getData()));
		messageText.setEditable(false);
		MenuPopupUtil.installContextMenu(messageText);
		this.add(messageText, detailGBC);			
		detailGBC.gridwidth = 3;
		        		
		//LABEL FEE POWER
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel feePowLabel = new JLabel(Lang.getInstance().translate("Fee Power") + ":");
		this.add(feePowLabel, labelGBC);
						
		//FEE POWER
		detailGBC.gridy = componentLevel;
		JTextField feePow = new JTextField(String.valueOf(recStatement.getFeePow()));
		feePow.setEditable(false);
		MenuPopupUtil.installContextMenu(feePow);
		this.add(feePow, detailGBC);	

		//LABEL FEE
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee") + ":");
		this.add(feeLabel, labelGBC);
						
		//FEE
		detailGBC.gridy = componentLevel;
		JTextField fee = new JTextField(recStatement.getFee().toPlainString());
		fee.setEditable(false);
		MenuPopupUtil.installContextMenu(fee);
		this.add(fee, detailGBC);	
		
		//LABEL CONFIRMATIONS
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel confirmationsLabel = new JLabel(Lang.getInstance().translate("Confirmations") + ":");
		this.add(confirmationsLabel, labelGBC);
								
		//CONFIRMATIONS
		detailGBC.gridy = componentLevel;
		JLabel confirmations = new JLabel(String.valueOf(recStatement.getConfirmations()));
		this.add(confirmations, detailGBC);	
		           
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
