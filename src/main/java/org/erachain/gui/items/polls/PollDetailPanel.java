package org.erachain.gui.items.polls;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.Gui;
import org.erachain.gui.library.MTextPane;
import org.erachain.gui.models.ItemPollOptionsTableModel;
import org.erachain.gui.models.PollOptionsTableModel;
import org.erachain.lang.Lang;
import org.erachain.utils.BigDecimalStringComparator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class PollDetailPanel extends JPanel {
    JTable optionsTable;
    private PollCls poll;
    private ItemPollOptionsTableModel pollOptionsTableModel;
    private AssetCls asset;

    @SuppressWarnings("unchecked")
    public PollDetailPanel(PollCls poll, AssetCls asset) {
        this.poll = poll;
        this.asset = asset;

        // LAYOUT
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0};
        gridBagLayout.rowHeights = new int[]{0, 69, 0, 0, 50};
        gridBagLayout.columnWidths = new int[]{119, 71, 0, 0};
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

        Transaction issue_record = poll.getIssueTransaction(DCSet.getInstance());

        // LABEL NAME
        labelGBC.gridy = 2;
        JLabel creatorLabel = new JLabel(Lang.T("Creator") + ":");
        GridBagConstraints gbc_creatorLabel = new GridBagConstraints();
        gbc_creatorLabel.insets = new Insets(0, 0, 5, 5);
        gbc_creatorLabel.gridx = 1;
        gbc_creatorLabel.gridy = 0;
        this.add(creatorLabel, gbc_creatorLabel);

        // NAME
        detailGBC.gridy = 2;
        JTextField creator = new JTextField(poll.getMaker().getPersonAsString());
        creator.setEditable(false);
        GridBagConstraints gbc_creator = new GridBagConstraints();
        gbc_creator.fill = GridBagConstraints.HORIZONTAL;
        gbc_creator.insets = new Insets(0, 0, 5, 5);
        gbc_creator.gridx = 2;
        gbc_creator.gridy = 0;
        this.add(creator, gbc_creator);

        // LABEL OPTIONS
        labelGBC.gridy = 5;

        // OPTIONS
        detailGBC.gridy = 5;
        pollOptionsTableModel = new ItemPollOptionsTableModel(poll, asset);

        optionsTable = Gui.createSortableTable(pollOptionsTableModel, 0);
        optionsTable.getColumnModel().getColumn(0).setPreferredWidth(5);
        optionsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        optionsTable.getColumnModel().getColumn(2).setPreferredWidth(10);
        optionsTable.getColumnModel().getColumn(3).setPreferredWidth(5);
        optionsTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        optionsTable.getColumnModel().getColumn(5).setPreferredWidth(10);

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

        JLabel nameLabel = new JLabel(Lang.T("Name") + ":");
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

        JLabel dateLabel = new JLabel(Lang.T("Creation") + ":");
        GridBagConstraints gbc_dateLabel = new GridBagConstraints();
        gbc_dateLabel.insets = new Insets(0, 0, 5, 5);
        gbc_dateLabel.gridx = 1;
        gbc_dateLabel.gridy = 2;
        this.add(dateLabel, gbc_dateLabel);

        JTextField date = new JTextField(issue_record.viewTimestamp() + " [" + issue_record.viewHeightSeq() + "]");
        date.setEditable(false);
        GridBagConstraints gbc_date = new GridBagConstraints();
        gbc_date.fill = GridBagConstraints.HORIZONTAL;
        gbc_date.insets = new Insets(0, 0, 5, 5);
        gbc_date.gridx = 2;
        gbc_date.gridy = 2;
        this.add(date, gbc_date);

        JLabel descriptionLabel = new JLabel(Lang.T("Description") + ":");
        GridBagConstraints gbc_descriptionLabel = new GridBagConstraints();
        gbc_descriptionLabel.insets = new Insets(0, 0, 5, 5);
        gbc_descriptionLabel.gridx = 1;
        gbc_descriptionLabel.gridy = 3;
        this.add(descriptionLabel, gbc_descriptionLabel);

        MTextPane txtAreaDescription = new MTextPane();
        txtAreaDescription.setText(poll.getDescription());
        txtAreaDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        txtAreaDescription.setPreferredSize(new Dimension(30, 260));

        GridBagConstraints gbc_txtAreaDescription = new GridBagConstraints();
        gbc_txtAreaDescription.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtAreaDescription.insets = new Insets(0, 0, 5, 5);
        gbc_txtAreaDescription.gridx = 2;
        gbc_txtAreaDescription.gridy = 3;
        gbc_txtAreaDescription.gridwidth = 10;
        this.add(txtAreaDescription, gbc_txtAreaDescription);
        txtAreaDescription.setBorder(name.getBorder());


        JLabel optionsLabel = new JLabel(Lang.T("Options") + ":");
        GridBagConstraints gbc_optionsLabel = new GridBagConstraints();
        gbc_optionsLabel.insets = new Insets(0, 0, 5, 5);
        gbc_optionsLabel.gridx = 1;
        gbc_optionsLabel.gridy = 4;
        this.add(optionsLabel, gbc_optionsLabel);

        this.add(new JScrollPane(optionsTable), detailGBC);

        // ADD EXCHANGE BUTTON
        detailGBC.gridy = 6;
        JButton allButton = new JButton(Lang.T("Vote"));
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

        new PollsDialog(this.poll, row, asset);
    }

    public void setAsset(AssetCls asset) {
        this.asset = asset;
        pollOptionsTableModel.setAsset(asset);
    }
}
