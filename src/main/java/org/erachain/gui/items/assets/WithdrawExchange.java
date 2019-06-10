package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.items.accounts.AccountAssetSendPanel;
import org.erachain.gui.library.MButton;
import org.erachain.gui.models.FundTokensComboBoxModel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//public class PersonConfirm extends JDialog { // InternalFrame  {
public class WithdrawExchange extends JPanel {

    // private JComboBox<Account> accountLBox;

    private static final Logger LOGGER = LoggerFactory.getLogger(WithdrawExchange.class);

    private static final long serialVersionUID = 2717571093561259483L;
    private MButton jButton_Cansel;
    private MButton jButton_Confirm;
    private JComboBox<Account> jComboBox_YourAddress;
    public JComboBox<AssetCls> cbxAssets;
    private JLabel jLabel_Address;
    private JLabel jLabel_Adress_Check;
    private JLabel jLabel_Asset;
    private JLabel jLabel_Details;
    private JLabel jLabel_YourAddress;
    private JTextField jTextField_Address = new JTextField();

    private AssetCls asset;

    public WithdrawExchange(AssetCls asset, Account account) {

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
        String urlGetRate = "https://api.face2face.cash/apipay/get_rate.json/10/9/1";
        String urlGetHistory = "https://api.face2face.cash/apipay/history.json/ERA/78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5";
        String urlGetDetails2 = "https://api.face2face.cash/apipay/get_uri_in.json/2/10/9/78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5/1000";

        JSONObject jsonObject;
        String inputText = "";
        String accountTo;
        String message = "";

        AssetCls assetIn = null;
        try {

            String urlGetDetails = "https://api.face2face.cash/apipay/get_uri_in.json/2/";
            assetIn = (AssetCls) cbxAssets.getSelectedItem();
            switch ((int) assetIn.getKey()) {
                case 12:
                    urlGetDetails += "12/3/" + jTextField_Address.getText() + "/0.1"; // eBTC -> BTC
                    message += "BTC";
                    break;
                case 95:
                    urlGetDetails += "13/3/" + jTextField_Address.getText() + "/100"; // eUSD -> BTC
                    message += "BTC";
                    break;
                case 94:
                    urlGetDetails += "14/3/" + jTextField_Address.getText() + "/100"; // eEUR -> BTC
                    message += "BTC";
                    break;
                default:
                    urlGetDetails += "10/3/" + jTextField_Address.getText() + "/1"; // COMPU -> BTC
                    String assetName = assetIn.getName();
                    if (assetName.equals("Bitcoin")) {
                        message += "BTC";
                    } else {
                        message += assetName;
                    }
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

            if (BlockChain.DEVELOP_USE) {
                jLabel_Adress_Check.setText("<html>" + StrJSonFine.convert(jsonObject) + "</html>");
            }

            accountTo = jsonObject.get("addr_in").toString();

        } catch (Exception e) {
            accountTo = null;
            jLabel_Adress_Check.setText(inputText);
            inputText = "";
        }

        if (assetIn != null && accountTo != null) {
            if (accountTo != null) {

                message += ":" + jTextField_Address.getText();
                MainPanel.getInstance().insertTab(new AccountAssetSendPanel(assetIn, TransactionAmount.ACTION_SEND,
                        null, new Account(accountTo), null, message));

            }

            jButton_Confirm.setEnabled(true);

        }
    }

    private void initComponents(AssetCls assetIn, Account account) {

        if (assetIn == null) {
            asset = Controller.getInstance().getAsset(1l);
        } else {
            asset = assetIn;
        }

        GridBagConstraints gridBagConstraints;

        //paneAssetInfo = new JScrollPane();
        jLabel_YourAddress = new JLabel();
        jComboBox_YourAddress = new JComboBox<>();
        jLabel_Address = new JLabel();
        jLabel_Asset = new JLabel();

        jLabel_Adress_Check = new JLabel();
        jLabel_Details = new JLabel();

        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[]{0, 9, 0, 9, 0, 9, 0};
        layout.rowHeights = new int[]{0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0};
        //getContentPane().setLayout(layout);
        this.setLayout(layout);

        int gridy = 0;

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

        cbxAssets.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    asset = (AssetCls) cbxAssets.getSelectedItem();
                }
            }
        });

        gridy +=3;

        ////////////////
        jLabel_Address.setText(Lang.getInstance().translate("Address to Withdraw") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 27, 0, 0);
        add(jLabel_Address, gridBagConstraints);

        if (account == null) {
            jLabel_Adress_Check.setText(Lang.getInstance().translate("Insert Withdraw Address"));
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
        jLabel_Adress_Check.setText("");
        add(jLabel_Adress_Check, gridBagConstraints);

        //////////////// BUTTONS

        gridy += 3;


        jButton_Confirm = new MButton(Lang.getInstance().translate("Withdraw"), 2);
        jButton_Confirm.setToolTipText("");
        jButton_Confirm.addActionListener(new ActionListener() {
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
        add(jButton_Confirm, gridBagConstraints);

        //////////////////////////
        gridy += 3;

        jButton_Cansel = new MButton(Lang.getInstance().translate("See Withdraw Transactions"), 2);
        jButton_Cansel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        //gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new Insets(1, 0, 29, 0);
        add(jButton_Cansel, gridBagConstraints);

    }

}
