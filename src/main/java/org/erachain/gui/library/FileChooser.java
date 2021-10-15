package org.erachain.gui.library;

import org.erachain.lang.Lang;
import org.erachain.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileChooser extends JFileChooser {

    protected static int default_Wight = Settings.getInstance().get_File_Chooser_Wight();
    protected static int default_Height = Settings.getInstance().get_File_Chooser_Height();
    private static String default_path = Settings.getInstance().get_File_Chooser_Paht();
    private FileChooser th;

    // настройка диалога файлового на русский язык
    public FileChooser() {
        super(default_path);
        th = this;
        if (default_Wight != 0 || default_Height != 0)
            this.setPreferredSize(new Dimension(default_Wight, default_Height));

        UIManager.put("FileChooser.openButtonText", Lang.T("Open"));
        UIManager.put("FileChooser.cancelButtonText", Lang.T("Cancel"));
        UIManager.put("FileChooser.lookInLabelText", Lang.T("Look in"));
        UIManager.put("FileChooser.fileNameLabelText", Lang.T("File Name"));
        UIManager.put("FileChooser.filesOfTypeLabelText", Lang.T("File Type"));

        UIManager.put("FileChooser.saveButtonText", Lang.T("Save"));
        UIManager.put("FileChooser.saveButtonToolTipText", Lang.T("Save"));
        UIManager.put("FileChooser.openButtonText", Lang.T("Open"));
        UIManager.put("FileChooser.openButtonToolTipText", Lang.T("Open"));
        UIManager.put("FileChooser.cancelButtonText", Lang.T("Cancel"));
        UIManager.put("FileChooser.cancelButtonToolTipText", Lang.T("Cancel"));

        UIManager.put("FileChooser.lookInLabelText", Lang.T("Folder"));
        UIManager.put("FileChooser.saveInLabelText", Lang.T("Folder"));
        UIManager.put("FileChooser.fileNameLabelText", Lang.T("File Name"));
        UIManager.put("FileChooser.folderNameLabelText", Lang.T("Folder Name"));
        UIManager.put("FileChooser.filesOfTypeLabelText", Lang.T("File Type"));

        UIManager.put("FileChooser.upFolderToolTipText", Lang.T("UP Folder"));
        UIManager.put("FileChooser.newFolderToolTipText", Lang.T("New Folder"));
        UIManager.put("FileChooser.listViewButtonToolTipText", Lang.T("List View"));
        UIManager.put("FileChooser.detailsViewButtonToolTipText", Lang.T("Details View"));
        UIManager.put("FileChooser.fileNameHeaderText", Lang.T("Name"));
        UIManager.put("FileChooser.fileSizeHeaderText", Lang.T("Size"));
        UIManager.put("FileChooser.fileTypeHeaderText", Lang.T("Type"));
        UIManager.put("FileChooser.fileDateHeaderText", Lang.T("File Date"));
        UIManager.put("FileChooser.fileAttrHeaderText", Lang.T("File Attr"));


        UIManager.put("FileChooser.detailsViewButtonAccessibleName", Lang.T("All Files"));
        this.updateUI();

        // save path
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                    // была нажата кнопка OK
                    // save path
                    default_path = getCurrentDirectory().getPath();
                    // save size
                    default_Wight = th.getWidth();
                    default_Height = th.getHeight();


                } else if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
                    // была нажата кнопка Cancel
                }
            }
        });

    }


    public static String get_Default_Path() {
        return default_path;
    }

    public static int get_Default_Width() {
        return default_Wight;
    }

    public static int get_Default_Height() {
        return default_Height;
    }


}
