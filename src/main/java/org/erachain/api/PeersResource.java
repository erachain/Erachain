package org.erachain.api;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.database.PeerMap.PeerInfo;
import org.erachain.network.Peer;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.APIUtils;
import org.erachain.utils.DateTimeFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("peers")
@Produces(MediaType.APPLICATION_JSON)
public class PeersResource {
    @Context
    HttpServletRequest request;

    @SuppressWarnings("unchecked")
    @GET
    public String getPeers() {
        List<Peer> peers = Controller.getInstance().getActivePeers();
        JSONArray array = new JSONArray();

        for (Peer peer : peers) {
            array.add(peer.getAddress().getHostAddress());
        }

        return array.toJSONString();
    }

    @POST
    public String addPeer(String address) {

        String password = null;
        APIUtils.askAPICallAllowed(password, "POST peers " + address, request, true);

        // CHECK WALLET UNLOCKED
        if (Controller.getInstance().doesWalletKeysExists() && !Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_WALLET_LOCKED);
        }

        Peer peer;
        try {
            peer = new Peer(InetAddress.getByName(address));
        } catch (UnknownHostException e) {
            throw ApiErrorFactory.getInstance().createError(
                    ApiErrorFactory.ERROR_INVALID_NETWORK_ADDRESS);
        }
        peer.addPingCounter();
        Controller.getInstance().getDLSet().getPeerMap().addPeer(peer, 0);

        return "OK";
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("height")
    public String getTest() {
        JSONArray array = new JSONArray();

        for (Peer peer : Controller.getInstance().network.getActivePeers(false)) {
            JSONObject o = new JSONObject();
            o.put("peer", peer.getAddress().getHostAddress());
            o.put("height", peer.getHWeight(false).a);
            o.put("weight", peer.getHWeight(false).b);
            array.add(o);
        }

        return array.toJSONString();
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    @GET
    @Path("detail")
    public String getDetail() {
        List<Peer> activePeers = Controller.getInstance().getActivePeers();
        Map output = getMapPeers(activePeers);
        return JSONValue.toJSONString(output);
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    @GET
    @Path("detail/knownpeers")
    public String getDetailKnownPeers() {
        List<Peer> knownPeers = Controller.getInstance().network.getAllPeers();
        Map output = getMapPeers(knownPeers);
        return JSONValue.toJSONString(output);
    }


    /**
     *
     * @param peers
     * @return
     */
    private Map getMapPeers(List<Peer> peers) {
        Map map = new LinkedHashMap();

        for (int i = 0; i < peers.size(); i++) {
            Peer peer = peers.get(i);

            if (peer != null) {
                map.put(peer.getAddress().getHostAddress(), this.getDetail(peer));
            }
        }
        return map;
    }

    @GET
    @Path("detail/{address}")
    public String getDetail(@PathParam("address") String address) {
        Peer peer = null;

        List<Peer> activePeers = Controller.getInstance().getActivePeers();

        for (Peer activePeer : activePeers) {
            if (activePeer.getAddress().getHostAddress().equals(address)) {
                if (peer == null) {
                    peer = activePeer;
                }

                if (activePeer.isWhite()) {
                    peer = activePeer;
                }
            }
        }

        if (peer == null) {
            try {
                peer = new Peer(InetAddress.getByName(address));
            } catch (UnknownHostException e) {
                throw ApiErrorFactory.getInstance().createError(
                        ApiErrorFactory.ERROR_INVALID_NETWORK_ADDRESS);
            }
        }

        return this.getDetail(peer).toJSONString();
    }

    @SuppressWarnings("unchecked")
    public JSONObject getDetail(Peer peer) {
        JSONObject o = new JSONObject();

        if (peer.isUsed()) {
            o.put("status", "connected");
        } else if (Controller.getInstance().getDLSet().getPeerMap().contains(peer.getAddress().getAddress())) {
            o.put("status", "known disconnected");
        }

        o.put("height", peer.getHWeight(false));
        o.put("version", peer.getVersion());
        if (Settings.getInstance().isWebEnabled() && Settings.getInstance().getWebAllowed().length == 0) {
            // разрешено всем - передадим его
            o.put("port", Settings.getInstance().getWebPort());
            o.put("scheme", Settings.getInstance().isWebUseSSL() ? "https" : "http");
        }
        o.put("buildTime", peer.getBuildTime() > 0 ?
                DateTimeFormat.timestamptoString(peer.getBuildTime(), "yyyy-MM-dd", "UTC")
                : "");
        if (peer.isPinger()) {
            o.put("ping", peer.getPing());
        }
        if (peer.getConnectionTime() > 0) {
            o.put("onlineTime", (NTP.getTime() - peer.getConnectionTime()) / 1000);
        }


        if (Controller.getInstance().getDLSet().getPeerMap().contains(peer.getAddress().getAddress())) {
            PeerInfo peerInfo = Controller.getInstance().getDLSet().getPeerMap().getInfo(peer.getAddress());

            o.put("findingTime", DateTimeFormat.timestamptoString(peerInfo.getFindingTime()));
            o.put("findingTimeStamp", peerInfo.getFindingTime());

            if (peerInfo.getWhiteConnectTime() > 0) {
                o.put("lastWhite", DateTimeFormat.timestamptoString(peerInfo.getWhiteConnectTime()));
                o.put("lastWhiteTimeStamp", peerInfo.getWhiteConnectTime());

            } else {
                o.put("lastWhite", "never");
            }
            if (peerInfo.getGrayConnectTime() > 0) {
                o.put("lastGray", DateTimeFormat.timestamptoString(peerInfo.getGrayConnectTime()));
                o.put("lastGrayTimeStamp", peerInfo.getGrayConnectTime());
            } else {
                o.put("lastGray", "never");
            }
            o.put("whitePingCounter", peerInfo.getWhitePingCouner());
        }

        if (o.isEmpty()) {
            o.put("status", "unknown disconnected");
        }

        return o;
    }


    @SuppressWarnings("unchecked")
    @GET
    @Path("best")
    public String getTopPeers() {
        List<Peer> peers = Controller.getInstance().network.getBestPeers();
        JSONArray array = new JSONArray();

        for (Peer peer : peers) {
            array.add(peer.getAddress().getHostAddress());
        }

        return array.toJSONString();
    }

    @SuppressWarnings({"unchecked"})
    @GET
    @Path("known")
    public String getKnown() throws UnknownHostException {
        List<String> addresses = Controller.getInstance().getDLSet().getPeerMap().getAllPeersAddresses(-1);

        JSONArray array = new JSONArray();

        array.addAll(addresses);

        return array.toJSONString();
    }

    @SuppressWarnings({"unchecked"})
    @GET
    @Path("preset")
    public String getPreset() {
        List<String> addresses = new ArrayList<>();
        for (Peer peer : Settings.getInstance().getKnownPeers()) {
            addresses.add(peer.getAddress().getHostAddress());
        }

        JSONArray array = new JSONArray();

        array.addAll(addresses);

        return array.toJSONString();
    }

    @DELETE
    @Path("/known")
    public String clearPeers() {
        Controller.getInstance().getDLSet().getPeerMap().clear();

        return "OK";
    }

    @GET
    @Path("/testghw/{address}")
    public String testHW(@PathParam("address") String address) {

        if (!BlockChain.TEST_MODE)
            return "not testnet";

        List<Peer> activePeers = Controller.getInstance().getActivePeers();

        for (Peer peer : activePeers) {
            if (peer.getAddress().getHostAddress().equals(address)) {
                boolean res = peer.tryPing();
                //Message pingMessage = MessageFactory.getInstance().createGetHWeightMessage();
                //pingMessage.setId(999999);
                //peer.sendMessage(pingMessage);
                return "sended " + res + " " + peer.getPing() + "ms";
            }
        }

        return address + " - peer not active";
    }

    @GET
    @Path("/ping/{address}")
    public String ping(@PathParam("address") String address) {

        List<Peer> activePeers = Controller.getInstance().getActivePeers();

        for (Peer peer : activePeers) {
            if (peer.getAddress().getHostAddress().equals(address)) {
                boolean res = peer.tryPing();
                return "sended " + res + " " + peer.getPing() + "ms";
            }
        }

        return address + " - peer not active";
    }
}
