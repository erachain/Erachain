package org.erachain.api;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.AddressForging;
import org.erachain.datachain.BlockMap;
import org.erachain.datachain.DCSet;
import org.erachain.utils.APIUtils;
import org.erachain.utils.Pair;
import org.json.simple.JSONArray;
import org.mapdb.Fun;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Path("blocks")
@Produces(MediaType.APPLICATION_JSON)
public class BlocksResource {
    @Context
    HttpServletRequest request;

    /**
     * @param signature signature or height of block
     * @return
     */
    @GET
    @Path("/{signature}")
    public static String getBlock(@PathParam("signature") String signature) {
        //DECODE SIGNATURE
        if (Base58.isExtraSymbols(signature))
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);

        Block block;

        try {
            Integer height = new Integer(signature);
            block = Controller.getInstance().getBlockByHeight(height);
        } catch (Exception e1) {
            byte[] signatureBytes;
            try {
                signatureBytes = Base58.decode(signature);
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
            }

            block = Controller.getInstance().getBlock(signatureBytes);
        }

        //CHECK IF BLOCK EXISTS
        if (block == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.BLOCK_NOT_EXIST);
        }

        return block.toJson().toJSONString();
    }

    @GET
    @Path("/last")
    public static String getLastBlock() {
        return Controller.getInstance().getLastBlock().toJson().toJSONString();
    }

    @GET
    @Path("/lasthead")
    public static String getLastBlockHead() {
        return Controller.getInstance().getDCSet().getBlocksHeadsMap().last().toJson().toJSONString();
    }

    @GET
    @Path("/height")
    public static String getHeight() {
        return String.valueOf(Controller.getInstance().getMyHeight());
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/fromheight/{height}")
    public static String getFromHeight(@PathParam("height") int height) {
        DCSet db = DCSet.getInstance();

        JSONArray array = new JSONArray();

        BlockMap map = db.getBlockMap();
        Block block;
        while (height < map.size()) {
            block = map.get(height++);
            array.add(block.toJson());
        }

        return array.toJSONString();
    }

    @GET
    @Deprecated
    @Path("/height/{signature}")
    public static String getHeight(@PathParam("signature") String signature) {
        //DECODE SIGNATURE
        byte[] signatureBytes;
        try {
            signatureBytes = Base58.decode(signature);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
        }

        Block block = DCSet.getInstance().getBlockSignsMap().getBlock(signatureBytes);

        //CHECK IF BLOCK EXISTS
        if (block == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
        }

        return String.valueOf(block.getHeight());
    }

    /*
     * GET HEADES as on nerwork communication
     *  -> response callback in controller.Controller.onMessage(Message)
         type = GET_SIGNATURES_TYPE
         FOR - core.Synchronizer.getBlockSignatures(byte[], Peer)

     */
    @GET
    @Path("/headers/{signature}")
    public static String getHeaders(@PathParam("signature") String signature) {
        //DECODE SIGNATURE
        byte[] signatureBytes;
        try {
            signatureBytes = Base58.decode(signature);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
        }

        List<byte[]> headers = Controller.getInstance().getNextHeaders(signatureBytes);

        //CHECK IF BLOCK EXISTS
        if (headers == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
        }
        List<String> result = new ArrayList<String>();
        for (byte[] sign : headers) {
            result.add(Base58.encode(sign));
        }

        return String.valueOf(result);
    }

    @GET
    @Deprecated
    @Path("/byheight/{height}")
    public static String getbyHeight(@PathParam("height") int height) {
        Block block;
        try {
            block = Controller.getInstance().getBlockByHeight(height);
            if (block == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
            }
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
        }
        return block.toJson().toJSONString();
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/addresses/{limit}")
    public String getLastAccountsBlocks(@PathParam("limit") int limit) {

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletKeysExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        List<Pair<Account, Block.BlockHead>> blocks = Controller.getInstance().getLastWalletBlocks(limit);
        JSONArray array = new JSONArray();

        for (Pair<Account, Block.BlockHead> block : blocks) {
            array.add(block.getB().toJson());
        }

        return array.toJSONString();
    }
	
	/*
	@GET
	@Path("/time")
	public String getTimePerBlock()
	{
		Block block = Controller.getInstance().getLastBlock();
		long timePerBlock = BlockGenerator.getBlockTime(block.getGeneratingBalance());
		return String.valueOf(timePerBlock);
	}
	
	@GET
	@Path("/time/{generatingbalance}")
	public String getTimePerBlock(@PathParam("generating") long generatingbalance)
	{
		long timePerBlock = BlockGenerator.getBlockTime(generatingbalance);
		return String.valueOf(timePerBlock);
	}
	*/

    @SuppressWarnings("unchecked")
    @GET
    @Path("/address/{address}/{limit}")
    public String getBlocks(@PathParam("address") String address, @PathParam("limit") int limit) {

        //CHECK ADDRESS
        if (!Crypto.getInstance().isValidAddress(address)) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_ADDRESS);
        }

        //CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletKeysExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        //CHECK ACCOUNT IN WALLET
        Account account = Controller.getInstance().getWalletAccountByAddress(address);
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
        }

        JSONArray array = new JSONArray();
        for (Block.BlockHead block : Controller.getInstance().getLastWalletBlocks(account, limit)) {
            array.add(block.toJson());
        }

        return array.toJSONString();
    }

    @GET
    @Path("/first")
    public String getFirstBlock() {
        return new GenesisBlock().toJson().toJSONString();
    }

    @GET
    @Path("/child/{signature}")
    public String getChild(@PathParam("signature") String signature) {
        //DECODE SIGNATURE
        byte[] signatureBytes;
        try {
            signatureBytes = Base58.decode(signature);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
        }

        Block block = Controller.getInstance().getBlock(signatureBytes);

        //CHECK IF BLOCK EXISTS
        if (block == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
        }

        Block child = block.getChild(DCSet.getInstance());

        //CHECK IF CHILD EXISTS
        if (child == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
        }

        return child.toJson().toJSONString();
    }

    @GET
    @Path("/generatingbalance")
    public String getGeneratingBalance() {
        long generatingBalance = Controller.getInstance().getNextBlockGeneratingBalance();
        return String.valueOf(generatingBalance);
    }

    @GET
    @Path("/generatingbalance/{signature}")
    public String getGeneratingBalance(@PathParam("signature") String signature) {
        //DECODE SIGNATURE
        byte[] signatureBytes;
        try {
            signatureBytes = Base58.decode(signature);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_SIGNATURE);
        }

        Block block = Controller.getInstance().getBlock(signatureBytes);

        //CHECK IF BLOCK EXISTS
        if (block == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_BLOCK_HEIGHT);
        }

        long generatingBalance = block.getForgingValue();
        return String.valueOf(generatingBalance);
    }

    @GET
    @Path("/orphanto/{height}")
    public String orphanTo(@PathParam("height") int heightTo, @QueryParam("password") String password) {

        APIUtils.askAPICallAllowed(password, "GET blocks/orphanto/", request, true);

        Controller.getInstance().setOrphanTo(heightTo);

        return "OK";
    }

    // get blocks/testforge/7KFgZFbJ6sKxoT2y45agHqJS7XLeXryqec
    @GET
    @Path("/testforge/{address}")
    public String testForge(@PathParam("address") String address) {

        Account account = new Account(address);

        DCSet dcSet = DCSet.getInstance();
        AddressForging map = dcSet.getAddressForging();
        int count = 0;
        Integer seqNo = 0;
        do {
            Fun.Tuple2<String, Integer> key = new Fun.Tuple2<String, Integer>(address, seqNo);
            Fun.Tuple3<Integer, Integer, Integer> item = map.get(key);
            if (item == null)
                break;

            count++;
            seqNo = item.a;
        } while (true);

        BigDecimal defaultAmount = dcSet.getAssetBalanceMap().getDefaultValue(Bytes.concat(account.getShortAddressBytes(), Longs.toByteArray(AssetCls.ERA_KEY))).a.b;
        return "count: " + count + ", forged: " + account.getBalance(dcSet, AssetCls.ERA_KEY, Account.BALANCE_POS_OWN).b.subtract(defaultAmount).toPlainString();

    }

}
