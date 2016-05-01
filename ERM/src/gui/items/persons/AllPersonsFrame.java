package gui.items.persons;

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
import gui.items.persons.PersonFrame;
import javax.swing.JTextField;
import javax.swing.JSplitPane;
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
				
				
				
			//	tb2.add(new JButton("Вторая"));
	
			//	tb2.add(new JButton("Третья"));
	
				//add(tb1, BorderLayout.NORTH); 
				getContentPane().add(tb2, BorderLayout.NORTH);
	
	
//	getContentPane().add(this,BorderLayout.CENTER);
	
	
	
	
	JPanel pane = new JPanel();		
	GridBagLayout gridBagLayout = new GridBagLayout();
	
	gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0};
	gridBagLayout.columnWeights = new double[]{0, 0.0, 0.0, 0.0};
	getContentPane().add(pane,BorderLayout.CENTER);
	pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
	
	JSplitPane splitPane_1 = new JSplitPane();
	splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
	pane.add(splitPane_1);
	
	AllPersonsPanel allPersonsFrame = new AllPersonsPanel();
	AllPersonsPanel splitPane = allPersonsFrame;
	splitPane_1.setLeftComponent(splitPane);
	allPersonsFrame.setSize(splitPane_1.getLeftComponent().getSize());
	
	PersonsPanel scrollPane = new PersonsPanel();
	scrollPane.setToolTipText("");
	splitPane_1.setRightComponent(scrollPane);
	
	
	
	
	/*
	
	
	JPanel allPersonsFrame = new AllPersonsFrame();
	
	
	
	
	JLabel lblNewJgoodiesLabel = DefaultComponentFactory.getInstance().createLabel("New JGoodies label");
	GridBagConstraints gbc_lblNewJgoodiesLabel = new GridBagConstraints();
	gbc_lblNewJgoodiesLabel.anchor = GridBagConstraints.WEST;
	gbc_lblNewJgoodiesLabel.insets = new Insets(0, 0, 5, 5);
	gbc_lblNewJgoodiesLabel.gridx = 0;
	gbc_lblNewJgoodiesLabel.gridy = 4;
	allPersonsFrame.add(lblNewJgoodiesLabel, gbc_lblNewJgoodiesLabel);
	
	
	
	JScrollPane scrollPane_no_confirm = new JScrollPane(new PersonsPanel());
	GridBagConstraints gbc_scrollPane_no_confirm = new GridBagConstraints();
	gbc_scrollPane_no_confirm.gridwidth = 4;
	gbc_scrollPane_no_confirm.insets = new Insets(0, 0, 0, 5);
	gbc_scrollPane_no_confirm.fill = GridBagConstraints.BOTH;
	gbc_scrollPane_no_confirm.gridx = 0;
	gbc_scrollPane_no_confirm.gridy = 5;
	allPersonsFrame.add(scrollPane_no_confirm, gbc_scrollPane_no_confirm);
	
	*/
		//класс взаимодействия с оконной системой ОС

        Toolkit kit = Toolkit.getDefaultToolkit();

        Dimension screens = kit.getScreenSize();

        int w,h;

        w = screens.width;

        h = screens.height;

     

     
        
        
        //SHOW FRAME
        this.pack();
     //   this.setLocationRelativeTo(null);
 
		this.setMaximizable(true);
		this.setTitle(Lang.getInstance().translate("Persons"));
		this.setClosable(true);
//		this.setResizable(true);
		
	
		this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
		this.setLocation(20, 20);
	//	this.setIconImages(icons);
		
		//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        this.setResizable(true);
        splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
        this.setVisible(true);
	
	}

}