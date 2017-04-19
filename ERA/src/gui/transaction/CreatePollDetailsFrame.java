package gui.transaction;

import gui.Gui;
import gui.models.PollOptionsTableModel;
import lang.Lang;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import core.crypto.Base58;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.transaction.CreatePollTransaction;
import utils.BigDecimalStringComparator;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class CreatePollDetailsFrame extends Rec_DetailsFrame
{
	@SuppressWarnings("unchecked")
	public CreatePollDetailsFrame(CreatePollTransaction pollCreation)
	{
		super(pollCreation);
				
		//LABEL NAME
		++labelGBC.gridy;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
		this.add(nameLabel, labelGBC);
		
		//NAME
		++detailGBC.gridy;
		JTextField name = new JTextField(pollCreation.getPoll().getName());
		name.setEditable(false);
		MenuPopupUtil.installContextMenu(name);
		this.add(name, detailGBC);		
		
		//LABEL DESCRIPTION
		++labelGBC.gridy;
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		this.add(descriptionLabel, labelGBC);
				
		//DESCRIPTION
		++detailGBC.gridy;
		JTextArea txtAreaDescription = new JTextArea(pollCreation.getPoll().getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(name.getBorder());
		txtAreaDescription.setEditable(false);
		MenuPopupUtil.installContextMenu(txtAreaDescription);
		this.add(txtAreaDescription, detailGBC);		
		
		//LABEL OPTIONS
		++labelGBC.gridy;
		JLabel optionsLabel = new JLabel(Lang.getInstance().translate("Options") + ":");
		this.add(optionsLabel, labelGBC);
		
		//OPTIONS
		++detailGBC.gridy;
		PollOptionsTableModel pollOptionsTableModel = new PollOptionsTableModel(pollCreation.getPoll(),
				Controller.getInstance().getAsset(AssetCls.FEE_KEY));
		JTable table = Gui.createSortableTable(pollOptionsTableModel, 0);
		
		TableRowSorter<PollOptionsTableModel> sorter =  (TableRowSorter<PollOptionsTableModel>) table.getRowSorter();
		sorter.setComparator(PollOptionsTableModel.COLUMN_VOTES, new BigDecimalStringComparator());
		
		this.add(new JScrollPane(table), detailGBC);
		
        //PACK
	//	this.pack();
    //    this.setResizable(false);
    //    this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
