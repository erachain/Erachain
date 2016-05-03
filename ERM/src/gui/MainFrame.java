package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import gui.items.persons.AllPersonsPanel;
import gui.items.persons.AllPersonsFrame;
import gui.items.persons.IssuePersonFrame;

import gui.items.persons.PersonConfirm;
import gui.items.persons.SearchPersons;
import gui.status.StatusPanel;
import lang.Lang;
import settings.Settings;

@SuppressWarnings("serial")
public class MainFrame extends JFrame{
	
private static final Color FFFF = null;
public static  JDesktopPane desktopPane;

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
		
       
		// tool bar
		JToolBar tb1 = new JToolBar(" Панель 1"),

				tb2 = new JToolBar(" Панель 2");

				tb1.setRollover(true);

				tb1.add(new JButton(new ImageIcon("Add24.gif"))); tb1.add(new JButton(new ImageIcon("AlignTop24.gif")));

				tb1.add(new JButton(new ImageIcon("About24.gif")));

				tb2.add(new JButton("Первая")); tb2.add(new JButton("Вторая"));

				tb2.add(new JButton("Третья"));

				//add(tb1, BorderLayout.NORTH); 
				add(tb2, BorderLayout.NORTH);

			

	
		
		
		//MENU
        Menu menu = new Menu(this);

        //ADD MENU TO FRAME
        this.setJMenuBar(menu);
        
        
        // создаем рабочий стол Swing
        desktopPane = new JDesktopPane();
     //   bqColor bq = new bqColor();
        desktopPane.setBackground(new Color(255, 255, 255, 255));//Color.LIGHT_GRAY);
      //  setSelectionBackground(new Color(209, 232, 255, 255))
      //  desktopPane.setSize(500, 300);
        // добавляем его в центр окна
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
    //    frame1.setClosable(true);
        frame1.setResizable(true);
        //ADD GENERAL TABPANE TO FRAME
        desktopPane.add(frame1);
        
       // JInternalFrame Jfacc = new AccountsPanel();
        
        //desktopPane.add(new IssuePersonFrame());
    //    desktopPane.add(new AccountsPanel());
    //    desktopPane.add(new AllPersonsFrame());
       
     // new PersonConfirm(); 
        //STATS
        this.add(new StatusPanel(), BorderLayout.SOUTH);
   //     this.add(new Person1());
        
        //CLOSE NICELY
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
            	new ClosingDialog();
            }
        });
        
   
      //класс взаимодействия с оконной системой ОС

        Toolkit kit = Toolkit.getDefaultToolkit();

        Dimension screens = kit.getScreenSize();

        int w,h;

        w = screens.width;

        h = screens.height;

        setSize((int) (w/1.5),(int) (h/1.5));

        setLocation(w/6, h/6);
        
        
        //SHOW FRAME
      //  this.pack();
     //   this.setLocationRelativeTo(null);
        this.setVisible(true);
      //  desktopPane.add(new AllPersonsFrame(this));
        
	}
}
