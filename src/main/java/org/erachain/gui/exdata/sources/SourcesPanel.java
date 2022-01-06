package org.erachain.gui.exdata.sources;

import org.erachain.gui.exdata.authors.AuthorsModel;
import org.erachain.gui.library.MTable;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SourcesPanel extends JPanel {
    public final SourcesModel sourcesModel;
    public final MTable jTableSources;
    private JScrollPane jScrollPaneSources;
    private JButton jButtonAddSources;
    private JButton jButtonRemoveSources;
    private GridBagConstraints gridBagConstraints;


    public SourcesPanel() {

        super();
        this.setName(Lang.T("Sources"));
        jButtonAddSources = new JButton();
        jScrollPaneSources = new JScrollPane();
        jButtonRemoveSources = new JButton();
        jButtonAddSources.setVisible(true);
        jButtonRemoveSources.setVisible(true);

        sourcesModel = new SourcesModel(0);
        jTableSources = new MTable(sourcesModel);

        this.jButtonRemoveSources.addActionListener(new ActionListener() {
            // delete row
            @Override
            public void actionPerformed(ActionEvent e) {
                int interval = 0;
                if (sourcesModel.getRowCount() > 0) {
                    int selRow = jTableSources.getSelectedRow();
                    if (selRow != -1 && sourcesModel.getRowCount() >= selRow) {
                        ((DefaultTableModel) sourcesModel).removeRow(selRow);

                        interval = selRow - 1;
                        if (interval < 0) interval = 0;
                    }
                }

                if (sourcesModel.getRowCount() < 1) {
                    sourcesModel.addEmpty();
                    interval = 0;
                }

                jTableSources.setRowSelectionInterval(interval, interval);
                sourcesModel.fireTableDataChanged();
            }
        });

        jButtonAddSources.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sourcesModel.addEmpty();
            }
        });


        this.setLayout(new GridBagLayout());

        jScrollPaneSources.setOpaque(false);
        jScrollPaneSources.setPreferredSize(new Dimension(0, 0));


        TableColumnModel columnModel = jTableSources.getColumnModel();
        TableColumn columnNo = columnModel.getColumn(AuthorsModel.KEY_COL);
        columnNo.setMinWidth(50);
        columnNo.setMaxWidth(150);
        columnNo.setPreferredWidth(100);
        columnNo.setWidth(100);
        columnNo.sizeWidthToFit();

        TableColumn columnShare = columnModel.getColumn(AuthorsModel.SHARE_COL);
        columnShare.setMinWidth(100);
        columnShare.setMaxWidth(150);
        columnShare.setPreferredWidth(100);
        columnShare.setWidth(100);
        columnShare.sizeWidthToFit();


        jTableSources.setVisible(true);
        jScrollPaneSources.setViewportView(jTableSources);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        this.add(jScrollPaneSources, gridBagConstraints);

        jButtonAddSources.setText(Lang.T("Add"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(jButtonAddSources, gridBagConstraints);

        jButtonRemoveSources.setText(Lang.T("Remove"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(jButtonRemoveSources, gridBagConstraints);
    }
}

