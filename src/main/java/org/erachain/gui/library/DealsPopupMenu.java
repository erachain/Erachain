package org.erachain.gui.library;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base32;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.items.accounts.*;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.items.statement.IssueDocumentPanel;
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

import static org.erachain.core.item.assets.AssetTypes.AS_BANK_GUARANTEE;
import static org.erachain.core.item.assets.AssetTypes.AS_BANK_GUARANTEE_TOTAL;

public class DealsPopupMenu extends JPopupMenu {

    protected Logger logger;

    protected JComboBox<ItemCls> assetSelector;
    protected AssetCls asset;
    protected PublicKeyAccount creator;
    protected Account recipient;
    protected MTable table;

    private JMenuItem sendMail;

    private Separator ownSeparator = new Separator();
    private JLabel ownTitle = new JLabel("    " + Lang.T("Actions for OWN balance") + "  ");
    private JMenuItem sendAsset;
    private JMenuItem sendAssetBackward;

    private Separator debtSeparator = new Separator();
    private JLabel debtTitle = new JLabel("    " + Lang.T("Actions for DEBT balance") + "  ");
    private JMenuItem debtAsset;
    private JMenuItem debtAssetReturn;
    private JMenuItem debtAssetBackward;

    private Separator holdSeparator = new Separator();
    private JLabel holdTitle = new JLabel("    " + Lang.T("Actions for HOLD balance") + "  ");
    private JMenuItem holdAsset;
    private JMenuItem holdAssetBackward;

    private Separator spendSeparator = new Separator();
    private JLabel spendTitle = new JLabel("    " + Lang.T("Actions for SPEND balance") + "  ");
    private JMenuItem spendAsset;
    private JMenuItem spendAssetBackward;

    public DealsPopupMenu(MTable table, JComboBox<ItemCls> assetSelector) {

        logger = LoggerFactory.getLogger(getClass());

        this.table = table;
        this.assetSelector = assetSelector;

        sendMail = new JMenuItem(Lang.T("Send mail"));
        sendMail.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertNewTab(Lang.T("Send mail"),
                        new MailSendPanel(creator, recipient, null));
            }
        });
        this.add(sendMail);

        this.add(ownSeparator);
        this.add(ownTitle);

        sendAsset = new JMenuItem(Lang.T("Send"));
        sendAsset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // AccountAssetLendPanel
                MainPanel.getInstance().insertNewTab(sendAsset.getText() + ":" + asset.getKey(),
                        new AccountAssetSendPanel(asset, creator, recipient, null, null, asset.isReverseSend()));

            }
        });
        this.add(sendAsset);

        sendAssetBackward = new JMenuItem(Lang.T("Backward"));
        sendAssetBackward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // AccountAssetLendPanel
                MainPanel.getInstance().insertNewTab(sendAssetBackward.getText() + ":" + asset.getKey(),
                        new AccountAssetSendPanel(asset, creator, recipient, null, null, !asset.isReverseSend()));

            }
        });
        this.add(sendAssetBackward);

        this.add(debtSeparator);
        this.add(debtTitle);

        debtAsset = new JMenuItem(Lang.T("Lend"));
        debtAsset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //new AccountLendDialog(asset, pubKey);
                MainPanel.getInstance().insertNewTab(debtAsset.getText() + ":" + asset.getKey(),
                        new AccountAssetLendPanel(asset, creator, recipient, null));

            }
        });
        this.add(debtAsset);

        debtAssetReturn = new JMenuItem(Lang.T("Repay Debt"));
        debtAssetReturn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(debtAssetReturn.getText() + ":" + asset.getKey(),
                        new AccountAssetRepayDebtPanel(asset, creator, recipient, null));

            }
        });
        this.add(debtAssetReturn);

        debtAssetBackward = new JMenuItem(Lang.T("Confiscate Debt"));
        debtAssetBackward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(debtAssetBackward.getText() + ":" + asset.getKey(),
                        new AccountAssetConfiscateDebtPanel(asset, creator, recipient, null));

            }
        });
        this.add(debtAssetBackward);

        this.add(holdSeparator);
        this.add(holdTitle);

        holdAsset = new JMenuItem(Lang.T("Hold")); /// GIVE
        holdAsset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(holdAsset.getText() + ":" + asset.getKey(),
                        new AccountAssetHoldPanel(asset, creator, recipient, null, false));

            }
        });
        this.add(holdAsset);

        holdAssetBackward = new JMenuItem(Lang.T("Backward Hold")); // TAKE
        holdAssetBackward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(holdAssetBackward.getText() + ":" + asset.getKey(),
                        new AccountAssetHoldPanel(asset, creator, recipient, null, true));

            }
        });
        this.add(holdAssetBackward);

        this.add(spendSeparator);
        this.add(spendTitle);

        spendAsset = new JMenuItem(Lang.T("Spend"));
        spendAsset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(spendAsset.getText() + ":" + asset.getKey(),
                        new AccountAssetSpendPanel(asset, creator, recipient, null, null, false));

            }
        });
        this.add(spendAsset);


        spendAssetBackward = new JMenuItem(Lang.T("Backward Spend"));
        spendAssetBackward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(spendAssetBackward.getText() + ":" + asset.getKey(),
                        new AccountAssetSpendPanel(asset, creator, recipient, null, null, true));

            }
        });
        this.add(spendAssetBackward);

        this.addSeparator();
        this.add(new JLabel("    " + Lang.T("Account actions") + ":"));

        JMenuItem copyAddress = new JMenuItem(Lang.T("Copy Account"));
        copyAddress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //      int row = table.getSelectedRow();
                //      if (row < 1 ) return;

                //      row = table.convertRowIndexToModel(row);
                //      Account account = tableModel.getAccount(row);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(creator.getAddress());
                clipboard.setContents(value, null);
            }
        });
        this.add(copyAddress);

        JMenuItem depositPolzaSbp = new JMenuItem(Lang.T("Deposit by SBP"));
        depositPolzaSbp.getAccessibleContext().setAccessibleDescription(Lang.T("Deposit accounts by SBP"));
        depositPolzaSbp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    URLViewer.openWebpage(new URL("http://bitloom.ru/polza/sbp/compu?amount=500&receiver=" + creator.getAddress()));
                } catch (MalformedURLException ex1) {
                }
            }
        });
        add(depositPolzaSbp);

        JMenuItem copyBalance = new JMenuItem(Lang.T("Copy Balance"));
        copyBalance.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                //long key = cbxFavorites.getItemAt(cbxFavorites.getSelectedIndex()).getKey();
                long key = asset.getKey();
                StringSelection value = new StringSelection(creator.getBalance(key).toString());
                clipboard.setContents(value, null);
            }
        });

        this.add(copyBalance);

        JMenuItem copyPublicKey = new JMenuItem(Lang.T("Copy Public Key"));
        copyPublicKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(creator.getBase58());
                clipboard.setContents(value, null);
            }
        });
        this.add(copyPublicKey);

        JMenuItem copyBankKey = new JMenuItem(Lang.T("Copy Public Key for BANK"));
        copyBankKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String bankKeyAccount = "+" + Base32.encode(creator.getPublicKey());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(bankKeyAccount);
                clipboard.setContents(value, null);
            }
        });
        this.add(copyBankKey);

        JMenuItem set_name = new JMenuItem(Lang.T("Edit name"));
        set_name.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AccountSetNameDialog(creator.getAddress());
                table.repaint();
            }
        });
        this.add(set_name);

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.T("Check in Blockexplorer"));
        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    URLViewer.openWebpage(new URL(Settings.getInstance().getBlockexplorerURL()
                            + "/index/blockexplorer.html"
                            + "?address=" + creator.getAddress()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });
        add(setSeeInBlockexplorer);

        this.addSeparator();
        this.add(new JLabel("    " + Lang.T("Make") + ":"));

        JMenuItem issueNote = new JMenuItem(Lang.T("Issue Document"));
        issueNote.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertNewTab(Lang.T("Issue Document"),
                        new IssueDocumentPanel(creator, null));

            }
        });
        this.add(issueNote);

        JMenuItem accruals = new JMenuItem(Lang.T("Make Accruals"));
        accruals.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IssueDocumentPanel panel = new IssueDocumentPanel(creator, asset);
                panel.selectAccruals(null, null);
                MainPanel.getInstance().insertNewTab(Lang.T("Make Accruals"), panel);
            }
        });
        add(accruals);

        JMenuItem dividend = new JMenuItem(Lang.T("Pay Dividend"));
        dividend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IssueDocumentPanel panel = new IssueDocumentPanel(asset.getMaker(), null);
                panel.selectAccruals(null, asset);
                MainPanel.getInstance().insertNewTab(Lang.T("Pay Dividend"), panel);
            }
        });
        add(dividend);


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

                creator = ((SendableModel) table.getModel()).getCreator(row);
                recipient = ((SendableModel) table.getModel()).getRecipent(row);
                init();

            }


        });

    }

    public void init() {

        boolean isCreatorMaker = asset != null && creator.equals(asset.getMaker());
        boolean isSelfManaged = asset.isSelfManaged();
        boolean isUnlimited = isSelfManaged || asset.isUnlimited(creator, false);

        this.sendAsset.setEnabled(true);
        this.sendAssetBackward.setEnabled(true);
        this.debtAsset.setEnabled(true);
        this.debtAssetBackward.setEnabled(true);
        this.debtAssetReturn.setEnabled(true);
        this.holdAsset.setEnabled(true);
        this.holdAssetBackward.setEnabled(true);
        this.spendAsset.setEnabled(true);
        this.spendAssetBackward.setEnabled(true);

        /// MAIL
        this.sendMail.setText(Lang.T("Send Mail"));

        String actionName;

        /// SET MENU BY ACTION

        /// **** SEND
        actionName = asset.viewAssetTypeAction(asset.isReverseSend(), TransactionAmount.ACTION_SEND, isCreatorMaker);
        if (actionName == null) {
            this.sendAsset.setVisible(false);
        } else {
            this.sendAsset.setText(Lang.T(actionName));
            this.sendAsset.setVisible(true);
        }

        actionName = asset.viewAssetTypeAction(!asset.isReverseSend(), TransactionAmount.ACTION_SEND, isCreatorMaker);
        if (actionName == null) {
            this.sendAssetBackward.setVisible(false);
        } else {
            this.sendAssetBackward.setText(Lang.T(actionName));
            this.sendAssetBackward.setVisible(true);
        }

        /// **** DEBT
        actionName = asset.viewAssetTypeAction(false, TransactionAmount.ACTION_DEBT, isCreatorMaker);
        if (actionName == null) {
            this.debtAsset.setVisible(false);
        } else {
            this.debtAsset.setText(Lang.T(actionName));
            this.debtAsset.setVisible(true);
        }

        actionName = asset.viewAssetTypeAction(false, TransactionAmount.ACTION_REPAY_DEBT, isCreatorMaker);
        if (actionName == null) {
            this.debtAssetReturn.setVisible(false);
        } else {
            this.debtAssetReturn.setText(Lang.T(actionName));
            this.debtAssetReturn.setVisible(true);
        }

        actionName = asset.viewAssetTypeAction(true, TransactionAmount.ACTION_DEBT, isCreatorMaker);
        if (actionName == null) {
            this.debtAssetBackward.setVisible(false);
        } else {
            this.debtAssetBackward.setText(Lang.T(actionName));
            this.debtAssetBackward.setVisible(true);
        }

        //// **** HOLD
        actionName = asset.viewAssetTypeAction(false, TransactionAmount.ACTION_HOLD, isCreatorMaker);
        if (actionName == null) {
            this.holdAsset.setVisible(false);
        } else {
            this.holdAsset.setText(Lang.T(actionName));
            this.holdAsset.setVisible(true);
        }

        actionName = asset.viewAssetTypeAction(true, TransactionAmount.ACTION_HOLD, isCreatorMaker);
        if (actionName == null) {
            this.holdAssetBackward.setVisible(false);
        } else {
            this.holdAssetBackward.setText(Lang.T(actionName));
            this.holdAssetBackward.setVisible(true);
        }

        //// **** SPEND
        actionName = asset.viewAssetTypeAction(false, TransactionAmount.ACTION_SPEND, isCreatorMaker);
        if (actionName == null) {
            this.spendAsset.setVisible(false);
        } else {
            this.spendAsset.setText(Lang.T(actionName));
            this.spendAsset.setVisible(true);
        }

        actionName = asset.viewAssetTypeAction(true, TransactionAmount.ACTION_SPEND, isCreatorMaker);
        if (actionName == null) {
            this.spendAssetBackward.setVisible(false);
        } else {
            this.spendAssetBackward.setText(Lang.T(actionName));
            this.spendAssetBackward.setVisible(true);
        }

        //// SET ENABLE by BALANCES
        Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>
                balance = creator.getBalance(asset.getKey());

        if (isUnlimited || balance.a.b.signum() > 0) {
            this.sendAsset.setEnabled(true);
            this.debtAsset.setEnabled(true);
        } else {
            this.sendAsset.setEnabled(false);
            this.debtAsset.setEnabled(false);
        }

        if (isUnlimited || balance.b.b.signum() > 0) {
            this.debtAssetReturn.setEnabled(true);
            this.debtAssetBackward.setEnabled(false);
        } else if (balance.b.b.signum() == 0) {
            this.debtAssetReturn.setEnabled(false);
            this.debtAssetBackward.setEnabled(false);
        } else {
            this.debtAssetReturn.setEnabled(false);
            this.debtAssetBackward.setEnabled(true);
        }

        // SET by COMMON ASSET TYPE

        // ALL OUTSIDE ASSETS
        if (asset.isOutsideType()) {

            this.debtAssetReturn.setVisible(false);

            if (creator.equals(asset.getMaker())) {
                this.holdAsset.setEnabled(false);
                this.debtAsset.setEnabled(false);
                this.debtAssetBackward.setEnabled(false);
                this.spendAsset.setEnabled(false);
            } else {
                if (balance.a.b.signum() <= 0) {
                    this.sendAsset.setEnabled(false);
                    this.debtAsset.setEnabled(false);
                    this.debtAssetBackward.setEnabled(false);
                    this.holdAsset.setEnabled(false);
                    this.spendAsset.setEnabled(false);
                } else {
                    this.sendAsset.setEnabled(true);
                    this.debtAsset.setEnabled(true);
                    this.debtAssetBackward.setEnabled(balance.b.b.signum() != 0);
                    this.spendAsset.setEnabled(true);
                    if (balance.a.b.add(balance.b.b).signum() <= 0) {
                        this.debtAsset.setEnabled(false);
                        this.sendAsset.setEnabled(false);
                    }
                }
            }
        } else if (isSelfManaged) {
            this.debtAssetReturn.setVisible(false);

            this.sendAsset.setEnabled(isCreatorMaker);
            this.debtAsset.setEnabled(isCreatorMaker);
            this.holdAsset.setEnabled(isCreatorMaker);
            this.spendAsset.setEnabled(isCreatorMaker);

        }

        // SET by this ASSET TYPE etc
        switch (this.asset.getAssetType()) {

            case AS_BANK_GUARANTEE:

                balance = creator.getBalance(asset.getKey());
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

            case AS_BANK_GUARANTEE_TOTAL:

                balance = creator.getBalance(asset.getKey());
                if (creator.equals(asset.getMaker()) || balance.a.b.signum() > 0) {

                } else {
                    this.sendAsset.setEnabled(false);
                    this.debtAsset.setEnabled(false);
                    this.debtAssetReturn.setEnabled(false);
                }

                break;

        }

        ownSeparator.setVisible(sendAsset.isVisible() || sendAssetBackward.isVisible());
        ownTitle.setVisible(sendAsset.isVisible() || sendAssetBackward.isVisible());

        debtSeparator.setVisible(debtAsset.isVisible() || debtAssetBackward.isVisible() || debtAssetReturn.isVisible());
        debtTitle.setVisible(debtAsset.isVisible() || debtAssetBackward.isVisible() || debtAssetReturn.isVisible());

        holdSeparator.setVisible(holdAsset.isVisible() || holdAssetBackward.isVisible());
        holdTitle.setVisible(holdAsset.isVisible() || holdAssetBackward.isVisible());

        spendSeparator.setVisible(spendAsset.isVisible() || spendAssetBackward.isVisible());
        spendTitle.setVisible(spendAsset.isVisible() || spendAssetBackward.isVisible());

    }
}