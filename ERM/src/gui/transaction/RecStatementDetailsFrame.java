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
public class RecStatementDetailsFrame extends Rec_DetailsFrame
{
	private JTextField messageText;
	
	public RecStatementDetailsFrame(final R_SignNote recStatement)
	{
		super(recStatement);
				
		//LABEL MESSAGE
		++labelGBC.gridy;
		JLabel serviceLabel = new JLabel(Lang.getInstance().translate("Message") + ":");
		this.add(serviceLabel, labelGBC);
		
		//ISTEXT
		++detailGBC.gridy;
		detailGBC.gridwidth = 2;
		messageText = new JTextField( ( recStatement.isText() ) ? new String(recStatement.getData(), Charset.forName("UTF-8")) : Base58.encode(recStatement.getData()));
		messageText.setEditable(false);
		MenuPopupUtil.installContextMenu(messageText);
		this.add(messageText, detailGBC);			
		detailGBC.gridwidth = 3;
		        				           
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
