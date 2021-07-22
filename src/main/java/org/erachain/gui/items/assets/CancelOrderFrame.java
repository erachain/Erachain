package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.Gui;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.ResultDialog;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

//import java.math.BigDecimal;

@SuppressWarnings("serial")
public class CancelOrderFrame extends JDialog {
    private Order order;
    private JTextField txtFeePow;
    private JButton cancelOrderButton;

    public CancelOrderFrame(Order order) {
        //super(Controller.getInstance().getApplicationName(false) + " - " + Lang.T("Cancel Order"));
        setTitle(Controller.getInstance().getApplicationName(false) + " - " + Lang.T("Cancel Order"));
        this.order = order;
        //	setAlwaysOnTop(true);
        setModal(true);

        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        //CLOSE
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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

        //LABEL TIMESTAMP
        labelGBC.gridy = 1;
        JLabel nameLabel = new JLabel(Lang.T("Timestamp") + ":");
        this.add(nameLabel, labelGBC);

        //TXT TIMESTAMP
        txtGBC.gridy = 1;

        Transaction transaction = DCSet.getInstance().getTransactionFinalMap().get(order.getId());
        JTextField txtTimestamp = new JTextField(DateTimeFormat.timestamptoString(transaction.getTimestamp()));
        txtTimestamp.setEditable(false);
        this.add(txtTimestamp, txtGBC);

        //LABEL HAVE
        labelGBC.gridy = 2;
        JLabel haveLabel = new JLabel(Lang.T("Have") + ":");
        this.add(haveLabel, labelGBC);

        //TXT HAVE
        txtGBC.gridy = 2;
        JTextField txtHave = new JTextField(String.valueOf(order.getHaveAssetKey()));
        txtHave.setEditable(false);
        this.add(txtHave, txtGBC);

        //LABEL WANT
        labelGBC.gridy = 3;
        JLabel wantLabel = new JLabel(Lang.T("Want") + ":");
        this.add(wantLabel, labelGBC);

        //TXT WANT
        txtGBC.gridy = 3;
        JTextField txtWant = new JTextField(String.valueOf(order.getWantAssetKey()));
        txtWant.setEditable(false);
        this.add(txtWant, txtGBC);

        //LABEL AMOUNT
        labelGBC.gridy = 4;
        JLabel amountLabel = new JLabel(Lang.T("Amount") + ":");
        this.add(amountLabel, labelGBC);

        //TXT WANT
        txtGBC.gridy = 4;
        JTextField txtAmount = new JTextField(order.getAmountHave().toPlainString());
        txtAmount.setEditable(false);
        this.add(txtAmount, txtGBC);

        //LABEL PRICE
        labelGBC.gridy = 5;
        JLabel priceLabel = new JLabel(Lang.T("Price") + ":");
        this.add(priceLabel, labelGBC);

        //TXT PRICE
        txtGBC.gridy = 5;
        JTextField txtPrice = new JTextField(order.getPrice().toPlainString());
        txtPrice.setEditable(false);
        this.add(txtPrice, txtGBC);

        //LABEL FULFILLED
        labelGBC.gridy = 6;
        JLabel fulfilledLabel = new JLabel(Lang.T("Fulfilled") + ":");
        this.add(fulfilledLabel, labelGBC);

        //TXT FULFILLED
        txtGBC.gridy = 6;
        JTextField txtFulfilled = new JTextField(order.getFulfilledHave().toPlainString());
        txtFulfilled.setEditable(false);
        this.add(txtFulfilled, txtGBC);

        //LABEL FEE
        labelGBC.gridy = 7;
        JLabel feeLabel = new JLabel(Lang.T("Fee Power") + ":");
        feeLabel.setVisible(Gui.SHOW_FEE_POWER);
        this.add(feeLabel, labelGBC);

        //TXT FEE
        txtGBC.gridy = 7;
        txtFeePow = new JTextField();
        this.txtFeePow.setText("0");
        txtFeePow.setVisible(Gui.SHOW_FEE_POWER);
        this.add(txtFeePow, txtGBC);

        //BUTTON CANCEL SALE
        buttonGBC.gridy = 8;
        cancelOrderButton = new JButton(Lang.T("Cancel Order"));
        //    cancelOrderButton.setPreferredSize(new Dimension(120, 25));
        cancelOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancelOrderClick();
            }
        });
        this.add(cancelOrderButton, buttonGBC);

        //PACK
        this.pack();
        this.setResizable(true);
        this.setPreferredSize(new Dimension((int) (MainFrame.getInstance().getWidth() * 0.8), (int) (MainFrame.getInstance().getHeight() * .8)));
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void onCancelOrderClick() {
        //DISABLE
        this.cancelOrderButton.setEnabled(false);

        //CHECK IF NETWORK OK
        if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            //NETWORK NOT OK
            JOptionPane.showMessageDialog(null, Lang.T("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

            //ENABLE
            this.cancelOrderButton.setEnabled(true);

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
                this.cancelOrderButton.setEnabled(true);

                return;
            }
        }

        int feePow = 0;
        try {
            feePow = Integer.parseInt(txtFeePow.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, Lang.T("Invalid fee!"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

            this.cancelOrderButton.setEnabled(true);

            return;
        }

        //CREATE NAME UPDATE
        PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(order.getCreator().getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        Transaction cancelOrder = Controller.getInstance().cancelOrder1(creator, order, feePow);
        if (ResultDialog.make(this, cancelOrder, false, null)) {
            dispose();
        }

        //ENABLE
        this.cancelOrderButton.setEnabled(true);
    }
}
