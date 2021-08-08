package org.erachain.gui.create;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.gui.Gui;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class NoWalletFrame extends JFrame {

    private JRadioButton createButton;
    private JRadioButton recoverButton;
    private Gui parent;
    private NoWalletFrame th;

    public NoWalletFrame(Gui parent) throws Exception {
        super(Controller.getInstance().getApplicationName(false) + " - " + Lang.T("No Wallet"));

        th = this;
        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        //PARENT
        this.parent = parent;

        //LAYOUT
        this.setLayout(new GridBagLayout());

        //PADDING
        ((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));

        //LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(5, 5, 5, 5);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.weightx = 1;
        labelGBC.gridwidth = 2;
        labelGBC.gridx = 0;

        //OPTIONS GBC
        GridBagConstraints optionsGBC = new GridBagConstraints();
        optionsGBC.insets = new Insets(5, 5, 5, 5);
        optionsGBC.fill = GridBagConstraints.NONE;
        optionsGBC.anchor = GridBagConstraints.NORTHWEST;
        optionsGBC.weightx = 1;
        optionsGBC.gridwidth = 2;
        optionsGBC.gridx = 0;
        optionsGBC.gridy = 2;

        //BUTTON GBC
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(5, 5, 0, 5);
        buttonGBC.fill = GridBagConstraints.NONE;
        buttonGBC.anchor = GridBagConstraints.NORTHWEST;
        buttonGBC.gridwidth = 1;
        buttonGBC.gridx = 0;

        //LABEL
        labelGBC.gridy = 0;
        JLabel label1 = new JLabel(Lang.T("No existing wallet was found."));
        this.add(label1, labelGBC);

        //LABEL
        labelGBC.gridy = 1;
        JLabel label2 = new JLabel(Lang.T("What would you like to do?"));
        this.add(label2, labelGBC);

        //ADD OPTIONS
        //	this.createButton = new JRadioButton(Lang.T("Create a new wallet."));
        //	this.createButton.setSelected(true);
        //	this.add(this.createButton, optionsGBC);
        // CREATE WALLET LABEL
        JLabel create_Label = new JLabel("<HTML><a href =''>" + Lang.T("Create a new wallet.") + " </a>");
        create_Label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.add(create_Label, optionsGBC);
        create_Label.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                // TODO Auto-generated method stub

                //OPEN CREATE WALLET FRAME
                th.setVisible(false);
                new LicenseJFrame(true, th, 1);

            }


        });


        optionsGBC.gridy = 3;
        //	this.recoverButton = new JRadioButton(Lang.T("Recover a wallet using an existing seed"));
        // 	this.add(this.recoverButton, optionsGBC);
        //
        // 	ButtonGroup group = new ButtonGroup();
        // 	group.add(this.createButton);
        // 	group.add(this.recoverButton);

        // CREATE WALLET LABEL
        JLabel recover_Label = new JLabel("<HTML><a href =''>" + Lang.T("Recover a wallet using an existing seed") + " </a>");
        recover_Label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.add(recover_Label, optionsGBC);
        recover_Label.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                // TODO Auto-generated method stub

                //OPEN CREATE WALLET FRAME
                th.setVisible(false);
                new LicenseJFrame(true, th, 2);


            }


        });


        optionsGBC.gridy = 4;
        //	this.recoverButton = new JRadioButton(Lang.T("Recover a wallet using an existing seed"));
        // 	this.add(this.recoverButton, optionsGBC);
        //
        // 	ButtonGroup group = new ButtonGroup();
        // 	group.add(this.createButton);
        // 	group.add(this.recoverButton);

        // CREATE WALLET LABEL
        JLabel dir_Label = new JLabel("<HTML><a href =''>" + Lang.T("Open Wallet") + " </a>");
        dir_Label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.add(dir_Label, optionsGBC);
        dir_Label.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                // TODO Auto-generated method stub

                //OPEN CREATE WALLET FRAME
                th.setVisible(false);
                new LicenseJFrame(true, th, 3);

            }


        });


        //BUTTON NEXT
        buttonGBC.gridy = 5;
        buttonGBC.gridx = 1;
        JButton nextButton = new JButton(Lang.T("Next"));
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onNextClick();
            }
        });
        //       nextButton.setPreferredSize(new Dimension(80, 25));
        //   	this.add(nextButton, buttonGBC);

        //BUTTON CANCEL
        buttonGBC.gridx = 0;
        buttonGBC.gridy = 5;
        JButton cancelButton = new JButton(Lang.T("Cancel"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancelClick();
            }
        });
        //       cancelButton.setPreferredSize(new Dimension(80, 25));
        //   	this.add(cancelButton, buttonGBC);

        //CLOSE NICELY
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Controller.getInstance().stopAndExit(0);
                //  	System.exit(0);
            }
        });

        //CALCULATE HEIGHT WIDTH
        this.pack();
        //    	this.setSize(500, this.getHeight());

        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void goAfterLicence(int createWallet) {
        // StartPool

        if (false && BlockChain.DEMO_MODE) {
            StartQuestion ss = new StartQuestion();
            ss.setVisible(true);
        }

        //
        if (createWallet == 1)
            new CreateWalletFrame(this);
        else if (createWallet == 2)
            new RecoverWalletFrame(this);
        else if (createWallet == 3) {
            // open file dialog
            int res = Controller.getInstance().loadWalletFromDir();
            if (res > 1) {
                JOptionPane.showMessageDialog(
                        new JFrame(), Lang.T("wallet does not exist") + "!",
                        "Error!",
                        JOptionPane.ERROR_MESSAGE);
                this.setVisible(true);
            } else {
                onWalletCreated();
                this.dispose();
            }
        }
    }

    public void onNextClick() {

	/*
        TemplateCls template = (TemplateCls) DCSet.getInstance().getItemTemplateMap().get(Controller.LICENSE_VERS);
        if (template == null) {
            // USE default LICENSE
            template = (TemplateCls) DCSet.getInstance().getItemTemplateMap().get(2l);
        }
        */

        if (createButton.isSelected()) {
            //OPEN CREATE WALLET FRAME
            this.setVisible(false);
            // new LicenseJFrame(template, true, this, true);
        }

        if (recoverButton.isSelected()) {
            //OPEN RECOVER WALLET FRAME
            this.setVisible(false);
            //  new LicenseJFrame(template, true, this, false);
        }
    }

    public void onCancelClick() {
        this.parent.onCancelCreateWallet();

        this.dispose();
    }

    public void onWalletCreated() {
        Controller.getInstance().forgingStatusChanged(Controller.getInstance().getForgingStatus());

        this.parent.onWalletCreated();

        this.dispose();
    }
}
