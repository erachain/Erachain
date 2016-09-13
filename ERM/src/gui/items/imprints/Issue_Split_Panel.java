package gui.items.imprints;

import java.awt.Button;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.imprints.Imprint;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.primitives.Longs;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.transaction.R_Hashes;
import core.transaction.Transaction;
import database.DBSet;
import gui.PasswordPane;
import gui.Split_Panel;
import lang.Lang;
import utils.Pair;

public class Issue_Split_Panel extends Split_Panel {
	Table_Model_Issue_Hashes table_Model;
	private JTable Table_Hash;
	private JButton jButton3_jToolBar_RightPanel;
	Issue_Hash_Imprint issue_Hash_Imprint;

	public Issue_Split_Panel() {

		// left panel

		this.toolBar_LeftPanel.setVisible(false);
		this.searchToolBar_LeftPanel.setVisible(false);
		this.jTable_jScrollPanel_LeftPanel.setVisible(false);
		issue_Hash_Imprint = new Issue_Hash_Imprint();
		this.jScrollPanel_LeftPanel.setViewportView(issue_Hash_Imprint);

		issue_Hash_Imprint.jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onIssueClick();
			}
		});

		// Right panel
		// this.jToolBar_RightPanel.setSize(WIDTH, 200);
		jButton2_jToolBar_RightPanel.setText(Lang.getInstance().translate("Import Hashs"));
		// jButton2_jToolBar_RightPanel.setSize(70, 30);
		// jButton2_jToolBar_RightPanel.setPreferredSize(new
		// Dimension(100,200));
		jButton2_jToolBar_RightPanel.addActionListener(new ActionListener() {
			// create Hashs
			@Override
			public void actionPerformed(ActionEvent e) {
				Hashs_from_Files(true);

			}
		});

		jButton1_jToolBar_RightPanel.setText(Lang.getInstance().translate("Delete Hash"));
		jButton1_jToolBar_RightPanel.addActionListener(new ActionListener() {
			// delete row
			@Override
			public void actionPerformed(ActionEvent e) {

				if (table_Model.getRowCount() > 1) {
					int selRow = Table_Hash.getSelectedRow();
					if (selRow != -1) {
						((DefaultTableModel) table_Model).removeRow(selRow);
						table_Model.fireTableDataChanged();
					}
				}
			}
		});

		jButton3_jToolBar_RightPanel = new JButton();
		jButton3_jToolBar_RightPanel.setText(Lang.getInstance().translate("Create Hash"));
		// jButton3_jToolBar_RightPanel.setFocusable(false);
		jButton3_jToolBar_RightPanel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		jButton3_jToolBar_RightPanel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		jButton3_jToolBar_RightPanel.addActionListener(new ActionListener() {
			// create Hashs
			@Override
			public void actionPerformed(ActionEvent e) {
				Hashs_from_Files(false);

			}
		});

		jToolBar_RightPanel.add(jButton3_jToolBar_RightPanel);

		jButton1_jToolBar_RightPanel.setFont(new Font("Tahoma", 0, 14));
		jButton2_jToolBar_RightPanel.setFont(new Font("Tahoma", 0, 14));
		jButton3_jToolBar_RightPanel.setFont(new Font("Tahoma", 0, 14));

		table_Model = new Table_Model_Issue_Hashes(0);
		Table_Hash = new JTable(table_Model);
		this.jScrollPane_jPanel_RightPanel.setViewportView(Table_Hash);

	}

	public void onIssueClick() {
		// DISABLE
		issue_Hash_Imprint.jButton.setEnabled(false);

		// CHECK IF NETWORK OK
		if (Controller.getInstance().getStatus() != Controller.STATUS_OK) {
			// NETWORK NOT OK
			JOptionPane.showMessageDialog(null,
					Lang.getInstance().translate(
							"You are unable to send a transaction while synchronizing or while having no connections!"),
					Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

			// ENABLE
			issue_Hash_Imprint.jButton.setEnabled(true);

			return;
		}

		// CHECK IF WALLET UNLOCKED
		if (!Controller.getInstance().isWalletUnlocked()) {
			// ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog();
			if (!Controller.getInstance().unlockWallet(password)) {
				// WRONG PASSWORD
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
						Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

				// ENABLE
				issue_Hash_Imprint.jButton.setEnabled(true);

				return;
			}
		}

		// READ CREATOR
		Account sender = (Account) issue_Hash_Imprint.jComboBox_Account.getSelectedItem();

		long parse = 0;
		int feePow = 0;
		String url = "";
		String description = "";
		try {

			// READ FEE POW
			feePow = Integer.parseInt(issue_Hash_Imprint.txtFeePow.getText());
			// READ AMOUNT
			// float amount = Float.parseFloat(this.txtAmount.getText());

			// NAME TOTAL
			url = issue_Hash_Imprint.jTextField_URL.getText().trim();

			description = issue_Hash_Imprint.jTextArea_Description.getText();

		} catch (Exception e) {
			if (parse == 0) {
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"),
						Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid quantity!"),
						Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
		}

		List<String> hashes = this.table_Model.getValues(0);

		List<String> twins = R_Hashes.findTwins(DBSet.getInstance(), hashes);
		if (twins.size() > 0) {
			JOptionPane.showMessageDialog(new JFrame(),
					Lang.getInstance().translate("Twin hashes: ") + twins.toString(),
					Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			issue_Hash_Imprint.jButton.setEnabled(true);
			return;
		}

		// CREATE IMPRINT
		PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
		Pair<Transaction, Integer> result = Controller.getInstance().r_Hashes(creator, feePow, url, description,
				String.join(" ", hashes));

		// CHECK VALIDATE MESSAGE
		if (result.getB() == Transaction.VALIDATE_OK) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Imprint issue has been sent!"),
					Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
			// this.dispose();
		} else {
			JOptionPane.showMessageDialog(new JFrame(),
					Lang.getInstance().translate("Unknown error") + "[" + result.getB() + "]!",
					Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
		}

		// ENABLE
		issue_Hash_Imprint.jButton.setEnabled(true);
	}

	protected void Hashs_from_Files(boolean importing) {
		// TODO Auto-generated method stub
		// true - если импорт из файла
		// false - если создаем хэш для файлов

		// открыть диалог для файла
		JFileChooser chooser = new JFileChooser();

		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);
		if (importing)
			chooser.setMultiSelectionEnabled(false);

		// FileNameExtensionFilter filter = new FileNameExtensionFilter(
		// "Image", "png", "jpg");
		// chooser.setFileFilter(filter);

		int returnVal = chooser.showOpenDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if (importing) {
				// IMPORT FROM FILE
				File patch = chooser.getSelectedFile();
				String file_name = patch.getPath();
				String hashesStr = "";
				try {
					hashesStr = new String(Files.readAllBytes(Paths.get(file_name)));
				} catch (IOException e) {
					e.printStackTrace();
					table_Model.addRow(new Object[] { "",
							Lang.getInstance().translate("error reading") + " - " + file_name });
				}
				
				if (hashesStr.length() > 0) {
				String[] hashes = hashesStr.split("\\s*(\\s|,|!|;|:|\n|\\.)\\s*");
					for (String hashB58: hashes) {
						if (hashB58!= null && !hashB58.equals(new String("")))	table_Model.addRow(new Object[] { hashB58, Lang.getInstance().translate("imported from") + " " +  file_name});					
					}
				}

			} else {

				// make HASHES from files
				File[] patchs = chooser.getSelectedFiles();

				for (File patch : patchs) {

					String file_name = patch.getPath();
					File file = new File(patch.getPath());

					// преобразуем в байты
					long file_len = file.length();
					if (file_len > Integer.MAX_VALUE) {
						table_Model.addRow(new Object[] { "",
								Lang.getInstance().translate("length very long") + " - " + file_name });
						continue;
					}
					byte[] fileInArray = new byte[(int) file.length()];
					FileInputStream f = null;
					try {
						f = new FileInputStream(patch.getPath());
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						table_Model.addRow(new Object[] { "",
								Lang.getInstance().translate("error streaming") + " - " + file_name });
						continue;
					}
					try {
						f.read(fileInArray);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						table_Model.addRow(new Object[] { "",
								Lang.getInstance().translate("error reading") + " - " + file_name });
						continue;
					}
					try {
						f.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}

					/// HASHING
					String hashes = Base58.encode(Crypto.getInstance().digest(fileInArray));
					table_Model.addRow(new Object[] { hashes,
							Lang.getInstance().translate("from file ") + file_name });

				}

			}
			table_Model.fireTableDataChanged();

		}

	}

}
