package org.erachain.gui.library;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.templates.ComboBoxModelItemsTemplates;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Set;

public class MFillTemplatePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    public javax.swing.JComboBox<ItemCls> jComboBox_Template;
    public ItemCls sel_Template = null;
    public JCheckBox add_Tamplate;

    // Variables declaration - do not modify
    public JCheckBox jCheckBox_Is_Encripted;
    public MSplitPane sp_pan;
    Params_Template_Model params_Template_Model;
    //private ComboBoxModelItemsTemplates comboBoxModelTemplates;
    private JCheckBox jCheckBox_Is_Text;
    private JLabel jLabel1;
    private JLabel jLabel_Template1;
    private JScrollPane jScrollPane_Message_Public_TextPane;
    private JScrollPane jScrollPane_Params_Template_Public_TextPane;
    private MTable jTable_Params_Message_Public;
    private MImprintEDITPane jTextPane_Message_Public;
    public JCheckBox checkBoxMakeHashAndCheckUniqueTemplate;

    public MFillTemplatePanel() {
        jTextPane_Message_Public = new MImprintEDITPane();
        jComboBox_Template = new JComboBox<>();
        jComboBox_Template.setModel(new ComboBoxModelItemsTemplates());
        jTextPane_Message_Public.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent arg0) {
                // TODO Auto-generated method stub
                String str = null;
                if (arg0.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
                    return;

                String findArg = arg0.getDescription().replace("!$@!", "");
                if (arg0.getDescription().indexOf("!$@!") != 0) {
                } else {
                    str = JOptionPane.showInputDialog(MainFrame.getInstance(),
                            Lang.T("Insert") + " " + findArg,
                            jTextPane_Message_Public.pars.get("{{" + findArg + "}}"));
                }
                if (str == null || str.isEmpty())
                    return;

                jTextPane_Message_Public.fixCaretPosition();

                // RISE event
                for (int i = 0; i < params_Template_Model.getRowCount(); i++) {
                    if (arg0.getDescription().replace("!$@!", "").equals(params_Template_Model.getValueAt(i, 0)))
                        params_Template_Model.setValueAt(str, i, 1);
                }


            }
        });

        initComponents();

        set_Template((ItemCls) jComboBox_Template.getSelectedItem());

        jComboBox_Template.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    set_Template((TemplateCls) jComboBox_Template.getSelectedItem());
                }
            }
        });
    }

    public HashMap<String, String> get_Params() {
        return jTextPane_Message_Public.get_Params();
    }

    public TemplateCls get_TemplateCls() {
        return (TemplateCls) jComboBox_Template.getSelectedItem();
    }

    @SuppressWarnings("unchecked")
    //// <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        jLabel_Template1 = new JLabel();
        jCheckBox_Is_Text = new JCheckBox();
        jCheckBox_Is_Encripted = new JCheckBox();
        sp_pan = new MSplitPane();
        jScrollPane_Message_Public_TextPane = new JScrollPane();
        jScrollPane_Params_Template_Public_TextPane = new JScrollPane();

        params_Template_Model = new Params_Template_Model();
        jTable_Params_Message_Public = new MTable(params_Template_Model);
        params_Template_Model.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent arg0) {
                // TODO Auto-generated method stub

                if (arg0.getType() != 0 && arg0.getColumn() < 0)
                    return;

                jTextPane_Message_Public.updateParam(params_Template_Model.getValueAt(arg0.getFirstRow(), 0).toString(),
                        (String) params_Template_Model.getValueAt(arg0.getFirstRow(), arg0.getColumn()));
            }
        });

        jComboBox_Template.setEnabled(false);
        jTextPane_Message_Public.setVisible(false);
        jTable_Params_Message_Public.setVisible(false);
        add_Tamplate = new JCheckBox();

        jLabel1 = new JLabel();

        setLayout(new GridBagLayout());

        // Make hash and check unique
        checkBoxMakeHashAndCheckUniqueTemplate = new JCheckBox(Lang.T("Make hash and check unique"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(checkBoxMakeHashAndCheckUniqueTemplate, gridBagConstraints);


        add_Tamplate.setText(Lang.T("Insert template") + "?");
        add_Tamplate.addActionListener(new ActionListener() {

                                           @Override
                                           public void actionPerformed(ActionEvent arg0) {
                                               // TODO Auto-generated method stub
                                               jComboBox_Template.setEnabled(add_Tamplate.isSelected());
                                               jTextPane_Message_Public.setVisible(add_Tamplate.isSelected());
                                               jTable_Params_Message_Public.setVisible(add_Tamplate.isSelected());
                                               if (add_Tamplate.isSelected()) {
                                                   set_Template((TemplateCls) jComboBox_Template.getSelectedItem());
                                               } else {
                                                   sel_Template = null;
                                               }

                                           }


                                       }
        );

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(8, 8, 8, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(add_Tamplate, gridBagConstraints);

        jLabel_Template1.setText(Lang.T("Select template") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(8, 8, 8, 0);
        add(jLabel_Template1, gridBagConstraints);


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(8, 0, 8, 0);
        add(jComboBox_Template, gridBagConstraints);

        jCheckBox_Is_Text.setText("jCheckBox1");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 8, 8, 0);
        //	add(jCheckBox_Is_Text, gridBagConstraints);

        jCheckBox_Is_Encripted.setText(Lang.T("Encrypt message"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 8, 0);
//		add(jCheckBox_Is_Encripted, gridBagConstraints);

        jScrollPane_Message_Public_TextPane.setViewportView(jTextPane_Message_Public);

        sp_pan.setLeftComponent(jScrollPane_Message_Public_TextPane);

        jTable_Params_Message_Public.setMinimumSize(new Dimension(250, 100));

        jScrollPane_Params_Template_Public_TextPane.setViewportView(jTable_Params_Message_Public);

        sp_pan.setRightComponent(jScrollPane_Params_Template_Public_TextPane);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new Insets(0, 8, 8, 8);
        add(sp_pan, gridBagConstraints);

        jLabel1.setText("                   ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(8, 0, 8, 8);
        add(jLabel1, gridBagConstraints);
    }// </editor-fold>
    // End of variables declaration

    private void set_Template(ItemCls item) {
        sel_Template = null;

        if (item == null || !add_Tamplate.isSelected())
            return;
        sel_Template = item; //(TemplateCls) jComboBox_Template.getSelectedItem();
        String ww = sel_Template.viewDescription();

        int ee = params_Template_Model.getRowCount() - 1;
        int ccc;
        for (ccc = params_Template_Model.getRowCount() - 1; ccc >= 0; ccc--) {
            params_Template_Model.removeRow(ccc);

        }
        jTextPane_Message_Public.pars.clear();
        jTextPane_Message_Public.setText(ww);
        HashMap<String, String> ss = jTextPane_Message_Public.get_Params();
        Set<String> sk = ss.keySet();

        for (String s : sk) {
            ss.get(s);
            params_Template_Model.addRow(new Object[]{s, ss.get(s)});
        }
    }

    class Params_Template_Model extends DefaultTableModel {
        private static final long serialVersionUID = 1L;

        public Params_Template_Model() {
            super(new Object[]{Lang.T("Name"), Lang.T("=")}, 0);
        }

        public int getColumnCount() {
            return 2;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == 1) return true;
            return new Boolean(null);
        }

        public Class<? extends Object> getColumnClass(int c) {     // set column type
            Object o = getValueAt(0, c);
            return o == null ? Null.class : o.getClass();
        }

        public Object getValueAt(int row, int col) {
            if (this.getRowCount() < row ||
                    this.getRowCount() == 0 ||
                    col < 0 || row < 0)
                return null;
            return super.getValueAt(row, col);
        }
    }
}
