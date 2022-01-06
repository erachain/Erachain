package org.erachain.gui.library;

import org.erachain.controller.Controller;
import org.erachain.gui.*;
import org.erachain.gui.create.LicenseDataJFrame;
import org.erachain.gui.create.LicenseJFrame;
import org.erachain.gui.settings.SettingsFrame;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.URLViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
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


        lockItem = new JMenuItem();
        lockItem.getAccessibleContext().setAccessibleDescription(Lang.T("Lock/Unlock Wallet"));
        lockItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));

        lockItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PasswordPane.switchLockDialog(th);
            }
        });
        if (false) {
            // removed to Wallet Menu
            add(lockItem);

            //SEPARATOR
            addSeparator();
        }

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
                AboutFrame.getInstance().console_Text.setVisible(false);
                AboutFrame.getInstance().setUserClose(true);
                AboutFrame.getInstance().setModal(true);
                AboutFrame.getInstance().setVisible(true);
            }
        });
        add(aboutItem);

        // ERACHAIN LICENSE
        JMenuItem licenseItem = new JMenuItem(Lang.T("License"));
        licenseItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LicenseJFrame();
            }
        });
        add(licenseItem);

        // CLONECHAIN LICENSE
        //ABOUT
        JMenuItem dataLicenseItem = new JMenuItem(Lang.T("Data License of Clonechain"));
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
    }

}
