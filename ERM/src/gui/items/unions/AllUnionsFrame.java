package gui.items.unions;

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
import javax.swing.table.TableColumn;

import lang.Lang;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;

import gui.MainFrame;
import gui.Menu;
import gui.items.notes.TableModelNotes;
import gui.items.unions.UnionFrame;
import javax.swing.JTextField;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.BoxLayout;

public class AllUnionsFrame extends JInternalFrame{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AllUnionsFrame (JFrame parent)
	{
	
		// tool bar
		JToolBar tb2 = new JToolBar(Lang.getInstance().translate("Toolbar unions"));
	
		JButton issueButton = new JButton(Lang.getInstance().translate("Issue Union"));
				tb2.add(issueButton);
				issueButton.addActionListener(new ActionListener()
				{
				    public void actionPerformed(ActionEvent e)
				    {
				    	 Menu.selectOrAdd( new IssueUnionFrame(), MainFrame.desktopPane.getAllFrames());
				    }
	
					
				});	
				
		getContentPane().add(tb2, BorderLayout.NORTH);
		
	    JTabbedPane main_jTabbedPane = new JTabbedPane();
	
		
		
		AllUnionsPanel allUnionsFrame = new AllUnionsPanel();
 
		MyUnionsPanel my_union_panel = new MyUnionsPanel();
	 
        main_jTabbedPane.addTab(Lang.getInstance().translate("My Unions"), null, my_union_panel, "");
        main_jTabbedPane.addTab(Lang.getInstance().translate("Search Union"), null, allUnionsFrame, "");
        
        getContentPane().add(main_jTabbedPane, BorderLayout.CENTER);
        main_jTabbedPane.getAccessibleContext().setAccessibleName("");
        main_jTabbedPane.getAccessibleContext().setAccessibleDescription("");
       //SHOW FRAME
        this.pack();
        this.setMaximizable(true);
		this.setTitle(Lang.getInstance().translate("Unions"));
		this.setClosable(true);
		this.setResizable(true);
		this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
		this.setLocation(20, 20);
	//	this.setIconImages(icons);
		//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        this.setResizable(true);
//        splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
        //my_union_panel.requestFocusInWindow();
        this.setVisible(true);
	
	}

}