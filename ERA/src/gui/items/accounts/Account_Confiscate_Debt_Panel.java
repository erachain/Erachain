package gui.items.accounts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.item.assets.AssetCls;
import core.transaction.R_Send;
import core.transaction.Transaction;
import gui.AccountRenderer;
import gui.MainFrame;
import gui.PasswordPane;
import gui.items.assets.AssetsComboBoxModel;
import gui.library.Issue_Confirm_Dialog;
import gui.models.AccountsComboBoxModel;
import gui.transaction.OnDealClick;
import gui.transaction.Send_RecordDetailsFrame;
import lang.Lang;
//import settings.Settings;
import utils.Converter;

@SuppressWarnings("serial")

public class Account_Confiscate_Debt_Panel extends Class_Account_Transaction_Panel {
	// private final MessagesTableModel messagesTableModel;

	private Transaction transaction;

	public Account_Confiscate_Debt_Panel(AssetCls asset, Account account) {
		String a;
		if (asset == null)
			a = "";
		else
			a = asset.viewName();

		sendButton.setText(Lang.getInstance().translate("Confiscate Debt"));
		jTextArea_Title.setText(Lang.getInstance()
				.translate("If You want to confiscate in debt issued asset %asset%, fill in this form")
				.replace("%asset%", a));

		// icon.setIcon(null);

		toLabel.setText(Lang.getInstance().translate("Debtor Account") + ":");
		recDetailsLabel.setText(Lang.getInstance().translate("Debtor Details") + ":");

		// favorite combo box
		cbxFavorites.setModel(new AssetsComboBoxModel());
		if (asset != null) {
			for (int i = 0; i < cbxFavorites.getItemCount(); i++) {
				AssetCls item = cbxFavorites.getItemAt(i);
				if (item.getKey() == asset.getKey()) {
					// not worked cbxFavorites.setSelectedItem(asset);
					cbxFavorites.setSelectedIndex(i);
					cbxFavorites.setEnabled(false);// .setEditable(false);
					break;
				} else {
					cbxFavorites.setEnabled(true);
				}
			}
		}

		// account ComboBox
		this.accountsModel = new AccountsComboBoxModel();
		this.cbxFrom.setModel(accountsModel);
		this.cbxFrom.setRenderer(new AccountRenderer(0));
		((AccountRenderer) cbxFrom.getRenderer()).setAsset(((AssetCls) cbxFavorites.getSelectedItem()).getKey());

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

				}

			}
		});

		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onSendClick();
			}
		});

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

		int parsing = 0;
		int feePow = 0;
		BigDecimal amount = null;
		try {
			// READ AMOUNT
			parsing = 1;
			amount = new BigDecimal(txtAmount.getText()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);

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

		if (amount.equals(new BigDecimal("0.0").setScale(BlockChain.AMOUNT_DEDAULT_SCALE))) {
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

		// String head = this.txt_Title.getText();
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
		transaction = Controller.getInstance().r_Send((byte) 2, core.transaction.TransactionAmount.BACKWARD_MASK,
				(byte) 0, Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), feePow,
				recipient, -key, amount, head, messageBytes, isTextByte, encrypted);

		String Status_text = "<HTML>" + Lang.getInstance().translate("Size") + ":&nbsp;" + transaction.viewSize(false)
				+ " Bytes, ";
		Status_text += "<b>" + Lang.getInstance().translate("Fee") + ":&nbsp;" + transaction.getFee().toString()
				+ " COMPU</b><br></body></HTML>";

		Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(MainFrame.getInstance(), true,
				Lang.getInstance().translate("Send Mail"), (int) (this.getWidth() / 1.2),
				(int) (this.getHeight() / 1.2), Status_text, Lang.getInstance().translate("Confirmation Transaction"));
		Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((R_Send) transaction);
		dd.jScrollPane1.setViewportView(ww);
		dd.setLocationRelativeTo(this);
		dd.setVisible(true);

		// JOptionPane.OK_OPTION
		if (dd.isConfirm) {

			result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, false);

			// test result = new Pair<Transaction, Integer>(null,
			// Transaction.VALIDATE_OK);

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
		// ENABLE
		this.sendButton.setEnabled(true);
	}

}
