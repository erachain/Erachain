package gui.library;

import core.item.templates.TemplateCls;
import gui.MainFrame;
import gui.items.templates.ComboBoxModelItemsTemplates;
import lang.Lang;

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

public class M_Fill_Template_Panel extends javax.swing.JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public JComboBox<TemplateCls> jComboBox_Template;
    public TemplateCls sel_Template = null;
    public JCheckBox add_Tamplate;
    // Variables declaration - do not modify
    public javax.swing.JCheckBox jCheckBox_Is_Encripted;
    public MSplitPane sp_pan;
    Params_Template_Model params_Template_Model;
    private ComboBoxModelItemsTemplates comboBoxModelTemplates;
    private javax.swing.JCheckBox jCheckBox_Is_Text;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel_Template1;
    private javax.swing.JScrollPane jScrollPane_Message_Public_TextPane;
    private javax.swing.JScrollPane jScrollPane_Params_Template_Public_TextPane;
    private MTable jTable_Params_Message_Public;
    private MImprintEDIT_Pane jTextPane_Message_Public;
    public M_Fill_Template_Panel() {
        jTextPane_Message_Public = new MImprintEDIT_Pane();
        comboBoxModelTemplates = new ComboBoxModelItemsTemplates();
        jTextPane_Message_Public.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent arg0) {
                // TODO Auto-generated method stub
                String str = null;
                if (arg0.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
                if (arg0.getDescription().toString().indexOf("!$@!") != 0) {
                    //		System.out.print(arg0.getDescription());
                    //		M_Template_Param_TextPane_Dialog d = new M_Template_Param_TextPane_Dialog(jTextPane_Message_Public.pars.get("{{"+ arg0.getDescription()+"}}"), getMousePosition());
                    //			str =d.tp.getText();
                } else {
                    str = JOptionPane.showInputDialog(MainFrame.getInstance(), Lang.getInstance().translate("Insert") + " " + arg0.getDescription().replace("!$@!", ""), jTextPane_Message_Public.pars.get("{{" + arg0.getDescription().replace("!$@!", "") + "}}"));

                }
                if (str == null || str.equals("")) return;
                jTextPane_Message_Public.pars.replace("{{" + arg0.getDescription().replace("!$@!", "") + "}}", str);
                jTextPane_Message_Public.init_view(jTextPane_Message_Public.text, jTextPane_Message_Public.get_Params());
                for (int i1 = 0; i1 < params_Template_Model.getRowCount(); i1++) {
                    if (arg0.getDescription().replace("!$@!", "").equals(params_Template_Model.getValueAt(i1, 0)))
                        params_Template_Model.setValueAt(str, i1, 1);


                }
                // System.out.print("\n"+ jTextPane_Message_Public.getText());

            }


        });


        initComponents();

        set_Template(comboBoxModelTemplates.getElementAt(0));

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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel_Template1 = new javax.swing.JLabel();
        jComboBox_Template = new JComboBox<TemplateCls>(comboBoxModelTemplates);
        jCheckBox_Is_Text = new javax.swing.JCheckBox();
        jCheckBox_Is_Encripted = new javax.swing.JCheckBox();
        sp_pan = new MSplitPane();
        jScrollPane_Message_Public_TextPane = new javax.swing.JScrollPane();
        jScrollPane_Params_Template_Public_TextPane = new javax.swing.JScrollPane();

        params_Template_Model = new Params_Template_Model();
        jTable_Params_Message_Public = new MTable(params_Template_Model);
        params_Template_Model.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent arg0) {
                // TODO Auto-generated method stub

                if (arg0.getType() != 0 && arg0.getColumn() < 0) return;
                //			System.out.print("\n row = " + arg0.getFirstRow() + "  Col="+ arg0.getColumn() + "   type =" + arg0.getType());
                String dd = params_Template_Model.getValueAt(arg0.getFirstRow(), arg0.getColumn()).toString();
                //			System.out.print("\n key:"+ params_Template_Model.getValueAt(arg0.getFirstRow(),  0) +" value:" + params_Template_Model.getValueAt(arg0.getFirstRow(),  arg0.getColumn()));

                jTextPane_Message_Public.pars.replace("{{" + params_Template_Model.getValueAt(arg0.getFirstRow(), 0) + "}}", (String) params_Template_Model.getValueAt(arg0.getFirstRow(), arg0.getColumn()));
                //			 System.out.print("\n" + get_TemplateCls().viewName() + "\n");
                //				System.out.print(get_Params());
                jTextPane_Message_Public.setText(jTextPane_Message_Public.init_String(jTextPane_Message_Public.text, false));
                arg0 = arg0;
            }
        });

        jComboBox_Template.setEnabled(false);
        jTextPane_Message_Public.setVisible(false);
        jTable_Params_Message_Public.setVisible(false);
        add_Tamplate = new JCheckBox();

        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        add_Tamplate.setText(Lang.getInstance().translate("Insert template") + "?");
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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        add(add_Tamplate, gridBagConstraints);

        jLabel_Template1.setText(Lang.getInstance().translate("Select template") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        add(jLabel_Template1, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        add(jComboBox_Template, gridBagConstraints);

        jCheckBox_Is_Text.setText("jCheckBox1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 8, 0);
        //	add(jCheckBox_Is_Text, gridBagConstraints);

        jCheckBox_Is_Encripted.setText(Lang.getInstance().translate("Encrypt message"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
//		add(jCheckBox_Is_Encripted, gridBagConstraints);

        jScrollPane_Message_Public_TextPane.setViewportView(jTextPane_Message_Public);

        sp_pan.setLeftComponent(jScrollPane_Message_Public_TextPane);

        jTable_Params_Message_Public.setMinimumSize(new Dimension(250, 100));

        jScrollPane_Params_Template_Public_TextPane.setViewportView(jTable_Params_Message_Public);

        sp_pan.setRightComponent(jScrollPane_Params_Template_Public_TextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 8, 8);
        add(sp_pan, gridBagConstraints);

        jLabel1.setText("                   ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 8);
        add(jLabel1, gridBagConstraints);
    }// </editor-fold>
    // End of variables declaration

    private void set_Template(TemplateCls item) {
        sel_Template = null;

        if (item == null || !add_Tamplate.isSelected())
            return;
        sel_Template = item; //(TemplateCls) jComboBox_Template.getSelectedItem();
        String ww = sel_Template.getDescription();

        int ee = params_Template_Model.getRowCount() - 1;
        int ccc;
        for (ccc = params_Template_Model.getRowCount() - 1; ccc >= 0; ccc--) {
            params_Template_Model.removeRow(ccc);

        }
        jTextPane_Message_Public.pars.clear();
        jTextPane_Message_Public.set_Text(ww);
        HashMap<String, String> ss = jTextPane_Message_Public.get_Params();
        Set<String> sk = ss.keySet();

        for (String s : sk) {
            ss.get(s);
            params_Template_Model.addRow(new Object[]{s, ss.get(s)});

        }


    }

    class Params_Template_Model extends DefaultTableModel {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public Params_Template_Model() {
            super(new Object[]{Lang.getInstance().translate("Name"), Lang.getInstance().translate("=")}, 0);

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


            if (this.getRowCount() < row || this.getRowCount() == 0 || col < 0 || row < 0) return null;
            return super.getValueAt(row, col);


        }


    }
}
