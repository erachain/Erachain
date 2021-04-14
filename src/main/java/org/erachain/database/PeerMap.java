package org.erachain.database;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedBytes;
import org.erachain.dbs.DCUMapImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.network.Peer;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.PeerInfoComparator;
import org.erachain.utils.ReverseComparator;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.*;

public class PeerMap extends DCUMapImpl<byte[], byte[]> {
    private static final byte[] BYTE_WHITELISTED = new byte[]{0, 0};
    //private static final byte[] BYTE_BLACKLISTED = new byte[]{1, 1};
    private static final byte[] BYTE_NOTFOUND = new byte[]{2, 2};
    static Logger LOGGER = LoggerFactory.getLogger(PeerMap.class.getName());

    public PeerMap(DLSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap("peers")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<byte[], byte[]>(UnsignedBytes.lexicographicalComparator());
    }

    public List<Peer> getAllPeers(int amount) {
        try (IteratorCloseable<byte[]> iterator = IteratorCloseableImpl.make(this.keySet().iterator())) {
            //GET ITERATOR

            //PEERS
            List<Peer> peers = new ArrayList<Peer>();

            //ITERATE AS LONG AS:
            // 1. we have not reached the amount of peers
            // 2. we have read all records
            while (iterator.hasNext() && peers.size() < amount) {
                //GET ADDRESS
                byte[] addressBI = iterator.next();

                //CHECK IF ADDRESS IS WHITELISTED
                if (Arrays.equals(Arrays.copyOfRange(this.get(addressBI), 0, 2), BYTE_WHITELISTED)) {
                    InetAddress address = InetAddress.getByAddress(addressBI);

                    //CHECK IF SOCKET IS NOT LOCALHOST
                    if (!Settings.getInstance().isLocalAddress(address)) {
                        //CREATE PEER
                        Peer peer = new Peer(address);

                        //ADD TO LIST
                        peers.add(peer);
                    }
                }
            }

            //RETURN
            return peers;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            return new ArrayList<Peer>();
        }
    }

    private List<Peer> bestPeers;
    private long lasTimeForBestPeers = 0;
    public List<Peer> getBestPeers(int amount, boolean allFromSettings) {

        if (bestPeers != null && System.currentTimeMillis() - lasTimeForBestPeers < 30000) {
            return bestPeers;
        }

        try {
            //PEERS
            List<Peer> peers = new ArrayList<Peer>();
            List<PeerInfo> listPeerInfo = new ArrayList<PeerInfo>();

            //////// GET first all WHITE PEERS
            try {
                //GET ITERATOR
                Iterator<byte[]> iterator = this.keySet().iterator();

                //ITERATE AS LONG AS:

                // 1. we have not reached the amount of peers
                // 2. we have read all records
                while (iterator.hasNext()
                    //&& listPeerInfo.size() < amount - take all known before SORT
                        ) {
                    //GET ADDRESS
                    byte[] addressBI = iterator.next();

                    //CHECK IF ADDRESS IS WHITELISTED

                    byte[] data = this.get(addressBI);

                    try {
                        PeerInfo peerInfo = new PeerInfo(addressBI, data);

                        InetAddress address = InetAddress.getByAddress(peerInfo.getAddress());
                        //CHECK IF SOCKET IS NOT LOCALHOST
                        if (Settings.getInstance().isLocalAddress(address))
                            continue;
                        //CHECK IF SOCKET IS NOT LOCALNET
                        if (address.isSiteLocalAddress())
                            continue;

                        if ( //use all peers an new and not known Arrays.equals(peerInfo.getStatus(), BYTE_WHITELISTED) &&
                                peerInfo.banTime < NTP.getTime()) {
                            listPeerInfo.add(peerInfo);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                Collections.sort(listPeerInfo, new ReverseComparator<PeerInfo>(new PeerInfoComparator()));

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            for (PeerInfo peerInfo : listPeerInfo) {
                InetAddress address = InetAddress.getByAddress(peerInfo.getAddress());
                peers.add(new Peer(address));
            }

            int cnt = peers.size();
            // show for mySelp in start only
            //if (cnt > 0 && allFromSettings)
            //    logger.info("White peers loaded from database : " + cnt);

            if (allFromSettings) {
                List<Peer> knownPeers = Settings.getInstance().getKnownPeers();
                //logger.info("Peers loaded from settings and internet: " + knownPeers.size());

                int insertIndex = 0;
                for (Peer knownPeer : knownPeers) {
                    try {
                        //if(!allFromSettings && peers.size() >= amount)
                        //	break;

                        int i = 0;
                        int found = -1;
                        for (Peer peer : peers) {
                            if (peer.getAddress().equals(knownPeer.getAddress())) {
                                found = i;
                                break;
                            }
                            i++;
                        }

                        if (found == -1) {
                            //ADD TO LIST
                            if (peers.size() > insertIndex) {
                                peers.add(insertIndex, knownPeer);
                            } else {
                                peers.add(knownPeer);
                            }
                            //peers.add(knownPeer);
                        } else {
                            // REMOVE from this PLACE
                            peers.remove(found);
                            // ADD in TOP
                            if (peers.size() > insertIndex) {
                                peers.add(insertIndex, knownPeer);
                            } else {
                                peers.add(knownPeer);
                            }
                        }
                        insertIndex++;

                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }

            }

            //////// GET in end all  PEERS in DB

            cnt = peers.size();
            //GET ITERATOR
            Iterator<byte[]> iterator = this.keySet().iterator();

            //ITERATE AS LONG AS:

            // 1. we have not reached the amount of peers
            // 2. we have read all org.erachain.records
            while (iterator.hasNext() && (amount == 0 || peers.size() < amount)) {
                //GET ADDRESS
                byte[] addressBI = iterator.next();
                boolean found = false;
                for (Peer peer : peers) {
                    if (Arrays.equals(peer.getAddress().getAddress(), addressBI)) {
                        found = true;
                        break;
                    }
                }

                if (found)
                    continue;

                //CHECK IF ADDRESS IS NOT BANNED

                byte[] data = this.get(addressBI);

                try {
                    PeerInfo peerInfo = new PeerInfo(addressBI, data);
                    InetAddress address = InetAddress.getByAddress(peerInfo.getAddress());
                    peers.add(new Peer(address));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

            // Show only for mySelf on start
            //if (allFromSettings)
                //logger.info("Peers loaded from database : " + (peers.size() - cnt));

            // STORE in CACHE
            bestPeers = peers;
            lasTimeForBestPeers = System.currentTimeMillis();

            //RETURN
            return peers;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            return new ArrayList<Peer>();
        }
    }

    public List<String> getAllPeersAddresses(int amount) {
        try {
            List<String> addresses = new ArrayList<String>();
            Iterator<byte[]> iterator = this.keySet().iterator();
            while (iterator.hasNext() && (amount == -1 || addresses.size() < amount)) {
                byte[] addressBI = iterator.next();
                addresses.add(InetAddress.getByAddress(addressBI).getHostAddress());
            }
            return addresses;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            return new ArrayList<String>();
        }
    }

    public List<PeerInfo> getAllInfoPeers(int amount) {
        try {
            //GET ITERATOR
            Iterator<byte[]> iterator = this.keySet().iterator();

            //PEERS
            List<PeerInfo> peers = new ArrayList<PeerInfo>();

            //ITERATE AS LONG AS:
            // 1. we have not reached the amount of peers
            // 2. we have read all org.erachain.records
            while (iterator.hasNext() && peers.size() < amount) {
                //GET ADDRESS
                byte[] addressBI = iterator.next();
                byte[] data = this.get(addressBI);

                peers.add(new PeerInfo(addressBI, data));
            }

            //SORT
            Collections.sort(peers, new ReverseComparator<PeerInfo>(new PeerInfoComparator()));

            //RETURN
            return peers;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            return new ArrayList<PeerInfo>();
        }
    }

    public void addPeer(Peer peer, int banMinutes) {
        if (this.map == null) {
            return;
        }

        PeerInfo peerInfo;
        byte[] address = peer.getAddress().getAddress();

        if (this.map.containsKey(address)) {
            byte[] data = this.map.get(address);
            peerInfo = new PeerInfo(address, data);
        } else {
            peerInfo = new PeerInfo(address, null);
        }

        if (banMinutes > 0) {
            // add ban timer by minutes
            peerInfo.banTime = NTP.getTime() + banMinutes * 60000;
        } else {
            peerInfo.banTime = 0;
        }

        if (peer.getPingCounter() > 1) {
            if (peer.isWhite()) {
                peerInfo.addWhitePingCouner(1);
                peerInfo.updateWhiteConnectTime();
            } else {
                peerInfo.updateGrayConnectTime();
            }
        }

        if (peer.isWhite()) {
            peerInfo.setStatus(BYTE_WHITELISTED);
        }

        //ADD PEER INTO DB
        this.map.put(address, peerInfo.toBytes());
    }

    public PeerInfo getInfo(InetAddress address) {
        byte[] addressByte = address.getAddress();

        if (this.map == null) {
            return new PeerInfo(addressByte, BYTE_NOTFOUND);
        }

        if (this.map.containsKey(addressByte)) {
            byte[] data = this.map.get(addressByte);

            return new PeerInfo(addressByte, data);
        }
        return new PeerInfo(addressByte, BYTE_NOTFOUND);
    }

    public boolean isBanned(byte[] key) {
        //CHECK IF PEER IS BLACKLISTED
        if (this.contains(key)) {
            byte[] data = this.map.get(key);
            PeerInfo peerInfo = new PeerInfo(key, data);

            if (peerInfo.banTime < NTP.getTime()) {
                peerInfo.setBanTime(0);
                return false;
            } else {
                return true;
            }
        }

        return false;
    }

    public boolean isBanned(InetAddress address) {
        //CHECK IF PEER IS BLACKLISTED
        return isBanned(address.getAddress());
    }

    public int getBanMinutes(Peer peer) {

        byte[] key = peer.getAddress().getAddress();

        //CHECK IF PEER IS BLACKLISTED
        if (this.contains(key)) {
            byte[] data = this.map.get(key);
            PeerInfo peerInfo = new PeerInfo(key, data);

            if (peerInfo.getBanTime() > NTP.getTime()) {
                return (int) ((peerInfo.getBanTime() - NTP.getTime()) / 60000);
            } else {
                return 0;
            }
        }

        return 0;
    }

    public boolean isBad(InetAddress address) {
        byte[] addressByte = address.getAddress();

        //CHECK IF PEER IS BAD
        if (this.contains(addressByte)) {
            byte[] data = this.map.get(addressByte);

            PeerInfo peerInfo = new PeerInfo(addressByte, data);

            boolean findMoreWeekAgo = (NTP.getTime() - peerInfo.getFindingTime() > 7 * 24 * 60 * 60 * 1000);

            boolean neverWhite = peerInfo.getWhitePingCouner() == 0;

            return findMoreWeekAgo && neverWhite;
        }

        return false;
    }

    public class PeerInfo {

        static final int TIMESTAMP_LENGTH = 8;
        static final int STATUS_LENGTH = 2;

        private byte[] address;
        private byte[] status;
        private long findingTime;
        private long whiteConnectTime;
        private long grayConnectTime;
        private long whitePingCouner;
        private long banTime;

        public PeerInfo(byte[] address, byte[] data) {

            if (data != null && data.length == STATUS_LENGTH + TIMESTAMP_LENGTH * 5) {
                int position = 0;

                byte[] statusBytes = Arrays.copyOfRange(data, position, position + STATUS_LENGTH);
                position += STATUS_LENGTH;

                byte[] findTimeBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
                long longFindTime = Longs.fromByteArray(findTimeBytes);
                position += TIMESTAMP_LENGTH;

                byte[] whiteConnectTimeBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
                long longWhiteConnectTime = Longs.fromByteArray(whiteConnectTimeBytes);
                position += TIMESTAMP_LENGTH;

                byte[] grayConnectTimeBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
                long longGrayConnectTime = Longs.fromByteArray(grayConnectTimeBytes);
                position += TIMESTAMP_LENGTH;

                byte[] whitePingCounerBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
                long longWhitePingCouner = Longs.fromByteArray(whitePingCounerBytes);
                position += TIMESTAMP_LENGTH;

                byte[] banTimeBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);

                this.address = address;
                this.status = statusBytes;
                this.findingTime = longFindTime;
                this.whiteConnectTime = longWhiteConnectTime;
                this.grayConnectTime = longGrayConnectTime;
                this.whitePingCouner = longWhitePingCouner;
                this.banTime = Longs.fromByteArray(banTimeBytes);

            } else if (Arrays.equals(data, BYTE_WHITELISTED)) {
                this.address = address;
                this.status = BYTE_WHITELISTED;
                this.findingTime = 0;
                this.whiteConnectTime = 0;
                this.grayConnectTime = 0;
                this.whitePingCouner = 0;
                this.banTime = 0;

                this.updateFindingTime();

            } else {
                this.address = address;
                this.status = BYTE_NOTFOUND;
                this.findingTime = 0;
                this.whiteConnectTime = 0;
                this.grayConnectTime = 0;
                this.whitePingCouner = 0;
                this.banTime = 0;

                this.updateFindingTime();
            }
        }

        public byte[] getAddress() {
            return address;
        }

        public void setStatus(byte[] status) {
            this.status = status;
        }

        public byte[] getStatus() {
            return status;
        }

        public long getFindingTime() {
            return findingTime;
        }

        public long getWhiteConnectTime() {
            return whiteConnectTime;
        }

        public long getGrayConnectTime() {
            return grayConnectTime;
        }

        public long getWhitePingCouner() {
            return whitePingCouner;
        }

        public long getBanTime() {
            return banTime;
        }

        public void setBanTime(long toTime) {
            banTime = toTime;
        }

        public void addWhitePingCouner(int n) {
            this.whitePingCouner += n;
        }

        public void updateWhiteConnectTime() {
            this.whiteConnectTime = NTP.getTime();
        }

        public void updateGrayConnectTime() {
            this.grayConnectTime = NTP.getTime();
        }

        public void updateFindingTime() {
            this.findingTime = NTP.getTime();
        }

        public byte[] toBytes() {

            byte[] findTimeBytes = Longs.toByteArray(this.findingTime);
            findTimeBytes = Bytes.ensureCapacity(findTimeBytes, TIMESTAMP_LENGTH, 0);

            byte[] whiteConnectTimeBytes = Longs.toByteArray(this.whiteConnectTime);
            whiteConnectTimeBytes = Bytes.ensureCapacity(whiteConnectTimeBytes, TIMESTAMP_LENGTH, 0);

            byte[] grayConnectTimeBytes = Longs.toByteArray(this.grayConnectTime);
            grayConnectTimeBytes = Bytes.ensureCapacity(grayConnectTimeBytes, TIMESTAMP_LENGTH, 0);

            byte[] whitePingCounerBytes = Longs.toByteArray(this.whitePingCouner);
            whitePingCounerBytes = Bytes.ensureCapacity(whitePingCounerBytes, TIMESTAMP_LENGTH, 0);

            byte[] banTimeBytes = Longs.toByteArray(this.banTime);
            banTimeBytes = Bytes.ensureCapacity(banTimeBytes, TIMESTAMP_LENGTH, 0);

            return Bytes.concat(this.status, findTimeBytes, whiteConnectTimeBytes, grayConnectTimeBytes, whitePingCounerBytes, banTimeBytes);
        }

    }
}
