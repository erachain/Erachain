package org.erachain.gui.items.persons;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.Gui;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.ntp.NTP;
import org.erachain.utils.Pair;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// TODO удалить или обновить
public class RIPPersonFrame extends JInternalFrame {

    /**
     *
     */
    private static final long serialVersionUID = 2717571093561259483L;
    static PersonCls person;
    private JComboBox<Account> accountLBox;

    public RIPPersonFrame(JFrame parent) {
        super(Lang.T("R.I.P Person"));


        final JTextField personKeyTxt = new JTextField();
        final JLabel personDetails = new JLabel();

        final JTextField endDate = new JTextField(".");
        final JTextField feePow = new JTextField("0");

        //CLOSE
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //LAYOUT
        this.setLayout(new GridBagLayout());
        //PADDING
        ((JComponent) this.getContentPane()).setBorder(new EmptyBorder(10, 15, 15, 10));

        // Create a constraints object, and specify some default values
        GridBagConstraints input = new GridBagConstraints();
        input.insets = new Insets(0, 5, 5, 0); // 5-pixel margins on all sides
        input.fill = GridBagConstraints.HORIZONTAL;
        input.anchor = GridBagConstraints.NORTHWEST;
        input.gridwidth = 5;
        input.gridheight = 1;

        GridBagConstraints label = new GridBagConstraints();
        label.insets = new Insets(0, 5, 5, 0);
        label.fill = GridBagConstraints.HORIZONTAL;
        label.anchor = GridBagConstraints.NORTHWEST;
        label.gridx = 0;
        label.gridheight = 1;

        //LABEL FROM
        ++label.gridy;
        this.add(new JLabel(Lang.T("Account") + ":"), label);

        //COMBOBOX FROM
        ++input.gridy;
        this.accountLBox = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.accountLBox, input);

        ++label.gridy;
        this.add(new JLabel(Lang.T("Person Key") + ":"), label);

        ++input.gridy;
        this.add(personKeyTxt, input);


        personKeyTxt.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent arg0) {
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                RIPPersonFrame.person = refreshReceiverDetails(personKeyTxt, personDetails);
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                RIPPersonFrame.person = refreshReceiverDetails(personKeyTxt, personDetails);
            }
        });

        ++input.gridy;
        ++label.gridy;
        JPanel htmlPanel = new JPanel();
        htmlPanel.setBorder(BorderFactory.createTitledBorder(Lang.T("Details")));

        String text = "";
        //font = new Font(null, Font.PLAIN, 10);

        GridBagConstraints detail = new GridBagConstraints();
        detail.insets = new Insets(0, 5, 5, 0);
        detail.fill = GridBagConstraints.BOTH; // components grow in both dimensions
        detail.anchor = GridBagConstraints.NORTHWEST;
        detail.gridx = 0;
        detail.gridy = 0;
        detail.gridwidth = 5;
        detail.gridheight = 3;
        detail.weightx = -5;
        detail.weighty = -2;
        personDetails.setText(text);
        htmlPanel.add(personDetails, detail);

        input.gridx = 0;
        input.gridwidth = 5;
        input.gridheight = 2;
        this.add(htmlPanel, input); // BorderLayout.SOUTH);

        ++label.gridy;
        this.add(new JLabel(Lang.T("Date ('.'=today)") + ":"), label);

        input.gridy = label.gridy;
        input.gridx = 1;
        input.gridwidth = 5;
        input.gridheight = 1;
        this.add(endDate, input);

        // FEE POWER
        ++label.gridy;
        JLabel feeLabel = new JLabel(Lang.T("Fee Power") + ":");
        feeLabel.setVisible(Gui.SHOW_FEE_POWER);
        this.add(feeLabel, label);

        ++input.gridy;
        feePow.setText("0");
        feePow.setVisible(Gui.SHOW_FEE_POWER);
        this.add(feePow, input);

        // BUTTONS
        ++input.gridy;
        input.gridx = 1;
        input.gridwidth = 2;
        input.gridheight = 1;
        JButton Button_Cancel = new JButton(Lang.T("Cancel"));
        Button_Cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // программа обработки при нажатии cancel
            }
        });
        this.add(Button_Cancel, input);

        input.gridx = 4;
        input.gridwidth = 2;
        input.gridheight = 1;
        JButton Button_Confirm = new JButton(Lang.T("Confirm"));
        this.add(Button_Confirm, input);
        Button_Confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onGoClick(Button_Confirm, feePow, person, endDate);
            }
        });

        //SHOW FRAME
        this.pack();
        this.setMaximizable(true);
        //this.setTitle(Lang.T("Persons"));
        //setPreferredSize(new Dimension(500, 600));
        this.setSize(new Dimension(500, 300));
        this.setClosable(true);
        this.setResizable(true);
        //this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
        this.setLocation(30, 20);

        //CLOSE
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        this.setResizable(true);
        this.setVisible(true);
    }

    private PersonCls refreshReceiverDetails(JTextField pubKeyTxt, JLabel pubKeyDetails) {
        String toValue = pubKeyTxt.getText();

        if (toValue.isEmpty()) {
            pubKeyDetails.setText("");
            return null;
        }

        long personKey = Long.parseLong(pubKeyTxt.getText());

        if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            pubKeyDetails.setText(Lang.T("Status must be OK to show public key details."));
            return null;
        }

        //CHECK IF RECIPIENT IS VALID ADDRESS
        PersonCls person = Controller.getInstance().getItemPerson(personKey);

        if (person == null) {
            // SHOW error message
            pubKeyDetails.setText(OnDealClick.resultMess(Transaction.ITEM_PERSON_NOT_EXIST));
        } else {
            // SHOW account for FEE asset
            String personDetails = person.toString() + "<br>";
            personDetails += person.getSkinColor() + ":" + person.getEyeColor() + ":" + person.getHairColor() + "<br>";
            personDetails += person.getHeight() + ":" + person.getBirthLatitude() + ":" + person.getBirthLongitude() + "<br>";

            // IF PERSON DEAD
            // TODO by PERSON
			/*
			Tuple5<Long, Long, byte[], Integer, Integer> aliveStatus = DLSet.getInstance().getPersonStatusMap().getItem(person.getKey(), StatusCls.ALIVE_KEY);
			if (aliveStatus == null) {}
			else if (aliveStatus != null && aliveStatus.c[0] == (byte)2)
			{
				if (false & aliveStatus.b == Long.MIN_VALUE)
					personDetails += "<br>Dead";
				else {
					long current_time = NTP.getTime();
					int daysLeft = (int)((aliveStatus.a - current_time) / 86400000l);
					personDetails += "<br>" + Lang.T("Died %days% days ago").replace("%days%", ""+daysLeft);
				}
			} else {
				// IF PERSON ALIVE
				if (aliveStatus.b == null || aliveStatus.b == Long.MAX_VALUE)
					personDetails += "<br>Alive";
				else {
					long current_time = NTP.getTime();
					int daysLeft = (int)((aliveStatus.a - current_time) / 86400000l);
					if (daysLeft < 0 ) personDetails += "<br>" + Lang.T("Person died %days% ago days ago").replace("%days%", ""+daysLeft);
					else personDetails += "<br>" + Lang.T("Person is still alive %days%").replace("%days%", ""+daysLeft);
				}
			}
			*/

            pubKeyDetails.setText("<html>" + personDetails + "</html>");

        }

        return person;
    }

    public void onGoClick(JButton Button_Confirm, JTextField feePowTxt,
                          PersonCls person, JTextField toDateTxt) {

        if (!OnDealClick.proccess1(Button_Confirm)) return;

        Account creator = (Account) this.accountLBox.getSelectedItem();
        //String address = pubKey1Txt.getText();
        //Long begDate = null;
        Long endDate = null;
        int feePow = 0;
        int parse = 0;
        String toDateStr = toDateTxt.getText();
        try {

            //READ FEE POW
            feePow = Integer.parseInt(feePowTxt.getText());

        } catch (Exception e) {
            if (parse == 0) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid fee"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }

        Pair<Integer, Long> endDateRes = ItemCls.resolveDateFromStr(toDateStr, NTP.getTime());
        if (endDateRes.getA() == -1) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid Date value"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            Button_Confirm.setEnabled(true);
            return;
        } else
            endDate = endDateRes.getB();

        //Account authenticator =  new Account(address);
        PrivateKeyAccount authenticator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(creator.getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }


        int version = 0; // without user signs
        int value_1 = 0;
        int value_2 = 0;
        byte[] data = null;//  new byte[]{2}; // set ALIVE status to DEAD
        long refParent = 0l;

        //Pair<Transaction, Integer> result = new Pair<Transaction, Integer>(null, 52);
        // TODO PERSON by
		/*
		Pair<Transaction, Integer> result = Controller.getInstance().r_SetStatusToItem(version, false, authenticator,
				feePow, StatusCls., person,
				//endDate, Long.MAX_VALUE,
				null, endDate,
				value_1, value_2, null, null,refParent, data
				);
		//CHECK VALIDATE MESSAGE
		if (result.getB() == Transaction.VALIDATE_OK) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.T("Person listed as dead"), Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);
			this.dispose();
		} else {
		
			JOptionPane.showMessageDialog(new JFrame(), Lang.T(OnDealClick.resultMess(result.getB())), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
		}
		*/

        //ENABLE
        Button_Confirm.setEnabled(true);

    }

}
