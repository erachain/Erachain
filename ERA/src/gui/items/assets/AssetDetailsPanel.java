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

import controller.Controller;
import core.account.Account;
import core.block.GenesisBlock;
import core.item.assets.AssetCls;
import core.transaction.Transaction;
import datachain.DCSet;
import lang.Lang;

public class AssetDetailsPanel extends JPanel {

	private static final long serialVersionUID = 4763074704570450206L;

	private AssetCls asset;

	private JButton favoritesButton;

	public AssetDetailsPanel(AssetCls asset)
	{
		this.asset = asset;
		this.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
		//LAYOUT

		this.setLayout(new GridBagLayout());

		//PADDING
		//	this.setBorder(new EmptyBorder(5, 5, 5, 5));
		//	this.setSize(50,100);
		int gridy = 0;
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(0, 5, 5, 0);
		labelGBC.fill = GridBagConstraints.BOTH;
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 1;
		labelGBC.weighty =1;
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

		//LABEL SEQ-NO
		labelGBC.gridy = ++gridy;
		this.add(new JLabel(Lang.getInstance().translate("Block-SeqNo") + ":"), labelGBC);

		// SEQ-NO
		Transaction record = Transaction.findByDBRef(DCSet.getInstance(), asset.getReference());
		detailGBC.gridy = gridy;
		JTextField txtSeqNo = new JTextField(record.viewHeightSeq(DCSet.getInstance()));
		txtSeqNo.setEditable(false);
		this.add(txtSeqNo, detailGBC);

		//LABEL NAME
		labelGBC.gridy = ++gridy;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
		this.add(nameLabel, labelGBC);

		//NAME
		detailGBC.gridy = gridy;
		JTextField txtName = new JTextField(asset.viewName());
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

		//OWNER
		Account owner = asset.getOwner();
		detailGBC.gridy = gridy;
		JTextField ownerTxt = new JTextField(GenesisBlock.CREATOR.equals(owner)?"GENESIS":owner.getAddress());
		ownerTxt.setEditable(false);
		this.add(ownerTxt, detailGBC);

		String personStr = owner.viewPerson();
		if (personStr.length()>0) {
			//LABEL PERSON
			detailGBC.gridy = ++gridy;
			this.add(new JLabel(personStr), detailGBC);

		}

		//LABEL DIVISIBLE
		labelGBC.gridy = ++gridy;
		this.add(new JLabel(Lang.getInstance().translate("Movable") + ":"), labelGBC);

		//DIVISIBLE
		detailGBC.gridy = gridy;
		JCheckBox chkMovable = new JCheckBox();
		chkMovable.setSelected(asset.isMovable());
		chkMovable.setEnabled(false);
		this.add(chkMovable, detailGBC);


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
		JLabel divisibleLabel = new JLabel(Lang.getInstance().translate("Divis-ible") + ":");
		this.add(divisibleLabel, labelGBC);

		//TYPE
		detailGBC.gridy = gridy;
		 JTextField textType = new JTextField(Lang.getInstance().translate(asset.viewAssetType()));

	//	textType.setSelected(asset.isDivisible()); // SELECT - OPION = asset.getAssetType();
	//	asset.viewAssetType()
	//	int option = asset.getAssetType();

		textType.setEnabled(false);
		this.add(textType, detailGBC);

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
				@Override
				public void actionPerformed(ActionEvent e)
				{
					onOpenPairClick();
				}
			});
			//		this.add(openPairButton, labelGBC);
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
				@Override
				public void actionPerformed(ActionEvent e)
				{
					onFavoriteClick();
				}
			});
			//		this.add(this.favoritesButton, labelGBC);

		}

		//PACK
		this.setVisible(true);
	}

	public void onOpenPairClick() {

		String action = null;
		//	new AssetPairSelect(this.asset.getKey(), action, "");
		new ExchangeFrame(this.asset,null, action, "");

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
