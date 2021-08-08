package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.datachain.BlocksHeadsMap;
import org.erachain.datachain.BlocksMapImpl;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.network.Network;
import org.erachain.network.Peer;
import org.erachain.settings.Settings;
import org.erachain.utils.APIUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("core")
@Produces(MediaType.APPLICATION_JSON)
public class CoreResource {
    @Context
    HttpServletRequest request;
    private Controller cnt = Controller.getInstance();

    public static JSONObject infoJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("version", getVersionJson());
        jsonObject.put("networkMode", Settings.NET_MODE);

        jsonObject.put("status", getStatus());
        jsonObject.put("forgingStatus", getForgingStatusJson());


        Block last = Controller.getInstance().getLastBlock();
        jsonObject.put("lastBlock", last.blockHead.toJson());
        jsonObject.put("height", last.getHeight());

        Settings setting = Settings.getInstance();
        jsonObject.put("rpc", setting.isRpcEnabled());
        jsonObject.put("web", setting.isWebEnabled());

        if (BlockChain.CLONE_MODE) {
            JSONObject jsonClone = new JSONObject();
            ///jsonSide.put("magic", Ints.fromByteArray(Controller.getInstance().getMessageMagic()));
            jsonClone.put("name", Controller.getInstance().APP_NAME);
            jsonClone.put("timestamp", Controller.getInstance().blockChain.getGenesisBlock().getTimestamp());
            jsonClone.put("sign", Base58.encode(Controller.getInstance().blockChain.getGenesisBlock().getSignature()));

            if (false) jsonObject.put(Settings.CLONE_OR_SIDE.toLowerCase(), jsonClone);
            else jsonObject.put("side", jsonClone);

        }
        return jsonObject;
    }

    @GET
    @Path("/status")
    public static String getStatus() {
        return String.valueOf(Controller.getInstance().getStatus());
    }

    @GET
    @Path("/stop")
    public String stop() {

        if (!BlockChain.TEST_MODE)
            APIUtils.askAPICallAllowed(null, "GET core/stop", request, true);

        //STOP
        Thread thread = new Thread(() -> {
            Controller.getInstance().stopAndExit(0);
        });
        thread.start();

        //RETURN
        return String.valueOf(true);
    }

    public static JSONObject getVersionJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("version", Controller.version);
        jsonObject.put("buildDate", Controller.getInstance().getBuildDateTimeString());
        jsonObject.put("buildTimeStamp", Controller.buildTimestamp);

        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/version")
    public static String getVersion() {
        return getVersionJson().toJSONString();
    }

    public static JSONObject getForgingStatusJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("code", Controller.getInstance().getForgingStatus().getStatusCode());
        jsonObject.put("name", Controller.getInstance().getForgingStatus().getName());

        return jsonObject;
    }

    @GET
    @Path("/status/forging")
    public static String getForgingStatus() {
        return getForgingStatusJson().toJSONString();
    }

    @GET
    @Path("/isuptodate")
    public String isUpToDate() {
        return String.valueOf(Controller.getInstance().checkStatus(0));
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/settings")
    public String getSettings() {
        if (Controller.getInstance().doesWalletExists() && !Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
        }

        if (!Controller.getInstance().doesWalletExists() || Controller.getInstance().isWalletUnlocked()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("settings.json", Settings.getInstance().Dump());
            jsonObject.put("peers.json", Settings.getInstance().getPeersJson());
            return jsonObject.toJSONString();
        }

        return "";
    }

    @GET
    public String info() {
        return infoJson().toJSONString();
    }

    @GET
    @Path("/notranslate")
    public String getNoTranslate() {
        return JSONValue.toJSONString(Lang.getInstance().getNoTranslate());
    }

    @GET
    @Path("/monitor/{path}")
    public String getMonitorPath(@PathParam("path") String path, @QueryParam("log") String log) {
        JSONObject jsonObject = new JSONObject();

        Controller cnt = Controller.getInstance();

        if (path.equals("network_acceptor"))
            return cnt.network.getAcceptor().monitorToJson("true".equals(log)).toJSONString();
        else if (path.equals("network_creator"))
            return cnt.network.getCreator().monitorToJson("true".equals(log)).toJSONString();
        else if (path.equals("block_generator"))
            return cnt.getBlockGenerator().monitorToJson("true".equals(log)).toJSONString();
        else {
            for (Peer peer : Controller.getInstance().network.getKnownPeers()) {
                if (peer.getName().startsWith(path)) {
                    return peer.monitorToJson("true".equals(log)).toJSONString();
                }
            }
        }

        return getMonitor(log);

    }

    @GET
    @Path("/monitor")
    public String getMonitor(@QueryParam("log") String log) {
        JSONObject jsonObject = new JSONObject();

        Controller cnt = Controller.getInstance();

        jsonObject.put("network_acceptor", cnt.network.getAcceptor().monitorToJson("true".equals(log)));
        jsonObject.put("network_creator", cnt.network.getCreator().monitorToJson("true".equals(log)));
        jsonObject.put("block_generator", cnt.getBlockGenerator().monitorToJson("true".equals(log)));

        for (Peer peer : Controller.getInstance().network.getKnownPeers()) {
            jsonObject.put(peer.toString(), peer.monitorToJson("true".equals(log)));
        }

        return jsonObject.toJSONString();
    }

    @GET
    @Path("/info/speed")
    public String getSpeedInfo() {
        return Controller.getInstance().getBenchmarks().toJSONString();
    }

    @GET
    @Path("/cwv")
    public String checkWinVal(@QueryParam("update") boolean update) {
        DCSet dcSet = DCSet.getInstance();
        BlocksHeadsMap mapHeads = dcSet.getBlocksHeadsMap();
        BlocksMapImpl mapBlock = dcSet.getBlockMap();
        int height = 1;
        long totalWV = 0;
        String out = "";
        do {
            Block.BlockHead head = mapHeads.get(++height);
            if (head == null)
                break;

            Block block = mapBlock.get(height);
            if (!block.getCreator().equals(head.creator)) {
                return "ERROR on Height: " + height + ", tCREATOR diff: " + block.getCreator() + " - " + head.creator;
            }

            // берем текущую - там есть предыдущая
            Fun.Tuple3<Integer, Integer, Integer> forgingData = head.creator.getForgingData(dcSet, height);
            long winValue = BlockChain.calcWinValue(dcSet, head.creator, height, forgingData.c, null);
            totalWV += winValue;
            if (update) {
                final long parentTarget = mapHeads.get(height - 1).target;
                long newTarget = BlockChain.calcTarget(height, parentTarget, winValue);
                head = new Block.BlockHead(block, head.heightBlock,
                        head.forgingValue, // его посреди цепочки не получится пересчитать - бем что тут сохранено
                        winValue, newTarget,
                        head.totalFee, head.emittedFee, totalWV);
                mapHeads.put(height, head);
                block.setWinValue(winValue);
                block.setTotalWinValue(totalWV);
                block.setTarget(newTarget);
                mapBlock.put(height, block);
                continue;
            }

            if (winValue != head.winValue) {
                return "ERROR on Height: " + height + ", WinValue diff: " + (winValue - head.winValue);
            }

            if (totalWV != head.totalWinValue) {
                return "ERROR on Height: " + height + ", total WinValue diff: " + (totalWV - head.totalWinValue);
            }


        } while (true);

        if (out.isEmpty())
            return "GOOD! height: " + (height - 1) + ", totalWV: " + totalWV;

        return out;
    }

    //@GET
    //@Path("/monitor/{path}")
    //public String getMonitorPath(@PathParam("path") String path, @QueryParam("log") String log) {

    @GET
    @Path("/sync/{toHeight}")
    public String sync(@PathParam("toHeight") int to, @QueryParam("peer") String peerStr) {
        Thread thread = new Thread(() -> {
            Controller.getInstance().getBlockGenerator().setSyncTo(to, Controller.getInstance().network.getKnownPeer(peerStr, Network.ANY_TYPE));
        });
        thread.setName("sync to " + to);
        thread.start();
        return "run";
    }

    @GET
    @Path("/dc/clearcache")
    public String dcClearCache() {
        Controller.getInstance().getDCSet().clearCache();
        return "run";
    }

    @GET
    @Path("/dl/clearcache")
    public String dlClearCache() {
        Controller.getInstance().getDLSet().clearCache();
        return "run";
    }

    @GET
    @Path("/dw/clearcache")
    public String dwClearCache() {
        Controller.getInstance().getWallet().dwSet.clearCache();
        return "run";
    }

}
