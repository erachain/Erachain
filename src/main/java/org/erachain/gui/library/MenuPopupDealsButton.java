package org.erachain.gui.library;

import org.erachain.gui.items.accounts.*;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuPopupDealsButton extends JButton {


    private static final long serialVersionUID = 5237335232850181080L;
    public static JMenuItem webServerItem;
    public static JMenuItem blockExplorerItem;
    public static JMenuItem lockItem;
    final JPopupMenu dealsMenu;
    private MenuPopupDealsButton this_component;


    public MenuPopupDealsButton() {


        super();

        this_component = this;
        this.setText(Lang.getInstance().translate("Deals"));
        //     button1_MainToolBar.setActionCommand("button1_Main_Panel");
        this.setFocusable(false);
        this.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        this.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        this.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {


                Dimension d = dealsMenu.getPreferredSize();
                d.width = Math.max(d.width, 300);
                dealsMenu.setPreferredSize(d);
                dealsMenu.show(this_component, 0, this_component.getHeight());
            }
        });


        dealsMenu = new JPopupMenu("popup menu");

        // DEALS
        // Send
        JMenuItem dealsMenuSendMessage = new JMenuItem(Lang.getInstance().translate("Send"));
        dealsMenuSendMessage.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Send Asset and Message"));
        dealsMenuSendMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                //Library.selectOrAdd(new SendFrame(null, null), MainFrame.getInstance().desktopPane.getAllFrames());
            }
        });
        dealsMenu.add(dealsMenuSendMessage);
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
        dealsMenu.add(dealsMenuVouchRecord);


        dealsMenu.addSeparator();


        // Take on HOLD

        JMenuItem dealsMenu_Take_On_Hold = new JMenuItem(Lang.getInstance().translate("Take on Hold"));
        //      dealsMenuLend.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("to Lend"));
        dealsMenu_Take_On_Hold.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertTab(new AccountTakeHoldPanel(null, null, null, null));

            }
        });
        dealsMenu.add(dealsMenu_Take_On_Hold);


        dealsMenu.addSeparator();

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
        dealsMenu.add(dealsMenuLend);


        // Confiscate_Debt

        JMenuItem dealsMenu_Confiscate_Debt = new JMenuItem(Lang.getInstance().translate("Confiscate Debt"));
        //      dealsMenuLend.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("to Lend"));
        dealsMenu_Confiscate_Debt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertTab(new AccountConfiscateDebtPanel(null, null, null, null));

            }
        });
        dealsMenu.add(dealsMenu_Confiscate_Debt);

        // Repay_Debt

        JMenuItem dealsMenu_Repay_Debt = new JMenuItem(Lang.getInstance().translate("Repay Debt"));
        //      dealsMenuLend.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("to Lend"));
        dealsMenu_Repay_Debt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertTab(new AccountRepayDebtPanel(null, null, null, null));

            }
        });
        dealsMenu.add(dealsMenu_Repay_Debt);
				        
				               
				                   
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


    }


}
			
		
		
		
		
		
			
		

	
	
	

