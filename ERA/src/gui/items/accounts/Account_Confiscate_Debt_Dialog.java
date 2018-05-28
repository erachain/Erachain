package gui.items.accounts;

import core.account.Account;
import core.item.assets.AssetCls;
import lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Account_Confiscate_Debt_Dialog extends JDialog {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Account_Confiscate_Debt_Dialog(AssetCls asset, Account account) {

        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);
        Account_Confiscate_Debt_Panel panel = new Account_Confiscate_Debt_Panel(asset, account);
        getContentPane().add(panel, BorderLayout.CENTER);

        //SHOW FRAME
        this.pack();
        //     this.setMaximizable(true);
        this.setTitle(Lang.getInstance().translate("Confiscate Debt"));
        //	this.setClosable(true);
        this.setResizable(false);
        //this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
        //	this.setLocation(20, 20);
        //	this.setIconImages(icons);
        //CLOSE
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

//		 setMinimumSize(new java.awt.Dimension(650, 23));
        setModal(true);
//        setPreferredSize(new java.awt.Dimension(650, 650));


        //PACK

        //    this.setResizable(false);
        this.setLocationRelativeTo(null);


        this.setVisible(true);

    }

}