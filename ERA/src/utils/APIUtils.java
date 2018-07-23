package utils;

import java.awt.GraphicsEnvironment;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.swing.JOptionPane;
import javax.ws.rs.WebApplicationException;

import core.web.ProfileHelper;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple3;

import api.ApiClient;
import api.ApiErrorFactory;
import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.transaction.R_Send;
import core.transaction.Transaction;
import core.web.ServletUtils;
import datachain.DCSet;
import gui.MainFrame;
import gui.PasswordPane;
import gui.library.Issue_Confirm_Dialog;
import gui.transaction.Send_RecordDetailsFrame;
import lang.Lang;
import settings.Settings;
//import test.records.TestRecTemplate;

public class APIUtils {
    
    static Logger LOGGER = Logger.getLogger(APIUtils.class.getName());
    
    public static String errorMess(int error, String message) {
        return "{ \"error\":" + error + ", \"message\": \"" + message + "\" }";
    }
    
    public static void disallowRemote(HttpServletRequest request, String ipAddress) throws WebApplicationException {

        // SEE in api.ApiService.ApiService -
        //      and in ProfileHelper.getActiveProfileOpt()
        
        //if (ServletUtils.isRemoteRequest(request, ipAddress)) {
        //    for (String ip: Settings.getInstance().getRpcAllowed()) {
        //        if (ip.equals(ipAddress))
        //            return;
        //    }

        //    throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_API_CALL_FORBIDDEN_BY_USER);
        //}
    }
    
    public static void askAPICallAllowed(String password, final String messageToDisplay, HttpServletRequest request)
            throws WebApplicationException {
        // CHECK API CALL ALLOWED
        
        try {
            
            String ipAddress = ServletUtils.getRemoteAddress(request);
            disallowRemote(request, ipAddress);
            
            if (!gui.Gui.isGuiStarted()) {
                
                if (!ServletUtils.isRemoteRequest(request, ipAddress)) {
                    if (Controller.getInstance().isWalletUnlocked())
                        return;
                    if (password != null && password.length() > 3
                            && Controller.getInstance().unlockOnceWallet(password))
                        return;
                } else {
                    if (password != null && password.length() > 6
                            && Controller.getInstance().unlockOnceWallet(password))
                        return;
                }
                
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
                
            } else {
                if (password != null && password.length() > 6
                        && Controller.getInstance().unlockOnceWallet(password))
                    return;
            }
            
            int answer = Controller.getInstance().checkAPICallAllowed(messageToDisplay, request);
            
            if (answer == JOptionPane.NO_OPTION) {
                throw ApiErrorFactory.getInstance()
                        .createError(ApiErrorFactory.ERROR_WALLET_API_CALL_FORBIDDEN_BY_USER);
                
            } else if (!GraphicsEnvironment.isHeadless() && (Settings.getInstance().isGuiEnabled())) {
                if (!BlockChain.DEVELOP_USE || answer != ApiClient.SELF_CALL
                        || !Controller.getInstance().isWalletUnlocked()) {
                    // if (true) {
                    password = PasswordPane.showUnlockWalletDialog(MainFrame.getInstance());
                    // password =
                    // PasswordPane.showUnlockWalletDialog(Main_Panel.getInstance());
                    // password =
                    // PasswordPane.showUnlockWalletDialog(Gui.getInstance());
                    // password = PasswordPane.showUnlockWalletDialog(new
                    // DebugTabPane());
                    
                    if (password.length() > 0 && Controller.getInstance().unlockWallet(password)) {
                        return;
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid password", "Unlock Wallet",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    return;
                }
            }
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
            
        } catch (Exception e) {
            if (e instanceof WebApplicationException) {
                throw (WebApplicationException) e;
            }
            LOGGER.error(e.getMessage(), e);
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_UNKNOWN);
        }
        
    }
        
    public static String processPayment(String password, String sender, String feePowStr, String recipient,
            String assetKeyString, String amount, String x, HttpServletRequest request, JSONObject jsonObject) {
        
        // PARSE AMOUNT
        AssetCls asset;
        
        if (assetKeyString == null) {
            asset = Controller.getInstance().getAsset(AssetCls.FEE_KEY);
        } else {
            try {
                asset = Controller.getInstance().getAsset(new Long(assetKeyString));
            } catch (Exception e) {
                throw ApiErrorFactory.getInstance().createError(Transaction.ITEM_ASSET_NOT_EXIST);
            }
        }
        
        if (asset == null)
            throw ApiErrorFactory.getInstance().createError(Transaction.ITEM_ASSET_NOT_EXIST);
        
        // PARSE AMOUNT
        BigDecimal bdAmount;
        try {
            bdAmount = new BigDecimal(amount);
            bdAmount = bdAmount.setScale(asset.getScale());
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_AMOUNT);
        }
        
        // PARSE FEE POWER
        int feePow;
        try {
            feePow = Integer.parseInt(feePowStr);
        } catch (Exception e) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_FEE_POWER);
        }
        
        // CHECK ADDRESS
        if (!Crypto.getInstance().isValidAddress(sender)) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_MAKER_ADDRESS);
        }
        
        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }
        
        String title = null;
        byte[] message = null;
        boolean istextmessage = true;
        boolean encrypt = false;
        if (jsonObject != null) {
            if (jsonObject.containsKey("title")) {
                title = (String) jsonObject.get("title");
                if (title.getBytes(StandardCharsets.UTF_8).length > 256) {
                    throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_HEAD_LENGTH);
                }
            }
            
            if (jsonObject.containsKey("message")) {
                String message_in = (String) jsonObject.get("message");
                message = message_in.getBytes(StandardCharsets.UTF_8);
                if (message.length > BlockChain.MAX_REC_DATA_BYTES) {
                    throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_DESCRIPTION_LENGTH);
                }
            }
            
            if (jsonObject.containsKey("istextmessage")) {
                try {
                    istextmessage = (boolean) jsonObject.get("istextmessage");
                } catch (Exception e) {
                    throw ApiErrorFactory.getInstance().createError(api.ApiErrorFactory.ERROR_JSON);
                }
            }
            if (jsonObject.containsKey("encrypt")) {
                try {
                    encrypt = (boolean) jsonObject.get("encrypt");
                } catch (Exception e) {
                    throw ApiErrorFactory.getInstance().createError(api.ApiErrorFactory.ERROR_JSON);
                }
            }
        }
        
        byte[] isText = istextmessage ? new byte[] { 1 } : new byte[] { 0 };
        byte[] isEncrypted = encrypt ? new byte[] { 1 } : new byte[] { 0 };
        
        // TRU UNLOCK
        askAPICallAllowed(password, "POST payment\n" + x, request);
        
        // CHECK WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
        }
        
        // GET ACCOUNT
        PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(sender);
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_WALLET_ADDRESS);
        }
        
        // TODO R_Send insert!
        Integer result;
        // SEND ASSET PAYMENT
        Transaction transaction = Controller.getInstance().r_Send(account, feePow, new Account(recipient),
                asset.getKey(DCSet.getInstance()), bdAmount, title, message, isText, isEncrypted);
        
        boolean confirmed = true;
        if (gui.Gui.isGuiStarted()) {
            String Status_text = "<HTML>" + Lang.getInstance().translate("Size") + ":&nbsp;"
                    + transaction.viewSize(true) + " Bytes, ";
            Status_text += "<b>" + Lang.getInstance().translate("Fee") + ":&nbsp;" + transaction.getFee().toString()
                    + " COMPU</b><br></body></HTML>";
            
            Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(MainFrame.getInstance(), true,
                    Lang.getInstance().translate("Send Mail"), (600), (450), Status_text,
                    Lang.getInstance().translate("Confirmation Transaction"));
            Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((R_Send) transaction);
            
            // ww.jTabbedPane1.setVisible(false);
            dd.jScrollPane1.setViewportView(ww);
            dd.setLocationRelativeTo(null);
            dd.setVisible(true);
            
            // JOptionPane.OK_OPTION
            confirmed = dd.isConfirm;
            
        }
        
        if (confirmed) {
            
            result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, false);
            
            if (result == Transaction.VALIDATE_OK)
                return transaction.toJson().toJSONString();
            else {
                
                // Lang.getInstance().translate(OnDealClick.resultMess(result.getB()));
                throw ApiErrorFactory.getInstance().createError(result);
                
            }
            
        }
        return "error";
    }
    
    public static Tuple3<JSONObject, PrivateKeyAccount, Integer> postPars(HttpServletRequest request, String x) {
        
        try {
            
            // READ JSON
            JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
            String creator;
            if (jsonObject.containsKey("creator")) {
                creator = (String) jsonObject.get("creator");
            } else if (jsonObject.containsKey("maker")) {
                creator = (String) jsonObject.get("maker");
            } else {
                creator = (String) jsonObject.get("sender");
            }
            
            String password = (String) jsonObject.get("password");
            
            // PARSE FEE POWER
            int feePow;
            try {
                feePow = (int) (long) jsonObject.get("feepow");
            } catch (Exception e0) {
                try {
                    String feePowStr = (String) jsonObject.get("feepow");
                    feePow = Integer.parseInt(feePowStr);
                } catch (Exception e) {
                    throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_FEE_POWER);
                }
            }
            
            // CHECK ADDRESS
            if (!Crypto.getInstance().isValidAddress(creator)) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_MAKER_ADDRESS);
            }
            
            // check this up here to avoid leaking wallet information to remote
            // user
            // full check is later to prompt user with calculated fee
            
            // CHECK IF WALLET EXISTS
            if (!Controller.getInstance().doesWalletExists()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
            }
            
            // TRY UNLOCK
            askAPICallAllowed(password, x, request);
            
            // CHECK WALLET UNLOCKED
            if (!Controller.getInstance().isWalletUnlocked()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
            }
            
            // GET ACCOUNT
            PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(creator);
            if (account == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_MAKER_ADDRESS);
            }
            
            return new Tuple3<JSONObject, PrivateKeyAccount, Integer>(jsonObject, account, feePow);
            
        } catch (NullPointerException | ClassCastException e) {
            // JSON EXCEPTION
            // LOGGER.info(e);
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }
        
    }
    
}
