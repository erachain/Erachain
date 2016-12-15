package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import gui.items.accounts.Main_Accounts_Frame;
import gui.items.assets.MainAssetsFrame;
import gui.items.documents.Main_Hash_Document_Frame;
import gui.items.imprints.MainImprintsFrame;
import gui.items.mails.Mails_Main_Frame;
import gui.items.notes.MainNotesFrame;
import gui.items.other.Other_Internal_Frame;
import gui.items.persons.MainPersonsFrame;
import gui.items.records.Records_Main_Frame;
import gui.items.statement.MainStatementsFrame;
import gui.items.statuses.MainStatusesFrame;
import gui.items.unions.MainUnionsFrame;
import gui.items.voting.MainVotingsFrame;
import gui.records.RecordsFrame;
import gui.status.StatusPanel;
import lang.Lang;
import settings.Settings;

@SuppressWarnings("serial")
public class MainFrame extends JFrame{
	
private static final Color FFFF = null;
public static  JDesktopPane desktopPane;
private JFrame parent;

	@SuppressWarnings("null")
	public MainFrame()
	{
		//CREATE FRAME
		super(Lang.getInstance().translate("DATACHAINS.world"));
		if(Settings.getInstance().isTestnet()) {
			setTitle(Lang.getInstance().translate("DATACHAINS.world TestNet ") + Settings.getInstance().getGenesisStamp());
		}
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
       
		parent = MainFrame.this;
		
		// tool bar
	//	JToolBar tb1 = new JToolBar(" РџР°РЅРµР»СЊ 1");

			JToolBar	 Toolbar_Main = new JToolBar(Lang.getInstance().translate("Menu"));

			//	tb1.setRollover(true);

			//	tb1.add(new JButton(new ImageIcon("Add24.gif"))); tb1.add(new JButton(new ImageIcon("AlignTop24.gif")));

			//	tb1.add(new JButton(new ImageIcon("About24.gif")));
				
		JButton button1_MainToolBar = new JButton();
		button1_MainToolBar.setText(Lang.getInstance().translate("Accounts"));
			//     button1_MainToolBar.setActionCommand("button1_Main_Panel");
		button1_MainToolBar.setFocusable(false);
		button1_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button1_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		button1_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
			//	gui.Menu.selectOrAdd( new AccountsFrame(parent), MainFrame.desktopPane.getAllFrames()); // old panel
				gui.Menu.selectOrAdd( new Main_Accounts_Frame(), MainFrame.desktopPane.getAllFrames());
		    }
		});
		Toolbar_Main.add(button1_MainToolBar);
		
			
		
			
		JButton button2_MainToolBar = new JButton();
		button2_MainToolBar.setText(Lang.getInstance().translate("Persons"));
		    //    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button2_MainToolBar.setFocusable(false);
		button2_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button2_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
       	button2_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
       		public void actionPerformed(java.awt.event.ActionEvent evt) {
       			gui.Menu.selectOrAdd( new MainPersonsFrame(), MainFrame.desktopPane.getAllFrames());
       		}
       	});
       	Toolbar_Main.add(button2_MainToolBar);
			
		
       	
      	JButton button_Mails_MainToolBar = new JButton();
		button_Mails_MainToolBar.setText(Lang.getInstance().translate("Mails"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button_Mails_MainToolBar.setFocusable(false);
		button_Mails_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button_Mails_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button_Mails_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.Menu.selectOrAdd( new Mails_Main_Frame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button_Mails_MainToolBar);
       	
       	
       	
       	
    	JButton button11_MainToolBar = new JButton();
		button11_MainToolBar.setText(Lang.getInstance().translate("Documents"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button11_MainToolBar.setFocusable(false);
		button11_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button11_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button11_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.Menu.selectOrAdd( new Main_Hash_Document_Frame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button11_MainToolBar);
       	
       	
       	
       	
       	JButton button_Statements_MainToolBar = new JButton();
       	button_Statements_MainToolBar.setText(Lang.getInstance().translate("Statements"));
		    //    button2_MainToolBar.setActionCommand("button1_Main_Panel");
       	button_Statements_MainToolBar.setFocusable(false);
       	button_Statements_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
       	button_Statements_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
       	button_Statements_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
       		public void actionPerformed(java.awt.event.ActionEvent evt) {
       			gui.Menu.selectOrAdd( new MainStatementsFrame(), MainFrame.desktopPane.getAllFrames());
       		}
       	});
       	
       	
       	Toolbar_Main.add(button_Statements_MainToolBar);
       	
       	
       	
       	
       	
       	
       	
       	
       	
       	
       	
       	
       	
       	JButton button3_MainToolBar = new JButton();
		button3_MainToolBar.setText(Lang.getInstance().translate("Assets"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button3_MainToolBar.setFocusable(false);
		button3_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button3_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button3_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.Menu.selectOrAdd( new MainAssetsFrame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button3_MainToolBar);	

		JButton button4_MainToolBar = new JButton();
		button4_MainToolBar.setText(Lang.getInstance().translate("Imprints"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button4_MainToolBar.setFocusable(false);
		button4_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button4_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button4_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.Menu.selectOrAdd( new MainImprintsFrame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button4_MainToolBar);
		        	
		JButton button5_MainToolBar = new JButton();
		button5_MainToolBar.setText(Lang.getInstance().translate("Unions"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button5_MainToolBar.setFocusable(false);
		button5_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button5_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button5_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.Menu.selectOrAdd( new MainUnionsFrame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button5_MainToolBar);
		
		
		JButton button41_MainToolBar = new JButton();
		button41_MainToolBar.setText(Lang.getInstance().translate("Templates"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button41_MainToolBar.setFocusable(false);
		button41_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button41_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button41_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.Menu.selectOrAdd( new MainNotesFrame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button41_MainToolBar);
		
		
		
		
		        	
		JButton button6_MainToolBar = new JButton();
		button6_MainToolBar.setText(Lang.getInstance().translate("Statuses"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button6_MainToolBar.setFocusable(false);
		button6_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button6_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button6_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.Menu.selectOrAdd( new MainStatusesFrame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button6_MainToolBar);
	/*	        	
		JButton button7_MainToolBar = new JButton();
		button7_MainToolBar.setText(Lang.getInstance().translate("Records"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button7_MainToolBar.setFocusable(false);
		button7_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button7_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button7_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.Menu.selectOrAdd( new RecordsFrame(parent), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button7_MainToolBar);
	*/	
		
		JButton button9_MainToolBar = new JButton();
		button9_MainToolBar.setText(Lang.getInstance().translate("Votings"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button9_MainToolBar.setFocusable(false);
		button9_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button9_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button9_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.Menu.selectOrAdd( new MainVotingsFrame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button9_MainToolBar);
		
		
		
	
		
		
		
		
		
		JButton button10_MainToolBar = new JButton();
		button10_MainToolBar.setText(Lang.getInstance().translate("Records"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button10_MainToolBar.setFocusable(false);
		button10_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button10_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button10_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.Menu.selectOrAdd( new Records_Main_Frame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button10_MainToolBar);
		
		
		
		
		JButton button8_MainToolBar = new JButton();
		button8_MainToolBar.setText(Lang.getInstance().translate("DashBoard"));
		//    button2_MainToolBar.setActionCommand("button1_Main_Panel");
		button8_MainToolBar.setFocusable(false);
		button8_MainToolBar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		button8_MainToolBar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);		       
		button8_MainToolBar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				gui.Menu.selectOrAdd( new Other_Internal_Frame(), MainFrame.desktopPane.getAllFrames());
			}
		});
		Toolbar_Main.add(button8_MainToolBar);
		
		
		
		
		
		
		
				//add(tb1, BorderLayout.NORTH); 
				add(Toolbar_Main, BorderLayout.NORTH);

		

		//MENU
        Menu menu = new Menu(this);

        //ADD MENU TO FRAME
        this.setJMenuBar(menu);
        
        
        // СЃРѕР·РґР°РµРј СЂР°Р±РѕС‡РёР№ СЃС‚РѕР» Swing
        desktopPane = new JDesktopPane();
        //desktopPane.setBackground(new Color(255, 255, 255, 255));//Color.LIGHT_GRAY);
        desktopPane.setBackground(MainFrame.getFrames()[0].getBackground());

        /*
        JInternalFrame item = new AccountsFrame(this);
        item.setVisible(true);
		MainFrame.desktopPane.add(item);
		try {
			 item.setSelected(true);
	        } catch (java.beans.PropertyVetoException e1) {}
	        */


      //  desktopPane.setSize(500, 300);
        // РґРѕР±Р°РІР»СЏРµРј РµРіРѕ РІ С†РµРЅС‚СЂ РѕРєРЅР°
        add(desktopPane);
        
        
        //GENERAL TABPANE
        GeneralTabPane generalTabPane = new GeneralTabPane();
        
        //
        JInternalFrame frame1 = new JInternalFrame();
        
        frame1.add(generalTabPane);
        frame1.setVisible(true);
        frame1.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame1.setSize(500, 500);
        frame1.setLocation(500, 80);
        frame1.setVisible(true);
        frame1.setMaximizable(true);
        frame1.setTitle(Lang.getInstance().translate("Old Panels"));
        //frame1.setClosable(true);
        frame1.setResizable(true);
        
        
        //ADD GENERAL TABPANE TO FRAME
  //      desktopPane.add(frame1);
        
        // WALLET STATS
        this.add(new StatusPanel(), BorderLayout.SOUTH);
        
        //CLOSE NICELY
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
            	new ClosingDialog();
            }
        });
        
   
      //РєР»Р°СЃСЃ РІР·Р°РёРјРѕРґРµР№СЃС‚РІРёСЏ СЃ РѕРєРѕРЅРЅРѕР№ СЃРёСЃС‚РµРјРѕР№ РћРЎ

        Toolkit kit = Toolkit.getDefaultToolkit();

        Dimension screens = kit.getScreenSize();

        int w,h;

        w = screens.width;

        setSize((int) (w/1.3),(int) (w/1.3/1.618));

        setLocation(w/12, w/12);
        
        
        //SHOW FRAME
      //  this.pack();
     //   this.setLocationRelativeTo(null);
        this.setVisible(true);
      //  desktopPane.add(new AllPersonsFrame(this));
    //    desktopPane.add(new MainImprintsFrame());
        
	}
}
