package org.erachain.gui.library;

import com.github.rjeschke.txtmark.Processor;
import net.sf.tinylaf.Theme;
import org.erachain.controller.Controller;
import org.erachain.core.Jsonable;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.PlaySound;
import org.erachain.utils.SysTray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;

// почемуто иногда она не может найти эту библиотеку при запуске JAR - надо закоментить ее и опять вставить здесь
// по Alt-Enter на Класса с вызовом Theme. ниже в коде

public class Library {

    protected static Logger logger = LoggerFactory.getLogger(Library.class);

    // PLAY SOUND
    public static void notifySysTrayRecord(Transaction transaction) {

        if (transaction.getCreator() == null)
            return;

        if (transaction.noDCSet())
            transaction.setDC(DCSet.getInstance(), false);

        switch ( transaction.getType()) {
            case Transaction.SEND_ASSET_TRANSACTION:
                RSend r_Send = (RSend) transaction;

                // AS RECIPIENT
                Account account = Controller.getInstance().getWalletAccountByAddress(r_Send.getRecipient().getAddress());
                if (account != null) {
                    if (r_Send.hasAmount()) {
                        if (Settings.getInstance().isSoundReceivePaymentEnabled())
                            PlaySound.getInstance().playSound("receivepayment.wav");

                        String amount = (r_Send.hasPacket() ? "package"
                                : r_Send.getAmount().toPlainString() + " [" + r_Send.getAsset() + "]") + "\n";
                        SysTray.getInstance().sendMessage("Payment received",
                                "From: " + r_Send.getCreator().getPersonAsString() + "\nTo: " + r_Send.getRecipient().getPersonAsString() + "\n"
                                        + amount
                                        + (r_Send.getTitle() != null ? "\n Title" + ":" + r_Send.getTitle() : "")
                                ,
                                MessageType.INFO);

                    } else {
                        if (Settings.getInstance().isSoundReceiveMessageEnabled())
                            PlaySound.getInstance().playSound("receivemessage.wav");

                        SysTray.getInstance().sendMessage("Message received",
                                "From: " + r_Send.getCreator().getPersonAsString() + "\nTo: " + r_Send.getRecipient().getPersonAsString() + "\n"
                                        + (r_Send.getTitle() != null ? "\n Title" + ":" + r_Send.getTitle() : "")
                                ,
                                MessageType.INFO);

                    }

                    return;
                }

                account = Controller.getInstance().getWalletAccountByAddress(r_Send.getCreator().getAddress());
                if (account != null) {
                    if (r_Send.hasAmount()) {

                        if (Settings.getInstance().isSoundNewTransactionEnabled())
                            PlaySound.getInstance().playSound("newtransaction.wav");

                        String amount = (r_Send.hasPacket() ? "package"
                                : r_Send.getAmount().toPlainString() + " [" + r_Send.getAsset() + "]") + "\n";
                        SysTray.getInstance().sendMessage("Payment send",
                                "From: " + transaction.getCreator().getPersonAsString() + "\nTo: " + r_Send.getRecipient().getPersonAsString() + "\n"
                                        + amount
                                        + (r_Send.getTitle() != null ? "\n Title" + ":" + r_Send.getTitle() : "")
                                ,
                                MessageType.INFO);

                    } else {

                        if (Settings.getInstance().isSoundNewTransactionEnabled())
                            PlaySound.getInstance().playSound("newtransaction.wav");

                        SysTray.getInstance().sendMessage("Message send",
                                "From: " + transaction.getCreator().getPersonAsString() + "\nTo: " + r_Send.getRecipient().getPersonAsString() + "\n"
                                        + (r_Send.getTitle() != null ? "\n Title" + ":" + r_Send.getTitle() : "")
                                ,
                                MessageType.INFO);

                    }
                    return;
                }

            default:
                account = Controller.getInstance().getWalletAccountByAddress(transaction.getCreator().getAddress());
                if (account != null) {
                    if (Settings.getInstance().isSoundNewTransactionEnabled()) {
                        PlaySound.getInstance().playSound("newtransaction.wav");
                    }

                    SysTray.getInstance().sendMessage("Transaction send",
                            "From: " + transaction.getCreator().getPersonAsString() + "\n"
                                    + transaction.toString()
                            ,
                            MessageType.INFO);
                }
        }
    }

    public static void setGuiLookAndFeel() {

        // theme
        String name_Theme = Settings.getInstance().get_LookAndFell();

        if (name_Theme.equals("System")) {

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException e1) {
                logger.error(e1.getMessage(), e1);
            }
        }

        if (name_Theme.equals("Metal")) {

            try {
                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //UIManager.getLookAndFeel();
            }
        }

        if (name_Theme.equals("Other")) {

            try {
                //UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
                UIManager.setLookAndFeel("net.sf.tinylaf.TinyLookAndFeel");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        System.setProperty("sun.awt.noerasebackground", "true");
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        int size_font = new Integer(Settings.getInstance().getFontSize());
        String name_font = Settings.getInstance().get_Font_Name();

        Font font = new Font(name_font, Font.TRUETYPE_FONT, size_font);
        UIManager.put("Button.font", font);
        UIManager.put("Table.font", font);
        UIManager.put("Label.font", font);
        UIManager.put("ComboBox.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("TableHeader.font", font);
        UIManager.put("TabbedPane.font", font);
        UIManager.put("RadioButton.font", font);
        UIManager.put("ComboBox.font", font);
        UIManager.put("CheckBox.font", font);
        UIManager.put("FormattedTextField.font", font);

        UIManager.put("Menu.font", font);
        UIManager.put("MenuItem.font", font);
        UIManager.put("Frame.titleFont", font);
        UIManager.put("InternalFrame.font", font);
        UIManager.put("InternalFrame.titleFont", font);

        UIManager.put("TextPane.font", font);
        // UIManager.put( "ScrollBar.minimumThumbSize", new Dimension(20,30) );
        // UIManager.put("ScrollBar.minimumThumbSize", new Dimension(25,25));
        // UIManager.put("Table.height", size_font*5);
        UIManager.put("TextArea.font", font);

        UIManager.put("InternalFrame.paletteTitleFont", font);
        UIManager.put("InternalFrame.normalTitleFont", font);

        UIManager.put("FileChooser.font", font);

        UIManager.put("CheckBoxMenuItem.acceleratorFont", font);
        UIManager.put("CheckBoxMenuItem.font", font);
        UIManager.put("ColorChooser.font", font);

        UIManager.put("EditorPane.font", font);
        UIManager.put("FormattedTextField.font", font);
        UIManager.put("IconButton.font", font);
        UIManager.put("InternalFrame.optionDialogTitleFont", font);
        UIManager.put("InternalFrame.paletteTitleFont", font);
        UIManager.put("InternalFrame.titleFont", font);
        UIManager.put("Label.font", font);
        UIManager.put("List.font", font);
        UIManager.put("Menu.acceleratorFont", font);
        UIManager.put("Menu.font", font);
        UIManager.put("MenuBar.font", font);
        UIManager.put("MenuItem.acceleratorFont", font);
        UIManager.put("MenuItem.font", font);
        UIManager.put("OptionPane.buttonFont", font);
        UIManager.put("OptionPane.font", font);
        UIManager.put("OptionPane.messageFont", font);
        UIManager.put("Panel.font", font);
        UIManager.put("PasswordField.font", font);
        UIManager.put("PopupMenu.font", font);
        UIManager.put("ProgressBar.font", font);
        UIManager.put("RadioButton.font", font);
        UIManager.put("RadioButtonMenuItem.acceleratorFont", font);
        UIManager.put("RadioButtonMenuItem.font", font);
        UIManager.put("ScrollPane.fon", font);
        UIManager.put("Slider.font", font);
        UIManager.put("Spinner.font", font);
        UIManager.put("TabbedPane.font", font);
        UIManager.put("TabbedPane.smallFont", font);
        UIManager.put("Table.font", font);
        UIManager.put("TableHeader.font", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("TextPane.font", font);
        UIManager.put("TitledBorder.font", font);
        UIManager.put("ToggleButton.font", font);
        UIManager.put("ToolBar.font", font);
        UIManager.put("ToolTip.font", font);
        UIManager.put("Tree.font", font);

        UIManager.put("TitledBorder.font", font);
        UIManager.put("Panel.font", font);

        // text to button optionPane
        UIManager.put("OptionPane.yesButtonText", Lang.T("Confirm"));
        UIManager.put("OptionPane.noButtonText", Lang.T("Cancel"));
        UIManager.put("OptionPane.cancelButtonText", Lang.T("Cancel"));
        UIManager.put("OptionPane.okButtonText", Lang.T("OK"));
        UIManager.put("OptionPane.titleFont", font);

        UIManager.put("SplitPane.oneTouchButtonSize", size_font * 2);
        UIManager.put("SplitPane.supportsOneTouchButtons", true);
        UIManager.put("SplitPane.dividerSize", size_font);
        UIManager.put("SplitPaneDivider.oneTouchButtonSize", size_font * 2);
        UIManager.put("SplitPane.centerOneTouchButtons", true);
        UIManager.put("ArrowButton.size", size_font*2);

        if (size_font > 16)
            UIManager.put("ScrollBar.width", size_font);

        int scrolH = (int) (size_font * 1.2);
        if (scrolH < 17)
            scrolH = 17;

        UIManager.put("InternalFrame.titlePaneHeight", scrolH);
        UIManager.put("InternalFrame.titleButtonHeight", scrolH);
        UIManager.put("InternalFrame.titleButtonWidth", scrolH);

        Theme.frameTitleFont.setFont(font);
        Theme.scrollSize.setValue(scrolH);

        Theme.internalPaletteTitleFont.setFont(font);
        Theme.toolTipFont.setFont(font);

        @SuppressWarnings("rawtypes")
        java.util.Enumeration keys = UIManager.getDefaults().keys();

        while (keys.hasMoreElements()) {

            String key = keys.nextElement().toString();
            if (key.contains("OptionPane")) {
            }
        }

    }

    public static String to_HTML(String str) {
        return viewDescriptionHTML(str);

    }

    public static boolean will_HTML(String str) {
        if (str.startsWith("#") || str.startsWith("[") || str.startsWith("<"))
            // it is HTML
            return true;

        return false;
    }

    public static String isNum_And_Length(String str, int length) {
        try {
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            return "Not Namber";
        }
        if (str.length() != length)
            return "Error Size";
        return "ok";
    }

    public static BigDecimal getBlockSegToBigInteger(Transaction transaction) {
        if (transaction == null)
            return new BigDecimal(-2);
        if (transaction.isConfirmed(DCSet.getInstance())) {
            String m = transaction.getBlockHeight() + "";
            String d = transaction.getSeqNo() + "";
            int zz = 5 - d.length();
            for (int z = 0; z < zz; z++) {
                d = "0" + d;
            }
            String bd = m + "." + d;
            return new BigDecimal(bd).setScale(5);
        }
        return new BigDecimal(-1);

    }

    public static String viewDescriptionHTML(String descr) {

        if (descr.startsWith("#"))
            // MARK DOWN
            return Processor.process(descr);

        if (descr.startsWith("["))
            // FORUM CKeditor
            // TODO CK_editor INSERT
            return Processor.process(descr);

        if (descr.startsWith("{"))
            // it is DOCX
            // TODO DOCX insert
            return descr;

        if (descr.startsWith("<"))
            // it is HTML
            return descr;

        // PLAIN TEXT
        return descr.replaceAll(" ", "&ensp;")
                .replaceAll("\t", "&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replaceAll("\n", "<br>");

    }

    /**
     * Save JSON String to era File
     *
     * @param parent  - getParent()
     * @param outText - JSON STRING
     * @param pref
     * @param extDesc
     * @param ext
     */
    public static void saveToFile(Container parent, String outText, String pref, String extDesc, String ext) {
        FileChooser chooser = new FileChooser();
        chooser.setDialogTitle(Lang.T("Save File"));
        // chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(extDesc, ext);
        chooser.setAcceptAllFileFilterUsed(false);// only filter
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        File file = new File(pref + "." + ext);
        chooser.setSelectedFile(file);

        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {

            String pp = chooser.getSelectedFile().getPath();
            File testFile = new File(pp);
            File ff;
            if (file.isFile()) {
                ff = testFile;
            } else {
                ff = new File(pp + "." + ext);
            }

            // if file
            if (ff.exists() && ff.isFile()) {
                int aaa = JOptionPane.showConfirmDialog(chooser,
                        Lang.T("File") + Lang.T("Exists") + "! "
                                + Lang.T("Overwrite") + "?",
                        Lang.T("Message"), JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                System.out.print("\n gggg " + aaa);
                if (aaa != 0) {
                    return;
                }
                ff.delete();

            }

            try (FileWriter fw = new FileWriter(ff)) {
                fw.write(outText);
            } catch (IOException e) {
                System.out.println(e);
            }

        }

    }

    public static void saveJSONtoFileSystem(Container parent, Jsonable jsonable, String pref) {
        Library.saveToFile(parent, jsonable.toJson().toJSONString(), pref, "JSON", "json");
    }

    public static void saveAsBase58FileSystem(Container parent, byte[] data, String pref) {
        Library.saveToFile(parent, Base58.encode(data), pref, "RAW Base58", "raw58");
    }

    public static void saveAsBase64FileSystem(Container parent, byte[] data, String pref) {
        Library.saveToFile(parent, Base64.getEncoder().encodeToString(data), pref, "RAW Base64", "raw64");
    }

    //добавляем в конец стандартные меню копировать, вырезать
    //
    public static void addStandartMenuItems(JPopupMenu menu, JTextField component) {
        JMenuItem item;
        item = new JMenuItem(new DefaultEditorKit.CopyAction());
        item.setText(Lang.T("Copy"));
        item.setEnabled(true);
        menu.add(item);

        item = new JMenuItem(new DefaultEditorKit.CutAction());
        item.setText(Lang.T("Cut"));
        item.setEnabled(true);
        menu.add(item);

        item = new JMenuItem(new DefaultEditorKit.PasteAction());
        item.setText(Lang.T("Paste"));
        item.setEnabled(component.isEditable());
        menu.add(item);

    }
}
