package gui.items.accounts;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
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
import gui.PasswordPane;
import gui.items.assets.AssetsComboBoxModel;
import gui.library.Issue_Confirm_Dialog;
import gui.library.MButton;
import gui.library.My_JFileChooser;
import gui.models.AccountsComboBoxModel;
import gui.transaction.OnDealClick;
import gui.transaction.Send_RecordDetailsFrame;
import lang.Lang;
//import settings.Settings;
import utils.Converter;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")

public class Account_Send_Panel extends JPanel {
	// private final MessagesTableModel messagesTableModel;
	// private final JTable table;
	// TODO - "A" - &
	final static String wrongFirstCharOfAddress = "A";

	protected JComboBox<Account> cbxFrom;
	protected JComboBox cbx_To;
	public JTextField txtTo;
	public JTextField txtAmount;
	protected JTextField txtFeePow;
	protected JTextArea txtMessage;
	protected JCheckBox encrypted;
	protected JCheckBox isText;
	protected MButton sendButton;
	protected AccountsComboBoxModel accountsModel;
	public JComboBox<AssetCls> cbxFavorites;
	protected JTextField txtRecDetails;
	protected JLabel messageLabel;
	int y;
	public JTextField txt_Title;
	PersonCls person;
	private Transaction transaction;
	public boolean noRecive = false;

	public Account_Send_Panel(AssetCls asset, Account account, Account account_To, PersonCls person) {
		this.person = person;
		sendButton = new MButton(Lang.getInstance().translate("Send"), 2);
		y = 0;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 112, 140, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0 };
		this.setLayout(gridBagLayout);

		// PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));

		txtRecDetails = new JTextField();

		// ASSET FAVORITES
		GridBagConstraints favoritesGBC = new GridBagConstraints();
		favoritesGBC.insets = new Insets(5, 5, 5, 0);
		favoritesGBC.fill = GridBagConstraints.BOTH;
		favoritesGBC.anchor = GridBagConstraints.NORTHWEST;
		favoritesGBC.weightx = 1;
		favoritesGBC.gridwidth = 5;
		favoritesGBC.gridx = 0;
		favoritesGBC.gridy = y;

		if (asset == null) {
			asset = Controller.getInstance().getAsset(2l);
		}

		cbxFavorites = new JComboBox<AssetCls>(new AssetsComboBoxModel());
		// this.add(cbxFavorites, favoritesGBC);
		/// if (asset != null) cbxFavorites.setSelectedItem(asset);
		// favorite combo box
		/// cbxFavorites.setModel(new AssetsComboBoxModel());
		if (asset != null) {
			for (int i = 0; i < cbxFavorites.getItemCount(); i++) {
				AssetCls item = cbxFavorites.getItemAt(i);
				if (item.getKey() == asset.getKey()) {
					// not worked cbxFavorites.setSelectedItem(asset);
					cbxFavorites.setSelectedIndex(i);
					cbxFavorites.setEnabled(true);// .setEditable(false);
					break;
				}
			}
		} else {
			cbxFavorites.setEnabled(true);
		}

		this.accountsModel = new AccountsComboBoxModel();

		// LABEL FROM
		GridBagConstraints labelFromGBC = new GridBagConstraints();
		labelFromGBC.insets = new Insets(5, 5, 5, 5);
		labelFromGBC.fill = GridBagConstraints.HORIZONTAL;
		labelFromGBC.anchor = GridBagConstraints.NORTHWEST;
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
		cbxFromGBC.insets = new Insets(5, 5, 5, 0);
		cbxFromGBC.fill = GridBagConstraints.HORIZONTAL;
		cbxFromGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxFromGBC.weightx = 0;
		cbxFromGBC.gridx = 1;
		cbxFromGBC.gridy = y;

		this.cbxFrom = new JComboBox<Account>(accountsModel);
		this.cbxFrom.setRenderer(new AccountRenderer(0));
		this.add(this.cbxFrom, cbxFromGBC);
		if (account != null)
			cbxFrom.setSelectedItem(account);

		// ON FAVORITES CHANGE

		cbxFavorites.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				AssetCls asset = ((AssetCls) cbxFavorites.getSelectedItem());

				if (asset != null) {
					((AccountRenderer) cbxFrom.getRenderer()).setAsset(asset.getKey());
					cbxFrom.repaint();
					refreshReceiverDetails();
				}

			}
		});

		// LABEL TO
		GridBagConstraints labelToGBC = new GridBagConstraints();
		labelToGBC.gridy = ++y;
		labelToGBC.insets = new Insets(5, 5, 5, 5);
		labelToGBC.fill = GridBagConstraints.HORIZONTAL;
		labelToGBC.anchor = GridBagConstraints.NORTHWEST;
		labelToGBC.weightx = 0;
		labelToGBC.gridx = 0;
		JLabel toLabel = new JLabel(Lang.getInstance().translate("To: (address or name)"));
		this.add(toLabel, labelToGBC);

		// TXT TO
		GridBagConstraints txtToGBC = new GridBagConstraints();
		txtToGBC.gridwidth = 4;
		txtToGBC.insets = new Insets(5, 5, 5, 0);
		txtToGBC.fill = GridBagConstraints.HORIZONTAL;
		txtToGBC.anchor = GridBagConstraints.NORTHWEST;
		txtToGBC.weightx = 0;
		txtToGBC.gridx = 1;
		txtToGBC.gridy = y;

		txtTo = new JTextField();

		// if person show selectbox with all adresses for person
		if (person != null) {

			Accounts_ComboBox_Model accounts_To_Model = new Accounts_ComboBox_Model(person.getKey());
			if (accounts_To_Model.getSize() != 0) {
				this.cbx_To = new JComboBox(accounts_To_Model);
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
		labelDetailsGBC.insets = new Insets(5, 5, 5, 5);
		labelDetailsGBC.fill = GridBagConstraints.HORIZONTAL;
		labelDetailsGBC.anchor = GridBagConstraints.NORTHWEST;
		labelDetailsGBC.weightx = 0;
		labelDetailsGBC.gridx = 0;
		JLabel recDetailsLabel = new JLabel(Lang.getInstance().translate("Receiver details") + ":");
		this.add(recDetailsLabel, labelDetailsGBC);

		// RECEIVER DETAILS
		GridBagConstraints txtReceiverGBC = new GridBagConstraints();
		txtReceiverGBC.gridwidth = 4;
		txtReceiverGBC.insets = new Insets(5, 5, 5, 0);
		txtReceiverGBC.fill = GridBagConstraints.HORIZONTAL;
		txtReceiverGBC.anchor = GridBagConstraints.NORTHWEST;
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
		txtMessageGBC.insets = new Insets(5, 5, 5, 0);
		txtMessageGBC.fill = GridBagConstraints.HORIZONTAL;
		txtMessageGBC.anchor = GridBagConstraints.NORTHWEST;
		txtMessageGBC.weightx = 0;
		txtMessageGBC.gridx = 1;
		txtMessageGBC.gridy = y;

		txt_Title = new JTextField();

		this.add(txt_Title, txtMessageGBC);

		// LABEL MESSAGE
		// GridBagConstraints labelMessageGBC = new GridBagConstraints();
		labelMessageGBC.insets = new Insets(5, 5, 5, 5);
		labelMessageGBC.fill = GridBagConstraints.HORIZONTAL;
		labelMessageGBC.anchor = GridBagConstraints.NORTHWEST;
		labelMessageGBC.weightx = 0;
		labelMessageGBC.gridx = 0;
		labelMessageGBC.gridy = ++y;

		messageLabel = new JLabel(Lang.getInstance().translate("Message") + ":");

		// TXT MESSAGE
		// GridBagConstraints txtMessageGBC = new GridBagConstraints();
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

		// LABEL ISTEXT
		GridBagConstraints labelIsTextGBC = new GridBagConstraints();
		labelIsTextGBC.gridy = ++y;
		labelIsTextGBC.insets = new Insets(5, 5, 5, 5);
		labelIsTextGBC.fill = GridBagConstraints.HORIZONTAL;
		labelIsTextGBC.anchor = GridBagConstraints.NORTHWEST;
		labelIsTextGBC.weightx = 0;
		labelIsTextGBC.gridx = 0;

		final JLabel isTextLabel = new JLabel(Lang.getInstance().translate("Text Message") + ":");
		isTextLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(isTextLabel, labelIsTextGBC);

		// TEXT ISTEXT
		GridBagConstraints isChkTextGBC = new GridBagConstraints();
		isChkTextGBC.insets = new Insets(5, 5, 5, 5);
		isChkTextGBC.fill = GridBagConstraints.HORIZONTAL;
		isChkTextGBC.anchor = GridBagConstraints.NORTHWEST;
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
		labelEncGBC.anchor = GridBagConstraints.NORTHWEST;
		labelEncGBC.weightx = 0;
		labelEncGBC.gridx = 4;
		labelEncGBC.gridx = 2;
		labelEncGBC.gridy = y;

		JLabel encLabel = new JLabel(Lang.getInstance().translate("Encrypt Message") + ":");
		encLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(encLabel, labelEncGBC);

		// ENCRYPTED CHECKBOX
		GridBagConstraints ChkEncGBC = new GridBagConstraints();
		ChkEncGBC.insets = new Insets(5, 5, 5, 5);
		ChkEncGBC.fill = GridBagConstraints.HORIZONTAL;
		ChkEncGBC.anchor = GridBagConstraints.NORTHWEST;
		ChkEncGBC.weightx = 0;
		ChkEncGBC.gridx = 3;
		ChkEncGBC.gridy = y;

		encrypted = new JCheckBox();
		encrypted.setSelected(true);
		this.add(encrypted, ChkEncGBC);

		// LABEL Coin
		GridBagConstraints labelCoin = new GridBagConstraints();
		labelCoin.insets = new Insets(5, 5, 5, 5);
		labelCoin.fill = GridBagConstraints.HORIZONTAL;
		labelCoin.anchor = GridBagConstraints.NORTHWEST;
		labelCoin.weightx = 0;
		labelCoin.gridx = 0;
		labelCoin.gridy = ++y;

		JLabel coin_Label = new JLabel(Lang.getInstance().translate("Asset") + ":");
		coin_Label.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(coin_Label, labelCoin);

		// TXT TITLE
		GridBagConstraints coinMessageGBC = new GridBagConstraints();
		coinMessageGBC.gridwidth = 4;
		coinMessageGBC.insets = new Insets(5, 5, 5, 0);
		coinMessageGBC.fill = GridBagConstraints.HORIZONTAL;
		coinMessageGBC.anchor = GridBagConstraints.NORTHWEST;
		coinMessageGBC.weightx = 0;
		coinMessageGBC.gridx = 1;
		coinMessageGBC.gridy = y;

		// txt_Title = new JTextField()

		this.add(cbxFavorites, coinMessageGBC);

		// LABEL AMOUNT
		GridBagConstraints amountlabelGBC = new GridBagConstraints();
		amountlabelGBC.insets = new Insets(5, 5, 5, 5);
		amountlabelGBC.fill = GridBagConstraints.HORIZONTAL;
		amountlabelGBC.anchor = GridBagConstraints.NORTHWEST;
		amountlabelGBC.weightx = 0;
		amountlabelGBC.gridx = 0;
		amountlabelGBC.gridy = ++y;

		final JLabel amountLabel = new JLabel(Lang.getInstance().translate("Amount") + ":");
		amountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(amountLabel, amountlabelGBC);

		// TXT AMOUNT
		GridBagConstraints txtAmountGBC = new GridBagConstraints();
		txtAmountGBC.insets = new Insets(5, 5, 5, 5);
		txtAmountGBC.fill = GridBagConstraints.HORIZONTAL;
		txtAmountGBC.anchor = GridBagConstraints.NORTHWEST;
		txtAmountGBC.weightx = 0;
		txtAmountGBC.gridx = 1;
		txtAmountGBC.gridy = y;

		txtAmount = new JTextField("0.00000000");
		// txtAmount.setPreferredSize(new Dimension(130,22));
		this.add(txtAmount, txtAmountGBC);

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
		feetxtGBC.gridwidth = 2;

		txtFeePow = new JTextField();
		txtFeePow.setText("0");
		// txtFeePow.setPreferredSize(new Dimension(130,22));
		this.add(txtFeePow, feetxtGBC);

		// BUTTON DECRYPTALL
		GridBagConstraints decryptAllGBC = new GridBagConstraints();
		decryptAllGBC.insets = new Insets(5, 5, 5, 5);
		decryptAllGBC.fill = GridBagConstraints.HORIZONTAL;
		decryptAllGBC.anchor = GridBagConstraints.NORTHWEST;
		decryptAllGBC.gridwidth = 1;
		decryptAllGBC.gridx = 3;
		decryptAllGBC.gridy = ++y;
		MButton decryptButton = new MButton(Lang.getInstance().translate("Decrypt All"), 2);
		decryptButton.setVisible(false);
		this.add(decryptButton, decryptAllGBC);

		// BUTTON SEND
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5, 5, 5, 5);
		buttonGBC.fill = GridBagConstraints.BOTH;
		buttonGBC.anchor = GridBagConstraints.PAGE_START;
		buttonGBC.gridx = 1;
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

		// table = new Send_TableModel();

		// table.setTableHeader(null);

		// table.setEditingColumn(0);
		// table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// JScrollPane scrollPane = new JScrollPane(table);
		// scrollPane.setPreferredSize(new Dimension(100, 100));
		// scrollPane.setWheelScrollingEnabled(true);

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

		// CONTEXT MENU
		MenuPopupUtil.installContextMenu(txtTo);
		MenuPopupUtil.installContextMenu(txtFeePow);
		MenuPopupUtil.installContextMenu(txtAmount);
		MenuPopupUtil.installContextMenu(txtMessage);
		MenuPopupUtil.installContextMenu(txtRecDetails);

		/*
		 * ScheduledExecutorService service =
		 * Executors.newSingleThreadScheduledExecutor();
		 * service.scheduleWithFixedDelay( new Runnable() { public void run() {
		 * 
		 * messageLabel.setText("<html>" +
		 * Lang.getInstance().translate("Message") + ":<br>("+
		 * txtMessage.getText().length()+")</html>");
		 * 
		 * }}, 0, 500, TimeUnit.MILLISECONDS);
		 * 
		 */
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
		refreshReceiverDetails();

	}

	private void refreshReceiverDetails() {
		String toValue = txtTo.getText();
		AssetCls asset = ((AssetCls) cbxFavorites.getSelectedItem());

		if (toValue == null || toValue.isEmpty()) {
			txtRecDetails.setText("");
			return;
		}

		if (txtTo.getText().equals("has no Accounts")) {
			txtRecDetails.setText(person.viewName() + " " + Lang.getInstance().translate("has no Accounts"));
			return;
		}

		// READ RECIPIENT

		Tuple2<Account, String> resultAccount = Account.tryMakeAccount(txtTo.getText());
		Account account = resultAccount.a;
		if (account == null) {
			txtRecDetails.setText(Lang.getInstance().translate(resultAccount.b));
		} else {
			txtRecDetails.setText(account.toString(asset.getKey()));
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
		Tuple2<Account, String> resultRecipient = Account.tryMakeAccount(txtTo.getText());
		if (resultRecipient.b != null) {
			JOptionPane.showMessageDialog(null, Lang.getInstance().translate(resultRecipient.b),
					Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			// ENABLE
			this.sendButton.setEnabled(true);
			return;
		}
		Account recipient = resultRecipient.a;
		
		// confirt sender = recipient
		if (sender.getAddress().equals(recipient.getAddress())){
			JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Sender and Recipient addresses match") + "!",
					Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
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

		if (amount.signum() == 0) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Amount must be greater 0.0"),
					Lang.getInstance().translate("Error") + ":  " + Lang.getInstance().translate("Invalid amount!"),
					JOptionPane.ERROR_MESSAGE);

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
		if (amount != null) {
			// CHECK IF PAYMENT OR ASSET TRANSFER
			asset = (AssetCls) this.cbxFavorites.getSelectedItem();
			key = asset.getKey();
		}

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
		transaction = Controller.getInstance().r_Send(
				Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), feePow, recipient, key,
				amount, head, messageBytes, isTextByte, encrypted);
		// test result = new Pair<Transaction, Integer>(null,
		// Transaction.VALIDATE_OK);

		String Status_text = "<HTML>" + Lang.getInstance().translate("Size") + ":&nbsp;" + transaction.viewSize(false)
				+ " Bytes, ";
		Status_text += "<b>" + Lang.getInstance().translate("Fee") + ":&nbsp;" + transaction.getFee().toString()
				+ " COMPU</b><br></body></HTML>";

		Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(null, true, Lang.getInstance().translate("Send Mail"),
				(int) (this.getWidth() / 1.2), (int) (this.getHeight() / 1.2), Status_text,
				Lang.getInstance().translate("Confirmation Transaction"));
		Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((R_Send) transaction);
		dd.jScrollPane1.setViewportView(ww);
		dd.pack();
		dd.setLocationRelativeTo(this);
		dd.setVisible(true);

		// JOptionPane.OK_OPTION
		if (dd.isConfirm) {

			if (noRecive) {

				// String raw = Base58.encode(transaction.toBytes(false, null));
				My_JFileChooser chooser = new My_JFileChooser();
				chooser.setDialogTitle(Lang.getInstance().translate("Save File"));
				// chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				// FileNameExtensionFilter filter = new
				// FileNameExtensionFilter("*.era","*.*");
				// chooser.setFileFilter(filter);

				// chooser.setAcceptAllFileFilterUsed(false);

				if (chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {

					String pp = chooser.getSelectedFile().getPath();

					File ff = new File(pp);
					// if file
					if (ff.exists() && ff.isFile()) {
						int aaa = JOptionPane.showConfirmDialog(chooser,
								Lang.getInstance().translate("File") + Lang.getInstance().translate("Exists") + "! "
										+ Lang.getInstance().translate("Overwrite") + "?",
								Lang.getInstance().translate("Message"), JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.INFORMATION_MESSAGE);
						System.out.print("\n gggg " + aaa);
						if (aaa != 0) {
							return;
						}
						ff.delete();

					}

					try (FileWriter fw = new FileWriter(ff)) {
						fw.write(transaction.toJson().toJSONString());
					} catch (IOException e) {
						System.out.println("Всё погибло!");
					}

					/*
					 * try(FileOutputStream fos=new FileOutputStream(pp)) { //
					 * перевод строки в байты // String ssst =
					 * model.getValueAt(row, 2).toString(); byte[] buffer
					 * =transaction.toBytes(false, null); // if ZIP
					 * 
					 * fos.wri.write(buffer, 0, buffer.length);
					 * 
					 * } catch(IOException ex){
					 * 
					 * System.out.println(ex.getMessage()); }
					 */
				}

				// JOptionPane.showMessageDialog(new JFrame(),
				// Lang.getInstance().translate("File save"),
				// Lang.getInstance().translate("Success"),
				// JOptionPane.INFORMATION_MESSAGE);

			} else {

				result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, false);

				// CHECK VALIDATE MESSAGE
				if (result == Transaction.VALIDATE_OK) {
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
		}
		// ENABLE
		this.sendButton.setEnabled(true);
	}

}
