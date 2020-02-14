package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.database.PeerMap.PeerInfo;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.network.Peer;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import java.util.*;

/**
 * TODO тут нужно применить SortableList для сортировки по полям?
 * Или тут более изящно сортировка сделана?
 */
@SuppressWarnings("serial")
public class PeersTableModel extends TimerTableModelCls<Peer> implements Observer {

    private static final int COLUMN_ADDRESS = 0;
    private static final int COLUMN_HEIGHT = 1;
    private static final int COLUMN_PINGMC = 2;
    private static final int COLUMN_REILABLE = 3;
    private static final int COLUMN_INITIATOR = 4;
    private static final int COLUMN_FINDING_AGO = 5;
    private static final int COLUMN_ONLINE_TIME = 6;
    private static final int COLUMN_VERSION = 7;

    /**
     * для сортировки по полям в особом виде
     */
    private List<Peer> peersView = new ArrayList<Peer>();
    private int view = 1;

    public PeersTableModel() {
        super(new String[]{"IP", "Height", "Ping mc", "Reliable", "Initiator", "Finding ago",
                        "Online Time", "Version"},
                null, false);

        addObservers();

    }

    // sort to Reliable
    // sort = 0 As
    // sort = 1 des
    public void setSortReliable(int sort) {
        peersView.sort(new Comparator<Peer>() {
            @Override
            public int compare(Peer o1, Peer o2) {
                int ret = 0;
                PeerInfo peerInfo1 = Controller.getInstance().getDLSet().getPeerMap().getInfo(o1.getAddress());
                PeerInfo peerInfo2 = Controller.getInstance().getDLSet().getPeerMap().getInfo(o2.getAddress());
                if (sort == 0)
                    ret = peerInfo1.getWhitePingCouner() > peerInfo2.getWhitePingCouner() ? 1 : -1;
                if (sort == 1)
                    ret = peerInfo1.getWhitePingCouner() < peerInfo2.getWhitePingCouner() ? 1 : -1;
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
                    ret = o1.getPing() > o2.getPing() ? 1 : -1;
                if (sort == 1)
                    ret = o1.getPing() < o2.getPing() ? 1 : -1;
                //      fireTableDataChanged();
                return ret;
            }
        });
    }

    // view peer
    // view == 0 only Active Peers
    // view == 1 all
    public void setView(int view) {
        this.view = view;
        peersView.clear();
        if (view == 0) {
            for (Peer peer : list) {
                if (peer.isUsed()) {
                    peersView.add(peer);
                }
            }
        } else if (view == 1) {
            peersView.addAll(list);
        }

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (peersView == null || this.peersView.size() - 1 < row) {
            return null;
        }

        if (Controller.getInstance().isOnStopping()) {
            this.deleteObservers();
            return null;
        }

        Peer peer = peersView.get(row);

        if (peer == null || DCSet.getInstance().isStoped())
            return null;

        PeerInfo peerInfo = Controller.getInstance().getDLSet().getPeerMap().getInfo(peer.getAddress());
        if (peerInfo == null)
            return null;

        switch (column) {
            case COLUMN_ADDRESS:
                return peer.getAddress().getHostAddress();

            case COLUMN_HEIGHT:
                if (!peer.isUsed()) {
                    int banMinutes = Controller.getInstance().getDLSet().getPeerMap().getBanMinutes(peer);
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
                return peer.getVersion() + " b: " + DateTimeFormat.timestamptoString(peer.getBuildTime(), "yyyy-MM-dd", "UTC");

        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        if (Controller.getInstance().isOnStopping()) {
            deleteObservers();
            return;
        }

        if (message.getType() == ObserverMessage.LIST_PEER_TYPE) {

            list = (List<Peer>) message.getValue();
            setView(view);
            needUpdate = true;

        } else if (message.getType() == ObserverMessage.UPDATE_PEER_TYPE) {
            setView(view);
            fireTableDataChanged();
            Peer peerValue = (Peer) message.getValue();
            for (int i = 0; i < peersView.size(); i++) {
                Peer peer = peersView.get(i);
                if (peerValue.equals(peer)) {
                    if (i < getRowCount()) {
                        fireTableRowsUpdated(i, i);
                        break;
                    }
                }
            }
        } else if (message.getType() == ObserverMessage.ADD_PEER_TYPE) {
            setView(view);
            needUpdate = true;

        } else if (message.getType() == ObserverMessage.REMOVE_PEER_TYPE) {
            setView(view);
            needUpdate = true;

        } else if (message.getType() == ObserverMessage.GUI_REPAINT
                && needUpdate) {
            needUpdate = false;
            fireTableDataChanged();
        }
    }

    public void addObservers() {
        Controller.getInstance().addActivePeersObserver(this);
    }

    public void deleteObservers() {
        Controller.getInstance().removeActivePeersObserver(this);
    }

}
