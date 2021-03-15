package org.erachain.gui.items.imprints;

import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.gui.library.MTextPane;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ImprintDetailsPanel extends JPanel {

    private static final long serialVersionUID = 4763074704570450206L;

    private ImprintCls imprint;

    private JButton favoritesButton;

    public ImprintDetailsPanel(ImprintCls imprint) {
        this.imprint = imprint;

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
        JLabel keyLabel = new JLabel(Lang.T("Key") + ":");
        this.add(keyLabel, labelGBC);

        //KEY
        detailGBC.gridy = 1;
        JTextField txtKey = new JTextField(Long.toString(imprint.getKey()));
        txtKey.setEditable(false);
        this.add(txtKey, detailGBC);

        //LABEL NAME
        labelGBC.gridy = 2;
        JLabel nameLabel = new JLabel(Lang.T("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        detailGBC.gridy = 2;
        JTextField txtName = new JTextField(imprint.viewName());
        txtName.setEditable(false);
        this.add(txtName, detailGBC);

        //LABEL DESCRIPTION
        labelGBC.gridy = 3;
        JLabel descriptionLabel = new JLabel(Lang.T("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        detailGBC.gridy = 3;
        MTextPane txtAreaDescription = new MTextPane(imprint.getDescription());
        //txtAreaDescription.setRows(4);
        txtAreaDescription.setBorder(txtName.getBorder());
        //txtAreaDescription.setEditable(false);
        this.add(txtAreaDescription, detailGBC);

        //LABEL OWNER
        labelGBC.gridy = 4;
        JLabel makerLabel = new JLabel(Lang.T("Maker") + ":");
        this.add(makerLabel, labelGBC);

        //OWNER
        detailGBC.gridy = 4;
        JTextField maker = new JTextField(imprint.getMaker().getPersonAsString());
        maker.setEditable(false);
        this.add(maker, detailGBC);

        //IF IMPRINT CONFIRMED
        if (this.imprint.getKey() >= 0) {
            //ADD ERM PAIR BUTTON
            labelGBC.gridy++;
            labelGBC.gridwidth = 2;
            JButton openPairButton = new JButton(Lang.T("Open pair"));
            openPairButton.setPreferredSize(new Dimension(200, 25));
            openPairButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onOpenPairClick();
                }
            });
            this.add(openPairButton, labelGBC);
        }
		
		/*
		//IF IMPRINT CONFIRMED AND NOT ERM
		if(this.imprint.getKey() > 2l)
		{
			//FAVORITES
			labelGBC.gridy++;
			labelGBC.gridwidth = 2;
			this.favoritesButton = new JButton();
			
			//CHECK IF FAVORITES
			if(Controller.getInstance().isItemFavorite(imprint))
			{
				this.favoritesButton.setText(Lang.T("Remove Favorite"));
			}
			else
			{
				this.favoritesButton.setText(Lang.T("Add Favorite"));
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
		*/

        //PACK
        this.setVisible(true);
    }

    public void onOpenPairClick() {

        //new ImprintPairSelect(this.imprint.getKey());

    }
	
	/*
	public void onFavoriteClick()
	{
		//CHECK IF FAVORITES
		if(Controller.getInstance().isItemFavorite(imprint))
		{
			this.favoritesButton.setText(Lang.T("Add Favorite"));
			Controller.getInstance().removeItemFavorite(this.imprint);
		}
		else
		{
			this.favoritesButton.setText(Lang.T("Remove Favorite"));
			Controller.getInstance().addItemFavorite(this.imprint);
		}
			
	}
	*/

}
