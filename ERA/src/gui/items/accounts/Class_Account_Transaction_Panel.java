package gui.items.accounts;

import gui.AccountRenderer;
import gui.PasswordPane;
import gui.items.assets.AssetsComboBoxModel;
import gui.models.AccountsComboBoxModel;
import gui.models.Send_TableModel;
import gui.transaction.OnDealClick;
import lang.Lang;
import ntp.NTP;

import java.awt.Container;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JInternalFrame;
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

//import settings.Settings;
import utils.Converter;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;
import utils.NameUtils;
import utils.NameUtils.NameResult;
import utils.Pair;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.transaction.Transaction;

@SuppressWarnings("serial")

public class Class_Account_Transaction_Panel extends JPanel
{
	//private final MessagesTableModel messagesTableModel;
   
    // TODO - "A" - &
    static String wrongFirstCharOfAddress = "A";
    
	public JComboBox<Account> cbxFrom;
	public JTextField txtTo;
	public JTextField txtAmount;
	public JTextField txtFeePow;
	public JTextArea txtMessage;
	public JCheckBox encrypted;
	public JCheckBox isText;
	public JButton sendButton;
	public AccountsComboBoxModel accountsModel;
	public JComboBox<AssetCls> cbxFavorites;
	public JTextField txtRecDetails;
	public JLabel messageLabel;
	public JLabel icon;
	public JTextArea jTextArea_Title;
	public JLabel toLabel;
	public JLabel recDetailsLabel;
	int y;

	public JTextField txt_Title;
	
	public Class_Account_Transaction_Panel()
	{
		
		y =0;
		GridBagLayout gridBagLayout = new GridBagLayout();
	//	gridBagLayout.columnWidths = new int[]{0, 112, 140, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.1, 0.1, 0.1, 0.1};
		this.setLayout(gridBagLayout);
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		//icon
		GridBagConstraints iconlabelGBC = new GridBagConstraints();
		iconlabelGBC.insets = new Insets(5,5,5,5);
		iconlabelGBC.fill = GridBagConstraints.BOTH;//.HORIZONTAL;   
		iconlabelGBC.anchor = GridBagConstraints.NORTHWEST;
		iconlabelGBC.weightx = 1;	
	//	iconlabelGBC.weighty = 0.1;
		iconlabelGBC.gridx = 0;
		iconlabelGBC.gridy = y;
		iconlabelGBC.gridwidth = 1;
		
		icon = new JLabel();
		icon.setIcon(new ImageIcon("images/icons/coin.png"));
		this.add(icon, iconlabelGBC);
		
		
		
		
		//title info
				GridBagConstraints titlelabelGBC = new GridBagConstraints();
				titlelabelGBC.insets = new Insets(5,5,5,5);
				titlelabelGBC.fill = GridBagConstraints.BOTH;//.HORIZONTAL;   
				titlelabelGBC.anchor = GridBagConstraints.NORTHWEST;
				titlelabelGBC.weightx = 0;	
				titlelabelGBC.weighty = 0.3;
				titlelabelGBC.gridx = 1;
				titlelabelGBC.gridy = y;
				titlelabelGBC.gridwidth = 4;
				

jTextArea_Title = new javax.swing.JTextArea();

jTextArea_Title.setEditable(false);

jTextArea_Title.setColumns(20);

jTextArea_Title.setRows(1);
jTextArea_Title.setLineWrap(true);
jTextArea_Title.setBackground(this.getBackground());
//jTextArea_Title.setEnabled(false);

jTextArea_Title.setFocusCycleRoot(true);
				
jTextArea_Title.setText(Lang.getInstance().translate("Title"));
//jTextArea_Title.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

				
				this.add(jTextArea_Title, titlelabelGBC);
		
		
		
		//ASSET FAVORITES
		GridBagConstraints favoritesGBC = new GridBagConstraints();
		favoritesGBC.insets = new Insets(5, 5, 5, 0);
		favoritesGBC.fill = GridBagConstraints.BOTH;  
		favoritesGBC.anchor = GridBagConstraints.NORTHWEST;
		favoritesGBC.weightx = 1;
		favoritesGBC.gridwidth = 5;
		favoritesGBC.gridx = 0;	
		favoritesGBC.gridy = ++y;	
		
		cbxFavorites = new JComboBox();
	//	this.add(cbxFavorites, favoritesGBC);
		
		
		this.accountsModel = new AccountsComboBoxModel();
        
		//LABEL FROM
		GridBagConstraints labelFromGBC = new GridBagConstraints();
		labelFromGBC.insets = new Insets(5, 5, 5, 5);
		labelFromGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelFromGBC.anchor = GridBagConstraints.NORTHWEST;
		labelFromGBC.weightx = 0;	
		labelFromGBC.gridx = 0;
		labelFromGBC.gridy = ++y;
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("Select Account") + ":");
		this.add(fromLabel, labelFromGBC);
		//fontHeight = fromLabel.getFontMetrics(fromLabel.getFont()).getHeight();
		
		//COMBOBOX FROM
		GridBagConstraints cbxFromGBC = new GridBagConstraints();
		cbxFromGBC.gridwidth = 4;
		cbxFromGBC.insets = new Insets(5, 5, 5, 0);
		cbxFromGBC.fill = GridBagConstraints.HORIZONTAL;   
		cbxFromGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxFromGBC.weightx = 0;	
		cbxFromGBC.gridx = 1;
		cbxFromGBC.gridy = y;
		
		this.cbxFrom = new JComboBox();
		this.cbxFrom.setRenderer(new AccountRenderer(0));
		this.add(this.cbxFrom, cbxFromGBC);
	
		
		//ON FAVORITES CHANGE

		
		
		//LABEL TO
		GridBagConstraints labelToGBC = new GridBagConstraints();
		labelToGBC.gridy = ++y;
		labelToGBC.insets = new Insets(5,5,5,5);
		labelToGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelToGBC.anchor = GridBagConstraints.NORTHWEST;
		labelToGBC.weightx = 0;	
		labelToGBC.gridx = 0;
		toLabel = new JLabel(Lang.getInstance().translate("To: (address or name)"));
		this.add(toLabel, labelToGBC);
      	
      	//TXT TO
		GridBagConstraints txtToGBC = new GridBagConstraints();
		txtToGBC.gridwidth = 4;
		txtToGBC.insets = new Insets(5, 5, 5, 0);
		txtToGBC.fill = GridBagConstraints.HORIZONTAL;   
		txtToGBC.anchor = GridBagConstraints.NORTHWEST;
		txtToGBC.weightx = 0;	
		txtToGBC.gridx = 1;
		txtToGBC.gridy = y;

		txtTo = new JTextField();
		this.add(txtTo, txtToGBC);
		
		txtTo.getDocument().addDocumentListener(new DocumentListener() {
            
			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				refreshReceiverDetails();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				refreshReceiverDetails();
			}
        });
      	
		//LABEL RECEIVER
		GridBagConstraints labelDetailsGBC = new GridBagConstraints();
		labelDetailsGBC.gridy = ++y;
		labelDetailsGBC.insets = new Insets(5,5,5,5);
		labelDetailsGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelDetailsGBC.anchor = GridBagConstraints.NORTHWEST;
		labelDetailsGBC.weightx = 0;	
		labelDetailsGBC.gridx = 0;
      	recDetailsLabel = new JLabel(Lang.getInstance().translate("Receiver details") + ":");
      	this.add(recDetailsLabel, labelDetailsGBC);
        
      	//RECEIVER DETAILS 
		GridBagConstraints txtReceiverGBC = new GridBagConstraints();
		txtReceiverGBC.gridwidth = 4;
		txtReceiverGBC.insets = new Insets(5, 5, 5, 0);
		txtReceiverGBC.fill = GridBagConstraints.HORIZONTAL;   
		txtReceiverGBC.anchor = GridBagConstraints.NORTHWEST;
		txtReceiverGBC.weightx = 0;	
		txtReceiverGBC.gridx = 1;
      	txtReceiverGBC.gridy = y;

      	txtRecDetails = new JTextField();
      	txtRecDetails.setEditable(false);
      	this.add(txtRecDetails, txtReceiverGBC);
      	
      	
      //LABEL TITLE
      	GridBagConstraints labelMessageGBC = new GridBagConstraints();
      	labelMessageGBC.insets = new Insets(5,5,5,5);
      	labelMessageGBC.fill = GridBagConstraints.HORIZONTAL;   
      	labelMessageGBC.anchor = GridBagConstraints.NORTHWEST;
      	labelMessageGBC.weightx = 0;	
      	labelMessageGBC.gridx = 0;
      	labelMessageGBC.gridy = ++y;
      	
      	JLabel title_Label = new JLabel(Lang.getInstance().translate("Title") + ":");
      	this.add(title_Label, labelMessageGBC);
      	
		//TXT TITLE
		GridBagConstraints txtMessageGBC = new GridBagConstraints();
		txtMessageGBC.gridwidth = 4;
		txtMessageGBC.insets = new Insets(5, 5, 5, 0);
		txtMessageGBC.fill = GridBagConstraints.HORIZONTAL;   
		txtMessageGBC.anchor = GridBagConstraints.NORTHWEST;
		txtMessageGBC.weightx = 0;	
		txtMessageGBC.gridx = 1;
        txtMessageGBC.gridy = y;
        
        txt_Title = new JTextField();
        
      	this.add(txt_Title, txtMessageGBC);
      	
      	
      	
      	
      	
      	
      	
      	
      	
      	//LABEL MESSAGE
   //   	GridBagConstraints labelMessageGBC = new GridBagConstraints();
      	labelMessageGBC.insets = new Insets(5,5,5,5);
      	labelMessageGBC.fill = GridBagConstraints.HORIZONTAL;   
      	labelMessageGBC.anchor = GridBagConstraints.NORTHWEST;
      	labelMessageGBC.weightx = 0;	
      	labelMessageGBC.gridx = 0;
      	labelMessageGBC.gridy = ++y;
      	
      	messageLabel = new JLabel(Lang.getInstance().translate("Message") + ":");
      	
		//TXT MESSAGE
	//	GridBagConstraints txtMessageGBC = new GridBagConstraints();
		txtMessageGBC.gridwidth = 4;
		txtMessageGBC.insets = new Insets(5, 5, 5, 0);
		txtMessageGBC.fill = GridBagConstraints.HORIZONTAL;   
		txtMessageGBC.anchor = GridBagConstraints.NORTHWEST;
		txtMessageGBC.weightx = 0;	
		txtMessageGBC.gridx = 1;
        txtMessageGBC.gridy = y;
        
        this.txtMessage = new JTextArea();
        this.txtMessage.setRows(4);
        this.txtMessage.setColumns(25);

        this.txtMessage.setBorder(this.txtTo.getBorder());

      	JScrollPane messageScroll = new JScrollPane(this.txtMessage);
      	messageScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      	messageScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(messageScroll, txtMessageGBC);
      	
      	this.add(messageLabel, labelMessageGBC);
      	
		//LABEL ISTEXT
		GridBagConstraints labelIsTextGBC = new GridBagConstraints();
		labelIsTextGBC.gridy = ++y;
		labelIsTextGBC.insets = new Insets(5,5,5,5);
		labelIsTextGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelIsTextGBC.anchor = GridBagConstraints.NORTHWEST;
		labelIsTextGBC.weightx = 0;	
		labelIsTextGBC.gridx = 0;     

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
		isChkTextGBC.gridy = y;
        
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
		labelEncGBC.gridy = y;
		
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
		ChkEncGBC.gridy = y;
		
		encrypted = new JCheckBox();
		encrypted.setSelected(true);
		this.add(encrypted, ChkEncGBC);
		
		
		 //coin TITLE
      	GridBagConstraints labecoin = new GridBagConstraints();
      	labecoin.insets = new Insets(5,5,5,5);
      	labecoin.fill = GridBagConstraints.HORIZONTAL;   
      	labecoin.anchor = GridBagConstraints.NORTHWEST;
      	labecoin.weightx = 0;	
      	labecoin.gridx = 0;
      	labecoin.gridy = ++y;
      	
      	JLabel coin_Label = new JLabel(Lang.getInstance().translate("Asset") + ":");
      	coin_Label.setHorizontalAlignment(SwingConstants.RIGHT);
      	this.add(coin_Label, labecoin);
      	
		//TXT TITLE
		GridBagConstraints txtCoin= new GridBagConstraints();
		txtCoin.gridwidth = 4;
		txtCoin.insets = new Insets(5, 5, 5, 0);
		txtCoin.fill = GridBagConstraints.HORIZONTAL;   
		txtCoin.anchor = GridBagConstraints.NORTHWEST;
		txtCoin.weightx = 0;	
		txtCoin.gridx = 1;
		txtCoin.gridy = y;
        
        
        
      	this.add(cbxFavorites, txtCoin);
		
		
    	//LABEL AMOUNT
		GridBagConstraints amountlabelGBC = new GridBagConstraints();
		amountlabelGBC.insets = new Insets(5,5,5,5);
		amountlabelGBC.fill = GridBagConstraints.HORIZONTAL;   
		amountlabelGBC.anchor = GridBagConstraints.NORTHWEST;
		amountlabelGBC.weightx = 0;	
		amountlabelGBC.gridx = 0;
		amountlabelGBC.gridy = ++y;
		
		final JLabel amountLabel = new JLabel(Lang.getInstance().translate("Amount") + ":");
		amountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(amountLabel, amountlabelGBC);
        
      	//TXT AMOUNT
		GridBagConstraints txtAmountGBC = new GridBagConstraints();
		txtAmountGBC.insets = new Insets(5,5,5,5);
		txtAmountGBC.fill = GridBagConstraints.HORIZONTAL;   
		txtAmountGBC.anchor = GridBagConstraints.NORTHWEST;
		txtAmountGBC.weightx = 0;	
		txtAmountGBC.gridx = 1;
		txtAmountGBC.gridy = y;
		
		txtAmount = new JTextField("0.00000000");
	//	txtAmount.setPreferredSize(new Dimension(130,22));
		this.add(txtAmount, txtAmountGBC);
		
      
		
    	//LABEL GBC
		GridBagConstraints feelabelGBC = new GridBagConstraints();
		feelabelGBC.anchor = GridBagConstraints.NORTHWEST;
		feelabelGBC.gridy = y;
		feelabelGBC.insets = new Insets(5,5,5,5);
		feelabelGBC.fill = GridBagConstraints.BOTH;
		feelabelGBC.weightx = 0;	
		feelabelGBC.gridx = 2;
		final JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee") + ":");
		feeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		feeLabel.setVerticalAlignment(SwingConstants.TOP);
		this.add(feeLabel, feelabelGBC);
		
		//FEE TXT
		GridBagConstraints feetxtGBC = new GridBagConstraints();
		feetxtGBC.fill = GridBagConstraints.BOTH;
		feetxtGBC.insets = new Insets(5, 5, 5, 5);
		feetxtGBC.anchor = GridBagConstraints.NORTH;
		feetxtGBC.gridx = 3;	
		feetxtGBC.gridy = y;

		txtFeePow = new JTextField();
		txtFeePow.setText("0");
	//	txtFeePow.setPreferredSize(new Dimension(130,22));
		this.add(txtFeePow, feetxtGBC);
		
		  //BUTTON SEND
        GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5,5,5,5);
		buttonGBC.fill = GridBagConstraints.BOTH;  
		buttonGBC.anchor = GridBagConstraints.PAGE_START;
		buttonGBC.gridx = 3;
		buttonGBC.gridy = ++y;
        
		sendButton = new JButton(Lang.getInstance().translate("Send"));
   //     sendButton.setPreferredSize(new Dimension(80, 25));
    	
		this.add(sendButton, buttonGBC);


        //CONTEXT MENU
		MenuPopupUtil.installContextMenu(txtTo);
		MenuPopupUtil.installContextMenu(txtFeePow);
		MenuPopupUtil.installContextMenu(txtAmount);
		MenuPopupUtil.installContextMenu(txtMessage);
		MenuPopupUtil.installContextMenu(txtRecDetails);
/*		
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(	new Runnable() { 
			public void run() {
				
				messageLabel.setText("<html>" + Lang.getInstance().translate("Message") + ":<br>("+ txtMessage.getText().length()+")</html>");
				
			}}, 0, 500, TimeUnit.MILLISECONDS);
		
 */
        /*
        this.pack();
		this.setLocationRelativeTo(null);
		this.setMaximizable(true);
		this.setTitle(Lang.getInstance().translate("Persons"));
		this.setClosable(true);
		this.setResizable(true);
		*/
		
	
		//Container parent = this.getParent();
		//this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
		//this.setLocation(20, 20);
	//	this.setIconImages(icons);
		
		//CLOSE
		//setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        //this.setResizable(true);
        //splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
        //this.setVisible(true);

        
	

	
	}
	
	private void refreshReceiverDetails()
	{
		String toValue = txtTo.getText();
		AssetCls asset = ((AssetCls) cbxFavorites.getSelectedItem());
		
		txtRecDetails.setText(Account.getDetails(toValue, asset));
		
		if(false && toValue!=null && toValue.startsWith(wrongFirstCharOfAddress))
		{
			encrypted.setEnabled(false);
			encrypted.setSelected(false);
			isText.setSelected(false);
		}
		else
		{
			encrypted.setEnabled(true);
		}
	}
	
}


