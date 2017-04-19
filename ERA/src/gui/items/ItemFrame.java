package gui.items;

import gui.models.BalancesTableModel;
import lang.Lang;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import core.item.ItemCls;

@SuppressWarnings("serial")
public class ItemFrame extends JFrame
{
	private ItemCls item;
	
	public ItemFrame(ItemCls item)
	{
		super(Lang.getInstance().translate("ARONICLE.com") + " - " + Lang.getInstance().translate("Check Details"));
		
		this.item = item;
		
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
		tabPane.add(Lang.getInstance().translate("Details"), new ItemDetailsPanel(this.item));
		
		//BALANCES
		BalancesTableModel balancesTableModel = new BalancesTableModel(item.getKey());
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
