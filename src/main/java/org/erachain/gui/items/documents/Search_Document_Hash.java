package org.erachain.gui.items.documents;

import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.gui.Split_Panel;
import org.erachain.gui.items.persons.Person_Info_002;
import org.erachain.gui.library.fileChooser;
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

public class Search_Document_Hash extends Split_Panel {


    Model_Hashes_info model_Hashs;
    JTable Table_Hash;

    public Search_Document_Hash() {
        super("Search_Document_Hash");
        this.searchToolBar_LeftPanel.setVisible(true);
        model_Hashs = new Model_Hashes_info();
        Table_Hash = new JTable(model_Hashs);


        Table_Hash.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                // TODO Auto-generated method stub

                PersonCls person = null;
                if (Table_Hash.getSelectedRow() >= 0)
                    person = model_Hashs.getCreatorAdress(Table_Hash.convertRowIndexToModel(Table_Hash.getSelectedRow()));
                if (person != null) {
                    Person_Info_002 info_panel = new Person_Info_002(person, false);
                    //info_panel..key_jLabel.setText(Lang.getInstance().translate("Information about the Signer"));
                    info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width - 50, jScrollPane_jPanel_RightPanel.getSize().height - 50));
                    jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
                }

            }


        });

        this.jButton2_jToolBar_RightPanel.setVisible(false);
        this.jButton1_jToolBar_RightPanel.setVisible(false);
        this.searth_Favorite_JCheckBox_LeftPanel.setVisible(false);
        this.searth_My_JCheckBox_LeftPanel.setVisible(false);
        this.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Hash"));
        this.searchTextField_SearchToolBar_LeftPanel.setMinimumSize(new Dimension(500, 20));
        this.searchTextField_SearchToolBar_LeftPanel.setPreferredSize(new Dimension(500, 20));
        this.button2_ToolBar_LeftPanel.setVisible(false);
        this.button1_ToolBar_LeftPanel.setVisible(false);
        JButton search_Button = new JButton();
        this.searchToolBar_LeftPanel.add(search_Button);

        search_Button.setText(Lang.getInstance().translate("Search hash"));

        search_Button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub

                //Hashs_from_Files();

                model_Hashs.Set_Data(searchTextField_SearchToolBar_LeftPanel.getText());

            }

        });

        searchTextField_SearchToolBar_LeftPanel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                try {
                    model_Hashs.Set_Data(searchTextField_SearchToolBar_LeftPanel.getText().toString());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }


        });

        JButton from_File_Button = new JButton(Lang.getInstance().translate("Get Hash"));
        this.searchToolBar_LeftPanel.add(from_File_Button);
        from_File_Button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub

                Hashs_from_Files();

            }

        });


        this.jScrollPanel_LeftPanel.setViewportView(Table_Hash);


    }


    protected void Hashs_from_Files() {

        // открыть диалог для файла
        //JFileChooser chooser = new JFileChooser();
        // руссификация диалога выбора файла
        //new All_Options().setUpdateUI(chooser);
        fileChooser chooser = new fileChooser();
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
                //		table_Model.addRow(new Object[] { "",
                //				Lang.getInstance().translate("length very long") + " - " + file_name });
                //		continue;
            }
            byte[] fileInArray = new byte[(int) file.length()];
            FileInputStream f = null;
            try {
                f = new FileInputStream(patch.getPath());
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //		table_Model.addRow(new Object[] { "",
                //				Lang.getInstance().translate("error streaming") + " - " + file_name });
                //		continue;
            }
            try {
                f.read(fileInArray);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //		table_Model.addRow(new Object[] { "",
                //				Lang.getInstance().translate("error reading") + " - " + file_name });
                //		continue;
            }
            try {
                f.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //		continue;
            }

            /// HASHING
            String hashe = Base58.encode(Crypto.getInstance().digest(fileInArray));
            //	table_Model.addRow(new Object[] { hashes,
            //			Lang.getInstance().translate("from file ") + file_name });
            this.searchTextField_SearchToolBar_LeftPanel.setText(hashe);

            model_Hashs.Set_Data(hashe);
            //	model_Hashs = new Model_Hashes_info(hashe);
            //		Table_Hash = new JTable(model_Hashs);
            //	this.jScrollPanel_LeftPanel.setViewportView(Table_Hash);


        }


    }


}
	
	


