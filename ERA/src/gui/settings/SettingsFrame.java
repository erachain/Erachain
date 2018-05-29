package gui.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import controller.Controller;
import gui.library.Menu_Files;
import lang.Lang;
import lang.LangFile;
import network.Network;
import settings.Settings;
import utils.SaveStrToFile;

@SuppressWarnings("serial")
public class SettingsFrame extends JDialog {
    static Logger LOGGER = Logger.getLogger(SettingsFrame.class.getName());

    public JSONObject settingsJSONbuf;
    private SettingsTabPane settingsTabPane;

    public SettingsFrame() {

        //CREATE FRAME
        setTitle(Lang.getInstance().translate("Erachain.org") + " - " + Lang.getInstance().translate("Settings"));
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
        gbc_tabPane.gridwidth = 4;
        gbc_tabPane.fill = GridBagConstraints.BOTH;
        gbc_tabPane.anchor = GridBagConstraints.NORTHWEST;
        gbc_tabPane.insets = new Insets(0, 0, 0, 0);
        gbc_tabPane.gridx = 0;
        gbc_tabPane.gridy = 0;

        this.add(this.settingsTabPane, gbc_tabPane);

        JButton btnNewButton = new JButton(Lang.getInstance().translate("Apply"));
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
                        new JFrame(), Lang.getInstance().translate("To apply the new settings?"),
                        Lang.getInstance().translate("Confirmation"),
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

        JButton btnCancel = new JButton(Lang.getInstance().translate("Cancel"));
        GridBagConstraints gbc_btnCancel = new GridBagConstraints();
        gbc_btnCancel.fill = GridBagConstraints.NONE;
        gbc_btnCancel.anchor = GridBagConstraints.WEST;
        gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
        gbc_btnCancel.gridx = 3;
        gbc_btnCancel.gridy = 1;
        gbc_btnCancel.weightx = 2;

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
        boolean changeLang = false;

        // save backup settings
        settingsJSONbuf.put("backupenabled", settingsTabPane.backUp_Setting_Panel.jCheckBox_Enable_BackUp.isSelected());
        settingsJSONbuf.put("backupasktostart", settingsTabPane.backUp_Setting_Panel.jCheckBox_Ask_To_Start.isSelected());

        if (!Settings.getInstance().getBackUpDir().equals(settingsTabPane.backUp_Setting_Panel.jTextField_BuckUp_Dir.getText())) {
            settingsJSONbuf.put("backuppath", settingsTabPane.backUp_Setting_Panel.jTextField_BuckUp_Dir.getText());
            changeWallet = true;
        }

        // font
        //	if(Settings.getInstance().getMinConnections() != MinConnections)
        //	{
        if (settingsTabPane.uI_Settings_Panel.size_Font.getSelectedItem().toString() != "") {
            settingsJSONbuf.put("font_size", settingsTabPane.uI_Settings_Panel.size_Font.getSelectedItem().toString());
            //		gui.library.library.Set_GUI_Font(settingsTabPane.settingsBasicPanel.size_Font.getSelectedItem().toString());
        }

        if (settingsTabPane.uI_Settings_Panel.font_Name.getSelectedItem().toString() != "") {
            settingsJSONbuf.put("font_name", settingsTabPane.uI_Settings_Panel.font_Name.getSelectedItem().toString());
            //		gui.library.library.Set_GUI_Font(settingsTabPane.settingsBasicPanel.font_Name.getSelectedItem().toString());
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

        if (Settings.getInstance().isSoundNewTransactionEnabled() != settingsTabPane.uI_Settings_Panel.chckbxSoundNewTransaction.isSelected()) {
            settingsJSONbuf.put("soundnewtransaction", settingsTabPane.uI_Settings_Panel.chckbxSoundNewTransaction.isSelected());
        }

        if (Settings.getInstance().isSoundReceiveMessageEnabled() != settingsTabPane.uI_Settings_Panel.chckbxSoundReceiveMessage.isSelected()) {
            settingsJSONbuf.put("soundreceivemessage", settingsTabPane.uI_Settings_Panel.chckbxSoundReceiveMessage.isSelected());
        }

        if (Settings.getInstance().isSoundReceivePaymentEnabled() != settingsTabPane.uI_Settings_Panel.chckbxSoundReceivePayment.isSelected()) {
            settingsJSONbuf.put("soundreceivepayment", settingsTabPane.uI_Settings_Panel.chckbxSoundReceivePayment.isSelected());
        }

        if (Settings.getInstance().isGuiEnabled() != settingsTabPane.settingsBasicPanel.chckbxGuiEnabled.isSelected()) {
            settingsJSONbuf.put("guienabled", settingsTabPane.settingsBasicPanel.chckbxGuiEnabled.isSelected());
        }

        if (Settings.getInstance().isGuiDynamic() != settingsTabPane.settingsBasicPanel.chckbxGuiDynamic.isSelected()) {
            settingsJSONbuf.put("guidynamic", settingsTabPane.settingsBasicPanel.chckbxGuiDynamic.isSelected());
        }
        if (Settings.getInstance().isRpcEnabled() != settingsTabPane.settingsBasicPanel.chckbxRpcEnabled.isSelected()) {
            settingsJSONbuf.put("rpcenabled", settingsTabPane.settingsBasicPanel.chckbxRpcEnabled.isSelected());
            settingsTabPane.settingsAllowedPanel.rpcServiceRestart = true;
        }

        if (!settingsTabPane.settingsBasicPanel.chckbxGuiEnabled.isSelected() && !settingsTabPane.settingsBasicPanel.chckbxRpcEnabled.isSelected()) {
            JOptionPane.showMessageDialog(
                    new JFrame(), Lang.getInstance().translate("Both gui and rpc cannot be disabled!"),
                    Lang.getInstance().translate("Error!"),
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
                        new JFrame(), "Rpc port " + newRpcPort + " " + Lang.getInstance().translate("already in use!"),
                        Lang.getInstance().translate("Error!"),
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
                        new JFrame(), "Web port " + newWebPort + " " + Lang.getInstance().translate("already in use!"),
                        Lang.getInstance().translate("Error!"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

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

        if (!Settings.getInstance().getWalletDir().equals(settingsTabPane.settingsBasicPanel.textWallet.getText())) {
            settingsJSONbuf.put("walletdir", settingsTabPane.settingsBasicPanel.textWallet.getText());
            changeWallet = true;
        }

        if (!Settings.getInstance().getDataDir().equals(settingsTabPane.settingsBasicPanel.textDataFolder.getText())) {
            settingsJSONbuf.put("datadir", settingsTabPane.settingsBasicPanel.textDataFolder.getText());
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

        Settings.FreeInstance();

        if (settingsTabPane.settingsAllowedPanel.rpcServiceRestart) {
            Controller.getInstance().rpcServiceRestart();
        }

        if (settingsTabPane.settingsAllowedPanel.webServiceRestart) {
            Controller.getInstance().webServiceRestart();

            Menu_Files.webServerItem.setVisible(Settings.getInstance().isWebEnabled());
            Menu_Files.blockExplorerItem.setVisible(Settings.getInstance().isWebEnabled());
        }
        Lang.getInstance().loadLang();


        if (changeDataDir || changeWallet) {
            JOptionPane.showMessageDialog(
                    new JFrame(), Lang.getInstance().translate("You changed WalletDir or DataDir. You need to restart the wallet for the changes to take effect."),
                    Lang.getInstance().translate("Attention!"),
                    JOptionPane.WARNING_MESSAGE);
        }
        if (changeKeyCaching) {
            JOptionPane.showMessageDialog(
                    new JFrame(), Lang.getInstance().translate("You changed Generator Key Caching option. You need to restart the wallet for the changes to take effect."),
                    Lang.getInstance().translate("Attention!"),
                    JOptionPane.WARNING_MESSAGE);
        }
        if (limitConnections) {
            JOptionPane.showMessageDialog(
                    new JFrame(), Lang.getInstance().translate("You changed max connections or min connections. You need to restart the wallet for the changes to take effect."),
                    Lang.getInstance().translate("Attention!"),
                    JOptionPane.WARNING_MESSAGE);
        }
        if (changeLang) {
            JOptionPane.showMessageDialog(
                    new JFrame(), Lang.getInstance().translate("You changed language. You need to restart the wallet for the changes to take effect."),
                    Lang.getInstance().translate("Attention!"),
                    JOptionPane.WARNING_MESSAGE);
        }

        return true;
    }


}
