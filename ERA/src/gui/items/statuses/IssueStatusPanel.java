package gui.items.statuses;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.item.statuses.StatusCls;
import core.transaction.IssueStatusRecord;
import core.transaction.Transaction;
import gui.MainFrame;
import gui.PasswordPane;
import gui.library.Issue_Confirm_Dialog;
import gui.library.My_Add_Image_Panel;
import gui.library.library;
import gui.models.AccountsComboBoxModel;
import gui.transaction.OnDealClick;
import lang.Lang;

@SuppressWarnings("serial")
public class IssueStatusPanel extends JPanel {
	private JComboBox<Account> cbxFrom;
	private JTextField txtFeePow;
	private JTextField txtName;
	private JTextArea txtareaDescription;
	private JButton issueButton;
	private JCheckBox jCheck_Unique;
	private IssueStatusPanel th;
	private My_Add_Image_Panel add_Image_Panel;
	private My_Add_Image_Panel add_Logo_Icon_Panel;

	// @SuppressWarnings({ "unchecked", "rawtypes" })
	public IssueStatusPanel() {

		String colorText = "ff0000"; // цвет текста в форме
		th = this;
		// CLOSE
		// setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// LAYOUT
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 33, 0, 0, 0, 0, 0, 0, 0, 65, 0, 0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0 };
		gridBagLayout.rowHeights = new int[] { -67, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		this.setLayout(gridBagLayout);
		// COMBOBOX GBC
		GridBagConstraints cbxGBC = new GridBagConstraints();
		// cbxGBC.insets = new Insets(5,5,5,5);
		cbxGBC.fill = GridBagConstraints.NONE;
		cbxGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxGBC.weightx = 0;
		cbxGBC.gridx = 1;
		cbxGBC.insets = new java.awt.Insets(5, 3, 5, 15);

		JLabel jLabel1 = new JLabel();
		jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
		jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		jLabel1.setText(Lang.getInstance().translate("Create Status"));
		jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 9;
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		gridBagConstraints.weightx = 1.3;
		gridBagConstraints.insets = new java.awt.Insets(8, 15, 8, 15);
		add(jLabel1, gridBagConstraints);
		// LABEL FROM
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
		GridBagConstraints gbc_fromLabel = new GridBagConstraints();
		gbc_fromLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_fromLabel.insets = new Insets(0, 0, 5, 5);
		gbc_fromLabel.gridx = 1;
		gbc_fromLabel.gridy = 1;
		this.add(fromLabel, gbc_fromLabel);

		// COMBOBOX FROM
		this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
		GridBagConstraints gbc_cbxFrom = new GridBagConstraints();
		gbc_cbxFrom.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbxFrom.gridwidth = 8;
		gbc_cbxFrom.insets = new Insets(0, 0, 5, 5);
		gbc_cbxFrom.gridx = 2;
		gbc_cbxFrom.gridy = 1;
		this.add(this.cbxFrom, gbc_cbxFrom);

		// size from width
		add_Image_Panel = new My_Add_Image_Panel(
				Lang.getInstance().translate("Add Image") + (" (max %1%kB)").replace("%1%", "1024"), 250, 250);

		GridBagConstraints gbc_add_Image_Panel = new GridBagConstraints();
		gbc_add_Image_Panel.anchor = GridBagConstraints.NORTH;
		gbc_add_Image_Panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_add_Image_Panel.gridheight = 8;
		gbc_add_Image_Panel.insets = new Insets(0, 0, 5, 5);
		gbc_add_Image_Panel.gridx = 0;
		gbc_add_Image_Panel.gridy = 1;
		add(add_Image_Panel, gbc_add_Image_Panel);

		// LABEL NAME
		JLabel nameLabel = new JLabel(
				"<HTML><p style=':#" + colorText + "'>" + Lang.getInstance().translate("Name") + ": </p></html>");
		GridBagConstraints gbc_nameLabel = new GridBagConstraints();
		gbc_nameLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_nameLabel.gridx = 1;
		gbc_nameLabel.gridy = 2;
		this.add(nameLabel, gbc_nameLabel);

		// TXT NAME
		this.txtName = new JTextField();
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridwidth = 5;
		gbc_txtName.insets = new Insets(0, 0, 5, 5);
		gbc_txtName.gridx = 2;
		gbc_txtName.gridy = 2;
		this.add(this.txtName, gbc_txtName);
		this.txtareaDescription.setBorder(this.txtName.getBorder());

		// size from height
		add_Logo_Icon_Panel = new My_Add_Image_Panel(Lang.getInstance().translate("Add Logo"), 50, 50);
		GridBagConstraints gbc_add_Logo_Icon_Panel = new GridBagConstraints();
		gbc_add_Logo_Icon_Panel.gridwidth = 2;
		gbc_add_Logo_Icon_Panel.gridheight = 2;
		gbc_add_Logo_Icon_Panel.insets = new Insets(0, 0, 5, 5);
		gbc_add_Logo_Icon_Panel.gridx = 8;
		gbc_add_Logo_Icon_Panel.gridy = 2;
		add(add_Logo_Icon_Panel, gbc_add_Logo_Icon_Panel);

		JLabel label_bottom = new JLabel();

		GridBagConstraints gbc_label_bottom = new GridBagConstraints();
		gbc_label_bottom.insets = new Insets(0, 0, 5, 0);
		gbc_label_bottom.gridx = 10;
		gbc_label_bottom.gridy = 2;
		this.add(label_bottom, gbc_label_bottom);

		this.setMinimumSize(new Dimension(0, 0));

		// LABEL DESCRIPTION
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		GridBagConstraints gbc_descriptionLabel = new GridBagConstraints();
		gbc_descriptionLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_descriptionLabel.insets = new Insets(0, 0, 5, 5);
		gbc_descriptionLabel.gridx = 1;
		gbc_descriptionLabel.gridy = 4;
		this.add(descriptionLabel, gbc_descriptionLabel);

		JScrollPane scrollDescription = new JScrollPane();
		scrollDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollDescription.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		GridBagConstraints gbc_scrollDescription = new GridBagConstraints();
		gbc_scrollDescription.fill = GridBagConstraints.HORIZONTAL;
		gbc_scrollDescription.gridwidth = 8;
		gbc_scrollDescription.insets = new Insets(0, 0, 5, 5);
		gbc_scrollDescription.gridx = 2;
		gbc_scrollDescription.gridy = 4;
		this.add(scrollDescription, gbc_scrollDescription);

		// TXTAREA DESCRIPTION
		this.txtareaDescription = new JTextArea();
		scrollDescription.setViewportView(txtareaDescription);

		this.txtareaDescription.setRows(6);
		this.txtareaDescription.setColumns(20);

		// LABEL FEE POW
		JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee Power") + " (0..6)" + ":");
		GridBagConstraints gbc_feeLabel = new GridBagConstraints();
		gbc_feeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_feeLabel.gridx = 1;
		gbc_feeLabel.gridy = 5;
		this.add(feeLabel, gbc_feeLabel);

		// TXT FEE POW
		this.txtFeePow = new JTextField();
		this.txtFeePow.setText("0");
		GridBagConstraints gbc_txtFeePow = new GridBagConstraints();
		gbc_txtFeePow.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFeePow.gridwidth = 8;
		gbc_txtFeePow.insets = new Insets(0, 0, 5, 5);
		gbc_txtFeePow.gridx = 2;
		gbc_txtFeePow.gridy = 5;
		this.add(this.txtFeePow, gbc_txtFeePow);

		// JCheckBox jCheck_Unique;
		JLabel unoqueLabel = new JLabel(Lang.getInstance().translate("Single") + ":");
		GridBagConstraints gbc_unoqueLabel = new GridBagConstraints();
		gbc_unoqueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_unoqueLabel.gridx = 1;
		gbc_unoqueLabel.gridy = 6;
		this.add(unoqueLabel, gbc_unoqueLabel);

		jCheck_Unique = new JCheckBox();
		GridBagConstraints gbc_jCheck_Unique = new GridBagConstraints();
		gbc_jCheck_Unique.insets = new Insets(0, 0, 5, 5);
		gbc_jCheck_Unique.gridx = 2;
		gbc_jCheck_Unique.gridy = 6;
		this.add(this.jCheck_Unique, gbc_jCheck_Unique);

		// BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.anchor = GridBagConstraints.NORTHEAST;
		buttonGBC.insets = new Insets(8, 5, 5, 15);
		buttonGBC.gridx = 0;

		// BUTTON Register
		buttonGBC.gridy = 7;
		buttonGBC.gridx = 1;

		this.issueButton = new JButton("Issue");
		this.issueButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onIssueClick();
			}
		});
		GridBagConstraints gbc_issueButton = new GridBagConstraints();
		gbc_issueButton.gridwidth = 3;
		gbc_issueButton.insets = new Insets(0, 0, 5, 5);
		gbc_issueButton.gridx = 7;
		gbc_issueButton.gridy = 7;
		this.add(this.issueButton, gbc_issueButton);
		this.setVisible(true);
	}

	public void onIssueClick() {
		// DISABLE
		this.issueButton.setEnabled(false);

		// CHECK IF WALLET UNLOCKED
		if (!Controller.getInstance().isWalletUnlocked()) {
			// ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(this);
			if (!Controller.getInstance().unlockWallet(password)) {
				// WRONG PASSWORD
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
						Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

				// ENABLE
				this.issueButton.setEnabled(true);

				return;
			}
		}

		// READ CREATOR
		Account sender = (Account) this.cbxFrom.getSelectedItem();

		int parse = 0;
		int feePow = 0;
		try {

			// READ FEE POW
			feePow = Integer.parseInt(this.txtFeePow.getText());

		} catch (Exception e) {
			String mess = "Invalid pars... " + parse;
			switch (parse) {
			case 0:
				mess = "Invalid fee power 0..6";
				break;
			}
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(e + mess),
					Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

			this.issueButton.setEnabled(true);
			return;
		}

		byte[] icon = add_Logo_Icon_Panel.imgButes;
		byte[] image = add_Image_Panel.imgButes;

		boolean unique = jCheck_Unique.isSelected();
		PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
		IssueStatusRecord issue_Status = (IssueStatusRecord) Controller.getInstance().issueStatus(creator,
				this.txtName.getText(), this.txtareaDescription.getText(), unique, icon, image, feePow);

		String text = "<HTML><body>";
		text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
				+ Lang.getInstance().translate("Create Status") + "<br><br><br>";
		text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issue_Status.getCreator() + "<br>";
		text += Lang.getInstance().translate("Name") + ":&nbsp;" + issue_Status.getItem().viewName() + "<br>";
		text += Lang.getInstance().translate("Description") + ":<br>"
				+ library.to_HTML(issue_Status.getItem().getDescription()) + "<br>";

		text += Lang.getInstance().translate("Unique") + ": " + ((StatusCls) issue_Status.getItem()).isUnique()
				+ "<br>";
		String Status_text = "<HTML>" + Lang.getInstance().translate("Size") + ":&nbsp;" + issue_Status.viewSize(false)
				+ " Bytes, ";
		Status_text += "<b>" + Lang.getInstance().translate("Fee") + ":&nbsp;" + issue_Status.getFee().toString()
				+ " COMPU</b><br></body></HTML>";

		System.out.print("\n" + text + "\n");

		Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(MainFrame.getInstance(), true, text,
				(int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2), Status_text,
				Lang.getInstance().translate("Confirmation Transaction"));
		dd.setLocationRelativeTo(th);
		dd.setVisible(true);

		// JOptionPane.OK_OPTION
		if (!dd.isConfirm) {

			this.issueButton.setEnabled(true);

			return;
		}

		// VALIDATE AND PROCESS
		int result = Controller.getInstance().getTransactionCreator().afterCreate(issue_Status, false);

		// CHECK VALIDATE MESSAGE
		if (result == Transaction.VALIDATE_OK) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Status issue has been sent!"),
					Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
			// this.dispose();
			clearPanel();

		} else {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)),
					Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
		}

		// ENABLE
		this.issueButton.setEnabled(true);
	}

	void clearPanel() {

		this.txtName.setText("");
		this.txtareaDescription.setText("");
		this.txtFeePow.setText("0");

	}

}
