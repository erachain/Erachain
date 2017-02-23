package gui.items.voting;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.voting.Poll;
import database.PollMap;
import gui.CoreRowSorter;
import gui.items.ComboBoxModelItemsAll;
import gui.models.PollsTableModel;
import lang.Lang;

@SuppressWarnings("serial")
public class All_Votings_Panel extends JPanel{

	public PollsTableModel pollsTableModel;
	public JComboBox<ItemCls> cbxAssets;
	public JTable pollsTable;
	
	public All_Votings_Panel() 
	{
		//CREATE FRAME
	//	super(Lang.getInstance().translate("ARONICLE.com") + " - " + Lang.getInstance().translate("All Polls"));
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
//		this.setIconImages(icons);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
//		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//SEACH LABEL GBC
		GridBagConstraints searchLabelGBC = new GridBagConstraints();
		searchLabelGBC.insets = new Insets(0, 5, 5, 0);
		searchLabelGBC.fill = GridBagConstraints.HORIZONTAL;   
		searchLabelGBC.anchor = GridBagConstraints.CENTER;
		searchLabelGBC.weightx = 0;	
		searchLabelGBC.gridwidth = 1;
		searchLabelGBC.gridx = 0;
		searchLabelGBC.gridy = 1;
		
		//SEACH GBC
		GridBagConstraints searchGBC = new GridBagConstraints();
		searchGBC.insets = new Insets(0, 5, 5, 0);
		searchGBC.fill = GridBagConstraints.HORIZONTAL;   
		searchGBC.anchor = GridBagConstraints.NORTHWEST;
		searchGBC.weightx = 1;	
		searchGBC.gridwidth = 1;
		searchGBC.gridx = 1;
		searchGBC.gridy = 1;
				
		//TABLE GBC
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.insets = new Insets(0, 5, 5, 0);
		tableGBC.fill = GridBagConstraints.BOTH;  
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;	
		tableGBC.weighty = 1;	
		tableGBC.gridwidth = 2;
		tableGBC.gridx = 0;	
		tableGBC.gridy = 2;	
		
		//ASSET LABEL GBC
		GridBagConstraints assetLabelGBC = new GridBagConstraints();
		assetLabelGBC.insets = new Insets(0, 5, 5, 0);
		assetLabelGBC.fill = GridBagConstraints.HORIZONTAL;   
		assetLabelGBC.anchor = GridBagConstraints.CENTER;
		assetLabelGBC.weightx = 0;	
		assetLabelGBC.gridwidth = 1;
		assetLabelGBC.gridx = 0;
		assetLabelGBC.gridy = 0;
		
		//ASSETS GBC
		GridBagConstraints assetsGBC = new GridBagConstraints();
		assetsGBC.insets = new Insets(0, 5, 5, 0);
		assetsGBC.fill = GridBagConstraints.HORIZONTAL;   
		assetsGBC.anchor = GridBagConstraints.NORTHWEST;
		assetsGBC.weightx = 0;	
		assetsGBC.gridwidth = 1;
		assetsGBC.gridx = 1;
		assetsGBC.gridy = 0;

		//CREATE TABLE
		this.pollsTableModel = new PollsTableModel();
		pollsTable = new JTable(this.pollsTableModel);
				
		//NAMESALES SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		indexes.put(PollsTableModel.COLUMN_NAME, PollMap.DEFAULT_INDEX);
		CoreRowSorter sorter = new CoreRowSorter(this.pollsTableModel, indexes);
		pollsTable.setRowSorter(sorter);

		pollsTable.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				Point p = e.getPoint();
				int row = pollsTable.rowAtPoint(p);
				pollsTable.setRowSelectionInterval(row, row);
		     }
		});
		
		pollsTable.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				Point p = e.getPoint();
				int row = pollsTable.rowAtPoint(p);
				pollsTable.setRowSelectionInterval(row, row);
				
				if(e.getClickCount() == 2)
				{
					row = pollsTable.convertRowIndexToModel(row);
					Poll poll = pollsTableModel.getPoll(row);
					AssetCls item = (AssetCls) cbxAssets.getSelectedItem();
	//				new PollFrame(poll, item);
				}
		     }
		});
		
		//CREATE SEARCH FIELD
		final JTextField txtSearch = new JTextField();

		// UPDATE FILTER ON TEXT CHANGE
		txtSearch.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}

			public void removeUpdate(DocumentEvent e) {
				onChange();
			}

			public void insertUpdate(DocumentEvent e) {
				onChange();
			}

			public void onChange() {

				// GET VALUE
				String search = txtSearch.getText();

			 	// SET FILTER
				pollsTableModel.getSortableList().setFilter(search);
				pollsTableModel.fireTableDataChanged();
			}
		});

		this.add(new JLabel(Lang.getInstance().translate("Search") + ":"), searchLabelGBC);
		this.add(txtSearch, searchGBC);
		this.add(new JScrollPane(pollsTable), tableGBC);

		this.add(new JLabel(Lang.getInstance().translate("Check") + ":"), assetLabelGBC);
		
		cbxAssets = new JComboBox<ItemCls>(new ComboBoxModelItemsAll(ItemCls.ASSET_TYPE));
		this.add(cbxAssets, assetsGBC);
		
		cbxAssets.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {

		    	ItemCls asset = ((ItemCls)cbxAssets.getSelectedItem());

		    	if(asset != null)
		    	{
		    		pollsTableModel.setAsset((AssetCls)asset);
		    	}
		    }
		});
	/*	
		//ON CLOSE
		this.addWindowListener(new WindowAdapter()
		{
            public void windowClosing(WindowEvent e)
            {
            	//REMOVE OBSERVERS/HANLDERS
            	pollsTableModel.removeObservers();
                
                //DISPOSE
                setVisible(false);
                dispose();
            }
        });
	*/	       
        
        //SHOW FRAME
    //    this.pack();
    //    this.setLocationRelativeTo(null);
        this.setVisible(true);
	}	
}
