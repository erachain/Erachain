package org.erachain.gui.status;

import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@SuppressWarnings("serial")
public class StatusPanel extends JPanel {
    private StatusPanel th;

    public StatusPanel() {
        super();
        th = this;

        this.add(new NetworkStatus(), BorderLayout.EAST);

        WalletStatus walletStatus = new WalletStatus();
        walletStatus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        walletStatus.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    PasswordPane.switchLockDialog(MainFrame.getInstance());
                }
            }
        });

        this.add(walletStatus, BorderLayout.EAST);
        this.add(new ForgingStatus(), BorderLayout.EAST);
        this.add(new UnconfirmTransactionStatus(), BorderLayout.EAST);
        this.add(new ErachainStatus(), BorderLayout.EAST);


    }
}

