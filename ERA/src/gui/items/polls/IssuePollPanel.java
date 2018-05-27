package gui.items.polls;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;

import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.transaction.IssuePollRecord;
import core.transaction.Transaction;
import gui.MainFrame;
import gui.PasswordPane;
import gui.library.Issue_Confirm_Dialog;
import gui.library.library;
import gui.models.AccountsComboBoxModel;
import gui.models.CreateOptionsTableModel;
import gui.transaction.OnDealClick;
import lang.Lang;
import utils.Pair;

@SuppressWarnings("serial")
public class IssuePollPanel extends JPanel {
	private JComboBox<Account> cbxFrom;
	private JTextField txtFee;
	private JTextField txtName;
	private JTextArea txtareaDescription;
	private JButton createButton;
	private CreateOptionsTableModel optionsTableModel;
	private IssuePollPanel th;
	protected int selRow;

	public IssuePollPanel() {
		// super(Lang.getInstance().translate("Erachain.org") + " - " +
		// Lang.getInstance().translate("Create Poll"));

		// CLOSE
		// setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// ICON

		// LAYOUT
		this.setLayout(new GridBagLayout());
		th = this;
		// PADDING
		// ((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5,
		// 5, 5));

		// LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(5, 5, 5, 5);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;
		labelGBC.gridx = 0;

		// COMBOBOX GBC
		GridBagConstraints cbxGBC = new GridBagConstraints();
		cbxGBC.insets = new Insets(5, 5, 5, 5);
		cbxGBC.fill = GridBagConstraints.NONE;
		cbxGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxGBC.weightx = 0;
		cbxGBC.gridx = 1;

		// TEXTFIELD GBC
		GridBagConstraints txtGBC = new GridBagConstraints();
		txtGBC.insets = new Insets(5, 5, 5, 5);
		txtGBC.fill = GridBagConstraints.HORIZONTAL;
		txtGBC.anchor = GridBagConstraints.NORTHWEST;
		txtGBC.weightx = 1;
		txtGBC.gridwidth = 2;
		txtGBC.gridx = 1;

		// BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5, 5, 5, 5);
		buttonGBC.fill = GridBagConstraints.NONE;
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridwidth = 2;
		buttonGBC.gridx = 0;

		// LABEL FROM
		labelGBC.gridy = 0;
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
		this.add(fromLabel, labelGBC);

		// COMBOBOX FROM
		txtGBC.gridy = 0;
		this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
		this.add(this.cbxFrom, txtGBC);

		// LABEL NAME
		labelGBC.gridy = 1;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
		this.add(nameLabel, labelGBC);

		// TXT NAME
		txtGBC.gridy = 1;
		this.txtName = new JTextField();
		this.add(this.txtName, txtGBC);

		// LABEL NAME
		labelGBC.gridy = 2;
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		this.add(descriptionLabel, labelGBC);

		// TXTAREA NAME
		// TABLE OPTIONS
		GridBagConstraints txtGBC2 = new GridBagConstraints();
		txtGBC2.gridy = 2;
		txtGBC2.weighty = 0.3;
		txtGBC2.gridwidth = 2;
		txtGBC2.insets = new Insets(0, 5, 5, 0);
		txtGBC2.fill = GridBagConstraints.BOTH;
		txtGBC2.anchor = GridBagConstraints.NORTHWEST;
		this.txtareaDescription = new JTextArea();
		// this.txtareaDescription.setRows(4);
		this.txtareaDescription.setBorder(this.txtName.getBorder());
		JScrollPane ss = new JScrollPane();
		ss.setViewportView(this.txtareaDescription);
		this.add(ss, txtGBC2);

		// LABEL OPTIONS
		labelGBC.gridy = 3;
		labelGBC.weighty = 0.5;
		JLabel optionsLabel = new JLabel(Lang.getInstance().translate("Options") + ":");
		this.add(optionsLabel, labelGBC);

		// TABLE OPTIONS
		GridBagConstraints txtGBC1 = new GridBagConstraints();
		txtGBC1.gridy = 3;
		txtGBC1.weighty = 0.1;
		txtGBC1.gridwidth = 2;
		txtGBC1.fill = GridBagConstraints.BOTH;
		txtGBC1.anchor = GridBagConstraints.NORTHWEST;
		this.optionsTableModel = new CreateOptionsTableModel(new Object[] { Lang.getInstance().translate("Name") }, 0);
		final JTable table = new JTable(optionsTableModel);
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(table);
		this.add(scroll, txtGBC1);

		// TABLE OPTIONS DELETE
		txtGBC.gridy = 4;
		txtGBC.weighty = 0;
		JButton deleteButton = new JButton(Lang.getInstance().translate("Delete"));
		deleteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (optionsTableModel.getRowCount() > 1) {
					int selRow = table.getSelectedRow();
					if (selRow != -1) {
						((DefaultTableModel) optionsTableModel).removeRow(selRow);
					}
				}
			}
		});

		this.add(deleteButton, txtGBC);

		// LABEL FEE
		labelGBC.gridy = 5;
		labelGBC.weighty = 0;
		JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee") + ":");
		this.add(feeLabel, labelGBC);

		// TXT FEE
		txtGBC.gridy = 5;
		this.txtFee = new JTextField();
		this.txtFee.setText("1");
		this.add(this.txtFee, txtGBC);

		// BUTTON Register
		buttonGBC.gridy = 6;
		createButton = new JButton(Lang.getInstance().translate("Create"));
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRegisterClick();
			}
		});
		this.add(createButton, buttonGBC);
		
		JPopupMenu menu = new JPopupMenu();

		menu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuCanceled(PopupMenuEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				// TODO Auto-generated method stub
				if (optionsTableModel.getRowCount() > 1) {
					selRow = table.getSelectedRow();
					
				}

			}
		});

		JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Delete"));
		copyAddress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (selRow > 0) {
					((DefaultTableModel) optionsTableModel).removeRow(selRow);
				}
			}
		});
		menu.add(copyAddress);
		
		table.setComponentPopupMenu(menu);
		
		

		// PACK
		// this.pack();
		// this.setResizable(false);
		// this.setLocationRelativeTo(null);
		this.setMinimumSize(new Dimension(0, 0));
		this.setVisible(true);
	}

	public void onRegisterClick() {
		// DISABLE
		this.createButton.setEnabled(false);

		// CHECK IF NETWORK OK
		if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
			// NETWORK NOT OK
			JOptionPane.showMessageDialog(null,
					Lang.getInstance().translate(
							"You are unable to send a transaction while synchronizing or while having no connections!"),
					Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

			// ENABLE
			this.createButton.setEnabled(true);

			return;
		}

		// CHECK IF WALLET UNLOCKED
		if (!Controller.getInstance().isWalletUnlocked()) {
			// ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(this);
			if (!Controller.getInstance().unlockWallet(password)) {
				// WRONG PASSWORD
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
						Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

				// ENABLE
				this.createButton.setEnabled(true);
				return;
			}
		}

		// READ CREATOR
		Account sender = (Account) cbxFrom.getSelectedItem();
		int feePow = 0;

		try {
			// READ FEE POWER
			feePow = Integer.parseInt(txtFee.getText());

		} catch (Exception e) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee Power!"),
					Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			// ENABLE
			this.createButton.setEnabled(true);
			return;

		}
		
		if (optionsTableModel.getRowCount() <1){
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Null Options!"),
					Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			// ENABLE
			this.createButton.setEnabled(true);
			return;
			
		}
		if (optionsTableModel.getRowCount() ==1 && optionsTableModel.getValueAt(0, 0).equals("") ){
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Null Options!"),
					Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			// ENABLE
			this.createButton.setEnabled(true);
			return;
			
		}
		

		// TODO: icon + IMAGE

		// CREATE POLL
		PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
		IssuePollRecord issuePoll = (IssuePollRecord) Controller.getInstance().issuePoll(creator,
				this.txtName.getText(), null, null, this.txtareaDescription.getText(),
				this.optionsTableModel.getOptions(), feePow);
		// Poll poll = issue_voiting.getPoll();

		// Issue_Asset_Confirm_Dialog cont = new
		// Issue_Asset_Confirm_Dialog(issueAssetTransaction);
		String text = "<HTML><body>";
		text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
				+ Lang.getInstance().translate("Issue Voting") + "<br><br><br>";
		text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issuePoll.getCreator() + "<br>";
		text += Lang.getInstance().translate("Name") + ":&nbsp;" + this.txtName.getText() + "<br>";
		text += "<br>" + Lang.getInstance().translate("Description") + ":<br>"
				+ library.to_HTML(this.txtareaDescription.getText()) + "<br>";
		text += "<br>" + Lang.getInstance().translate("Options") + ":<br>";

		List<String> op = this.optionsTableModel.getOptions();

		int i;
		for (i = 0; i < op.size(); i++) {
			text += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + op.get(i);

		}
		text += "<br>    ";

		String Status_text = "<HTML>" + Lang.getInstance().translate("Size") + ":&nbsp;" + issuePoll.viewSize(false)
				+ " Bytes, ";
		Status_text += "<b>" + Lang.getInstance().translate("Fee") + ":&nbsp;" + issuePoll.getFee().toString()
				+ " COMPU</b><br></body></HTML>";

		Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(MainFrame.getInstance(), true, text,
				(int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2), Status_text,
				Lang.getInstance().translate("Confirmation Transaction"));
		dd.setLocationRelativeTo(th);
		dd.setVisible(true);

		// JOptionPane.OK_OPTION
		if (!dd.isConfirm) { // s!= JOptionPane.OK_OPTION) {

			this.createButton.setEnabled(true);

			return;
		}

		// VALIDATE AND PROCESS
		int result = Controller.getInstance().getTransactionCreator().afterCreate(issuePoll, false);

		if (result == Transaction.VALIDATE_OK) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Poll issue has been sent!"),
					Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);

			// this.dispose();

		} else {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)),
					Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
		}

		// ENABLE
		this.createButton.setEnabled(true);
	}
}
