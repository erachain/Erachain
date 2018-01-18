package gui.library;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.TrayIcon.MessageType;
import java.math.BigDecimal;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import com.github.rjeschke.txtmark.Processor;
import controller.Controller;
import core.account.Account;
import core.transaction.R_Send;
import core.transaction.Transaction;
import datachain.DCSet;
import de.muntjak.tinylookandfeel.Theme;

/*
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.skin.*;
import org.jvnet.substance.skin.SubstanceNebulaBrickWallLookAndFeel;
*/

import lang.Lang;
import settings.Settings;
import utils.PlaySound;
import utils.SysTray;

public class library {

	// PLAY SOUND
	public static void notifySysTrayRecord(Transaction record) {

		R_Send r_Send = (R_Send) record;
		Account account = Controller.getInstance().getAccountByAddress(r_Send.getRecipient().getAddress());
		if (account != null) {
			if (Settings.getInstance().isSoundReceiveMessageEnabled()) {
				PlaySound.getInstance().playSound("receivemessage.wav", ((Transaction) record).getSignature());
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
		 * Рё РјРµС‚Р°Р» //
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
		UIManager.put("OptionPane.okButtonText", Lang.getInstance().translate("Yes"));
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

		if (descr.startsWith("]"))
			// FORUM CKeditor
			// TODO CK_editor INSERT
			return Processor.process(descr);

		if (descr.startsWith("}"))
			// it is DOCX
			// TODO DOCX insert
			return descr;

		if (descr.startsWith(">"))
			// it is HTML
			return descr;

		// PLAIN TEXT
		return descr.replaceAll(" ", "&ensp;").replaceAll("\t", "&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;")
				.replaceAll("\n", "<br>");

	}
}
