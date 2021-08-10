package org.erachain.gui.library;

import org.erachain.controller.Controller;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.accounts.*;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.records.toSignRecordDialog;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuDeals extends JMenu {

    public MenuDeals() {

        // DEALS

        // MAIL
        JMenuItem dealsMenuMail = new JMenuItem(Lang.T("Send mail"));
        dealsMenuMail.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.T("Send mail"),
                        new MailSendPanel(null, null, null));

            }
        });
        add(dealsMenuMail);

        addSeparator();

        // Send
        JMenuItem dealsMenuSendMessage = new JMenuItem(Lang.T("Send"));
        dealsMenuSendMessage.getAccessibleContext().setAccessibleDescription(Lang.T("Send Asset and Message"));
        dealsMenuSendMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                MainPanel.getInstance().insertNewTab(Lang.T("Send"),
                        new AccountAssetSendPanel(null, null,
                                null, null, null, null, false));

            }
        });
        add(dealsMenuSendMessage);

        addSeparator();

        // to lend

        JMenuItem dealsMenuLend = new JMenuItem(Lang.T("Lend"));
        dealsMenuLend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.T("Lend"),
                        new AccountAssetLendPanel(null, null, null, null));

            }
        });
        add(dealsMenuLend);

        // Confiscate_Debt

        JMenuItem dealsMenu_Confiscate_Debt = new JMenuItem(Lang.T("Confiscate Debt"));
        dealsMenu_Confiscate_Debt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.T("Confiscate Debt"),
                        new AccountAssetConfiscateDebtPanel(null, null, null, null));

            }
        });
        add(dealsMenu_Confiscate_Debt);

        // Repay_Debt

        JMenuItem dealsMenu_Repay_Debt = new JMenuItem(Lang.T("Repay Debt"));
        dealsMenu_Repay_Debt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.T("Repay Debt"),
                        new AccountAssetRepayDebtPanel(null, null, null, null));

            }
        });
        add(dealsMenu_Repay_Debt);

        addSeparator();

        // Take on HOLD

        JMenuItem dealsMenu_Take_On_Hold = new JMenuItem(Lang.T("Take on Hold"));
        dealsMenu_Take_On_Hold.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.T("Take on Hold"),
                        new AccountAssetHoldPanel(null, null, null, null, true));
            }
        });
        add(dealsMenu_Take_On_Hold);

        addSeparator();

        // Spend

        JMenuItem dealsMenu_Spend = new JMenuItem(Lang.T("Spend"));
        dealsMenu_Spend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.T("Spend"),
                        new AccountAssetSpendPanel(null,
                                null, null, null, null, false));

            }
        });
        add(dealsMenu_Spend);

        addSeparator();

        //vouch
        JMenuItem dealsMenuVouchRecord = new JMenuItem(Lang.T("Sign / Vouch"));
        dealsMenuVouchRecord.getAccessibleContext().setAccessibleDescription(Lang.T("Vouching record"));
        dealsMenuVouchRecord.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new toSignRecordDialog(null, null);
            }
        });
        add(dealsMenuVouchRecord);


        JMenuItem dealsMenu_Open_Wallet = new JMenuItem(Lang.T("Open Wallet"));
        dealsMenu_Open_Wallet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                int res = Controller.getInstance().loadWalletFromDir();
                if (res == 0) {
                    JOptionPane.showMessageDialog(
                            new JFrame(), Lang.T("wallet does not exist") + "!",
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
		        
		        JMenuItem dealsMenuSignNote = new JMenuItem(Lang.T("Statement"));
		        dealsMenuSignNote.getAccessibleContext().setAccessibleDescription(Lang.T("Statement record"));
		        dealsMenuSignNote.addActionListener(new ActionListener()
		        {
		        	public void actionPerformed(ActionEvent e)
		        	{
		        		// 
		        		//selectOrAdd(new toSignRecordDialog(), MainFrame.desktopPane.getAllFrames());
		        		selectOrAdd(new Sign_Frame(null, null), MainFrame.desktopPane.getAllFrames());
		        	}
		        });
		    //    dealsMenu.add(dealsMenuSignNote);

		        // Imprints menu
		        JMenuItem imprintsMenuList = new JMenuItem(Lang.T("List"));
		        imprintsMenuList.getAccessibleContext().setAccessibleDescription(Lang.T("Imprints List"));
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
		        JMenuItem recordsMenuList = new JMenuItem(Lang.T("List"));
		        recordsMenuList.getAccessibleContext().setAccessibleDescription(Lang.T("All Records"));
		        recordsMenuList.addActionListener(new ActionListener()
		        {
		        	public void actionPerformed(ActionEvent e)
		        	{
		        		selectOrAdd( new RecordsFrame(parent), MainFrame.desktopPane.getAllFrames());
		        	}
		        });
		        recordsMenu.add(recordsMenuList);
		        
		        ///// STATUSES
		        JMenuItem allStatusesMenu = new JMenuItem(Lang.T("List"));
		        allStatusesMenu.getAccessibleContext().setAccessibleDescription(Lang.T("All Statuses"));
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
		        
		        JMenuItem assignStatusMenu = new JMenuItem(Lang.T("Assign"));
		        assignStatusMenu.getAccessibleContext().setAccessibleDescription(Lang.T("Assign Status"));
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

		        
		        JMenuItem issueStatusesMenu = new JMenuItem(Lang.T("New"));
		        issueStatusesMenu.getAccessibleContext().setAccessibleDescription(Lang.T("New Status"));
		   //     issueStatusesMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		        issueStatusesMenu.addActionListener(new ActionListener()
		        {
		        	public void actionPerformed(ActionEvent e)
		        	{
		             
		        		JInternalFrame frame = new JInternalFrame(Lang.T("Issue new Status"),true, true, true, true);
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
		        JMenuItem allUnionsMenu = new JMenuItem(Lang.T("All Unions"));
		        allUnionsMenu.getAccessibleContext().setAccessibleDescription(Lang.T("All Unions"));
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
		        JMenuItem issueUnionMenu = new JMenuItem(Lang.T("Establish"));
		        issueUnionMenu.getAccessibleContext().setAccessibleDescription(Lang.T("Establish a new Union"));
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
		        JMenuItem allAssetsMenu = new JMenuItem(Lang.T("All assets"));
		        allAssetsMenu.getAccessibleContext().setAccessibleDescription(Lang.T("All assets"));
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
		        JMenuItem issueAssetMenu = new JMenuItem(Lang.T("Issue asset"));
		        issueAssetMenu.getAccessibleContext().setAccessibleDescription(Lang.T("Issue asset"));
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
        JMenuItem FindHashFromDir = new JMenuItem(Lang.T("Find Hash from DIR"));
        FindHashFromDir.getAccessibleContext().setAccessibleDescription(Lang.T("Find Hash from DIR"));
        FindHashFromDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                //selectOrAdd(new toSignRecordDialog(), MainFrame.desktopPane.getAllFrames());
                new FindHashFrmDirDialog();
            }
        });
        add(FindHashFromDir);
*/

    }

}
