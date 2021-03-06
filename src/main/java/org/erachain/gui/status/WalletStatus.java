package org.erachain.gui.status;

import org.erachain.controller.Controller;
import org.erachain.core.wallet.Wallet;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletStatus extends JLabel implements Observer {
    static Logger LOGGER = LoggerFactory.getLogger(WalletStatus.class);

    private ImageIcon unlockedIcon;
    private ImageIcon lockedIcon;

    public WalletStatus() {
        super();

        try {
            //LOAD IMAGES
            BufferedImage unlockedImage = ImageIO.read(new File("images/wallet/unlocked.png"));
            this.unlockedIcon = new ImageIcon(unlockedImage.getScaledInstance(20, 16, Image.SCALE_SMOOTH));

            BufferedImage lockedImage = ImageIO.read(new File("images/wallet/locked.png"));
            this.lockedIcon = new ImageIcon(lockedImage.getScaledInstance(20, 16, Image.SCALE_SMOOTH));

            //LISTEN ON WALLET
            Controller.getInstance().addWalletObserver(this);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        ObserverMessage message = (ObserverMessage) arg1;

        if (message.getType() == ObserverMessage.WALLET_STATUS) {
            int status = (int) message.getValue();

            if (status == Wallet.STATUS_UNLOCKED) {
                this.setIcon(this.unlockedIcon);
                this.setText(Lang.T("Wallet is unlocked"));
            } else {
                this.setIcon(this.lockedIcon);
                this.setText(Lang.T("Wallet is locked"));
            }
        }
    }
}
