package org.erachain.gui.library;

import org.erachain.lang.Lang;
import org.erachain.utils.FileHash;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

// button open choose panel
// user select file
// calc file hash
// set text in inputObject
public class ASMakeHashMenuItem extends JMenuItem {
    ASMakeHashMenuItem th;

    public ASMakeHashMenuItem(JTextField inputObject){
        super();
        th = this;
   //     this.set_Text_and_Size_From_UIManaget(Lang.getInstance().translate("make Hash"),1.0);
        this.setText(Lang.getInstance().translate("make Hash"));
        this.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                th.setEnabled(false);
                String ss = inputObject.getText();
                inputObject.setText("  " + Lang.getInstance().translate("Waiting..."));
                inputObject.setEnabled(false);
                // read file
                FileChooser chooser = new FileChooser();
                chooser.setDialogTitle(Lang.getInstance().translate("Select File"));
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setMultiSelectionEnabled(true);
                chooser.setMultiSelectionEnabled(false);
                int returnVal = chooser.showOpenDialog(getParent());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    new Thread() {
                        @Override
                        public void run() {
                    File patch = chooser.getSelectedFile();
                    /// HASHING
                    FileHash gf = new FileHash(patch);
                    String hashes = gf.getHash();
                    inputObject.setText(gf.getHash());
                    th.setEnabled(true);
                    inputObject.setEnabled(true);
                            inputObject.grabFocus();
                        }
                    }.start();
                }else{
                    inputObject.setText(ss) ;
                    th.setEnabled(true);
                    inputObject.setEnabled(true);
                    inputObject.grabFocus();
                }

            }

        });




    }

}
