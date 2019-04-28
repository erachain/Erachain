package org.erachain.gui.library;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.accounts.*;
import org.erachain.gui.items.assets.ExchangeFrame;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuExchange extends JMenu {

    public MenuExchange() {

        // DEALS
        // Send
        JMenuItem deposit = new JMenuItem(Lang.getInstance().translate("Deposit"));
        deposit.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Deposit funds to Exchange"));
        deposit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                new ExchangeFrame((AssetCls) DCSet.getInstance().getItemAssetMap().get((long) 2), null, "Buy", null);
            }
        });
        add(deposit);

        JMenuItem trade = new JMenuItem(Lang.getInstance().translate("Trade"));
        trade.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Trade on Exchange"));
        trade.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                new ExchangeFrame((AssetCls) DCSet.getInstance().getItemAssetMap().get((long) 2), null, "Buy", null);
            }
        });
        add(trade);

        JMenuItem withdraw = new JMenuItem(Lang.getInstance().translate("Withdraw"));
        withdraw.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Withdraw funds from Exchange"));
        withdraw.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                new ExchangeFrame((AssetCls) DCSet.getInstance().getItemAssetMap().get((long) 2), null, "Buy", null);
            }
        });
        add(withdraw);
    }
}
