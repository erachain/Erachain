package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.library.MButton;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.models.FundTokensComboBoxModel;
import org.erachain.lang.Lang;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//public class PersonConfirm extends JDialog { // InternalFrame  {
public class DepositExchange extends JPanel {

    // private JComboBox<Account> accountLBox;

    private static final Logger LOGGER = LoggerFactory.getLogger(DepositExchange.class);

    private static final long serialVersionUID = 2717571093561259483L;
    private MButton jButtonHistory;
    private MButton jButton_getDetails;
    private JComboBox<Account> jComboBox_YourAddress;
    public JComboBox<AssetCls> cbxAssets;
    private JLabel jLabel_Address;
    private JLabel jLabel_Adress_Check;
    private JLabel jLabel_Asset;
    private JLabel jLabel_Details;
    private JTextField jTextField_Details;
    private JLabel jTextField_Details_Check;
    private JLabel jLabel_YourAddress;
    private JTextField jTextField_Address = new JTextField();

    public DepositExchange(AssetCls asset, Account account) {

        initComponents(asset, account);
        this.setVisible(true);
    }

    private void refreshReceiverDetails(String text, JLabel details) {
        // CHECK IF RECIPIENT IS VALID ADDRESS
        details.setText("<html>" + text + "</html>");

    }

    public void onGoClick() {

        jButton_getDetails.setEnabled(false);
        jTextField_Details.setText("");
        jTextField_Details_Check.setText(Lang.getInstance().translate("wait"));

        // http://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
        //String url_string = "https://api.face2face.cash/apipay/index.json";
        String urlGetRate = "https://api.face2face.cash/apipay/get_rate.json/10/9/1";
        String urlGetHistory = "https://api.face2face.cash/apipay/history.json/ERA/78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5";
        String urlGetDetailsTest = "https://api.face2face.cash/apipay/get_uri_in.json/2/3/12/78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5/1000";

        JSONObject jsonObject;

        String urlGetDetails = "https://api.face2face.cash/apipay/get_uri_in.json/2/";

        AssetCls asset = (AssetCls) cbxAssets.getSelectedItem();
        switch ((int)asset.getKey()) {
            case 12:
                urlGetDetails += "3/12/" + jTextField_Address.getText() + "/0.1"; // BTC -> eBTC
                break;
            case 2:
                urlGetDetails += "3/10/" + jTextField_Address.getText() + "/0.1"; // BTC -> COMPU
                break;
            case 95:
                urlGetDetails += "3/13/" + jTextField_Address.getText() + "/0.1"; // BTC -> eUSD
                break;
            default:
                urlGetDetails += "3/10/" + jTextField_Address.getText() + "/0.1"; // BTC -> COMPU
        }

        String inputText = "";
        try {

            // CREATE CONNECTION
            URL url = new URL(urlGetDetails);
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
            while ((inputLine = bufferedReader.readLine()) != null)
                inputText += inputLine;
            bufferedReader.close();

            jsonObject = (JSONObject) JSONValue.parse(inputText);

        } catch (Exception e) {
            jsonObject = null;
            inputText = "";
        }

        if (jsonObject != null && jsonObject.containsKey("addr_in")) {
            if (BlockChain.DEVELOP_USE) {
                jLabel_Adress_Check.setText("<html>" + StrJSonFine.convert(jsonObject) + "</html>");
            }

            String rate = jsonObject.get("rate").toString();
            String bal = jsonObject.get("bal").toString();

            LOGGER.debug(StrJSonFine.convert(jsonObject));
            String help;

            String incomeAssetName = "bitcoins";
            asset = (AssetCls) cbxAssets.getSelectedItem();
            switch ((int)asset.getKey()) {
                case 2:
                    help = Lang.getInstance().translate("Transfer <b>%1</b> to this address for buy")
                            .replace("%1", incomeAssetName) + " <b>COMPU</B>"
                        + " " + Lang.getInstance().translate("by rate") + ": <b>" + rate + "</b>"
                        + ", " + Lang.getInstance().translate("max buy amount") + ": <b>" + bal + "</b> COMPU";
                    break;
                default:
                    help = Lang.getInstance().translate("Transfer <b>%1</B> to this address for deposit your account on Exchange")
                            .replace("%1", incomeAssetName);
            }

            jTextField_Details.setText(jsonObject.get("addr_in").toString());
            jTextField_Details_Check.setText("<html>" + help + "</html>");

        } else {
            jLabel_Adress_Check.setText("<html>" + inputText + "</html>");
            jTextField_Details.setText("");
            jTextField_Details_Check.setText(Lang.getInstance().translate("error"));
        }

        jButton_getDetails.setEnabled(true);

    }

    private void initComponents(AssetCls asset_in, Account account) {

        AssetCls asset;
        if (asset_in == null) {
            asset = Controller.getInstance().getAsset(2l);
        } else {
            asset = asset_in;
        }

        GridBagConstraints gridBagConstraints;

        //paneAssetInfo = new JScrollPane();
        jLabel_YourAddress = new JLabel();
        jComboBox_YourAddress = new JComboBox<>();
        jLabel_Address = new JLabel();
        jLabel_Asset = new JLabel();

        jLabel_Adress_Check = new JLabel();
        jLabel_Details = new JLabel();
        jTextField_Details = new JTextField();
        jTextField_Details_Check = new JLabel();

        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[]{0, 9, 0, 9, 0, 9, 0};
        layout.rowHeights = new int[]{0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0};
        //getContentPane().setLayout(layout);
        this.setLayout(layout);

        int gridy = 0;
        jLabel_YourAddress.setText(Lang.getInstance().translate("Your account") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(21, 27, 0, 0);
        add(jLabel_YourAddress, gridBagConstraints);

        jComboBox_YourAddress = new JComboBox<Account>(new AccountsComboBoxModel());
        jTextField_Address.setText(((Account) jComboBox_YourAddress.getSelectedItem()).getAddress());

        jComboBox_YourAddress.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                jTextField_Address.setText(((Account) jComboBox_YourAddress.getSelectedItem()).getAddress());

            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(21, 0, 0, 13);
        add(jComboBox_YourAddress, gridBagConstraints);

        ////////////////
        jLabel_Address.setText(Lang.getInstance().translate("Account to Deposit") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 27, 0, 0);
        add(jLabel_Address, gridBagConstraints);

        if (account == null) {
            jLabel_Adress_Check.setText(Lang.getInstance().translate("Insert Deposit Account"));
        } else {
            jTextField_Address.setText(account.getAddress());
        }

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(jTextField_Address, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        add(jLabel_Adress_Check, gridBagConstraints);

        gridy++;
        /////////////// ASSET
        jLabel_Asset.setText(Lang.getInstance().translate("Asset") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        // gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        gridBagConstraints.insets = new Insets(21, 27, 0, 0);
        add(jLabel_Asset, gridBagConstraints);

        GridBagConstraints favoritesGBC = new GridBagConstraints();
        favoritesGBC.insets = new Insets(21, 0, 0, 13);
        favoritesGBC.fill = GridBagConstraints.HORIZONTAL;
        favoritesGBC.anchor = GridBagConstraints.LINE_END;
        favoritesGBC.gridwidth = 3;
        favoritesGBC.gridx = 2;
        favoritesGBC.gridy = gridy;

        cbxAssets = new JComboBox<AssetCls>(new FundTokensComboBoxModel());
        this.add(cbxAssets, favoritesGBC);

        JLabel detailsHead = new JLabel();

        cbxAssets.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    AssetCls asset = (AssetCls) cbxAssets.getSelectedItem();
                    //paneAssetInfo.setViewportView(new AssetInfo(asset, false));

                    jTextField_Details.setText("");
                    jTextField_Details_Check.setText("");

                    switch ((int)asset.getKey()) {
                        case 2:
                            jLabel_Details.setText(Lang.getInstance().translate("Bitcoin Address for buy") + ":");
                            refreshReceiverDetails(Lang.getInstance().translate("Payment Details") +
                                            " " + Lang.getInstance().translate("for buy") + " COMPU",
                                    detailsHead);
                            break;
                        default:
                            jLabel_Details.setText(Lang.getInstance().translate("Bitcoin Address for deposit") + ":");
                            refreshReceiverDetails(Lang.getInstance().translate("Payment Details") +
                                            " " + Lang.getInstance().translate("for deposit") + " bitcoins",
                                    detailsHead);
                    }
                }
            }
        });

        //////////////// BUTTONS

        gridy += 3;

        jButton_getDetails = new MButton(Lang.getInstance().translate("Get Payment Details"), 2);
        jButton_getDetails.setToolTipText("");
        jButton_getDetails.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onGoClick();
            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        //gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new Insets(1, 0, 29, 0);
        add(jButton_getDetails, gridBagConstraints);

        ////////////// DETAILS
        gridy += 3;


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = ++gridy;
        //gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(detailsHead, gridBagConstraints);
        //detailsHead.setHorizontalAlignment(JTextField.LEFT);
        detailsHead.setText(Lang.getInstance().translate("Payment Details"));

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 27, 0, 0);
        add(jLabel_Details, gridBagConstraints);
        jLabel_Details.setHorizontalAlignment(JTextField.LEFT);
        jLabel_Details.setText(Lang.getInstance().translate("Bitcoin Address for deposit") + ":");

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(jTextField_Details, gridBagConstraints);
        jTextField_Details.setEditable(false);
        jTextField_Details.setToolTipText("");
        jTextField_Details.setText(""); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        add(jTextField_Details_Check, gridBagConstraints);

        //////////////////////////
        JLabel jText_History = new JLabel();

        gridy += 3;

        jButtonHistory = new MButton(Lang.getInstance().translate("See Deposit History"), 2);
        jButtonHistory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                jText_History.setText(showHistory((AssetCls) cbxAssets.getSelectedItem(),
                        jTextField_Address.getText(), jLabel_Adress_Check));

            }
        });


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        //gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new Insets(1, 0, 29, 0);

        add(jButtonHistory, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        //gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(jText_History, gridBagConstraints);

    }

    public static String showHistory(AssetCls asset, String address, JLabel tip) {
        JSONObject jsonObject;

        // [TOKEN]/[ADDRESS]
        String urlGetDetails = "https://api.face2face.cash/apipay/history.json/";

        switch ((int) asset.getKey()) {
            case 12:
                urlGetDetails += "@BTC/"; // BTC -> eBTC
                break;
            case 14:
                urlGetDetails += "@ETH/"; // BTC -> eBTC
                break;
            case 92:
                urlGetDetails += "@RUB/"; // BTC -> eUSD
                break;
            case 95:
                urlGetDetails += "@USD/"; // BTC -> eUSD
                break;
            default:
                urlGetDetails += "COMPU/"; // BTC -> COMPU
        }

        urlGetDetails += address;


        String inputText = "";
        try {

            // CREATE CONNECTION
            URL url = new URL(urlGetDetails);
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
            while ((inputLine = bufferedReader.readLine()) != null)
                inputText += inputLine;
            bufferedReader.close();

            jsonObject = (JSONObject) JSONValue.parse(inputText);

        } catch (Exception e) {
            jsonObject = null;
            inputText = "";
        }

        LOGGER.debug(inputText);

        if (jsonObject != null) {
            if (jsonObject.containsKey("deal")) {
                if (BlockChain.DEVELOP_USE) {
                    tip.setText("<html>" + StrJSonFine.convert(jsonObject) + "</html>");
                }

                String resultText = "<html>";
                JSONArray unconfirmed = (JSONArray) jsonObject.get("unconfirmed");
                if (!unconfirmed.isEmpty()) {
                    resultText += "<h3>" + Lang.getInstance().translate("Pending") + "</h3>";
                    /**
                     *  [{"abbrev": "COMPU", "id": 10},
                     *      "0.01", "5zAuwca5nvAGboRNagXKamtZCHvLdBs5Zii1Sb51wMMigFccQnPzVdASQSWftmjotaazZKWKZLd4DtaEN5iVJ5qN",
                     *      0, 0, 2, "2019-06-21 16:35:10", "7KJjTKrj7Zmm7cYJANWHmLfmNPoZbwmbiy"]
                     */
                    for (Object item : unconfirmed) {
                        try {
                            JSONArray array = (JSONArray) item;
                            JSONObject curr = (JSONObject) array.get(0);
                            resultText += array.get(1) + " " + curr.get("abbrev")
                                    + " - " + array.get(6)
                                    + "  <span style='font-size:0.8em'>" + array.get(2) + "</span><br>";

                        } catch (Exception e) {
                            resultText += item.toString() + "<br>";
                        }
                    }
                }

                JSONArray in_process = (JSONArray) jsonObject.get("in_process");
                if (!in_process.isEmpty()) {
                    resultText += "<h3>" + Lang.getInstance().translate("In Process") + "</h3>";
                    /**
                     * ???
                     */
                    for (Object item : in_process) {
                        try {
                            JSONArray array = (JSONArray) item;
                            JSONObject curr = (JSONObject) array.get(0);
                            resultText += array.get(1) + " " + curr.get("abbrev")
                                    + " : " + array.get(6)
                                    + " : " + array.get(2) + "<br>";

                        } catch (Exception e) {
                            resultText += item.toString() + "<br>";
                        }
                    }
                }

                JSONArray done = (JSONArray) jsonObject.get("done");
                if (!done.isEmpty()) {
                    resultText += "<h3>" + Lang.getInstance().translate("Done") + "</h3>";
                    /**
                     * {"acc": "7KJjTKrj7Zmm7cYJANWHmLfmNPoZbwmbiy", "stasus": "ok",
                     * "curr_in": {"abbrev": "COMPU", "id": 10},
                     * "curr_out": {"abbrev": "COMPU", "id": 10},
                     * "pay_out": {"status": null, "info": null, "created_ts": 1561122345.0, "amo_in": 0.02, "amo_taken": 0.01890641, "tax_mess": null,
                     *      "vars": {"payment_id": "4g1bxW9aA8moR1vEwefmma6B3DpYawtrL2NaBHAaPFsibPgs8UAVZnJ6zytWew8KXNCyt28oL76EYKbJRasjfqUQ", "status": "success"},
                     *      "created_on": "2019-06-21 16:05:45", "txid": "4g1bxW9aA8moR1vEwefmma6B3DpYawtrL2NaBHAaPFsibPgs8UAVZnJ6zytWew8KXNCyt28oL76EYKbJRasjfqUQ",
                     *      "amount": 0.01888609, "amo_to_pay": 0.0, "amo_gift": 2.032e-05, "id": 77, "amo_partner": 0.0
                     *      },
                     *  "amount_in": 0.02, "created": "2019-06-21 16:03:10", "confitmations": 1,
                     *  "txid": "3C82efTYLiPjDgPpJqgeU8Kp5b9Bwe2aKsc1aXjtnXHhxpo9QbuuNrr3juhkEhcBTaV7fxeUynYdkPFSuXb6trU5"},
                     */
                    for (Object item : done) {
                        try {
                            JSONObject json = (JSONObject) item;
                            JSONObject curr_in = (JSONObject) json.get("curr_in");
                            JSONObject curr_out = (JSONObject) json.get("curr_out");
                            JSONObject pay_out = (JSONObject) json.get("pay_out");

                            resultText += json.get("amount_in") + " " + curr_in.get("abbrev")
                                    + " :: " + pay_out.get("amo_taken") + " " + curr_out.get("abbrev")
                                    + " - " + pay_out.get("created_on")
                                    + "  <span style='font-size:0.8em'>" + pay_out.get("txid") + "</span><br>";
                        } catch (Exception e) {
                            resultText += item.toString() + "<br>";
                        }
                    }
                }

                if (unconfirmed.isEmpty() && in_process.isEmpty() && done.isEmpty()) {
                    resultText += "<h3>" + Lang.getInstance().translate("Not Found") + "</h3>";
                }
                resultText += "</html>";
                return resultText;

            } else {
                LOGGER.debug(inputText);
                return inputText;
            }
        } else {
            return inputText;
        }
    }

}
