package org.erachain.gui.items.accounts;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JInternalFrame;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.lang.Lang;

public class AccountConfiscateDebtDialog extends JDialog {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public AccountConfiscateDebtDialog(AssetCls asset, Account account) {

        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);
        if (asset == null) asset = Controller.getInstance().getAsset(1);
        AccountConfiscateDebtPanel panel = new AccountConfiscateDebtPanel(asset, account, null,null);
        getContentPane().add(panel, BorderLayout.CENTER);

        
        
       
       
        //     this.setMaximizable(true);
        this.setTitle(Lang.getInstance().translate(asset.isOutsideType()? "Подтвердить погашение требования" : "Confiscate Debt"));
        //	this.setClosable(true);
        this.setResizable(true);
        //this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
        //	this.setLocation(20, 20);
        //	this.setIconImages(icons);
        //CLOSE
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        
//		 setMinimumSize(new java.awt.Dimension(650, 23));
        setModal(true);
//        setPreferredSize(new java.awt.Dimension(650, 650));


        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screens = kit.getScreenSize();
        int h = screens.height - 50;
        int w = screens.width - 50;
        this.setPreferredSize(new Dimension(w,h));
        //PACK
        this.pack();
        //    this.setResizable(false);
        this.setLocationRelativeTo(null);


        this.setVisible(true);

    }

}