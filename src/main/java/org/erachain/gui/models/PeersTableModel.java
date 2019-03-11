package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.database.PeerMap.PeerInfo;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.network.Peer;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.util.*;

@SuppressWarnings("serial")
public class PeersTableModel extends AbstractTableModel implements Observer {

    private static final int COLUMN_ADDRESS = 0;
    private static final int COLUMN_HEIGHT = 1;
    private static final int COLUMN_PINGMC = 2;
    private static final int COLUMN_REILABLE = 3;
    private static final int COLUMN_INITIATOR = 4;
    private static final int COLUMN_FINDING_AGO = 5;
    private static final int COLUMN_ONLINE_TIME = 6;
    private static final int COLUMN_VERSION = 7;
    String[] columnNames = new String[] { "IP", "Height", "Ping mc", "Reliable", "Initiator", "Finding ago",
            "Online Time", "Version" };
    private Timer timer;
    private List<Peer> peers;
    List<Peer> peersView = new ArrayList<Peer>();
    int view = 1;
    // String[] columnNames = Lang.getInstance().translate(new String[]{"IP",
    // "Height", "Ping mc", "Reliable", "Initiator", "Finding ago", "Online
    // Time", "Version"});
    private Boolean[] column_AutuHeight = new Boolean[] { false, false, false, false, false, false, false, false };

    public PeersTableModel() {
        Controller.getInstance().addActivePeersObserver(this);

        if (this.timer == null) {
            this.timer = new Timer("Peers Table");

            TimerTask action = new TimerTask() {
                public void run() {
                    try {
                        fireTableDataChanged();
                    } catch (Exception e) {
                        // LOGGER.error(e.getMessage(),e);
                    }
                }
            };

            this.timer.schedule(action,
                    // Settings.getInstance().getPingInterval()>>1,
                    5000,
                    // Settings.getInstance().getPingInterval()
                    10000);
        }
    }

    // sort to Reliable
    // sort = 0 As
    // sort = 1 des
    public void setSortReliable(int sort) {
        peersView.sort(new Comparator<Peer>() {
            @Override
            public int compare(Peer o1, Peer o2) {
                // TODO Auto-generated method stub
                int ret = 0;
                PeerInfo peerInfo1 = Controller.getInstance().getDBSet().getPeerMap().getInfo(o1.getAddress());
                PeerInfo peerInfo2 = Controller.getInstance().getDBSet().getPeerMap().getInfo(o2.getAddress());
                if (sort == 0)
                    ret =peerInfo1.getWhitePingCouner() > peerInfo2.getWhitePingCouner()? 1: -1;
                if (sort == 1)
                    ret =peerInfo1.getWhitePingCouner() < peerInfo2.getWhitePingCouner()? 1: -1;
           //     fireTableDataChanged();
                return ret;
            }
        });
    }

    // sort to Ping
    // // sort = 0 As
    // sort = 1 des
    public void setSortPing(int sort) {
        peersView.sort(new Comparator<Peer>() {
            @Override
            public int compare(Peer o1, Peer o2) {
                // TODO Auto-generated method stub
                int ret = 0;
                if (sort == 0)
                    ret = o1.getPing() > o2.getPing()? 1: -1;
                if (sort == 1)
                    ret = o1.getPing() < o2.getPing()? 1: -1;
          //      fireTableDataChanged();
                return ret;
            }
        });
    }
    
    // view peer
    // view = 0 only Active Peers
    // view ==1 all
    public void setView(int view) {
        this.view = view;
        peersView.clear();
        if (view != 0) {
            peersView.addAll(peers);
        } else {
            for (Peer peer : peers) {
                if (view == 0) {
                    if (peer.isUsed())
                        peersView.add(peer);
                }
            }
        }
      
    }

    public Class<? extends Object> getColumnClass(int c) { // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    public Boolean[] get_Column_AutoHeight() {

        return this.column_AutuHeight;
    }

    public void set_get_Column_AutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }

    public Peer get_Peers(int row) {
        if (row < 0)
            return null;
        return peers.get(row);
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int index) {
        return Lang.getInstance().translate(columnNames[index]);
    }

    public String getColumnNameNO_Translate(int index) {
        return columnNames[index];
    }

    @Override
    public int getRowCount() {
        if (peersView == null) {
            return 0;
        }

        return peersView.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (peersView == null || this.peersView.size() - 1 < row) {
            return null;
        }

        if (Controller.getInstance().isOnStopping()) {
            this.timer.cancel();
            return null;
        }

        Peer peer = peersView.get(row);

        if (peer == null || DCSet.getInstance().isStoped())
            return null;

        PeerInfo peerInfo = Controller.getInstance().getDBSet().getPeerMap().getInfo(peer.getAddress());
        if (peerInfo == null)
            return null;

        switch (column) {
        case COLUMN_ADDRESS:
            return peer.getAddress().getHostAddress();

        case COLUMN_HEIGHT:
            if (!peer.isUsed()) {
                int banMinutes = Controller.getInstance().getDBSet().getPeerMap().getBanMinutes(peer);
                if (banMinutes > 0) {
                    return Lang.getInstance().translate("Banned") + " " + banMinutes + "m";
                } else {
                    return Lang.getInstance().translate("Broken");
                }
            }
            Tuple2<Integer, Long> res = Controller.getInstance().getHWeightOfPeer(peer);
            if (res == null) {
                return Lang.getInstance().translate("Waiting...");
            } else {
                return res.a.toString() + " " + res.b.toString();
            }

        case COLUMN_PINGMC:
            if (!peer.isUsed()) {
                return Lang.getInstance().translate("Broken");
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
            return Controller.getInstance().getVersionOfPeer(peer).getA();

        }

        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            // GUI ERROR
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        if (Controller.getInstance().isOnStopping()) {
            this.timer.cancel();
            return;
        }

        if (message.getType() == ObserverMessage.LIST_PEER_TYPE) {

            this.peers = (List<Peer>) message.getValue();
            setView(view);
            this.fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.UPDATE_PEER_TYPE) {
            Peer peer1 = (Peer) message.getValue();
            int n = 0;
            for (Peer peer2 : this.peers) {
                if (Arrays.equals(peer1.getAddress().getAddress(), peer2.getAddress().getAddress())) {
                    /// this.peersStatus.set(n, true);
                    break;
                }
                n++;
            }
            setView(view);
            this.fireTableRowsUpdated(n, n);

        } else if (message.getType() == ObserverMessage.ADD_PEER_TYPE) {
            setView(view);
            this.fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.REMOVE_PEER_TYPE) {
            setView(view);
            this.fireTableDataChanged();
        }
    }

    public void deleteObserver() {
        Controller.getInstance().removeActivePeersObserver(this);
        if (this.timer != null){
            this.timer.cancel();
            this.timer = null;
        }

    }

}
