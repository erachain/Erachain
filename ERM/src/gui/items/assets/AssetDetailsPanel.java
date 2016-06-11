package gui.items.assets;

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
import core.account.Account;
import core.item.assets.AssetCls;
import lang.Lang;

public class AssetDetailsPanel extends JPanel {

	private static final long serialVersionUID = 4763074704570450206L;
	
	private AssetCls asset;

	private JButton favoritesButton;
	
	public AssetDetailsPanel(AssetCls asset)
	{
		this.asset = asset;
	
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setSize(50,100);
		int gridy = 0;
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
		labelGBC.gridy = ++gridy;
		JLabel keyLabel = new JLabel(Lang.getInstance().translate("Key") + ":");
		this.add(keyLabel, labelGBC);
				
		//KEY
		detailGBC.gridy = gridy;
		JTextField txtKey = new JTextField(Long.toString(asset.getKey()));
		txtKey.setEditable(false);
		this.add(txtKey, detailGBC);	
		
		//LABEL NAME
		labelGBC.gridy = ++gridy;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
		this.add(nameLabel, labelGBC);
		
		//NAME
		detailGBC.gridy = gridy;
		JTextField txtName = new JTextField(asset.getName());
		txtName.setEditable(false);
		this.add(txtName, detailGBC);		
		
		//LABEL DESCRIPTION
		labelGBC.gridy = ++gridy;
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		this.add(descriptionLabel, labelGBC);
		           
		//DESCRIPTION
		detailGBC.gridy = gridy;
		JTextArea txtAreaDescription = new JTextArea(asset.getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(txtName.getBorder());
		txtAreaDescription.setEditable(false);
		this.add(txtAreaDescription, detailGBC);	
		
		//LABEL OWNER
		labelGBC.gridy = ++gridy;
		JLabel ownerLabel = new JLabel(Lang.getInstance().translate("Owner") + ":");
		this.add(ownerLabel, labelGBC);
		
		Account creator = asset.getCreator();
		//OWNER
		detailGBC.gridy = gridy;
		JTextField owner = new JTextField(creator.getAddress());
		owner.setEditable(false);
		this.add(owner, detailGBC);		
		
		String personStr = creator.viewPerson();
		if (personStr.length()>0) {
			//LABEL PERSON
			detailGBC.gridy = ++gridy;
			this.add(new JLabel(personStr), detailGBC);

		}
		
		//LABEL QUANTITY
		labelGBC.gridy = ++gridy;
		JLabel quantityLabel = new JLabel(Lang.getInstance().translate("Quantity") + ":");
		this.add(quantityLabel, labelGBC);
		           
		//QUANTITY
		detailGBC.gridy = gridy;
		JTextField txtQuantity = new JTextField(asset.getQuantity().toString());
		txtQuantity.setEditable(false);
		this.add(txtQuantity, detailGBC);		
		
		//LABEL DIVISIBLE
		labelGBC.gridy = ++gridy;
		JLabel divisibleLabel = new JLabel(Lang.getInstance().translate("Divisible") + ":");
		this.add(divisibleLabel, labelGBC);
		           
		//DIVISIBLE
		detailGBC.gridy = gridy;
		JCheckBox chkDivisible = new JCheckBox();
		chkDivisible.setSelected(asset.isDivisible());
		chkDivisible.setEnabled(false);
		this.add(chkDivisible, detailGBC);	
				
		//IF ASSET CONFIRMED
		if(this.asset.getKey() >= 0)
		{
			//ADD ERM PAIR BUTTON
			labelGBC.gridy++;
			labelGBC.gridwidth = 2;
			JButton openPairButton = new JButton(Lang.getInstance().translate("Open pair"));
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
		if(this.asset.getKey() >= AssetCls.INITIAL_FAVORITES)
		{
			//FAVORITES
			labelGBC.gridy++;
			labelGBC.gridwidth = 2;
			this.favoritesButton = new JButton();
			
			//CHECK IF FAVORITES
			if(Controller.getInstance().isItemFavorite(asset))
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
		
		new AssetPairSelect(this.asset.getKey());
		
	}
	
	public void onFavoriteClick()
	{
		//CHECK IF FAVORITES
		if(Controller.getInstance().isItemFavorite(asset))
		{
			this.favoritesButton.setText(Lang.getInstance().translate("Add Favorite"));
			Controller.getInstance().removeItemFavorite(this.asset);
		}
		else
		{
			this.favoritesButton.setText(Lang.getInstance().translate("Remove Favorite"));
			Controller.getInstance().addItemFavorite(this.asset);
		}
			
	}
	
}
