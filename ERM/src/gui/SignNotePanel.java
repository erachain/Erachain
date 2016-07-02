package gui;

import gui.items.assets.AssetsComboBoxModel;
import gui.items.ComboBoxModelItems;
import gui.models.AccountsComboBoxModel;
import gui.models.Send_TableModel;
import lang.Lang;
import ntp.NTP;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import gui.items.notes.ComboBoxModelItemsNotes;
import utils.Converter;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import core.account.Account;
import core.crypto.Base58;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.notes.NoteCls;
import core.transaction.Transaction;

@SuppressWarnings("serial")

public class SignNotePanel extends JPanel 
{
	//private static long NONE_KEY = Transaction.FEE_KEY;
	//private final MessagesTableModel messagesTableModel;
    private final JTable table;
    
	private JComboBox<Account> cbxFrom;
	private JTextField txtFeePow;
	public JTextArea txtMessage;
	private JCheckBox isText;
	private JCheckBox encrypted;
	private JButton sendButton;
	private JButton packButton;
	private AccountsComboBoxModel accountsModel;
	private JComboBox<NoteCls> cbxFavorites;
	private JTextArea txtRecDetails;
	private JLabel messageLabel;
	
	public SignNotePanel()
	{
		
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 112, 140, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		this.setLayout(gridBagLayout);
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		int gridy = 0;
		//NOTE FAVORITES
		GridBagConstraints favoritesGBC = new GridBagConstraints();
		favoritesGBC.insets = new Insets(5, 5, 5, 5);
		favoritesGBC.fill = GridBagConstraints.BOTH;  
		favoritesGBC.anchor = GridBagConstraints.NORTHWEST;
		favoritesGBC.weightx = 1;
		favoritesGBC.gridwidth = 5;
		favoritesGBC.gridx = 0;	
		favoritesGBC.gridy = gridy++;	
		
		//cbxFavorites = new JComboBox<ItemCls>(new ComboBoxModelItems(
		//			ObserverMessage.LIST_NOTE_FAVORITES_TYPE, ItemCls.NOTE_TYPE));
		cbxFavorites = new JComboBox<NoteCls>(new ComboBoxModelItemsNotes());
		this.add(cbxFavorites, favoritesGBC);

		//LABEL RECEIVER
		GridBagConstraints labelDetailsGBC = new GridBagConstraints();
		labelDetailsGBC.gridy = gridy++;
		labelDetailsGBC.insets = new Insets(5, 5, 5, 5);
		labelDetailsGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelDetailsGBC.anchor = GridBagConstraints.NORTHWEST;
		labelDetailsGBC.weightx = 0;	
		labelDetailsGBC.gridx = 0;
      	JLabel recDetailsLabel = new JLabel(Lang.getInstance().translate("Note Body") + ":");
      	this.add(recDetailsLabel, labelDetailsGBC);
        
      	//RECEIVER DETAILS 
		GridBagConstraints txtReceiverGBC = new GridBagConstraints();
		txtReceiverGBC.gridwidth = 6;
		txtReceiverGBC.insets = new Insets(5, 5, 5, 5);
		txtReceiverGBC.fill = GridBagConstraints.BOTH;   
		txtReceiverGBC.anchor = GridBagConstraints.NORTHWEST;
		txtReceiverGBC.weightx = 0;	
		txtReceiverGBC.gridx = 0;
      	txtReceiverGBC.gridy = gridy++;

        this.txtRecDetails = new JTextArea();
        this.txtRecDetails.setRows(6);
      	txtRecDetails.setEditable(false);
        //this.txtRecDetails.setColumns(25);

        //this.txtMessage.setBorder(this.txtFrom.getBorder());

      	JScrollPane notePageScroll = new JScrollPane(this.txtRecDetails);
      	notePageScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      	notePageScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(notePageScroll, txtReceiverGBC);
      	
      	//LABEL MESSAGE
      	GridBagConstraints labelMessageGBC = new GridBagConstraints();
      	labelMessageGBC.insets = new Insets(5,5,5,5);
      	labelMessageGBC.fill = GridBagConstraints.HORIZONTAL;   
      	labelMessageGBC.anchor = GridBagConstraints.NORTHWEST;
      	labelMessageGBC.weightx = 0;	
      	labelMessageGBC.gridx = 0;
      	labelMessageGBC.gridy = gridy++;
      	
      	messageLabel = new JLabel(Lang.getInstance().translate("Message") + ":");
      	
		//TXT MESSAGE
		GridBagConstraints txtMessageGBC = new GridBagConstraints();
		txtMessageGBC.weightx = 2;	
		txtMessageGBC.gridwidth = 0;
		txtMessageGBC.insets = new Insets(5, 5, 5, 5);
		txtMessageGBC.fill = GridBagConstraints.BOTH;   
		txtMessageGBC.anchor = GridBagConstraints.NORTHWEST;
		txtMessageGBC.gridx = 0;
        txtMessageGBC.gridy = gridy++;
        
        this.txtMessage = new JTextArea();
        this.txtMessage.setRows(4);
        this.txtMessage.setColumns(25);
        //this.txtMessage.setBorder(this.txtFrom.getBorder());

      	JScrollPane messageScroll = new JScrollPane(this.txtMessage);
      	messageScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      	messageScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(messageScroll, txtMessageGBC);
      	
      	this.add(messageLabel, labelMessageGBC);
      	
      	/*
		//LABEL ISTEXT
		GridBagConstraints labelIsTextGBC = new GridBagConstraints();
		labelIsTextGBC.gridy = gridy++;
		labelIsTextGBC.insets = new Insets(5,5,5,5);
		labelIsTextGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelIsTextGBC.anchor = GridBagConstraints.NORTHWEST;
		labelIsTextGBC.weightx = 0;	
		labelIsTextGBC.gridx = 0;     

		final JLabel isTextLabel = new JLabel(Lang.getInstance().translate("Text Message") + ":");
      	isTextLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      	this.add(isTextLabel, labelIsTextGBC);
     	*/
      	
        //TEXT ISTEXT
		GridBagConstraints isChkTextGBC = new GridBagConstraints();
		isChkTextGBC.insets = new Insets(5,5,5,5);
		isChkTextGBC.fill = GridBagConstraints.HORIZONTAL;   
		isChkTextGBC.anchor = GridBagConstraints.NORTHWEST;
		isChkTextGBC.weightx = 0;	
		isChkTextGBC.gridx = 1;
		isChkTextGBC.gridy = gridy;
        
		isText = new JCheckBox();
        isText.setSelected(true);
        this.add(isText, isChkTextGBC);

        //LABEL ENCRYPTED
		GridBagConstraints labelEncGBC = new GridBagConstraints();
		labelEncGBC.insets = new Insets(5,5,5,5);
		labelEncGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelEncGBC.anchor = GridBagConstraints.NORTHWEST;
		labelEncGBC.weightx = 0;	
		labelEncGBC.gridx = 4;
		labelEncGBC.gridx = 2;
		labelEncGBC.gridy = 5;
		
		JLabel encLabel = new JLabel(Lang.getInstance().translate("Encrypt Message") + ":");
		encLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(encLabel, labelEncGBC);
		
        //ENCRYPTED CHECKBOX
		GridBagConstraints ChkEncGBC = new GridBagConstraints();
		ChkEncGBC.insets = new Insets(5,5,5,5);
		ChkEncGBC.fill = GridBagConstraints.HORIZONTAL;   
		ChkEncGBC.anchor = GridBagConstraints.NORTHWEST;
		ChkEncGBC.weightx = 0;	
		ChkEncGBC.gridx = 3;
		ChkEncGBC.gridy = 5;
		
		encrypted = new JCheckBox();
		encrypted.setSelected(true);
		this.add(encrypted, ChkEncGBC);
		
		/////
		this.accountsModel = new AccountsComboBoxModel();
        
		//LABEL FROM
		GridBagConstraints labelFromGBC = new GridBagConstraints();
		labelFromGBC.insets = new Insets(5, 5, 5, 5);
		labelFromGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelFromGBC.anchor = GridBagConstraints.NORTHWEST;
		labelFromGBC.weightx = 0;	
		labelFromGBC.gridx = 0;
		labelFromGBC.gridy = gridy++;
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("From") + ":");
		this.add(fromLabel, labelFromGBC);
		//fontHeight = fromLabel.getFontMetrics(fromLabel.getFont()).getHeight();
		
		//COMBOBOX FROM
		GridBagConstraints cbxFromGBC = new GridBagConstraints();
		cbxFromGBC.gridwidth = 4;
		cbxFromGBC.insets = new Insets(5, 5, 5, 5);
		cbxFromGBC.fill = GridBagConstraints.HORIZONTAL;   
		cbxFromGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxFromGBC.weightx = 0;	
		cbxFromGBC.gridx = 1;
		cbxFromGBC.gridy = gridy;
		
		this.cbxFrom = new JComboBox<Account>(accountsModel);
		this.cbxFrom.setRenderer(new AccountRenderer(0));
		this.add(this.cbxFrom, cbxFromGBC);

    	//LABEL GBC
		GridBagConstraints feelabelGBC = new GridBagConstraints();
		feelabelGBC.anchor = GridBagConstraints.NORTHWEST;
		feelabelGBC.gridy = gridy++;
		feelabelGBC.insets = new Insets(5,5,5,5);
		feelabelGBC.fill = GridBagConstraints.HORIZONTAL;
		feelabelGBC.weightx = 0;	
		feelabelGBC.gridx = 0;
		final JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee") + ":");
		//feeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		//feeLabel.setVerticalAlignment(SwingConstants.TOP);
		this.add(feeLabel, feelabelGBC);
		
		//FEE TXT
		GridBagConstraints feetxtGBC = new GridBagConstraints();
		feetxtGBC.fill = GridBagConstraints.BOTH;
		feetxtGBC.insets = new Insets(5, 5, 5, 5);
		feetxtGBC.anchor = GridBagConstraints.NORTH;
		feetxtGBC.gridx = 3;	
		feetxtGBC.gridy = gridy;

		txtFeePow = new JTextField();
		txtFeePow.setText("0");
		txtFeePow.setPreferredSize(new Dimension(130,22));
		this.add(txtFeePow, feetxtGBC);


        //BUTTON SEND
        GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5,5,5,5);
		buttonGBC.fill = GridBagConstraints.HORIZONTAL;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridx = 0;
		buttonGBC.gridy = 11;
        
		sendButton = new JButton(Lang.getInstance().translate("Sign and Send"));
        sendButton.setPreferredSize(new Dimension(160, 25));
    	sendButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onSendClick();
		    }
		});	
		this.add(sendButton, buttonGBC);

        //BUTTON PACK
        GridBagConstraints buttonPBC = new GridBagConstraints();
        buttonPBC.insets = new Insets(5,5,5,5);
        buttonPBC.fill = GridBagConstraints.HORIZONTAL;  
        buttonPBC.anchor = GridBagConstraints.NORTHEAST;
        buttonPBC.gridx = 4;
        buttonPBC.gridy = 11;

		packButton = new JButton(Lang.getInstance().translate("Sign and Pack"));
		packButton.setPreferredSize(new Dimension(160, 25));
		packButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onPackClick();
		    }
		});	
		this.add(packButton, buttonPBC);

				
		//MESSAGES HISTORY TABLE

    	table = new Send_TableModel();
    	
    	table.setTableHeader(null);
    	table.setSelectionBackground(new Color(209, 232, 255, 255));
    	table.setEditingColumn(0);
    	table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(100, 30));
        scrollPane.setWheelScrollingEnabled(true);

        //BOTTOM GBC
		GridBagConstraints messagesGBC = new GridBagConstraints();
		messagesGBC.insets = new Insets(5,5,5,5);
		messagesGBC.fill = GridBagConstraints.BOTH;
		messagesGBC.anchor = GridBagConstraints.NORTHWEST;
		messagesGBC.weightx = 0;	
		messagesGBC.gridx = 0;
		
        //ADD BOTTOM SO IT PUSHES TO TOP
		messagesGBC.gridy = 13;
		messagesGBC.weighty = 4;
		messagesGBC.gridwidth = 5;
		
        add(scrollPane, messagesGBC);
 
        //CONTEXT MENU
		MenuPopupUtil.installContextMenu(txtFeePow);
		MenuPopupUtil.installContextMenu(txtMessage);
		
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(	new Runnable() { 
			public void run() {
				
				messageLabel.setText("<html>" + Lang.getInstance().translate("Message") + ":<br>("+ txtMessage.getText().length()+"/4000)</html>");
				
			}}, 0, 500, TimeUnit.MILLISECONDS);
	}
	
	public Pair<Transaction, Integer> makeDeal(boolean asPack)
	{
		
		//CHECK IF WALLET UNLOCKED
		if(!Controller.getInstance().isWalletUnlocked())
		{
			//ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(); 
			if(password.equals(""))
			{
				return null;
			}
			if(!Controller.getInstance().unlockWallet(password))
			{
				//WRONG PASSWORD
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
								
				return null;
			}
		}
		
		Pair<Transaction, Integer> result;

		//READ SENDER
		Account sender = (Account) cbxFrom.getSelectedItem();
				
		int parsing = 0;
		try
		{
			//READ AMOUNT
			parsing = 1;
			
			//READ FEE
			parsing = 2;
			int feePow = Integer.parseInt(txtFeePow.getText());			
			
			String message = txtMessage.getText();
			
			boolean isTextB = isText.isSelected();
			
			byte[] messageBytes;
			
			if ( isTextB )
			{
				messageBytes = message.getBytes( Charset.forName("UTF-8") );
			}
			else
			{
				try
				{
					messageBytes = Converter.parseHexString( message );
				}
				catch (Exception g)
				{
					try
					{
						messageBytes = Base58.decode(message);
					}
					catch (Exception e)
					{
						JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Message format is not base58 or hex!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
						
					}
					return null;
				}
			}
			if ( messageBytes.length < 10 || messageBytes.length > 4000 )
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Message size exceeded! 10...4000"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
								
				return null;
			}
			
			//Pair<Transaction, Integer> result;

			byte[] isTextByte = (isTextB)? new byte[] {1}:new byte[]{0};

			boolean encryptMessage = encrypted.isSelected();			
			byte[] encrypted = (encryptMessage)?new byte[]{1}:new byte[]{0};
			
			//CHECK IF PAYMENT OR ASSET TRANSFER
			NoteCls note = (NoteCls) this.cbxFavorites.getSelectedItem();
			long key = note.getKey(); 

			//CREATE TX MESSAGE
			result = Controller.getInstance().signNote(asPack,
					Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()),
					feePow, key, messageBytes, isTextByte, encrypted);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OK:
				
				//RESET FIELDS
				
				this.txtMessage.setText("");
				return result;
							
			case Transaction.INVALID_ADDRESS:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid address!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.NEGATIVE_AMOUNT:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Amount must be positive!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.NOT_ENOUGH_FEE:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Not enough balance!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
								
			case Transaction.NO_BALANCE:
			
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Not enough balance!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
				
			default:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Unknown error!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;		
				
			}
		}
		catch(Exception e)
		{
			//CHECK WHERE PARSING ERROR HAPPENED
			switch(parsing)
			{
			case 1:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid amount!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
				
			case 2:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		
		return null;
		
	}

	public void onSendClick()
	{
		this.sendButton.setEnabled(false);
		Pair<Transaction, Integer> result = makeDeal(false);
		if (result != null) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Statement has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
		}
		this.sendButton.setEnabled(true);
	}
	public void onPackClick()
	{
		this.packButton.setEnabled(false);
		Pair<Transaction, Integer> result = makeDeal(true);
		if (result != null) {
			this.txtMessage.setText( Base58.encode(result.getA().toBytes(true, null)));
		}
		
		this.packButton.setEnabled(true);
	}

}


