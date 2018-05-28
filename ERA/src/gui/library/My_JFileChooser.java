package gui.library;

import lang.Lang;
import settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class My_JFileChooser extends JFileChooser {

    protected static int default_Wight = Settings.getInstance().get_File_Chooser_Wight();
    protected static int default_Height = Settings.getInstance().get_File_Chooser_Height();
    private static String default_path = Settings.getInstance().get_File_Chooser_Paht();
    private My_JFileChooser th;

    // настройка диалога файлового на русский язык
    public My_JFileChooser() {
        super(default_path);
        th = this;
        if (default_Wight != 0 || default_Height != 0)
            this.setPreferredSize(new Dimension(default_Wight, default_Height));

        UIManager.put("FileChooser.openButtonText", Lang.getInstance().translate("Open"));
        UIManager.put("FileChooser.cancelButtonText", Lang.getInstance().translate("Cancel"));
        UIManager.put("FileChooser.lookInLabelText", Lang.getInstance().translate("Look in"));
        UIManager.put("FileChooser.fileNameLabelText", Lang.getInstance().translate("File Name"));
        UIManager.put("FileChooser.filesOfTypeLabelText", Lang.getInstance().translate("File Type"));

        UIManager.put("FileChooser.saveButtonText", Lang.getInstance().translate("Save"));
        UIManager.put("FileChooser.saveButtonToolTipText", Lang.getInstance().translate("Save"));
        UIManager.put("FileChooser.openButtonText", Lang.getInstance().translate("Open"));
        UIManager.put("FileChooser.openButtonToolTipText", Lang.getInstance().translate("Open"));
        UIManager.put("FileChooser.cancelButtonText", Lang.getInstance().translate("Cancel"));
        UIManager.put("FileChooser.cancelButtonToolTipText", Lang.getInstance().translate("Cancel"));

        UIManager.put("FileChooser.lookInLabelText", Lang.getInstance().translate("Folder"));
        UIManager.put("FileChooser.saveInLabelText", Lang.getInstance().translate("Folder"));
        UIManager.put("FileChooser.fileNameLabelText", Lang.getInstance().translate("File Name"));
        UIManager.put("FileChooser.folderNameLabelText", Lang.getInstance().translate("Folder Name"));
        UIManager.put("FileChooser.filesOfTypeLabelText", Lang.getInstance().translate("File Type"));

        UIManager.put("FileChooser.upFolderToolTipText", Lang.getInstance().translate("UP Folder"));
        UIManager.put("FileChooser.newFolderToolTipText", Lang.getInstance().translate("New Folder"));
        UIManager.put("FileChooser.listViewButtonToolTipText", Lang.getInstance().translate("List View"));
        UIManager.put("FileChooser.detailsViewButtonToolTipText", Lang.getInstance().translate("Details View"));
        UIManager.put("FileChooser.fileNameHeaderText", Lang.getInstance().translate("Name"));
        UIManager.put("FileChooser.fileSizeHeaderText", Lang.getInstance().translate("Size"));
        UIManager.put("FileChooser.fileTypeHeaderText", Lang.getInstance().translate("Type"));
        UIManager.put("FileChooser.fileDateHeaderText", Lang.getInstance().translate("File Date"));
        UIManager.put("FileChooser.fileAttrHeaderText", Lang.getInstance().translate("File Attr"));


        UIManager.put("FileChooser.detailsViewButtonAccessibleName", Lang.getInstance().translate("All Files"));
        this.updateUI();
	
		
		/*
		      FileChooser.acceptAllFileFilterText=Все файлы
    FileChooser.cancelButtonText=Отмена
    FileChooser.cancelButtonToolTipText=Отмена
    FileChooser.deleteFileButtonText=Удалить
    FileChooser.deleteFileButtonToolTipText=Удалить файл
    FileChooser.detailsViewButtonAccessibleName=Подробно
    FileChooser.detailsViewButtonToolTipText=Подробно
    FileChooser.directoryDescriptionText=Папка
    FileChooser.directoryOpenButtonText=Открыть
    FileChooser.directoryOpenButtonToolTipText=Открыть
    FileChooser.enterFilenameLabelText=Имя
    FileChooser.fileDescriptionText=Описание
    FileChooser.fileNameLabelText=Имя файла
    FileChooser.filesLabelText=Файлы
    FileChooser.filesOfTypeLabelText=Типы файлов
    FileChooser.filterLabelText=Тип(ы) файла
    FileChooser.foldersLabelText=Папка
    FileChooser.helpButtonText=Помощь
    FileChooser.helpButtonToolTipText=Помощь
    FileChooser.homeFolderAccessibleName=Дом
    FileChooser.homeFolderToolTipText=Дом
    FileChooser.listViewButtonAccessibleName=Список
    FileChooser.listViewButtonToolTipText=Список
    FileChooser.lookInLabelText=Католог:
    FileChooser.newFolderAccessibleName=Создать папку
    FileChooser.newFolderButtonText=Создать папку
    FileChooser.newFolderButtonToolTipText=Создать папку
    FileChooser.newFolderDialogText=Создать папку
    FileChooser.newFolderErrorSeparator=Ошибка создания
    FileChooser.newFolderErrorText=Ошибка создания
    FileChooser.newFolderToolTipText=Создать папку
    FileChooser.openButtonText=Открыть
    FileChooser.openButtonToolTipText=Открыть
    FileChooser.openDialogTitleText=Открыть
    FileChooser.other.newFolder=Создать папку
    FileChooser.other.newFolder.subsequent=Создать папку
    FileChooser.win32.newFolder=Создать папку
    FileChooser.win32.newFolder.subsequent=Создать папку
    FileChooser.pathLabelText=Путь
    FileChooser.renameFileButtonText=Переименовать
    FileChooser.renameFileButtonToolTipText=Переименовать
    FileChooser.renameFileDialogText=Переименовать
    FileChooser.renameFileErrorText=Ошибка переименования
    FileChooser.renameFileErrorTitle=Ошибка переименования
    FileChooser.saveButtonText=Сохранить
    FileChooser.saveButtonToolTipText=Сохранить
    FileChooser.saveDialogTitleText=Сохранить
    FileChooser.saveInLabelText=Католог:
    FileChooser.updateButtonText=Обновить
    FileChooser.updateButtonToolTipText=Обновить
    FileChooser.upFolderAccessibleName=Вверх
    FileChooser.upFolderToolTipText=Вверх
		 */

//   My_JFileChooser.set_Default_Path(chooser.getCurrentDirectory().getPath());
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
