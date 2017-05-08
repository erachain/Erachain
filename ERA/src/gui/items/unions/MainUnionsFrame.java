package gui.items.unions;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import javax.swing.DefaultRowSorter;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import core.item.unions.UnionCls;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.Split_Panel;
import gui.items.statuses.IssueStatusPanel;
import gui.library.MTable;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletItemUnionsTableModel;
import lang.Lang;

public class MainUnionsFrame extends Main_Internal_Frame{

	private static final long serialVersionUID = 1L;
	private TableModelUnions tableModelUnions;

public MainUnionsFrame (){
// not show buttons main Toolbar
		this.setTitle(Lang.getInstance().translate("Unions"));
		 this.jButton2_jToolBar.setVisible(false);
		 this.jButton3_jToolBar.setVisible(false);
// buttun1
		 this.jButton1_jToolBar.setText(Lang.getInstance().translate("Issue Union"));
// status panel
		 //this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with Unions"));
		 
		 this.jButton1_jToolBar.addActionListener(new ActionListener()
			{
			    public void actionPerformed(ActionEvent e)
			    {
			    //	 Menu.selectOrAdd( new IssueUnionFrame(), MainFrame.desktopPane.getAllFrames());
			    	 new IssueUnionDialog();
			    }

				
		});	
		 
		 this.jToolBar.setVisible(false);

// all unions 
		 Search_Union_Tab search_Union_SplitPanel =new Search_Union_Tab();		 

		My_Unions_Tab my_Union_SplitPanel = new My_Unions_Tab();
		
// issiue panel
		
		
		 JPanel issuePanel = new IssueUnionPanel();
		 issuePanel.setName(Lang.getInstance().translate("Issue Union"));	
		
		
							
		this.jTabbedPane.add(my_Union_SplitPanel);
		this.jTabbedPane.add(search_Union_SplitPanel);
		this.jTabbedPane.add(issuePanel);

		this.pack();
//			this.setSize(800,600);
		this.setMaximizable(true);
		this.setClosable(true);
		this.setResizable(true);
//			this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
		this.setLocation(20, 20);
//			this.setIconImages(icons);
			//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		 this.setResizable(true);
	    this.setVisible(true);
		Dimension size = MainFrame.getInstance().desktopPane.getSize();
		this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));

		search_Union_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
		my_Union_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));


	}
}
