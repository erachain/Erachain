package gui.library;

import lang.Lang;
import settings.Settings;
import utils.TableMenuPopupUtil;
import utils.Zip_Bytes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class M_Attached_Files_Panel extends JPanel {

    private Attache_Files_Model model;
    private MTable table;
    private JScrollPane scrollPane;
    private My_JFileChooser chooser;

    public M_Attached_Files_Panel() {

        setLayout(new java.awt.GridBagLayout());
        model = new Attache_Files_Model();
        table = new MTable(model);
        JPopupMenu menu = new JPopupMenu();
        java.awt.GridBagConstraints gridBagConstraints;


        JMenuItem vsend_Coins_Item = new JMenuItem(Lang.getInstance().translate("Save File"));

        vsend_Coins_Item.addActionListener(new ActionListener() {


            @Override
            public void actionPerformed(ActionEvent e) {

                if (table.getSelectedRow() < 0) return;
                int row = table.convertRowIndexToModel(table.getSelectedRow());
                chooser = new My_JFileChooser();
                String str = (String) model.getValueAt(row, 0);
                chooser.setDialogTitle(Lang.getInstance().translate("Save File") + ": " + str);
                //chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                //chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {

                    String pp = chooser.getSelectedFile().getPath() + File.separatorChar + str;

                    File ff = new File(pp);
                    // if file
                    if (ff.exists() && ff.isFile()) {
                        int aaa = JOptionPane.showConfirmDialog(chooser, Lang.getInstance().translate("File") + " " + str + " " + Lang.getInstance().translate("Exists") + "! " + Lang.getInstance().translate("Overwrite") + "?", Lang.getInstance().translate("Message"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        System.out.print("\n gggg " + aaa);
                        if (aaa != 0) {
                            return;
                        }
                        ff.delete();

                    }


                    try (FileOutputStream fos = new FileOutputStream(pp)) {
                        byte[] buffer = (byte[]) model.getValueAt(row, 2);
                        // if ZIP
                        if ((boolean) model.getValueAt(row, 1)) {
                            byte[] buffer1 = null;
                            try {
                                buffer1 = Zip_Bytes.decompress(buffer);
                            } catch (DataFormatException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                            fos.write(buffer1, 0, buffer1.length);
                        } else {
                            fos.write(buffer, 0, buffer.length);
                        }

                    } catch (IOException ex) {

                        System.out.println(ex.getMessage());
                    }

                }


            }
        });
        
        menu.add(vsend_Coins_Item);
        
        JMenuItem open_Item = new JMenuItem(Lang.getInstance().translate("Open File"));

        open_Item.addActionListener(new ActionListener() {


            @Override
            public void actionPerformed(ActionEvent e) {

                if (table.getSelectedRow() < 0) return;
                int row = table.convertRowIndexToModel(table.getSelectedRow());
                String str = (String) model.getValueAt(row, 0);
               

               

                    String pp = Settings.getInstance().getTemDir()+ File.separator + str;// "era_default_file" ;

                    File ff = new File( pp);
                    // if file
                    if (ff.exists() && ff.isFile()) {
                        ff.delete();

                    }


                    try (FileOutputStream fos = new FileOutputStream(pp)) {
                        byte[] buffer = (byte[]) model.getValueAt(row, 2);
                        // if ZIP
                        if ((boolean) model.getValueAt(row, 1)) {
                            byte[] buffer1 = null;
                            try {
                                buffer1 = Zip_Bytes.decompress(buffer);
                            } catch (DataFormatException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                            fos.write(buffer1, 0, buffer1.length);
                        } else {
                            fos.write(buffer, 0, buffer.length);
                        }
                       
                        

                    } catch (IOException ex) {

                      //  System.out.println(ex.getMessage());
                    }

                                
                        try {
                             
                            Desktop.getDesktop().open(ff);
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                


            }
        });
        
        // if desctop supported
        if(Desktop.getDesktop().isDesktopSupported())    menu.add(open_Item);

        TableMenuPopupUtil.installContextMenu(table, menu);


        scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;

        add(scrollPane, gridBagConstraints);

        // TODO Auto-generated constructor stub
    }

    public void insert_Row(String name, boolean zip, byte[] data) {
        model.addRow(new Object[]{name, zip, data});
        model.fireTableDataChanged();

    }

    private byte[] zip_un(byte[] compressedData) throws Exception {
        //    byte[] compressedData = null;
        Inflater decompressor = new Inflater();
        decompressor.setInput(compressedData);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);
        byte[] buf = null;
        while (!decompressor.finished()) {
            int count = decompressor.inflate(buf);
            bos.write(buf, 0, count);
        }
        //   bos.close();
        return bos.toByteArray();
    }

    public My_JFileChooser getchooser() {
        return chooser;
    }


}

class Attache_Files_Model extends DefaultTableModel {

    public Attache_Files_Model() {
        super(new Object[]{Lang.getInstance().translate("Name"), "Zip?", Lang.getInstance().translate("Data")}, 0);

    }

    public int getColumnCount() {
        return 3;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return new Boolean(null);
    }

    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    public Object getValueAt(int row, int col) {


        if (this.getRowCount() < row || this.getRowCount() == 0 || col < 0 || row < 0) return null;
        return super.getValueAt(row, col);


    }
}

class MyByteArrayDecompress {

    public byte[] decompressByteArray(byte[] bytes) {

        ByteArrayOutputStream baos = null;
        Inflater iflr = new Inflater();
        iflr.setInput(bytes);
        baos = new ByteArrayOutputStream();
        byte[] tmp = new byte[4 * 1024];
        try {
            while (!iflr.finished()) {
                int size = iflr.inflate(tmp);
                baos.write(tmp, 0, size);
            }
        } catch (Exception ex) {

        } finally {
            try {
                if (baos != null) baos.close();
            } catch (Exception ex) {
            }
        }

        return baos.toByteArray();
    }

}
