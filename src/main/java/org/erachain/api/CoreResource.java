package org.erachain.api;

import com.google.gson.JsonObject;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.lang.Lang;
import org.erachain.network.Peer;
import org.erachain.settings.Settings;
import org.erachain.utils.APIUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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

        if (!BlockChain.DEVELOP_USE)
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

        jsonObject.put("version", Controller.getInstance().getVersion());
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
        else

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

        for (Peer peer: Controller.getInstance().network.getActivePeers(false)) {
            jsonObject.put(peer.toString(), peer.monitorToJson("true".equals(log)));
        }

        return jsonObject.toJSONString();
    }

    @GET
    @Path("/info/speed")
    public String getSpeedInfo() {
        JSONObject jsonObj = new JSONObject();
        Controller cnt = Controller.getInstance();

        if (BlockChain.DEVELOP_USE) {
            jsonObj.put("missedTelegrams", cnt.getInstance().network.missedTelegrams.get());
            jsonObj.put("missedTransactions", cnt.getInstance().network.missedTransactions.get());
            jsonObj.put("activePeersCounter", cnt.getInstance().network.getKnownPeers());
            jsonObj.put("missedWinBlocks", cnt.getInstance().network.missedWinBlocks.get());
            jsonObj.put("missedMessages", cnt.getInstance().network.missedMessages.get());
            jsonObj.put("missedSendes", cnt.getInstance().network.missedSendes.get());
            jsonObj.put("msgTimingAvrg", cnt.getInstance().network.telegramer.messageTimingAverage);
            jsonObj.put("unconfMsgTimingAvrg", cnt.getInstance().getUnconfigmedMessageTimingAverage());
            jsonObj.put("transactionWinnedTimingAvrg", cnt.getInstance().getBlockChain().transactionWinnedTimingAverage);
            jsonObj.put("transactionMakeTimingAvrg", cnt.getInstance().getTransactionMakeTimingAverage());
            jsonObj.put("transactionValidateTimingAvrg", cnt.getInstance().getBlockChain().transactionValidateTimingAverage);
            jsonObj.put("transactionProcessTimingAvrg", cnt.getInstance().getBlockChain().transactionProcessTimingAverage);
        }
        else {
            jsonObj.put("null", "null");
        }
        return jsonObj.toJSONString();
    }

}
