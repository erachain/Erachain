package org.erachain.gui.settings;

import org.apache.commons.io.FileUtils;
import org.erachain.controller.Controller;
import org.erachain.gui.MainFrame;
import org.erachain.lang.Lang;
import org.erachain.lang.LangFile;
import org.erachain.network.Network;
import org.erachain.settings.Settings;
import org.erachain.utils.SaveStrToFile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class SettingsFrame extends JDialog {
    static Logger LOGGER = LoggerFactory.getLogger(SettingsFrame.class);

    public JSONObject settingsJSONbuf;
    private SettingsTabPane settingsTabPane;

    public SettingsFrame() {

        //CREATE FRAME
        setTitle(Controller.getInstance().getApplicationName(false) + " - " + Lang.T("Settings"));
        setModal(true);
        setResizable(false);
        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);


        settingsJSONbuf = new JSONObject();
        settingsJSONbuf = Settings.getInstance().Dump();

        this.setLayout(new GridBagLayout());

        //////////
        //SETTINGS TABPANE
        this.settingsTabPane = new SettingsTabPane();
        GridBagConstraints gbc_tabPane = new GridBagConstraints();
        gbc_tabPane.gridwidth = 5;
        gbc_tabPane.fill = GridBagConstraints.BOTH;
        gbc_tabPane.anchor = GridBagConstraints.NORTHWEST;
        gbc_tabPane.insets = new Insets(0, 0, 0, 0);
        gbc_tabPane.gridx = 0;
        gbc_tabPane.gridy = 0;

        this.add(this.settingsTabPane, gbc_tabPane);

        JButton btnNewButton = new JButton(Lang.T("Apply"));
        GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
        gbc_btnNewButton.fill = GridBagConstraints.NONE;
        gbc_btnNewButton.anchor = GridBagConstraints.EAST;
        gbc_btnNewButton.insets = new Insets(5, 5, 5, 5);
        gbc_btnNewButton.gridx = 0;
        gbc_btnNewButton.gridy = 1;
        gbc_btnNewButton.weightx = 2;

        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int n = JOptionPane.showConfirmDialog(
                        new JFrame(), Lang.T("To apply the new settings?"),
                        Lang.T("Confirmation"),
                        JOptionPane.OK_CANCEL_OPTION);
                if (n == JOptionPane.OK_OPTION) {
                    if (saveSettings()) {
                        settingsTabPane.close();
                        //DISPOSE
                        setVisible(false);
                        dispose();
                    }
                }
                if (n == JOptionPane.CANCEL_OPTION) {

                }
            }
        });
        //       btnNewButton.setPreferredSize(new Dimension(100, 25));

        this.add(btnNewButton, gbc_btnNewButton);

        JButton btnCancel = new JButton(Lang.T("Cancel"));
        GridBagConstraints gbc_btnCancel = new GridBagConstraints();
        gbc_btnCancel.fill = GridBagConstraints.NONE;
        gbc_btnCancel.anchor = GridBagConstraints.WEST;
        gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
        gbc_btnCancel.gridx = 3;
        gbc_btnCancel.gridy = 1;
  //      gbc_btnCancel.weightx = 2;

        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                settingsTabPane.close();

                //DISPOSE
                setVisible(false);
                dispose();
            }
        });

        //    btnCancel.setPreferredSize(new Dimension(100, 25));

        this.add(btnCancel, gbc_btnCancel);
        //AS
        JButton btnDefaultSettings = new JButton(Lang.T("Default Settings"));
        GridBagConstraints gbc_btnDefaultSettings = new GridBagConstraints();
        gbc_btnDefaultSettings.fill = GridBagConstraints.NONE;
        gbc_btnDefaultSettings.anchor = GridBagConstraints.WEST;
        gbc_btnDefaultSettings.insets = new Insets(5, 5, 5, 5);
        gbc_btnDefaultSettings.gridx = 4;
        gbc_btnDefaultSettings.gridy = 1;
        gbc_btnDefaultSettings.weightx = 2;

        btnDefaultSettings.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                try {
                    SaveStrToFile.saveJsonFine(Settings.getInstance().getSettingsPath(), new JSONObject());
                } catch (IOException e1) {
                    LOGGER.error(e1.getMessage(), e1);
                    JOptionPane.showMessageDialog(
                            new JFrame(), "Error writing to the file: " + Settings.getInstance().getSettingsPath()
                                    + "\nProbably there is no access.",
                            "Error!",
                            JOptionPane.ERROR_MESSAGE);
                }

                Settings.freeInstance();

                JOptionPane.showMessageDialog(
                        new JFrame(), Lang.T("You need to restart the application for the changes to take effect"),
                        Lang.T("Attention") + "!",
                        JOptionPane.WARNING_MESSAGE);

                settingsTabPane.close();
                //DISPOSE
                setVisible(false);
                dispose();

            }
        });

        //AS
        //    btnCancel.setPreferredSize(new Dimension(100, 25));

        this.add(btnDefaultSettings, gbc_btnDefaultSettings);


        //ON CLOSE
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                //CLOSE DEBUG
                settingsTabPane.close();

                //DISPOSE
                setVisible(false);
                dispose();
            }
        });

        //SHOW FRAME
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    public boolean saveSettings() {
        boolean changeKeyCaching = false;
        boolean changeWallet = false;
        boolean changeDataDir = false;
        boolean limitConnections = false;
        boolean localPeerScanner = false;
        boolean changeLang = false;

        // save Rate

        settingsJSONbuf.put("compuRateUseDEX", settingsTabPane.rates_Setting_Panel.getUseDEX());
        settingsJSONbuf.put("compuRate", settingsTabPane.rates_Setting_Panel.getRate().toString());
        settingsJSONbuf.put("compuRateAsset", settingsTabPane.rates_Setting_Panel.getRateAsset().getKey());
        settingsJSONbuf.put("defaultPairAsset", settingsTabPane.rates_Setting_Panel.getDefaultPairAsset().getKey());

        // save backup settings
        settingsJSONbuf.put("backupenabled", settingsTabPane.backUp_Setting_Panel.jCheckBox_Enable_BackUp.isSelected());
        settingsJSONbuf.put("backupasktostart", settingsTabPane.backUp_Setting_Panel.jCheckBox_Ask_To_Start.isSelected());

        if (!Settings.getInstance().getBackUpPath().equals(settingsTabPane.backUp_Setting_Panel.jTextField_BuckUp_Dir.getText())) {
            settingsJSONbuf.put("backuppath", settingsTabPane.backUp_Setting_Panel.jTextField_BuckUp_Dir.getText());
            changeWallet = true;
        }

        // font
        //	if(Settings.getInstance().getMinConnections() != MinConnections)
        //	{
        if (settingsTabPane.uI_Settings_Panel.size_Font.getSelectedItem().toString() != "") {
            settingsJSONbuf.put("font_size", settingsTabPane.uI_Settings_Panel.size_Font.getSelectedItem().toString());
            //		gui.Library.Library.Set_GUI_Font(settingsTabPane.settingsBasicPanel.size_Font.getSelectedItem().toString());
        }

        if (settingsTabPane.uI_Settings_Panel.font_Name.getSelectedItem().toString() != "") {
            settingsJSONbuf.put("font_name", settingsTabPane.uI_Settings_Panel.font_Name.getSelectedItem().toString());
            //		gui.Library.Library.Set_GUI_Font(settingsTabPane.settingsBasicPanel.font_Name.getSelectedItem().toString());
        }

        // themes

        if (settingsTabPane.uI_Settings_Panel.other_Themes.isSelected()) {
            settingsJSONbuf.put("LookAndFell", "Other");

            if (settingsTabPane.uI_Settings_Panel.jComboBox_Thems.getSelectedItem().toString() != "") {
                settingsJSONbuf.put("theme", settingsTabPane.uI_Settings_Panel.jComboBox_Thems.getSelectedItem().toString());
                String path = Settings.getInstance().getUserPath();
                File source = new File(path + "themes/" + settingsTabPane.uI_Settings_Panel.jComboBox_Thems.getSelectedItem().toString(), "Default.theme");
                File dest = new File(path + "Default.theme");
                try {
                    FileUtils.copyFile(source, dest);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }

        }


        if (settingsTabPane.uI_Settings_Panel.system_Theme.isSelected()) {
            settingsJSONbuf.put("LookAndFell", "System");
        }

        if (settingsTabPane.uI_Settings_Panel.metal_Theme.isSelected()) {
            settingsJSONbuf.put("LookAndFell", "Metal");
        }


        if (Settings.getInstance().isGeneratorKeyCachingEnabled() != settingsTabPane.settingsBasicPanel.chckbxKeyCaching.isSelected()) {
            settingsJSONbuf.put("generatorkeycaching", settingsTabPane.settingsBasicPanel.chckbxKeyCaching.isSelected());
            changeKeyCaching = true;
        }

        // SOUND EVENTS
        if (Settings.getInstance().isSoundNewTransactionEnabled() != settingsTabPane.uI_Settings_Panel.chckbxSoundNewTransaction.isSelected()) {
            settingsJSONbuf.put("soundnewtransaction", settingsTabPane.uI_Settings_Panel.chckbxSoundNewTransaction.isSelected());
        }

        if (Settings.getInstance().isSoundReceiveMessageEnabled() != settingsTabPane.uI_Settings_Panel.chckbxSoundReceiveMessage.isSelected()) {
            settingsJSONbuf.put("soundreceivemessage", settingsTabPane.uI_Settings_Panel.chckbxSoundReceiveMessage.isSelected());
        }

        if (Settings.getInstance().isSoundReceivePaymentEnabled() != settingsTabPane.uI_Settings_Panel.chckbxSoundReceivePayment.isSelected()) {
            settingsJSONbuf.put("soundreceivepayment", settingsTabPane.uI_Settings_Panel.chckbxSoundReceivePayment.isSelected());
        }

        if (Settings.getInstance().isSoundForgedBlockEnabled() != settingsTabPane.uI_Settings_Panel.chckbxSoundForgedBlock.isSelected()) {
            settingsJSONbuf.put("soundforgedblock", settingsTabPane.uI_Settings_Panel.chckbxSoundForgedBlock.isSelected());
        }

        if (Settings.getInstance().isSysTrayEnabled() != settingsTabPane.uI_Settings_Panel.chckbxSysTrayEvent.isSelected()) {
            settingsJSONbuf.put("trayeventenabled", settingsTabPane.uI_Settings_Panel.chckbxSoundForgedBlock.isSelected());
        }

        // COLORS
        if (Settings.getInstance().markIncome() != settingsTabPane.uI_Settings_Panel.checkMarkIncome.isSelected()) {
            settingsJSONbuf.put("markincome", settingsTabPane.uI_Settings_Panel.checkMarkIncome.isSelected());
        }

        if (Settings.getInstance().markColorObj() != settingsTabPane.uI_Settings_Panel.markColorExample.getForeground()) {
            Color color = settingsTabPane.uI_Settings_Panel.markColorExample.getForeground();
            settingsJSONbuf.put("markcolor", color.getRed() + "," + color.getGreen() + "," + color.getBlue());
        }
        if (Settings.getInstance().markColorSelectedObj() != settingsTabPane.uI_Settings_Panel.markColorSelectedExample.getForeground()) {
            Color color = settingsTabPane.uI_Settings_Panel.markColorSelectedExample.getForeground();
            settingsJSONbuf.put("markcolorselected", color.getRed() + "," + color.getGreen() + "," + color.getBlue());
        }

        // GUI
        if (Settings.getInstance().isGuiEnabled() != settingsTabPane.settingsBasicPanel.chckbxGuiEnabled.isSelected()) {
            settingsJSONbuf.put("guienabled", settingsTabPane.settingsBasicPanel.chckbxGuiEnabled.isSelected());
        }

        if (Settings.getInstance().isGuiDynamic() != settingsTabPane.settingsBasicPanel.chckbxGuiDynamic.isSelected()) {
            settingsJSONbuf.put("guidynamic", settingsTabPane.settingsBasicPanel.chckbxGuiDynamic.isSelected());
            Controller.getInstance().setDynamicGUI(settingsTabPane.settingsBasicPanel.chckbxGuiDynamic.isSelected());
        }
        if (Settings.getInstance().isRpcEnabled() != settingsTabPane.settingsBasicPanel.chckbxRpcEnabled.isSelected()) {
            settingsJSONbuf.put("rpcenabled", settingsTabPane.settingsBasicPanel.chckbxRpcEnabled.isSelected());
            Controller.getInstance().rpcServiceRestart();
            settingsTabPane.settingsAllowedPanel.rpcServiceRestart = true;
        }

        if (!settingsTabPane.settingsBasicPanel.chckbxGuiEnabled.isSelected() && !settingsTabPane.settingsBasicPanel.chckbxRpcEnabled.isSelected()) {
            JOptionPane.showMessageDialog(
                    new JFrame(), Lang.T("Both gui and rpc cannot be disabled!"),
                    Lang.T("Error!"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (Settings.getInstance().isWebEnabled() != settingsTabPane.settingsBasicPanel.chckbxWebEnabled.isSelected()) {
            settingsJSONbuf.put("webenabled", settingsTabPane.settingsBasicPanel.chckbxWebEnabled.isSelected());
            settingsTabPane.settingsAllowedPanel.webServiceRestart = true;
        }

        int newRpcPort = Integer.parseInt(settingsTabPane.settingsBasicPanel.txtRpcPort.getText());
        if (Settings.getInstance().getRpcPort() != newRpcPort) {
            if (Network.isPortAvailable(newRpcPort)) {
                settingsJSONbuf.put("rpcport", newRpcPort);
                settingsTabPane.settingsAllowedPanel.rpcServiceRestart = true;
            } else {
                JOptionPane.showMessageDialog(
                        new JFrame(), "Rpc port " + newRpcPort + " " + Lang.T("already in use!"),
                        Lang.T("Error!"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        int newWebPort = Integer.parseInt(settingsTabPane.settingsBasicPanel.txtWebport.getText());

        if (Settings.getInstance().getWebPort() != newWebPort) {
            if (Network.isPortAvailable(newWebPort)) {
                settingsJSONbuf.put("webport", newWebPort);
                settingsTabPane.settingsAllowedPanel.webServiceRestart = true;
            } else {
                JOptionPane.showMessageDialog(
                        new JFrame(), "Web port " + newWebPort + " " + Lang.T("already in use!"),
                        Lang.T("Error!"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        // { save SSL settings
        JSONObject settingsWebSSLJSONbuf = new JSONObject();
        // save use SSL
        if(settingsTabPane.settingsBasicPanel.chckbxWebUseSSL.isSelected() != Settings.getInstance().isWebUseSSL()) {
            settingsTabPane.settingsAllowedPanel.webServiceRestart = true;
            Settings.getInstance().setWebUseSSL(settingsTabPane.settingsBasicPanel.chckbxWebUseSSL.isSelected());
        }
        settingsWebSSLJSONbuf.put("Enable", settingsTabPane.settingsBasicPanel.chckbxWebUseSSL.isSelected());

        // save keystore pass
        if(!(new String(settingsTabPane.settingsBasicPanel.textWebKeystorePass.getPassword()).equals(Settings.getInstance().getWebKeyStorePassword()))){
            settingsTabPane.settingsAllowedPanel.webServiceRestart = true;
            Settings.getInstance().setWebKeyStorePassword(new String(settingsTabPane.settingsBasicPanel.textWebKeystorePass.getPassword()));
        }
        settingsWebSSLJSONbuf.put("KeyStorePassword", new String(settingsTabPane.settingsBasicPanel.textWebKeystorePass.getPassword()));

        // save certificate pass
        if(!(new String(settingsTabPane.settingsBasicPanel.textWebCertificatePass.getPassword()).equals(Settings.getInstance().getWebStoreSourcePassword()))){
            settingsTabPane.settingsAllowedPanel.webServiceRestart = true;
            Settings.getInstance().setWebStoreSourcePassword(new String(settingsTabPane.settingsBasicPanel.textWebCertificatePass.getPassword()));
        }
        settingsWebSSLJSONbuf.put("KeyStoreSourcePassword", new String(settingsTabPane.settingsBasicPanel.textWebCertificatePass.getPassword()));

        // save keystore fale path
        if(!(settingsTabPane.settingsBasicPanel.textWebKeyStoreFilePath.getText().equals(Settings.getInstance().getWebKeyStorePath()))) {
            settingsTabPane.settingsAllowedPanel.webServiceRestart = true;
            Settings.getInstance().setWebKeyStorePath(settingsTabPane.settingsBasicPanel.textWebKeyStoreFilePath.getText());
        }
        settingsWebSSLJSONbuf.put("KeyStorePath", settingsTabPane.settingsBasicPanel.textWebKeyStoreFilePath.getText());

        settingsJSONbuf.put("WEB_SSL",settingsWebSSLJSONbuf);
        // save SSL settings }


        int MinConnections = Integer.parseInt(settingsTabPane.settingsBasicPanel.textMinConnections.getText());
        if (Settings.getInstance().getMinConnections() != MinConnections) {
            settingsJSONbuf.put("minconnections", MinConnections);
            limitConnections = true;
        }

        int MaxConnections = Integer.parseInt(settingsTabPane.settingsBasicPanel.textMaxConnections.getText());
        if (Settings.getInstance().getMaxConnections() != MaxConnections) {
            settingsJSONbuf.put("maxconnections", MaxConnections);
            limitConnections = true;
        }

        String blockExplorer = settingsTabPane.settingsBasicPanel.txtBlockExplorer.getText();
        if (!Settings.getInstance().getBlockexplorerURL().equals(blockExplorer)) {
            settingsJSONbuf.put("explorerURL", blockExplorer);
        }

        if (Settings.getInstance().isLocalPeersScannerEnabled() != settingsTabPane.settingsBasicPanel.chckbxLocalPeersScannerEnabled.isSelected()) {
            settingsJSONbuf.put("localpeerscanner", settingsTabPane.settingsBasicPanel.chckbxLocalPeersScannerEnabled.isSelected());
            localPeerScanner = true;
        }


        if (!Settings.getInstance().getWalletKeysPath().equals(settingsTabPane.settingsBasicPanel.textWallet.getText())) {
            settingsJSONbuf.put("walletKeysPath", settingsTabPane.settingsBasicPanel.textWallet.getText());
            changeWallet = true;
        }

        if (!Settings.getInstance().getDataChainPath().equals(settingsTabPane.settingsBasicPanel.textDataFolder.getText())) {
            settingsJSONbuf.put("dataChainPath", settingsTabPane.settingsBasicPanel.textDataFolder.getText());
            changeDataDir = true;
        }

        String fileName = ((LangFile) settingsTabPane.uI_Settings_Panel.jComboBox_Lang.getSelectedItem()).getFileName();
        if (!Settings.getInstance().getLangFileName().equals(fileName)) {
            settingsJSONbuf.put("lang", fileName);
            changeLang = true;
        }

        List<String> peersToSave = settingsTabPane.settingsKnownPeersPanel.knownPeersTableModel.getPeers();

        JSONArray peersJson = Settings.getInstance().getPeersJson();
        JSONArray newPeersJson = new JSONArray();

        for (Object peer : peersJson) {
            if (peersToSave.contains((String) peer)) {
                newPeersJson.add(peer);
            }
        }

        if (newPeersJson.size() != peersJson.size()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("knownpeers", newPeersJson);

                SaveStrToFile.saveJsonFine(Settings.getInstance().getPeersPath(), jsonObject);

            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(
                        new JFrame(), "Error writing to the file: " + Settings.getInstance().getPeersPath()
                                + "\nProbably there is no access.",
                        "Error!",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        JSONArray peersToSaveApproved = new JSONArray();

        if (peersJson != null) {
            for (String peer : peersToSave) {
                if (!peersJson.contains(peer)) {
                    peersToSaveApproved.add(peer);
                }
            }
        }

        settingsJSONbuf.put("knownpeers", peersToSaveApproved);

        if (settingsTabPane.settingsAllowedPanel.chckbxWebAllowForAll.isSelected()) {
            settingsJSONbuf.put("weballowed", new ArrayList<String>());
        } else {
            settingsJSONbuf.put("weballowed", settingsTabPane.settingsAllowedPanel.webAllowedTableModel.getPeers());
        }

        if (settingsTabPane.settingsAllowedPanel.chckbxRpcAllowForAll.isSelected()) {
            settingsJSONbuf.put("rpcallowed", new ArrayList<String>());
        } else {
            settingsJSONbuf.put("rpcallowed", settingsTabPane.settingsAllowedPanel.rpcAllowedTableModel.getPeers());
        }


        try {
            SaveStrToFile.saveJsonFine(Settings.getInstance().getSettingsPath(), settingsJSONbuf);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(
                    new JFrame(), "Error writing to the file: " + Settings.getInstance().getSettingsPath()
                            + "\nProbably there is no access.",
                    "Error!",
                    JOptionPane.ERROR_MESSAGE);
        }

        Settings.freeInstance();

        if (settingsTabPane.settingsAllowedPanel.rpcServiceRestart) {
            Controller.getInstance().rpcServiceRestart();
        }

        if (settingsTabPane.settingsAllowedPanel.webServiceRestart) {
            Controller.getInstance().webServiceRestart();

            MainFrame.getInstance().jMenu_Files.webServerItem.setVisible(Settings.getInstance().isWebEnabled());
            MainFrame.getInstance().jMenu_Files.blockExplorerItem.setVisible(Settings.getInstance().isWebEnabled());
        }
        Lang.getInstance().setLangForNode();


        if (changeDataDir || changeWallet) {
            JOptionPane.showMessageDialog(
                    new JFrame(), Lang.T("You changed WalletDir or DataDir. You need to restart the wallet for the changes to take effect."),
                    Lang.T("Attention!"),
                    JOptionPane.WARNING_MESSAGE);
        }
        if (changeKeyCaching) {
            JOptionPane.showMessageDialog(
                    new JFrame(), Lang.T("You changed Generator Key Caching option. You need to restart the wallet for the changes to take effect."),
                    Lang.T("Attention!"),
                    JOptionPane.WARNING_MESSAGE);
        }
        if (limitConnections) {
            JOptionPane.showMessageDialog(
                    new JFrame(), Lang.T("You changed max connections or min connections. You need to restart the wallet for the changes to take effect."),
                    Lang.T("Attention!"),
                    JOptionPane.WARNING_MESSAGE);
        }
        if (localPeerScanner) {
            JOptionPane.showMessageDialog(
                    new JFrame(), Lang.T("You changed local peer discovery. You need to restart the wallet for the changes to take effect."),
                    Lang.T("Attention!"),
                    JOptionPane.WARNING_MESSAGE);
        }
        if (changeLang) {
            JOptionPane.showMessageDialog(
                    new JFrame(), Lang.T("You changed language. You need to restart the wallet for the changes to take effect."),
                    Lang.T("Attention!"),
                    JOptionPane.WARNING_MESSAGE);
        }

        return true;
    }


}
