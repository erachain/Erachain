package gui.items.mails;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.transaction.R_Send;
import core.transaction.Transaction;
import gui.AccountRenderer;
import gui.MainFrame;
import gui.PasswordPane;
import gui.items.accounts.Accounts_ComboBox_Model;
import gui.library.Issue_Confirm_Dialog;
import gui.library.MButton;
import gui.models.AccountsComboBoxModel;
import gui.models.Send_TableModel;
import gui.transaction.OnDealClick;
import lang.Lang;
//import settings.Settings;
import utils.Converter;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")

public class Mail_Send_Panel extends JPanel {
	// private final MessagesTableModel messagesTableModel;
	private final JTable table;
	// TODO - "A" - &
	final static String wrongFirstCharOfAddress = "A";

	private JComboBox<Account> cbxFrom;
	private JComboBox cbx_To;
	private JTextField txtTo;
	private JTextField txtAmount;
	private JTextField txtFeePow;
	public JTextArea txtMessage;
	private JCheckBox encrypted;
	private JCheckBox isText;
	private MButton sendButton;
	private AccountsComboBoxModel accountsModel;
	private JTextField txtRecDetails;
	private JLabel messageLabel;
	public JTextField txt_Title;
	int y;
	PersonCls person;
	private Mail_Send_Panel th;

	public Mail_Send_Panel(AssetCls asset, Account account, Account account_To, PersonCls person) {

		th = this;
		this.person = person;
		sendButton = new MButton(Lang.getInstance().translate("Send"), 2);
		y = 0;
		this.setName(Lang.getInstance().translate("Send Mail"));
		if (asset == null) {
			asset = Controller.getInstance().getAsset(1l);
		}
		GridBagLayout gridBagLayout = new GridBagLayout();
		// gridBagLayout.columnWidths = new int[]{0, 112, 140, 0, 0};
		// gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		this.setLayout(gridBagLayout);

		// PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
		// ASSET FAVORITES
		GridBagConstraints favoritesGBC = new GridBagConstraints();
		favoritesGBC.insets = new Insets(5, 5, 5, 0);
		favoritesGBC.fill = GridBagConstraints.BOTH;
		favoritesGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
		favoritesGBC.weightx = 1.0;
		favoritesGBC.gridwidth = 5;
		favoritesGBC.gridx = 0;
		favoritesGBC.gridy = y;

		txtRecDetails = new JTextField();

		this.accountsModel = new AccountsComboBoxModel();

		// LABEL FROM
		GridBagConstraints labelFromGBC = new GridBagConstraints();
		labelFromGBC.insets = new Insets(5, 10, 5, 5);
		labelFromGBC.fill = GridBagConstraints.HORIZONTAL;
		labelFromGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
		labelFromGBC.weightx = 0;
		labelFromGBC.gridx = 0;
		labelFromGBC.gridy = ++y;
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("Select Account") + ":");
		this.add(fromLabel, labelFromGBC);
		// fontHeight =
		// fromLabel.getFontMetrics(fromLabel.getFont()).getHeight();

		// COMBOBOX FROM
		GridBagConstraints cbxFromGBC = new GridBagConstraints();
		cbxFromGBC.gridwidth = 4;
		cbxFromGBC.insets = new Insets(5, 5, 5, 10);
		cbxFromGBC.fill = GridBagConstraints.HORIZONTAL;
		cbxFromGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxFromGBC.weightx = 0.1;
		cbxFromGBC.gridx = 1;
		cbxFromGBC.gridy = y;

		this.cbxFrom = new JComboBox<Account>(accountsModel);
		this.cbxFrom.setRenderer(new AccountRenderer(0));
		this.add(this.cbxFrom, cbxFromGBC);
		if (account != null)
			cbxFrom.setSelectedItem(account);

		// LABEL TO
		GridBagConstraints labelToGBC = new GridBagConstraints();
		labelToGBC.gridy = ++y;
		labelToGBC.insets = new Insets(5, 10, 5, 10);
		labelToGBC.fill = GridBagConstraints.HORIZONTAL;
		labelToGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
		labelToGBC.weightx = 0;
		labelToGBC.gridx = 0;
		JLabel toLabel = new JLabel(Lang.getInstance().translate("To: (address or name)"));
		this.add(toLabel, labelToGBC);

		// TXT TO
		GridBagConstraints txtToGBC = new GridBagConstraints();
		txtToGBC.gridwidth = 4;
		txtToGBC.insets = new Insets(5, 5, 5, 10);
		txtToGBC.fill = GridBagConstraints.HORIZONTAL;
		txtToGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
		txtToGBC.weightx = 0.1;
		txtToGBC.gridx = 1;
		txtToGBC.gridy = y;

		txtTo = new JTextField();
		// if person show selectbox with all adresses for person
		if (person != null) {

			Accounts_ComboBox_Model accounts_To_Model = new Accounts_ComboBox_Model(person.getKey());
			this.cbx_To = new JComboBox(accounts_To_Model);
			if (accounts_To_Model.getSize() != 0) {
				this.add(this.cbx_To, txtToGBC);
				txtTo.setText(cbx_To.getSelectedItem().toString());
				Account account1 = new Account(txtTo.getText());
				txtRecDetails.setText(account1.toString());
				toLabel.setText(Lang.getInstance().translate("Select Account To") + ": ");
				cbx_To.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String str = (String) cbx_To.getSelectedItem();
						if (str != null) {
							txtTo.setText(cbx_To.getSelectedItem().toString());
							refreshReceiverDetails();
						}

					}
				});
			} else {

				this.txtTo.setText("has no Accounts");
				sendButton.setEnabled(false);

			}
		} else {

			if (account_To != null) {
				txtTo.setText(account_To.getAddress());
			}
			this.add(txtTo, txtToGBC);
		}

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

		// LABEL RECEIVER
		GridBagConstraints labelDetailsGBC = new GridBagConstraints();
		labelDetailsGBC.gridy = ++y;
		labelDetailsGBC.insets = new Insets(5, 10, 5, 5);
		labelDetailsGBC.fill = GridBagConstraints.HORIZONTAL;
		labelDetailsGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
		labelDetailsGBC.weightx = 0;
		labelDetailsGBC.gridx = 0;
		JLabel recDetailsLabel = new JLabel(Lang.getInstance().translate("Receiver details") + ":");
		this.add(recDetailsLabel, labelDetailsGBC);

		// RECEIVER DETAILS
		GridBagConstraints txtReceiverGBC = new GridBagConstraints();
		txtReceiverGBC.gridwidth = 4;
		txtReceiverGBC.insets = new Insets(5, 5, 5, 10);
		txtReceiverGBC.fill = GridBagConstraints.HORIZONTAL;
		txtReceiverGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
		txtReceiverGBC.weightx = 0;
		txtReceiverGBC.gridx = 1;
		txtReceiverGBC.gridy = y;

		txtRecDetails.setEditable(false);
		this.add(txtRecDetails, txtReceiverGBC);

		// LABEL TITLE
		GridBagConstraints labelMessageGBC = new GridBagConstraints();
		labelMessageGBC.insets = new Insets(5, 5, 5, 5);
		labelMessageGBC.fill = GridBagConstraints.HORIZONTAL;
		labelMessageGBC.anchor = GridBagConstraints.NORTHWEST;
		labelMessageGBC.weightx = 0;
		labelMessageGBC.gridx = 0;
		labelMessageGBC.gridy = ++y;

		JLabel title_Label = new JLabel(Lang.getInstance().translate("Title") + ":");
		this.add(title_Label, labelMessageGBC);

		// TXT TITLE
		GridBagConstraints txtMessageGBC = new GridBagConstraints();
		txtMessageGBC.gridwidth = 4;
		txtMessageGBC.insets = new Insets(5, 5, 5, 10);
		txtMessageGBC.fill = GridBagConstraints.HORIZONTAL;
		txtMessageGBC.anchor = GridBagConstraints.NORTHWEST;
		txtMessageGBC.weightx = 0;
		txtMessageGBC.gridx = 1;
		txtMessageGBC.gridy = y;

		txt_Title = new JTextField();

		this.add(txt_Title, txtMessageGBC);

		// LABEL MESSAGE
		// GridBagConstraints labelMessageGBC = new GridBagConstraints();
		labelMessageGBC.insets = new Insets(5, 10, 5, 5);
		labelMessageGBC.fill = GridBagConstraints.HORIZONTAL;
		labelMessageGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
		labelMessageGBC.weightx = 0;
		labelMessageGBC.gridx = 0;
		labelMessageGBC.gridy = ++y;

		messageLabel = new JLabel(Lang.getInstance().translate("Message") + ":");

		// TXT MESSAGE
		// GridBagConstraints txtMessageGBC = new GridBagConstraints();
		txtMessageGBC.gridwidth = 4;
		txtMessageGBC.insets = new Insets(5, 5, 5, 10);
		// txtMessageGBC.fill = GridBagConstraints.HORIZONTAL;
		txtMessageGBC.fill = java.awt.GridBagConstraints.BOTH;
		txtMessageGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
		txtMessageGBC.weightx = 0;
		txtMessageGBC.gridx = 1;
		txtMessageGBC.gridy = y;
		txtMessageGBC.weighty = 0.2;

		this.txtMessage = new JTextArea();
		this.txtMessage.setRows(4);
		this.txtMessage.setColumns(25);
		// this.txtMessage.setMinimumSize(new Dimension(200,150));

		this.txtMessage.setBorder(this.txtTo.getBorder());

		JScrollPane messageScroll = new JScrollPane(this.txtMessage);
		// messageScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		// messageScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(messageScroll, txtMessageGBC);

		this.add(messageLabel, labelMessageGBC);

		// LABEL ISTEXT
		GridBagConstraints labelIsTextGBC = new GridBagConstraints();
		labelIsTextGBC.gridy = ++y;
		labelIsTextGBC.insets = new Insets(5, 5, 5, 5);
		labelIsTextGBC.fill = GridBagConstraints.HORIZONTAL;
		labelIsTextGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
		labelIsTextGBC.weightx = 0;
		labelIsTextGBC.gridx = 0;

		final JLabel isTextLabel = new JLabel(Lang.getInstance().translate("Text Message") + ":");
		isTextLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(isTextLabel, labelIsTextGBC);

		// TEXT ISTEXT
		GridBagConstraints isChkTextGBC = new GridBagConstraints();
		isChkTextGBC.insets = new Insets(5, 5, 5, 5);
		isChkTextGBC.fill = GridBagConstraints.HORIZONTAL;
		isChkTextGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
		isChkTextGBC.weightx = 0;
		isChkTextGBC.gridx = 1;
		isChkTextGBC.gridy = y;

		isText = new JCheckBox();
		isText.setSelected(true);
		this.add(isText, isChkTextGBC);

		// LABEL ENCRYPTED
		GridBagConstraints labelEncGBC = new GridBagConstraints();
		labelEncGBC.insets = new Insets(5, 5, 5, 5);
		labelEncGBC.fill = GridBagConstraints.HORIZONTAL;
		labelEncGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
		labelEncGBC.weightx = 0;
		labelEncGBC.gridx = 2;
		labelEncGBC.gridy = y;

		JLabel encLabel = new JLabel(Lang.getInstance().translate("Encrypt Message") + ":");
		encLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(encLabel, labelEncGBC);

		// ENCRYPTED CHECKBOX
		GridBagConstraints ChkEncGBC = new GridBagConstraints();
		ChkEncGBC.insets = new Insets(5, 5, 5, 5);
		ChkEncGBC.fill = GridBagConstraints.HORIZONTAL;
		ChkEncGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
		ChkEncGBC.weightx = 0;
		ChkEncGBC.gridx = 3;
		ChkEncGBC.gridy = y;

		encrypted = new JCheckBox();
		encrypted.setSelected(true);
		this.add(encrypted, ChkEncGBC);

		// LABEL AMOUNT
		GridBagConstraints amountlabelGBC = new GridBagConstraints();
		amountlabelGBC.insets = new Insets(5, 5, 5, 5);
		amountlabelGBC.fill = GridBagConstraints.HORIZONTAL;
		amountlabelGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
		amountlabelGBC.weightx = 0;
		amountlabelGBC.gridx = 0;
		amountlabelGBC.gridy = ++y;

		final JLabel amountLabel = new JLabel(Lang.getInstance().translate("Amount") + ":");
		amountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		// this.add(amountLabel, amountlabelGBC);

		// TXT AMOUNT
		GridBagConstraints txtAmountGBC = new GridBagConstraints();
		txtAmountGBC.insets = new Insets(5, 5, 5, 5);
		txtAmountGBC.fill = GridBagConstraints.HORIZONTAL;
		txtAmountGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
		txtAmountGBC.weightx = 0;
		txtAmountGBC.gridx = 1;
		txtAmountGBC.gridy = y;

		txtAmount = new JTextField("0.00000000");
		txtAmount.setPreferredSize(new Dimension(130, 22));
		// this.add(txtAmount, txtAmountGBC);

		// LABEL GBC
		GridBagConstraints feelabelGBC = new GridBagConstraints();
		feelabelGBC.anchor = GridBagConstraints.EAST;
		feelabelGBC.gridy = y;
		feelabelGBC.insets = new Insets(5, 5, 5, 5);
		feelabelGBC.fill = GridBagConstraints.BOTH;
		feelabelGBC.weightx = 0;
		feelabelGBC.gridx = 2;
		final JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee") + ":");
		feeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		feeLabel.setVerticalAlignment(SwingConstants.TOP);
		this.add(feeLabel, feelabelGBC);

		// FEE TXT
		GridBagConstraints feetxtGBC = new GridBagConstraints();
		feetxtGBC.fill = GridBagConstraints.BOTH;
		feetxtGBC.insets = new Insets(5, 5, 5, 5);
		feetxtGBC.anchor = GridBagConstraints.NORTH;
		feetxtGBC.gridx = 3;
		feetxtGBC.gridy = y;

		txtFeePow = new JTextField();
		txtFeePow.setText("0");
		txtFeePow.setPreferredSize(new Dimension(130, 22));
		this.add(txtFeePow, feetxtGBC);

		// BUTTON DECRYPTALL
		GridBagConstraints decryptAllGBC = new GridBagConstraints();
		decryptAllGBC.insets = new Insets(5, 5, 5, 5);
		decryptAllGBC.fill = GridBagConstraints.HORIZONTAL;
		decryptAllGBC.anchor = GridBagConstraints.NORTHWEST;
		decryptAllGBC.gridwidth = 1;
		decryptAllGBC.gridx = 3;
		decryptAllGBC.gridy = ++y;
		JButton decryptButton = new JButton(Lang.getInstance().translate("Decrypt All"));
		// this.add(decryptButton, decryptAllGBC);

		// BUTTON SEND
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5, 25, 5, 5);
		buttonGBC.fill = GridBagConstraints.BOTH;
		buttonGBC.anchor = GridBagConstraints.PAGE_START;
		buttonGBC.gridx = 2;
		buttonGBC.gridy = y;

		// sendButton.setPreferredSize(new Dimension(80, 25));
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onSendClick();
			}
		});
		this.add(sendButton, buttonGBC);

		// MESSAGES HISTORY TABLE

		table = new Send_TableModel();

		table.setTableHeader(null);
		table.setEditingColumn(0);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(100, 100));
		scrollPane.setWheelScrollingEnabled(true);

		// BOTTOM GBC
		GridBagConstraints messagesGBC = new GridBagConstraints();
		messagesGBC.insets = new Insets(5, 5, 5, 5);
		messagesGBC.fill = GridBagConstraints.BOTH;
		messagesGBC.anchor = GridBagConstraints.NORTHWEST;
		messagesGBC.weightx = 0;
		messagesGBC.gridx = 0;

		// ADD BOTTOM SO IT PUSHES TO TOP
		messagesGBC.gridy = ++y;
		messagesGBC.weighty = 4;
		messagesGBC.gridwidth = 5;

		// add(scrollPane, messagesGBC);

		// BUTTON DECRYPTALL
		decryptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((Send_TableModel) table).CryptoOpenBoxAll();
			}
		});

		// CONTEXT MENU
		MenuPopupUtil.installContextMenu(txtTo);
		MenuPopupUtil.installContextMenu(txtFeePow);
		MenuPopupUtil.installContextMenu(txtAmount);
		MenuPopupUtil.installContextMenu(txtMessage);
		MenuPopupUtil.installContextMenu(txtRecDetails);

		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {

				messageLabel.setText("<html>" + Lang.getInstance().translate("Message") + ":<br>("
						+ txtMessage.getText().length() + ")</html>");

			}
		}, 0, 500, TimeUnit.MILLISECONDS);

		/*
		 * this.pack(); this.setLocationRelativeTo(null);
		 * this.setMaximizable(true);
		 * this.setTitle(Lang.getInstance().translate("Persons"));
		 * this.setClosable(true); this.setResizable(true);
		 */

		// Container parent = this.getParent();
		// this.setSize(new Dimension(
		// (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
		// this.setLocation(20, 20);
		// this.setIconImages(icons);

		// CLOSE
		// setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		// this.setResizable(true);
		// splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
		// this.setVisible(true);
		refreshReceiverDetails();
		this.setMinimumSize(new Dimension(0, 0));

	}

	private void refreshReceiverDetails() {
		String toValue = txtTo.getText();

		AssetCls asset = Controller.getInstance().getAsset(Transaction.FEE_KEY);

		if (toValue.isEmpty()) {
			txtRecDetails.setText("");
			return;
		}

		if (txtTo.getText().equals("has no Addresses")) {
			txtRecDetails.setText(person.viewName() + " " + Lang.getInstance().translate("has no Accounts"));
			return;
		}

		if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
			txtRecDetails.setText(Lang.getInstance().translate("Status must be OK to show receiver details."));
			return;
		}

		Account account = null;
		Tuple2<Account, String> accountRes = Account.tryMakeAccount(toValue);

		// CHECK IF RECIPIENT IS VALID ADDRESS
		if (accountRes.a == null) {
			txtRecDetails.setText(accountRes.b);
		} else {
			account = accountRes.a;

			txtRecDetails.setText(account.toString(asset.getKey()));

			if (account.getBalanceUSE(asset.getKey()).compareTo(BigDecimal.ZERO) == 0
					&& account.getBalanceUSE(Transaction.FEE_KEY).compareTo(BigDecimal.ZERO) == 0) {
				txtRecDetails.setText(Lang.getInstance().translate("Warning!") + " " + txtRecDetails.getText());
			}
		}

		if (false && account != null && account.getAddress().startsWith(wrongFirstCharOfAddress)) {
			encrypted.setEnabled(false);
			encrypted.setSelected(false);
			isText.setSelected(false);
		} else {
			encrypted.setEnabled(true);
		}

	}

	public void onSendClick() {
		// DISABLE
		this.sendButton.setEnabled(false);

		// TODO TEST
		// CHECK IF NETWORK OK
		/*
		 * if(Controller.getInstance().getStatus() != Controller.STATUS_OKE) {
		 * //NETWORK NOT OK JOptionPane.showMessageDialog(null,
		 * "You are unable to send a transaction while synchronizing or while having no connections!"
		 * , "Error", JOptionPane.ERROR_MESSAGE);
		 * 
		 * //ENABLE this.sendButton.setEnabled(true);
		 * 
		 * return; }
		 */

		// CHECK IF WALLET UNLOCKED
		if (!Controller.getInstance().isWalletUnlocked()) {
			// ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(this);
			if (password.equals("")) {
				this.sendButton.setEnabled(true);
				return;
			}
			if (!Controller.getInstance().unlockWallet(password)) {
				// WRONG PASSWORD
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
						Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

				// ENABLE
				this.sendButton.setEnabled(true);
				return;
			}
		}

		// READ SENDER
		Account sender = (Account) cbxFrom.getSelectedItem();

		// READ RECIPIENT
		String recipientAddress = txtTo.getText();

		// ORDINARY RECIPIENT
		Tuple2<Account, String> accountRes = Account.tryMakeAccount(recipientAddress);
		Account recipient = accountRes.a;
		if (recipient == null) {
			JOptionPane.showMessageDialog(null, accountRes.b, Lang.getInstance().translate("Error"),
					JOptionPane.ERROR_MESSAGE);

			// ENABLE
			this.sendButton.setEnabled(true);
			return;
		}

		int parsing = 0;
		int feePow = 0;
		BigDecimal amount = null;
		try {
			// READ AMOUNT
			parsing = 1;
			amount = new BigDecimal(txtAmount.getText());

			// READ FEE
			parsing = 2;
			feePow = Integer.parseInt(txtFeePow.getText());
		} catch (Exception e) {
			// CHECK WHERE PARSING ERROR HAPPENED
			switch (parsing) {
			case 1:

				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid amount!"),
						Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;

			case 2:

				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"),
						Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
			}
			// ENABLE
			this.sendButton.setEnabled(true);
			return;
		}

		String message = txtMessage.getText();

		boolean isTextB = isText.isSelected();

		byte[] messageBytes = null;

		if (message != null && message.length() > 0) {
			if (isTextB) {
				messageBytes = message.getBytes(Charset.forName("UTF-8"));
			} else {
				try {
					messageBytes = Converter.parseHexString(message);
				} catch (Exception g) {
					try {
						messageBytes = Base58.decode(message);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(new JFrame(),
								Lang.getInstance().translate("Message format is not base58 or hex!"),
								Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

						// ENABLE
						this.sendButton.setEnabled(true);
						return;
					}
				}
			}
		}

		// if no TEXT - set null
		if (messageBytes != null && messageBytes.length == 0)
			messageBytes = null;
		// if amount = 0 - set null
		if (amount.compareTo(BigDecimal.ZERO) == 0)
			amount = null;

		boolean encryptMessage = encrypted.isSelected();

		byte[] encrypted = (encryptMessage) ? new byte[] { 1 } : new byte[] { 0 };
		byte[] isTextByte = (isTextB) ? new byte[] { 1 } : new byte[] { 0 };

		AssetCls asset;
		long key = 0l;

		Integer result;

		if (messageBytes != null) {
			if (messageBytes.length > BlockChain.MAX_REC_DATA_BYTES) {
				JOptionPane.showMessageDialog(new JFrame(),
						Lang.getInstance().translate("Message size exceeded!") + " <= MAX",
						Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

				// ENABLE
				this.sendButton.setEnabled(true);
				return;
			}

			if (encryptMessage) {
				// sender
				PrivateKeyAccount account = Controller.getInstance()
						.getPrivateKeyAccountByAddress(sender.getAddress().toString());
				byte[] privateKey = account.getPrivateKey();

				// recipient
				byte[] publicKey = Controller.getInstance().getPublicKeyByAddress(recipient.getAddress());
				if (publicKey == null) {
					JOptionPane.showMessageDialog(new JFrame(),
							Lang.getInstance().translate(
									"The recipient has not yet performed any action in the blockchain.\nYou can't send an encrypted message to him."),
							Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

					// ENABLE
					this.sendButton.setEnabled(true);

					return;
				}

				messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey, publicKey);
			}
		}

		String head = this.txt_Title.getText();
		if (head == null)
			head = "";
		if (head.getBytes(StandardCharsets.UTF_8).length > 256) {

			JOptionPane.showMessageDialog(new JFrame(),
					Lang.getInstance().translate("Title size exceeded!") + " <= 256",
					Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			return;

		}

		// CREATE TX MESSAGE
		Transaction transaction = Controller.getInstance().r_Send(
				Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), feePow, recipient, key,
				amount, head, messageBytes, isTextByte, encrypted);
		// test result = new Pair<Transaction, Integer>(null,
		// Transaction.VALIDATE_OK);

		String Status_text = "<HTML>" + Lang.getInstance().translate("Size") + ":&nbsp;" + transaction.viewSize(false)
				+ " Bytes, ";
		Status_text += "<b>" + Lang.getInstance().translate("Fee") + ":&nbsp;" + transaction.getFee().toString()
				+ " COMPU</b><br></body></HTML>";

		Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(MainFrame.getInstance(), true,
				Lang.getInstance().translate("Send Mail"), (int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2),
				Status_text, Lang.getInstance().translate("Confirmation Transaction") + " "
						+ Lang.getInstance().translate("Send Mail"));

		Mail_Info ww = new Mail_Info((R_Send) transaction);
		ww.jTabbedPane1.setVisible(false);
		dd.jScrollPane1.setViewportView(ww);
		dd.setLocationRelativeTo(th);
		dd.setVisible(true);

		// JOptionPane.OK_OPTION
		if (dd.isConfirm) {

			result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, false);

			// CHECK VALIDATE MESSAGE
			if (result == transaction.VALIDATE_OK) {
				// RESET FIELDS

				if (amount != null && amount.compareTo(BigDecimal.ZERO) == 1) // IF
																				// MORE
																				// THAN
																				// ZERO
				{
					this.txtAmount.setText("0");
				}

				// TODO "A" ??
				if (false && this.txtTo.getText().startsWith(wrongFirstCharOfAddress)) {
					this.txtTo.setText("");
				}

				this.txtMessage.setText("");

				// TODO "A" ??
				if (true || this.txtTo.getText().startsWith(wrongFirstCharOfAddress)) {
					JOptionPane.showMessageDialog(new JFrame(),
							Lang.getInstance().translate("Message and/or payment has been sent!"),
							Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(new JFrame(),
						Lang.getInstance().translate(OnDealClick.resultMess(result)),
						Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
		}
		// ENABLE
		this.sendButton.setEnabled(true);
	}

}
