package gui;

import gui.models.AccountsComboBoxModel;
//import gui.models.AssetsComboBoxModel;
import gui.models.MessagesTableModel;
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

import qora.account.Account;
import qora.crypto.Base58;
import qora.transaction.Transaction;
import utils.Converter;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;
import utils.Pair;
import controller.Controller;

@SuppressWarnings("serial")

public class RecStatementPanel extends JPanel 
{
	private static long FEE_KEY = Transaction.FEE_KEY;
	//private final MessagesTableModel messagesTableModel;
    private final JTable table;
    
	private JComboBox<Account> cbxFrom;
	private JTextField txtFeePow;
	public JTextArea txtMessage;
	private JCheckBox isText;
	private JButton goButton;
	private AccountsComboBoxModel accountsModel;
	private JLabel messageLabel;
	
	public RecStatementPanel()
	{
		
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 112, 140, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		this.setLayout(gridBagLayout);
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		this.accountsModel = new AccountsComboBoxModel();
        
		//LABEL FROM
		GridBagConstraints labelFromGBC = new GridBagConstraints();
		labelFromGBC.insets = new Insets(5, 5, 5, 5);
		labelFromGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelFromGBC.anchor = GridBagConstraints.NORTHWEST;
		labelFromGBC.weightx = 0;	
		labelFromGBC.gridx = 0;
		labelFromGBC.gridy = 1;
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("From:"));
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
		cbxFromGBC.gridy = 1;
		
		this.cbxFrom = new JComboBox<Account>(accountsModel);
		this.cbxFrom.setRenderer(new AccountRenderer(0));
		this.add(this.cbxFrom, cbxFromGBC);
		

      	//LABEL MESSAGE
      	GridBagConstraints labelMessageGBC = new GridBagConstraints();
      	labelMessageGBC.insets = new Insets(5,5,5,5);
      	labelMessageGBC.fill = GridBagConstraints.HORIZONTAL;   
      	labelMessageGBC.anchor = GridBagConstraints.NORTHWEST;
      	labelMessageGBC.weightx = 0;	
      	labelMessageGBC.gridx = 0;
      	labelMessageGBC.gridy = 4;
      	
      	messageLabel = new JLabel(Lang.getInstance().translate("Message:"));
      	
		//TXT MESSAGE
		GridBagConstraints txtMessageGBC = new GridBagConstraints();
		txtMessageGBC.gridwidth = 4;
		txtMessageGBC.insets = new Insets(5, 5, 5, 0);
		txtMessageGBC.fill = GridBagConstraints.HORIZONTAL;   
		txtMessageGBC.anchor = GridBagConstraints.NORTHWEST;
		txtMessageGBC.weightx = 0;	
		txtMessageGBC.gridx = 1;
        txtMessageGBC.gridy = 4;
        
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
		labelIsTextGBC.gridy = 5;
		labelIsTextGBC.insets = new Insets(5,5,5,5);
		labelIsTextGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelIsTextGBC.anchor = GridBagConstraints.NORTHWEST;
		labelIsTextGBC.weightx = 0;	
		labelIsTextGBC.gridx = 0;     

		final JLabel isTextLabel = new JLabel(Lang.getInstance().translate("Text Message:"));
      	isTextLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      	this.add(isTextLabel, labelIsTextGBC);
     	
        //TEXT ISTEXT
		GridBagConstraints isChkTextGBC = new GridBagConstraints();
		isChkTextGBC.insets = new Insets(5,5,5,5);
		isChkTextGBC.fill = GridBagConstraints.HORIZONTAL;   
		isChkTextGBC.anchor = GridBagConstraints.NORTHWEST;
		isChkTextGBC.weightx = 0;	
		isChkTextGBC.gridx = 1;
		isChkTextGBC.gridy = 5;
        
		isText = new JCheckBox();
        isText.setSelected(true);
        this.add(isText, isChkTextGBC);
		
        //BUTTON GO
        GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5,5,5,5);
		buttonGBC.fill = GridBagConstraints.BOTH;  
		buttonGBC.anchor = GridBagConstraints.PAGE_START;
		buttonGBC.gridx = 0;
		buttonGBC.gridy = 11;
        
		goButton = new JButton(Lang.getInstance().translate("Go"));
        goButton.setPreferredSize(new Dimension(80, 25));
    	goButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onSendClick();
		    }
		});	
		this.add(goButton, buttonGBC);
		
    	//LABEL GBC
		GridBagConstraints feelabelGBC = new GridBagConstraints();
		feelabelGBC.anchor = GridBagConstraints.EAST;
		feelabelGBC.gridy = 6;
		feelabelGBC.insets = new Insets(5,5,5,5);
		feelabelGBC.fill = GridBagConstraints.BOTH;
		feelabelGBC.weightx = 0;	
		feelabelGBC.gridx = 2;
		final JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee:"));
		feeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		feeLabel.setVerticalAlignment(SwingConstants.TOP);
		this.add(feeLabel, feelabelGBC);
		
		//FEE TXT
		GridBagConstraints feetxtGBC = new GridBagConstraints();
		feetxtGBC.fill = GridBagConstraints.BOTH;
		feetxtGBC.insets = new Insets(5, 5, 5, 5);
		feetxtGBC.anchor = GridBagConstraints.NORTH;
		feetxtGBC.gridx = 3;	
		feetxtGBC.gridy = 6;

		txtFeePow = new JTextField();
		txtFeePow.setText("3");
		txtFeePow.setPreferredSize(new Dimension(130,22));
		this.add(txtFeePow, feetxtGBC);
				
		//MESSAGES HISTORY TABLE

    	table = new MessagesTableModel();
    	
    	table.setTableHeader(null);
    	table.setSelectionBackground(new Color(209, 232, 255, 255));
    	table.setEditingColumn(0);
    	table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(100, 100));
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
				
				messageLabel.setText("<html>" + Lang.getInstance().translate("Message:") + "<br>("+ txtMessage.getText().length()+"/4000)</html>");
				
			}}, 0, 500, TimeUnit.MILLISECONDS);
	}
	
	public void onSendClick()
	{
		//DISABLE
		this.goButton.setEnabled(false);
		
		
		//CHECK IF WALLET UNLOCKED
		if(!Controller.getInstance().isWalletUnlocked())
		{
			//ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(); 
			if(password.equals(""))
			{
				this.goButton.setEnabled(true);
				return;
			}
			if(!Controller.getInstance().unlockWallet(password))
			{
				//WRONG PASSWORD
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.goButton.setEnabled(true);
				
				return;
			}
		}
		
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
						
						//ENABLE
						this.goButton.setEnabled(true);
					}
					return;
				}
			}
			if ( messageBytes.length < 10 || messageBytes.length > 4000 )
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Message size exceeded! 10...4000"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.goButton.setEnabled(true);
				
				return;
			}
			
			Pair<Transaction, Integer> result;

			byte[] isTextByte = (isTextB)? new byte[] {1}:new byte[]{0};

			//CREATE TX MESSAGE
			result = Controller.getInstance().recStatement(Controller.getInstance()
					.getPrivateKeyAccountByAddress(sender.getAddress()), feePow, messageBytes, isTextByte);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OK:
				
				//RESET FIELDS
				
				this.txtMessage.setText("");
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Statement has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
				break;	
			
			case Transaction.INVALID_ADDRESS:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid address!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.NEGATIVE_AMOUNT:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Amount must be positive!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.NOT_ENOUGH_FEE:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Not enough balance!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.FEE_LESS_REQUIRED:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Fee below the minimum for this size of a transaction!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
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
		
		//ENABLE
		this.goButton.setEnabled(true);
	}
	
}


