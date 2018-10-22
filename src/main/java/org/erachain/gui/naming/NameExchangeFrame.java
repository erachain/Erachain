package org.erachain.gui.naming;

import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class NameExchangeFrame extends JFrame {

    private NameExchangeTabPane nameExchangeTabPane;

    public NameExchangeFrame() {
        //CREATE FRAME
        super(Lang.getInstance().translate("Erachain.org") + " - " + Lang.getInstance().translate("Name Exchange"));

        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        //NAME EXCHANGE TABPANE
        this.nameExchangeTabPane = new NameExchangeTabPane();

        //ON CLOSE
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                //CLOSE name EXCHANGE
                nameExchangeTabPane.close();

                //DISPOSE
                setVisible(false);
                dispose();
            }
        });

        //ADD GENERAL TABPANE TO FRAME
        this.add(this.nameExchangeTabPane);

        //SHOW FRAME
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
