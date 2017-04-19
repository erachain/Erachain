package gui.items.notes;

import gui.models.BalancesTableModel;
import lang.Lang;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import core.item.notes.NoteCls;

@SuppressWarnings("serial")
public class NoteFrame extends JFrame
{
	private NoteCls note;
	
	public NoteFrame(NoteCls note)
	{
		super(Lang.getInstance().translate("ARONICLE.com") + " - " + Lang.getInstance().translate("Check Details"));
		
		this.note = note;
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		//this.setLayout(new GridBagLayout());
		
		//TAB PANE
		JTabbedPane tabPane = new JTabbedPane();
		
		//DETAILS
		tabPane.add(Lang.getInstance().translate("Details"), new NoteDetailsPanel(this.note));
		
		//BALANCES
		BalancesTableModel balancesTableModel = new BalancesTableModel(note.getKey());
		final JTable balancesTable = new JTable(balancesTableModel);
		tabPane.add(Lang.getInstance().translate("Holders"), new JScrollPane(balancesTable));
		
		//ADD TAB PANE
		this.add(tabPane);
		
        //PACK
		this.pack();
		//this.setSize(500, this.getHeight());
        //this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
