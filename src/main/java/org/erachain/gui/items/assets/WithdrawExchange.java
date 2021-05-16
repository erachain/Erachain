package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.IconPanel;
import org.erachain.gui.items.accounts.AccountAssetSendPanel;
import org.erachain.gui.library.MButton;
import org.erachain.gui.models.FundTokensComboBoxModel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.utils.StrJSonFine;
import org.erachain.utils.URLViewer;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//public class PersonConfirm extends JDialog { // InternalFrame  {
public class WithdrawExchange extends IconPanel {

    public static String NAME = "WithdrawExchange";
    public static String TITLE = "Withdraw Exchange";

    private static final Logger LOGGER = LoggerFactory.getLogger(WithdrawExchange.class);

    private static final long serialVersionUID = 2717571093561259483L;
    private MButton jButton_ShowForm;
    private MButton jButton_Confirm;
    private JComboBox<Account> jComboBox_YourAddress;
    public JComboBox<AssetCls> cbxAssets;
    private JLabel jLabel_Address;
    private JLabel jLabelAdressCheck;
    private JLabel jLabel_Asset;
    private JLabel jLabel_Details;
    private JLabel jLabel_YourAddress;
    private JTextField jTextField_Address = new JTextField();

    private AssetCls asset;

    public WithdrawExchange(AssetCls asset, Account account) {
        super(NAME, TITLE);
        initComponents(asset, account);
        this.setVisible(true);
    }

    private void refreshReceiverDetails(String text, JLabel pubKeyDetails) {
        // CHECK IF RECIPIENT IS VALID ADDRESS
        pubKeyDetails.setText("<html>" + text + "</html>");

    }

    public void onGoClick() {


        // http://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
        //String url_string = "https://api.face2face.cash/apipay/index.json";
        //String urlGetRate = "https://api.face2face.cash/apipay/get_rate.json/10/9/1";
        //String urlGetHistory = "https://api.face2face.cash/apipay/history.json/ERA/78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5";
        //String urlGetDetails2 = "https://api.face2face.cash/apipay/get_uri_in.json/2/10/9/78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5/1000";

        JSONObject jsonObject = null;
        String inputText = "";
        String accountTo;
        String message = "";
        String rate = null;
        AssetCls assetIn = null;
        String address = jTextField_Address.getText().trim();
        try {

            // GET RATE
            String urlGetDetails = "https://api.face2face.cash/apipay/get_uri_in.json/2/";
            assetIn = (AssetCls) cbxAssets.getSelectedItem();
            String abbrevIN = assetIn.getName();
            switch ((int) assetIn.getKey()) {
                case 1840:
                    urlGetDetails += "fUSD/BTC/" + address + "/100"; // eUSD -> BTC
                    message += "BTC";
                    break;
                case (int) DepositExchange.TEST_ASSET:
                    urlGetDetails = "http://185.195.26.197/7pay_in/apipay/get_uri_in.json/2/";
                    urlGetDetails += "fZEN/ZEN/" + address + "/30"; // eZEN -> ZEN
                    message += "ZEN";
                    break;
                default:
                    urlGetDetails += "f" + abbrevIN + "/" + abbrevIN + "/" + address + "/10";
                    message += abbrevIN;
            }

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

            if (BlockChain.TEST_MODE) {
                jLabelAdressCheck.setText("<html>" + StrJSonFine.convert(jsonObject) + "</html>");
            }

            LOGGER.debug(StrJSonFine.convert(jsonObject));

            accountTo = jsonObject.get("addr_in").toString();
            rate = jsonObject.get("rate").toString();

        } catch (Exception e) {
            if (jsonObject != null && jsonObject.containsKey("wrong")) {
                jLabelAdressCheck.setText(jsonObject.get("wrong").toString());
            } else {
                jLabelAdressCheck.setText(inputText + " " + e.getMessage());
            }
            jsonObject = null;
            accountTo = null;
        }

        if (assetIn != null && accountTo != null && rate != null) {

            message += ":" + jTextField_Address.getText();
            AccountAssetSendPanel panel = new AccountAssetSendPanel(assetIn,
                    null, new Account(accountTo), null, message, false);

            panel.jTextFieldTXTitle.setEnabled(false);
            panel.jComboBox_Asset.setEnabled(false);
            panel.recipientAddress.setEnabled(false);
            panel.jTextArea_Message.setEnabled(false);
            jLabelAdressCheck.setText("");
            rate = jsonObject.get("rate").toString();
            String bal = jsonObject.get("bal").toString();

            String formTitle;
            String incomeAssetName = assetIn.getName();
            switch ((int) assetIn.getKey()) {
                case 1:
                case 2:
                    formTitle = Lang.T("Transfer <b>%1</b> to this address for buy")
                            .replace("%1", incomeAssetName) + " <b>BTC</B>"
                            + " " + Lang.T("by rate") + ": <b>" + rate + "</b>"
                            + ", " + Lang.T("max buy amount") + ": <b>" + bal + "</b> BTC";
                    break;
                case (int) DepositExchange.TEST_ASSET:
                    incomeAssetName = "ZEN";
                    formTitle = Lang.T("Withdraw %1 to").replace("%1", incomeAssetName) + " " + address;
                    break;
                default:
                    formTitle = Lang.T("Withdraw %1 to").replace("%1", incomeAssetName) + " " + address;
            }

            if (jsonObject.containsKey("may_pay")) {
                formTitle += "<br>" + Lang.T("You may pay maximum") + ": " + jsonObject.get("may_pay").toString()
                        + incomeAssetName;
            }

            panel.jLabel_Title.setText("<html><h2>" + formTitle + "</h2></html>");
            panel.setName(Lang.T("Withdraw"));

            MainPanel.getInstance().removeTab(panel.getName());
            MainPanel.getInstance().insertNewTab(Lang.T("Send asset"), panel);

        }

        jButton_Confirm.setEnabled(true);

    }

    private void initComponents(AssetCls assetIn, Account account) {

        if (assetIn == null) {
            asset = Controller.getInstance().getAsset(2L);
        } else {
            asset = assetIn;
        }

        GridBagConstraints gridBagConstraints;

        //paneAssetInfo = new JScrollPane();
        jLabel_YourAddress = new JLabel();
        jComboBox_YourAddress = new JComboBox<>();
        jLabel_Address = new JLabel();
        jLabel_Asset = new JLabel();

        jLabelAdressCheck = new JLabel();
        jLabel_Details = new JLabel();

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
        jText_Title.setText("<html><h1>" + Lang.T("Withdraw from the Exchange") + "</h1></html>");

        JLabel jText_Help = new JLabel();

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        //gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(jText_Help, gridBagConstraints);
        String cryto = "BTC";
        jText_Help.setText("<html><h2>1. " + Lang.T("Select the Asset that you want to withdraw") + "</h2>"
                + "<h3>2. " + Lang.T("Set the address to which you want to withdraw funds")
                + ". " + Lang.T("And click button '%1' to open the panel for payment").replace("%1",
                Lang.T("Withdraw"))
                + ". " + Lang.T("Where You need to set only amount of withdraw asset in the panel for payment")
                + ".</h3>"
                + Lang.T("Minimal payment in equivalent")
                + " <b>" + 0.00025 + " " + cryto + "</b>" + "<br>"
                //+ Lang.T("Service will take commission fee approx - %1%").replace("%1","2.75")
                + Lang.T("Service will have some commission")
                + "</html>");

        /////////////// ASSET
        jLabel_Asset.setText(Lang.T("Asset") + ":");
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

        cbxAssets = new JComboBox<AssetCls>(new FundTokensComboBoxModel(false));
        this.add(cbxAssets, favoritesGBC);

        cbxAssets.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    asset = (AssetCls) cbxAssets.getSelectedItem();

                    String cryto;
                    switch ((int) asset.getKey()) {
                        case (int) DepositExchange.TEST_ASSET:
                            cryto = "ZEN";
                            break;
                        default:
                            cryto = asset.getName();
                    }

                    jText_Help.setText("<html><h3>2. " + Lang.T("Set the address to which you want to withdraw")
                            + " " + cryto
                            + ". " + Lang.T("And click button '%1' to open the panel for payment").replace("%1",
                            Lang.T("Next"))
                            + ". " + Lang.T("Where You need to set only amount of withdraw asset in the panel for payment")
                            + ".</h3>"
                            + Lang.T("Minimal payment in equivalent")
                            + " <b>" + 0.00025 + " " + cryto + "</b>" + "<br>"
                            //+ Lang.T("Service will take commission fee approx - %1%").replace("%1","2.75")
                            + Lang.T("Service will have some commission")
                    );
                }
            }
        });

        gridy += 3;

        ////////////////
        jLabel_Address.setText(Lang.T("Address to Withdraw") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 27, 0, 0);
        add(jLabel_Address, gridBagConstraints);

        if (account == null) {
            jLabelAdressCheck.setText(Lang.T("Insert Withdraw Address"));
        } else {
            jTextField_Address.setText(account.getAddress());
        }

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(jTextField_Address, gridBagConstraints);

        // BUTN NEXT
        jButton_Confirm = new MButton(Lang.T("Next"), 1);
        jButton_Confirm.setToolTipText("");
        jButton_Confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onGoClick();
            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(jButton_Confirm, gridBagConstraints);

        // TIP
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        jLabelAdressCheck.setText("");
        add(jLabelAdressCheck, gridBagConstraints);

        //////////////////////////
        JEditorPane jText_History = new JEditorPane();
        jText_History.setContentType("text/html");
        jText_History.setEditable(false);

        jText_History.setBackground(UIManager.getColor("Panel.background"));
        // не пашет - надо внутри ручками в тексте jText_History.setFont(UIManager.getFont("Label.font"));

        gridy += 2;

        jButton_ShowForm = new MButton(Lang.T("See Withdraw Transactions"), 2);
        jButton_ShowForm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jText_History.setText(DepositExchange.showHistory(null,
                        jTextField_Address.getText(), jLabelAdressCheck));
            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        //gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new Insets(1, 0, 29, 0);
        add(jButton_ShowForm, gridBagConstraints);


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
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        add(jText_History, gridBagConstraints);

    }

}
