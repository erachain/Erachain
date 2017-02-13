package gui.items.statement;

import gui.items.assets.AssetsComboBoxModel;
import gui.AccountRenderer;
import gui.PasswordPane;
import gui.items.ComboBoxModelItems;
import gui.models.AccountsComboBoxModel;
import gui.models.Send_TableModel;
import gui.transaction.OnDealClick;
import lang.Lang;
import ntp.NTP;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
import gui.library.My_JFileChooser;
import utils.Compressor_ZIP;
import utils.Converter;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import core.account.Account;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.notes.NoteCls;
import core.transaction.Transaction;

@SuppressWarnings("serial")

public class Issue_Statement_Panel extends JPanel 
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
	private JButton file_Button;
	private AccountsComboBoxModel accountsModel;
	private JComboBox<NoteCls> cbxFavorites;
	private JTextArea txtRecDetails;
	private JLabel messageLabel;
	
	public Issue_Statement_Panel(NoteCls note, Account account)
	{
		
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 112, 140, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		this.setLayout(gridBagLayout);
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		int gridy = 0;
		
		GridBagConstraints favoritesGBC = new GridBagConstraints();
		favoritesGBC.insets = new Insets(5, 5, 5, 5);
		favoritesGBC.fill = GridBagConstraints.BOTH;  
		favoritesGBC.anchor = GridBagConstraints.NORTHWEST;
		favoritesGBC.weightx = 1;
		favoritesGBC.gridwidth = 5;
		favoritesGBC.gridx = 0;	
		favoritesGBC.gridy = gridy++;	
		JLabel label_Title = new JLabel(Lang.getInstance().translate("Issue Statement"));
		label_Title.setHorizontalAlignment(SwingConstants.CENTER);
      	this.add(label_Title, favoritesGBC);
		
		
		
		
	//	GridBagConstraints favoritesGBC = new GridBagConstraints();
		favoritesGBC.insets = new Insets(5, 5, 5, 5);
		favoritesGBC.fill = GridBagConstraints.BOTH;  
		favoritesGBC.anchor = GridBagConstraints.NORTHWEST;
		//favoritesGBC.weightx = 1;
		//favoritesGBC.gridwidth = 5;
		favoritesGBC.gridx = 0;	
		favoritesGBC.gridy = gridy++;	
		JLabel label_Templates = new JLabel(Lang.getInstance().translate("Select Template") + ":");
      	this.add(label_Templates, favoritesGBC);
		
		
		
		
		//NOTE FAVORITES
	//	GridBagConstraints favoritesGBC = new GridBagConstraints();
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
		if (note != null) cbxFavorites.setSelectedItem(note);


		//LABEL RECEIVER
		GridBagConstraints labelDetailsGBC = new GridBagConstraints();
		labelDetailsGBC.gridy = gridy++;
		labelDetailsGBC.insets = new Insets(5, 5, 5, 5);
		labelDetailsGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelDetailsGBC.anchor = GridBagConstraints.NORTHWEST;
		labelDetailsGBC.weightx = 0;	
		labelDetailsGBC.gridx = 0;
      	JLabel recDetailsLabel = new JLabel(Lang.getInstance().translate("Statement Body") + ":");
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
      	
		//LABEL ISTEXT
		GridBagConstraints labelIsTextGBC = new GridBagConstraints();
		labelIsTextGBC.insets = new Insets(5,5,5,5);
		labelIsTextGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelIsTextGBC.anchor = GridBagConstraints.NORTHWEST;
		labelIsTextGBC.weightx = 0;	
		labelIsTextGBC.gridx = 4;
		labelIsTextGBC.gridx = 0;
		labelIsTextGBC.gridy = gridy;

		final JLabel isTextLabel = new JLabel(Lang.getInstance().translate("Text Message") + ":");
      	isTextLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      	this.add(isTextLabel, labelIsTextGBC);
     	
      	
        //TEXT ISTEXT
		GridBagConstraints isChkTextGBC = new GridBagConstraints();
		isChkTextGBC.insets = new Insets(5,5,5,5);
		isChkTextGBC.fill = GridBagConstraints.HORIZONTAL;   
		isChkTextGBC.anchor = GridBagConstraints.NORTHWEST;
		isChkTextGBC.weightx = 0;	
		isChkTextGBC.gridx = 1;
		isChkTextGBC.gridy = gridy++;
        
		isText = new JCheckBox();
        isText.setSelected(true);
        this.add(isText, isChkTextGBC);
        
        
        //BUTTON PACK
        GridBagConstraints buttonPBC = new GridBagConstraints();
        buttonPBC.insets = new Insets(15,5,5,5);
        buttonPBC.fill = GridBagConstraints.HORIZONTAL;  
        buttonPBC.anchor = GridBagConstraints.NORTHEAST;
        buttonPBC.gridx = 2;
        buttonPBC.gridy = gridy;

		file_Button = new JButton(Lang.getInstance().translate("Insert File"));
		file_Button.setPreferredSize(new Dimension(160, 25));
		file_Button.setSize(new Dimension(160, 25));
        this.add(file_Button, buttonPBC);
        file_Button.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        Include_File();
		    }
		});	
        //LABEL ENCRYPTED
		GridBagConstraints labelEncGBC = new GridBagConstraints();
		labelEncGBC.insets = new Insets(5,5,5,5);
		labelEncGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelEncGBC.anchor = GridBagConstraints.NORTHWEST;
		labelEncGBC.weightx = 0;	
		labelEncGBC.gridx = 4;
		labelEncGBC.gridx = 0;
		labelEncGBC.gridy = gridy;
		
		JLabel encLabel = new JLabel(Lang.getInstance().translate("Encrypt Message") + ":");
		//encLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(encLabel, labelEncGBC);
		
        //ENCRYPTED CHECKBOX
		GridBagConstraints ChkEncGBC = new GridBagConstraints();
		ChkEncGBC.insets = new Insets(5,5,5,5);
		ChkEncGBC.fill = GridBagConstraints.HORIZONTAL;   
		ChkEncGBC.anchor = GridBagConstraints.NORTHWEST;
		ChkEncGBC.weightx = 0;	
		ChkEncGBC.gridx = 1;
		ChkEncGBC.gridy = gridy++;
		
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
		labelFromGBC.gridy = gridy;
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("Select Account") + ":");
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
		cbxFromGBC.gridy = gridy++;
		
		this.cbxFrom = new JComboBox<Account>(accountsModel);
		this.cbxFrom.setRenderer(new AccountRenderer(0));
		this.add(this.cbxFrom, cbxFromGBC);
		if (account != null) cbxFrom.setSelectedItem(account);

    	//LABEL GBC
		GridBagConstraints feelabelGBC = new GridBagConstraints();
		feelabelGBC.anchor = GridBagConstraints.NORTHWEST;
		feelabelGBC.gridy = gridy;
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
		feetxtGBC.gridx = 1;	
		feetxtGBC.gridy = gridy++;

		txtFeePow = new JTextField();
		txtFeePow.setText("0");
		txtFeePow.setPreferredSize(new Dimension(130,22));
		this.add(txtFeePow, feetxtGBC);


        //BUTTON SEND
        GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(15,5,5,5);
		buttonGBC.fill = GridBagConstraints.HORIZONTAL;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridx = 0;
		buttonGBC.gridy = gridy;
        
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
   //     GridBagConstraints buttonPBC = new GridBagConstraints();
        buttonPBC.insets = new Insets(15,5,5,5);
        buttonPBC.fill = GridBagConstraints.HORIZONTAL;  
        buttonPBC.anchor = GridBagConstraints.NORTHEAST;
        buttonPBC.gridx = 2;
        buttonPBC.gridy = gridy;

		packButton = new JButton(Lang.getInstance().translate("Sign and Pack"));
		packButton.setPreferredSize(new Dimension(160, 25));
		packButton.setSize(new Dimension(160, 25));
		packButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onPackClick();
		    }
		});	
		this.add(packButton, buttonPBC);

				
		//MESSAGES HISTORY TABLE

    	table = new Statements_Table_Model();
    	
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
		
//        add(scrollPane, messagesGBC);
 
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
			
		int feePow = 0;
		String message = null;
		boolean isTextB = true;
		byte[] messageBytes;
		long key = 0;
		byte[] isTextByte;
		byte[] encrypted;
		
		int parsing = 0;
		try
		{
			//READ AMOUNT
			parsing = 1;
			
			//READ FEE
			parsing = 2;
			feePow = Integer.parseInt(txtFeePow.getText());			
			
			message = txtMessage.getText();
			
			isTextB = isText.isSelected();
						
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

			isTextByte = (isTextB)? new byte[] {1}:new byte[]{0};

			boolean encryptMessage = this.encrypted.isSelected();			
			encrypted = (encryptMessage)?new byte[]{1}:new byte[]{0};
			
			//READ NOTE
			parsing = 5;
			//CHECK IF PAYMENT OR ASSET TRANSFER
			NoteCls note = (NoteCls) this.cbxFavorites.getSelectedItem();
			key = note.getKey(); 

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

			case 5:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Note not exist!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
			}
			return null;
		}

		//CREATE TX MESSAGE
		result = Controller.getInstance().signNote(asPack,
				Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()),
				feePow, key, messageBytes, isTextByte, encrypted);
		
		//CHECK VALIDATE MESSAGE
		if (result.getB() == Transaction.VALIDATE_OK) {
			return result;
		} else {		
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result.getB())), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
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

	private List<String> vars = new ArrayList<String>();

	// find variables in description
	// ...{{Name!format}}...
	// format - 
	private void handleVars(String description) {
		Pattern pattern = Pattern.compile(Pattern.quote("{{") + "(.+?)" + Pattern.quote("}}"));
		//Pattern pattern = Pattern.compile("{{(.+)}}");
		Matcher matcher = pattern.matcher(description);
		while (matcher.find()) {
			String varName = matcher.group(1);
			vars.add(varName);
			//description = description.replace(matcher.group(), getImgHtml(url));
		}
	}
	
// вставить файл	
	private void  Include_File(){
	
		// TODO Auto-generated method stub
				// открыть диалог для файла
				//JFileChooser chooser = new JFileChooser();
				// руссификация диалога выбора файла
				//new All_Options().setUpdateUI(chooser);
				My_JFileChooser chooser = new My_JFileChooser();
				chooser.setDialogTitle(Lang.getInstance().translate("Select File"));
				
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setMultiSelectionEnabled(true);
				int returnVal = chooser.showOpenDialog(getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
// make HASHES from files
					File[] patchs = chooser.getSelectedFiles();
						for (File patch : patchs) {
							String file_name = patch.getPath();
							File file = new File(patch.getPath());

							// преобразуем в байты
							long file_len = file.length();
							if (file_len > Integer.MAX_VALUE) {
								JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("length very long") + " - " + file_name, Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
								return;
							}
							byte[] fileInArray = new byte[(int) file.length()];
							FileInputStream f = null;
							try {
								f = new FileInputStream(patch.getPath());
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("error streaming") + " - " + file_name , Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
								return;
							}
							try {
								f.read(fileInArray);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("error reading") + " - " + file_name  , Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
								return;
							}
							try {
								f.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								return;
							}

							/// HASHING
							String file_hashes = Base58.encode(Crypto.getInstance().digest(fileInArray));
							// extention
							// если в имени файла есть точка и она не является первым символом в названии файла
							String file_extension;
							if(file_name.lastIndexOf(".") != -1 && file_name.lastIndexOf(".") != 0)
							// то вырезаем все знаки после последней точки в названии файла, то есть ХХХХХ.txt -> txt
							file_extension = file_name.substring(file_name.lastIndexOf(".")+1);
							// в противном случае возвращаем заглушку, то есть расширение не найдено
							else file_extension = "";
							// ZIP
							 Compressor_ZIP zip = new Compressor_ZIP();
							byte[] file_content_zip = zip.compress(fileInArray);	
							
							fileInArray=fileInArray;
							
						}
					}
				}
}