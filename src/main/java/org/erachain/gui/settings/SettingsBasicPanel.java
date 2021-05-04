package org.erachain.gui.settings;

import org.erachain.gui.MainFrame;
import org.erachain.gui.library.FileChooser;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.lang.Lang;
import org.erachain.lang.LangFile;
import org.erachain.settings.Settings;
import org.erachain.webserver.SslUtils;
import org.mapdb.Fun;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.cert.Certificate;

@SuppressWarnings("serial")
public class SettingsBasicPanel extends JPanel {
    private final JLabel labelTextwebCertificatePass;
    private final JLabel labelTextWebKeystorePass;
    private final JButton buttonResetWebKeystoreFilePath;
    private final JButton buttonBrowseWebKeystoreFilePath;
    private final JLabel labelTextWebKeystoreFilePath;
    private final JButton buttonVeryfytWebKeystoreFilePath;
    public JTextField txtRpcPort;
    public JTextField textWebKeyStoreFilePath;
    public JTextField txtWebport;
    public JTextField textDataFolder;
    public JTextField textWallet;
    public JPasswordField textWebKeystorePass;
    public JPasswordField textWebCertificatePass;
    public JCheckBox chckbxGuiEnabled;
    public JCheckBox chckbxKeyCaching;
    public JCheckBox chckbxRpcEnabled;
    public JCheckBox chckbxWebEnabled;
    public JCheckBox chckbxWebUseSSL;
    public JCheckBox chckbxLocalPeersScannerEnabled;
    public JCheckBox chckbxSoundNewTransaction;
    public JCheckBox chckbxSoundReceiveMessage;
    public JCheckBox chckbxSoundReceivePayment;
    public JTextField textMinConnections;
    public JTextField textMaxConnections;
    public JDialog waitDialog;
    public JComboBox<LangFile> cbxListOfAvailableLangs;
    public JButton btnLoadNewLang;
    public JComboBox<String> size_Font;
    public JComboBox<String> font_Name;
    public JCheckBox chckbxGuiDynamic;

    public SettingsBasicPanel() {

        //PADDING
        this.setBorder(new EmptyBorder(10, 5, 5, 10));
        int panelRow=0;
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowHeights = new int[]{30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 0, 30, 40};
        //gridBagLayout.columnWidths = new int[] {40, 70, 92, 88, 92, 30, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        setLayout(gridBagLayout);

        chckbxGuiEnabled = new JCheckBox(Lang.T("GUI enabled"));
        chckbxGuiEnabled.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxGuiEnabled.setSelected(Settings.getInstance().isGuiEnabled());
        GridBagConstraints gbc_chckbxGuiEnabled = new GridBagConstraints();
        gbc_chckbxGuiEnabled.fill = GridBagConstraints.BOTH;
        gbc_chckbxGuiEnabled.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxGuiEnabled.gridwidth = 2;
        gbc_chckbxGuiEnabled.gridx = 1;
        gbc_chckbxGuiEnabled.gridy = panelRow;//0;
        add(chckbxGuiEnabled, gbc_chckbxGuiEnabled);


        chckbxGuiDynamic = new JCheckBox(Lang.T("GUI Dynamic"));
        chckbxGuiDynamic.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxGuiDynamic.setSelected(Settings.getInstance().isGuiDynamic());
        GridBagConstraints gbc_chckbxGuiDynamic = new GridBagConstraints();
        gbc_chckbxGuiDynamic.fill = GridBagConstraints.BOTH;
        gbc_chckbxGuiDynamic.insets = new Insets(0, 0, 5, 5);
        // gbc_chckbxGuiDynamic.anchor = GridBagConstraints.EAST;
        gbc_chckbxGuiDynamic.anchor = GridBagConstraints.WEST;
        // gbc_chckbxGuiDynamic.gridwidth = 2;
        gbc_chckbxGuiDynamic.gridx = 4;
        gbc_chckbxGuiDynamic.gridy = panelRow;//0;
        add(chckbxGuiDynamic, gbc_chckbxGuiDynamic);


        JLabel lblGUIExplanatoryText = new JLabel(Lang.T("Enable/Disable the Graphical User Interface."));
        lblGUIExplanatoryText.setVerticalAlignment(SwingConstants.TOP);
        lblGUIExplanatoryText.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_lblExplanatoryText = new GridBagConstraints();
        gbc_lblExplanatoryText.fill = GridBagConstraints.BOTH;
        gbc_lblExplanatoryText.insets = new Insets(0, 0, 5, 5);
        gbc_lblExplanatoryText.gridwidth = 4;
        gbc_lblExplanatoryText.gridx = 1;
        gbc_lblExplanatoryText.gridy = ++panelRow;//1;
        add(lblGUIExplanatoryText, gbc_lblExplanatoryText);

        chckbxRpcEnabled = new JCheckBox(Lang.T("RPC enabled"));
        chckbxRpcEnabled.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxRpcEnabled.setSelected(Settings.getInstance().isRpcEnabled());
        GridBagConstraints gbc_chckbxRpcEnabled = new GridBagConstraints();
        gbc_chckbxRpcEnabled.gridwidth = 2;
        gbc_chckbxRpcEnabled.fill = GridBagConstraints.BOTH;
        gbc_chckbxRpcEnabled.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxRpcEnabled.gridx = 1;
        gbc_chckbxRpcEnabled.gridy = ++panelRow;//2;
        add(chckbxRpcEnabled, gbc_chckbxRpcEnabled);

        JLabel lblRpcPort = new JLabel(Lang.T("RPC port") + ":");
        GridBagConstraints gbc_lblRpcPort = new GridBagConstraints();
        gbc_lblRpcPort.anchor = GridBagConstraints.EAST;
        gbc_lblRpcPort.insets = new Insets(0, 0, 5, 5);
        gbc_lblRpcPort.gridx = 3;
        gbc_lblRpcPort.gridy = panelRow;//2;
        add(lblRpcPort, gbc_lblRpcPort);

        txtRpcPort = new JTextField();
        txtRpcPort.setText(Integer.toString(Settings.getInstance().getRpcPort()));
        txtRpcPort.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_txtRpcPort = new GridBagConstraints();
        gbc_txtRpcPort.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtRpcPort.anchor = GridBagConstraints.WEST;
        gbc_txtRpcPort.insets = new Insets(0, 0, 5, 5);
        gbc_txtRpcPort.gridx = 4;
        gbc_txtRpcPort.gridy = panelRow;//2;
        add(txtRpcPort, gbc_txtRpcPort);
        txtRpcPort.setColumns(10);

        JLabel lblRPCExplanatoryText = new JLabel(Lang.T("Enable/Disable API calls using the given port."));
        lblRPCExplanatoryText.setVerticalAlignment(SwingConstants.TOP);
        lblRPCExplanatoryText.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_lblAnExplanatoryText_1 = new GridBagConstraints();
        gbc_lblAnExplanatoryText_1.fill = GridBagConstraints.BOTH;
        gbc_lblAnExplanatoryText_1.insets = new Insets(0, 0, 5, 0);
        gbc_lblAnExplanatoryText_1.gridwidth = 5;
        gbc_lblAnExplanatoryText_1.gridx = 1;
        gbc_lblAnExplanatoryText_1.gridy = ++panelRow;//3;
        add(lblRPCExplanatoryText, gbc_lblAnExplanatoryText_1);

        chckbxWebEnabled = new JCheckBox(Lang.T("WEB enabled"));
        chckbxWebEnabled.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxWebEnabled.setSelected(Settings.getInstance().isWebEnabled());
        GridBagConstraints gbc_chckbxWebEnabled = new GridBagConstraints();
        //gbc_chckbxWebEnabled.gridwidth = 2;
        gbc_chckbxWebEnabled.fill = GridBagConstraints.BOTH;
        gbc_chckbxWebEnabled.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxWebEnabled.gridx = 1;
        gbc_chckbxWebEnabled.gridy = ++panelRow;//4;
        add(chckbxWebEnabled, gbc_chckbxWebEnabled);

        //Use SSL checkBox
        chckbxWebUseSSL = new JCheckBox(Lang.T("Use SSL"));
        chckbxWebUseSSL.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxWebUseSSL.setSelected(Settings.getInstance().isWebUseSSL());
        GridBagConstraints gbc_chckbxchckbxUseSSL = new GridBagConstraints();
        gbc_chckbxchckbxUseSSL.fill = GridBagConstraints.BOTH;
        gbc_chckbxchckbxUseSSL.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxchckbxUseSSL.gridx = 2;
        gbc_chckbxchckbxUseSSL.gridy = panelRow;//4;
        add(chckbxWebUseSSL, gbc_chckbxchckbxUseSSL);


        chckbxWebUseSSL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                    setEnableSslSettingOption();

            }
        }
       );

    // web settings
        JLabel lblWebPort = new JLabel(Lang.T("WEB port") + ":");
        GridBagConstraints gbc_lblWebPort = new GridBagConstraints();
        gbc_lblWebPort.anchor = GridBagConstraints.EAST;
        gbc_lblWebPort.insets = new Insets(0, 0, 5, 5);
        gbc_lblWebPort.gridx = 3;
        gbc_lblWebPort.gridy = panelRow;//4;
        add(lblWebPort, gbc_lblWebPort);

        txtWebport = new JTextField();
        txtWebport.setText(Integer.toString(Settings.getInstance().getWebPort()));
        txtWebport.setHorizontalAlignment(SwingConstants.LEFT);
        txtWebport.setColumns(10);
        GridBagConstraints gbc_txtWebport = new GridBagConstraints();
        gbc_txtWebport.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtWebport.anchor = GridBagConstraints.WEST;
        gbc_txtWebport.insets = new Insets(0, 0, 5, 5);
        gbc_txtWebport.gridx = 4;
        gbc_txtWebport.gridy = panelRow;//4;
        add(txtWebport, gbc_txtWebport);

        // { WEB SSL Settings
        // Keystore File
        labelTextWebKeystoreFilePath = new JLabel(Lang.T("Keystore file path") + ":");
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = ++panelRow;//20;
        add(labelTextWebKeystoreFilePath, gridBagConstraints);

        textWebKeyStoreFilePath = new JTextField();
        textWebKeyStoreFilePath.setText(Settings.getInstance().getWebKeyStorePath());
        textWebKeyStoreFilePath.setHorizontalAlignment(SwingConstants.LEFT);
        textWebKeyStoreFilePath.setColumns(10);
        textWebKeyStoreFilePath.setEditable(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = panelRow;//20;
        add(textWebKeyStoreFilePath, gridBagConstraints);

        buttonBrowseWebKeystoreFilePath = new JButton(Lang.T("Browse..."));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = panelRow;//20;
        buttonBrowseWebKeystoreFilePath.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileChooser fileopen = new FileChooser();
                fileopen.setFileSelectionMode(FileChooser.FILES_ONLY);
                fileopen.setCurrentDirectory(new File(textWebKeyStoreFilePath.getText()));
                int ret = fileopen.showDialog(null, Lang.T("Set Keystore file path"));
                if (ret == FileChooser.APPROVE_OPTION) {
                    textWebKeyStoreFilePath.setText(fileopen.getSelectedFile().toString());
                }
            }
        });
        add(buttonBrowseWebKeystoreFilePath, gridBagConstraints);
        // Reset
        buttonResetWebKeystoreFilePath = new JButton(Lang.T("Reset"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = panelRow;//20;
        buttonResetWebKeystoreFilePath.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textWebKeyStoreFilePath.setText(Settings.getInstance().getWebKeyStorePath());
            }
        });
        add(buttonResetWebKeystoreFilePath, gridBagConstraints);
        //reset
        // Keystore password
        labelTextWebKeystorePass = new JLabel(Lang.T("Keystore password") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = ++panelRow;//21;
        add(labelTextWebKeystorePass, gridBagConstraints);

        textWebKeystorePass = new JPasswordField();
        textWebKeystorePass.setText(Settings.getInstance().getWebKeyStorePassword());
        textWebKeystorePass.setHorizontalAlignment(SwingConstants.LEFT);
        textWebKeystorePass.setColumns(10);
        //textKeystorePass.setEditable(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = panelRow;//21;
        add(textWebKeystorePass, gridBagConstraints);

        // certificate password
        labelTextwebCertificatePass = new JLabel(Lang.T("Certificate password") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = panelRow;//21;
        add(labelTextwebCertificatePass, gridBagConstraints);

        textWebCertificatePass = new JPasswordField();
        textWebCertificatePass.setText(Settings.getInstance().getWebStoreSourcePassword());
        textWebCertificatePass.setHorizontalAlignment(SwingConstants.LEFT);
        textWebCertificatePass.setColumns(10);
        //textlblCertificatePassPass.setEditable(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = panelRow;//21;
        add(textWebCertificatePass, gridBagConstraints);

        // verifi button
        buttonVeryfytWebKeystoreFilePath = new JButton(Lang.T("Check") + " SSL");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = panelRow;//20;
        buttonVeryfytWebKeystoreFilePath.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Fun.Tuple3<KeyStore, Certificate, String> result = SslUtils.getWebKeyStore(textWebKeyStoreFilePath.getText(),new String(textWebKeystorePass.getPassword()), new String(textWebCertificatePass.getPassword()));
                    if(result.a == null){
                        JOptionPane.showMessageDialog(
                                new JFrame(), Lang.T(result.c),
                                Lang.T("Error!"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                    else{
                        Certificate cetrtyficatemy = result.b;

                        IssueConfirmDialog dd = new IssueConfirmDialog(null, true, null,
                                Lang.T("SSL is OK "), MainFrame.getInstance().getWidth() - 100, MainFrame.getInstance().getHeight() - 100, Lang.T(""));

                        dd.setTitle(Lang.T("SSL is OK."));
                        dd.jTextPane1.setText("Certificate: \n " + cetrtyficatemy.toString());
                        dd.jButtonRAW.setVisible(false);
                        dd.jButtonFREE.setVisible(false);
                        dd.jButtonGO.setText(Lang.T("OK"));
                        dd.setLocationRelativeTo(null);
                        dd.setVisible(true);

                    }
                } catch (FileNotFoundException e1) {
                    JOptionPane.showMessageDialog(
                            new JFrame(), Lang.T(e1.getLocalizedMessage()),
                            Lang.T("Error!"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        add(buttonVeryfytWebKeystoreFilePath, gridBagConstraints);

        // WEB SSL Settings }

        JLabel lblWEBExplanatoryText = new JLabel(Lang.T("Enable/Disable decentralized web server. Use \"Access permission\" tab for additional options."));
        lblWEBExplanatoryText.setVerticalAlignment(SwingConstants.TOP);
        lblWEBExplanatoryText.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_lblAnExplanatoryText_2 = new GridBagConstraints();
        gbc_lblAnExplanatoryText_2.fill = GridBagConstraints.BOTH;
        gbc_lblAnExplanatoryText_2.insets = new Insets(0, 0, 5, 0);
        gbc_lblAnExplanatoryText_2.gridwidth = 5;
        gbc_lblAnExplanatoryText_2.gridx = 1;
        gbc_lblAnExplanatoryText_2.gridy = ++panelRow;//5;
        add(lblWEBExplanatoryText, gbc_lblAnExplanatoryText_2);

        chckbxKeyCaching = new JCheckBox(Lang.T("Generator Key Caching"));
        chckbxKeyCaching.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxKeyCaching.setSelected(Settings.getInstance().isGeneratorKeyCachingEnabled());
        GridBagConstraints gbc_chckbxKeyCaching = new GridBagConstraints();
        gbc_chckbxKeyCaching.fill = GridBagConstraints.BOTH;
        gbc_chckbxKeyCaching.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxKeyCaching.gridwidth = 4;
        gbc_chckbxKeyCaching.gridx = 1;
        gbc_chckbxKeyCaching.gridy = ++panelRow;//6;
        add(chckbxKeyCaching, gbc_chckbxKeyCaching);

        JLabel lblKeyCachingExplanatoryText = new JLabel(Lang.T("Allows forging even when your wallet is locked. You need to unlock it once."));
        lblKeyCachingExplanatoryText.setVerticalAlignment(SwingConstants.TOP);
        lblKeyCachingExplanatoryText.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_lblAnExplanatoryText_3 = new GridBagConstraints();
        gbc_lblAnExplanatoryText_3.fill = GridBagConstraints.BOTH;
        gbc_lblAnExplanatoryText_3.insets = new Insets(0, 0, 5, 5);
        gbc_lblAnExplanatoryText_3.gridwidth = 5;
        gbc_lblAnExplanatoryText_3.gridx = 1;
        gbc_lblAnExplanatoryText_3.gridy = ++panelRow;//7;
        add(lblKeyCachingExplanatoryText, gbc_lblAnExplanatoryText_3);

        JLabel lblDataDir = new JLabel(Lang.T("DataChain dir") + ":");
        GridBagConstraints gbc_lblDataDir = new GridBagConstraints();
        gbc_lblDataDir.anchor = GridBagConstraints.WEST;
        gbc_lblDataDir.insets = new Insets(0, 0, 5, 5);
        gbc_lblDataDir.gridx = 1;
        gbc_lblDataDir.gridy = ++panelRow;//8;
        add(lblDataDir, gbc_lblDataDir);

        textDataFolder = new JTextField();
        textDataFolder.setText(Settings.getInstance().getDataChainPath());
        textDataFolder.setHorizontalAlignment(SwingConstants.LEFT);
        textDataFolder.setColumns(10);
        textDataFolder.setEditable(false);
        GridBagConstraints gbc_textDataFolder = new GridBagConstraints();
        gbc_textDataFolder.gridwidth = 2;
        gbc_textDataFolder.insets = new Insets(0, 0, 5, 5);
        gbc_textDataFolder.fill = GridBagConstraints.HORIZONTAL;
        gbc_textDataFolder.gridx = 2;
        gbc_textDataFolder.gridy = panelRow;//8;
        add(textDataFolder, gbc_textDataFolder);

        JButton btnBrowseDataFolder = new JButton(Lang.T("Browse..."));
        GridBagConstraints gbc_btnBrowseDataFolder = new GridBagConstraints();
        gbc_btnBrowseDataFolder.anchor = GridBagConstraints.WEST;
        gbc_btnBrowseDataFolder.insets = new Insets(0, 0, 5, 5);
        gbc_btnBrowseDataFolder.gridx = 4;
        gbc_btnBrowseDataFolder.gridy = panelRow;//8;
        btnBrowseDataFolder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileChooser fileopen = new FileChooser();
                fileopen.setFileSelectionMode(FileChooser.DIRECTORIES_ONLY);
                fileopen.setCurrentDirectory(new File(textDataFolder.getText()));
                int ret = fileopen.showDialog(null, Lang.T("Set datachain dir"));
                if (ret == FileChooser.APPROVE_OPTION) {
                    String path = Settings.normalizePath(fileopen.getSelectedFile().toString());
                    textDataFolder.setText(path);
                }
            }
        });
        add(btnBrowseDataFolder, gbc_btnBrowseDataFolder);
        // AS
        JButton resetDataDirButton = new JButton(Lang.T("Reset"));
        GridBagConstraints gbc_resetDataDirButton = new GridBagConstraints();
        gbc_resetDataDirButton.anchor = GridBagConstraints.WEST;
        gbc_resetDataDirButton.insets = new Insets(0, 0, 5, 5);
        gbc_resetDataDirButton.gridx = 5;
        gbc_resetDataDirButton.gridy = panelRow;//8;
        resetDataDirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textDataFolder.setText(Settings.DEFAULT_DATA_CHAIN_DIR);
            }
        });
        add(resetDataDirButton, gbc_resetDataDirButton);

        //AS
        JLabel lblWelletDir = new JLabel(Lang.T("WalletKeys dir") + ":");
        GridBagConstraints gbc_lblWelletDir = new GridBagConstraints();
        gbc_lblWelletDir.anchor = GridBagConstraints.WEST;
        gbc_lblWelletDir.insets = new Insets(0, 0, 5, 5);
        gbc_lblWelletDir.gridx = 1;
        gbc_lblWelletDir.gridy = ++panelRow;//9;
        add(lblWelletDir, gbc_lblWelletDir);

        textWallet = new JTextField();
        textWallet.setText(Settings.getInstance().getWalletKeysPath());
        textWallet.setHorizontalAlignment(SwingConstants.LEFT);
        textWallet.setColumns(10);
        textWallet.setEditable(false);
        GridBagConstraints gbc_textWallet = new GridBagConstraints();
        gbc_textWallet.gridwidth = 2;
        gbc_textWallet.insets = new Insets(0, 0, 5, 5);
        gbc_textWallet.fill = GridBagConstraints.HORIZONTAL;
        gbc_textWallet.gridx = 2;
        gbc_textWallet.gridy = panelRow;//9;
        add(textWallet, gbc_textWallet);

        JButton btnBrowseWallet = new JButton(Lang.T("Browse..."));
        GridBagConstraints gbc_BrowseWalletbutton = new GridBagConstraints();
        gbc_BrowseWalletbutton.anchor = GridBagConstraints.WEST;
        gbc_BrowseWalletbutton.insets = new Insets(0, 0, 5, 5);
        gbc_BrowseWalletbutton.gridx = 4;
        gbc_BrowseWalletbutton.gridy = panelRow;//9;

        btnBrowseWallet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser();
                fileopen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileopen.setCurrentDirectory(new File(textWallet.getText()));
                int ret = fileopen.showDialog(null, Lang.T("Set walletKeys dir"));
                if (ret == JFileChooser.APPROVE_OPTION) {
                    String path = Settings.normalizePath(fileopen.getSelectedFile().toString());
                    textWallet.setText(path);
                }
            }
        });

        add(btnBrowseWallet, gbc_BrowseWalletbutton);

        // AS
        JButton resetWaletDirButton = new JButton(Lang.T("Reset"));
        GridBagConstraints gbc_resetWaletDirButton = new GridBagConstraints();
        gbc_resetWaletDirButton.anchor = GridBagConstraints.WEST;
        gbc_resetWaletDirButton.insets = new Insets(0, 0, 5, 5);
        gbc_resetWaletDirButton.gridx = 5;
        gbc_resetWaletDirButton.gridy = panelRow;//9;
        resetWaletDirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textWallet.setText(Settings.DEFAULT_WALLET_KEYS_DIR);
            }
        });
        add(resetWaletDirButton, gbc_resetWaletDirButton);

        //AS


        JLabel lblAnExplanatoryText_4 = new JLabel(Lang.T("The data folder contains blockchain data. The wallet dir contains user specific data."));
        lblAnExplanatoryText_4.setVerticalAlignment(SwingConstants.TOP);
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel_1.gridwidth = 4;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_1.gridx = 1;
        gbc_lblNewLabel_1.gridy = ++panelRow;//10;
        add(lblAnExplanatoryText_4, gbc_lblNewLabel_1);

        JLabel lblMinConnections = new JLabel(Lang.T("Min connections") + ":");
        GridBagConstraints gbc_lblMinConnections = new GridBagConstraints();
        gbc_lblMinConnections.anchor = GridBagConstraints.EAST;
        gbc_lblMinConnections.insets = new Insets(0, 0, 5, 5);
        gbc_lblMinConnections.gridx = 1;
        gbc_lblMinConnections.gridy = ++panelRow;//11;
        add(lblMinConnections, gbc_lblMinConnections);

        textMinConnections = new JTextField();
        textMinConnections.setText(Integer.toString(Settings.getInstance().getMinConnections()));
        textMinConnections.setHorizontalAlignment(SwingConstants.LEFT);
        textMinConnections.setColumns(10);
        GridBagConstraints gbc_textMinConnections = new GridBagConstraints();
        gbc_textMinConnections.anchor = GridBagConstraints.WEST;
        gbc_textMinConnections.insets = new Insets(0, 0, 5, 5);
        gbc_textMinConnections.fill = GridBagConstraints.HORIZONTAL;
        gbc_textMinConnections.gridx = 2;
        gbc_textMinConnections.gridy = panelRow;//11;
        add(textMinConnections, gbc_textMinConnections);

        JLabel lblMaxConnections = new JLabel(Lang.T("Max connections") + ":");
        GridBagConstraints gbc_lblMaxConnections = new GridBagConstraints();
        gbc_lblMaxConnections.anchor = GridBagConstraints.EAST;
        gbc_lblMaxConnections.insets = new Insets(0, 0, 5, 5);
        gbc_lblMaxConnections.gridx = 3;
        gbc_lblMaxConnections.gridy = panelRow;//11;
        add(lblMaxConnections, gbc_lblMaxConnections);

        textMaxConnections = new JTextField();
        textMaxConnections.setText(Integer.toString(Settings.getInstance().getMaxConnections()));
        textMaxConnections.setHorizontalAlignment(SwingConstants.LEFT);
        textMaxConnections.setColumns(10);
        GridBagConstraints gbc_textMaxConnections = new GridBagConstraints();
        gbc_textMaxConnections.anchor = GridBagConstraints.WEST;
        gbc_textMaxConnections.insets = new Insets(0, 0, 5, 5);
        gbc_textMaxConnections.fill = GridBagConstraints.HORIZONTAL;
        gbc_textMaxConnections.gridx = 4;
        gbc_textMaxConnections.gridy = panelRow;//11;
        add(textMaxConnections, gbc_textMaxConnections);

        JLabel lbllimitConnections = new JLabel(Lang.T("Allows you to change the amount of simultaneous connections to the server."));
        lbllimitConnections.setVerticalAlignment(SwingConstants.TOP);
        GridBagConstraints gbc_lbllimitConnections = new GridBagConstraints();
        gbc_lbllimitConnections.fill = GridBagConstraints.BOTH;
        gbc_lbllimitConnections.gridwidth = 4;
        gbc_lbllimitConnections.insets = new Insets(0, 0, 0, 5);
        gbc_lbllimitConnections.gridx = 1;
        gbc_lbllimitConnections.gridy = ++panelRow;//12;
        add(lbllimitConnections, gbc_lbllimitConnections);

        chckbxLocalPeersScannerEnabled = new JCheckBox(Lang.T("Local peer discovery"));
        chckbxLocalPeersScannerEnabled.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxLocalPeersScannerEnabled.setSelected(Settings.getInstance().isLocalPeersScannerEnabled());
        GridBagConstraints gbc_chckbxLocalPeersScannerEnabled = new GridBagConstraints();
        gbc_chckbxLocalPeersScannerEnabled.gridwidth = 2;
        gbc_chckbxLocalPeersScannerEnabled.fill = GridBagConstraints.BOTH;
        gbc_chckbxLocalPeersScannerEnabled.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxLocalPeersScannerEnabled.gridx = 1;
        gbc_chckbxLocalPeersScannerEnabled.gridy = ++panelRow;//13;
        add(chckbxLocalPeersScannerEnabled, gbc_chckbxLocalPeersScannerEnabled);

        setEnableSslSettingOption();

    }
    private void setEnableSslSettingOption(){
        labelTextWebKeystoreFilePath.setEnabled(chckbxWebUseSSL.isSelected());
        buttonBrowseWebKeystoreFilePath.setEnabled(chckbxWebUseSSL.isSelected());
        buttonResetWebKeystoreFilePath.setEnabled(chckbxWebUseSSL.isSelected());
        textWebKeyStoreFilePath.setEnabled(chckbxWebUseSSL.isSelected());
        labelTextWebKeystorePass.setEnabled(chckbxWebUseSSL.isSelected());
        textWebKeystorePass.setEnabled(chckbxWebUseSSL.isSelected());
        labelTextwebCertificatePass.setEnabled(chckbxWebUseSSL.isSelected());
        textWebCertificatePass.setEnabled(chckbxWebUseSSL.isSelected());

    }
}