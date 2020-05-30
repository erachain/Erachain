package org.erachain.gui;

import org.erachain.controller.Controller;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class DebugFrame extends JDialog {

    private DebugTabPane debugTabPane;

    public DebugFrame() {
        //CREATE FRAME
        setTitle(Controller.getInstance().getApplicationName(false) + " - " + Lang.getInstance().translate("Debug"));
        setModal(true);

        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        //DEBUG TABPANE
        this.debugTabPane = new DebugTabPane();

        //ON CLOSE
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                //CLOSE DEBUG
                debugTabPane.close();

                //DISPOSE
                setVisible(false);
                dispose();
            }
        });

        //ADD GENERAL TABPANE TO FRAME
        this.add(this.debugTabPane);

        //PACK
        this.pack();
        this.setSize(800, this.getHeight());
        setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }
}
