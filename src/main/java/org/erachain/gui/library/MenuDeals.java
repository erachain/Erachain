package org.erachain.gui.library;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.accounts.*;
import org.erachain.gui.items.assets.ExchangeFrame;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;

import javax.swing.*;

import org.erachain.controller.Controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuDeals extends JMenu {

    public MenuDeals() {

        // DEALS

        // Send
        JMenuItem dealsMenuSendMessage = new JMenuItem(Lang.getInstance().translate("Send"));
        dealsMenuSendMessage.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Send Asset and Message"));
        dealsMenuSendMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                MainPanel.getInstance().insertTab(new AccountAssetSendPanel(null, TransactionAmount.ACTION_SEND,
                        null, null, null, null));

            }
        });
        add(dealsMenuSendMessage);
        //vouch
        JMenuItem dealsMenuVouchRecord = new JMenuItem(Lang.getInstance().translate("Vouch"));
        dealsMenuVouchRecord.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Vouching record"));
        dealsMenuVouchRecord.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                //selectOrAdd(new VouchRecordDialog(), MainFrame.desktopPane.getAllFrames());
                new VouchRecordDialog(null, null);
            }
        });
        add(dealsMenuVouchRecord);


        addSeparator();


        // Take on HOLD

        JMenuItem dealsMenu_Take_On_Hold = new JMenuItem(Lang.getInstance().translate("Take on Hold"));
        //      dealsMenuLend.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("to Lend"));
        dealsMenu_Take_On_Hold.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertTab(new AccountAssetHoldPanel(null, null, null, null));
            }
        });
        add(dealsMenu_Take_On_Hold);


        addSeparator();

        // to lend

        JMenuItem dealsMenuLend = new JMenuItem(Lang.getInstance().translate("Lend"));
        //      dealsMenuLend.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("to Lend"));
        dealsMenuLend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                //selectOrAdd(new VouchRecordDialog(), MainFrame.desktopPane.getAllFrames());
                //new AccountLendDialog(null, null);
                MainPanel.getInstance().insertTab(new MailSendPanel(null, null, null, null));

            }
        });
        add(dealsMenuLend);


        // Confiscate_Debt

        JMenuItem dealsMenu_Confiscate_Debt = new JMenuItem(Lang.getInstance().translate("Confiscate Debt"));
        //      dealsMenuLend.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("to Lend"));
        dealsMenu_Confiscate_Debt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertTab(new AccountAssetConfiscateDebtPanel(null, null, null, null));

            }
        });
        add(dealsMenu_Confiscate_Debt);

        // Repay_Debt

        JMenuItem dealsMenu_Repay_Debt = new JMenuItem(Lang.getInstance().translate("Repay Debt"));
        //      dealsMenuLend.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("to Lend"));
        dealsMenu_Repay_Debt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertTab(new AccountAssetRepayDebtPanel(null, null, null, null));

            }
        });
        add(dealsMenu_Repay_Debt);

        JMenuItem dealsMenu_Open_Wallet = new JMenuItem(Lang.getInstance().translate("Open Wallet"));
        //      dealsMenuLend.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("to Lend"));
        dealsMenu_Open_Wallet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                //selectOrAdd(new VouchRecordDialog(), MainFrame.desktopPane.getAllFrames());
                // new AccountRepayDebtDialog(null, null);
                int res = Controller.getInstance().loadWalletFromDir();
                if (res == 0) {
                    JOptionPane.showMessageDialog(
                            new JFrame(), Lang.getInstance().translate("wallet does not exist") + "!",
                            "Error!",
                            JOptionPane.ERROR_MESSAGE);

                } else {
                    Controller.getInstance().forgingStatusChanged(Controller.getInstance().getForgingStatus());
                    MainFrame.getInstance().mainPanel.jTabbedPane1.removeAll();
                }
            }
        });

        add(dealsMenu_Open_Wallet);

		     /*   
		        
		        JMenuItem dealsMenuSignNote = new JMenuItem(Lang.getInstance().translate("Statement"));
		        dealsMenuSignNote.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Statement record"));
		        dealsMenuSignNote.addActionListener(new ActionListener()
		        {
		        	public void actionPerformed(ActionEvent e)
		        	{
		        		// 
		        		//selectOrAdd(new VouchRecordDialog(), MainFrame.desktopPane.getAllFrames());
		        		selectOrAdd(new Sign_Frame(null, null), MainFrame.desktopPane.getAllFrames());
		        	}
		        });
		    //    dealsMenu.add(dealsMenuSignNote);

		        // Imprints menu
		        JMenuItem imprintsMenuList = new JMenuItem(Lang.getInstance().translate("List"));
		        imprintsMenuList.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Imprints List"));
		        imprintsMenuList.addActionListener(new ActionListener()
		        {
		        	public void actionPerformed(ActionEvent e)
		        	{
		        		//selectOrAdd(new ImprintsPanel(), MainFrame.desktopPane.getAllFrames());
		        		selectOrAdd(new MainImprintsFrame(), MainFrame.desktopPane.getAllFrames());
		        	}
		        });
		        imprintsMenu.add(imprintsMenuList);     
		        
		        //// RECORDS ////
		        // меню Persons
		        JMenuItem recordsMenuList = new JMenuItem(Lang.getInstance().translate("List"));
		        recordsMenuList.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("All Records"));
		        recordsMenuList.addActionListener(new ActionListener()
		        {
		        	public void actionPerformed(ActionEvent e)
		        	{
		        		selectOrAdd( new RecordsFrame(parent), MainFrame.desktopPane.getAllFrames());
		        	}
		        });
		        recordsMenu.add(recordsMenuList);
		        
		        ///// STATUSES
		        JMenuItem allStatusesMenu = new JMenuItem(Lang.getInstance().translate("List"));
		        allStatusesMenu.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("All Statuses"));
		   //     allStatusesMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		        allStatusesMenu.addActionListener(new ActionListener()
		        {
		        	public void actionPerformed(ActionEvent e)
		        	{
		             
		        		selectOrAdd( new MainStatusesFrame(), MainFrame.desktopPane.getAllFrames());
		        		//selectOrAdd( new AllStatusesFrame(parent), MainFrame.desktopPane.getAllFrames());
		        		
		        	}
		        });
		        statusesMenu.add(allStatusesMenu);  
		 //       statusesMenu.addSeparator();
		        
		        JMenuItem assignStatusMenu = new JMenuItem(Lang.getInstance().translate("Assign"));
		        assignStatusMenu.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Assign Status"));
		   //     allStatusesMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		        assignStatusMenu.addActionListener(new ActionListener()
		        {
		        	public void actionPerformed(ActionEvent e)
		        	{
		             
		        		//selectOrAdd( new AllStatusesFrame(parent), MainFrame.desktopPane.getAllFrames());
		        		
		        	}
		        });
		    //    statusesMenu.add(assignStatusMenu);  
		    //    statusesMenu.addSeparator();

		        
		        JMenuItem issueStatusesMenu = new JMenuItem(Lang.getInstance().translate("New"));
		        issueStatusesMenu.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("New Status"));
		   //     issueStatusesMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		        issueStatusesMenu.addActionListener(new ActionListener()
		        {
		        	public void actionPerformed(ActionEvent e)
		        	{
		             
		        		JInternalFrame frame = new JInternalFrame(Lang.getInstance().translate("Issue new Status"),true, true, true, true);
		        		//frame.getContentPane().add(new AllStatusesPanel());
		        		frame.getContentPane().add(new IssueStatusDialog());
		        		frame.setName("new status");
		        		frame.pack();
		        		frame.setLocation(50, 60);
		        		frame.setVisible(true);
		        		selectOrAdd( frame, MainFrame.desktopPane.getAllFrames());
		        		
		        	}
		        });
		   //     statusesMenu.add(issueStatusesMenu);  

		        ///// UNIONS
		        JMenuItem allUnionsMenu = new JMenuItem(Lang.getInstance().translate("All Unions"));
		        allUnionsMenu.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("All Unions"));
		   //     searchPerson.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		        allUnionsMenu.addActionListener(new ActionListener()
		        {
		        	public void actionPerformed(ActionEvent e)
		        	{
		             
		        		//selectOrAdd( new AllUnionsFrame(parent), MainFrame.desktopPane.getAllFrames());
		        		selectOrAdd( new MainUnionsFrame(), MainFrame.desktopPane.getAllFrames());
		        		
		        	}
		        });
		        unionsMenu.add(allUnionsMenu);
		        unionsMenu.addSeparator();
		        
		        // issue Person menu
		        JMenuItem issueUnionMenu = new JMenuItem(Lang.getInstance().translate("Establish"));
		        issueUnionMenu.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Establish a new Union"));
		   //     searchPerson.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		        issueUnionMenu.addActionListener(new ActionListener()
		        {
		        	public void actionPerformed(ActionEvent e)
		        	{
		             
		        		//selectOrAdd( new IssueUnionFrame(), MainFrame.desktopPane.getAllFrames());
		        		new IssueUnionDialog();
		        		
		        	}
		        });
		        unionsMenu.add(issueUnionMenu);  

		        // ASSETS
		        // ALL ASSETS
		        JMenuItem allAssetsMenu = new JMenuItem(Lang.getInstance().translate("All assets"));
		        allAssetsMenu.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("All assets"));
		   //     searchPerson.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		        allAssetsMenu.addActionListener(new ActionListener()
		        {
		        	public void actionPerformed(ActionEvent e)
		        	{
		             
		        		//selectOrAdd( new IssueUnionFrame(), MainFrame.desktopPane.getAllFrames());
		        		selectOrAdd( new MainAssetsFrame(), MainFrame.desktopPane.getAllFrames());
		        		
		        	}
		        });
		        assetsMenu.add(allAssetsMenu);
		        
		        
		        assetsMenu.addSeparator();

		        // issue asset menu
		        JMenuItem issueAssetMenu = new JMenuItem(Lang.getInstance().translate("Issue asset"));
		        issueAssetMenu.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Issue asset"));
		        // searchPerson.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		        issueAssetMenu.addActionListener(new ActionListener()
		        {
		        	public void actionPerformed(ActionEvent e)
		        	{
		             
		        		//selectOrAdd( new IssueUnionFrame(), MainFrame.desktopPane.getAllFrames());
		        		new IssueAssetDialog();
		        		
		        	}
		        });
		        assetsMenu.add(issueAssetMenu);
		        
		        */
        
/*
        JMenuItem FindHashFromDir = new JMenuItem(Lang.getInstance().translate("Find Hash from DIR"));
        FindHashFromDir.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Find Hash from DIR"));
        FindHashFromDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                //selectOrAdd(new VouchRecordDialog(), MainFrame.desktopPane.getAllFrames());
                new FindHashFrmDirDialog();
            }
        });
        add(FindHashFromDir);
*/

    }

}
