package org.erachain.api;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.block.Block;
import org.erachain.datachain.BlocksHeadsMap;
import org.erachain.datachain.BlocksMapImpl;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
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

    @GET
    @Path("/stop")
    public String stop() {

        if (!BlockChain.TEST_MODE)
            APIUtils.askAPICallAllowed(null, "GET core/stop", request, true);

        //STOP
        Controller.getInstance().stopAll(0);
        //	System.exit(0);

        //RETURN
        return String.valueOf(true);
    }

    @GET
    @Path("/status")
    public String getStatus() {
        return String.valueOf(Controller.getInstance().getStatus());
    }

    @GET
    @Path("/status/forging")
    public String getForgingStatus() {
        return String.valueOf(Controller.getInstance().getForgingStatus().getStatuscode());
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

    @SuppressWarnings("unchecked")
    @GET
    @Path("/version")
    public String getVersion() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("version", Controller.getInstance().getVersion(true));
        jsonObject.put("buildDate", Controller.getInstance().getBuildDateString());
        jsonObject.put("buildTimeStamp", Controller.buildTimestamp);


        return jsonObject.toJSONString();
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
            if (winValue != head.winValue) {
                if (update) {
                    totalWV += winValue;
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
                return "ERROR on Height: " + height + ", WinValue diff: " + (winValue - head.winValue);
            }

            if (totalWV != head.totalWinValue) {
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
                return "ERROR on Height: " + height + ", total WinValue diff: " + (totalWV - head.totalWinValue);
            }


        } while (true);

        if (out.isEmpty())
            return "GOOD! height: " + (height - 1) + ", totalWV: " + totalWV;

        return out;
    }

}
