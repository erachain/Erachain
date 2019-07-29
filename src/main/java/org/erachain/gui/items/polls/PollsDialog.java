package org.erachain.gui.items.polls;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.VoteOnItemPollTransaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.accounts.AccountRenderer;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.items.ComboBoxModelItemsAll;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.models.OptionsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.gui.transaction.VoteOnItemPollDetailsFrame;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

//import java.math.BigDecimal;

@SuppressWarnings("serial")
public class PollsDialog extends JDialog {
    private PollCls poll;
    private JComboBox<Account> cbxAccount;
    private JComboBox<String> cbxOptions;
    private JButton voteButton;
    private JTextField txtFeePow;
    private JComboBox<ItemCls> cbxAssets;

    public PollsDialog(PollCls poll, int option, AssetCls asset) {
        // super(Lang.getInstance().translate("Erachain.org") + " - " +
        // Lang.getInstance().translate("Vote"));

        if (poll == null)
            return;
        this.poll = poll;

        // ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        // CLOSE
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // LAYOUT
        this.setLayout(new GridBagLayout());

        // PADDING
        ((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));

        // LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(0, 5, 5, 0);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.weightx = 0;
        labelGBC.gridx = 0;

        // DETAIL GBC
        GridBagConstraints detailGBC = new GridBagConstraints();
        detailGBC.insets = new Insets(0, 5, 5, 0);
        detailGBC.fill = GridBagConstraints.HORIZONTAL;
        detailGBC.anchor = GridBagConstraints.NORTHWEST;
        detailGBC.weightx = 1;
        detailGBC.gridwidth = 2;
        detailGBC.gridx = 1;

        // LABEL NAME
        labelGBC.gridy = 1;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Poll") + ":");
        this.add(nameLabel, labelGBC);

        // NAME
        detailGBC.gridy = 1;
        JTextField name = new JTextField(poll.getName());
        name.setEditable(false);
        this.add(name, detailGBC);

        // LABEL DESCRIPTION
        labelGBC.gridy = 2;
        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        // DESCRIPTION
        GridBagConstraints descrGBC = new GridBagConstraints();
        descrGBC.gridy = 2;
        descrGBC.weighty = 0.1;
        descrGBC.gridwidth = 2;
        descrGBC.insets = new Insets(0, 5, 5, 0);
        descrGBC.fill = GridBagConstraints.BOTH;
        JTextArea txtAreaDescription = new JTextArea("<html>" + Library.to_HTML(poll.getDescription()));
        txtAreaDescription.setRows(4);
        txtAreaDescription.setBorder(name.getBorder());
        txtAreaDescription.setEditable(false);
        JScrollPane ss = new JScrollPane();
        ss.setViewportView(txtAreaDescription);

        this.add(ss, descrGBC);

        // ASSET LABEL GBC
        GridBagConstraints assetLabelGBC = new GridBagConstraints();
        assetLabelGBC.insets = new Insets(0, 5, 5, 0);
        assetLabelGBC.fill = GridBagConstraints.HORIZONTAL;
        assetLabelGBC.anchor = GridBagConstraints.CENTER;
        assetLabelGBC.weightx = 0;
        assetLabelGBC.gridwidth = 1;
        assetLabelGBC.gridx = 0;
        assetLabelGBC.gridy = 3;

        // ASSETS GBC
        GridBagConstraints assetsGBC = new GridBagConstraints();
        assetsGBC.insets = new Insets(0, 5, 5, 0);
        assetsGBC.fill = GridBagConstraints.HORIZONTAL;
        assetsGBC.anchor = GridBagConstraints.NORTHWEST;
        assetsGBC.weightx = 0;
        assetsGBC.gridwidth = 2;
        assetsGBC.gridx = 1;
        assetsGBC.gridy = 3;

        this.add(new JLabel(Lang.getInstance().translate("Check") + ":"), assetLabelGBC);

        cbxAssets = new JComboBox<ItemCls>(new ComboBoxModelItemsAll(ItemCls.ASSET_TYPE));
        cbxAssets.setSelectedItem(asset);
        this.add(cbxAssets, assetsGBC);

        cbxAssets.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                AssetCls asset = ((AssetCls) cbxAssets.getSelectedItem());

                if (asset != null) {
                    ((AccountRenderer) cbxAccount.getRenderer()).setAsset(asset.getKey(DCSet.getInstance()));
                    cbxAccount.repaint();
                    cbxOptions.repaint();

                }
            }
        });

        // LABEL ACCOUNT
        labelGBC.gridy = 4;
        JLabel ownerLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
        this.add(ownerLabel, labelGBC);

        // CBX ACCOUNT
        detailGBC.gridy = 4;
        this.cbxAccount = new JComboBox<Account>(new AccountsComboBoxModel());
        cbxAccount.setRenderer(new AccountRenderer(asset.getKey(DCSet.getInstance())));

        this.add(this.cbxAccount, detailGBC);

        // LABEL OPTIONS
        labelGBC.gridy = 5;
        JLabel optionsLabel = new JLabel(Lang.getInstance().translate("Option") + ":");
        this.add(optionsLabel, labelGBC);

        // CBX ACCOUNT
        detailGBC.gridy = 5;
        this.cbxOptions = new JComboBox<String>(new OptionsComboBoxModel(poll.getOptions()));
        if (this.cbxOptions.getItemCount() > option)
            this.cbxOptions.setSelectedIndex(option);
		/*		
		this.cbxOptions.setRenderer(new DefaultListCellRenderer() {
			@SuppressWarnings("rawtypes")
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				//AssetCls asset = ((AssetCls) cbxAssets.getSelectedItem());

				return super.getListCellRendererComponent(list, (String)value, index, isSelected, cellHasFocus);
			}
		});
	*/
        this.add(this.cbxOptions, detailGBC);

        // LABEL FEE
        labelGBC.gridy = 6;
        JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee Power") + ":");
        this.add(feeLabel, labelGBC);

        // TXT FEE
        detailGBC.gridy = 6;
        this.txtFeePow = new JTextField();
        this.txtFeePow.setText("0");
        this.add(this.txtFeePow, detailGBC);

        // ADD EXCHANGE BUTTON
        detailGBC.gridy = 7;
        voteButton = new JButton(Lang.getInstance().translate("Vote"));
        voteButton.setPreferredSize(new Dimension(100, 25));
        voteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onVoteClick();
            }
        });
        this.add(voteButton, detailGBC);

        setPreferredSize(new java.awt.Dimension(800, 650));
        setMinimumSize(new java.awt.Dimension(650, 23));
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        // PACK
        this.pack();
        this.setResizable(true);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    public void onVoteClick() {

        // DISABLE
        this.voteButton.setEnabled(false);

        // CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            // ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (password.equals("")) {
                this.voteButton.setEnabled(true);
                return;
            }
            if (!Controller.getInstance().unlockWallet(password)) {
                // WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
                        Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                // ENABLE
                this.voteButton.setEnabled(true);
                return;
            }
        }

        // READ CREATOR
        Account sender = (Account) cbxAccount.getSelectedItem();

        int feePow = 0;
        try {
            // READ FEE
            feePow = Integer.parseInt(txtFeePow.getText());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee Power!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }

        // CREATE POLL
        PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.getInstance().translate(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        int option = this.cbxOptions.getSelectedIndex();

        Transaction transaction = Controller.getInstance().createItemPollVote(creator, poll.getKey(DCSet.getInstance()),
                option, feePow);

        // CHECK VALIDATE MESSAGE
        String Status_text = "";
        IssueConfirmDialog dd = new IssueConfirmDialog(null, true, transaction,
                Lang.getInstance().translate("Vote on Poll"),
                (int) (this.getWidth() / 1.2), (int) (this.getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation Transaction"));
        VoteOnItemPollDetailsFrame ww = new VoteOnItemPollDetailsFrame((VoteOnItemPollTransaction) transaction);
        dd.jScrollPane1.setViewportView(ww);
        dd.pack();
        dd.setLocationRelativeTo(this);
        dd.setVisible(true);

        // JOptionPane.OK_OPTION
        if (dd.isConfirm) {

            int result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);

            // CHECK VALIDATE MESSAGE
            if (result == Transaction.VALIDATE_OK) {
                // RESET FIELDS

                // TODO "A" ??

                // TODO "A" ??
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate("Message and/or payment has been sent!"),
                        Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate(OnDealClick.resultMess(result)),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }

        // ENABLE
        this.voteButton.setEnabled(true);
    }
}
