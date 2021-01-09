package org.erachain.gui.library;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.*;
import org.erachain.gui.create.LicenseDataJFrame;
import org.erachain.gui.create.LicenseJFrame;
import org.erachain.gui.items.accounts.AccountAssetSendPanel;
import org.erachain.gui.settings.SettingsFrame;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.URLViewer;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MenuFiles extends JMenu {

    protected Logger logger;

    public  JMenuItem webServerItem;
    public  JMenuItem blockExplorerItem;
    public  JMenuItem lockItem;
    public ImageIcon lockedIcon;
    public ImageIcon unlockedIcon;
    private MenuFiles th;

    public MenuFiles() {
        super();

        logger = LoggerFactory.getLogger(getClass());

        th = this;
        addMenuListener(new MenuListener() {

            @Override
            public void menuCanceled(MenuEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void menuDeselected(MenuEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void menuSelected(MenuEvent arg0) {
                // TODO Auto-generated method stub


                if (Controller.getInstance().isWalletUnlocked()) {
                    lockItem.setText(Lang.T("Lock Wallet"));
                    lockItem.setIcon(lockedIcon);
                } else {
                    lockItem.setText(Lang.T("Unlock Wallet"));
                    lockItem.setIcon(unlockedIcon);
                }
                //		Dimension d = fileMenu.getPreferredSize();
                //		d.width = Math.max(d.width, 300);
                //		fileMenu.setPreferredSize(d);
                //		fileMenu.show(this_component, 0, this_component.getHeight());
            }
        });


        //LOAD IMAGES


        BufferedImage lockedImage;
        try {
            lockedImage = ImageIO.read(new File("images/wallet/locked.png"));

            this.lockedIcon = new ImageIcon(lockedImage.getScaledInstance(20, 16, Image.SCALE_SMOOTH));

            BufferedImage unlockedImage = ImageIO.read(new File("images/wallet/unlocked.png"));
            this.unlockedIcon = new ImageIcon(unlockedImage.getScaledInstance(20, 16, Image.SCALE_SMOOTH));
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }


        lockItem = new JMenuItem("lock");
        lockItem.getAccessibleContext().setAccessibleDescription(Lang.T("Lock/Unlock Wallet"));
        lockItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));

        lockItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PasswordPane.switchLockDialog(th);
            }
        });
        add(lockItem);

        //SEPARATOR
        addSeparator();

        //CONSOLE
        JMenuItem consoleItem = new JMenuItem(Lang.T("Debug"));
        consoleItem.getAccessibleContext().setAccessibleDescription(Lang.T("Debug information"));
        consoleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
        consoleItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new DebugFrame();
            }
        });
        add(consoleItem);

        //SETTINGS
        JMenuItem settingsItem = new JMenuItem(Lang.T("Settings"));
        settingsItem.getAccessibleContext().setAccessibleDescription(Lang.T("Settings of program"));
        settingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        settingsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SettingsFrame();
            }
        });
        add(settingsItem);

        // read transaction

        JMenuItem readTransItem = new JMenuItem(Lang.T("Read Transaction"));
        readTransItem.getAccessibleContext().setAccessibleDescription(Lang.T("Read Transaction"));
        readTransItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
        readTransItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

//        		String raw = Base58.encode(transaction.toBytes(false, null));
                FileChooser chooser = new FileChooser();
                chooser.setDialogTitle(Lang.T("Open File"));
                //chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("*.era","*.*");
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
        add(readTransItem);

        // write teransaction

        JMenuItem writeTransItem = new JMenuItem(Lang.T("Write Transaction"));
        writeTransItem.getAccessibleContext().setAccessibleDescription(Lang.T("Read Transaction"));
        writeTransItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
        writeTransItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //  new SettingsFrame();
                // no receive
                //AccountSendDialog dd = new AccountSendDialog(null, null, null, null, false);
                MainPanel.getInstance().insertNewTab(Lang.T("Write Transaction"),
                        new AccountAssetSendPanel(null,
                                null, null, null, null, false));


            }
        });
        if (BlockChain.TEST_MODE) add(writeTransItem);

        //WEB SERVER
        webServerItem = new JMenuItem(Lang.T("Decentralized Web server"));
        webServerItem.getAccessibleContext().setAccessibleDescription("http://127.0.0.1:" + Settings.getInstance().getWebPort());
        webServerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.ALT_MASK));
        webServerItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                try {
                    URLViewer.openWebpage(new URL("http://127.0.0.1:" + Settings.getInstance().getWebPort()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }

            }
        });
        add(webServerItem);

        webServerItem.setVisible(Settings.getInstance().isWebEnabled());

        //WEB SERVER
        blockExplorerItem = new JMenuItem(Lang.T("Built-in BlockExplorer"));
        blockExplorerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.ALT_MASK));
        blockExplorerItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                try {
                    String blockExplorerProtocol = "Http";
                    if (Settings.getInstance().isWebUseSSL()){
                        blockExplorerProtocol = "Https";
                    }
                    String blockExplorerUrl = blockExplorerProtocol + "://127.0.0.1:" + Settings.getInstance().getWebPort() + "/index/blockexplorer.html";
                    URLViewer.openWebpage(new URL(blockExplorerUrl));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }

            }
        });
        add(blockExplorerItem);

        blockExplorerItem.setVisible(Settings.getInstance().isWebEnabled());

        //ABOUT
        JMenuItem aboutItem = new JMenuItem(Lang.T("About"));
        aboutItem.getAccessibleContext().setAccessibleDescription(Lang.T("Information about the application"));
        aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AboutFrame.getInstance().setCursor(new Cursor(Cursor.HAND_CURSOR));
                AboutFrame.getInstance().set_console_Text("");
                AboutFrame.getInstance().setUserClose(true);
                AboutFrame.getInstance().setModal(true);
                AboutFrame.getInstance().setVisible(true);
            }
        });
        add(aboutItem);

        // ERACHAIN LICENSE
        JMenuItem licenseItem = new JMenuItem(Lang.T("License"));
        //    licenseItem.getAccessibleContext().setAccessibleDescription(Lang.T("Information about the application"));
        //    licenseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
        licenseItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LicenseJFrame();
            }
        });
        add(licenseItem);

        // CLONECHAIN LICENSE
        //ABOUT
        JMenuItem dataLicenseItem = new JMenuItem(Lang.T("Data License of Clonechain"));
        //    licenseItem.getAccessibleContext().setAccessibleDescription(Lang.T("Information about the application"));
        //    licenseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
        dataLicenseItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LicenseDataJFrame();
            }
        });
        add(dataLicenseItem);


        //SEPARATOR
        addSeparator();

        //QUIT
        JMenuItem quitItem = new JMenuItem(Lang.T("Quit"));
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        quitItem.getAccessibleContext().setAccessibleDescription(Lang.T("Quit the application"));
        quitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.getInstance().closeFrame();
                MainFrame.getInstance().dispose();
                 new ClosingDialog();
            }
        });

        add(quitItem);
/*        
        fileMenu.addMenuListener(new MenuListener()
        {
			@Override
			public void menuSelected(MenuEvent arg0) {
        		if(Controller.getInstance().isWalletUnlocked()) {
        			lockItem.setText(Lang.T("Lock Wallet"));
        			lockItem.setIcon(lockedIcon);
        		} else {
        			lockItem.setText(Lang.T("Unlock Wallet"));
        			lockItem.setIcon(unlockedIcon);
        		}
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				
			}
        });
        	*/


    }

}
