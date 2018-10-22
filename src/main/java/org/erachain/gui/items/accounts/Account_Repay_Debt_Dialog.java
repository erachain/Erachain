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

//import org.erachain.gui.*;
//import org.erachain.gui.*;
//import org.erachain.gui.*;

public class Account_Repay_Debt_Dialog extends JDialog {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Account_Repay_Debt_Dialog(AssetCls asset, Account account) {


        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);
        if (asset == null) asset = Controller.getInstance().getAsset(1);
        Account_Repay_Debt_Panel panel = new Account_Repay_Debt_Panel(asset, account, null,null);
        getContentPane().add(panel, BorderLayout.CENTER);

        
        //     this.setMaximizable(true);
        this.setTitle(Lang.getInstance().translate("Repay Debt"));
        //	this.setClosable(true);
        this.setResizable(true);
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screens = kit.getScreenSize();
        int h = screens.height - 50;
        int w = screens.width - 50;
        this.setPreferredSize(new Dimension(w,h));
        //this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
        //	this.setLocation(20, 20);
        //	this.setIconImages(icons);
        //CLOSE
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
      //SHOW FRAME
        this.pack();
        //	 setMinimumSize(new java.awt.Dimension(650, 23));
        setModal(true);
        //     setPreferredSize(new java.awt.Dimension(650, 650));


        //PACK

        //    this.setResizable(false);
        this.setLocationRelativeTo(null);

        this.setVisible(true);

    }

}