package org.erachain.gui.items.assets;

import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.IconPanel;
import org.erachain.gui.library.MButton;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.Converter;
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
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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
import java.nio.charset.StandardCharsets;

//public class PersonConfirm extends JDialog { // InternalFrame  {
public class DepositExchange extends IconPanel {

    public static String NAME = "DepositExchange";
    public static String TITLE = "Deposit Exchange";

    public final static String TEST_ASSET = "-"; // "ETH"

    private static final Logger LOGGER = LoggerFactory.getLogger(DepositExchange.class);

    private static final long serialVersionUID = 2717571093561259483L;

    protected JPanel jPanelHistory = new javax.swing.JPanel();
    protected JPanel jPanelMain = new javax.swing.JPanel();

    private MButton jButtonHistory;
    private JLabel detailsHead;
    private JLabel jText_Help;
    private MButton jButton_getDetails;
    private JButton jButton_copyAddress = new JButton();
    private JButton jButton_copyDetails = new JButton();
    private JComboBox<Account> jComboBox_YourAddress;
    public JComboBox<String> cbxAssetsBuy;
    public JComboBox<String> cbxAssetsInput;
    private JLabel jLabel_Address;
    private JLabel jLabel_Adress_Check;
    private JLabel jLabel_Asset;
    private JLabel jLabel_AssetInput;
    private JLabel jLabel_Details;
    private JTextField payToAddressField;
    private JLabel jLabel_AreaDetails;
    /**
     * for Etherium data
     */
    private JTextArea payToAddressDetails;
    private JLabel jLabel_DetailsCheck;
    private JLabel payToAddressCheck;
    private JLabel jLabel_YourAddress;
    private JTextField jTextField_Address = new JTextField();
    protected int step = 0;

    BigDecimal amount;

    public DepositExchange(String asset, Account account, BigDecimal amount) {
        super(NAME, TITLE);

        cbxAssetsInput = new JComboBox<>(new String[]{
                "BTC",
                //"DOGE", "LTC",
                //"DASH"
        });

        cbxAssetsBuy = new JComboBox<>(new String[]{
                //AssetCls.ERA_ABBREV, AssetCls.FEE_NAME,
                "BTC",
                //"USD" //, "DOGE", "LTC", "DASH"
        });

        this.amount = amount;

        initComponents(asset, account);

        this.setVisible(true);
    }

    private void setPaymentDetails(String text, JLabel details) {
        details.setText("<html>" + text + "</html>");
    }

    public void onGoClick(JLabel jText_Help) {

        jButton_getDetails.setEnabled(false);
        payToAddressField.setText("");
        payToAddressDetails.setText("");
        payToAddressCheck.setText("<html>" + Lang.T("wait"));

        // http://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
        //String url_string = "https://api.face2face.cash/apipay/index.json";
        //String urlGetRate = "https://api.face2face.cash/apipay/get_rate.json/10/9/1";
        //String urlGetHistory = "https://api.face2face.cash/apipay/history.json/FOIL/78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5";
        //String urlGetDetailsTest = "https://api.face2face.cash/apipay/get_uri_in.json/2/3/12/78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5/1000";

        JSONObject jsonObject;

        String urlGetDetails = "https://api.face2face.cash/apipay/get_uri_in.json/2/";

        String assetBuy = (String) cbxAssetsBuy.getSelectedItem();
        String assetInput = (String) cbxAssetsInput.getSelectedItem();

        switch (assetBuy) {
            case DepositExchange.TEST_ASSET:
                /// http://185.195.26.197/7pay_in/apipay/get_uri_in.json/2/ETH/@ETH/7KmL1nheVHYVmdaEB4rsRjiENUD3sTr7EE/1
                urlGetDetails = "http://185.195.26.197/7pay_in/apipay/get_uri_in.json/2/";
                assetInput = assetBuy;
                break;
            default:
        }

        urlGetDetails += assetInput + "/" + getOutExtName(assetBuy) + "/" + jTextField_Address.getText() + "/";

        switch (assetInput) {
            case "BTC":
                urlGetDetails += "0.01";
                break;
            case "DOGE":
                urlGetDetails += "1000";
                break;
            default:
                // rate not need
                urlGetDetails += "100";
        }

        LOGGER.info(urlGetDetails);

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
            if (false && BlockChain.TEST_MODE) {
                payToAddressCheck.setText("<html>" + StrJSonFine.convert(jsonObject) + "</html>");
            }

            if (jsonObject.containsKey("wrong")) {
                payToAddressCheck.setText("<html><b>" + jsonObject.get("wrong") + "</b></html>");
                payToAddressField.setText(jsonObject.get("addr_in").toString());

                if (jsonObject.containsKey("addr_out_full") && assetBuy == DepositExchange.TEST_ASSET) {
                    String payMess = jsonObject.get("addr_out_full").toString();
                    jLabel_AreaDetails.setVisible(true);
                    jButton_copyDetails.setVisible(true);
                    jButton_copyDetails.setVisible(true);

                    payToAddressDetails.setText("0x" + Converter.toHex(payMess.getBytes(StandardCharsets.UTF_8)));
                }

            } else {

                /// ВНИМАНИЕ - НЕЛЬЗЯ делать setText("") - без HTML - иначе мягкий перенос слов перестанет работать
                String help = "<html><p>";

                String rate = "" + (float) (double) jsonObject.get("rate");
                String bal = jsonObject.get("bal").toString();

                //LOGGER.debug(StrJSonFine.convert(jsonObject));

                if (isStableCoin(assetBuy)) {
                    help += Lang.T("Transfer <b>%1</B> to this address for deposit your account on Exchange")
                            .replace("%1", assetInput);
                } else {
                    help += Lang.T("Transfer <b>%1</b> to this address for buy")
                            .replace("%1", assetInput) + " <b>" + assetBuy + "</b>"
                            + " " + Lang.T("by rate") + ": <b>" + rate + "</b>.";

                    //help += " " + Lang.T("max buy amount") + ": <b>" + bal + "</b> " + assetBuy;
                }

                if (assetBuy == "ETH") {
                    String payMess = jsonObject.get("addr_out_full").toString();
                    jLabel_AreaDetails.setVisible(true);
                    jButton_copyDetails.setVisible(true);
                    payToAddressDetails.setVisible(true);
                    payToAddressDetails.setText("0x" + Converter.toHex(payMess.getBytes(StandardCharsets.UTF_8)));
                }

                if (!isStableCoin(assetBuy) && jsonObject.containsKey("may_pay")) {
                    help += "<p>" + Lang.T("Service can accept a maximum of %1 now")
                            .replace("%1",
                                    "<b>" + jsonObject.get("may_pay").toString() + " " + assetInput + "</b>"
                            ) + ".";
                }

                help += "<p>" + Lang.T("Minimal payment in equivalent")
                        + " <b>" + 10 + " USD</b>.";

                payToAddressField.setText(jsonObject.get("addr_in").toString());
                payToAddressCheck.setText(help);
            }

        } else {
            payToAddressCheck.setText("<html><h2>" + Lang.T("error") + "</h2>"
                    + inputText);
        }

        jButton_getDetails.setEnabled(true);

        jText_Help.setText("<html><h2>5. " + Lang.T(
                "Transfer Assets to address below") + "</h2></html>");
        jButton_getDetails.setText(Lang.T("Get Payment Details"));

    }

    private void initComponents(String asset_in, Account account) {

        setLayout(new GridLayout(1, 2));

        jPanelMain.setLayout(new GridBagLayout());
        jPanelHistory.setLayout(new GridBagLayout());

        String asset;
        if (asset_in == null) {
            asset = AssetCls.ERA_ABBREV;
        } else {
            asset = asset_in;
        }

        int gridy = 0;

        GridBagConstraints titleGBC = new GridBagConstraints();
        titleGBC.gridx = 0;
        titleGBC.gridwidth = 2;
        titleGBC.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        titleGBC.fill = java.awt.GridBagConstraints.HORIZONTAL;
        titleGBC.insets = new java.awt.Insets(0, 5, 5, 0);

        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.gridx = 0;
        labelGBC.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        labelGBC.insets = new java.awt.Insets(0, 5, 5, 0);

        GridBagConstraints fieldGBC = new GridBagConstraints();
        fieldGBC.gridx = 1;
        fieldGBC.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        fieldGBC.fill = java.awt.GridBagConstraints.HORIZONTAL;
        fieldGBC.insets = new java.awt.Insets(0, 5, 5, 8);

        //paneAssetInfo = new JScrollPane();
        jLabel_YourAddress = new JLabel();
        jComboBox_YourAddress = new JComboBox<>();
        jLabel_Address = new JLabel();
        jLabel_Asset = new JLabel();
        jLabel_AssetInput = new JLabel();

        jLabel_Adress_Check = new JLabel();
        jLabel_Details = new JLabel();
        detailsHead = new JLabel();
        payToAddressField = new JTextField();
        jLabel_AreaDetails = new JLabel();
        payToAddressDetails = new JTextArea();
        payToAddressDetails.setLineWrap(true);
        payToAddressDetails.setRows(3);
        payToAddressDetails.setEditable(false);
        payToAddressDetails.setToolTipText("");
        payToAddressDetails.setText("");


        jLabel_DetailsCheck = new JLabel();
        payToAddressCheck = new JLabel();

        JLabel jText_Title = new JLabel();

        jPanelMain.add(jText_Title, titleGBC);
        jText_Title.setText("<html><h1>" + Lang.T("Deposit of Assets to the Exchange") + "</h1></html>");

        jText_Help = new JLabel();

        titleGBC.gridy = ++gridy;
        jPanelMain.add(jText_Help, titleGBC);
        jText_Help.setText("<html><h3>" + Lang.T("Select account or insert manual") + "</h3></html>");

        jLabel_YourAddress.setText(Lang.T("Select account") + ":");
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel_YourAddress, labelGBC);

        jComboBox_YourAddress = new JComboBox<Account>(new AccountsComboBoxModel());
        jComboBox_YourAddress.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                jTextField_Address.setText(((Account) jComboBox_YourAddress.getSelectedItem()).getAddress());
                jText_Help.setText("");
                reset();

            }
        });

        if (account != null) {
            jComboBox_YourAddress.setSelectedItem(account);
            jComboBox_YourAddress.setEnabled(false);
            jTextField_Address.setEditable(false);
        }
        jTextField_Address.setText(((Account) jComboBox_YourAddress.getSelectedItem()).getAddress());

        fieldGBC.gridy = gridy;
        jPanelMain.add(jComboBox_YourAddress, fieldGBC);

        ////////////////
        jLabel_Address.setText(Lang.T("Account to Deposit") + ":");
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel_Address, labelGBC);

        if (account == null) {
            jLabel_Adress_Check.setText(Lang.T("Insert Deposit Account"));
        } else {
            jTextField_Address.setText(account.getAddress());
        }

        fieldGBC.gridy = gridy;
        jPanelMain.add(jTextField_Address, fieldGBC);

        titleGBC.gridy = ++gridy;
        jPanelMain.add(jLabel_Adress_Check, titleGBC);

        /////////////// ASSET
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel_Asset, labelGBC);

        if (asset != null) {
            cbxAssetsBuy.setSelectedItem(asset);
        }
        fieldGBC.gridy = gridy;
        this.jPanelMain.add(cbxAssetsBuy, fieldGBC);

        /////////////// INPUT ASSET
        jLabel_AssetInput.setText(Lang.T("What to pay") + ":");
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel_AssetInput, labelGBC);

        fieldGBC.gridy = gridy;
        this.jPanelMain.add(cbxAssetsInput, fieldGBC);

        //////////////// BUTTONS

        jButton_getDetails = new MButton(Lang.T("Get Payment Details"), 2);
        jButton_getDetails.setToolTipText("");
        jButton_getDetails.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton_getDetails.setText(Lang.T("Wait") + "...");
                jText_Help.setText("<html><h3><blink>4. " + Lang.T(
                        "Please Wait...") + "</blink></h3></html>");

                // чтобы не ждать зависание скрипта - и отображение текста моментальное будеьт
                new Thread(() -> {
                    onGoClick(jText_Help);
                }).start();
            }
        });

        fieldGBC.gridy = ++gridy;
        jPanelMain.add(jButton_getDetails, fieldGBC);

        ////////////// DETAILS
        detailsHead.setText(Lang.T("Payment Details"));
        titleGBC.gridy = ++gridy;
        jPanelMain.add(detailsHead, titleGBC);

        jLabel_Details.setText(Lang.T("Address") + ":");
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel_Details, labelGBC);

        fieldGBC.gridy = gridy;
        jPanelMain.add(payToAddressField, fieldGBC);
        payToAddressField.setEditable(false);
        payToAddressField.setToolTipText("");
        payToAddressField.setText("");

        ImageIcon image = new ImageIcon("images/icons/copy.png");
        int x = image.getIconWidth();
        int y = image.getIconHeight();

        int x1 = payToAddressField.getPreferredSize().height;
        double k = ((double) x / (double) x1);
        y = (int) ((double) y / k);

        jButton_copyAddress.setIcon(new ImageIcon(image.getImage().getScaledInstance(x1, y, 1)));
        ++fieldGBC.gridx;
        jPanelMain.add(jButton_copyAddress, fieldGBC);
        --fieldGBC.gridx;

        jLabel_AreaDetails.setText(Lang.T("Data") + ":");
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel_AreaDetails, labelGBC);

        fieldGBC.gridy = gridy;
        jPanelMain.add(payToAddressDetails, fieldGBC);

        jButton_copyDetails.setIcon(new ImageIcon(image.getImage().getScaledInstance(x1, y, 1)));
        ++fieldGBC.gridx;
        jPanelMain.add(jButton_copyDetails, fieldGBC);
        --fieldGBC.gridx;

        jLabel_AreaDetails.setVisible(false);
        payToAddressDetails.setVisible(false);
        jButton_copyDetails.setVisible(false);


        jLabel_DetailsCheck.setText(Lang.T("Status") + ":");
        labelGBC.gridy = ++gridy;
        jPanelMain.add(jLabel_DetailsCheck, labelGBC);
        fieldGBC.gridy = gridy;
        fieldGBC.gridheight = 3;
        jPanelMain.add(payToAddressCheck, fieldGBC);

        //////////////////////////
        JTextPane jText_History = new JTextPane();
        //jText_History.setStyledDocument(styleDocument);

        jButtonHistory = new MButton(Lang.T("See Deposit History"), 2);
        jButtonHistory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                jText_History.setText(showHistory((String) cbxAssetsBuy.getSelectedItem(),
                        jTextField_Address.getText(), jLabel_Adress_Check));

            }
        });

        fieldGBC.gridy = 0;
        jPanelHistory.add(jButtonHistory, fieldGBC);

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

        titleGBC.gridy = 1;
        //gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        jPanelHistory.add(jText_History, titleGBC);

        cbxAssetsBuy.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                reset();
            }
        });

        cbxAssetsInput.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                reset();
            }
        });

        jText_History.setText("");

        //////// PANEL LEFT
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        add(jPanelMain, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        add(jPanelHistory, gridBagConstraints);

        jButton_copyAddress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(payToAddressField.getText());
                clipboard.setContents(value, null);
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.T("Address %1 was copied")
                                .replace("%1", payToAddressField.getText())
                                + ".",
                        Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);
            }
        });

        jButton_copyDetails.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(payToAddressDetails.getText());
                clipboard.setContents(value, null);
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.T("Data was copied"),
                        Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

            }
        });

        reset();

    }

    private void reset() {
        String assetBuy = (String) cbxAssetsBuy.getSelectedItem();

        payToAddressField.setText("");
        payToAddressDetails.setText("");
        payToAddressCheck.setText("<html>"); // только HTML! иначе перенос слов убьется

        jLabel_AreaDetails.setVisible(false);
        payToAddressDetails.setVisible(false);
        jButton_copyDetails.setVisible(false);


        switch (assetBuy) {
            case AssetCls.ERA_NAME:
            case AssetCls.FEE_NAME:
            case "USD":
                //jLabel_Details.setText(Lang.T("Address for buy") + ":");
                setPaymentDetails(Lang.T("Payment details for buy") + " " + assetBuy,
                        detailsHead);
                jLabel_Asset.setText(Lang.T("What to buy"));
                jLabel_AssetInput.setVisible(true);
                cbxAssetsInput.setVisible(true);
                break;
            case "ETH":
                //jLabel_Details.setText(Lang.T("Address to deposit") + " ETH" + ":");
                setPaymentDetails(Lang.T("Payment details to deposit") + " ETH",
                        detailsHead);
                jLabel_Asset.setText(Lang.T("What to deposit"));
                jLabel_AssetInput.setVisible(false);
                cbxAssetsInput.setVisible(false);
                break;
            default:
                //jLabel_Details.setText(Lang.T("Address to deposit") + ":");
                setPaymentDetails(Lang.T("Payment details to deposit") + " " + assetBuy,
                        detailsHead);
                jLabel_Asset.setText(Lang.T("What to deposit"));
                jLabel_AssetInput.setVisible(false);
                cbxAssetsInput.setVisible(false);
        }

        //jText_Help.setText("<html><h2>3. " + Lang.T(
        //        "Click the button '%1' and transfer the Assets to the received address")
        //        .replace("%1", Lang.T("Get Payment Details")) + "</h2></html>");

    }

    public static String showHistory(String asset, String address, JLabel tip) {
        JSONObject jsonObject;

        // [TOKEN]/[ADDRESS]
        String urlGetDetails = "https://api.face2face.cash/apipay/history.json/";

        String txURLin;
        String txURLout;

        urlGetDetails += getOutExtName(asset) + "/" + address;
        LOGGER.info(urlGetDetails);

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
                    resultText += Lang.T("Awaiting for payout")
                            + ": " + to_pay.toPlainString()
                            + " (" + Lang.T("maybe volume is too small for payout, please send more") + ")";
                }
                if (deal_acc.containsKey("message")) {
                    resultText += deal_acc.get("message");
                }

                JSONArray unconfirmed = (JSONArray) jsonObject.get("unconfirmed");
                if (!unconfirmed.isEmpty()) {
                    resultText += "<h3>" + Lang.T("Pending") + "</h3>";
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
                    resultText += "<h3>" + Lang.T("In Process") + "</h3>";
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
                    resultText += "<h3>" + Lang.T("Done") + "</h3>";

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

                                if (isStableCoin(asset)) {
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
                    resultText += "<h3>" + Lang.T("Not Found") + "</h3>";
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

    /**
     * ABBREV on SWAP
     *
     * @param assetBuy
     * @return
     */
    public static String getOutExtName(String assetBuy) {
        switch (assetBuy) {
            case "ERA":
            case "COMPU":
                return assetBuy;
            default:
                return "@" + assetBuy;
        }
    }

    /**
     * used as Depose or Withdraw
     *
     * @param assetBuy
     * @return
     */
    public static boolean isStableCoin(String assetBuy) {
        switch (assetBuy) {
            case AssetCls.ERA_NAME:
            case AssetCls.FEE_NAME:
            case "USD":
                return false;
            default:
                return true;
        }
    }
}
