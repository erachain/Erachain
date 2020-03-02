package org.erachain.gui.library;

import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base32;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.items.accounts.*;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.models.AccountsTableModel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.URLViewer;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;

public class DealsPopupMenu extends JPopupMenu {

    protected Logger logger;

    public AccountsTableModel tableModel;
    protected JComboBox<ItemCls> assetSelector;
    protected AssetCls asset;
    protected PublicKeyAccount pubKey;
    protected MTable table;

    private JMenuItem sendAsset;
    private JMenuItem sendMail;
    private JMenuItem debtAsset;
    private JMenuItem debtAssetReturn;
    private JMenuItem debtAssetBackward;
    private JMenuItem holdAsset;

    public DealsPopupMenu(AccountsTableModel tableModel, MTable table, JComboBox<ItemCls> assetSelector) {

        logger = LoggerFactory.getLogger(getClass());

        this.tableModel = tableModel;
        this.table = table;
        this.assetSelector = assetSelector;

        sendMail = new JMenuItem(Lang.getInstance().translate("Send mail"));
        sendMail.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertTab(new MailSendPanel(pubKey, null, null));
            }
        });
        this.add(sendMail);

        this.addSeparator();

        sendAsset = new JMenuItem(Lang.getInstance().translate("Send"));
        sendAsset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // AccountAssetLendPanel
                MainPanel.getInstance().insertTab(new AccountAssetSendPanel(asset, TransactionAmount.ACTION_SEND, pubKey, null, null, null));

            }
        });
        this.add(sendAsset);

        this.addSeparator();

        debtAsset = new JMenuItem(Lang.getInstance().translate("Lend"));
        debtAsset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //new AccountLendDialog(asset, pubKey);
                MainPanel.getInstance().insertTab(new AccountAssetLendPanel(asset, pubKey, null, null));

            }
        });
        this.add(debtAsset);

        debtAssetReturn = new JMenuItem(Lang.getInstance().translate("Repay Debt"));
        debtAssetReturn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertTab(new AccountAssetRepayDebtPanel(asset, pubKey, null, null));

            }
        });
        this.add(debtAssetReturn);

        // asset != null && asset.isOutsideType()? "Подтвердить погашение требования" : "Confiscate Debt")
        debtAssetBackward = new JMenuItem(Lang.getInstance().translate("Confiscate Debt"));
        debtAssetBackward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertTab(new AccountAssetConfiscateDebtPanel(asset, pubKey, null, null));

            }
        });
        this.add(debtAssetBackward);

        this.addSeparator();

        holdAsset = new JMenuItem(Lang.getInstance().translate("Hold"));
        holdAsset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertTab(new AccountAssetHoldPanel(asset, pubKey, null, null));

            }
        });
        this.add(holdAsset);

        this.addSeparator();

        JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Copy Account"));
        copyAddress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //      int row = table.getSelectedRow();
                //      if (row < 1 ) return;

                //      row = table.convertRowIndexToModel(row);
                //      Account account = tableModel.getAccount(row);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(pubKey.getAddress());
                clipboard.setContents(value, null);
            }
        });
        this.add(copyAddress);

        JMenuItem copyBalance = new JMenuItem(Lang.getInstance().translate("Copy Balance"));
        copyBalance.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                //long key = cbxFavorites.getItemAt(cbxFavorites.getSelectedIndex()).getKey();
                long key = asset.getKey();
                StringSelection value = new StringSelection(pubKey.getBalance(key).toString());
                clipboard.setContents(value, null);
            }
        });

        this.add(copyBalance);

        this.addSeparator();

        JMenuItem copyPublicKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key"));
        copyPublicKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(pubKey.getBase58());
                clipboard.setContents(value, null);
            }
        });
        this.add(copyPublicKey);

        JMenuItem copyBankKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key for BANK"));
        copyBankKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String bankKeyAccount = "+" + Base32.encode(pubKey.getPublicKey());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(bankKeyAccount);
                clipboard.setContents(value, null);
            }
        });
        this.add(copyBankKey);

        JMenuItem set_name = new JMenuItem(Lang.getInstance().translate("Edit name"));
        set_name.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AccountSetNameDialog(pubKey.getAddress());
                table.repaint();
            }
        });
        this.add(set_name);

        this.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    URLViewer.openWebpage(new URL("http://" + Settings.getInstance().getBlockexplorerURL()
                            + ":" + Settings.getInstance().getWebPort() + "/index/blockexplorer.html"
                            + "?address=" + pubKey.getAddress()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });

        add(setSeeInBlockexplorer);

        this.addPopupMenuListener(new PopupMenuListener() {

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

                // TODO Auto-generated method stub

                asset = (AssetCls) assetSelector.getSelectedItem();

                int row = table.getSelectedRow();
                if (row < 0)
                    return;
                row = table.convertRowIndexToModel(row);

                pubKey = tableModel.getItem(row);
                init();

            }


        });

    }

    public void init() {

        this.sendAsset.setEnabled(true);
        this.holdAsset.setEnabled(true);
        this.debtAsset.setEnabled(true);
        this.debtAssetReturn.setEnabled(true);
        this.debtAssetBackward.setEnabled(true);

        this.sendMail.setText(Lang.getInstance().translate("Send Mail"));

        String actionName = asset.viewAssetTypeAction(false, TransactionAmount.ACTION_SEND);
        if (actionName == null) {
            this.sendAsset.setVisible(false);
        } else {
            this.sendAsset.setText(Lang.getInstance().translate(actionName));
            this.sendAsset.setVisible(true);
        }

        actionName = asset.viewAssetTypeAction(true, TransactionAmount.ACTION_HOLD);
        if (actionName == null) {
            this.holdAsset.setVisible(false);
        } else {
            this.holdAsset.setText(Lang.getInstance().translate(actionName));
            this.holdAsset.setVisible(true);
        }

        actionName = asset.viewAssetTypeAction(false, TransactionAmount.ACTION_DEBT);
        if (actionName == null) {
            this.debtAsset.setVisible(false);
        } else {
            this.debtAsset.setText(Lang.getInstance().translate(actionName));
            this.debtAsset.setVisible(true);
        }

        actionName = asset.viewAssetTypeAction(false, TransactionAmount.ACTION_REPAY_DEBT);
        if (actionName == null) {
            this.debtAssetReturn.setVisible(false);
        } else {
            this.debtAssetReturn.setText(Lang.getInstance().translate(actionName));
            this.debtAssetReturn.setVisible(true);
        }

        actionName = asset.viewAssetTypeAction(true, TransactionAmount.ACTION_DEBT);
        if (actionName == null) {
            this.debtAssetBackward.setVisible(false);
        } else {
            this.debtAssetBackward.setText(Lang.getInstance().translate(actionName));
            this.debtAssetBackward.setVisible(true);
        }

        Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>
                balance = pubKey.getBalance(asset.getKey());

        if (balance.a.b.signum() == 0
                && (asset.getQuantity() > 0
                || !pubKey.equals(asset.getOwner()))) {
            this.sendAsset.setEnabled(false);
            this.debtAsset.setEnabled(false);
        }

        if (balance.b.b.signum() == 0
                && (asset.getQuantity() > 0
                || !pubKey.equals(asset.getOwner()))) {
            this.debtAssetReturn.setEnabled(false);
        }

        switch (this.asset.getAssetType()) {

            case AssetCls.AS_BANK_GUARANTEE:

                balance = pubKey.getBalance(asset.getKey());
                if (balance.a.b.signum() > 0) {
                    this.holdAsset.setEnabled(false);
                    this.debtAssetReturn.setEnabled(false);
                    if (balance.b.b.signum() < 0) {
                        this.debtAsset.setEnabled(false);
                    } else {
                        this.debtAssetBackward.setEnabled(false);
                    }
                } else {
                    this.sendAsset.setEnabled(false);
                    this.debtAsset.setEnabled(false);
                    this.debtAssetBackward.setEnabled(false);
                    if (balance.b.b.signum() <= 0) {
                        this.debtAssetReturn.setEnabled(false);
                        this.holdAsset.setEnabled(false);
                    } else {
                        if (balance.c.b.signum() > 0) {
                            this.holdAsset.setEnabled(false);
                        }
                    }
                }

                break;

            case AssetCls.AS_BANK_GUARANTEE_TOTAL:

                balance = pubKey.getBalance(asset.getKey());
                if (pubKey.equals(asset.getOwner()) || balance.a.b.signum() > 0) {

                } else {
                    this.sendAsset.setEnabled(false);
                    this.debtAsset.setEnabled(false);
                    this.debtAssetReturn.setEnabled(false);
                }

                break;

        }
    }
}
