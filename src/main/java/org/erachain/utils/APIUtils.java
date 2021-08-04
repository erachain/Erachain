package org.erachain.utils;

import org.erachain.api.ApiClient;
import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.ServletUtils;
import org.erachain.gui.Gui;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.transaction.Send_RecordDetailsFrame;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import javax.ws.rs.WebApplicationException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
//import test.org.erachain.records.TestRecTemplate;

public class APIUtils {
    
    static Logger LOGGER = LoggerFactory.getLogger(APIUtils.class.getName());


    public static String openUrl(String command) {

        String inputText = null;
        try {
            // CREATE CONNECTION
            URL url = new URL(command);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // EXECUTE
            int resCode = connection.getResponseCode();

            //READ RESULT
            InputStream stream;
            if (resCode == 400) {
                stream = connection.getErrorStream();
            } else {
                stream = connection.getInputStream();
            }

            InputStreamReader isReader = new InputStreamReader(stream, "UTF-8");
            //String result = new BufferedReader(isReader).readLine();

            BufferedReader bufferedReader = new BufferedReader(isReader);
            String inputLine;
            inputText = "";
            while ((inputLine = bufferedReader.readLine()) != null)
                inputText += inputLine;
            bufferedReader.close();

        } catch (Exception e) {

        }

        return inputText;

    }

    public static String errorMess(int error, String message) {
        return "{ \"error\":" + error + ", \"message\": \"" + message + "\" }";
    }

    public static String errorMess(int error, String message, Transaction transaction) {

        String errorMsg = "{ \"error\":" + error + ", \"message\": \"" + message + "\"";
        if (transaction != null && transaction.errorValue != null) {
            errorMsg += ", \"value\":\"" + transaction.errorValue + "\"";
        }
        return errorMsg + " }";
    }

    public static String errorMess(int error, String message, Transaction transaction, String lang) {

        JSONObject langObj = Lang.getInstance().getLangJson(lang);

        String errorMsg = "{ \"error\":" + error + ", \"message\": \"" +
                (langObj == null ? message : Lang.T(message, langObj)) + "\"";
        if (transaction != null && transaction.errorValue != null) {
            errorMsg += ", \"value\":\"" + transaction.errorValue + "\"";
        }
        return errorMsg + " }";
    }

    public static void disallowRemote(HttpServletRequest request, String ipAddress) throws WebApplicationException {

        // SEE in org.erachain.api.ApiService.ApiService -
        //      and in ProfileHelper.getActiveProfileOpt()

        //if (ServletUtils.isRemoteRequest(request, ipAddress)) {
        //    for (String ip: Settings.getInstance().getRpcAllowed()) {
        //        if (ip.equals(ipAddress))
        //            return;
        //    }

        //    throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_API_CALL_FORBIDDEN_BY_USER);
        //}
    }
    
    public static void askAPICallAllowed(String password, final String messageToDisplay, HttpServletRequest request, boolean once)
            throws WebApplicationException {
        // CHECK API CALL ALLOWED

        try {
            
            String ipAddress = ServletUtils.getRemoteAddress(request);
            // now not used - disallowRemote(request, ipAddress);

            int min_length;
            boolean noGUI = !Gui.isGuiStarted();
            if (noGUI) {

                // если кошелек открыт на постоянно то не спрашиваем пароль
                if (Controller.getInstance().doesWalletExists() && Controller.getInstance().isWalletUnlockedForRPC())
                    return;

                if (!ServletUtils.isRemoteRequest(request, ipAddress)) {
                    if (messageToDisplay.equals("GET core/stop"))
                        return;
                    min_length = 4;
                } else {
                    min_length = 8;
                }
            } else {
                min_length = 8;
            }

            //min_length = 0;
            if (BlockChain.TEST_MODE)
                min_length = 0;

            if (password != null) {
                JSONObject errorJson;
                if (password.length() <= min_length) {
                    throw ApiErrorFactory.getInstance()
                            .createError(ApiErrorFactory.ERROR_WALLET_PASSWORD_SO_SHORT,
                                    "need > " + min_length);
                }

                if (once) {
                    if (Controller.getInstance().unlockOnceWallet(password))
                        return;
                } else {
                    Controller.getInstance().setSecondsToUnlock(-1);
                    if (Controller.getInstance().unlockWallet(password))
                        return;
                }

                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);

            } else if (noGUI) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
            }

            // GUI ON and not PASSWORD
            int answer = Controller.getInstance().checkAPICallAllowed(messageToDisplay, request);
            
            if (answer == JOptionPane.NO_OPTION) {
                throw ApiErrorFactory.getInstance()
                        .createError(ApiErrorFactory.ERROR_WALLET_API_CALL_FORBIDDEN_BY_USER);
            } else if (answer == ApiClient.SELF_CALL) {
                // STOP ACCESS from CONCOLE without PASSWORD or UNLOCKED wallet
                if (Controller.getInstance().isWalletUnlocked())
                    return;
            } else {
                //password = PasswordPane.showUnlockWalletDialog(MainFrame.getInstance());
                // password =
                // PasswordPane.showUnlockWalletDialog(MainPanel.getInstance());
                //Gui.getInstance().bringtoFront();
                password = PasswordPane.showUnlockWalletDialog(Gui.getInstance());
                //Gui.getInstance().bringtoFront();
                // password = PasswordPane.showUnlockWalletDialog(new
                // DebugTabPane());

                if (password.length() > 0) {
                    if (once) {
                        if (Controller.getInstance().unlockOnceWallet(password))
                            return;
                    } else {
                        Controller.getInstance().setSecondsToUnlock(-1);
                        if (Controller.getInstance().unlockWallet(password))
                            return;
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid password", "Unlock Wallet",
                            JOptionPane.ERROR_MESSAGE);
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
        boolean istext = true;
        boolean encrypt = false;
        if (jsonObject != null) {

            if (jsonObject.containsKey("istext")) {
                try {
                    istext = (boolean) jsonObject.get("istext");
                } catch (Exception e) {
                    throw ApiErrorFactory.getInstance().createError(org.erachain.api.ApiErrorFactory.ERROR_JSON);
                }
            }
            if (jsonObject.containsKey("encrypt")) {
                try {
                    encrypt = (boolean) jsonObject.get("encrypt");
                } catch (Exception e) {
                    throw ApiErrorFactory.getInstance().createError(org.erachain.api.ApiErrorFactory.ERROR_JSON);
                }
            }
        }
        
        byte[] isText = istext ? new byte[] { 1 } : new byte[] { 0 };
        byte[] isEncrypted = encrypt ? new byte[] { 1 } : new byte[] { 0 };
        
        // TRU UNLOCK
        askAPICallAllowed(password, "POST payment\n" + x, request, true);
        
        // CHECK WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
        }
        
        // GET ACCOUNT
        PrivateKeyAccount account = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender);
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_WALLET_ADDRESS);
        }
        
        // TODO RSend insert!
        Integer result;
        // SEND ASSET PAYMENT
        Transaction transaction = Controller.getInstance().r_Send(account, null, feePow, new Account(recipient),
                asset.getKey(), bdAmount, title,
                message, isText, isEncrypted, 0);
        
        int confirmed = IssueConfirmDialog.CONFIRM;
        if (Gui.isGuiStarted()) {
            String Status_text = "";
            IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                    Lang.T("Send Mail"), (600), (450), Status_text,
                    Lang.T("Confirmation Transaction"));
            Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((RSend) transaction);

            // ww.jTabbedPane1.setVisible(false);
            confirmDialog.jScrollPane1.setViewportView(ww);
            confirmDialog.setLocationRelativeTo(null);
            confirmDialog.setVisible(true);

            // JOptionPane.OK_OPTION
            confirmed = confirmDialog.isConfirm;

        }

        if (confirmed > 0) {

            result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK,
                    confirmed == IssueConfirmDialog.TRY_FREE, false);

            if (result == Transaction.VALIDATE_OK)
                return transaction.toJson().toJSONString();
            else {

                // Lang.T(OnDealClick.resultMess(result.getB()));
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
                feePow = (int) (long) jsonObject.getOrDefault("feepow", jsonObject.get("feePow"));
            } catch (Exception e0) {
                try {
                    String feePowStr = (String) jsonObject.getOrDefault("feepow", jsonObject.get("feePow"));
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
            askAPICallAllowed(password, x, request, true);
            
            // CHECK WALLET UNLOCKED
            if (!Controller.getInstance().isWalletUnlocked()) {
                throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
            }
            
            // GET ACCOUNT
            PrivateKeyAccount account = Controller.getInstance().getWalletPrivateKeyAccountByAddress(creator);
            if (account == null) {
                throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_MAKER_ADDRESS);
            }

            return new Tuple3<JSONObject, PrivateKeyAccount, Integer>(jsonObject, account, feePow);

        } catch (NullPointerException | ClassCastException e) {
            // JSON EXCEPTION
            // logger.error(e.getMessage());
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
        }

    }

    public static Fun.Tuple2<PrivateKeyAccount, byte[]> postIssueRawItem(HttpServletRequest request, String x,
                                                                         Account creator, String password, String walletMess) {

        byte[] raw;
        try {
            raw = Base58.decode(x);
        } catch (Exception e0) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_RAW_DATA);
        }

        if (raw == null)
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_RAW_DATA);

        // check this up here to avoid leaking wallet information to remote
        // user
        // full check is later to prompt user with calculated fee

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        // TRY UNLOCK
        askAPICallAllowed(password, walletMess, request, true);

        // CHECK WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
        }

        // GET ACCOUNT
        PrivateKeyAccount account = Controller.getInstance().getWalletPrivateKeyAccountByAddress(creator);
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_MAKER_ADDRESS);
        }

        return new Fun.Tuple2<>(account, raw);

    }

    /**
     * короче какая-то фиггня была - прилетал блок при тестах в котром транзакции были по номерам перепуьаны
     * и ХЭШ блока не сходился с расчитываемым тут - как это могло произойти?
     * Я ловил где было не совпадение - оно было в 6 на 7 трнзакции в блоке 264590
     * потом этот блок откатился ситемой и заново пересобрался и все норм стало
     */
    public static boolean testTxSigns(int heightBlock, int seqNo, String signatureStr) {
        String peerIP = Controller.getInstance().getSynchronizer().getPeer().getAddress().getHostName();
        String txStr = APIUtils.openUrl(
                    //"http://138.68.225.51:9047/apirecords/getbynumber/"
                    "http://" + peerIP + ":" + Settings.getInstance().getWebPort() + "/apirecords/getbynumber/"
                            + heightBlock + "-" + seqNo);
            if (txStr == null) {
                Long error = null;
                LOGGER.debug(peerIP + " -- " + heightBlock + "-" + seqNo
                        + " NOT FOUND");
                //break;
            } else if (!txStr.contains(signatureStr)) {
                Long error = null;
                LOGGER.debug(peerIP + " -- " + heightBlock + "-" + seqNo
                        + " WRONG SIGNATURE");
                return false;
            } else {
                LOGGER.debug(peerIP + " -- " + heightBlock + "-" + seqNo
                        + " good!");
            }

        return true;

    }

    public static PrivateKeyAccount getPrivateKeyCreator(String creator) {

        // CHECK ADDRESS
        Fun.Tuple2<Account, String> result = Account.tryMakeAccount(creator);

        if (result.a == null) {
            throw ApiErrorFactory.getInstance().createError(result.b);
        }

        // CHECK IF WALLET EXISTS
        if (!Controller.getInstance().doesWalletExists()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
        }

        // CHECK WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_LOCKED);
        }

        // GET ACCOUNT
        PrivateKeyAccount account = Controller.getInstance().getWalletPrivateKeyAccountByAddress(creator);
        if (account == null) {
            throw ApiErrorFactory.getInstance().createError(Transaction.INVALID_WALLET_ADDRESS);
        }

        return account;
    }

}
