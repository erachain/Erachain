package org.erachain.gui;

import org.erachain.controller.Controller;
import org.erachain.lang.Lang;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class ClosingDialog extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClosingDialog.class);
    private JDialog waitDialog;
    private AboutFrame about_Frame;

    public ClosingDialog() {
        try {
            Gui.getInstance().hideMainFrame();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        //CREATE WAIT DIALOG
        //	JOptionPane optionPane = new JOptionPane(Lang.getInstance().translate("Saving database. Please wait..."), JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        if (Controller.useGui) about_Frame = AboutFrame.getInstance();
        if (Controller.useGui)
            about_Frame.set_console_Text(Lang.getInstance().translate("Saving database. Please wait..."));
        this.waitDialog = AboutFrame.getInstance();//new JDialog();
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.waitDialog.setIconImages(icons);
        this.waitDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.waitDialog.setTitle(Lang.getInstance().translate("Closing..."));
        //this.waitDialog.setContentPane(about_Frame);
        this.waitDialog.setModal(false);
        this.waitDialog.pack();
        this.waitDialog.setLocationRelativeTo(null);
        this.waitDialog.setAlwaysOnTop(false);
        this.waitDialog.setVisible(true);

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                waitDialog.dispose();
                Controller.getInstance().stopAll(0);
            }
        });

    }
}
