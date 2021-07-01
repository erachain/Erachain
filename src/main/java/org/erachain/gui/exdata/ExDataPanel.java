package org.erachain.gui.exdata;

import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.exdata.ExData;
import org.erachain.core.exdata.exActions.ExAirDrop;
import org.erachain.core.exdata.exActions.ExPays;
import org.erachain.core.exdata.exLink.*;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.exdata.authors.AuthorsPanel;
import org.erachain.gui.exdata.exActions.ExAccrualsPanel;
import org.erachain.gui.exdata.exActions.ExAirDropPanel;
import org.erachain.gui.exdata.sources.SourcesPanel;
import org.erachain.gui.items.link_hashes.TableModelIssueHashes;
import org.erachain.gui.items.statement.IssueDocumentPanel;
import org.erachain.gui.library.*;
import org.erachain.lang.Lang;
import org.erachain.utils.FileHash;
import org.erachain.utils.ZipBytes;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
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
public class ExDataPanel extends JPanel {

    IssueDocumentPanel parentPanel;

    public MultipleRecipientsPanel multipleRecipientsPanel;
    public MFillTemplatePanel fill_Template_Panel;
    public JTextField jTextField_Title_Message;
    public MTable jTable_Params_Message_Public;
    protected TemplateCls sel_Template;
    private TableModelIssueHashes hashes_Table_Model;
    private AttacheFilesModel attached_Files_Model;
    private MButton jButton_Add_Attached_Files;
    private MButton jButton_Add_From_File_Other_Hashes;
    private MButton jButton_Add_Other_Hashes;
    private MButton jButton_Remove_Attached_Files;
    private MButton jButton_Remove_Other_Hashes;
    private MButton jButton_Input_Hashes_From_File_Other_Hashes;
    private JLabel jLabel_Title_Message;
    private JPanel jPanel_Attached_Files;
    private AuthorsPanel authorsPanel;
    private SourcesPanel sourcesPanel;
    private JPanel jPanel_Message;
    private JPanel jPanel_Other_Attached_Files_Work;
    private JPanel jPanel_Other_Hashes;
    private JPanel jPanel_Title;
    private JScrollPane jScrollPane_Attached_Files_Table;
    private JScrollPane jScrollPane_Hashes_Files_Tale;
    private JScrollPane jScrollPane_Message_TextPane;
    private JScrollPane jScrollPane_Message_Public_TextPane;
    public JTabbedPane jTabbedPane_Type;
    private JTabbedPane jTabbedPane_Other;
    private MTable jTable_Attached_Files;
    private MTable jTable_Other_Hashes;
    private JTextPane jTextPane_Message;
    public JCheckBox checkBoxMakeHashAndCheckUniqueText;
    public JCheckBox checkBoxMakeHashAndCheckUniqueHashes;
    public JCheckBox checkBoxMakeHashAndCheckUniqueAttachedFiles;
    public DocTypeAppendixPanel docTypeAppendixPanel;
    public ExAccrualsPanel exAccrualsPanel;
    public ExAirDropPanel exAirDropPanel;

    /**
     * Creates new form IssueDocumentPanel
     */
    public ExDataPanel(IssueDocumentPanel parentPanel) {

        this.parentPanel = parentPanel;

        initComponents();

        jLabel_Title_Message.setText(Lang.T("Title") + ":");
        jTextField_Title_Message.setText("");

        this.jButton_Remove_Other_Hashes.setText(Lang.T("Delete"));
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

        this.jButton_Add_From_File_Other_Hashes.setText(Lang.T("Create Hash"));
        // jButton3_jToolBar_RightPanel.setFocusable(false);
        jButton_Add_From_File_Other_Hashes.addActionListener(new ActionListener() {
            // create Hashs
            @Override
            public void actionPerformed(ActionEvent e) {
                Hashs_from_Files(false);

            }
        });

        this.jButton_Add_Other_Hashes.setText(Lang.T("Add"));
        jButton_Add_Other_Hashes.addActionListener(new ActionListener() {
            // create Hashs
            @Override
            public void actionPerformed(ActionEvent e) {
                String str = JOptionPane.showInputDialog(null, Lang.T("Insert Hash"),
                        Lang.T("Add"), JOptionPane.INFORMATION_MESSAGE);
                if (str == null || str == "" || str.isEmpty())
                    return;
                hashes_Table_Model.addRow(new Object[]{str, "Add"});
                hashes_Table_Model.fireTableDataChanged();
            }
        });
        this.jButton_Input_Hashes_From_File_Other_Hashes.setText(Lang.T("Import Hashs"));
        jButton_Input_Hashes_From_File_Other_Hashes.addActionListener(new ActionListener() {
            // create Hashs
            @Override
            public void actionPerformed(ActionEvent e) {
                Hashs_from_Files(true);
            }
        });

        this.jButton_Add_Attached_Files.setText(Lang.T("Add"));
        jButton_Add_Attached_Files.addActionListener(new ActionListener() {
            // create Hashs
            @Override
            public void actionPerformed(ActionEvent e) {
                attache_Files();
            }
        });

        this.jButton_Remove_Attached_Files.setText(Lang.T("Delete"));
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
                            // CompressorZIP zip = new CompressorZIP();
                            // attached_Files_Model.setValueAt(zip.compress((byte[])
                            // attached_Files_Model.getValueAt(row, 4)) , row,
                            // 5);

                            try {
                                attached_Files_Model.setValueAt(
                                        ZipBytes.compress((byte[]) attached_Files_Model.getValueAt(row, 4)), row, 5);

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



    @SuppressWarnings("rawtypes")
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        multipleRecipientsPanel = new MultipleRecipientsPanel();
        jTabbedPane_Type = new JTabbedPane();
        jScrollPane_Message_Public_TextPane = new JScrollPane();
        jPanel_Message = new JPanel();
        jScrollPane_Message_TextPane = new JScrollPane();
        jTextPane_Message = new JTextPane();
        jTabbedPane_Other = new JTabbedPane();
        jPanel_Attached_Files = new JPanel();
        jScrollPane_Attached_Files_Table = new JScrollPane();
        jPanel_Other_Attached_Files_Work = new JPanel();
        jButton_Remove_Attached_Files = new MButton();
        jButton_Add_Attached_Files = new MButton();
        jPanel_Other_Hashes = new JPanel();
        jScrollPane_Hashes_Files_Tale = new JScrollPane();
        jButton_Add_From_File_Other_Hashes = new MButton();
        jButton_Add_Other_Hashes = new MButton();
        jButton_Remove_Other_Hashes = new MButton();
        jPanel_Title = new JPanel();

        authorsPanel = new AuthorsPanel();
        sourcesPanel = new SourcesPanel();
        jLabel_Title_Message = new JLabel();
        jTextField_Title_Message = new JTextField();
        jButton_Input_Hashes_From_File_Other_Hashes = new MButton();
        docTypeAppendixPanel = new DocTypeAppendixPanel(this);
        exAccrualsPanel = new ExAccrualsPanel(this);
        exAirDropPanel = new ExAirDropPanel(this);

        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[]{0, 0, 0};
        layout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
        setLayout(layout);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;

        jTabbedPane_Type.addTab(Lang.T("Type"), docTypeAppendixPanel);

        //////////////// ACCRUALS
        JScrollPane multiPayScrollBar = new JScrollPane();
        multiPayScrollBar.setViewportView(exAccrualsPanel);
        jTabbedPane_Type.addTab(Lang.T("Accruals"), multiPayScrollBar);

        jTabbedPane_Type.addTab(Lang.T("Recipients"), multipleRecipientsPanel);
        jTabbedPane_Type.addTab(Lang.T(authorsPanel.getName()), authorsPanel);
        jTabbedPane_Type.addTab(Lang.T(sourcesPanel.getName()), sourcesPanel);

        fill_Template_Panel = new MFillTemplatePanel();
        jTabbedPane_Type.addTab(Lang.T("Template"), fill_Template_Panel);

        jPanel_Message.setLayout(new GridBagLayout());

        ////////////
        jScrollPane_Message_TextPane.setViewportView(jTextPane_Message);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanel_Message.add(jScrollPane_Message_TextPane, gridBagConstraints);

        checkBoxMakeHashAndCheckUniqueText = new JCheckBox(Lang.T("Make hash and check unique"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_Message.add(checkBoxMakeHashAndCheckUniqueText, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        // jPanel_Message.add(jCheckBox_Message_Private, gridBagConstraints);

        jTabbedPane_Type.addTab(Lang.T("Text"), jPanel_Message);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new Insets(16, 8, 0, 8);
        add(jTabbedPane_Type, gridBagConstraints);

        jPanel_Attached_Files.setMinimumSize(new Dimension(0, 0));
        jPanel_Attached_Files.setPreferredSize(new Dimension(0, 64));
        jPanel_Attached_Files.setLayout(new GridBagLayout());

        // Make hash and check unique
        checkBoxMakeHashAndCheckUniqueAttachedFiles = new JCheckBox(Lang.T("Make hash and check unique"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_Attached_Files.add(checkBoxMakeHashAndCheckUniqueAttachedFiles, gridBagConstraints);


        attached_Files_Model = new AttacheFilesModel(); // new
        // javax.swing.table.DefaultTableModel(new
        // Object [][][][][]
        // { {null,null,
        // null,
        // null,null}}, new
        // String []
        // {Lang.T("Path"),
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

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_Attached_Files.add(jScrollPane_Attached_Files_Table, gridBagConstraints);

        jPanel_Other_Attached_Files_Work.setLayout(new GridBagLayout());

        jButton_Remove_Attached_Files.setText(Lang.T("Remove File"));
        jButton_Remove_Attached_Files.setToolTipText("");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(8, 41, 8, 8);
        jPanel_Other_Attached_Files_Work.add(jButton_Remove_Attached_Files, gridBagConstraints);

        jButton_Add_Attached_Files.setText("Add File");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        jPanel_Other_Attached_Files_Work.add(jButton_Add_Attached_Files, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        jPanel_Attached_Files.add(jPanel_Other_Attached_Files_Work, gridBagConstraints);

        jPanel_Other_Hashes.setLayout(new GridBagLayout());


        // Make hash and check unique
        checkBoxMakeHashAndCheckUniqueHashes = new JCheckBox(Lang.T("Check unique"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_Other_Hashes.add(checkBoxMakeHashAndCheckUniqueHashes, gridBagConstraints);


        jScrollPane_Hashes_Files_Tale.setOpaque(false);
        jScrollPane_Hashes_Files_Tale.setPreferredSize(new Dimension(0, 0));

        hashes_Table_Model = new TableModelIssueHashes(0);
        jTable_Other_Hashes = new MTable(hashes_Table_Model);
        jScrollPane_Hashes_Files_Tale.setViewportView(jTable_Other_Hashes);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanel_Other_Hashes.add(jScrollPane_Hashes_Files_Tale, gridBagConstraints);

        jButton_Input_Hashes_From_File_Other_Hashes.setText("Import Hashs");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        jPanel_Other_Hashes.add(jButton_Input_Hashes_From_File_Other_Hashes, gridBagConstraints);

        jButton_Add_From_File_Other_Hashes.setText("Add From File");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        jPanel_Other_Hashes.add(jButton_Add_From_File_Other_Hashes, gridBagConstraints);

        jButton_Add_Other_Hashes.setText("Add");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        jPanel_Other_Hashes.add(jButton_Add_Other_Hashes, gridBagConstraints);

        jButton_Remove_Other_Hashes.setText("Remove");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        jPanel_Other_Hashes.add(jButton_Remove_Other_Hashes, gridBagConstraints);

        jTabbedPane_Type.addTab(Lang.T("Hashes"), jPanel_Other_Hashes);

        jTabbedPane_Type.addTab(Lang.T("Attached Files"), jPanel_Attached_Files);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new Insets(16, 8, 0, 8);
        // add(jTabbedPane_Other, gridBagConstraints);

        jPanel_Title.setLayout(new GridBagLayout());

        // jLabel_Template.setText("Template: ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(8, 8, 8, 0);
        // jPanel_Title.add(jLabel_Template, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        // jPanel_Title.add(jComboBox_Template, gridBagConstraints);

        jLabel_Title_Message.setText("Title:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(8, 8, 8, 0);
        jPanel_Title.add(jLabel_Title_Message, gridBagConstraints);

        jTextField_Title_Message.setText("jTextField1");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        jPanel_Title.add(jTextField_Title_Message, gridBagConstraints);

        // jButton_View.setText("View ");
        // jButton_View.setToolTipText("");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        // jPanel_Title.add(jButton_View, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(8, 8, 0, 8);
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
        FileChooser chooser = new FileChooser();
        chooser.setDialogTitle(Lang.T("Select File"));

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
                            new Object[]{"", Lang.T("error reading") + " - " + file_name});
                }

                if (hashesStr.length() > 0) {
                    String[] hashes = hashesStr.split("\\s*(\\s|,|!|;|:|\n|\\.)\\s*");
                    for (String hashB58 : hashes) {
                        if (hashB58 != null && !hashB58.equals(new String("")))
                            hashes_Table_Model.addRow(new Object[]{hashB58,
                                    Lang.T("imported from") + " " + file_name});
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
                            .addRow(new Object[]{hashes, Lang.T("from file ") + patch.getPath()});
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
        FileChooser chooser = new FileChooser();
        chooser.setDialogTitle(Lang.T("Select File"));

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

    public void updateRecipients() {
        if (docTypeAppendixPanel.parentTx == null) {
            return;
        }

        int typeLink = docTypeAppendixPanel.getSelectedItem();
        if (typeLink == ExData.LINK_COMMENT_TYPE_FOR_VIEW) {
            multipleRecipientsPanel.recipientsTableModel.clearRecipients();
        } else if (typeLink == ExData.LINK_REPLY_COMMENT_TYPE
                || typeLink == ExData.LINK_APPENDIX_TYPE) {

            Account sender = (Account) parentPanel.jComboBox_Account_Work.getSelectedItem();

            HashSet<Account> accountsTx = docTypeAppendixPanel.parentTx.getInvolvedAccounts();
            accountsTx.remove(sender);
            multipleRecipientsPanel.recipientsTableModel.setRecipients(accountsTx.toArray(new Account[]{}));
        }

    }

    public Fun.Tuple2<byte[], String> makeExData(PrivateKeyAccount creator, boolean isEncrypted) {

        Account[] recipients = multipleRecipientsPanel.recipientsTableModel.getRecipients();
        boolean signCanOnlyRecipients = multipleRecipientsPanel.signCanRecipientsCheckBox.isSelected();

        ExLinkAuthor[] authors = authorsPanel.authorsAuthorsModel.getAuthors();
        ExLinkSource[] sources = sourcesPanel.sourcesModel.getSources();

        String tags = docTypeAppendixPanel.tagsField.getText();

        // hashes StandardCharsets.UTF_8
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

        Transaction parent = DCSet.getInstance().getTransactionFinalMap().getRecord(docTypeAppendixPanel.parentReference.getText());
        ExLink exLink;
        int linkType = docTypeAppendixPanel.getSelectedItem();
        if (parent == null || linkType == ExData.LINK_SIMPLE_TYPE) {
            exLink = null;
        } else {
            switch (linkType) {
                case ExData.LINK_APPENDIX_TYPE:
                    exLink = new ExLinkAppendix(parent.getDBRef());
                    break;
                case ExData.LINK_REPLY_COMMENT_TYPE:
                    exLink = new ExLinkReply(parent.getDBRef());
                    break;
                case ExData.LINK_COMMENT_TYPE_FOR_VIEW:
                    APPENDIX_TYPE:
                    exLink = new ExLinkReply(parent.getDBRef());
                    break;
                default:
                    exLink = null;
            }
        }

        Fun.Tuple2<ExPays, String> exAccrualsResult = exAccrualsPanel.getAccruals();
        if (exAccrualsResult.b != null) {
            return new Fun.Tuple2(null, exAccrualsResult.b);
        }
        Fun.Tuple2<ExAirDrop, String> exAirDropResult = exAirDropPanel.getAccruals();
        if (exAirDropResult.b != null) {
            return new Fun.Tuple2(null, exAirDropResult.b);
        }


        Long templateKey = fill_Template_Panel.sel_Template == null ? null : fill_Template_Panel.sel_Template.getKey();

        byte[] exData;
        try {
            exData = ExData.make(exLink, exAccrualsResult.a, creator, jTextField_Title_Message.getText(),
                    signCanOnlyRecipients, recipients, authors, sources, tags, isEncrypted,
                    templateKey, fill_Template_Panel.get_Params(),
                    fill_Template_Panel.checkBoxMakeHashAndCheckUniqueTemplate.isSelected(),
                    jTextPane_Message.getText(), checkBoxMakeHashAndCheckUniqueText.isSelected(),
                    hashes_Map, checkBoxMakeHashAndCheckUniqueHashes.isSelected(),
                    files_1, checkBoxMakeHashAndCheckUniqueAttachedFiles.isSelected());
        } catch (Exception e) {
            return new Fun.Tuple2(null, e.getMessage());
        }

        return new Fun.Tuple2(exData, null);

    }
    // End of variables declaration
}

