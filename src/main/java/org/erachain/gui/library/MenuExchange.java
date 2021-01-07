package org.erachain.gui.library;

import org.erachain.gui.items.assets.DepositExchange;
import org.erachain.gui.items.assets.ExchangePanel;
import org.erachain.gui.items.assets.MyOrderTab;
import org.erachain.gui.items.assets.WithdrawExchange;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuExchange extends JMenu {

    public MenuExchange() {

        if (Settings.EXCHANGE_IN_OUT) {
            /// DEPOSIT
            JMenuItem deposit = new JMenuItem(Lang.T("Deposit or Buy"));
            deposit.getAccessibleContext().setAccessibleDescription(Lang.T("Deposit funds to Exchange"));
            deposit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //
                    MainPanel.getInstance().addTab(DepositExchange.class.getSimpleName());
                }
            });
            add(deposit);

            addSeparator();
        }

        // TRADE
        JMenuItem trade = new JMenuItem(Lang.T("Trade on DEX"));
        trade.getAccessibleContext().setAccessibleDescription(Lang.T("Trade on Exchange"));
        trade.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                MainPanel.getInstance().addTab(ExchangePanel.class.getSimpleName());
            }
        });
        add(trade);

        // TRADE
        JMenuItem orders = new JMenuItem(Lang.T("My Orders"));
        orders.getAccessibleContext().setAccessibleDescription(Lang.T("See My Orders"));
        orders.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                MainPanel.getInstance().addTab(MyOrderTab.class.getSimpleName());
            }
        });
        add(orders);

        if (Settings.EXCHANGE_IN_OUT) {
            addSeparator();

            // WITHDRAW
            JMenuItem withdraw = new JMenuItem(Lang.T("Withdraw or Sell"));
            withdraw.getAccessibleContext().setAccessibleDescription(Lang.T("Withdraw funds from Exchange"));
            withdraw.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //
                    MainPanel.getInstance().addTab(WithdrawExchange.class.getSimpleName());
                }
            });
            add(withdraw);
        }
    }
}
