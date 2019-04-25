package org.erachain.gui.items.persons;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.toedter.calendar.JDateChooser;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.transaction.RSetStatusToItem;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.statuses.ComboBoxStatusesModel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MButton;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.records.RecordInfo;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.gui.transaction.SetStatusToItemDetailsFrame;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PersonSetStatusDialog extends JDialog {
    private static final long serialVersionUID = 2717571093561259483L;
    private static Transaction parentRecord;
    private static RecordInfo infoPanel;
    private javax.swing.JTextField jAData1Txt;
    private javax.swing.JTextField jAData2Txt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel_Fee;
    private javax.swing.JLabel jLabel_Addition1;
    private javax.swing.JLabel jLabel_Addition2;
    private javax.swing.JLabel jLabel_Address;
    private javax.swing.JLabel jLabel_Data_From;
    private javax.swing.JLabel jLabel_Data_To;
    private javax.swing.JLabel jLabel_Param1;
    private javax.swing.JLabel jLabel_Param2;
    private javax.swing.JLabel jLabel_Parent_record;
    private javax.swing.JLabel jLabel_Status;
    private javax.swing.JLabel jLabel_Title;
    private javax.swing.JLabel jLabel__Description;
    private javax.swing.JScrollPane jLabel_PersonInfo;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea_Description;
    private PersonCls person;
    private MButton jButton_Cansel;
    private MButton jButton_SetStatus;
    private JComboBox<ItemCls> jComboBox_Status;

    /*
     * To change this license header, choose License Headers in Project Properties.
     * To change this template file, choose Tools | Templates
     * and open the template in the editor.
     */
    private JComboBox<Account> jComboBox_YourAddress;
    private javax.swing.JTextField jPar1Txt;
    private javax.swing.JTextField jPar2Txt;
    private javax.swing.JTextField jParentRecTxt;
    private javax.swing.JTextField jFeeTxt;
    private JDateChooser jFormattedTextField_fromDate;
    private JDateChooser jFormattedTextField_toDate;
    private javax.swing.JScrollPane jLabel_RecordInfo;
    public PersonSetStatusDialog(PersonCls person) {
        super();
        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);
        this.person = person;
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        this.setTitle(Lang.getInstance().translate("Set status"));
        jLabel_Address.setText(Lang.getInstance().translate("Your account") + ":");
        jLabel_Data_From.setText(Lang.getInstance().translate("From Date") + ":");
        jLabel_Data_To.setText(" " + Lang.getInstance().translate("To Date") + ":");
        jLabel_Param1.setText("%1 (" + Lang.getInstance().translate("integer") + ") :");
        jLabel_Param2.setText("%2 (" + Lang.getInstance().translate("integer") + ") :");
        jLabel_Addition1.setText("%3 (" + Lang.getInstance().translate("string") + "):");
        jLabel_Addition2.setText("%4 (" + Lang.getInstance().translate("string") + "):");
        jLabel_Parent_record.setText(Lang.getInstance().translate("Parent record") + ":");
        jLabel_Status.setText(Lang.getInstance().translate("Status") + ":");
        jLabel_Title.setText(Lang.getInstance().translate("Information about the person") + ":");
        jLabel__Description.setText("%D (" + Lang.getInstance().translate("text") + ") :");
        ;
        jLabel_Fee.setText(Lang.getInstance().translate("Fee Power") + " (0..6):");
        jComboBox_Status.setModel(new ComboBoxStatusesModel());
        jComboBox_YourAddress.setModel(new AccountsComboBoxModel());
        jLabel_PersonInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        PersonInfo info = new PersonInfo();
        info.show_001(person);
        info.setFocusable(false);
        jLabel_PersonInfo.setViewportView(info);
        jFormattedTextField_fromDate.setDateFormatString("yyyy-MM-dd");
        jFormattedTextField_toDate.setDateFormatString("yyyy-MM-dd");
        jPar1Txt.setText("");
        jPar2Txt.setText("");
        jAData1Txt.setText("");
        jAData2Txt.setText("");
        jFeeTxt.setText("0");
        jParentRecTxt.setText("0");
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setPreferredSize(MainFrame.getInstance().getPreferredSize());
        setModal(true);
        //PACK
        this.pack();
        //   this.setResizable(true);
        this.setLocationRelativeTo(null);
        this.setVisible(true);


    }

    private Transaction refreshRecordDetails() {

		/*
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			infoPanel.show_mess(Lang.getInstance().translate("Status must be OK to show public key details."));
	        jLabel_RecordInfo.setViewportView(infoPanel);
			return null;
		}
		*/

        Transaction record = null;
        if (jParentRecTxt.getText().length() == 0) {
            infoPanel.show_mess(Lang.getInstance().translate(""));
            jLabel_RecordInfo.setViewportView(infoPanel);
            return record;
        }

        record = DCSet.getInstance().getTransactionFinalMap().getRecord(jParentRecTxt.getText());
        if (record == null) {
            infoPanel.show_mess(Lang.getInstance().translate("Error") + " - use 1233-321.");
            jLabel_RecordInfo.setViewportView(infoPanel);
            return record;
        }

        ////ENABLE
        //jButton_Confirm.setEnabled(true);

        infoPanel.show_001(record);
        //infoPanel.setFocusable(false);
        jLabel_RecordInfo.setViewportView(infoPanel);

        return record;
    }

    public void onGoClick(
            PersonCls person, JButton Button_Confirm,
            Account creator, StatusCls status,
            String str_jFormattedTextField_fromDate, String str_jFormattedTextField_toDate, JTextField feePowTxt) {

        if (!OnDealClick.proccess1(Button_Confirm)) return;


        long fromDate = 0;
        long toDate = 0;
        int feePow = 0;
        int parse = 0;

        long value_1 = 0l;
        long value_2 = 0l;
        byte[] data_1 = jAData1Txt.getText().length() == 0 ? null :
                jAData1Txt.getText().getBytes(Charset.forName("UTF-8"));
        byte[] data_2 = jAData2Txt.getText().length() == 0 ? null :
                jAData2Txt.getText().getBytes(Charset.forName("UTF-8"));
        long refParent = 0l;

        byte[] description = jTextArea_Description.getText().length() == 0 ? null :
                jTextArea_Description.getText().getBytes(Charset.forName("UTF-8"));

        try {

            //READ FEE POW
            feePow = Integer.parseInt(feePowTxt.getText());

            //READ FROM DATE
            parse++;
            String str = str_jFormattedTextField_fromDate;
            if (str == null || str.equals("0000-00-00"))
                fromDate = Long.MIN_VALUE;
            else {
                if (str.length() < 11) str = str + " 12:12:12";
                fromDate = Timestamp.valueOf(str).getTime();
            }

            //READ TO DATE
            parse++;
            str = str_jFormattedTextField_toDate;
            if (str == null || str.equals("0000-00-00"))
                toDate = Long.MAX_VALUE;
            else {
                if (str.length() < 11) str = str + " 12:12:12";
                toDate = Timestamp.valueOf(str).getTime();
            }

            //READ VALUE 1
            parse++;
            if (jPar1Txt.getText().length() > 0) {
                value_1 = Long.parseLong(jPar1Txt.getText());
                assert (value_1 >= 0);
            }

            //READ VALUE 2
            parse++;
            if (jPar2Txt.getText().length() > 0) {
                value_2 = Long.parseLong(jPar2Txt.getText());
                assert (value_2 >= 0);
            }

        } catch (Exception e) {
            if (parse == 0) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee"), Lang.getInstance().translate("Error") + e, JOptionPane.ERROR_MESSAGE);
            } else if (parse == 1) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid From Date") + e, Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            } else if (parse == 2) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid To Date") + e, Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            } else if (parse == 3) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid Value 1") + e, Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            } else if (parse == 4) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid Value 2") + e, Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            }

            //ENABLE
            Button_Confirm.setEnabled(true);

            return;

        }

        //Account authenticator =  new Account(address);
        PrivateKeyAccount authenticator = Controller.getInstance().getPrivateKeyAccountByAddress(creator.getAddress());

        int version = 0;
        if (PersonSetStatusDialog.parentRecord != null) {
            int blockID = PersonSetStatusDialog.parentRecord.getBlockHeight();
            int seqNo = PersonSetStatusDialog.parentRecord.getSeqNo();
            byte[] bytesParent = Ints.toByteArray(blockID);
            bytesParent = Bytes.concat(bytesParent, Ints.toByteArray(seqNo));
            refParent = Longs.fromByteArray(bytesParent);
        }

        Transaction transaction = Controller.getInstance().r_SetStatusToItem(version, false, authenticator,
                feePow, status.getKey(),
                person, fromDate, toDate,
                value_1, value_2, data_1, data_2, refParent, description
        );

        String Status_text = "";
        IssueConfirmDialog dd = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                Lang.getInstance().translate("Send Mail"), (int) (this.getWidth() / 1.2), (int) (this.getHeight() / 1.2), Status_text, Lang.getInstance().translate("Confirmation Transaction"));

        SetStatusToItemDetailsFrame ww = new SetStatusToItemDetailsFrame((RSetStatusToItem) transaction);
        dd.jScrollPane1.setViewportView(ww);
        dd.setLocationRelativeTo(this);
        dd.setVisible(true);

        //	JOptionPane.OK_OPTION
        if (dd.isConfirm) {


            Integer result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);


            //CHECK VALIDATE MESSAGE
            if (result == Transaction.VALIDATE_OK) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Status assigned!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            } else {

                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }
        //ENABLE
        Button_Confirm.setEnabled(true);

    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jComboBox_YourAddress = new javax.swing.JComboBox<>();
        jLabel_Address = new javax.swing.JLabel();
        jLabel_Status = new javax.swing.JLabel();
        jComboBox_Status = new javax.swing.JComboBox<>();
        jLabel_Data_From = new javax.swing.JLabel();
        jFormattedTextField_fromDate = new JDateChooser("yyyy-MM-dd", "####-##-##", '_');// new javax.swing.JFormattedTextField();
        jLabel_Data_To = new javax.swing.JLabel();
        jFormattedTextField_toDate = new JDateChooser("yyyy-MM-dd", "####-##-##", '_');// new javax.swing.JFormattedTextField();
        jLabel_Param1 = new javax.swing.JLabel();
        jPar1Txt = new javax.swing.JTextField();
        jLabel_Param2 = new javax.swing.JLabel();
        jPar2Txt = new javax.swing.JTextField();
        jLabel_Addition1 = new javax.swing.JLabel();
        jAData1Txt = new javax.swing.JTextField();
        jLabel_Addition2 = new javax.swing.JLabel();
        jAData2Txt = new javax.swing.JTextField();
        jLabel_Parent_record = new javax.swing.JLabel();
        jParentRecTxt = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea_Description = new javax.swing.JTextArea();
        jLabel_Fee = new javax.swing.JLabel();
        jFeeTxt = new javax.swing.JTextField();


        jLabel_Title = new javax.swing.JLabel();
        jLabel__Description = new javax.swing.JLabel();
        jLabel_PersonInfo = new javax.swing.JScrollPane();
        jLabel1 = new javax.swing.JLabel();

        //      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.awt.GridBagLayout layout1 = new java.awt.GridBagLayout();
        //      layout.columnWidths = new int[] {0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0};
        //     layout.rowHeights = new int[] {0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0};
        getContentPane().setLayout(layout1);

        //       jComboBox_YourAddress.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 19);
        getContentPane().add(jComboBox_YourAddress, gridBagConstraints);

        jLabel_Address.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(17, 16, 0, 0);
        getContentPane().add(jLabel_Address, gridBagConstraints);

        jLabel_Status.setText("jLabel3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(15, 16, 0, 0);
        getContentPane().add(jLabel_Status, gridBagConstraints);

        //     jComboBox_Status.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 19);
        getContentPane().add(jComboBox_Status, gridBagConstraints);

        jLabel_Data_From.setText("jLabel4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(22, 16, 0, 0);
        getContentPane().add(jLabel_Data_From, gridBagConstraints);

        //       jFormattedTextField_fromDate.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("yyyy-MM-dd"))));
        jFormattedTextField_fromDate.setMinimumSize(new java.awt.Dimension(80, 20));
        jFormattedTextField_fromDate.setName(""); // NOI18N
        jFormattedTextField_fromDate.setFont(UIManager.getFont("TextField.font"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(22, 0, 0, 0);
        getContentPane().add(jFormattedTextField_fromDate, gridBagConstraints);

        jLabel_Data_To.setText("jLabel5");
        jLabel_Data_To.setFont(UIManager.getFont("TextField.font"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(22, 0, 0, 0);
        getContentPane().add(jLabel_Data_To, gridBagConstraints);

        //    jFormattedTextField_toDate.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("yyyy-MM-dd"))));
        jFormattedTextField_toDate.setMinimumSize(new java.awt.Dimension(80, 20));
        jFormattedTextField_toDate.setName(""); // NOI18N
        //      jFormattedTextField_toDate.addActionListener(new java.awt.event.ActionListener() {
        //          public void actionPerformed(java.awt.event.ActionEvent evt) {
        //              jFormattedTextField_toDateActionPerformed(evt);
        //          }
        //      });
        jFormattedTextField_toDate.setFont(UIManager.getFont("TextField.font"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(22, 0, 0, 1);
        getContentPane().add(jFormattedTextField_toDate, gridBagConstraints);

        jLabel_Param1.setText("jLabel6");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(21, 16, 0, 0);
        getContentPane().add(jLabel_Param1, gridBagConstraints);

        jPar1Txt.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(21, 0, 0, 19);
        getContentPane().add(jPar1Txt, gridBagConstraints);

        jLabel_Param2.setText("jLabel7");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        getContentPane().add(jLabel_Param2, gridBagConstraints);

        jPar2Txt.setText("jTextField2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 19);
        getContentPane().add(jPar2Txt, gridBagConstraints);

        jLabel_Addition1.setText("jLabel8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(17, 16, 0, 0);
        getContentPane().add(jLabel_Addition1, gridBagConstraints);

        jAData1Txt.setText("jTextField3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(17, 0, 0, 19);
        getContentPane().add(jAData1Txt, gridBagConstraints);

        jLabel_Addition2.setText("jLabel9");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        getContentPane().add(jLabel_Addition2, gridBagConstraints);

        jAData2Txt.setText("jTextField4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 19);
        getContentPane().add(jAData2Txt, gridBagConstraints);

        jLabel_Parent_record.setText("jLabel10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(15, 16, 0, 0);
        getContentPane().add(jLabel_Parent_record, gridBagConstraints);

        jParentRecTxt.setText("jTextField5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 19);
        getContentPane().add(jParentRecTxt, gridBagConstraints);

        jTextArea_Description.setColumns(20);
        jTextArea_Description.setRows(5);
        jScrollPane2.setViewportView(jTextArea_Description);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(13, 0, 0, 19);
        getContentPane().add(jScrollPane2, gridBagConstraints);

        jLabel_Fee.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel_Fee.setText("jLabel11");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(jLabel_Fee, gridBagConstraints);

        jFeeTxt.setText("jTextField6");
        jFeeTxt.setMinimumSize(new java.awt.Dimension(80, 20));
        jFeeTxt.setName(""); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;

        gridBagConstraints.insets = new java.awt.Insets(20, 0, 20, 17);
        getContentPane().add(jFeeTxt, gridBagConstraints);

        jButton_Cansel = new MButton(Lang.getInstance().translate("Cancel"), 2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 23, 0);
        getContentPane().add(jButton_Cansel, gridBagConstraints);

        jButton_Cansel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
            }
        });


        jButton_SetStatus = new MButton(Lang.getInstance().translate("Set status"), 2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.ipadx = 1;
        // gridBagConstraints.fill = gridBagConstraints.HORIZONTAL;
        // gridBagConstraints.weightx = 0.1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 23, 16);
        getContentPane().add(jButton_SetStatus, gridBagConstraints);


        jButton_SetStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {


                Date date;
                String str_jFormattedTextField_fromDate;
                try {
                    date = jFormattedTextField_fromDate.getCalendar().getTime();
                    str_jFormattedTextField_fromDate = (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + (date.getDate());
                } catch (Exception e2) {
                    str_jFormattedTextField_fromDate = null;

                }

                String str_jFormattedTextField_toDate;
                try {
                    date = jFormattedTextField_toDate.getCalendar().getTime();
                    str_jFormattedTextField_toDate = (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + (date.getDate());

                } catch (Exception e3) {
                    str_jFormattedTextField_toDate = null;

                }

                onGoClick(person, jButton_SetStatus, (Account) jComboBox_YourAddress.getSelectedItem(),
                        (StatusCls) jComboBox_Status.getSelectedItem(),
                        str_jFormattedTextField_fromDate, str_jFormattedTextField_toDate, jFeeTxt);

            }
        });


        jLabel_Title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_Title.setText("jLabel12");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 17, 0, 20);
        getContentPane().add(jLabel_Title, gridBagConstraints);

        jLabel__Description.setText("jLabel15");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(13, 16, 0, 0);
        getContentPane().add(jLabel__Description, gridBagConstraints);

        jLabel_PersonInfo.setMaximumSize(new java.awt.Dimension(700, 200));
        jLabel_PersonInfo.setMinimumSize(new java.awt.Dimension(500, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 19);
        getContentPane().add(jLabel_PersonInfo, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        getContentPane().add(jLabel1, gridBagConstraints);


    }// </editor-fold>

}
