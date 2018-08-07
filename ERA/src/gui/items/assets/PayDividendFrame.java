package gui.items.assets;

import controller.Controller;
import core.account.Account;
import core.item.assets.AssetCls;
import core.payment.Payment;
import datachain.SortableList;
import gui.BalanceRenderer;
import gui.models.BalancesComboBoxModel;
import lang.Lang;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import utils.Pair;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class PayDividendFrame extends JFrame {
    private AssetCls asset;
    private JTextField txtAsset;
    private JTextField txtAccount;
    private JComboBox<Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> cbxAssetToPay;
    private JTextField txtAmount;
    private JTextField txtHolders;
    private JButton generateButton;

    public PayDividendFrame(AssetCls asset) {
        super(Lang.getInstance().translate("Erachain.org") + " - " + Lang.getInstance().translate("Pay Dividend"));

        this.asset = asset;

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

        //LABEL ASSET
        labelGBC.gridy = 0;
        JLabel assetLabel = new JLabel(Lang.getInstance().translate("Check") + ":");
        this.add(assetLabel, labelGBC);

        //TXT ASSET
        txtGBC.gridy = 0;
        this.txtAsset = new JTextField(asset.toString());
        this.txtAsset.setEditable(false);
        this.add(this.txtAsset, txtGBC);

        //LABEL ACCOUNT
        labelGBC.gridy = 1;
        JLabel accountLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
        this.add(accountLabel, labelGBC);

        //TXT ACCOUNT
        txtGBC.gridy = 1;
        this.txtAccount = new JTextField(asset.getOwner().getAddress());
        this.txtAccount.setEditable(false);
        this.add(this.txtAccount, txtGBC);

        //LABEL ASSET TO PAY
        labelGBC.gridy = 2;
        JLabel AssetToPayLabel = new JLabel(Lang.getInstance().translate("Check to pay") + ":");
        this.add(AssetToPayLabel, labelGBC);

        //CBX ASSET TO PAY
        txtGBC.gridy = 2;
        this.cbxAssetToPay = new JComboBox<Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>>
                (new BalancesComboBoxModel(asset.getOwner()));
        this.cbxAssetToPay.setRenderer(new BalanceRenderer());
        this.add(this.cbxAssetToPay, txtGBC);

        //LABEL AMOUNT
        labelGBC.gridy = 3;
        JLabel amountLabel = new JLabel(Lang.getInstance().translate("Amount to pay:"));
        this.add(amountLabel, labelGBC);

        //TXT AMOUNT
        txtGBC.gridy = 3;
        this.txtAmount = new JTextField();
        this.txtAmount.setText("1");
        this.add(this.txtAmount, txtGBC);

        //LABEL HOLDERS TO PAY
        labelGBC.gridy = 4;
        JLabel holdersToPayLabel = new JLabel(Lang.getInstance().translate("Holders to pay (1-400):"));
        this.add(holdersToPayLabel, labelGBC);

        //TXT QUANTITY
        txtGBC.gridy = 4;
        this.txtHolders = new JTextField();
        this.txtHolders.setText("1");
        this.add(this.txtHolders, txtGBC);

        //BUTTON GENERATE
        buttonGBC.gridy = 5;
        this.generateButton = new JButton(Lang.getInstance().translate("Generate Payment"));
        this.generateButton.setPreferredSize(new Dimension(160, 25));
        this.generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onGenerateClick();
            }
        });
        this.add(this.generateButton, buttonGBC);

        //PACK
        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    public void onGenerateClick() {
        int parsing = 0;
        try {
            //HOLDERS TO PAY
            parsing = 1;
            int holders = Integer.parseInt(this.txtHolders.getText());

            //AMOUNT TO PAY
            parsing = 2;
            BigDecimal amount = new BigDecimal(txtAmount.getText());

            //ASSET TO PAY
            long assetKey = ((Pair<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>>) this.cbxAssetToPay.getSelectedItem()).getA().b;
            AssetCls assetToPay = Controller.getInstance().getAsset(assetKey);

            //BALANCES
            SortableList<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> balances = Controller.getInstance().getBalances(this.asset.getKey());

            //GET ACCOUNTS AND THEIR TOTAL BALANCE
            List<Account> accounts = new ArrayList<Account>();
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < holders && i < balances.size(); i++) {
                Account account = new Account(balances.get(i).getA().a);
                accounts.add(account);

                total = total.add(balances.get(i).getB().a.b);
            }

            //CREATE PAYMENTS
            List<Payment> payments = new ArrayList<Payment>();
            for (Account account : accounts) {
                //CALCULATE PERCENTAGE OF TOTAL
                BigDecimal percentage = account.getBalanceUSE(this.asset.getKey()).divide(total, 8, RoundingMode.DOWN);

                //CALCULATE AMOUNT
                BigDecimal accountAmount = amount.multiply(percentage);

                //ROUND AMOUNT
                accountAmount = accountAmount.setScale(assetToPay.getScale(), RoundingMode.DOWN);

                //CHECK IF AMOUNT NOT ZERO
                if (accountAmount.compareTo(BigDecimal.ZERO) > 0) {
                    Payment payment = new Payment(account, assetToPay.getKey(), accountAmount);
                    payments.add(payment);
                }
            }

            new MultiPaymentFrame(this.asset, payments);
        } catch (Exception e) {
            //CHECK WHERE PARSING ERROR HAPPENED
            switch (parsing) {
                case 1:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid holders to pay!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case 2:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid amount to pay!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;
            }
        }
    }
}
