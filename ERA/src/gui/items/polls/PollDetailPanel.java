package gui.items.polls;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

import core.item.assets.AssetCls;
import core.item.polls.PollCls;
import core.transaction.CreatePollTransaction;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.Gui;
import gui.models.ItemPollOptionsTableModel;
import gui.models.PollOptionsTableModel;
import lang.Lang;
import utils.BigDecimalStringComparator;
import utils.DateTimeFormat;

@SuppressWarnings("serial")
public class PollDetailPanel extends JPanel {
	private PollCls poll;
	JTable optionsTable;
	private ItemPollOptionsTableModel pollOptionsTableModel;
	private AssetCls asset;

	@SuppressWarnings("unchecked")
	public PollDetailPanel(PollCls poll, AssetCls asset) {
		this.poll = poll;
		this.asset = asset;

		// LAYOUT
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		gridBagLayout.rowHeights = new int[] { 0, 69, 0, 0, 50 };
		gridBagLayout.columnWidths = new int[] { 119, 71, 0, 0 };
		this.setLayout(gridBagLayout);

		// PADDING
		this.setBorder(new EmptyBorder(5, 5, 5, 5));

		// LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(0, 5, 5, 0);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;
		labelGBC.gridx = 0;

		// DETAIL GBC
		GridBagConstraints detailGBC = new GridBagConstraints();
		detailGBC.insets = new Insets(0, 5, 0, 5);
		detailGBC.fill = GridBagConstraints.HORIZONTAL;
		detailGBC.anchor = GridBagConstraints.NORTHWEST;
		detailGBC.weightx = 1;
		detailGBC.gridx = 2;

		// LABEL CREATOR
		labelGBC.gridy = 1;

		// CREATOR
		detailGBC.gridy = 4;

		// LABEL NAME
		labelGBC.gridy = 2;
		JLabel creatorLabel = new JLabel(Lang.getInstance().translate("Creator") + ":");
		GridBagConstraints gbc_creatorLabel = new GridBagConstraints();
		gbc_creatorLabel.insets = new Insets(0, 0, 5, 5);
		gbc_creatorLabel.gridx = 1;
		gbc_creatorLabel.gridy = 0;
		this.add(creatorLabel, gbc_creatorLabel);

		// NAME
		detailGBC.gridy = 2;
		JTextField creator = new JTextField(poll.getOwner().getAddress());
		creator.setEditable(false);
		GridBagConstraints gbc_creator = new GridBagConstraints();
		gbc_creator.fill = GridBagConstraints.HORIZONTAL;
		gbc_creator.insets = new Insets(0, 0, 5, 5);
		gbc_creator.gridx = 2;
		gbc_creator.gridy = 0;
		this.add(creator, gbc_creator);

		// LABEL DATE
		labelGBC.gridy = 3;

		String dateTime = "";

		List<Transaction> transactions = DCSet.getInstance().getTransactionFinalMap()
				.getTransactionsByTypeAndAddress(poll.getOwner().getAddress(), Transaction.CREATE_POLL_TRANSACTION, 0);
		for (Transaction transaction : transactions) {
			CreatePollTransaction createPollTransaction = ((CreatePollTransaction) transaction);
			if (createPollTransaction.getPoll().getName().equals(poll.getName())) {
				dateTime = DateTimeFormat.timestamptoString(createPollTransaction.getTimestamp());
				break;
			}
		}

		// DATE
		detailGBC.gridy = 3;

		// LABEL DESCRIPTION
		labelGBC.gridy = 4;

		// DESCRIPTION
		detailGBC.gridy = 4;

		// LABEL OPTIONS
		labelGBC.gridy = 5;

		// OPTIONS
		detailGBC.gridy = 5;
		pollOptionsTableModel = new ItemPollOptionsTableModel(poll, asset);
		optionsTable = Gui.createSortableTable(pollOptionsTableModel, 0);

		TableRowSorter<PollOptionsTableModel> sorter = (TableRowSorter<PollOptionsTableModel>) optionsTable.getRowSorter();
		sorter.setComparator(PollOptionsTableModel.COLUMN_VOTES, new BigDecimalStringComparator());

		JLabel ImageLabel = new JLabel("");
		GridBagConstraints gbc_ImageLabel = new GridBagConstraints();
		gbc_ImageLabel.anchor = GridBagConstraints.NORTH;
		gbc_ImageLabel.gridheight = 3;
		gbc_ImageLabel.insets = new Insets(0, 0, 5, 5);
		gbc_ImageLabel.gridx = 0;
		gbc_ImageLabel.gridy = 1;
		add(ImageLabel, gbc_ImageLabel);

		ImageIcon image = new ImageIcon(poll.getImage());
		int x = image.getIconWidth();
		int y = image.getIconHeight();
		int x1 = 250;
		double k = ((double) x / (double) x1);
		y = (int) ((double) y / k);

		if (y != 0) {
			Image Im = image.getImage().getScaledInstance(x1, y, 1);

			ImageLabel.setIcon(new ImageIcon(Im));
		}

		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
		GridBagConstraints gbc_nameLabel = new GridBagConstraints();
		gbc_nameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_nameLabel.gridx = 1;
		gbc_nameLabel.gridy = 1;
		this.add(nameLabel, gbc_nameLabel);

		JTextField name = new JTextField(poll.getName());
		name.setEditable(false);
		GridBagConstraints gbc_name = new GridBagConstraints();
		gbc_name.fill = GridBagConstraints.HORIZONTAL;
		gbc_name.insets = new Insets(0, 0, 5, 5);
		gbc_name.gridx = 2;
		gbc_name.gridy = 1;
		this.add(name, gbc_name);

		JLabel dateLabel = new JLabel(Lang.getInstance().translate("Creation date") + ":");
		GridBagConstraints gbc_dateLabel = new GridBagConstraints();
		gbc_dateLabel.insets = new Insets(0, 0, 5, 5);
		gbc_dateLabel.gridx = 1;
		gbc_dateLabel.gridy = 2;
		this.add(dateLabel, gbc_dateLabel);

		JTextField date = new JTextField(dateTime);
		date.setEditable(false);
		GridBagConstraints gbc_date = new GridBagConstraints();
		gbc_date.fill = GridBagConstraints.HORIZONTAL;
		gbc_date.insets = new Insets(0, 0, 5, 5);
		gbc_date.gridx = 2;
		gbc_date.gridy = 2;
		this.add(date, gbc_date);

		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		GridBagConstraints gbc_descriptionLabel = new GridBagConstraints();
		gbc_descriptionLabel.insets = new Insets(0, 0, 5, 5);
		gbc_descriptionLabel.gridx = 1;
		gbc_descriptionLabel.gridy = 3;
		this.add(descriptionLabel, gbc_descriptionLabel);

		JTextArea txtAreaDescription = new JTextArea(poll.getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setEditable(false);
		GridBagConstraints gbc_txtAreaDescription = new GridBagConstraints();
		gbc_txtAreaDescription.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtAreaDescription.insets = new Insets(0, 0, 5, 5);
		gbc_txtAreaDescription.gridx = 2;
		gbc_txtAreaDescription.gridy = 3;
		this.add(txtAreaDescription, gbc_txtAreaDescription);

		txtAreaDescription.setBorder(name.getBorder());
		JLabel optionsLabel = new JLabel(Lang.getInstance().translate("Options") + ":");
		GridBagConstraints gbc_optionsLabel = new GridBagConstraints();
		gbc_optionsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_optionsLabel.gridx = 1;
		gbc_optionsLabel.gridy = 4;
		this.add(optionsLabel, gbc_optionsLabel);

		this.add(new JScrollPane(optionsTable), detailGBC);

		// ADD EXCHANGE BUTTON
		detailGBC.gridy = 6;
		JButton allButton = new JButton(Lang.getInstance().translate("Vote"));
		allButton.setPreferredSize(new Dimension(100, 25));
		allButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onVoteClick();
			}
		});
		// PACK
		this.setVisible(true);
	}

	public void onVoteClick() {
		// GET SELECTED OPTION
		int row = this.optionsTable.getSelectedRow();
		if (row == -1) {
			row = 0;
		}
		row = this.optionsTable.convertRowIndexToModel(row);

		new Polls_Dialog(this.poll, row, asset);
	}

	public void setAsset(AssetCls asset) {
		this.asset = asset;
		pollOptionsTableModel.setAsset(asset);
	}
}
