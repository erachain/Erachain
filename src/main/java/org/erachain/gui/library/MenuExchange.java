package org.erachain.gui.library;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.accounts.*;
import org.erachain.gui.items.assets.ExchangeFrame;
import org.erachain.gui.items.assets.Exchange_Panel;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuExchange extends JMenu {

    public MenuExchange() {

        /// DEPOSIT
        JMenuItem deposit = new JMenuItem(Lang.getInstance().translate("Deposit"));
        deposit.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Deposit funds to Exchange"));
        deposit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                MainPanel.getInstance().addTab(Exchange_Panel.class.getName());
            }
        });
        add(deposit);

        // TRADE
        JMenuItem trade = new JMenuItem(Lang.getInstance().translate("Trade"));
        trade.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Trade on Exchange"));
        trade.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                MainPanel.getInstance().addTab(Exchange_Panel.class.getSimpleName());
            }
        });
        add(trade);

        // WITHDRAW
        JMenuItem withdraw = new JMenuItem(Lang.getInstance().translate("Withdraw"));
        withdraw.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Withdraw funds from Exchange"));
        withdraw.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                MainPanel.getInstance().addTab(Exchange_Panel.class.getName());
            }
        });
        add(withdraw);
    }
}
