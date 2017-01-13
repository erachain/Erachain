package gui.items;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import controller.Controller;
import core.item.ItemCls;
import lang.Lang;

public class ItemDetailsPanel extends JPanel {

	private static final long serialVersionUID = 4763074704570450206L;
	
	private ItemCls item;

	private JButton favoritesButton;
	
	public ItemDetailsPanel(ItemCls item)
	{
		this.item = item;
	
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(0, 5, 5, 0);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;
		
		//DETAIL GBC
		GridBagConstraints detailGBC = new GridBagConstraints();
		detailGBC.insets = new Insets(0, 5, 5, 0);
		detailGBC.fill = GridBagConstraints.HORIZONTAL;  
		detailGBC.anchor = GridBagConstraints.NORTHWEST;
		detailGBC.weightx = 1;	
		detailGBC.gridwidth = 2;
		detailGBC.gridx = 1;		
		
		//LABEL KEY
		labelGBC.gridy = 1;
		JLabel keyLabel = new JLabel(Lang.getInstance().translate("Key") + ":");
		this.add(keyLabel, labelGBC);
				
		//KEY
		detailGBC.gridy = 1;
		JTextField txtKey = new JTextField(Long.toString(item.getKey()));
		txtKey.setEditable(false);
		this.add(txtKey, detailGBC);	
		
		//LABEL NAME
		labelGBC.gridy = 2;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
		this.add(nameLabel, labelGBC);
		
		//NAME
		detailGBC.gridy = 2;
		JTextField txtName = new JTextField(item.getName());
		txtName.setEditable(false);
		this.add(txtName, detailGBC);		
		
		//LABEL DESCRIPTION
		labelGBC.gridy = 3;
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		this.add(descriptionLabel, labelGBC);
		           
		//DESCRIPTION
		detailGBC.gridy = 3;
		JTextArea txtAreaDescription = new JTextArea(item.getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(txtName.getBorder());
		txtAreaDescription.setEditable(false);
		this.add(txtAreaDescription, detailGBC);	
		
		//LABEL OWNER
		labelGBC.gridy = 4;
		JLabel ownerLabel = new JLabel(Lang.getInstance().translate("Owner") + ":");
		this.add(ownerLabel, labelGBC);
				
		//OWNER
		detailGBC.gridy = 4;
		JTextField owner = new JTextField(item.getCreator().getPersonAsString());
		owner.setEditable(false);
		this.add(owner, detailGBC);
						
		//IF ASSET CONFIRMED
		if(this.item.getKey() >= 0)
		{
			//ADD ERM PAIR BUTTON
			labelGBC.gridy++;
			labelGBC.gridwidth = 2;
			JButton openPairButton = new JButton(Lang.getInstance().translate("Sign Item"));
			openPairButton.setPreferredSize(new Dimension(200, 25));
			openPairButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					onOpenPairClick();
				}
			});	
			this.add(openPairButton, labelGBC);
		}
		
		//IF ASSET CONFIRMED AND NOT ERM
		if(this.item.getKey() > 2l)
		{
			//FAVORITES
			labelGBC.gridy++;
			labelGBC.gridwidth = 2;
			this.favoritesButton = new JButton();
			
			//CHECK IF FAVORITES
			if(Controller.getInstance().isItemFavorite(item))
			{
				this.favoritesButton.setText(Lang.getInstance().translate("Remove Favorite"));
			}
			else
			{
				this.favoritesButton.setText(Lang.getInstance().translate("Add Favorite"));
			}
				
			this.favoritesButton.setPreferredSize(new Dimension(200, 25));
			this.favoritesButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					onFavoriteClick();
				}
			});	
			this.add(this.favoritesButton, labelGBC);
			
		}
		
        //PACK
		this.setVisible(true);
	}
		
	public void onOpenPairClick() {
		
		//new ItemPairSelect(this.item.getKey());
		
	}
	
	public void onFavoriteClick()
	{
		//CHECK IF FAVORITES
		if(Controller.getInstance().isItemFavorite(item))
		{
			this.favoritesButton.setText(Lang.getInstance().translate("Add Favorite"));
			Controller.getInstance().removeItemFavorite(this.item);
		}
		else
		{
			this.favoritesButton.setText(Lang.getInstance().translate("Remove Favorite"));
			Controller.getInstance().addItemFavorite(this.item);
		}
			
	}
	
}
