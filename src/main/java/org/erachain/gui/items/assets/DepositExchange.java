package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.IconPanel;
import org.erachain.gui.library.MButton;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.models.FundTokensComboBoxModel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.StrJSonFine;
import org.erachain.utils.URLViewer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//public class PersonConfirm extends JDialog { // InternalFrame  {
public class DepositExchange extends IconPanel {

    public static String NAME = "DepositExchange";
    public static String TITLE = "Deposit Exchange";

    private static final Logger LOGGER = LoggerFactory.getLogger(DepositExchange.class);

    private static final long serialVersionUID = 2717571093561259483L;
    private MButton jButtonHistory;
    private MButton jButton_getDetails;
    private JComboBox<Account> jComboBox_YourAddress;
    public JComboBox<AssetCls> cbxAssets;
    public JComboBox<AssetCls> cbxAssetsInput;
    private JLabel jLabel_Address;
    private JLabel jLabel_Adress_Check;
    private JLabel jLabel_Asset;
    private JLabel jLabel_AssetInput;
    private JLabel jLabel_Details;
    private JTextField jTextField_Details;
    private JLabel jTextField_Details_Check;
    private JLabel jLabel_YourAddress;
    private JTextField jTextField_Address = new JTextField();
    protected int step = 0;

    public DepositExchange(AssetCls asset, Account account, BigDecimal amount, AssetCls assetInput) {
        super(NAME, TITLE);

        initComponents(asset, account, amount);
        this.setVisible(true);
    }

    private void refreshReceiverDetails(String text, JLabel details) {
        // CHECK IF RECIPIENT IS VALID ADDRESS
        details.setText("<html>" + text + "</html>");

    }

    public void onGoClick(JLabel jText_Help) {

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
        AssetCls assetInput = (AssetCls) cbxAssetsInput.getSelectedItem();
        String assetInputAbbrev;
        switch ((int) assetInput.getKey()) {
            case 1:
                assetInputAbbrev = "ERA/";
                break;
            case 12:
                assetInputAbbrev = "BTC/";
                break;
            case 95:
                assetInputAbbrev = "RUB/";
                break;
            case 1114:
                assetInputAbbrev = "ZEN/";
                break;
            default:
                assetInputAbbrev = "COMPU/";
        }

        switch ((int) asset.getKey()) {
            case 1:
                urlGetDetails += "3/" + assetInputAbbrev + jTextField_Address.getText() + "/0.1"; // BTC -> ERA
                break;
            case 12:
                urlGetDetails += "3/" + assetInputAbbrev + jTextField_Address.getText() + "/0.1"; // BTC -> eBTC
                break;
            case 95:
                urlGetDetails += "3/" + assetInputAbbrev + jTextField_Address.getText() + "/0.1"; // BTC -> eUSD
                break;
            case 1114:
                urlGetDetails = "http://185.195.26.197/7pay_in/apipay/get_uri_in.json/2/";
                urlGetDetails += "7/" + assetInputAbbrev + jTextField_Address.getText() + "/10"; // ZEN -> eZEN
                break;
            default:
                urlGetDetails += "3/" + assetInputAbbrev + jTextField_Address.getText() + "/0.1"; // BTC -> COMPU
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
            if (BlockChain.TEST_MODE) {
                jLabel_Adress_Check.setText("<html>" + StrJSonFine.convert(jsonObject) + "</html>");
            }

            if (jsonObject.containsKey("wrong")) {
                jTextField_Details.setText(Lang.getInstance().translate("error"));
                jTextField_Details_Check.setText("<html><b>" + jsonObject.get("wrong") + "<b></html>");

            } else {

                String rate = jsonObject.get("rate").toString();
                String bal = jsonObject.get("bal").toString();

                LOGGER.debug(StrJSonFine.convert(jsonObject));
                String help;

                String incomeAssetName = "bitcoins";
                String incomeName = "BTC";
                String outcomeAssetName;
                asset = (AssetCls) cbxAssets.getSelectedItem();
                switch ((int) asset.getKey()) {
                    case 1:
                        outcomeAssetName = "ERA";
                        help = Lang.getInstance().translate("Transfer <b>%1</b> to this address for buy")
                                .replace("%1", incomeAssetName) + " <b>" + outcomeAssetName + "</b>"
                                + " " + Lang.getInstance().translate("by rate") + ": <b>" + rate + "</b>"
                                + ", " + Lang.getInstance().translate("max buy amount") + ": <b>" + bal + "</b> " + outcomeAssetName;
                        break;
                    case 2:
                        outcomeAssetName = "COMPU";
                        help = Lang.getInstance().translate("Transfer <b>%1</b> to this address for buy")
                                .replace("%1", incomeAssetName) + " <b>" + outcomeAssetName + "</b>"
                                + " " + Lang.getInstance().translate("by rate") + ": <b>" + rate + "</b>"
                                + ", " + Lang.getInstance().translate("max buy amount") + ": <b>" + bal + "</b> " + outcomeAssetName;
                        break;
                    case 1114:
                        outcomeAssetName = incomeAssetName = "ZEN";
                        help = Lang.getInstance().translate("Transfer <b>%1</B> to this address for deposit your account on Exchange")
                                .replace("%1", incomeAssetName);
                        break;
                    default:
                        help = Lang.getInstance().translate("Transfer <b>%1</B> to this address for deposit your account on Exchange")
                                .replace("%1", incomeAssetName);
                }

                if (jsonObject.containsKey("may_pay")) {
                    help += "<br>" + Lang.getInstance().translate("You may pay maximum") + ": " + jsonObject.get("may_pay").toString()
                            + incomeName;
                }

                help += "<br>" + Lang.getInstance().translate("Minimal payment in equivalent")
                        + " <b>" + 0.0025 + " " + incomeName + "</b>" + "<br>";

                jTextField_Details.setText(jsonObject.get("addr_in").toString());
                jTextField_Details_Check.setText("<html>" + help + "</html>");
            }

        } else {
            jLabel_Adress_Check.setText("<html>" + inputText + "</html>");
            jTextField_Details.setText("");
            jTextField_Details_Check.setText(Lang.getInstance().translate("error"));
        }

        jButton_getDetails.setEnabled(true);

        jText_Help.setText("<html><h2>5. " + Lang.getInstance().translate(
                "Transfer Assets to address below") + "</h2></html>");
        jButton_getDetails.setText(Lang.getInstance().translate("Get Payment Details"));

    }

    private void initComponents(AssetCls asset_in, Account account, BigDecimal amount) {

        AssetCls asset;
        if (asset_in == null) {
            asset = Controller.getInstance().getAsset(AssetCls.FEE_KEY);
        } else {
            asset = asset_in;
        }

        GridBagConstraints gridBagConstraints;

        //paneAssetInfo = new JScrollPane();
        jLabel_YourAddress = new JLabel();
        jComboBox_YourAddress = new JComboBox<>();
        jLabel_Address = new JLabel();
        jLabel_Asset = new JLabel();
        jLabel_AssetInput = new JLabel();

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

        JLabel jText_Title = new JLabel();

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        //gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(jText_Title, gridBagConstraints);
        jText_Title.setText("<html><h1>" + Lang.getInstance().translate("Deposit of Assets to the Exchange") + "</h1></html>");

        JLabel jText_Help = new JLabel();

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        //gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(jText_Help, gridBagConstraints);
        jText_Help.setText("<html><h2>1. " + Lang.getInstance().translate("Select Your account or insert another account in field below") + "</h2></html>");

        jLabel_YourAddress.setText(Lang.getInstance().translate("Your account") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(21, 27, 0, 0);
        add(jLabel_YourAddress, gridBagConstraints);

        jComboBox_YourAddress = new JComboBox<Account>(new AccountsComboBoxModel());
        jTextField_Address.setText(((Account) jComboBox_YourAddress.getSelectedItem()).getAddress());

        jComboBox_YourAddress.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                jTextField_Address.setText(((Account) jComboBox_YourAddress.getSelectedItem()).getAddress());

                jText_Help.setText("<html><h2>2. " + Lang.getInstance().translate(
                        "Select Asset that Your wish to receive") + "</h2></html>");

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
        jLabel_Asset.setText(Lang.getInstance().translate("Deposit") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(jLabel_Asset, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        cbxAssets = new JComboBox<AssetCls>(new FundTokensComboBoxModel(true));
        this.add(cbxAssets, gridBagConstraints);

        JLabel detailsHead = new JLabel();

        cbxAssets.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (true || e.getStateChange() == ItemEvent.SELECTED) {
                    AssetCls asset = (AssetCls) cbxAssets.getSelectedItem();
                    //paneAssetInfo.setViewportView(new AssetInfo(asset, false));

                    jTextField_Details.setText("");
                    jTextField_Details_Check.setText("");

                    jLabel_Asset.setText(Lang.getInstance().translate("To Deposit"));

                    switch ((int)asset.getKey()) {
                        case 1:
                            jLabel_Details.setText(Lang.getInstance().translate("Bitcoin Address for buy") + ":");
                            refreshReceiverDetails(Lang.getInstance().translate("Payment Details") +
                                            " " + Lang.getInstance().translate("for buy") + " ERA",
                                    detailsHead);
                            jLabel_Asset.setText(Lang.getInstance().translate("To Buy"));
                            jLabel_AssetInput.setVisible(true);
                            cbxAssetsInput.setVisible(true);
                            break;
                        case 2:
                            jLabel_Details.setText(Lang.getInstance().translate("Bitcoin Address for buy") + ":");
                            refreshReceiverDetails(Lang.getInstance().translate("Payment Details") +
                                            " " + Lang.getInstance().translate("for buy") + " COMPU",
                                    detailsHead);
                            jLabel_Asset.setText(Lang.getInstance().translate("To Buy"));
                            jLabel_AssetInput.setVisible(true);
                            cbxAssetsInput.setVisible(true);
                            break;
                        case 1114:
                            jLabel_Details.setText(Lang.getInstance().translate("Address for deposit") + " ZEN" + ":");
                            refreshReceiverDetails(Lang.getInstance().translate("Payment Details") +
                                            " " + Lang.getInstance().translate("for deposit") + " ZEN",
                                    detailsHead);
                            break;
                        default:
                            jLabel_Details.setText(Lang.getInstance().translate("Bitcoin Address for deposit") + ":");
                            refreshReceiverDetails(Lang.getInstance().translate("Payment Details") +
                                            " " + Lang.getInstance().translate("for deposit") + " bitcoins",
                                    detailsHead);
                            jLabel_AssetInput.setVisible(false);
                            cbxAssetsInput.setVisible(false);
                    }

                    jText_Help.setText("<html><h2>3. " + Lang.getInstance().translate(
                            "Click the button '%1' and transfer the Assets to the received address")
                            .replace("%1", Lang.getInstance().translate("Get Payment Details")) + "</h2></html>");

                }
            }
        });

        if (asset != null) {
            cbxAssets.setSelectedItem(asset);
        }

        /////////////// INPUT ASSET
        jLabel_AssetInput.setText(Lang.getInstance().translate("Asset to Pay") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(jLabel_AssetInput, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        cbxAssetsInput = new JComboBox<AssetCls>(new FundTokensComboBoxModel(new long[]{AssetCls.BTC_KEY, 14L}));
        this.add(cbxAssetsInput, gridBagConstraints);

        //////////////// BUTTONS

        jButton_getDetails = new MButton(Lang.getInstance().translate("Get Payment Details"), 2);
        jButton_getDetails.setToolTipText("");
        jButton_getDetails.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton_getDetails.setText(Lang.getInstance().translate("Wait") + "...");
                jText_Help.setText("<html><h2><blink>4. " + Lang.getInstance().translate(
                        "Please Wait...") + "</blink></h2></html>");

                // чтобы не ждать зависание скрипта - и отображение текста моментальное будеьт
                new Thread(() -> {
                    onGoClick(jText_Help);
                }).start();
            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        //gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new Insets(1, 0, 29, 0);

        add(jButton_getDetails, gridBagConstraints);

        ////////////// DETAILS
        gridy += 1;

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
        jLabel_Details.setText(Lang.getInstance().translate("Address for deposit") + ":");

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
        JTextPane jText_History = new JTextPane();
        //jText_History.setStyledDocument(styleDocument);

        gridy += 1;

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

        jText_History.setContentType("text/html");
        jText_History.setEditable(false);
        jText_History.setBackground(UIManager.getColor("Panel.background"));
        // не пашет - надо внутри ручками в тексте jText_History.setFont(UIManager.getFont("Label.font"));

        jText_History.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent arg0) {
                // TODO Auto-generated method stub
                HyperlinkEvent.EventType type = arg0.getEventType();
                if (type != HyperlinkEvent.EventType.ACTIVATED)
                    return;

                try {
                    URLViewer.openWebpage(new URL(arg0.getDescription()));
                } catch (MalformedURLException e1) {
                    LOGGER.error(e1.getMessage(), e1);
                }

            }
        });

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

        String txURLin;
        String txURLout;
        boolean isWithdraw = asset == null;
        if (isWithdraw) {
            // значит это биткоин как стандарт вывода
            urlGetDetails += "BTC/";
        } else {

            switch ((int) asset.getKey()) {
                case 1:
                    urlGetDetails += "ERA/"; // BTC -> eBTC
                    break;
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
                case 1114:
                    urlGetDetails += "@ZEN/"; // ZEN -> eUSD
                    break;
                default:
                    urlGetDetails += "COMPU/"; // BTC -> COMPU
            }

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
                if (BlockChain.TEST_MODE) {
                    tip.setText("<html>" + StrJSonFine.convert(jsonObject) + "</html>");
                }

                //String color = "#" + Integer.toHexString(UIManager.getColor("Panel.background").getRGB()).substring(2);
                String resultText = "<body style='font-family:" + UIManager.getFont("Label.font").getFamily()
                        + "; font-size: " + UIManager.getFont("Label.font").getSize() + "pt;"
                        //+ "background:" + color
                        + "'>";
                //String resultText = "<body>";

                JSONObject deal_acc = (JSONObject) jsonObject.get("deal_acc");
                BigDecimal to_pay = new BigDecimal(deal_acc.get("to_pay").toString());
                if (to_pay.signum() != 0) {
                    resultText += Lang.getInstance().translate("Awaiting for payout")
                            + ": " + to_pay.toPlainString()
                    + " (" + Lang.getInstance().translate("maybe volume is too small for payout, please send more") + ")";
                }
                if (false && deal_acc.containsKey("message")) {
                    resultText += deal_acc.get("message");
                }

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

                    for (Object item : done) {
                        try {
                            JSONObject json = (JSONObject) item;

                            JSONObject curr_in = (JSONObject) json.get("curr_in");
                            JSONObject curr_out = (JSONObject) json.get("curr_out");
                            String amount_in = json.get("amount_in").toString();

                            if (json.containsKey("pay_out")) {

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

                                JSONObject pay_out = (JSONObject) json.get("pay_out");

                                if (isWithdraw) {
                                    txURLin = Settings.getInstance().getBlockexplorerURL()
                                            + "/index/blockexplorer.html"
                                            + "?tx=";

                                    if (curr_out.get("abbrev").equals("BTC")) {
                                        txURLout = "https://www.blockchain.com/ru/btc/tx/";
                                    } else if (curr_out.get("abbrev").equals("BTC")) {
                                        txURLout = "https://www.blockchain.com/ru/eth/tx/";
                                    } else {
                                        txURLout = "";
                                    }

                                } else {
                                    if (curr_in.get("abbrev").equals("BTC")) {
                                        txURLin = "https://www.blockchain.com/ru/btc/tx/";
                                    } else if (curr_in.get("abbrev").equals("BTC")) {
                                        txURLin = "https://www.blockchain.com/ru/eth/tx/";
                                    } else {
                                        txURLin = "";
                                    }

                                    txURLout = Settings.getInstance().getBlockexplorerURL()
                                            + "/index/blockexplorer.html"
                                            + "?tx=";

                                }

                                resultText += amount_in + " " + curr_in.get("abbrev")
                                        + " <a href='" + txURLin + json.get("txid").toString() + "'>(TX)</a>";


                                resultText +=
                                        //+ " &#9654; "
                                         " &#10144; "
                                        + pay_out.get("amo_taken") + " " + curr_out.get("abbrev")
                                        + " - " + pay_out.get("created_on")
                                        + " <a href='" + txURLout + pay_out.get("txid").toString() + "'>(TX)</a>"
                                ;
                            } else if (json.containsKey("stasus")) {
                                /** есди выплаты не было и платеж со статусом ожидания и т.д.
                                 * {"done": [{"acc": "1JiKoayUWVaPwzKq8oM8oaZ6TwYznLmNfJ", "stasus": "added",
                                 *      "curr_in": {"abbrev": "COMPU", "id": 10}, "curr_out": {"abbrev": "BTC", "id": 3},
                                 *      "amount_in": 0.0065, "created": "2019-07-20 09:33:29", "confitmations": 1,
                                 *      "status_mess": "2.426e-05", "txid": "3Bwqtmdu58Jn4pMo4558LTUasFdQ2wBwzuy78a4HgpjERiYTekJGHXzkhNxnLDdKh3Q2CW4amt1JAdTthNosfJVu"}
                                 *      ],
                                 * "deal_acc": {"payed_month": 0.02621535, "gift_pick": 2.6e-06,
                                 * "name": "1JiKoayUWVaPwzKq8oM8oaZ6TwYznLmNfJ", "price": 0.0,
                                 * "payed": 0.02621535, "to_pay": 0.00028671, "gift_payed": 8.02e-06,
                                 * "message": "<div class=\"row\"><div class=\"col-sm-12\" style=\"\"><h3>Congratulations! You have a gift <b>1.821e-05</b> <i class=\"fa fa-rub\" style=\"color:chartreuse;\"></i></h3>Use the gift code to receive more gifts. Gift code can be taken from our partners.<br />The probability to get <b>2.6e-06</b> them in the following payment is: <b>1</b>. You've already got 8.02e-06</div></div><div class=\"row\"><div class=\"col-sm-12\" style=\"\"><h4>\u0412\u0430\u0448\u0430 \u043f\u0435\u0440\u0435\u043f\u043b\u0430\u0442\u0430 <b>0.00028671 <i class=\"fa fa-rub\"\"></i></b> <small style=\"color:white\">\u041e\u043d\u0430 \u0431\u0443\u0434\u0435\u0442 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430 \u043a \u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0435\u043c\u0443 \u043f\u043b\u0430\u0442\u0435\u0436\u0443</small></h4></div></div>", "curr_out_id": 3, "id": 108, "gift_amount": 1.821e-05}, "in_process": [], "deal": {"MAX": 0.0, "name": "to COIN", "id": 2}, "unconfirmed": []}
                                 */

                                String txURL;
                                txURL = Settings.getInstance().getBlockexplorerURL()
                                        + "/index/blockexplorer.html"
                                        + "?tx=";

                                resultText += json.get("created") + " - " + json.get("amount_in") + " " + curr_in.get("abbrev")
                                        + " <a href='" + txURL + json.get("txid").toString() + "'>(TX)</a>"
                                        //+ " &#9654; "
                                        + " &#10144; ";

                                if (json.containsKey("status_mess")) {
                                    resultText += " <b>" + json.get("status_mess").toString() + "</b>";
                                }
                                resultText += " " + curr_out.get("abbrev") + " " + json.get("stasus");
                            }

                        } catch (Exception e) {
                            resultText += item.toString();
                        }

                        resultText += "<br>";
                    }
                }

                if (unconfirmed.isEmpty() && in_process.isEmpty() && done.isEmpty()) {
                    resultText += "<h3>" + Lang.getInstance().translate("Not Found") + "</h3>";
                }
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
