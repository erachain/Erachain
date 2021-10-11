package org.erachain.gui.library;

import org.erachain.controller.Controller;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.accounts.*;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.records.VouchTransactionDialog;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuDeals extends JMenu {

    public MenuDeals() {

        // DEALS

        // MAIL
        JMenuItem dealsMenuMail = new JMenuItem(Lang.T("Send mail"));
        dealsMenuMail.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.T("Send mail"),
                        new MailSendPanel(null, null, null));

            }
        });
        add(dealsMenuMail);

        addSeparator();

        // Send
        JMenuItem dealsMenuSendMessage = new JMenuItem(Lang.T("Send"));
        dealsMenuSendMessage.getAccessibleContext().setAccessibleDescription(Lang.T("Send Asset and Message"));
        dealsMenuSendMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                MainPanel.getInstance().insertNewTab(Lang.T("Send"),
                        new AccountAssetSendPanel(null, null,
                                null, null, null, null, false));

            }
        });
        add(dealsMenuSendMessage);

        addSeparator();

        // to lend

        JMenuItem dealsMenuLend = new JMenuItem(Lang.T("Lend"));
        dealsMenuLend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.T("Lend"),
                        new AccountAssetLendPanel(null, null, null, null));

            }
        });
        add(dealsMenuLend);

        // Confiscate_Debt

        JMenuItem dealsMenu_Confiscate_Debt = new JMenuItem(Lang.T("Confiscate Debt"));
        dealsMenu_Confiscate_Debt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.T("Confiscate Debt"),
                        new AccountAssetConfiscateDebtPanel(null, null, null, null));

            }
        });
        add(dealsMenu_Confiscate_Debt);

        // Repay_Debt

        JMenuItem dealsMenu_Repay_Debt = new JMenuItem(Lang.T("Repay Debt"));
        dealsMenu_Repay_Debt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.T("Repay Debt"),
                        new AccountAssetRepayDebtPanel(null, null, null, null));

            }
        });
        add(dealsMenu_Repay_Debt);

        addSeparator();

        // Take on HOLD

        JMenuItem dealsMenu_Take_On_Hold = new JMenuItem(Lang.T("Take on Hold"));
        dealsMenu_Take_On_Hold.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.T("Take on Hold"),
                        new AccountAssetHoldPanel(null, null, null, null, true));
            }
        });
        add(dealsMenu_Take_On_Hold);

        addSeparator();

        // Spend

        JMenuItem dealsMenu_Spend = new JMenuItem(Lang.T("Spend"));
        dealsMenu_Spend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.T("Spend"),
                        new AccountAssetSpendPanel(null,
                                null, null, null, null, false));

            }
        });
        add(dealsMenu_Spend);

        addSeparator();

        //vouch
        JMenuItem dealsMenuVouchRecord = new JMenuItem(Lang.T("Sign / Vouch"));
        dealsMenuVouchRecord.getAccessibleContext().setAccessibleDescription(Lang.T("Vouching record"));
        dealsMenuVouchRecord.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new VouchTransactionDialog(null, null);
            }
        });
        add(dealsMenuVouchRecord);


        JMenuItem dealsMenu_Open_Wallet = new JMenuItem(Lang.T("Open Wallet"));
        dealsMenu_Open_Wallet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                int res = Controller.getInstance().loadWalletFromDir();
                if (res == 0) {
                    JOptionPane.showMessageDialog(
                            new JFrame(), Lang.T("wallet does not exist") + "!",
                            "Error!",
                            JOptionPane.ERROR_MESSAGE);

                } else {
                    Controller.getInstance().forgingStatusChanged(Controller.getInstance().getForgingStatus());
                    MainFrame.getInstance().mainPanel.jTabbedPane1.removeAll();
                }
            }
        });

        add(dealsMenu_Open_Wallet);

        if (false) {
            JMenuItem FindHashFromDir = new JMenuItem(Lang.T("Find Hash from DIR"));
            FindHashFromDir.getAccessibleContext().setAccessibleDescription(Lang.T("Find Hash from DIR"));
            FindHashFromDir.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //
                    //selectOrAdd(new VouchTransactionDialog(), MainFrame.desktopPane.getAllFrames());
                    new FindHashFrmDirDialog();
                }
            });
            add(FindHashFromDir);
        }

    }

}
