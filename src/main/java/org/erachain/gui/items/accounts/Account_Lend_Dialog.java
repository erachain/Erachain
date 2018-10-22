package org.erachain.gui.items.accounts;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.MainFrame;
import org.erachain.lang.Lang;

import javax.swing.*;

import org.erachain.controller.Controller;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Account_Lend_Dialog extends JDialog {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Account_Lend_Dialog(AssetCls asset, Account account) {
        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);
        if (asset == null) asset = Controller.getInstance().getAsset(1);
        Account_Lend_Panel panel = new Account_Lend_Panel(asset, account, null,null);
        getContentPane().add(panel, BorderLayout.CENTER);
        this.setTitle(Lang.getInstance().translate("Lend"));
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screens = kit.getScreenSize();
        int h = screens.height - 50;
        int w = screens.width - 50;
        this.setPreferredSize(new Dimension(w,h));
        //SHOW FRAME
        this.pack();
        //     this.setMaximizable(true);
        
        //	this.setClosable(true);
        this.setResizable(true);
        //this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
        //	this.setLocation(20, 20);
        //	this.setIconImages(icons);
        //CLOSE
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

        //	 setMinimumSize(new java.awt.Dimension(650, 23));
        setModal(true);
        //     setPreferredSize(new java.awt.Dimension(650, 650));


        this.setLocationRelativeTo(null);


        this.setVisible(true);

    }

}