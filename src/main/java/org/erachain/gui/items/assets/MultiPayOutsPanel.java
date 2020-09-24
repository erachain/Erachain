package org.erachain.gui.items.assets;


import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.IconPanel;
import org.erachain.gui.models.RenderComboBoxOtborPoDeistviy;
import org.erachain.gui.models.RenderComboBoxVidBalance;


public class MultiPayOutsPanel extends IconPanel {


    public static String NAME = "MultiPayOutsPanel";
    public static String TITLE = "Multi paument out";
    public ComboBoxAssetsModel accountsModel;
    public ComboBoxAssetsModel accountsModel1;

    public MultiPayOutsPanel() {
        super(NAME, TITLE);
        initComponents();
        accountsModel = new ComboBoxAssetsModel();
        accountsModel1 = new ComboBoxAssetsModel();
        this.jComboBoxAsset.setModel(accountsModel);
        this.jComboBoxOtborAsset.setModel(accountsModel1);
        jComboBoxVidBalance.setModel(new javax.swing.DefaultComboBoxModel(new Integer[] {
                TransactionAmount.ACTION_DEBT,
                TransactionAmount.ACTION_SEND,
                TransactionAmount.ACTION_HOLD,
                TransactionAmount.ACTION_REPAY_DEBT,
                TransactionAmount.ACTION_SPEND,
                TransactionAmount.ACTION_PLEDGE,
                TransactionAmount.ACTION_RESERCED_6
        }));
        jComboBoxVidBalance.setRenderer(new RenderComboBoxVidBalance());
        jComboBoxStoronaBalance.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
            "Всего Дебет", "Остаток",    "Всего Кредит"
        }));
        jComboBoxOtborPoDeistviy.setModel(new javax.swing.DefaultComboBoxModel<>(new Integer[] {1,2,3,4,5 }));
        jComboBoxOtborPoDeistviy.setRenderer(new RenderComboBoxOtborPoDeistviy());
    }



    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel13 = new javax.swing.JLabel();
        buttonGroupGender = new javax.swing.ButtonGroup();
        jLabelTitle = new javax.swing.JLabel();
        jComboBoxAsset = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jComboBoxOtborAsset = new javax.swing.JComboBox<>();
        jLabelVidBalance = new javax.swing.JLabel();
        jComboBoxVidBalance = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldBQ = new javax.swing.JTextField();
        jTextFieldLQ = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabelAsset1 = new javax.swing.JLabel();
        jLabelTitlemetod = new javax.swing.JLabel();
        jRadioButtonMetodCoff = new javax.swing.JRadioButton();
        jRadioButtonMetodSumm = new javax.swing.JRadioButton();
        jLabelMetodPaumentDecscription = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabelMetodPaumentItemMin = new javax.swing.JLabel();
        jTextFieldMetodPaumentItemMin = new javax.swing.JTextField();
        jLabelMetodPaumentItemMax = new javax.swing.JLabel();
        jTextFieldMetodPaumentItemMax = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabelDataStart = new javax.swing.JLabel();
        jTextFieldDateStart = new javax.swing.JTextField();
        jLabelDateEnd = new javax.swing.JLabel();
        jTextFieldDateEnd = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jComboBoxStoronaBalance = new javax.swing.JComboBox<>();
        jLabel20 = new javax.swing.JLabel();
        jComboBoxOtborPoDeistviy = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jCheckBoxConfirmResult = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jCheckBoxOnlyPersons = new javax.swing.JCheckBox();
        jRadioButtonPersonsOnlyWomem = new javax.swing.JRadioButton();
        jRadioButtonPersonsOnlyMen = new javax.swing.JRadioButton();
        jRadioButtonPersonsAll = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();

        jLabel13.setText("jLabel13");

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        layout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        setLayout(layout);

        jLabelTitle.setText("Multi paument");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(jLabelTitle, gridBagConstraints);

//        jComboBoxAsset.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jComboBoxAsset, gridBagConstraints);

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("отбор по активу");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(14, 0, 0, 0);
        add(jLabel5, gridBagConstraints);

//        jComboBoxOtborAsset.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jComboBoxOtborAsset, gridBagConstraints);

        jLabelVidBalance.setText("Вид баланса");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabelVidBalance, gridBagConstraints);

//        jComboBoxVidBalance.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jComboBoxVidBalance, gridBagConstraints);

        jLabel8.setText("Больше чем");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        add(jLabel8, gridBagConstraints);

        jTextFieldBQ.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        add(jTextFieldBQ, gridBagConstraints);

        jTextFieldLQ.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jTextFieldLQ, gridBagConstraints);

        jLabel9.setText("Меньше чем");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 17, 0, 0);
        add(jLabel9, gridBagConstraints);

        jLabelAsset1.setText("Asset");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabelAsset1, gridBagConstraints);

        jLabelTitlemetod.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelTitlemetod.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTitlemetod.setText("Metod payment");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabelTitlemetod, gridBagConstraints);

        buttonGroup1.add(jRadioButtonMetodCoff);
        jRadioButtonMetodCoff.setText("Koficient");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        add(jRadioButtonMetodCoff, gridBagConstraints);

        buttonGroup1.add(jRadioButtonMetodSumm);
        jRadioButtonMetodSumm.setText("Summa");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        add(jRadioButtonMetodSumm, gridBagConstraints);

        jLabelMetodPaumentDecscription.setText("по общеме значению");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jLabelMetodPaumentDecscription, gridBagConstraints);

        java.awt.GridBagLayout jPanel1Layout = new java.awt.GridBagLayout();
        jPanel1Layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0};
        jPanel1Layout.rowHeights = new int[] {0};
        jPanel1.setLayout(jPanel1Layout);

        jLabelMetodPaumentItemMin.setText("Значение Мин.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanel1.add(jLabelMetodPaumentItemMin, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.2;
        jPanel1.add(jTextFieldMetodPaumentItemMin, gridBagConstraints);

        jLabelMetodPaumentItemMax.setText("Значение Макс.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(jLabelMetodPaumentItemMax, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.2;
        jPanel1.add(jTextFieldMetodPaumentItemMax, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jPanel1, gridBagConstraints);

        java.awt.GridBagLayout jPanel2Layout = new java.awt.GridBagLayout();
        jPanel2Layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0};
        jPanel2Layout.rowHeights = new int[] {0};
        jPanel2.setLayout(jPanel2Layout);

        jLabelDataStart.setText("Data start");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanel2.add(jLabelDataStart, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        jPanel2.add(jTextFieldDateStart, gridBagConstraints);

        jLabelDateEnd.setText("Date end");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 13, 0, 0);
        jPanel2.add(jLabelDateEnd, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        jPanel2.add(jTextFieldDateEnd, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 32;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jPanel2, gridBagConstraints);

        jLabel19.setText("Сторона баланса");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel19, gridBagConstraints);

        jComboBoxStoronaBalance.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jComboBoxStoronaBalance, gridBagConstraints);

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("Отбор по действию");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
        add(jLabel20, gridBagConstraints);

//        jComboBoxOtborPoDeistviy.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 30;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jComboBoxOtborPoDeistviy, gridBagConstraints);

        java.awt.GridBagLayout jPanel3Layout = new java.awt.GridBagLayout();
        jPanel3Layout.columnWidths = new int[] {0, 5, 0, 5, 0};
        jPanel3Layout.rowHeights = new int[] {0};
        jPanel3.setLayout(jPanel3Layout);

        jButton2.setText("ViewResult");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel3.add(jButton2, gridBagConstraints);

        jButton3.setText("Cancel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel3.add(jButton3, gridBagConstraints);

        jButton1.setText("Send");
        jButton1.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        jPanel3.add(jButton1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 44;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
      //  add(jPanel3, gridBagConstraints);

        jLabel1.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 48;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.weighty = 0.2;
        add(jLabel1, gridBagConstraints);

        jCheckBoxConfirmResult.setText("Подтверждаю правильность результата");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 42;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        add(jCheckBoxConfirmResult, gridBagConstraints);

        java.awt.GridBagLayout jPanel4Layout = new java.awt.GridBagLayout();
        jPanel4Layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0};
        jPanel4Layout.rowHeights = new int[] {0};
        jPanel4.setLayout(jPanel4Layout);

        jCheckBoxOnlyPersons.setText("Только персоны");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 22);
        jPanel4.add(jCheckBoxOnlyPersons, gridBagConstraints);

        buttonGroupGender.add(jRadioButtonPersonsOnlyWomem);
        jRadioButtonPersonsOnlyWomem.setText("Женщинам");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        jPanel4.add(jRadioButtonPersonsOnlyWomem, gridBagConstraints);

        buttonGroupGender.add(jRadioButtonPersonsOnlyMen);
        jRadioButtonPersonsOnlyMen.setText("Мужчинам");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        jPanel4.add(jRadioButtonPersonsOnlyMen, gridBagConstraints);

        buttonGroupGender.add(jRadioButtonPersonsAll);
        jRadioButtonPersonsAll.setText("Всем");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        jPanel4.add(jRadioButtonPersonsAll, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 38;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        add(jPanel4, gridBagConstraints);

        jLabel2.setText("Выберите Актив");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        add(jLabel2, gridBagConstraints);

        jLabel3.setText("Выберите действие");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 30;
        add(jLabel3, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Отбор по персонам");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 36;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(jLabel4, gridBagConstraints);

        jLabel6.setText("Сумма");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        add(jLabel6, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(jSeparator1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(jSeparator2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(jSeparator3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 34;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(jSeparator4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 40;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        add(jSeparator5, gridBagConstraints);
    }


    public javax.swing.ButtonGroup buttonGroup1;
    public javax.swing.ButtonGroup buttonGroupGender;
    public javax.swing.JButton jButton1;
    public javax.swing.JButton jButton2;
    public javax.swing.JButton jButton3;
    public javax.swing.JCheckBox jCheckBoxConfirmResult;
    public javax.swing.JCheckBox jCheckBoxOnlyPersons;
    public javax.swing.JComboBox<ItemCls> jComboBoxAsset;
    public javax.swing.JComboBox<ItemCls> jComboBoxOtborAsset;
    public javax.swing.JComboBox<Integer> jComboBoxOtborPoDeistviy;
    public javax.swing.JComboBox<String> jComboBoxStoronaBalance;
    public javax.swing.JComboBox<Integer> jComboBoxVidBalance;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JLabel jLabel13;
    public javax.swing.JLabel jLabel19;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel20;
    public javax.swing.JLabel jLabel3;
    public javax.swing.JLabel jLabel4;
    public javax.swing.JLabel jLabel5;
    public javax.swing.JLabel jLabel6;
    public javax.swing.JLabel jLabel8;
    public javax.swing.JLabel jLabel9;
    public javax.swing.JLabel jLabelAsset1;
    public javax.swing.JLabel jLabelDataStart;
    public javax.swing.JLabel jLabelDateEnd;
    public javax.swing.JLabel jLabelMetodPaumentDecscription;
    public javax.swing.JLabel jLabelMetodPaumentItemMax;
    public javax.swing.JLabel jLabelMetodPaumentItemMin;
    public javax.swing.JLabel jLabelTitle;
    public javax.swing.JLabel jLabelTitlemetod;
    public javax.swing.JLabel jLabelVidBalance;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JPanel jPanel2;
    public javax.swing.JPanel jPanel3;
    public javax.swing.JPanel jPanel4;
    public javax.swing.JRadioButton jRadioButtonMetodCoff;
    public javax.swing.JRadioButton jRadioButtonMetodSumm;
    public javax.swing.JRadioButton jRadioButtonPersonsAll;
    public javax.swing.JRadioButton jRadioButtonPersonsOnlyMen;
    public javax.swing.JRadioButton jRadioButtonPersonsOnlyWomem;
    public javax.swing.JSeparator jSeparator1;
    public javax.swing.JSeparator jSeparator2;
    public javax.swing.JSeparator jSeparator3;
    public javax.swing.JSeparator jSeparator4;
    public javax.swing.JSeparator jSeparator5;
    public javax.swing.JTextField jTextFieldBQ;
    public javax.swing.JTextField jTextFieldDateEnd;
    public javax.swing.JTextField jTextFieldDateStart;
    public javax.swing.JTextField jTextFieldLQ;
    public javax.swing.JTextField jTextFieldMetodPaumentItemMax;
    public javax.swing.JTextField jTextFieldMetodPaumentItemMin;


}
