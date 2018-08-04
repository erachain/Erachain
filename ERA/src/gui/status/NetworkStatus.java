package gui.status;
// 30/03

import controller.Controller;
import core.BlockChain;
import datachain.DCSet;
import lang.Lang;
import network.Peer;
import org.mapdb.Fun.Tuple3;
import utils.GUIUtils;
import utils.ObserverMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class NetworkStatus extends JLabel implements Observer {
    private ImageIcon noConnectionsIcon;
    private ImageIcon synchronizingIcon;
    private ImageIcon walletSynchronizingIcon;
    private ImageIcon okeIcon;
    private int currentHeight = 1;

    public NetworkStatus() {
        super();

        //CREATE ICONS
        this.noConnectionsIcon = this.createIcon(Color.RED);
        this.synchronizingIcon = this.createIcon(Color.ORANGE);
        this.walletSynchronizingIcon = this.createIcon(Color.MAGENTA);
        this.okeIcon = this.createIcon(Color.GREEN);

        ToolTipManager.sharedInstance().setDismissDelay((int) TimeUnit.SECONDS.toMillis(5));

        this.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent mEvt) {
                String mess = Lang.getInstance().translate("Network Port") + ": " + BlockChain.getNetworkPort()
                        + ", " + Lang.getInstance().translate("target")
                        + ": " + Controller.getInstance().getBlockChain().getTarget(DCSet.getInstance()) + ", ";

                if (Controller.getInstance().getStatus() == Controller.STATUS_OK || Controller.getInstance().getStatus() == Controller.STATUS_NO_CONNECTIONS) {
                    mess += Lang.getInstance().translate("Block height") + ": " + Controller.getInstance().getBlockChain().getHWeightFull(DCSet.getInstance()).a;
                } else if (Controller.getInstance().getWalletSyncHeight() > 0) {
                    mess += Lang.getInstance().translate("Block height") + ": " + currentHeight + "/" + Controller.getInstance().getBlockChain().getHWeightFull(DCSet.getInstance()).a + "/" + Controller.getInstance().getMaxPeerHWeight(0);
                } else {
                    mess += Lang.getInstance().translate("Block height") + ": " + currentHeight + "/" + Controller.getInstance().getMaxPeerHWeight(0).a;
                }
                setToolTipText(mess);
            }
        });
        //LISTEN ON STATUS
        Controller.getInstance().addObserver(this);
        //Controller.getInstance().addWalletListener(this);
    }

    private ImageIcon createIcon(Color color) {
        return GUIUtils.createIcon(color, this.getBackground());
    }

    private void viewProgress() {
        currentHeight = Controller.getInstance().getMyHeight();
        Tuple3<Integer, Long, Peer> heightW = Controller.getInstance().getMaxPeerHWeight(0);
        if (heightW == null)
            return;

        int height = heightW.a;

        if (Controller.getInstance().getStatus() == Controller.STATUS_SYNCHRONIZING) {
            this.setText(Lang.getInstance().translate("Synchronizing") + " " + 100 * currentHeight / height + "%");
        }

    }

    @Override
    public void update(Observable arg0, Object arg1) {
        ObserverMessage message = (ObserverMessage) arg1;
        int type = message.getType();

        if (type == ObserverMessage.WALLET_SYNC_STATUS) {
            currentHeight = (int) message.getValue();
            int height = DCSet.getInstance().getBlockMap().size();
            if (currentHeight == 0 || currentHeight == height) {
                this.update(null, new ObserverMessage(
                        ObserverMessage.NETWORK_STATUS, Controller.getInstance().getStatus()));
                //currentHeight = Controller.getInstance().getMyHWeight(false).a;
                return;
            }

            this.setIcon(walletSynchronizingIcon);
            this.setText(Lang.getInstance().translate("Wallet Synchronizing") + " " + 100 * currentHeight / height + "%");

        } else if (type == ObserverMessage.BLOCKCHAIN_SYNC_STATUS) {
            viewProgress();

        } else if (type == ObserverMessage.NETWORK_STATUS) {
            int status = (int) message.getValue();

            if (status == Controller.STATUS_NO_CONNECTIONS) {
                this.setIcon(noConnectionsIcon);
                this.setText(Lang.getInstance().translate("No connections"));
            }
            if (status == Controller.STATUS_SYNCHRONIZING) {
                this.setIcon(synchronizingIcon);
                //this.setText(Lang.getInstance().translate("Synchronizing"));
                viewProgress();
            }
            if (status == Controller.STATUS_OK) {
                this.setIcon(okeIcon);
                this.setText(Lang.getInstance().translate("OK"));
            }
        }
    }
}
