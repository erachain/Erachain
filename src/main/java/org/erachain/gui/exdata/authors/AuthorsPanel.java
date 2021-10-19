package org.erachain.gui.exdata.authors;

import org.erachain.gui.library.MTable;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AuthorsPanel extends JPanel {
    public final AuthorsModel authorsAuthorsModel;
    public final MTable jTableAuthors;
    private JScrollPane jScrollPaneAuthors;
    private JButton jButtonAddAuthor;
    private JButton jButtonRemoveAuthor;
    private GridBagConstraints gridBagConstraints;


    public AuthorsPanel() {

        super();
        this.setName(Lang.T("Authors"));
        jButtonAddAuthor = new JButton();
        jScrollPaneAuthors = new JScrollPane();
        jButtonRemoveAuthor = new JButton();
        jButtonAddAuthor.setVisible(false);
        jButtonRemoveAuthor.setVisible(true);

        authorsAuthorsModel = new AuthorsModel(0);
        jTableAuthors = new MTable(authorsAuthorsModel);

        this.jButtonRemoveAuthor.addActionListener(new ActionListener() {
            // delete row
            @Override
            public void actionPerformed(ActionEvent e) {
                int interval = 0;
                if (authorsAuthorsModel.getRowCount() > 0) {
                    int selRow = jTableAuthors.getSelectedRow();
                    if (selRow != -1 && authorsAuthorsModel.getRowCount() >= selRow) {
                        ((DefaultTableModel) authorsAuthorsModel).removeRow(selRow);

                        interval = selRow - 1;
                        if (interval < 0) interval = 0;
                    }
                }

                if (authorsAuthorsModel.getRowCount() < 1) {
                    authorsAuthorsModel.addEmpty();
                    interval = 0;
                }

                jTableAuthors.setRowSelectionInterval(interval, interval);
                authorsAuthorsModel.fireTableDataChanged();
            }
        });

        this.setLayout(new GridBagLayout());

        jScrollPaneAuthors.setOpaque(false);
        //jScrollPaneAuthors.setPreferredSize(new Dimension(0, 0));


        TableColumnModel columnModel = jTableAuthors.getColumnModel();
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


        jTableAuthors.setVisible(true);
        jScrollPaneAuthors.setViewportView(jTableAuthors);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        this.add(jScrollPaneAuthors, gridBagConstraints);

        jButtonAddAuthor.setText(Lang.T("Add"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(jButtonAddAuthor, gridBagConstraints);

        jButtonRemoveAuthor.setText(Lang.T("Remove"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        this.add(jButtonRemoveAuthor, gridBagConstraints);


    }


    // table model class



}

