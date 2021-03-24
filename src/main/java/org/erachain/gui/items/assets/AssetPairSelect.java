package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.MainFrame;
import org.erachain.gui.library.MDecimalFormatedTextField;
import org.erachain.gui.library.MTable;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class AssetPairSelect extends JDialog {

    public AssetPairSelectTableModel assetPairSelectTableModel;
    public AssetCls pairAsset;
    AssetsPairSelectPanel pair_Panel = new AssetsPairSelectPanel();
    RowSorter sorter;
    private MDecimalFormatedTextField key_Item;

    public AssetPairSelect(long key, String action, String account) {

        this.setTitle(Controller.getInstance().getApplicationName(false) + " - " + Controller.getInstance().getAsset(key).toString() + " - " + Lang.T("Select pair"));
        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        //CLOSE
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setModal(true);
        //LAYOUT
        this.setLayout(new GridBagLayout());

        //this.setSize(800, 600);

        //PADDING
        ((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));


        //Label GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(0, 5, 5, 0);
        labelGBC.fill = GridBagConstraints.BOTH;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.weightx = 1;
        labelGBC.weighty = 1;
        labelGBC.gridwidth = 2;
        labelGBC.gridx = 0;
        labelGBC.gridy = 0;

        JLabel label = new JLabel("Выберите пару");

        if (action == "Buy")
            label.setText("Укажите актив на который хотите купить " + Controller.getInstance().getAsset(key).toString());
        if (action == "To sell")
            label.setText("Укажите актив за который хотите продать " + Controller.getInstance().getAsset(key).toString());
        //TABLE GBC
        GridBagConstraints tableGBC = new GridBagConstraints();
        tableGBC.insets = new Insets(0, 5, 5, 0);
        tableGBC.fill = GridBagConstraints.BOTH;
        tableGBC.anchor = GridBagConstraints.NORTHWEST;
        tableGBC.weightx = 1;
        tableGBC.weighty = 1;
        tableGBC.gridwidth = 2;
        tableGBC.gridx = 0;
        tableGBC.gridy = 1;


        pair_Panel.button1ToolBarLeftPanel.setVisible(false);
        pair_Panel.button1ToolBarLeftPanel.setEnabled(false);
        pair_Panel.button1ToolBarLeftPanel.setFocusable(true);
        pair_Panel.button1ToolBarLeftPanel.setText(Lang.T("Next"));
        pair_Panel.button1ToolBarLeftPanel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                selectAsset();

            }


        });


        pair_Panel.button2ToolBarLeftPanel.setVisible(false);
        pair_Panel.searchMyJCheckBoxLeftPanel.setVisible(false);
        pair_Panel.searchFavoriteJCheckBoxLeftPanel.setVisible(false);
        pair_Panel.jButton1_jToolBar_RightPanel.setVisible(false);
        pair_Panel.jButton2_jToolBar_RightPanel.setVisible(false);

        pair_Panel.searthLabelSearchToolBarLeftPanel.setText(Lang.T("Search") + ":  ");

        pair_Panel.searchToolBar_LeftPanel.setVisible(true);
        pair_Panel.toolBarLeftPanel.add(new JLabel(Lang.T("Find Key") + ":"));
        key_Item = new MDecimalFormatedTextField();
        key_Item.setToolTipText("");
        key_Item.setAlignmentX(1.0F);
        key_Item.setMinimumSize(new java.awt.Dimension(100, 20));
        key_Item.setName(""); // NOI18N
        key_Item.setPreferredSize(new java.awt.Dimension(100, 20));
        key_Item.setMaximumSize(new java.awt.Dimension(2000, 20));

        MenuPopupUtil.installContextMenu(key_Item);

        pair_Panel.toolBarLeftPanel.add(key_Item);
        key_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub

                pair_Panel.searchTextFieldSearchToolBarLeftPanelDocument.setText("");

                pair_Panel.Label_search_Info_Panel.setText(Lang.T("Waiting..."));

                new Thread() {
                    @Override
                    public void run() {
                        assetPairSelectTableModel.findByKey(key_Item.getText());
                        if (assetPairSelectTableModel.getRowCount() < 1) {
                            pair_Panel.Label_search_Info_Panel.setText(Lang.T("Not Found Assets"));
                            pair_Panel.jScrollPanelLeftPanel.setViewportView(pair_Panel.search_Info_Panel);
                            return;
                        }
                        pair_Panel.jTableJScrollPanelLeftPanel.setRowSelectionInterval(0, 0);
                        // ddd.dispose();
                        pair_Panel.jScrollPanelLeftPanel.setViewportView(pair_Panel.jTableJScrollPanelLeftPanel);
                    }
                }.start();


            }

        });


        pair_Panel.searchTextFieldSearchToolBarLeftPanelDocument.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                // GET VALUE
                String search = pair_Panel.searchTextFieldSearchToolBarLeftPanelDocument.getText();
                if (search.equals("")) {
                    pair_Panel.jScrollPaneJPanelRightPanel.setViewportView(null);
                    assetPairSelectTableModel.clear();
                    pair_Panel.Label_search_Info_Panel.setText(Lang.T("Enter more  2 characters"));
                    pair_Panel.jScrollPanelLeftPanel.setViewportView(pair_Panel.search_Info_Panel);
                    return;
                }
                if (search.length() < 3) {
                    pair_Panel.Label_search_Info_Panel.setText(Lang.T("Enter more  2 characters"));
                    pair_Panel.jScrollPanelLeftPanel.setViewportView(pair_Panel.search_Info_Panel);


                    return;
                }
                key_Item.setText("");

                pair_Panel.Label_search_Info_Panel.setText(Lang.T("Waiting..."));
                pair_Panel.jScrollPanelLeftPanel.setViewportView(pair_Panel.search_Info_Panel);


                //	search_Table_Model.findByName(search);

                new Thread() {
                    @Override
                    public void run() {
                        assetPairSelectTableModel.set_Filter_By_Name(search);
                        if (assetPairSelectTableModel.getRowCount() < 1) {
                            pair_Panel.Label_search_Info_Panel.setText(Lang.T("Not Found Assets"));
                            pair_Panel.jScrollPanelLeftPanel.setViewportView(pair_Panel.search_Info_Panel);
                            return;
                        }
                        if (!assetPairSelectTableModel.isEmpty())
                            pair_Panel.jTableJScrollPanelLeftPanel.setRowSelectionInterval(0, 0);

                        // ddd.dispose();
                        pair_Panel.jScrollPanelLeftPanel.setViewportView(pair_Panel.jTableJScrollPanelLeftPanel);
                    }
                }.start();


            }
        });


        assetPairSelectTableModel = new AssetPairSelectTableModel(key);

        final MTable assetsPairTable = new MTable(assetPairSelectTableModel);

        sorter = new TableRowSorter(assetPairSelectTableModel);
        assetsPairTable.setRowSorter(sorter);


        pair_Panel.jTableJScrollPanelLeftPanel.setModel(assetPairSelectTableModel);
        pair_Panel.jTableJScrollPanelLeftPanel = assetsPairTable;

        pair_Panel.jTableJScrollPanelLeftPanel.setIntercellSpacing(new java.awt.Dimension(2, 2));

        pair_Panel.jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @SuppressWarnings({"unused"})
            @Override
            public void valueChanged(ListSelectionEvent arg0) {

                if (false && pair_Panel.jTableJScrollPanelLeftPanel.getSelectedRow() >= 0) {
                    // GET ROW
                    int row = pair_Panel.jTableJScrollPanelLeftPanel.getSelectedRow();

                    try {
                        row = pair_Panel.jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                        AssetCls asset = assetPairSelectTableModel.getItem(row).a;

                        if (asset == null)
                            return;

                        pair_Panel.jScrollPaneJPanelRightPanel.setViewportView(new AssetInfo((AssetCls) asset, false));
                        pair_Panel.button1ToolBarLeftPanel.setEnabled(true);

                        } catch (Exception e) {
                        pair_Panel.jScrollPaneJPanelRightPanel.setViewportView(null);
                    }

                }
            }
        });

        pair_Panel.jTableJScrollPanelLeftPanel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        //Custom renderer for the String column;

        // column #1
        TableColumn column1 = pair_Panel.jTableJScrollPanelLeftPanel.getColumnModel().getColumn(AssetPairSelectTableModel.COLUMN_KEY);//.COLUMN_CONFIRMED);
        column1.setMinWidth(1);
        column1.setMaxWidth(200);
        column1.setPreferredWidth(100);
        // column #1
        TableColumn column2 = pair_Panel.jTableJScrollPanelLeftPanel.getColumnModel().getColumn(AssetPairSelectTableModel.COLUMN_NAME);//.COLUMN_CONFIRMED);
        column2.setMinWidth(150);
        column2.setMaxWidth(500);
        column2.setPreferredWidth(300);

        // изменение высоты строки при изменении ширины - в заголовках можно HTML с переносом сделать
        //		pair_Panel.setRowHeightFormat(true);

        pair_Panel.jTableJScrollPanelLeftPanel.getTableHeader().setPreferredSize(new Dimension(10, (int) (pair_Panel.jTableJScrollPanelLeftPanel.getTableHeader().getPreferredSize().getHeight() + 6)));

        pair_Panel.jTableJScrollPanelLeftPanel.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        pair_Panel.jTableJScrollPanelLeftPanel.addMouseListener(new MouseAdapter() {


            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();

                    if (row < assetPairSelectTableModel.getRowCount()) {
                        selectAsset();
                    }
                }
            }
        });

        //this.add(new JScrollPane(assetsPairTable), tableGBC);
        //this.add(label, labelGBC);

        pair_Panel.jScrollPanelLeftPanel.setViewportView(pair_Panel.jTableJScrollPanelLeftPanel);
        pair_Panel.searthLabelSearchToolBarLeftPanel.setText(Lang.T("Search") + ":  ");

        // UPDATE FILTER ON TEXT CHANGE
        pair_Panel.searchTextFieldSearchToolBarLeftPanelDocument.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                onChange();
            }

            public void removeUpdate(DocumentEvent e) {
                onChange();
            }

            public void insertUpdate(DocumentEvent e) {
                onChange();
            }

            public void onChange() {

                // GET VALUE
                String search = pair_Panel.searchTextFieldSearchToolBarLeftPanelDocument.getText();

                // SET FILTER
                assetPairSelectTableModel.fireTableDataChanged();
                RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
                ((DefaultRowSorter) sorter).setRowFilter(filter);
                assetPairSelectTableModel.fireTableDataChanged();

            }
        });


        //    Dimension size = MainFrame.getInstance().desktopPane.getSize();
        //    pair_Panel.jSplitPanel.setDividerLocation((int)(size.width/2.5));
        this.add(pair_Panel, labelGBC);
        //PACK
        this.pack();
        //	this.setSize( size.width-(size.width/8), size.height-(size.width/8));
        pair_Panel.jSplitPanel.setDividerLocation(MainFrame.getInstance().getHeight() / 3);
        this.setResizable(true);
        this.setSize(MainFrame.getInstance().getWidth() - 300, MainFrame.getInstance().getHeight() - 300);
        this.setLocationRelativeTo(MainFrame.getInstance());
        this.setVisible(true);
    }

    private void selectAsset() {
        if (pair_Panel.jTableJScrollPanelLeftPanel.getSelectedRow() >= 0) {
            AssetPairSelectTableModel tableModelAssets1 = (AssetPairSelectTableModel) pair_Panel.jTableJScrollPanelLeftPanel.getModel();//new WalletItemAssetsTableModel();//(WalletItemAssetsTableModel) my_Assets_SplitPanel.jTableJScrollPanelLeftPanel.getModel();
            pairAsset = (AssetCls) tableModelAssets1.getItem(pair_Panel.jTableJScrollPanelLeftPanel.convertRowIndexToModel(pair_Panel.jTableJScrollPanelLeftPanel.getSelectedRow())).a;
            dispose();
        }


    }


}
