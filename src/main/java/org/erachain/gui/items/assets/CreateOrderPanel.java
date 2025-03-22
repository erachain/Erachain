package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.CreateOrderTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.Gui;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.ResultDialog;
import org.erachain.gui.items.accounts.AccountRenderer;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MDecimalFormatedTextField;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.CreateOrderDetailsFrame;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;

@SuppressWarnings("serial")
public class CreateOrderPanel extends JPanel {
    static Logger LOGGER = LoggerFactory.getLogger(CreateOrderPanel.class);
    public JComboBox<Account> cbxAccount;
    public MDecimalFormatedTextField txtAmountHave;
    public MDecimalFormatedTextField txtPrice;
    private AssetCls have;
    private AssetCls want;
    private JButton sellButton;
    private JComboBox<String> txtFeePow;
    private MDecimalFormatedTextField txtAmountWant;
    private boolean SHOW_HINTS = false;
    boolean noUpdateFields = false;

    MDecimalFormatedTextField[] queve = new MDecimalFormatedTextField[2];

    public CreateOrderPanel(AssetCls have, AssetCls want, boolean buying, String account) {
        this.setLayout(new GridBagLayout());
        this.have = have;
        this.want = want;

        // PADDING
        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        // LABEL GBC
        GridBagConstraints superhintGBC = new GridBagConstraints();
        superhintGBC.insets = new Insets(0, 5, 5, 0);
        superhintGBC.fill = GridBagConstraints.BOTH;
        superhintGBC.anchor = GridBagConstraints.SOUTHWEST;
        superhintGBC.gridx = 0;
        superhintGBC.gridwidth = 3;
        superhintGBC.weightx = superhintGBC.weighty = 1.0;
        superhintGBC.weighty = 1.0;

        // LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(0, 5, 5, 0);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.gridx = 0;

        // DETAIL GBC
        GridBagConstraints detailGBC = new GridBagConstraints();
        detailGBC.insets = new Insets(0, 5, 5, 0);
        detailGBC.gridx = 1;
        detailGBC.fill = java.awt.GridBagConstraints.HORIZONTAL;
        detailGBC.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        detailGBC.weightx = 1.0;

        // DETAIL GBC
        GridBagConstraints assetHintGBC = new GridBagConstraints();
        assetHintGBC.insets = new Insets(0, 5, 5, 0);
        assetHintGBC.anchor = GridBagConstraints.FIRST_LINE_START;
        assetHintGBC.fill = java.awt.GridBagConstraints.HORIZONTAL;
        assetHintGBC.weightx = 1.0;

        assetHintGBC.gridx = 2;

        labelGBC.gridy = 0;
        detailGBC.gridy = 0;

        // label buy
        // DETAIL GBC
        GridBagConstraints Label_sell_buy = new GridBagConstraints();
        Label_sell_buy.insets = new Insets(0, 5, 5, 0);
        Label_sell_buy.gridx = 0;
        Label_sell_buy.gridwidth = 3;
        Label_sell_buy.fill = java.awt.GridBagConstraints.HORIZONTAL;
        Label_sell_buy.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        Label_sell_buy.weightx = 1.0;

        Label_sell_buy.gridy = ++labelGBC.gridy;
        detailGBC.gridy = ++detailGBC.gridy;
        JLabel lblWish = new JLabel(
                "<html>" + (buying ? Lang.T("To buy") + ": " + "<b>" + this.want.toString()
                        : Lang.T("To sell") + ": " + "<b>" + this.have.toString()) + "</b></html>");

        this.add(lblWish, Label_sell_buy);

        JLabel lblResult = new JLabel(
                "<html>" + (buying ? Lang.T("For") + ": <b>" + this.have.toString()
                        : Lang.T("For") + ": <b>" + this.want.toString()) + "</b></html>");

        // Label sell
        Label_sell_buy.gridy = ++labelGBC.gridy;
        detailGBC.gridy = ++detailGBC.gridy;
        this.add(lblResult, Label_sell_buy);

        // LABEL FROM
        labelGBC.gridy = ++labelGBC.gridy;

        // COMBOBOX FROM
        detailGBC.gridx = 0;
        detailGBC.gridy = ++detailGBC.gridy;
        detailGBC.gridwidth = 3;
        this.cbxAccount = new JComboBox<Account>(new AccountsComboBoxModel(TransactionAmount.ACTION_SEND));
        this.cbxAccount.setRenderer(new AccountRenderer(this.have.getKey()));

        // select accounts in combobox
        if (account != "" && account != null) {
            for (int i = 0; this.cbxAccount.getModel().getSize() > i; i++) {
                Account elem = this.cbxAccount.getModel().getElementAt(i);
                if (elem.getAddress().toString().contentEquals(account))
                    this.cbxAccount.setSelectedIndex(i);

                this.cbxAccount.repaint();
            }
            this.cbxAccount.setEnabled(false);
        }
        cbxAccount
                .setPreferredSize(new Dimension(100, cbxAccount.getFontMetrics(cbxAccount.getFont()).getHeight() + 8));

        this.add(this.cbxAccount, detailGBC);
        detailGBC.gridx = 1;
        detailGBC.gridwidth = 1;
        // ASSET HINT
        assetHintGBC.gridy = detailGBC.gridy;
        if (SHOW_HINTS) {
            JLabel accountHintLabel = new JLabel(have.getShortName());
            this.add(accountHintLabel, assetHintGBC);
        }

        // LABEL AMOUNT
        labelGBC.gridy++;

        String mes = buying ? Lang.T("Quantity to buy")
                : Lang.T("Quantity to sell");

        JLabel amountLabel = new JLabel(mes + ":");
        this.add(amountLabel, labelGBC);

        // AMOUNT
        detailGBC.gridy++;
        this.txtAmountHave = new MDecimalFormatedTextField(Double.class);
        // set scale
        this.txtAmountHave.setScale(have == null ? 8 : buying ? want.getScale() : have.getScale());
        this.add(this.txtAmountHave, detailGBC);

        // ASSET HINT
        assetHintGBC.gridy = detailGBC.gridy;
        if (SHOW_HINTS) {
            JLabel amountHintLabel = new JLabel(buying ? want.getShortName() : have.getShortName());
            this.add(amountHintLabel, assetHintGBC);
        }

        // LABEL PRICE
        labelGBC.gridy++;
        JLabel priceLabel = new JLabel(
                "<html><b>" + Lang.T("Price per unit") + " " + ":</b></html>");
        this.add(priceLabel, labelGBC);
        // PRICE
        detailGBC.gridy++;
        txtPrice = new MDecimalFormatedTextField(Double.class);
        // set scale

        txtPrice.setScale(setScale(6, want));
        this.add(txtPrice, detailGBC);
        // ASSET HINT
        assetHintGBC.gridy = detailGBC.gridy;
        if (SHOW_HINTS) {
            JLabel priceHintLabel = new JLabel(buying ? have.getShortName() : want.getShortName());
            this.add(priceHintLabel, assetHintGBC);
        }

        // ON PRICE CHANGE
        txtPrice.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                if (noUpdateFields) return;

                calculateAmounts(txtPrice, buying, true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (noUpdateFields) return;

                calculateAmounts(txtPrice, buying, true);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (noUpdateFields) return;

                calculateAmounts(txtPrice, buying, true);
            }
        });

        // LABEL AMOUNT
        labelGBC.gridy++;

        mes = Lang.T("Total");

        JLabel buyingAmountLabel = new JLabel(mes + ":");
        this.add(buyingAmountLabel, labelGBC);

        // AMOUNT
        detailGBC.gridy++;
        txtAmountWant = new MDecimalFormatedTextField(Double.class);
        txtAmountWant.setScale(want == null ? 8 : buying ? have.getScale() : want.getScale());
        txtAmountWant.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        this.add(txtAmountWant, detailGBC);

        // ON PRICE CHANGE
        txtAmountWant.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                if (noUpdateFields) return;

                calculateAmounts(txtAmountWant, buying, true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (noUpdateFields) return;

                calculateAmounts(txtAmountWant, buying, true);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (noUpdateFields) return;

                calculateAmounts(txtAmountWant, buying, true);
            }
        });


        // ASSET HINT
        assetHintGBC.gridy = detailGBC.gridy;
        if (SHOW_HINTS) {
            JLabel buyingAmountHintLabel = new JLabel(buying ? have.getShortName() : want.getShortName());
            this.add(buyingAmountHintLabel, assetHintGBC);
        }

        // ON PRICE CHANGE
        txtAmountHave.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                if (noUpdateFields) return;

                calculateAmounts(txtAmountHave, buying, true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (noUpdateFields) return;

                calculateAmounts(txtAmountHave, buying, true);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (noUpdateFields) return;

                calculateAmounts(txtAmountHave, buying, true);
            }
        });

        // LABEL FEE
        labelGBC.gridy++;
        JLabel feeLabel = new JLabel(Lang.T("Fee Power") + ": ");
        feeLabel.setVisible(Gui.SHOW_FEE_POWER);
        this.add(feeLabel, labelGBC);

        // FEE
        detailGBC.gridy++;
        txtFeePow = new JComboBox<String>();
        txtFeePow.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8"}));
        txtFeePow.setSelectedIndex(0);
        txtFeePow.setVisible(Gui.SHOW_FEE_POWER);
        this.add(txtFeePow, detailGBC);

        // ASSET HINT
        assetHintGBC.gridy = detailGBC.gridy;
        /////JLabel feeHintLabel = new JLabel(Controller.getInstance().getAsset(AssetCls.FEE_KEY).getName());
        ////this.add(feeHintLabel, assetHintGBC);

        // ADD SELL BUTTON
        labelGBC.gridy++;
        labelGBC.gridwidth = 3;

        labelGBC.gridy++;

        if (buying) {
            this.sellButton = new JButton(Lang.T("Buy"));
            this.sellButton.setBackground(new Color(204, 255, 204));
        } else {
            this.sellButton = new JButton(Lang.T("Sell"));
            this.sellButton.setBackground(new Color(255, 153, 153));
        }

        sellButton.setEnabled(false);
        this.sellButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSellClick(buying);
            }
        });
        labelGBC.insets = new java.awt.Insets(10, 20, 0, 20);
        this.add(this.sellButton, labelGBC);
    }

    //нужно отдельно цену считаь для подстановок извне
    public synchronized void calculateAmounts(MDecimalFormatedTextField editedField, boolean buying, boolean recurse) {

        sellButton.setEnabled(false);

        if (recurse) {
            noUpdateFields = true;

            addQueve(editedField);
        }

        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal price = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        try {

            try {
                if (recurse && notQueved(txtAmountWant)
                        || !recurse && queve[1].equals(txtAmountWant)) {

                    amount = new BigDecimal(txtAmountHave.getText());
                    if (amount.signum() == 0) {
                        return;
                    }

                    price = new BigDecimal(txtPrice.getText());
                    if (price.signum() == 0) {
                        return;
                    }

                    if (buying) {
                        total = price.multiply(amount).setScale(have.getScale(), RoundingMode.HALF_DOWN);
                    } else {
                        total = price.multiply(amount).setScale(want.getScale(), RoundingMode.HALF_DOWN);
                    }
                    txtAmountWant.setText(total.toPlainString());

                } else if (recurse && notQueved(txtPrice)
                        || !recurse && queve[1].equals(txtPrice)) {

                    amount = new BigDecimal(txtAmountHave.getText());
                    if (amount.signum() == 0) {
                        return;
                    }

                    total = new BigDecimal(txtAmountWant.getText());
                    if (total.signum() == 0) {
                        return;
                    }

                    if (buying) {
                        price = Order.calcPrice(amount, total);
                    } else {
                        price = Order.calcPrice(amount, total);
                    }
                    txtPrice.setText(price.toPlainString());

                } else if (recurse && notQueved(txtAmountHave)
                        || !recurse && queve[1].equals(txtAmountHave)) {

                    total = new BigDecimal(txtAmountWant.getText());
                    if (total.signum() == 0) {
                        return;
                    }

                    price = new BigDecimal(txtPrice.getText());
                    if (price.signum() == 0) {
                        return;
                    }

                    if (buying) {
                        amount = total.divide(price, want.getScale(), RoundingMode.HALF_DOWN);
                    } else {
                        amount = total.divide(price, have.getScale(), RoundingMode.HALF_DOWN);
                    }
                    txtAmountHave.setText(amount.toPlainString());
                }
            } catch (Exception e) {
                sellButton.setEnabled(false);
                return;
            }

            if (recurse) {

                if (queve[1] != null) {
                    // так же пересчитать еще и поле второе для точности
                    calculateAmounts(queve[1], buying, false);
                }

                try {
                    BigDecimal value = new BigDecimal(editedField.getText());
                    if (value.signum() <= 0) {
                        sellButton.setEnabled(false);
                    } else {
                        sellButton.setEnabled(true);
                    }
                } catch (Exception e) {
                    sellButton.setEnabled(false);
                }

                noUpdateFields = false;

            } else {

                if (amount.signum() != 0 && price.signum() != 0 && total.signum() != 0) {
                    sellButton.setEnabled(true);
                }

            }

        } finally {

            noUpdateFields = false;

        }

    }

    public synchronized void calculateWant(BigDecimal amount, BigDecimal price, boolean buying) {

        noUpdateFields = true;

        try {

            BigDecimal total;

            if (buying) {
                txtAmountHave.setText(amount.toPlainString());
                addQueve(txtAmountHave); // очередность запомним иначе при первом двойном клике потом цену не пересчитывает
                txtPrice.setText(price.toPlainString());
                addQueve(txtPrice);
                /////////// Тут мы покупаем - значит втречно рынку нужно меньшую цену дать
                total = price.multiply(amount)
                        // точность от HAVE
                        .setScale(have.getScale(), RoundingMode.HALF_DOWN);
                txtAmountWant.setText(total.toPlainString());

            } else {
                txtAmountHave.setText(amount.toPlainString());
                addQueve(txtAmountHave); // очередность запомним иначе при первом двойном клике потом цену не пересчитывает
                txtPrice.setText(price.toPlainString());
                addQueve(txtPrice);
                /////////// Тут мы продаем - значит втречно рынку нужно большую цену дать
                total = price.multiply(amount)
                        // точность от WANT
                        .setScale(want.getScale(), RoundingMode.HALF_UP);
                txtAmountWant.setText(total.toPlainString());
            }

            sellButton.setEnabled(true);

        } catch (Exception e) {
            sellButton.setEnabled(false);
        }

        noUpdateFields = false;

    }

    public synchronized void setFields(BigDecimal amountHave, BigDecimal price, BigDecimal amountWant) {

        noUpdateFields = true;

        try {

            txtAmountHave.setText(amountHave.toPlainString());
            addQueve(txtAmountHave); // очередность запомним иначе при первом двойном клике потом цену не пересчитывает
            txtPrice.setText(price.toPlainString());
            addQueve(txtPrice);
            txtAmountWant.setText(amountWant.toPlainString());

            sellButton.setEnabled(true);

        } catch (Exception e) {
            sellButton.setEnabled(false);
        }

        noUpdateFields = false;

    }

    public void onSellClick(boolean buying) {

        // DISABLE
        this.sellButton.setEnabled(false);

        try {
            // READ CREATOR
            Account sender = (Account) this.cbxAccount.getSelectedItem();

            int feePow;
            BigDecimal amountHave;
            BigDecimal amountWant;
            long parse = 0;
            try {
                // READ FEE
                feePow = Integer.parseInt((String) this.txtFeePow.getSelectedItem());

                // READ AMOUNT
                parse = 1;
                amountHave = new BigDecimal(this.txtAmountHave.getText());

                // READ PRICE
                parse = 2;
                // price = new BigDecimal(this.txtPrice.getText());
                amountWant = new BigDecimal(this.txtAmountWant.getText());

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);

                if (parse == 0) {
                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid fee") + "!",
                            Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                }
                if (parse == 1) {
                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid amount") + "!",
                            Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                }
                if (parse == 2) {
                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid price") + "!",
                            Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                }
                return;
            }

            if (amountWant.compareTo(new BigDecimal(0)) == 0 || amountHave.compareTo(new BigDecimal(0)) == 0) {
                return;
            }

            // CREATE ORDER

            // BigDecimal amountWant = amountHave.multiply(price);
            if (buying) {
                BigDecimal amountTemp = amountWant;
                amountWant = amountHave;
                amountHave = amountTemp;
            }

            if (false) {
                // for develop
                JOptionPane.showMessageDialog(new JFrame(), amountHave.toPlainString() + " - " + amountWant.toPlainString(),
                        Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            // CHECK IF WALLET UNLOCKED
            if (!Controller.getInstance().isWalletUnlocked()) {
                // ASK FOR PASSWORD
                String password = PasswordPane.showUnlockWalletDialog(this);
                if (!Controller.getInstance().unlockWallet(password)) {
                    // WRONG PASSWORD
                    JOptionPane.showMessageDialog(null, Lang.T("Invalid password"),
                            Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);


                    return;
                }
            }

            PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress());
            if (creator == null) {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.T(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                        Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            Transaction transaction = Controller.getInstance().createOrder(creator, this.have, this.want,
                    amountHave.setScale(this.have.getScale(), RoundingMode.HALF_UP),
                    amountWant.setScale(this.want.getScale(), RoundingMode.HALF_DOWN), feePow);

            String Status_text = "";
            IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                    Lang.T("Send Order"), (int) (MainFrame.getInstance().getWidth() / 1.2),
                    (int) (MainFrame.getInstance().getHeight() / 1.2), Status_text,
                    Lang.T("Confirmation Transaction") + ": "
                            + Lang.T("order creation"));

            CreateOrderDetailsFrame ww = new CreateOrderDetailsFrame((CreateOrderTransaction) transaction);
            confirmDialog.jScrollPane1.setViewportView(ww);
            confirmDialog.pack();
            confirmDialog.setLocationRelativeTo(null);
            confirmDialog.setVisible(true);

            // JOptionPane.OK_OPTION
            if (confirmDialog.isConfirm > 0) {
                ResultDialog.make(this, transaction, confirmDialog.isConfirm == IssueConfirmDialog.TRY_FREE, null, null);
            }

        } finally {
            // ENABLE
            this.sellButton.setEnabled(true);
        }

    }

    // confirm asset & return scale
    private int setScale(int powerAmountHave, AssetCls assetWant) {
        if (assetWant != null) {
            return Order.calcPriceScale(powerAmountHave, assetWant.getScale(), 1);
        }
        return 10;
    }

    // добавляем как в очередь
    // только если он там не первый
    private void addQueve(MDecimalFormatedTextField item) {
        if (queve[0] != null) {
            if (queve[0].equals(item))
                return;
            queve[1] = queve[0];
        }

        queve[0] = item;
    }

    // элемента нет в очереди?
    private boolean notQueved(MDecimalFormatedTextField item) {
        if (queve[0].equals(item)
                || queve[1].equals(item))
            return false;
        return true;
    }
}
