package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.database.PeerMap.PeerInfo;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.network.Peer;
import org.erachain.settings.Settings;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class KnownPeersTableModel extends AbstractTableModel implements Observer {

    private static final int COLUMN_ADDRESS = 0;
    private static final int COLUMN_HEIGHT = 1;
    private static final int COLUMN_PINGMC = 2;
    private static final int COLUMN_REILABLE = 3;
    private static final int COLUMN_INITIATOR = 4;
    private static final int COLUMN_FINDING_AGO = 5;
    private static final int COLUMN_ONLINE_TIME = 6;
    private static final int COLUMN_VERSION = 7;
    static Logger logger = LoggerFactory.getLogger(KnownPeersTableModel.class);
    String[] columnNames = Lang.getInstance().translate(new String[]{"IP", "Height",
            "Ping mc", "Reliable", "Initiator", "Finding ago", "Online Time", "Version"});
    private List<Peer> peers;
    private List<Boolean> peersStatus = new ArrayList<Boolean>();

    public KnownPeersTableModel() {
        peers = Settings.getInstance().getKnownPeers();
        for (int i = 0; i < peers.size(); i++) {
            peersStatus.add(false);
        }
        Controller.getInstance().addActivePeersObserver(this);
    }

    public List<String> getPeers() {
        List<String> result = new ArrayList<String>();
        for (Peer peer : peers) {
            result.add(peer.getAddress().getHostAddress());
        }
        return result;
    }

    public void addAddress(String address) {
        try {
            Peer peer = new Peer(InetAddress.getByName(address));
            peers.add(peer);
            peersStatus.add(false);
            fireTableDataChanged();
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void deleteAddress(int row) {
        String address = getValueAt(row, 0).toString();
        int n = JOptionPane.showConfirmDialog(
                new JFrame(), Lang.getInstance().translate("Do you want to remove address %address%?").replace("%address%", address),
                Lang.getInstance().translate("Confirmation"),
                JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            peers.remove(row);
            peersStatus.remove(row);
            fireTableDataChanged();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int index) {
        return columnNames[index];
    }

    @Override
    public int getRowCount() {
        if (peers == null) {
            return 0;
        }
        return peers.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (peers == null || peers.size() - 1 < row) {
            return null;
        }

        Peer peer = peers.get(row);

        if (peer == null || DCSet.getInstance().isStoped())  {
            return null;
        }

        PeerInfo peerInfo = Controller.getInstance().getDLSet().getPeerMap().getInfo(peer.getAddress());
        if (peerInfo == null){
            return null;
        }

        switch (column) {
            case COLUMN_ADDRESS:
                return peer.getAddress().getHostAddress();

            case COLUMN_HEIGHT:
                Tuple2<Integer, Long> res = peer.getHWeight();
                if (res == null) {
                    if (peer.isUsed()) {
                        return Lang.getInstance().translate("Waiting...");
                    } else {
                        int banMinutes = Controller.getInstance().getDLSet().getPeerMap().getBanMinutes(peer);
                        if (banMinutes > 0) {
                            return "(" + peer.getBanMessage() + ")";
                        } else {
                            return Lang.getInstance().translate("Broken");
                        }
                    }
                } else {
                    return res.a.toString() + " " + res.b.toString();
                }

            case COLUMN_PINGMC:
                if (!peer.isUsed()) {
                    int banMinutes = Controller.getInstance().getDLSet().getPeerMap().getBanMinutes(peer);
                    if (banMinutes > 0) {
                        return Lang.getInstance().translate("Banned") + " " + banMinutes + "m";
                    } else {
                        return Lang.getInstance().translate("Broken");
                    }
                } else if (peer.getPing() > 1000000) {
                    return Lang.getInstance().translate("Waiting...");
                } else {
                    return peer.getPing();
                }

            case COLUMN_REILABLE:
                return peerInfo.getWhitePingCouner();

            case COLUMN_INITIATOR:
                if (peer.isWhite()) {
                    return Lang.getInstance().translate("You");
                } else {
                    return Lang.getInstance().translate("Remote");
                }

            case COLUMN_FINDING_AGO:
                return DateTimeFormat.timeAgo(peerInfo.getFindingTime());

            case COLUMN_ONLINE_TIME:
                return DateTimeFormat.timeAgo(peer.getConnectionTime());

            case COLUMN_VERSION:
                return peer.getBuildTime() > 0 ? peer.getVersion() + " " + DateTimeFormat.timestamptoString(peer.getBuildTime(), "yyyy-MM-dd", "UTC")
                        : "";
        }
        return null;

    }


    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        if (message.getType() == ObserverMessage.LIST_PEER_TYPE) {
            //(JTable)this.get
            //component.setRowSelectionInterval(row, row);

            List<Peer> peersBuff = (List<Peer>) message.getValue();
            int n = 0;

            for (Peer peer1 : this.peers) {
                boolean connected = false;

                for (Peer peer2 : peersBuff) {
                    if (peer1.getAddress().toString().equals(peer2.getAddress().toString())) {
                        this.peersStatus.set(n, true);
                        connected = true;
                    }
                }

                if (!connected) {
                    this.peersStatus.set(n, false);
                    this.fireTableRowsUpdated(n, n);
                }

                n++;
            }
        } else if (message.getType() == ObserverMessage.UPDATE_PEER_TYPE) {
            Peer peer1 = (Peer) message.getValue();
            int n = 0;
            for (Peer peer2 : this.peers) {
                if (peer2.equals(peer1)) {
                    break;
                }
                n++;
            }
            this.fireTableRowsUpdated(n, n);

        } else if (message.getType() == ObserverMessage.ADD_PEER_TYPE) {
            //this.peers.add((Peer) message.getValue());
            this.fireTableDataChanged();
        } else if (message.getType() == ObserverMessage.REMOVE_PEER_TYPE) {
            //this.peers.remove((Peer) message.getValue());
            this.fireTableDataChanged();
        }

    }

    public void removeObservers() {
        Controller.getInstance().removeActivePeersObserver(this);

    }
}
