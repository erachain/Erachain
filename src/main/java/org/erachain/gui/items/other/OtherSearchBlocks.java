package org.erachain.gui.items.other;

import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.items.records.SearchTransactionsSplitPanel;
import org.erachain.gui.library.FileChooser;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class OtherSearchBlocks extends SplitPanel {

    SearchTransactionsSplitPanel rp = new SearchTransactionsSplitPanel();
    private OtherSeasrchBlocksTableModel tamleModel;
    private JTable Table_Hash;
    private int start;
    private int end;

    public OtherSearchBlocks() {
        super("OtherSearchBlocks");

        this.jButton2_jToolBar_RightPanel.setVisible(false);
        this.jButton1_jToolBar_RightPanel.setVisible(false);
        this.searchFavoriteJCheckBoxLeftPanel.setVisible(false);
        this.searchMyJCheckBoxLeftPanel.setVisible(false);
        this.searchToolBar_LeftPanel.setVisible(true);
        this.searthLabelSearchToolBarLeftPanel.setText(Lang.getInstance().translate("Block") + " (1-20)");
        this.searchTextFieldSearchToolBarLeftPanelDocument.setMinimumSize(new Dimension(500, 20));
        this.button2ToolBarLeftPanel.setVisible(false);
        this.button1ToolBarLeftPanel.setVisible(false);
        tamleModel = new OtherSeasrchBlocksTableModel();
        Table_Hash = new JTable(tamleModel);

        searchTextFieldSearchToolBarLeftPanelDocument.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                // GET VALUE
                find();
            }

        });

        // select row
        Table_Hash.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent arg0) {

                if (Table_Hash == null || Table_Hash.getSelectedRow() < 0) {
                    jScrollPaneJPanelRightPanel.setViewportView(null);
                    return;
                }
                Block item_Table_Selected = tamleModel.getBlock(Table_Hash
                        .convertRowIndexToModel(Table_Hash.getSelectedRow()));
                if (item_Table_Selected == null)
                    return;

                //	  tr = DCSet.getInstance().getTransactionFinalMap().get(itemTableSelected.b, itemTableSelected.c);


                rp.searchToolBar_LeftPanel.setVisible(false);
                rp.toolBarLeftPanel.setVisible(false);
                rp.searchTextFieldSearchToolBarLeftPanelDocument.setText(item_Table_Selected.getHeight() + "");
                rp.listener();
                jScrollPaneJPanelRightPanel.setViewportView(rp);
                //			itemTableSelected = null;

            }

        });

        this.jScrollPanelLeftPanel.setViewportView(Table_Hash);

    }

    private void find() {
        String search = searchTextFieldSearchToolBarLeftPanelDocument.getText();


        try {
            start = Integer.parseInt(search);
            end = start;


        } catch (Exception e1) {
            try {
                String[] strA = search.split("\\-");
                start = Integer.parseInt(strA[0]);
                end = Integer.parseInt(strA[1]);
            } catch (Exception e2) {
                start = 0;
                end = 0;
            }
        }


        Label_search_Info_Panel.setText(Lang.getInstance().translate("Waiting..."));
        jScrollPanelLeftPanel.setViewportView(search_Info_Panel);
        new Thread() {
            @Override
            public void run() {

                tamleModel.searchBlock(start, end);
                if (tamleModel.getRowCount() < 1) {
                    Label_search_Info_Panel.setText(Lang.getInstance().translate("Not Found"));
                    jScrollPanelLeftPanel.setViewportView(search_Info_Panel);
                    jScrollPaneJPanelRightPanel.setViewportView(null);
                    return;
                }
                jScrollPanelLeftPanel.setViewportView(Table_Hash);
            }
        }.start();
    }

    protected void Hashs_from_Files() {

        // открыть диалог для файла
        // JFileChooser chooser = new JFileChooser();
        // руссификация диалога выбора файла
        // new All_Options().setUpdateUI(chooser);
        FileChooser chooser = new FileChooser();
        chooser.setDialogTitle(Lang.getInstance().translate("Select File"));

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        // FileNameExtensionFilter filter = new FileNameExtensionFilter(
        // "Image", "png", "jpg");
        // chooser.setFileFilter(filter);

        int returnVal = chooser.showOpenDialog(getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            // make HASHES from files
            File patch = chooser.getSelectedFile();

            File file = new File(patch.getPath());

            // преобразуем в байты
            long file_len = file.length();
            if (file_len > Integer.MAX_VALUE) {
                // tableModel.addRow(new Object[] { "",
                // Lang.getInstance().translate("length very long") + " - " +
                // file_name });
                // continue;
            }
            byte[] fileInArray = new byte[(int) file.length()];
            FileInputStream f = null;
            try {
                f = new FileInputStream(patch.getPath());
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // tableModel.addRow(new Object[] { "",
                // Lang.getInstance().translate("error streaming") + " - " +
                // file_name });
                // continue;
            }
            try {
                f.read(fileInArray);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // tableModel.addRow(new Object[] { "",
                // Lang.getInstance().translate("error reading") + " - " +
                // file_name });
                // continue;
            }
            try {
                f.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // continue;
            }

            /// HASHING
            String hashe = Base58.encode(Crypto.getInstance().digest(fileInArray));
            // tableModel.addRow(new Object[] { hashes,
            // Lang.getInstance().translate("from file ") + file_name });
            this.searchTextFieldSearchToolBarLeftPanelDocument.setText(hashe);
            find();

        }

    }

}
