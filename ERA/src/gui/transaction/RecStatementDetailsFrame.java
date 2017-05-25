package gui.transaction;

import gui.PasswordPane;
import lang.Lang;

import java.awt.Dimension;
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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;

import com.github.rjeschke.txtmark.Processor;

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
public class RecStatementDetailsFrame extends Rec_DetailsFrame
{
	private JTextPane messageText;

	private RecStatementDetailsFrame th;
	
	private static final Logger LOGGER = Logger.getLogger(Send_RecordDetailsFrame.class);
	
	public RecStatementDetailsFrame(final R_SignNote r_Statement)
	{
		super(r_Statement);
		th = this;
		if (r_Statement.getKey() > 0) {
			++labelGBC.gridy;
			++detailGBC.gridy;
			detailGBC.gridx = 1;
			detailGBC.gridwidth = 3;		           
			JTextField note = new JTextField(Controller.getInstance().getNote( r_Statement.getKey()).toString());
			note.setEditable(false);
			MenuPopupUtil.installContextMenu(note);
			this.add(note, detailGBC);	
		}
		
		if (r_Statement.getData() != null) {
			//LABEL MESSAGE
			++labelGBC.gridy;
			JLabel serviceLabel = new JLabel(Lang.getInstance().translate("Message") + ":");
			this.add(serviceLabel, labelGBC);
			
			// ISTEXT
			++detailGBC.gridy;
			detailGBC.gridwidth = 2;
			messageText = new JTextPane();
			messageText.setContentType("text/html");
			String ss = (( r_Statement.isText() ) ? Processor.process(new String(r_Statement.getData(), Charset.forName("UTF-8"))) : Processor.process(Converter.toHex(r_Statement.getData())));
			messageText.setEditable(false);
			//messageText.setSize(200, 300);
			//messageText.setPreferredSize(new Dimension(800,200));
			MenuPopupUtil.installContextMenu(messageText);
	
			
			ss = "<div  style='word-wrap: break-word;'>" +ss;
			
			messageText.setText(ss);
			
			JScrollPane scrol = new JScrollPane();
			
			
		//	scrol.setPreferredSize(new Dimension(800,300));
			int rr = (int) (getFontMetrics( UIManager.getFont("Table.font")).stringWidth(this.signature.getText()));	
			
			scrol.setPreferredSize(new Dimension(rr,300));
			scrol.setViewportView(messageText);
			detailGBC.fill = GridBagConstraints.NONE;
			
			
			
			
			this.add(scrol, detailGBC);
	
			
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
	        
	        encrypted.setSelected(r_Statement.isEncrypted());
	        encrypted.setEnabled(r_Statement.isEncrypted());
	        
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
		        			String password = PasswordPane.showUnlockWalletDialog(th); 
		        			if(!Controller.getInstance().unlockWallet(password))
		        			{
		        				//WRONG PASSWORD
		        				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
		        				
		        				encrypted.setSelected(!encrypted.isSelected());
		        				
		        				return;
		        			}
		        		}
		
		        		Account account = Controller.getInstance().getAccountByAddress(r_Statement.getCreator().getAddress());	
		        		
		        		byte[] privateKey = null; 
		        		byte[] publicKey = null;
	            		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(account.getAddress());
	    				privateKey = accountRecipient.getPrivateKey();		
	    				
	    				publicKey = accountRecipient.getPublicKey();    				
		        		
		        		try {
		        			messageText.setText(new String(AEScrypto.dataDecrypt(r_Statement.getData(), privateKey, publicKey), "UTF-8"));
						} catch (UnsupportedEncodingException | InvalidCipherTextException e1) {
							LOGGER.error(e1.getMessage(),e1);
						}
	        		}
	        		else
	        		{
	        			try {
	        				messageText.setText(new String(r_Statement.getData(), "UTF-8"));
						} catch (UnsupportedEncodingException e1) {
							LOGGER.error(e1.getMessage(),e1);
						}
	        		}
	        		//encrypted.isSelected();
	        		
	        	}
	        });
		}
		        				           
        //PACK
		
	    //    this.setResizable(false);
    //    this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
