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
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
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
import core.transaction.R_Send;

@SuppressWarnings("serial")
public class Send_RecordDetailsFrame extends Rec_DetailsFrame
{
	private JTextField messageText;

	private JScrollPane jScrollPane1;

	private JTextPane jTextArea_Messge;
	
	private static final Logger LOGGER = Logger.getLogger(Send_RecordDetailsFrame.class);
	
	public Send_RecordDetailsFrame(final R_Send r_Send)
	{
		super(r_Send);
				
		//LABEL RECIPIENT
		++labelGBC.gridy;
		JLabel recipientLabel = new JLabel(Lang.getInstance().translate("Recipient") + ":");
		this.add(recipientLabel, labelGBC);
		
		//RECIPIENT
		++detailGBC.gridy;
		JTextField recipient = new JTextField(r_Send.getRecipient().getAddress());
		recipient.setEditable(false);
		MenuPopupUtil.installContextMenu(recipient);
		this.add(recipient, detailGBC);		
		
		String personStr = r_Send.getRecipient().viewPerson();
		if (personStr.length()>0) {
			++labelGBC.gridy;
			++detailGBC.gridy;
			this.add(new JLabel(personStr), detailGBC);
		}
		
		if(r_Send.getHead() != null){
			//LABEL MESSAGE
			++labelGBC.gridy;
			JLabel title_Label = new JLabel(Lang.getInstance().translate("Title") + ":");
			this.add(title_Label, labelGBC);
			
			// ISTEXT
			++detailGBC.gridy;
			detailGBC.gridwidth = 2;
			JTextField head_Text = new JTextField( r_Send.getHead());
			head_Text.setEditable(false);
			MenuPopupUtil.installContextMenu(head_Text);
			this.add(head_Text, detailGBC);		
		}
		

		if (r_Send.getData() != null) {
			//LABEL MESSAGE
			++labelGBC.gridy;
			JLabel serviceLabel = new JLabel(Lang.getInstance().translate("Message") + ":");
			this.add(serviceLabel, labelGBC);
			
			jScrollPane1 = new javax.swing.JScrollPane();
	        //jTextArea_Messge = new javax.swing.JTextArea();
	        jTextArea_Messge = new javax.swing.JTextPane();
			
			
			  jTextArea_Messge.setEditable(false);
				jTextArea_Messge.setContentType("text/html");

		       
		  //      MenuPopupUtil.installContextMenu(jTextArea_Messge);
		   //     jTextArea_Messge.setColumns(20);
		   //     jTextArea_Messge.setRows(5);
		   //     jTextArea_Messge.setLineWrap(true);
		        jTextArea_Messge.setText((r_Send.isText() ) ? new String(r_Send.getData(), Charset.forName("UTF-8")) : Converter.toHex(r_Send.getData()));
		        
		        //jTextArea_Messge.setText();
		        jScrollPane1.setViewportView(jTextArea_Messge);

		        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		        gridBagConstraints.gridx = 1;
		        gridBagConstraints.gridy =detailGBC.gridy+1;
		        gridBagConstraints.gridwidth = 3;
		        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		        gridBagConstraints.weightx = 0.1;
		        gridBagConstraints.weighty = 0.6;
		        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 9);
		        add(jScrollPane1, gridBagConstraints);

			
			
			
			
			
			
			// ISTEXT
			++detailGBC.gridy;
			detailGBC.gridwidth = 2;
			messageText = new JTextField( ( r_Send.isText() ) ? new String(r_Send.getData(), Charset.forName("UTF-8")) : Converter.toHex(r_Send.getData()));
			messageText.setEditable(false);
			MenuPopupUtil.installContextMenu(messageText);
//			this.add(messageText, detailGBC);			
			detailGBC.gridwidth = 3;
			
			
			
			
			
			//ENCRYPTED CHECKBOX
			
			//ENCRYPTED
			GridBagConstraints chcGBC = new GridBagConstraints();
			chcGBC.fill = GridBagConstraints.HORIZONTAL;  
			chcGBC.anchor = GridBagConstraints.NORTHWEST;
			chcGBC.gridy = ++labelGBC.gridy;
			chcGBC.gridx = 2;
			chcGBC.gridwidth = 1;
	        final JCheckBox encrypted = new JCheckBox(Lang.getInstance().translate("Encrypted"));
	        
	        encrypted.setSelected(r_Send.isEncrypted());
	        encrypted.setEnabled(r_Send.isEncrypted());
	        
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
		
		        		Account account = Controller.getInstance().getAccountByAddress(r_Send.getCreator().getAddress());	
		        		
		        		byte[] privateKey = null; 
		        		byte[] publicKey = null;
		        		//IF SENDER ANOTHER
		        		if(account == null)
		        		{
		            		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(r_Send.getRecipient().getAddress());
		    				privateKey = accountRecipient.getPrivateKey();		
		    				
		    				publicKey = r_Send.getCreator().getPublicKey();    				
		        		}
		        		//IF SENDER ME
		        		else
		        		{
		            		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(account.getAddress());
		    				privateKey = accountRecipient.getPrivateKey();		
		    				
		    				publicKey = Controller.getInstance().getPublicKeyByAddress(r_Send.getRecipient().getAddress());    				
		        		}
		        		
		        		try {
		        			jTextArea_Messge.setText(new String(AEScrypto.dataDecrypt(r_Send.getData(), privateKey, publicKey), "UTF-8"));
						} catch (UnsupportedEncodingException | InvalidCipherTextException e1) {
							LOGGER.error(e1.getMessage(),e1);
						}
	        		}
	        		else
	        		{
	        			try {
	        				jTextArea_Messge.setText(new String(r_Send.getData(), "UTF-8"));
						} catch (UnsupportedEncodingException e1) {
							LOGGER.error(e1.getMessage(),e1);
						}
	        		}
	        		//encrypted.isSelected();
	        		
	        	}
	        });
		}
        
		if (r_Send.getAmount() != null) {
			
			String sendType = Lang.getInstance().translate(r_Send.viewSendType());
			//LABEL AMOUNT
			++labelGBC.gridy;
			JLabel amountLabel = new JLabel(sendType + ":");
			this.add(amountLabel, labelGBC);
					
			//AMOUNT
			++detailGBC.gridy;
			detailGBC.gridwidth = 2;
			JTextField amount = new JTextField(r_Send.getAmount().toPlainString());
			amount.setEditable(false);
			MenuPopupUtil.installContextMenu(amount);
			this.add(amount, detailGBC);	
			
			//ASSET
			//detailGBC.gridy;
			detailGBC.gridx = 3;
			detailGBC.gridwidth = 1;
			JTextField asset = new JTextField(Controller.getInstance().getAsset( r_Send.getAbsKey()).toString());
			asset.setEditable(false);
			MenuPopupUtil.installContextMenu(asset);
			this.add(asset, detailGBC);	
			detailGBC.gridx = 1;
			detailGBC.gridwidth = 3;		           
		}
		
        //PACK
	//	this.pack();
    //    this.setResizable(false);
    //    this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
