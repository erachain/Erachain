package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.payment.Payment;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.Gui;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.models.PaymentsTableModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.utils.BigDecimalStringComparator;
import org.erachain.utils.Pair;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class MultiPaymentFrame extends JFrame {
    private AssetCls asset;
    private List<Payment> payments;

    private JTextField txtAccount;
    private JButton sendButton;
    private JTextField txtFeePow;

    @SuppressWarnings("unchecked")
    public MultiPaymentFrame(AssetCls asset, List<Payment> payments) {
        super(Controller.getInstance().getApplicationName(false) + " - " + Lang.T("Pay dividend"));

        this.asset = asset;
        this.payments = payments;

        //CLOSE
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        //LAYOUT
        this.setLayout(new GridBagLayout());

        //PADDING
        ((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));

        //LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(5, 5, 5, 5);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.weightx = 0;
        labelGBC.gridx = 0;

        //COMBOBOX GBC
        GridBagConstraints cbxGBC = new GridBagConstraints();
        cbxGBC.insets = new Insets(5, 5, 5, 5);
        cbxGBC.fill = GridBagConstraints.NONE;
        cbxGBC.anchor = GridBagConstraints.NORTHWEST;
        cbxGBC.weightx = 0;
        cbxGBC.gridx = 1;

        //TEXTFIELD GBC
        GridBagConstraints txtGBC = new GridBagConstraints();
        txtGBC.insets = new Insets(5, 5, 5, 5);
        txtGBC.fill = GridBagConstraints.HORIZONTAL;
        txtGBC.anchor = GridBagConstraints.NORTHWEST;
        txtGBC.weightx = 1;
        txtGBC.gridwidth = 2;
        txtGBC.gridx = 1;

        //BUTTON GBC
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(5, 5, 5, 5);
        buttonGBC.fill = GridBagConstraints.NONE;
        buttonGBC.anchor = GridBagConstraints.NORTHWEST;
        buttonGBC.gridwidth = 2;
        buttonGBC.gridx = 0;


        //LABEL ACCOUNT
        labelGBC.gridy = 0;
        JLabel accountLabel = new JLabel(Lang.T("Account") + ":");
        this.add(accountLabel, labelGBC);

        //TXT ACCOUNT
        txtGBC.gridy = 0;
        this.txtAccount = new JTextField(asset.getMaker().getAddress());
        this.txtAccount.setEditable(false);
        this.add(this.txtAccount, txtGBC);

        //LABEL PAYMENTS
        labelGBC.gridy = 1;
        JLabel paymentsLabel = new JLabel(Lang.T("Payments") + ":");
        this.add(paymentsLabel, labelGBC);

        //OPTIONS
        txtGBC.gridy = 1;
        PaymentsTableModel paymentsTableModel = new PaymentsTableModel(this.payments);
        JTable table = Gui.createSortableTable(paymentsTableModel, 1);

        TableRowSorter<PaymentsTableModel> sorter = (TableRowSorter<PaymentsTableModel>) table.getRowSorter();
        sorter.setComparator(PaymentsTableModel.COLUMN_AMOUNT, new BigDecimalStringComparator());

        this.add(new JScrollPane(table), txtGBC);

        //LABEL FEE
        labelGBC.gridy = 2;
        JLabel feeLabel = new JLabel(Lang.T("Fee Power") + ":");
        feeLabel.setVisible(Gui.SHOW_FEE_POWER);
        this.add(feeLabel, labelGBC);

        //TXT AMOUNT
        txtGBC.gridy = 2;
        txtFeePow = new JTextField();

        //BigDecimal fee = BigDecimal.ONE;
        //fee = fee.add(BigDecimal.valueOf(this.payments.size()).divide(BigDecimal.valueOf(5)));
        txtFeePow.setText("0");
        txtFeePow.setVisible(Gui.SHOW_FEE_POWER);

        this.add(txtFeePow, txtGBC);

        //BUTTON GENERATE
        buttonGBC.gridy = 3;
        this.sendButton = new JButton(Lang.T("Send"));
        this.sendButton.setPreferredSize(new Dimension(160, 25));
        this.sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSendClick();
            }
        });
        this.add(this.sendButton, buttonGBC);

        //PACK
        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void onSendClick() {
        //DISABLE
        this.sendButton.setEnabled(false);

        //CHECK IF NETWORK OK
        if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            //NETWORK NOT OK
            JOptionPane.showMessageDialog(null, Lang.T("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

            //ENABLE
            this.sendButton.setEnabled(true);

            return;
        }

        //CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            //ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (!Controller.getInstance().unlockWallet(password)) {
                //WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.T("Invalid password"), Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                this.sendButton.setEnabled(true);

                return;
            }
        }

        int parsing = 0;
        try {
            //READ FEE
            parsing = 2;
            int feePow = Integer.parseInt(txtFeePow.getText());

            //CREATE MULTI PAYMENT
            PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(this.asset.getMaker().getAddress());
            if (creator == null) {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.T(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                        Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            Pair<Transaction, Integer> result = Controller.getInstance().sendMultiPayment(creator, this.payments, feePow);

            //CHECK VALIDATE MESSAGE
            switch (result.getB()) {
                case Transaction.VALIDATE_OK:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Payment has been sent!"), Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);
                    break;

                case Transaction.INVALID_PAYMENTS_LENGTH:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("The amount of payments must be between (1-400)!"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case Transaction.INVALID_ADDRESS:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid Account!"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case Transaction.NEGATIVE_AMOUNT:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Amount must be positive!"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case Transaction.NOT_ENOUGH_FEE:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Not enough %fee% balance!").replace("%fee%", AssetCls.FEE_NAME), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case Transaction.NO_BALANCE:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Not enough balance!"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                default:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Unknown error!"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

            }
        } catch (Exception e) {
            //CHECK WHERE PARSING ERROR HAPPENED
            switch (parsing) {

                case 2:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid fee!"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    break;
            }
        }

        //ENABLE
        this.sendButton.setEnabled(true);
    }
}
