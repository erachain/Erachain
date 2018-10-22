package org.erachain.gui.records;

import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;

public class RecordsFrame extends JInternalFrame {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public RecordsFrame(JFrame parent) {

        RecordsPanel panel = new RecordsPanel();
        getContentPane().add(panel, BorderLayout.CENTER);

        //SHOW FRAME
        this.pack();
        this.setMaximizable(true);
        this.setTitle(Lang.getInstance().translate("Records List"));
        this.setClosable(true);
        this.setResizable(true);
        this.setSize(new Dimension((int) parent.getSize().getWidth() - 80, (int) parent.getSize().getHeight() - 150));
        this.setLocation(0, 0);
        //	this.setIconImages(icons);
        //CLOSE
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        this.setResizable(true);
//        splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
        this.setVisible(true);

    }

}