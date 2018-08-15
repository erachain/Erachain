package gui.library;

import com.github.rjeschke.txtmark.Processor;
import controller.Controller;
import core.account.Account;
import core.transaction.ArbitraryTransaction;
import core.transaction.BuyNameTransaction;
import core.transaction.CancelOrderTransaction;
import core.transaction.CancelSellNameTransaction;
import core.transaction.CreateOrderTransaction;
import core.transaction.CreatePollTransaction;
import core.transaction.GenesisCertifyPersonRecord;
import core.transaction.GenesisIssueAssetTransaction;
import core.transaction.GenesisIssueTemplateRecord;
import core.transaction.GenesisTransferAssetTransaction;
import core.transaction.IssueAssetTransaction;
import core.transaction.IssueImprintRecord;
import core.transaction.IssuePersonRecord;
import core.transaction.IssuePollRecord;
import core.transaction.IssueStatusRecord;
import core.transaction.IssueTemplateRecord;
import core.transaction.IssueUnionRecord;
import core.transaction.MultiPaymentTransaction;
import core.transaction.R_Hashes;
import core.transaction.R_Send;
import core.transaction.R_SertifyPubKeys;
import core.transaction.R_SetStatusToItem;
import core.transaction.R_SignNote;
import core.transaction.R_Vouch;
import core.transaction.RegisterNameTransaction;
import core.transaction.SellNameTransaction;
import core.transaction.Transaction;
import core.transaction.UpdateNameTransaction;
import core.transaction.VoteOnItemPollTransaction;
import core.transaction.VoteOnPollTransaction;
import datachain.DCSet;
import de.muntjak.tinylookandfeel.Theme;
import lang.Lang;
import settings.Settings;
import utils.PlaySound;
import utils.SysTray;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

/*
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.skin.*;
import org.jvnet.substance.skin.SubstanceNebulaBrickWallLookAndFeel;
 */

public class library {

    // PLAY SOUND
    public static void notifySysTrayRecord(Transaction record) {

        R_Send r_Send = (R_Send) record;
        Account account = Controller.getInstance().getAccountByAddress(r_Send.getRecipient().getAddress());
        if (account != null) {
            if (Settings.getInstance().isSoundReceiveMessageEnabled()) {
                PlaySound.getInstance().playSound("receivemessage.wav", record.getSignature());
            }

            SysTray.getInstance().sendMessage("Payment received",
                    "From: " + r_Send.getCreator().getPersonAsString() + "\nTo: " + account.getPersonAsString() + "\n"
                            + "Asset Key" + ": " + r_Send.getAbsKey() + ", " + "Amount" + ": "
                            + r_Send.getAmount().toPlainString(),
                    MessageType.INFO);
        } else if (Settings.getInstance().isSoundNewTransactionEnabled()) {
            PlaySound.getInstance().playSound("newtransaction.wav", record.getSignature());
        }

    }

    public static void Set_GUI_Look_And_Feel(String text) {
        String name_font = "Courier";
        int size_font;

        // theme
        String name_Theme = Settings.getInstance().get_LookAndFell();

        if (name_Theme.equals("System")) {

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        if (name_Theme.equals("Metal")) {

            try {
                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (name_Theme.equals("Other")) {

            try {

                UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        /*
         * //USE SYSTEM STYLE try { int a = 1; //
         * UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         * // UIManager.setLookAndFeel(
         * UIManager.getCrossPlatformLookAndFeelClassName()); // С‚РѕР¶Рµ С‡С‚Рѕ
         * UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
         * // work //
         * UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
         * ;; // РЅРµ СѓРІРµР»РёС‡РёРІР°РµС‚ С€СЂРёС„С‚С‹ //
         * UIManager.setLookAndFeel(
         * "com.sun.java.swing.plaf.motif.MotifLookAndFeel");; // work //
         * UIManager.setLookAndFeel(
         * "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); // works //
         * UIManager.setLookAndFeel(
         * "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel"); //
         * works // UIManager.setLookAndFeel(
         * UIManager.getCrossPlatformLookAndFeelClassName());
         * //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel")
         * ;// // com.sun.java.swing.plaf.gtk.GTKLookAndFeel //
         * com.sun.java.swing.plaf.motif.MotifLookAndFeel //
         * com.sun.java.swing.plaf.windows.WindowsLookAndFeel
         *
         *
         *
         *
         */

        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        System.setProperty("sun.awt.noerasebackground", "true");
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        if (text == "") {
            size_font = new Integer(Settings.getInstance().get_Font());
            name_font = Settings.getInstance().get_Font_Name();

        } else {
            size_font = new Integer(text);
        }

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
        UIManager.put("OptionPane.yesButtonText", Lang.getInstance().translate("Confirm"));
        UIManager.put("OptionPane.noButtonText", Lang.getInstance().translate("Cancel"));
        UIManager.put("OptionPane.cancelButtonText", Lang.getInstance().translate("Cancel"));
        UIManager.put("OptionPane.okButtonText", Lang.getInstance().translate("OK"));
        UIManager.put("OptionPane.titleFont", font);

        UIManager.put("SplitPane.oneTouchButtonSize", size_font * 2);
        UIManager.put("SplitPane.supportsOneTouchButtons", true);
        UIManager.put("SplitPane.dividerSize", size_font);
        UIManager.put("SplitPaneDivider.oneTouchButtonSize", size_font * 2);
        UIManager.put("SplitPane.centerOneTouchButtons", true);

        if (size_font > 16)
            UIManager.put("ScrollBar.width", size_font);

        // .setUIFont(new
        // javax.swing.plaf.FontUIResource("Tahoma",Font.PLAIN,12));

        // ArrayList<Tuple2<String,Object>> ss = new ArrayList<Tuple2<String,
        // Object>>();

        // UIManager.put("RadioButton.focus", new Color(0, 0, 0, 0));
        // UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        // UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
        // UIManager.put("ComboBox.focus", new Color(0, 0, 0, 0));
        // UIManager.put("TextArea.font", UIManager.get("TextField.font"));

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

        // font = font;

    }

    /*
     * public static void setupSubstance() { try { final String fileName =
     * System.getProperty("user.home") + System.getProperty("file.separator") +
     * "insubstantial.txt"; final Properties properties = new Properties();
     * LookAndFeel laf = new SubstanceGeminiLookAndFeel();
     * UIManager.setLookAndFeel(laf);
     * UIManager.put(SubstanceGeminiLookAndFeel.SHOW_EXTRA_WIDGETS,
     * Boolean.TRUE); JFrame.setDefaultLookAndFeelDecorated(true);
     * JDialog.setDefaultLookAndFeelDecorated(true);
     * Runtime.getRuntime().addShutdownHook(new Thread() {
     *
     * @Override public void run() { try { String skinClassName =
     * SubstanceLookAndFeel.getCurrentSkin().getClass().getCanonicalName();
     * properties.setProperty("skinClassName", skinClassName);
     * properties.store(new FileOutputStream(fileName), fileName); } catch
     * (Throwable t) { t.printStackTrace(); } } }); properties.load(new
     * FileInputStream(fileName)); String skinClassName =
     * properties.getProperty("skinClassName"); ((SubstanceLookAndFeel)
     * laf).setSkin(skinClassName); } catch (Throwable t) { t.printStackTrace();
     * } }
     */

    public static String to_HTML(String str) {

        return viewDescriptionHTML(str);

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
        if (transaction.isConfirmed(DCSet.getInstance())) {
            String m = transaction.getBlockHeight(DCSet.getInstance()) + "";
            String d = transaction.getSeqNo(DCSet.getInstance()) + "";
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
        return descr.replaceAll(" ", "&ensp;").replaceAll("\t", "&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;")
                .replaceAll("\n", "<br>");

    }
    
    /**
     * Save JSON String to era File
     * @param parent - getParent()
     * @param JSONString - JSON STRING
     */
    public static void saveJSONStringToEraFile(Container parent, String JSONString){
        // String raw = Base58.encode(transaction.toBytes(false, null));
        My_JFileChooser chooser = new My_JFileChooser();
        chooser.setDialogTitle(Lang.getInstance().translate("Save File"));
        // chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.era","*.*");
        chooser.setFileFilter(filter);

        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {

            String pp = chooser.getSelectedFile().getPath();

            File ff = new File(pp + ".era");
            // if file
            if (ff.exists() && ff.isFile()) {
                int aaa = JOptionPane.showConfirmDialog(chooser,
                        Lang.getInstance().translate("File") + Lang.getInstance().translate("Exists") + "! "
                                + Lang.getInstance().translate("Overwrite") + "?",
                        Lang.getInstance().translate("Message"), JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                System.out.print("\n gggg " + aaa);
                if (aaa != 0) {
                    return;
                }
                ff.delete();

            }

            try (FileWriter fw = new FileWriter(ff)) {
                fw.write(JSONString);
            } catch (IOException e) {
                System.out.println(e);
            }

            
        }

       
    }
    
    public static void saveTransactionJSONtoFileSystem(Container parent,Transaction trans){
        String jsonString ="";
        switch (trans.getType()) {

        case Transaction.SIGN_NOTE_TRANSACTION:

            jsonString = ((R_SignNote) trans).toJson().toJSONString();
            break;
            
        case Transaction.REGISTER_NAME_TRANSACTION:

            jsonString = ((RegisterNameTransaction) trans).toJson().toJSONString();
            break;

        case Transaction.UPDATE_NAME_TRANSACTION:

            jsonString = ((UpdateNameTransaction) trans).toJson().toJSONString();
            break;

        case Transaction.SELL_NAME_TRANSACTION:

            jsonString = ((SellNameTransaction) trans).toJson().toJSONString();
            break;

        case Transaction.CANCEL_SELL_NAME_TRANSACTION:

            jsonString = ((CancelSellNameTransaction) trans).toJson().toJSONString();
            break;

        case Transaction.BUY_NAME_TRANSACTION:

            jsonString = ((BuyNameTransaction) trans).toJson().toJSONString();
            break;

        case Transaction.CREATE_POLL_TRANSACTION:

            jsonString = ((CreatePollTransaction) trans).toJson().toJSONString();

            break;

        case Transaction.VOTE_ON_POLL_TRANSACTION:

            jsonString = ((VoteOnPollTransaction) trans).toJson().toJSONString();
            break;

        case Transaction.VOTE_ON_ITEM_POLL_TRANSACTION:

            jsonString = ((VoteOnItemPollTransaction) trans).toJson().toJSONString();
            break;

        case Transaction.ARBITRARY_TRANSACTION:

            jsonString = ((ArbitraryTransaction) trans).toJson().toJSONString();

            break;

        case Transaction.ISSUE_ASSET_TRANSACTION:

            jsonString = ((IssueAssetTransaction) trans).toJson().toJSONString();
            break;

        case Transaction.ISSUE_PERSON_TRANSACTION:

            jsonString = ((IssuePersonRecord) trans).toJson().toJSONString();

            break;

        case Transaction.ISSUE_POLL_TRANSACTION:

            jsonString = ((IssuePollRecord) trans).toJson().toJSONString();
            break;

        case Transaction.SET_STATUS_TO_ITEM_TRANSACTION:

            jsonString = ((R_SetStatusToItem) trans).toJson().toJSONString();
            break;
           
        case Transaction.CREATE_ORDER_TRANSACTION:

            jsonString = ((CreateOrderTransaction) trans).toJson().toJSONString();
            break;

        case Transaction.CANCEL_ORDER_TRANSACTION:

            jsonString = ((CancelOrderTransaction) trans).toJson().toJSONString();
            break;

        case Transaction.MULTI_PAYMENT_TRANSACTION:

            jsonString = ((MultiPaymentTransaction) trans).toJson().toJSONString();
            break;

        case Transaction.SEND_ASSET_TRANSACTION:
            jsonString = ((R_Send) trans).toJson().toJSONString();
            break;

        case Transaction.VOUCH_TRANSACTION:
            jsonString = ((R_Vouch) trans).toJson().toJSONString();
            break;

        case Transaction.CERTIFY_PUB_KEYS_TRANSACTION:
            jsonString = ((R_SertifyPubKeys) trans).toJson().toJSONString();
            break;

        case Transaction.HASHES_RECORD:
            jsonString = ((R_Hashes) trans).toJson().toJSONString();
            break;

        case Transaction.ISSUE_IMPRINT_TRANSACTION:

            jsonString = ((IssueImprintRecord) trans).toJson().toJSONString();
            break;

        case Transaction.ISSUE_TEMPLATE_TRANSACTION:

            jsonString = ((IssueTemplateRecord) trans).toJson().toJSONString();
            break;
            
        case Transaction.ISSUE_UNION_TRANSACTION:

            jsonString = ((IssueUnionRecord) trans).toJson().toJSONString();
            break;

        case Transaction.ISSUE_STATUS_TRANSACTION:

            jsonString = ((IssueStatusRecord) trans).toJson().toJSONString();
            break;

        case Transaction.GENESIS_SEND_ASSET_TRANSACTION:

            jsonString = ((GenesisTransferAssetTransaction) trans).toJson().toJSONString();

            break;

        case Transaction.GENESIS_ISSUE_TEMPLATE_TRANSACTION:

            jsonString = ((GenesisIssueTemplateRecord) trans).toJson().toJSONString();
            break;

        case Transaction.GENESIS_ISSUE_ASSET_TRANSACTION:

            jsonString = ((GenesisIssueAssetTransaction) trans).toJson().toJSONString();

            break;

        case Transaction.GENESIS_ISSUE_PERSON_TRANSACTION:

            jsonString = ((GenesisCertifyPersonRecord) trans).toJson().toJSONString();

            break;
         default:
             jsonString = (trans).toJson().toJSONString();
        
        }
        if (jsonString.equals("")) return;
        library.saveJSONStringToEraFile(parent, jsonString);
    }
}
