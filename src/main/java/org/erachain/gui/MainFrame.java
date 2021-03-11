package org.erachain.gui;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.blockexplorer.WebTransactionsHTML;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionFactory;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.accounts.AccountAssetSendPanel;
import org.erachain.gui.library.*;
import org.erachain.gui.status.StatusPanel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.SaveStrToFile;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.*;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements Observer {
    private static MainFrame instance;
    private JSONObject settingsJSONbuf;
    private JSONObject main_Frame_settingsJSON;
    // Variables declaration - do not modify
    public MenuFiles jMenu_Files;
    private MenuDeals jMenu2;
    private JMenu jMenuTX;
    private MenuExchange jMenuExchange;
    private javax.swing.JMenuBar jMenuBar1;
    public MainPanel mainPanel;
    private StatusPanel statusPanel;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration
    //private MainFrame th;
    protected Logger logger = LoggerFactory.getLogger(MainFrame.class.getName());

    private MainFrame() {

        // CREATE FRAME
        super(Controller.getInstance().getApplicationName(true) + "   " + Lang.T("KEYS") + ": " + Settings.getInstance().getWalletKeysPath());
        this.setVisible(false);

        //th = this;
        //    this.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        Controller.getInstance().addObserver(this);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        settingsJSONbuf = new JSONObject();
        settingsJSONbuf = Settings.getInstance().Dump();
        initComponents();

        // WALLET STATS
        this.add(new StatusPanel(), BorderLayout.SOUTH);
        this.setVisible(true);
    }

    public static MainFrame getInstance() {
        if (instance == null) {
            instance = new MainFrame();
        }
        return instance;
    }

    public void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jTabbedPane1 = new javax.swing.JTabbedPane();
        mainPanel = MainPanel.getInstance();
        // statusPanel = new StatusPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu_Files = new MenuFiles();
        jMenu2 = new MenuDeals();
        jMenuExchange = new MenuExchange();

        jMenuTX = new JMenu(Lang.T("Transaction"));

        JMenuItem readTransItemJSON = new JMenuItem(Lang.T("Read Transaction from JSON"));
        readTransItemJSON.getAccessibleContext().setAccessibleDescription(Lang.T("Read Transaction as JSON"));
        //readTransItemJSON.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
        readTransItemJSON.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

//        		String raw = Base58.encode(transaction.toBytes(false, null));
                FileChooser chooser = new FileChooser();
                chooser.setDialogTitle(Lang.T("Open File"));
                //chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Erachain TX", "json");
                chooser.setFileFilter(filter);

                //chooser.setAcceptAllFileFilterUsed(false);
                String res = "";
                if (chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {

                    String pp = chooser.getSelectedFile().getPath();

                    File ff = new File(pp);

                    // new ArrayList<String>();
                    try {
                        BufferedReader in = new BufferedReader(new FileReader(ff));
                        String str;
                        while ((str = in.readLine()) != null) {
                            res += (str);
                        }
                        in.close();
                    } catch (IOException e1) {
                        logger.error(e1.getMessage(), e1);
                        return;
                    }

  			/*	 try(FileOutputStream fos=new FileOutputStream(pp))
  		        {
  		            // перевод строки в байты
  				//	String ssst = model.getValueAt(row, 2).toString();
  		            byte[] buffer =transaction.toBytes(false, null);
  		            // if ZIP

  		            fos.wri.write(buffer, 0, buffer.length);

  		        }
  		        catch(IOException ex){

  		            System.out.println(ex.getMessage());
  		        }
  	          */
                }

                try {
                    JSONObject js = new JSONObject();
                    js = (JSONObject) JSONValue.parse(res);
                    String creator = "";
                    if (!js.containsKey("type")) return;
                    int type = ((Long) js.get("type")).intValue();
                    if (type != 31) return;
                    if (js.containsKey("creator")) creator = (String) js.get("creator");
                    Controller ct = Controller.getInstance();
                    if (!js.containsKey("asset")) return;
                    long assetKey = ((Long) js.get("asset"));
                    if (!js.containsKey("recipient")) return;
                    Account recipient = Account.tryMakeAccount((String) js.get("recipient")).a;
                    if (!js.containsKey("head")) return;
                    String head = (String) js.get("head");
                    if (!js.containsKey("amount")) return;
                    String amount = (String) js.get("amount");
                    Boolean backward = (Boolean) js.get("backward");
                    AccountAssetSendPanel panel = new AccountAssetSendPanel(ct.getAsset(assetKey),
                            ct.getWalletAccountByAddress(creator), recipient, null, null, backward);
                    MainPanel.getInstance().insertNewTab(Lang.T("Read Transaction"),
                            panel);

                    AssetCls asset = ct.getAsset(assetKey);
                    panel.recipientAddress.setSelectedAccount(recipient);
                    panel.recipientAddress.setEditable(false);
                    panel.jTextFieldTXTitle.setText(head);
                    panel.jTextFieldTXTitle.setEditable(false);
                    panel.jTextField_Amount.setText(amount);
                    panel.jTextField_Amount.setEditable(false);
                    panel.jComboBox_Asset.setSelectedItem(asset);
                    panel.jComboBox_Asset.setEnabled(false);
                } catch (Exception e1) {
                    logger.error(e1.getMessage(), e1);
                }

            }
        });
        if (BlockChain.DEMO_MODE)
            jMenuTX.add(readTransItemJSON);

        JMenuItem readTransItemRAW = new JMenuItem(Lang.T("Read Transaction as RAW"));
        readTransItemRAW.getAccessibleContext().setAccessibleDescription(Lang.T("Read Transaction as RAW"));
        readTransItemRAW.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
        readTransItemRAW.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

//        		String raw = Base58.encode(transaction.toBytes(false, null));
                FileChooser chooser = new FileChooser();
                chooser.setDialogTitle(Lang.T("Open File"));
                //chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Erachain TX in RAW", "raw58", "raw64");
                chooser.setFileFilter(filter);

                //chooser.setAcceptAllFileFilterUsed(false);
                String res = "";
                String fileExt = "";

                if (chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {

                    String fileName = chooser.getSelectedFile().getPath();

                    int i = fileName.lastIndexOf('.');
                    if (i > 0) {
                        fileExt = fileName.substring(i + 1);
                    }

                    File ff = new File(fileName);

                    // new ArrayList<String>();
                    try {
                        BufferedReader in = new BufferedReader(new FileReader(ff));
                        String str;
                        while ((str = in.readLine()) != null) {
                            res += (str).trim();
                        }
                        in.close();
                    } catch (IOException e1) {
                        logger.error(e1.getMessage(), e1);
                        return;
                    }

                    byte[] data = fileExt.equals("raw58") ? Base58.decode(res)
                            : Base64.getDecoder().decode(res);
                    Transaction transaction;

                    try {
                        transaction = TransactionFactory.getInstance().parse(data, Transaction.FOR_NETWORK);
                        if (transaction != null) {
                            WebTransactionsHTML webHTML = new WebTransactionsHTML(DCSet.getInstance(), Lang.getInstance().getLangForNode());
                            JSONObject outJson = webHTML.get_HTML_Body(transaction, "");
                            String htmlDescr = (String) outJson.get("head");
                            htmlDescr += (String) outJson.get("body");
                            htmlDescr = transaction.toJson().toJSONString();
                            htmlDescr = htmlDescr.replace(",", ",<br>");
                            //IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                            //        (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), "");
                            IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                                    htmlDescr, (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), "",
                                    Lang.T("CHECK") + " " + transaction.viewFullTypeName());
                            confirmDialog.setVisible(true);

                            if (confirmDialog.isConfirm > 0) {
                                ResultDialog.make(null, transaction, confirmDialog.isConfirm == IssueConfirmDialog.TRY_FREE);
                            }

                        }

                    } catch (Exception e1) {
                        logger.error(e1.getMessage(), e1);
                    }

                }

            }
        });
        jMenuTX.add(readTransItemRAW);

        jMenuTX.addSeparator();

        // getContentPane().setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        // getContentPane().add(jTabbedPane1, gridBagConstraints);

        add(jTabbedPane1, BorderLayout.NORTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.2;
        // getContentPane().add(jPanelHead, gridBagConstraints);

        add(mainPanel, BorderLayout.CENTER);

        // javax.swing.GroupLayout jPanel2Layout = new
        // javax.swing.GroupLayout(statusPanel);
        // statusPanel.setLayout(jPanel2Layout);
        // jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        // .addGap(0, 0, Short.MAX_VALUE));
        // jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        // .addGap(0, 0, Short.MAX_VALUE));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        // getContentPane().add(jPanelCopyButton, gridBagConstraints);
        // this.add(new StatusPanel(), BorderLayout.SOUTH);

        jMenu_Files.setText(Lang.T("File"));
        jMenuBar1.add(jMenu_Files);

        jMenuTX.setText(Lang.T("Transaction"));
        jMenuBar1.add(jMenuTX);

        jMenu2.setText(Lang.T("Deals"));
        jMenuBar1.add(jMenu2);

        jMenuExchange.setText(Lang.T("Exchange"));
        jMenuBar1.add(jMenuExchange);


        setJMenuBar(jMenuBar1);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowActivated(WindowEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowClosed(WindowEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowClosing(WindowEvent arg0) {
                // TODO Auto-generated method stub
                // read settings
                // You can still stop closing if you want to
                int res = JOptionPane.showConfirmDialog(instance, Lang.T("Are you sure you want to close?"), Lang.T("Close?"), JOptionPane.YES_NO_OPTION);
                if (res != 0) return;
                closeFrame();
                dispose();
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new ClosingDialog();
                    }
                });
            }

            @Override
            public void windowDeactivated(WindowEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowDeiconified(WindowEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowIconified(WindowEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowOpened(WindowEvent arg0) {
                // TODO Auto-generated method stub
            }

        });

        pack();
        // set perameters size $ split panel
        int devLastLoc = 250;
        int devLoc = 250;
        int x = 0;
        int y = 0;
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screens = kit.getScreenSize();
        int h = screens.height - 50;
        int w = screens.width - 50;
        int orientation = 1;

        if (settingsJSONbuf.containsKey("Main_Frame_Setting")) {
            main_Frame_settingsJSON = new JSONObject();
            main_Frame_settingsJSON = (JSONObject) settingsJSONbuf.get("Main_Frame_Setting");

            if (main_Frame_settingsJSON.containsKey("Main_Frame_Loc_X"))
                x = new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Loc_X")); // x
            if (main_Frame_settingsJSON.containsKey("Main_Frame_Loc_Y"))
                y = new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Loc_Y")); // y

            if (main_Frame_settingsJSON.containsKey("Main_Frame_Height"))
                h = new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Height")); // высота
            if (main_Frame_settingsJSON.containsKey("Main_Frame_Width"))
                w = new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Width")); // длина

            if (main_Frame_settingsJSON.containsKey("Main_Frame_is_Max")) {
                Boolean bb = new Boolean((String) main_Frame_settingsJSON.get("Main_Frame_is_Max"));
                if (bb) {
                    this.setExtendedState(MAXIMIZED_BOTH);
                }
            }

            if (main_Frame_settingsJSON.containsKey("Main_Frame_Div_Orientation"))
                orientation = new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Div_Orientation"));

            if (main_Frame_settingsJSON.containsKey("Main_Frame_Div_Loc"))
                devLoc = new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Div_Loc"));

            if (main_Frame_settingsJSON.containsKey("Main_Frame_Div_Last_Loc"))
                devLastLoc = new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Div_Last_Loc"));

            // load tabs
            if (main_Frame_settingsJSON.containsKey("OpenTabbeds")) {
                JSONObject openTabes = (JSONObject) main_Frame_settingsJSON.get("OpenTabbeds");
                Set ot = openTabes.keySet();
                for (int i = 0; i < ot.size(); i++) {
                    String value = (String) openTabes.get(i + "");
                    mainPanel.addTab(value);
                }

                if (main_Frame_settingsJSON.containsKey("Main_Frame_Selected_Tab"))
                    try {
                        mainPanel.jTabbedPane1.setSelectedIndex(
                                new Integer((String) main_Frame_settingsJSON.get("Main_Frame_Selected_Tab")));
                    } catch (Exception e) {
                    }
            }

        } else {
            this.setExtendedState(MAXIMIZED_BOTH);

            // setExtendedState(MAXIMIZED_BOTH);
            // mainPanel.jSplitPane1.setDividerLocation(250);
            // mainPanel.jSplitPane1.setLastDividerLocation(300);

        }
        setLocation(x, y);
        setSize(w, h);
        mainPanel.jSplitPane1.setOrientation(orientation);
        mainPanel.jSplitPane1.setLastDividerLocation(devLastLoc);
        mainPanel.jSplitPane1.setDividerLocation(devLoc);
        mainPanel.jSplitPane1.set_button_title(); // set title diveders
        // buttons

        // reat Main tree

        if (settingsJSONbuf.containsKey("Main_Tree")) {
            JSONObject aa = (JSONObject) settingsJSONbuf.get("Main_Tree");
            Iterator<?> it = aa.values().iterator();
            TreeSet s1 = new TreeSet();
            while (it.hasNext()) {
                long d2 = Long.parseLong(it.next().toString());
                s1.add(d2);

            }
            Iterator<?> s1_It = s1.iterator();
            while (s1_It.hasNext()) {
                long sa = Long.parseLong(s1_It.next().toString());
                mainPanel.mlp.tree.tree.collapseRow(Integer.valueOf((int) sa));

            }

        }
    }// </editor-fold>

    @Override
    public void update(Observable arg0, Object arg1) {

        ObserverMessage message = (ObserverMessage) arg1;
        if (message.getType() == ObserverMessage.NETWORK_STATUS) {
            int status = (int) message.getValue();

            if (status == Controller.STATUS_NO_CONNECTIONS) {
                List<Image> icons = new ArrayList<Image>();
                icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16_No.png"));
                icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32_No.png"));
                icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32_No.png"));
                icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32_No.png"));
                this.setIconImages(icons);

            }
            if (status == Controller.STATUS_SYNCHRONIZING) {
                List<Image> icons = new ArrayList<Image>();
                icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16_Se.png"));
                icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32_Se.png"));
                icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32_Se.png"));
                icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32_Se.png"));
                this.setIconImages(icons);
            }
            if (status == Controller.STATUS_OK) {
                // ICON
                List<Image> icons = new ArrayList<Image>();
                icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
                icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
                icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
                icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
                this.setIconImages(icons);
            }
        }

    }

    public void closeFrame() {
        int lDiv;
        int div;
        SplitPanel sP;
        HashMap outOpenTabbeds = new HashMap();
        JSONObject settingsJSON = new JSONObject();
        settingsJSONbuf = Settings.getInstance().getJSONObject();
        if (settingsJSONbuf.containsKey("Main_Frame_Setting"))
            settingsJSON = (JSONObject) settingsJSONbuf.get("Main_Frame_Setting");
        if (getExtendedState() != MAXIMIZED_BOTH) {
            settingsJSON.put("Main_Frame_is_Max", "false");
            settingsJSON.put("Main_Frame_Height", getHeight() + ""); // высота
            settingsJSON.put("Main_Frame_Width", getWidth() + ""); // длина
            settingsJSON.put("Main_Frame_Loc_X", getX() + ""); // высота
            settingsJSON.put("Main_Frame_Loc_Y", getY() + ""); // высота
        } else {

            settingsJSON.put("Main_Frame_is_Max", "true");
        }

        settingsJSON.put("Main_Frame_Div_Orientation", mainPanel.jSplitPane1.getOrientation() + "");
        // horisontal - vertical orientation
        lDiv = mainPanel.jSplitPane1.getLastDividerLocation();
        div = mainPanel.jSplitPane1.getDividerLocation();

        settingsJSON.put("Main_Frame_Div_Last_Loc", lDiv + "");
        settingsJSON.put("Main_Frame_Div_Loc", div + "");
        settingsJSON.remove("OpenTabbeds");
        for (int i = 0; i < mainPanel.jTabbedPane1.getTabCount(); i++) {
            // write in setting opet tabbs
            Component comp = mainPanel.jTabbedPane1.getComponentAt(i);
            outOpenTabbeds.put(i, comp.getClass().getSimpleName());

            // write open tabbed settings Split panel
            if (comp instanceof SplitPanel) {
                HashMap outTabbedDiv = new HashMap();

                sP = ((SplitPanel) comp);
                outTabbedDiv.put("Div_Orientation", sP.jSplitPanel.getOrientation() + "");

                // write

                lDiv = sP.jSplitPanel.getLastDividerLocation();
                div = sP.jSplitPanel.getDividerLocation();

                outTabbedDiv.put("Div_Last_Loc", lDiv + "");
                outTabbedDiv.put("Div_Loc", div + "");

                settingsJSON.put(comp.getClass().getSimpleName(), outTabbedDiv);
            }

            settingsJSON.put("OpenTabbeds", outOpenTabbeds);

        }
        settingsJSON.put("Main_Frame_Selected_Tab", mainPanel.jTabbedPane1.getSelectedIndex() + "");

        settingsJSONbuf.put("Main_Frame_Setting", settingsJSON);
        settingsJSONbuf.put("FileChooser_Path", new String(FileChooser.get_Default_Path()));
        settingsJSONbuf.put("FileChooser_Wight", FileChooser.get_Default_Width());
        settingsJSONbuf.put("FileChooser_Height", FileChooser.get_Default_Height());

        settingsJSONbuf.put("Telegram_Sender", Settings.getInstance().getTelegramDefaultSender());
        settingsJSONbuf.put("Telegram_Reciever", Settings.getInstance().getTelegramDefaultReciever());
        settingsJSONbuf.put("Telegram_Ratio_Reciever", Settings.getInstance().getTelegramRatioReciever());

        // saving menu
        int tree_Row = 0;
        HashMap treeJSON = new HashMap();
        for (int rr = 0; rr < mainPanel.mlp.tree.tree.getRowCount(); rr++) {

            if (mainPanel.mlp.tree.tree.isCollapsed(rr)) {
                // write to Json
                // TreePath tree_Component =
                // mainPanel.mlp.tree.tree.getPathForRow(rr);
                treeJSON.put(tree_Row++, rr);

            }
            ;

        }
        if (!treeJSON.isEmpty()) {
            settingsJSONbuf.put("Main_Tree", treeJSON);

        }

        // save setting to setting file
        try {
            SaveStrToFile.saveJsonFine(Settings.getInstance().getSettingsPath(), settingsJSONbuf);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(new JFrame(), "Error writing to the file: "
                            + Settings.getInstance().getSettingsPath() + "\nProbably there is no access.", "Error!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
