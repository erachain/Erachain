package gui.create;
// 30/03

import com.google.common.base.Charsets;
import controller.Controller;
import lang.Lang;
import lang.LangFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import settings.Settings;
import utils.SaveStrToFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class SettingLangFrame extends JDialog {

    private static final Logger LOGGER = Logger
            .getLogger(SettingLangFrame.class);
    private JList<LangFile> listLang;
    private JComboBox<String> size_Font;
    private JButton nextButton;
    private SettingLangFrame th;
    private JLabel labelSelect;
    private JLabel label_font_size;

    public SettingLangFrame() {
        super();
        this.setTitle("Erachain.org" + " - " + "Language select");
        this.setModal(true);
        this.isAlwaysOnTop();
        th = this;
        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{10, 300, 10};
        gridBagLayout.rowHeights = new int[]{20, 200, 20, 10};

        //LAYOUT
        this.setLayout(gridBagLayout);

        //LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.fill = GridBagConstraints.BOTH;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.gridy = 0;
        labelGBC.gridx = 1;

        //LANGS GBC
        GridBagConstraints listLangGBC = new GridBagConstraints();
        listLangGBC.insets = new Insets(0, 0, 5, 0);
        listLangGBC.fill = GridBagConstraints.BOTH;
        listLangGBC.anchor = GridBagConstraints.NORTHWEST;
        listLangGBC.gridy = 1;
        listLangGBC.gridx = 1;

        //LANGS GBC
        GridBagConstraints font_LabelGBC = new GridBagConstraints();
        font_LabelGBC.insets = new Insets(0, 0, 5, 0);
        font_LabelGBC.fill = GridBagConstraints.BOTH;
        font_LabelGBC.anchor = GridBagConstraints.NORTHWEST;
        font_LabelGBC.gridy = 2;
        font_LabelGBC.gridx = 1;

        label_font_size = new JLabel("Font Size:");
        this.add(label_font_size, font_LabelGBC);
        //LANGS GBC
        GridBagConstraints fontGBC = new GridBagConstraints();
        fontGBC.insets = new Insets(0, 0, 5, 0);
        fontGBC.fill = GridBagConstraints.BOTH;
        fontGBC.anchor = GridBagConstraints.NORTHWEST;
        fontGBC.gridy = 3;
        fontGBC.gridx = 1;

        size_Font = new javax.swing.JComboBox<String>();
        size_Font.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[]{"11", "12", "14", "16", "18", "20", "24"}));
        this.add(size_Font, fontGBC);

        //BUTTON GBC
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(0, 0, 0, 0);
        buttonGBC.fill = GridBagConstraints.BOTH;
        buttonGBC.anchor = GridBagConstraints.NORTHWEST;
        buttonGBC.gridy = 4;
        buttonGBC.gridx = 1;

        labelSelect = new JLabel(Lang.getInstance().translate("Language") + ":");
        this.add(labelSelect, labelGBC);

        // read internet
        String stringFromInternet;
        try {
            String url = Lang.translationsUrl + "available.json";

            URL u = new URL(url);
            InputStream in = u.openStream();
            stringFromInternet = IOUtils.toString(in, Charsets.UTF_8);
        } catch (Exception e1) {
            LOGGER.error(e1.getMessage(), e1);
            stringFromInternet = "";
        }

        JSONObject inernetLangsJSON = (JSONObject) JSONValue.parse(stringFromInternet);

        DefaultListModel<LangFile> listModel = new DefaultListModel<LangFile>();
        listModel.addElement(new LangFile());
        if (inernetLangsJSON != null && !inernetLangsJSON.isEmpty()) {
            for (Object internetKey : inernetLangsJSON.keySet()) {
                JSONObject internetValue = (JSONObject) inernetLangsJSON.get(internetKey);
                listModel.addElement(new LangFile((String) internetValue.get("_lang_name_"),
                        (String) internetValue.get("_file_"),
                        Long.parseLong(internetValue.get("_timestamp_of_translation_").toString())
                ));
            }
        }

        listLang = new JList<LangFile>(listModel);
        listLang.setSelectedIndex(0);
        listLang.setFocusable(false);
        listLang.addListSelectionListener(e -> {
            String valueLang = listLang.getSelectedValue().toString();

            switch (valueLang) {
                case "[ru] Русский":
                    label_font_size.setText("Размер шрифта:");
                    labelSelect.setText("Язык");
                    break;
                case "[en] English":
                    label_font_size.setText("Font size:");
                    labelSelect.setText("Language");
                    break;
            }
        });

        JScrollPane scrollPaneLang = new JScrollPane(listLang);

        this.add(scrollPaneLang, listLangGBC);

        if (inernetLangsJSON == null || inernetLangsJSON.isEmpty()) {
            onOKClick();
            return;
        }

        //BUTTON OK
        nextButton = new JButton("OK");
        nextButton.addActionListener(e -> onOKClick());
        this.add(nextButton, buttonGBC);

        //CLOSE NICELY
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Controller.getInstance().stopAll(0);
                // 		System.exit(0);
            }
        });

        size_Font.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String size = size_Font.getSelectedItem().toString();

                    //  nextButton.repaint();
                    //  SwingUtilities.updateComponentTreeUI(nextButton);
                    Font font = listLang.getFont();
                    listLang.setFont(new Font(font.getName(), Font.PLAIN, new Integer(size)));
                    labelSelect.setFont(new Font(font.getName(), Font.PLAIN, new Integer(size)));
                    label_font_size.setFont(new Font(font.getName(), Font.PLAIN, new Integer(size)));
                    size_Font.setFont(new Font(font.getName(), Font.PLAIN, new Integer(size)));
                    nextButton.setFont(new Font(font.getName(), Font.PLAIN, new Integer(size)));
                    pack();
                    th.repaint();
                }
            }
        });

        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
        this.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    public void onOKClick() {
        try {
            String langFileName = listLang.getSelectedValue().getFileName();

            if (listLang.getSelectedIndex() > 0) {
                String url = Lang.translationsUrl + langFileName;
                FileUtils.copyURLToFile(new URL(url), new File(Settings.getInstance().getLangDir(), langFileName));
            }
            JSONObject settingsLangJSON = Settings.getInstance().Dump();
            settingsLangJSON.put("lang", langFileName);
            if (size_Font.getSelectedItem().toString() != "") {
                settingsLangJSON.put("font_size", size_Font.getSelectedItem().toString());
            }
            SaveStrToFile.saveJsonFine(Settings.getInstance().getSettingsPath(), settingsLangJSON);
            Settings.FreeInstance();
            Lang.getInstance().loadLang();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(
                    new JFrame(), "Error writing to the file: "
                            + "\nProbably there is no access.",
                    "Error!",
                    JOptionPane.ERROR_MESSAGE);
        }

        this.dispose();
    }
}