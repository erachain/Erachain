package gui.items.persons;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import lang.Lang;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;

import gui.MainFrame;
import gui.Menu;
import gui.items.notes.TableModelNotes;
import gui.items.persons.PersonFrame;
import javax.swing.JTextField;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.BoxLayout;

public class AllPersonsFrame extends JInternalFrame{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AllPersonsFrame (JFrame parent)
	{
	
		// tool bar
		JToolBar tb2 = new JToolBar(Lang.getInstance().translate("Toolbar persons"));
	
		JButton issueButton = new JButton(Lang.getInstance().translate("Issue Person"));
				tb2.add(issueButton);
				issueButton.addActionListener(new ActionListener()
				{
				    public void actionPerformed(ActionEvent e)
				    {
				    	 Menu.selectOrAdd( new IssuePersonFrame(), MainFrame.desktopPane.getAllFrames());
				    }
	
					
				});	
				
		getContentPane().add(tb2, BorderLayout.NORTH);
		
	    JTabbedPane main_jTabbedPane = new JTabbedPane();
	
		
		
		AllPersonsPanel allPersonsFrame = new AllPersonsPanel();
 
		MyPersonsPanel my_person_panel = new MyPersonsPanel();
	 
        main_jTabbedPane.addTab(Lang.getInstance().translate("My Persons"), null, my_person_panel, "");
        main_jTabbedPane.addTab(Lang.getInstance().translate("Search Person"), null, allPersonsFrame, "");
        
        getContentPane().add(main_jTabbedPane, BorderLayout.CENTER);
        main_jTabbedPane.getAccessibleContext().setAccessibleName("");
        main_jTabbedPane.getAccessibleContext().setAccessibleDescription("");
       //SHOW FRAME
        this.pack();
        this.setMaximizable(true);
		this.setTitle(Lang.getInstance().translate("Persons"));
		this.setClosable(true);
		this.setResizable(true);
		this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
		this.setLocation(20, 20);
	//	this.setIconImages(icons);
		//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        this.setResizable(true);
//        splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
        //my_person_panel.requestFocusInWindow();
        this.setVisible(true);
    /*    
        Window w = SwingUtilities.getWindowAncestor(this);
    	Dimension d;
    	if (w != null)  d = w.getSize();
    	*/
    	Rectangle k = this.getNormalBounds();
        this.setBounds(k);
	
	}

}