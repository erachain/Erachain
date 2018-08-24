package core.exdata;

import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.templates.TemplateCls;
import gui.items.link_hashes.Table_Model_Issue_Hashes;
import gui.library.*;
import lang.Lang;
import org.mapdb.Fun.Tuple3;

import utils.FileHash;
import utils.Zip_Bytes;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.validation.constraints.Null;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Саша
 */
public class ExData_Panel extends javax.swing.JPanel {

    public MSplitPane sp_pan;
    public M_Fill_Template_Panel fill_Template_Panel;
    public javax.swing.JTextField jTextField_Title_Message;
    public MTable jTable_Params_Message_Public;
    protected TemplateCls sel_Template;
    private Table_Model_Issue_Hashes hashes_Table_Model;
    private DefaultTableModel attached_Files_Model;
    private DefaultTableModel params_Template_Model;
    private ExData_Panel th;
    // Variables declaration - do not modify
    private MButton jButton_Add_Attached_Files;
    private MButton jButton_Add_From_File_Other_Hashes;
    private MButton jButton_Add_Other_Hashes;
    private MButton jButton_Remove_Attached_Files;
    private MButton jButton_Remove_Other_Hashes;
    private MButton jButton_Input_Hashes_From_File_Other_Hashes;
    private javax.swing.JLabel jLabel_Title_Message;
    private javax.swing.JPanel jPanel_Attached_Files;
    private javax.swing.JPanel jPanel_Message;
    private javax.swing.JPanel jPanel_Message_Public;
    private javax.swing.JPanel jPanel_Other_Attached_Files_Work;
    private javax.swing.JPanel jPanel_Other_Hashes;
    private javax.swing.JPanel jPanel_Title;
    private javax.swing.JScrollPane jScrollPane_Attached_Files_Table;
    private javax.swing.JScrollPane jScrollPane_Hashes_Files_Tale;
    private javax.swing.JScrollPane jScrollPane_Message_TextPane;
    private javax.swing.JScrollPane jScrollPane_Message_Public_TextPane;
    private javax.swing.JScrollPane jScrollPane_Params_Template_Public_TextPane;
    private javax.swing.JTabbedPane jTabbedPane_Message;
    private javax.swing.JTabbedPane jTabbedPane_Other;
    private MTable jTable_Attached_Files;
    private MTable jTable_Other_Hashes;
    private javax.swing.JTextPane jTextPane_Message_Private;
    private MImprintEDIT_Pane jTextPane_Message_Public;
    /**
     * Creates new form Issue_Document_Panel
     */
    public ExData_Panel() {

        jTextPane_Message_Public = new MImprintEDIT_Pane();
        jTextPane_Message_Public.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent arg0) {
                // TODO Auto-generated method stub
                EventType i = arg0.getEventType();
                if (arg0.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
                    return;
                String str = JOptionPane.showInputDialog(jTextPane_Message_Public.th,
                        Lang.getInstance().translate("Insert") + " " + arg0.getDescription(),
                        jTextPane_Message_Public.pars.get("{{" + arg0.getDescription() + "}}"));
                if (str == null || str.equals(""))
                    return;
                jTextPane_Message_Public.pars.replace("{{" + arg0.getDescription() + "}}", str);
                jTextPane_Message_Public
                        .setText(jTextPane_Message_Public.init_String(jTextPane_Message_Public.text, false));
                for (int i1 = 0; i1 < params_Template_Model.getRowCount(); i1++) {
                    if (arg0.getDescription().equals(params_Template_Model.getValueAt(i1, 0)))
                        params_Template_Model.setValueAt(str, i1, 1);
                }
            }
        });
        th = this;
        initComponents();

        // jLabel_Template.setText(Lang.getInstance().translate("Select
        // Template") + ":");
        jLabel_Title_Message.setText(Lang.getInstance().translate("Title") + ":");
        jTextField_Title_Message.setText("");

        this.jButton_Remove_Other_Hashes.setText(Lang.getInstance().translate("Delete"));
        this.jButton_Remove_Other_Hashes.addActionListener(new ActionListener() {
            // delete row
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hashes_Table_Model.getRowCount() > 0) {
                    int selRow = jTable_Other_Hashes.getSelectedRow();
                    if (selRow != -1 && hashes_Table_Model.getRowCount() >= selRow) {
                        ((DefaultTableModel) hashes_Table_Model).removeRow(selRow);
                        hashes_Table_Model.fireTableDataChanged();
                    }
                }
            }
        });

        this.jButton_Add_From_File_Other_Hashes.setText(Lang.getInstance().translate("Create Hash"));
        // jButton3_jToolBar_RightPanel.setFocusable(false);
        jButton_Add_From_File_Other_Hashes.addActionListener(new ActionListener() {
            // create Hashs
            @Override
            public void actionPerformed(ActionEvent e) {
                Hashs_from_Files(false);

            }
        });

        this.jButton_Add_Other_Hashes.setText(Lang.getInstance().translate("Add"));
        jButton_Add_Other_Hashes.addActionListener(new ActionListener() {
            // create Hashs
            @Override
            public void actionPerformed(ActionEvent e) {
                String str = JOptionPane.showInputDialog(null, Lang.getInstance().translate("Insert Hash"),
                        Lang.getInstance().translate("Add"), JOptionPane.INFORMATION_MESSAGE);
                if (str == null || str == "" || str.equals(""))
                    return;
                hashes_Table_Model.addRow(new Object[]{str, "Add"});
                hashes_Table_Model.fireTableDataChanged();
            }
        });
        this.jButton_Input_Hashes_From_File_Other_Hashes.setText(Lang.getInstance().translate("Import Hashs"));
        jButton_Input_Hashes_From_File_Other_Hashes.addActionListener(new ActionListener() {
            // create Hashs
            @Override
            public void actionPerformed(ActionEvent e) {
                Hashs_from_Files(true);
            }
        });

        this.jButton_Add_Attached_Files.setText(Lang.getInstance().translate("Add"));
        jButton_Add_Attached_Files.addActionListener(new ActionListener() {
            // create Hashs
            @Override
            public void actionPerformed(ActionEvent e) {
                attache_Files();
            }
        });

        this.jButton_Remove_Attached_Files.setText(Lang.getInstance().translate("Delete"));
        this.jButton_Remove_Attached_Files.addActionListener(new ActionListener() {
            // delete row
            @Override
            public void actionPerformed(ActionEvent e) {

                if (attached_Files_Model.getRowCount() > 0) {
                    int selRow = jTable_Attached_Files.getSelectedRow();
                    if (selRow != -1 && attached_Files_Model.getRowCount() >= selRow) {

                        // Object a = attached_Files_Model.getValueAt(selRow,
                        // 1);

                        ((DefaultTableModel) attached_Files_Model).removeRow(selRow);
                        attached_Files_Model.fireTableDataChanged();
                    }
                }
            }
        });

        TableColumnModel at_F_Col_M = jTable_Attached_Files.getColumnModel();

        TableColumn col = at_F_Col_M.getColumn(2);
        col.setMinWidth(50);
        col.setPreferredWidth(60);
        col.setMaxWidth(100);
        col = at_F_Col_M.getColumn(3);
        col.setMinWidth(150);
        col.setPreferredWidth(160);
        col.setMaxWidth(200);

        jTable_Attached_Files.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("static-access")
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = jTable_Attached_Files.rowAtPoint(p);
                jTable_Attached_Files.setRowSelectionInterval(row, row);

                if (e.getClickCount() == 2) {
                    // row = assetsTable.convertRowIndexToModel(row);
                    // AssetCls asset = tableModelItemAssets.getAsset(row);
                    // new AssetPairSelect(asset.getKey(), "","");
                    // new ExchangeFrame(asset,null, "", "");
                    // new AssetFrame(asset);
                }
                if (e.getClickCount() == 1 & e.getButton() == e.BUTTON1) {

                    if (jTable_Attached_Files.getSelectedColumn() == 2) {
                        row = jTable_Attached_Files.convertRowIndexToModel(row);
                        attached_Files_Model.setValueAt(new Boolean(!(boolean) attached_Files_Model.getValueAt(row, 2)),
                                row, 2);
                        if (new Boolean((boolean) attached_Files_Model.getValueAt(row, 2))) {
                            // Compressor_ZIP zip = new Compressor_ZIP();
                            // attached_Files_Model.setValueAt(zip.compress((byte[])
                            // attached_Files_Model.getValueAt(row, 4)) , row,
                            // 5);

                            try {
                                attached_Files_Model.setValueAt(
                                        Zip_Bytes.compress((byte[]) attached_Files_Model.getValueAt(row, 4)), row, 5);

                            } catch (Exception e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                            // read & un Zip info

                            try {

                            } catch (Exception e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }

                        } else {
                            attached_Files_Model.setValueAt(attached_Files_Model.getValueAt(row, 4), row, 5);

                        }

                        attached_Files_Model.setValueAt(((byte[]) attached_Files_Model.getValueAt(row, 5)).length, row,
                                3);

                    }

                }
            }
        });

        // hand cursor for Favorite column
        jTable_Attached_Files.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {

                if (jTable_Attached_Files.columnAtPoint(e.getPoint()) == 2) {

                    jTable_Attached_Files.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    jTable_Attached_Files.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }

            public void mouseDragged(MouseEvent e) {
            }
        });

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */

    @SuppressWarnings("rawtypes")
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jTabbedPane_Message = new javax.swing.JTabbedPane();
        jPanel_Message_Public = new javax.swing.JPanel();
        jScrollPane_Message_Public_TextPane = new javax.swing.JScrollPane();
        jScrollPane_Params_Template_Public_TextPane = new javax.swing.JScrollPane();
        jPanel_Message = new javax.swing.JPanel();
        jScrollPane_Message_TextPane = new javax.swing.JScrollPane();
        jTextPane_Message_Private = new javax.swing.JTextPane();
        jTabbedPane_Other = new javax.swing.JTabbedPane();
        jPanel_Attached_Files = new javax.swing.JPanel();
        jScrollPane_Attached_Files_Table = new javax.swing.JScrollPane();
        jPanel_Other_Attached_Files_Work = new javax.swing.JPanel();
        jButton_Remove_Attached_Files = new MButton();
        jButton_Add_Attached_Files = new MButton();
        jPanel_Other_Hashes = new javax.swing.JPanel();
        jScrollPane_Hashes_Files_Tale = new javax.swing.JScrollPane();
        jButton_Add_From_File_Other_Hashes = new MButton();
        jButton_Add_Other_Hashes = new MButton();
        jButton_Remove_Other_Hashes = new MButton();
        jPanel_Title = new javax.swing.JPanel();

        jLabel_Title_Message = new javax.swing.JLabel();
        jTextField_Title_Message = new javax.swing.JTextField();
        jButton_Input_Hashes_From_File_Other_Hashes = new MButton();

        params_Template_Model = new Params_Template_Model();
        jTable_Params_Message_Public = new MTable(params_Template_Model);
        params_Template_Model.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent arg0) {
                // TODO Auto-generated method stub

                if (arg0.getType() != 0 && arg0.getColumn() < 0)
                    return;
                jTextPane_Message_Public.pars.replace(
                        "{{" + params_Template_Model.getValueAt(arg0.getFirstRow(), 0) + "}}",
                        (String) params_Template_Model.getValueAt(arg0.getFirstRow(), arg0.getColumn()));
                // System.out.print("\n");
                // System.out.print(jTextPane_Message_Public.pars);
                jTextPane_Message_Public
                        .setText(jTextPane_Message_Public.init_String(jTextPane_Message_Public.text, false));
            }
        });

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[]{0, 0, 0};
        layout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
        setLayout(layout);

        jPanel_Message_Public.setLayout(new java.awt.GridBagLayout());

        jScrollPane_Message_Public_TextPane.setViewportView(jTextPane_Message_Public);
        sp_pan = new MSplitPane();
        sp_pan.set_CloseOnOneTouch(MSplitPane.ONE_TOUCH_CLOSE_RIGHT_BOTTOM);
        sp_pan.setLeftComponent(jScrollPane_Message_Public_TextPane);
        jScrollPane_Params_Template_Public_TextPane.setViewportView(jTable_Params_Message_Public);
        jScrollPane_Params_Template_Public_TextPane.setMinimumSize(new Dimension(0, 0));
        jTable_Params_Message_Public.setMinimumSize(new Dimension(0, 0));
        sp_pan.setRightComponent(jScrollPane_Params_Template_Public_TextPane);
        sp_pan.setDividerLocation(400);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanel_Message_Public.add(sp_pan, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        // jPanel_Message_Public.add(jCheckBox_Message_Public,
        // gridBagConstraints);

        // jTabbedPane_Message.addTab(Lang.getInstance().translate("Public
        // Part"), jPanel_Message_Public);
        fill_Template_Panel = new M_Fill_Template_Panel();
        jTabbedPane_Message.addTab(Lang.getInstance().translate("Template"), fill_Template_Panel);

        jPanel_Message.setLayout(new java.awt.GridBagLayout());

        jScrollPane_Message_TextPane.setViewportView(jTextPane_Message_Private);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanel_Message.add(jScrollPane_Message_TextPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        // jPanel_Message.add(jCheckBox_Message_Private, gridBagConstraints);

        jTabbedPane_Message.addTab(Lang.getInstance().translate("Message"), jPanel_Message);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(16, 8, 0, 8);
        add(jTabbedPane_Message, gridBagConstraints);

        jPanel_Attached_Files.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel_Attached_Files.setPreferredSize(new java.awt.Dimension(0, 64));
        jPanel_Attached_Files.setLayout(new java.awt.GridBagLayout());

        attached_Files_Model = new Attache_Files_Model(); // new
        // javax.swing.table.DefaultTableModel(new
        // Object [][][][][]
        // { {null,null,
        // null,
        // null,null}}, new
        // String []
        // {Lang.getInstance().translate("Path"),
        // "Data","ZIP?",
        // "Size/Zip Size",
        // "www"});
        // attached_Files_Model.removeRow(0);
        jTable_Attached_Files = new MTable(attached_Files_Model);
        jTable_Attached_Files.removeColumn(jTable_Attached_Files.getColumnModel().getColumn(5));
        jTable_Attached_Files.removeColumn(jTable_Attached_Files.getColumnModel().getColumn(4));

        jTable_Attached_Files.setAlignmentX(0.0F);
        jTable_Attached_Files.setAlignmentY(0.0F);
        jScrollPane_Attached_Files_Table.setViewportView(jTable_Attached_Files);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_Attached_Files.add(jScrollPane_Attached_Files_Table, gridBagConstraints);

        jPanel_Other_Attached_Files_Work.setLayout(new java.awt.GridBagLayout());

        jButton_Remove_Attached_Files.setText(Lang.getInstance().translate("Remove File"));
        jButton_Remove_Attached_Files.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 41, 8, 8);
        jPanel_Other_Attached_Files_Work.add(jButton_Remove_Attached_Files, gridBagConstraints);

        jButton_Add_Attached_Files.setText("Add File");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Other_Attached_Files_Work.add(jButton_Add_Attached_Files, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel_Attached_Files.add(jPanel_Other_Attached_Files_Work, gridBagConstraints);

        // if (BlockChain.DEVELOP_USE == true)
        jTabbedPane_Message.addTab(Lang.getInstance().translate("Attached Files"), jPanel_Attached_Files);

        jPanel_Other_Hashes.setLayout(new java.awt.GridBagLayout());

        jScrollPane_Hashes_Files_Tale.setOpaque(false);
        jScrollPane_Hashes_Files_Tale.setPreferredSize(new java.awt.Dimension(0, 0));

        hashes_Table_Model = new Table_Model_Issue_Hashes(0);
        jTable_Other_Hashes = new MTable(hashes_Table_Model);
        jScrollPane_Hashes_Files_Tale.setViewportView(jTable_Other_Hashes);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanel_Other_Hashes.add(jScrollPane_Hashes_Files_Tale, gridBagConstraints);

        jButton_Input_Hashes_From_File_Other_Hashes.setText("Import Hashs");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Other_Hashes.add(jButton_Input_Hashes_From_File_Other_Hashes, gridBagConstraints);

        jButton_Add_From_File_Other_Hashes.setText("Add From File");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Other_Hashes.add(jButton_Add_From_File_Other_Hashes, gridBagConstraints);

        jButton_Add_Other_Hashes.setText("Add");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Other_Hashes.add(jButton_Add_Other_Hashes, gridBagConstraints);

        jButton_Remove_Other_Hashes.setText("Remove");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Other_Hashes.add(jButton_Remove_Other_Hashes, gridBagConstraints);

        jTabbedPane_Message.addTab(Lang.getInstance().translate("Hashes"), jPanel_Other_Hashes);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(16, 8, 0, 8);
        // add(jTabbedPane_Other, gridBagConstraints);

        jPanel_Title.setLayout(new java.awt.GridBagLayout());

        // jLabel_Template.setText("Template: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        // jPanel_Title.add(jLabel_Template, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        // jPanel_Title.add(jComboBox_Template, gridBagConstraints);

        jLabel_Title_Message.setText("Title:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jPanel_Title.add(jLabel_Title_Message, gridBagConstraints);

        jTextField_Title_Message.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        jPanel_Title.add(jTextField_Title_Message, gridBagConstraints);

        // jButton_View.setText("View ");
        // jButton_View.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        // jPanel_Title.add(jButton_View, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        add(jPanel_Title, gridBagConstraints);

    }// </editor-fold>

    protected void Hashs_from_Files(boolean importing) {
        // TODO Auto-generated method stub
        // true - если импорт из файла
        // false - если создаем хэш для файлов

        // открыть диалог для файла
        // JFileChooser chooser = new JFileChooser();
        // руссификация диалога выбора файла
        // new All_Options().setUpdateUI(chooser);
        My_JFileChooser chooser = new My_JFileChooser();
        chooser.setDialogTitle(Lang.getInstance().translate("Select File"));

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        if (importing)
            chooser.setMultiSelectionEnabled(false);

        // FileNameExtensionFilter filter = new FileNameExtensionFilter(
        // "Image", "png", "jpg");
        // chooser.setFileFilter(filter);

        int returnVal = chooser.showOpenDialog(getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // если есть пустые строки удаляем их
            int i;
            for (i = 0; i <= hashes_Table_Model.getRowCount() - 1; i++) {
                if (hashes_Table_Model.getValueAt(i, 0).toString().equals("")) {

                    hashes_Table_Model.removeRow(i);

                }
            }

            if (importing) {
                // IMPORT FROM FILE
                File patch = chooser.getSelectedFile();
                String file_name = patch.getPath();
                String hashesStr = "";
                try {
                    hashesStr = new String(Files.readAllBytes(Paths.get(file_name)));
                } catch (IOException e) {
                    e.printStackTrace();
                    hashes_Table_Model.addRow(
                            new Object[]{"", Lang.getInstance().translate("error reading") + " - " + file_name});
                }

                if (hashesStr.length() > 0) {
                    String[] hashes = hashesStr.split("\\s*(\\s|,|!|;|:|\n|\\.)\\s*");
                    for (String hashB58 : hashes) {
                        if (hashB58 != null && !hashB58.equals(new String("")))
                            hashes_Table_Model.addRow(new Object[]{hashB58,
                                    Lang.getInstance().translate("imported from") + " " + file_name});
                    }

                }

            } else {

                // make HASHES from files
                File[] patchs = chooser.getSelectedFiles();

                for (File patch : patchs) {

                  /// HASHING
                    FileHash gf = new FileHash(patch);
                    String hashes = gf.getHash();
                    hashes_Table_Model
                            .addRow(new Object[]{hashes, Lang.getInstance().translate("from file ") + patch.getPath()});
                    gf = null;
                }

            }
            // hashes_Table_Model.addRow(new Object[] { "",""});
            hashes_Table_Model.fireTableDataChanged();
            jTable_Other_Hashes.setRowSelectionInterval(hashes_Table_Model.getRowCount() - 1,
                    hashes_Table_Model.getRowCount() - 1);

        }

    }

    @SuppressWarnings("resource")
    protected void attache_Files() {
        // TODO Auto-generated method stub
        // true - если импорт из файла
        // false - если создаем хэш для файлов

        // открыть диалог для файла
        // JFileChooser chooser = new JFileChooser();
        // руссификация диалога выбора файла
        // new All_Options().setUpdateUI(chooser);
        My_JFileChooser chooser = new My_JFileChooser();
        chooser.setDialogTitle(Lang.getInstance().translate("Select File"));

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);

        int returnVal = chooser.showOpenDialog(getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            // make HASHES from files
            File[] patchs = chooser.getSelectedFiles();

            for (File patch : patchs) {

                File file = new File(patch.getPath());
                byte[] fileInArray = new byte[(int) file.length()];
                FileInputStream f = null;
                try {
                    f = new FileInputStream(patch.getPath());
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                }
                try {
                    f.read(fileInArray);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                }
                try {
                    f.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    continue;
                }

                attached_Files_Model.addRow(new Object[]{patch.getName().toString(),
                        patch.getPath().substring(0, patch.getPath().length() - patch.getName().length()),
                        new Boolean(false), new Integer(fileInArray.length), fileInArray, fileInArray});

            }

            // hashes_Table_Model.addRow(new Object[] { "",""});
            attached_Files_Model.fireTableDataChanged();
            jTable_Attached_Files.setRowSelectionInterval(attached_Files_Model.getRowCount() - 1,
                    attached_Files_Model.getRowCount() - 1);

        }

    }

    public byte[] getExData() throws Exception {

        // hashes Charset.forName("UTF-8")
        HashMap<String, String> hashes_Map = new HashMap<String, String>();
        int hR = hashes_Table_Model.getRowCount();
        for (int i = 0; i < hR; i++) {
            hashes_Map.put((String) hashes_Table_Model.getValueAt(i, 0), (String) hashes_Table_Model.getValueAt(i, 1));
        }
        // files
        Set<Tuple3<String, Boolean, byte[]>> files_1 = new HashSet<Tuple3<String, Boolean, byte[]>>();
        int oF = attached_Files_Model.getRowCount();
        for (int i = 0; i < oF; i++) {
            files_1.add(new Tuple3<String, Boolean, byte[]>((String) attached_Files_Model.getValueAt(i, 0),
                    (Boolean) attached_Files_Model.getValueAt(i, 2), (byte[]) attached_Files_Model.getValueAt(i, 5)));
        }

        return ExData.toByte_V2(jTextField_Title_Message.getText(), fill_Template_Panel.sel_Template,
                this.fill_Template_Panel.get_Params(), hashes_Map, jTextPane_Message_Private.getText(), files_1);

    }
    // End of variables declaration
}

class Attache_Files_Model extends DefaultTableModel {

    public Attache_Files_Model() {
        super(new Object[]{Lang.getInstance().translate("Name"), Lang.getInstance().translate("Path"), "ZIP?",
                Lang.getInstance().translate("Size/Zip Size")}, 0);

    }

    public int getColumnCount() {
        return 6;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return new Boolean(null);
    }

    public Class<? extends Object> getColumnClass(int c) { // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    public Object getValueAt(int row, int col) {

        if (this.getRowCount() < row || this.getRowCount() == 0 || col < 0 || row < 0)
            return null;
        return super.getValueAt(row, col);

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
        if (column == 1)
            return true;
        return new Boolean(null);
    }

    public Class<? extends Object> getColumnClass(int c) { // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    public Object getValueAt(int row, int col) {

        if (this.getRowCount() < row || this.getRowCount() == 0 || col < 0 || row < 0)
            return null;
        return super.getValueAt(row, col);

    }

}
