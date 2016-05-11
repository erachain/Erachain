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
import core.transaction.MessageTransaction;

@SuppressWarnings("serial")
public class MessageTransactionDetailsFrame extends Rec_DetailsFrame
{
	private JTextField messageText;
	
	private static final Logger LOGGER = Logger.getLogger(MessageTransactionDetailsFrame.class);
	
	public MessageTransactionDetailsFrame(final MessageTransaction messageTransaction)
	{
		super(messageTransaction);
				
		//LABEL RECIPIENT
		++labelGBC.gridy;
		JLabel recipientLabel = new JLabel(Lang.getInstance().translate("Recipient") + ":");
		this.add(recipientLabel, labelGBC);
		
		//RECIPIENT
		++detailGBC.gridy;
		JTextField recipient = new JTextField(messageTransaction.getRecipient().getAddress());
		recipient.setEditable(false);
		MenuPopupUtil.installContextMenu(recipient);
		this.add(recipient, detailGBC);		
		
		String personStr = messageTransaction.getRecipient().viewPerson();
		if (personStr.length()>0) {
			++labelGBC.gridy;
			++detailGBC.gridy;
			this.add(new JLabel(personStr), detailGBC);
		}

		//LABEL MESSAGE
		++labelGBC.gridy;
		JLabel serviceLabel = new JLabel(Lang.getInstance().translate("Message") + ":");
		this.add(serviceLabel, labelGBC);
		
		// ISTEXT
		++detailGBC.gridy;
		detailGBC.gridwidth = 2;
		messageText = new JTextField( ( messageTransaction.isText() ) ? new String(messageTransaction.getData(), Charset.forName("UTF-8")) : Converter.toHex(messageTransaction.getData()));
		messageText.setEditable(false);
		MenuPopupUtil.installContextMenu(messageText);
		this.add(messageText, detailGBC);			
		detailGBC.gridwidth = 3;
		
		//ENCRYPTED CHECKBOX
		
		//ENCRYPTED
		GridBagConstraints chcGBC = new GridBagConstraints();
		chcGBC.fill = GridBagConstraints.HORIZONTAL;  
		chcGBC.anchor = GridBagConstraints.NORTHWEST;
		chcGBC.gridy = labelGBC.gridy;
		chcGBC.gridx = 3;
		chcGBC.gridwidth = 1;
        final JCheckBox encrypted = new JCheckBox(Lang.getInstance().translate("Encrypted"));
        
        encrypted.setSelected(messageTransaction.isEncrypted());
        encrypted.setEnabled(messageTransaction.isEncrypted());
        
        this.add(encrypted, chcGBC);
        
        encrypted.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		if(!encrypted.isSelected())
        		{
	        		if(!Controller.getInstance().isWalletUnlocked())
	        		{
	        			//ASK FOR PASSWORD
	        			String password = PasswordPane.showUnlockWalletDialog(); 
	        			if(!Controller.getInstance().unlockWallet(password))
	        			{
	        				//WRONG PASSWORD
	        				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
	        				
	        				encrypted.setSelected(!encrypted.isSelected());
	        				
	        				return;
	        			}
	        		}
	
	        		Account account = Controller.getInstance().getAccountByAddress(messageTransaction.getCreator().getAddress());	
	        		
	        		byte[] privateKey = null; 
	        		byte[] publicKey = null;
	        		//IF SENDER ANOTHER
	        		if(account == null)
	        		{
	            		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(messageTransaction.getRecipient().getAddress());
	    				privateKey = accountRecipient.getPrivateKey();		
	    				
	    				publicKey = messageTransaction.getCreator().getPublicKey();    				
	        		}
	        		//IF SENDER ME
	        		else
	        		{
	            		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(account.getAddress());
	    				privateKey = accountRecipient.getPrivateKey();		
	    				
	    				publicKey = Controller.getInstance().getPublicKeyByAddress(messageTransaction.getRecipient().getAddress());    				
	        		}
	        		
	        		try {
	        			messageText.setText(new String(AEScrypto.dataDecrypt(messageTransaction.getData(), privateKey, publicKey), "UTF-8"));
					} catch (UnsupportedEncodingException | InvalidCipherTextException e1) {
						LOGGER.error(e1.getMessage(),e1);
					}
        		}
        		else
        		{
        			try {
        				messageText.setText(new String(messageTransaction.getData(), "UTF-8"));
					} catch (UnsupportedEncodingException e1) {
						LOGGER.error(e1.getMessage(),e1);
					}
        		}
        		//encrypted.isSelected();
        		
        	}
        });	  
        
		//LABEL AMOUNT
		++labelGBC.gridy;
		JLabel amountLabel = new JLabel(Lang.getInstance().translate("Amount") + ":");
		this.add(amountLabel, labelGBC);
				
		//AMOUNT
		++detailGBC.gridy;
		detailGBC.gridwidth = 2;
		JTextField amount = new JTextField(messageTransaction.getAmount().toPlainString());
		amount.setEditable(false);
		MenuPopupUtil.installContextMenu(amount);
		this.add(amount, detailGBC);	
		
		//ASSET
		//detailGBC.gridy;
		detailGBC.gridx = 3;
		detailGBC.gridwidth = 1;
		JTextField asset = new JTextField(Controller.getInstance().getAsset( messageTransaction.getKey()).toString());
		asset.setEditable(false);
		MenuPopupUtil.installContextMenu(asset);
		this.add(asset, detailGBC);	
		detailGBC.gridx = 1;
		detailGBC.gridwidth = 3;
				           
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
