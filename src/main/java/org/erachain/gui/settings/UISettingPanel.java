package org.erachain.gui.settings;

import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.erachain.gui.library.MTable;
import org.erachain.lang.Lang;
import org.erachain.lang.LangFile;
import org.erachain.settings.Settings;
import org.erachain.utils.DateTimeFormat;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Саша
 */
public class UISettingPanel extends javax.swing.JPanel {

    protected Logger logger;

    public ButtonGroup group;
    public javax.swing.JComboBox<String> font_Name;
    public javax.swing.JComboBox<String> size_Font;
    public javax.swing.JComboBox<LangFile> jComboBox_Lang;
    public javax.swing.JComboBox<String> jComboBox_Thems;
    public JCheckBox checkMarkIncome;
    public javax.swing.JTextField markColor;
    public javax.swing.JTextField markColorSelected;

    public JCheckBox chckbxSysTrayEvent;
    public JCheckBox chckbxSoundReceivePayment;
    public JCheckBox chckbxSoundReceiveMessage;
    public JCheckBox chckbxSoundNewTransaction;
    public JCheckBox chckbxSoundForgedBlock;

    public JCheckBox chckbxSystemLookFeel;
    public JCheckBox chckbxMetallLookFeel;
    public JCheckBox chckbxOtherTemes;
    public JPanel Theme_Select_Panel;
    public JRadioButton other_Themes;
    public JRadioButton system_Theme;
    public JRadioButton metal_Theme;
    // Variables declaration - do not modify
    private javax.swing.JLabel Label_Titlt;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton_Download_Lang;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel_Font;
    private javax.swing.JLabel jLabel_Lang;
    private javax.swing.JLabel jLabel_Thems;
    private javax.swing.JLabel jLabel_sounds;
    /**
     * Creates new form UISetting_Panel
     */
    public UISettingPanel() {

        logger = LoggerFactory.getLogger(getClass());

        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        Label_Titlt = new JLabel();
        jLabel_Thems = new JLabel();
        jComboBox_Thems = new JComboBox<String>();
        jLabel_Font = new JLabel();
        // jComboBox_Font_Name = new javax.swing.JComboBox<>();
        // jComboBox_Font_Size = new javax.swing.JComboBox<>();
        jLabel_Lang = new JLabel();
        //  jComboBox_Lang = new javax.swing.JComboBox<LangFile>();
        jButton_Download_Lang = new JButton();
        //   jCheckBoxSend_Asset = new javax.swing.JCheckBox();
        //    jCheckBox_Send_message = new javax.swing.JCheckBox();
        //    javax.swing.JCheckBox jCheckBox3_Other_Trans = new javax.swing.JCheckBox();
        jLabel_sounds = new JLabel();
        jLabel1 = new JLabel();
        jButton1 = new JButton();
        jButton2 = new JButton();

        setLayout(new GridBagLayout());

        Label_Titlt.setText("");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(8, 12, 0, 0);
        //add(Label_Titlt, gridBagConstraints);


        group = new ButtonGroup();

        other_Themes = new JRadioButton(Lang.getInstance().translate("Other Themes"), false);
        group.add(other_Themes);
        system_Theme = new JRadioButton(Lang.getInstance().translate("System Theme"), true);
        group.add(system_Theme);
        metal_Theme = new JRadioButton(Lang.getInstance().translate("Metal Theme"), true);
        group.add(metal_Theme);
        other_Themes.isSelected();


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(7, 0, 8, 10);
        add(other_Themes, gridBagConstraints);


        if (Settings.getInstance().get_LookAndFell().equals("Other")) other_Themes.setSelected(true);


        other_Themes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // Размер соответствует последнему параметру метода addRadioButton
                jComboBox_Thems.setEnabled(other_Themes.isSelected());
            }
        });


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(7, 0, 8, 10);
        add(system_Theme, gridBagConstraints);
        system_Theme.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // Размер соответствует последнему параметру метода addRadioButton
                jComboBox_Thems.setEnabled(!system_Theme.isSelected());
            }
        });

        if (Settings.getInstance().get_LookAndFell().equals("System")) {


            jComboBox_Thems.setEnabled(false);
            system_Theme.setSelected(true);
        }


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(7, 0, 8, 10);
        add(metal_Theme, gridBagConstraints);

        metal_Theme.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // Размер соответствует последнему параметру метода addRadioButton
                jComboBox_Thems.setEnabled(!metal_Theme.isSelected());
            }
        });

        if (Settings.getInstance().get_LookAndFell().equals("Metal")) {


            jComboBox_Thems.setEnabled(false);
            metal_Theme.setSelected(true);
        }


        jLabel_Thems.setText(Lang.getInstance().translate("Select Theme") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(0, 10, 0, 10);
        add(jLabel_Thems, gridBagConstraints);


        jComboBox_Thems.setModel(new DefaultComboBoxModel<String>(new String[]{"YQ Theme", "Unicode", "Silver", "Plastic", "Nightly", "Golden", "Forest"}));

        jComboBox_Thems.setSelectedItem(Settings.getInstance().get_Theme());


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(8, 0, 9, 11);
        add(jComboBox_Thems, gridBagConstraints);


        jLabel_Font.setText(Lang.getInstance().translate("Font") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new Insets(0, 10, 0, 10);
        add(jLabel_Font, gridBagConstraints);

        font_Name = new JComboBox<String>();


        font_Name.setModel(new DefaultComboBoxModel<String>(new String[]{"Arial", "Courier", "Tahoma", "Times New Roman"}));
        font_Name.setSelectedItem(Settings.getInstance().get_Font_Name());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new Insets(0, 0, 11, 0);
        add(font_Name, gridBagConstraints);

        size_Font = new JComboBox<String>();
        size_Font.setModel(new DefaultComboBoxModel<String>(new String[]{"11", "12", "14", "16", "18", "20", "24"}));
        size_Font.setSelectedItem(Settings.getInstance().get_Font());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 7, 8, 10);
        add(size_Font, gridBagConstraints);

        jLabel_Lang.setText(Lang.getInstance().translate("Interface language") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new Insets(0, 10, 0, 10);
        add(jLabel_Lang, gridBagConstraints);


        jComboBox_Lang = new JComboBox<LangFile>();

        for (LangFile langFile : Lang.getInstance().getLangListAvailable()) {
            jComboBox_Lang.addItem(langFile);

            if (langFile.getFileName().equals(Settings.getInstance().getLangFileName())) {
                jComboBox_Lang.setSelectedItem(langFile);
            }
        }


        //       jComboBox_Lang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.insets = new Insets(0, 0, 11, 0);
        add(jComboBox_Lang, gridBagConstraints);

        jButton_Download_Lang.setText(Lang.getInstance().translate("Download"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new Insets(0, 21, 0, 12);
        add(jButton_Download_Lang, gridBagConstraints);


        jButton_Download_Lang.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton_Download_Lang.setText("...");
                jButton_Download_Lang.repaint(0);

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final JPopupMenu menu = new JPopupMenu();

                            String stringFromInternet = "";
                            try {
                                String url = Lang.translationsUrl + "available.json";

                                URL u = new URL(url);

                                InputStream in = u.openStream();
                                stringFromInternet = IOUtils.toString(in, Charsets.UTF_8);
                            } catch (Exception e1) {
                                logger.error(e1.getMessage(), e1);
                            }
                            JSONObject inernetLangsJSON = (JSONObject) JSONValue.parse(stringFromInternet);


                            for (Object internetKey : inernetLangsJSON.keySet()) {

                                JSONObject internetValue = (JSONObject) inernetLangsJSON.get(internetKey);

                                String itemText = null;
                                final String langFileName = (String) internetValue.get("_file_");

                                long time_of_translation = Long.parseLong(internetValue.get("_timestamp_of_translation_").toString());

                                try {
                                    //logger.error("try lang file: " + langFileName);
                                    JSONObject oldLangFile = Lang.openLangFile(langFileName);

                                    if (oldLangFile == null) {
                                        itemText = (String) internetValue.get("download lang_name translation");

                                    } else if (time_of_translation >
                                            Long.parseLong(oldLangFile.get("_timestamp_of_translation_").toString())
                                            ) {
                                        itemText = ((String) internetValue.get("download update of lang_name translation from %date%")).replace("%date%", DateTimeFormat.timestamptoString(time_of_translation, "yyyy-MM-dd", ""));
                                    }
                                } catch (Exception e2) {
                                    itemText = (String) internetValue.get("download lang_name translation");
                                }

                                if (itemText != null) {

                                    JMenuItem item = new JMenuItem();
                                    item.setText("[" + (String) internetKey + "] " + itemText);

                                    item.addActionListener(new ActionListener() {
                                        public void actionPerformed(ActionEvent e) {
                                            try {
                                                String url = Lang.translationsUrl + langFileName;

                                                FileUtils.copyURLToFile(new URL(url), new File(Settings.getInstance().getLangDir(), langFileName));

                                            } catch (Exception e1) {
                                                logger.error(e1.getMessage(), e1);
                                            }

                                            jComboBox_Lang.removeAllItems();

                                            for (LangFile langFile : Lang.getInstance().getLangListAvailable()) {
                                                jComboBox_Lang.addItem(langFile);

                                                if (langFile.getFileName().equals(langFileName)) {
                                                    jComboBox_Lang.setSelectedItem(langFile);
                                                }
                                            }
                                        }
                                    });

                                    menu.add(item);
                                }

                            }
                            if (menu.getComponentCount() == 0) {
                                JMenuItem item = new JMenuItem();
                                item.setText(Lang.getInstance().translate("No new translations"));
                                item.setEnabled(false);
                                menu.add(item);
                            }

                            menu.show(jComboBox_Lang, 0, jComboBox_Lang.getHeight());
                        } finally {
                            jButton_Download_Lang.setText(Lang.getInstance().translate("Download"));
                        }
                    }
                });
            }
        });

        int gridy = 5;

        checkMarkIncome = new JCheckBox(Lang.getInstance().translate("Mark Outcome or Income"));
        checkMarkIncome.setHorizontalAlignment(SwingConstants.LEFT);
        checkMarkIncome.setSelected(Settings.getInstance().markIncome());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(7, 0, 8, 10);
        add(checkMarkIncome, gridBagConstraints);

        JLabel lLabel_Color = new JLabel(Lang.getInstance().translate("Color") + " (RGB):");
        lLabel_Color.setHorizontalAlignment(SwingConstants.RIGHT);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(7, 0, 8, 10);
        add(lLabel_Color, gridBagConstraints);

        MTable<Object, Object> table = new MTable<>(null);
        markColor = new JTextField("  " + Settings.getInstance().markColor() + "  ");
        markColor.setForeground(Settings.getInstance().markColorObj());
        markColor.setBackground(table.getBackground());
        markColor.setHorizontalAlignment(SwingConstants.LEFT);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(7, 0, 8, 10);
        add(markColor, gridBagConstraints);
        markColor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent arg0) {
                Settings.getInstance().getJSONObject().put("markcolor", markColor.getText());
                markColor.setForeground(Settings.getInstance().markColorObj());
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                Settings.getInstance().getJSONObject().put("markcolor", markColor.getText());
                markColor.setForeground(Settings.getInstance().markColorObj());
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                Settings.getInstance().getJSONObject().put("markcolor", markColor.getText());
                markColor.setForeground(Settings.getInstance().markColorObj());
            }
        });

        JLabel lLabel_ColorS = new JLabel(Lang.getInstance().translate("Selected Color") + " (RGB):");
        lLabel_ColorS.setHorizontalAlignment(SwingConstants.RIGHT);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(7, 0, 8, 10);
        add(lLabel_ColorS, gridBagConstraints);

        markColorSelected = new JTextField("  " + Settings.getInstance().markColorSelected() + "  ");
        markColorSelected.setForeground(Settings.getInstance().markColorSelectedObj());
        markColorSelected.setBackground(table.getSelectionBackground());
        markColorSelected.setHorizontalAlignment(SwingConstants.LEFT);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(7, 0, 8, 10);
        add(markColorSelected, gridBagConstraints);
        markColorSelected.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent arg0) {
                Settings.getInstance().getJSONObject().put("markcolorselected", markColorSelected.getText());
                markColorSelected.setForeground(Settings.getInstance().markColorSelectedObj());
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                Settings.getInstance().getJSONObject().put("markcolorselected", markColorSelected.getText());
                markColorSelected.setForeground(Settings.getInstance().markColorSelectedObj());
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                Settings.getInstance().getJSONObject().put("markcolorselected", markColorSelected.getText());
                markColorSelected.setForeground(Settings.getInstance().markColorSelectedObj());
            }
        });

        chckbxSysTrayEvent = new JCheckBox(Lang.getInstance().translate("System Tray Events and Sounds"));
        chckbxSysTrayEvent.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxSysTrayEvent.setSelected(Settings.getInstance().isSysTrayEnabled());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(7, 0, 8, 10);
        add(chckbxSysTrayEvent, gridBagConstraints);

        ++gridy;
        jLabel_sounds.setText(Lang.getInstance().translate("Sounds") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(8, 0, 0, 0);
        add(jLabel_sounds, gridBagConstraints);

        chckbxSoundReceivePayment = new JCheckBox(Lang.getInstance().translate("Receive payment"));
        chckbxSoundReceivePayment.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxSoundReceivePayment.setSelected(Settings.getInstance().isSoundReceivePaymentEnabled());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(7, 0, 8, 10);
        add(chckbxSoundReceivePayment, gridBagConstraints);

        chckbxSoundReceiveMessage = new JCheckBox(Lang.getInstance().translate("Receive message"));
        chckbxSoundReceiveMessage.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxSoundReceiveMessage.setSelected(Settings.getInstance().isSoundReceiveMessageEnabled());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(7, 0, 8, 10);
        add(chckbxSoundReceiveMessage, gridBagConstraints);

        chckbxSoundNewTransaction = new JCheckBox(Lang.getInstance().translate("Other transactions"));
        chckbxSoundNewTransaction.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxSoundNewTransaction.setSelected(Settings.getInstance().isSoundNewTransactionEnabled());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(7, 0, 8, 10);
        add(chckbxSoundNewTransaction, gridBagConstraints);

        chckbxSoundForgedBlock = new JCheckBox(Lang.getInstance().translate("Forged Block"));
        chckbxSoundForgedBlock.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxSoundForgedBlock.setSelected(Settings.getInstance().isSoundForgedBlockEnabled());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(7, 0, 8, 10);
        add(chckbxSoundForgedBlock, gridBagConstraints);

        if (false) {
            JLabel jLabel_UI = new JLabel(Lang.getInstance().translate("UI") + ":");
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = ++gridy;
            gridBagConstraints.anchor = GridBagConstraints.LINE_END;
            gridBagConstraints.insets = new Insets(8, 0, 0, 0);
            //     add(jLabel_UI, gridBagConstraints);


            jLabel1.setText("UI");
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 10;
            gridBagConstraints.gridwidth = 7;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.weighty = 0.1;
            gridBagConstraints.insets = new Insets(0, 10, 0, 0);
            add(jLabel1, gridBagConstraints);
        }


    }// </editor-fold>
    // End of variables declaration                   
}
