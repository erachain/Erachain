package gui.items.statuses;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JToolBar;

import lang.Lang;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;

import gui.MainFrame;
import gui.Menu;
import gui.items.statuses.StatusFrame;
import javax.swing.JTextField;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.BoxLayout;

public class AllStatusesFrame extends JInternalFrame{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AllStatusesFrame (JFrame parent)
	{
	this.setName("all statuses");
	
		/*
		// tool bar
		JToolBar tb2 = new JToolBar(Lang.getInstance().translate("Toolbar statuses"));
	
		JButton issueButton = new JButton(Lang.getInstance().translate("Issue Status"));
				tb2.add(issueButton);
				issueButton.addActionListener(new ActionListener()
				{
				    public void actionPerformed(ActionEvent e)
				    {
				    	 Menu.selectOrAdd( new IssueStatusPanel(), MainFrame.desktopPane.getAllFrames());
				    }
	
					
				});	
				
		getContentPane().add(tb2, BorderLayout.NORTH);
		*/
	
	
		AllStatusesPanel allStatusesFrame = new AllStatusesPanel();
        getContentPane().add(allStatusesFrame, BorderLayout.CENTER);
		
	    /*
		JTabbedPane main_jTabbedPane = new JTabbedPane();
	
		 
        main_jTabbedPane.addTab(Lang.getInstance().translate("Search Status"), null, allStatusesFrame, "");
        
        getContentPane().add(main_jTabbedPane, BorderLayout.CENTER);
        main_jTabbedPane.getAccessibleContext().setAccessibleName("");
        main_jTabbedPane.getAccessibleContext().setAccessibleDescription("");
        */
		
		//SHOW FRAME
        this.pack();
        this.setMaximizable(true);
		this.setTitle(Lang.getInstance().translate("Statuses"));
		this.setClosable(true);
		this.setResizable(true);
		this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
		this.setLocation(20, 20);
	//	this.setIconImages(icons);
		//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        this.setResizable(true);
//        splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
        this.setVisible(true);
	
	}

}