package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.database.PeerMap.PeerInfo;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.network.Peer;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;

import java.util.*;

/**
 * TODO тут нужно применить SortableList для сортировки по полям?
 * Или тут более изящно сортировка сделана?
 */
@SuppressWarnings("serial")
public class PeersTableModel extends TimerTableModelCls<Peer> implements Observer {

    public static final int COLUMN_ADDRESS = 0;
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
    Controller cnt;
    DCSet dcSet;

    public PeersTableModel() {
        super(new String[]{"IP", "Height", "Ping mc", "Reliable", "Initiator", "Finding ago",
                        "Online Time", "Version"},
                new Boolean[]{true, true, true, true, true, true, true, false}, false);

        cnt = Controller.getInstance();
        dcSet = DCSet.getInstance();
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
                PeerInfo peerInfo1 = cnt.getDLSet().getPeerMap().getInfo(o1.getAddress());
                PeerInfo peerInfo2 = cnt.getDLSet().getPeerMap().getInfo(o2.getAddress());
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

        if (cnt.isOnStopping()) {
            this.deleteObservers();
            return null;
        }

        Peer peer = peersView.get(row);

        if (peer == null || dcSet.isStoped())
            return null;

        PeerInfo peerInfo = cnt.getDLSet().getPeerMap().getInfo(peer.getAddress());
        if (peerInfo == null)
            return null;

        switch (column) {
            case COLUMN_ADDRESS:
                JSONObject info = peer.getNodeInfo();
                if (info != null) {
                    Long port = (Long) info.get("port");
                    if (port != null) {
                        return "<html><a href=#>" + info.getOrDefault("host",
                                info.getOrDefault("host2",
                                        peer.getAddress().getHostName())) + "</a>";
                    }
                }
                return peer.getAddress().getHostName();

            case COLUMN_HEIGHT:
                Tuple2<Integer, Long> res = peer.getHWeight(true);
                if (res == null || res.a == 0) {
                    if (peer.isUsed()) {
                        return Lang.T("Waiting...");
                    }
                    return Lang.T("");
                }
                long diffWeight = (res.b - cnt.blockChain.getHWeightFull(dcSet).b);
                return "H=" + res.a.toString() + " W" + (diffWeight > 0 ? "+" + diffWeight : diffWeight) + (peer.getMute() > 0 ? " mute:" + peer.getMute() : "");

            case COLUMN_PINGMC:
                if (!peer.isUsed()) {
                    int banMinutes = cnt.getDLSet().getPeerMap().getBanMinutes(peer);
                    if (banMinutes > 0) {
                        return Lang.T("Banned") + " " + banMinutes + "m" + " (" + peer.getBanMessage() + ")";
                    } else {
                        return Lang.T("Broken") + (peer.getBanMessage() == null ? "" : " (" + peer.getBanMessage() + ")");
                    }
                } else if (peer.getPing() > 1000000) {
                    return Lang.T("Waiting...");
                } else {
                    return "" + peer.getPing();
                }

            case COLUMN_REILABLE:
                return peerInfo.getWhitePingCouner();

            case COLUMN_INITIATOR:
                if (peer.isWhite()) {
                    return Lang.T("You");
                } else {
                    return Lang.T("Remote");
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

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        if (cnt.isOnStopping()) {
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
        cnt.addActivePeersObserver(this);
    }

    public void deleteObservers() {
        cnt.removeActivePeersObserver(this);
    }

}
