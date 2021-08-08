package org.erachain.gui.create;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.lang.Lang;
import org.erachain.lang.LangFile;
import org.erachain.settings.Settings;
import org.erachain.utils.SaveStrToFile;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class SettingLangFrame extends JDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingLangFrame.class);
    private JList<LangFile> listLang;
    private JComboBox<String> size_Font;
    private JButton nextButton;
    //private SettingLangFrame th;
    private JLabel labelSelect;
    private JLabel Label_font_size;

    public SettingLangFrame() {
        super();
        this.setTitle(Controller.getInstance().getApplicationName(false) + " - " + "Language select");
        this.setModal(true);
        this.isAlwaysOnTop();
        //th = this;
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

        Label_font_size = new JLabel("Font Size:");
        this.add(Label_font_size, font_LabelGBC);
        //LANGS GBC
        GridBagConstraints fontGBC = new GridBagConstraints();
        fontGBC.insets = new Insets(0, 0, 5, 0);
        fontGBC.fill = GridBagConstraints.BOTH;
        fontGBC.anchor = GridBagConstraints.NORTHWEST;
        fontGBC.gridy = 3;
        fontGBC.gridx = 1;

        size_Font = new javax.swing.JComboBox<String>();
        size_Font.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[]{"11", "12", "14", "16", "18", "20", "24"}));
        size_Font.setSelectedItem(Settings.getInstance().getFontSize());
        this.add(size_Font, fontGBC);

        //BUTTON GBC
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(0, 0, 0, 0);
        buttonGBC.fill = GridBagConstraints.BOTH;
        buttonGBC.anchor = GridBagConstraints.NORTHWEST;
        buttonGBC.gridy = 4;
        buttonGBC.gridx = 1;

        labelSelect = new JLabel(Lang.T("Language") + ":");
        this.add(labelSelect, labelGBC);

        // read internet
        DefaultListModel<LangFile> listModel = new DefaultListModel<LangFile>();
        for (String iso : Lang.getInstance().getLangListAvailable().keySet()) {
            listModel.addElement(Lang.getInstance().getLangFile(iso));
        }

        listLang = new JList<LangFile>(listModel);
        listLang.setSelectedIndex(0);
        listLang.setFocusable(false);
        listLang.addListSelectionListener(e -> {
            String valueLang = listLang.getSelectedValue().toString();

            switch (valueLang) {
                case "[ru] Русский":
                    Label_font_size.setText("Размер шрифта:");
                    labelSelect.setText("Язык");
                    break;
                case "[en] English":
                    Label_font_size.setText("Font size:");
                    labelSelect.setText("Language");
                    break;
            }
        });

        JScrollPane scrollPaneLang = new JScrollPane(listLang);

        this.add(scrollPaneLang, listLangGBC);

        onOKClick();

        //BUTTON OK
        nextButton = new JButton("OK");
        nextButton.addActionListener(e -> onOKClick());
        this.add(nextButton, buttonGBC);

        //CLOSE NICELY
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Controller.getInstance().stopAndExit(0);
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
                    Label_font_size.setFont(new Font(font.getName(), Font.PLAIN, new Integer(size)));
                    size_Font.setFont(new Font(font.getName(), Font.PLAIN, new Integer(size)));
                    nextButton.setFont(new Font(font.getName(), Font.PLAIN, new Integer(size)));
                    pack();
                    repaint();
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
//                String url = Lang.translationsUrl + langFileName;
//                FileUtils.copyURLToFile(new URL(url), new File(Settings.getInstance().getLangDir(), langFileName));
            }
            JSONObject settingsLangJSON = Settings.getInstance().Dump();
            settingsLangJSON.put("lang", langFileName);
            if (size_Font.getSelectedItem().toString() != "") {
                settingsLangJSON.put("font_size", size_Font.getSelectedItem().toString());
            }
            SaveStrToFile.saveJsonFine(Settings.getInstance().getSettingsPath(), settingsLangJSON);
            Settings.freeInstance();
            Lang.getInstance().setLangForNode();
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