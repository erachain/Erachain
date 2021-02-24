package org.erachain.gui.transaction;

import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.library.MAccoutnTextField;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class RecDetailsFrame extends JPanel //JFrame
{

    public GridBagConstraints labelGBC;
    public GridBagConstraints detailGBC;
    public JTextField signature;
    Transaction transaction;

    public RecDetailsFrame(Transaction transaction, boolean andSetup) {

        this.transaction = transaction;
        DCSet dcSet = DCSet.getInstance();
        this.transaction.setDC(dcSet, andSetup);

        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
//		this.setIconImages(icons);

        //CLOSE
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //LAYOUT
        this.setLayout(new GridBagLayout());

        //PADDING
//		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));

        //LABEL GBC
        labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(10, 5, 5, 5);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.FIRST_LINE_START;//..NORTHWEST;
        labelGBC.weightx = 0;
        labelGBC.gridx = 0;

        //DETAIL GBC
        detailGBC = new GridBagConstraints();
        detailGBC.insets = new Insets(10, 5, 5, 5);
        detailGBC.fill = GridBagConstraints.HORIZONTAL;
        detailGBC.anchor = GridBagConstraints.FIRST_LINE_START;//.NORTHWEST;
        detailGBC.weightx = 1;
        detailGBC.gridwidth = 3;
        detailGBC.gridx = 1;


        int componentLevel = 0;
		
		/*
		//LABEL TYPE
		labelGBC.gridy = componentLevel;
		
		JLabel typeLabel = new JLabel(Lang.T("Type") + ":");
		this.add(typeLabel, labelGBC);
		
		//TYPE
		detailGBC.gridy = componentLevel;
		JLabel type = new JLabel(Lang.T("Message Transaction"));
		this.add(type, detailGBC);
		componentLevel ++;
		*/

        //LABEL Height + Seq
        labelGBC.gridy = componentLevel;
        JLabel heSeqLabel = new JLabel(Lang.T("Short Info") + ":");
        this.add(heSeqLabel, labelGBC);

        //Height + Seq
        detailGBC.gridy = componentLevel++;
        JTextField shortInfo = new JTextField(DateTimeFormat.timestamptoString(transaction.getTimestamp())
                + " [" + transaction.viewHeightSeq() + " "
                + String.valueOf(transaction.getDataLength(Transaction.FOR_NETWORK, true)) + "^" + String.valueOf(transaction.getFeePow())
                + "=" + transaction.getFee() //+ ">>" + core.item.assets.AssetCls.FEE_ABBREV
                + ">>" + transaction.getConfirmations(dcSet));
        shortInfo.setEditable(false);
//		MenuPopupUtil.installContextMenu(shortInfo);
        this.add(shortInfo, detailGBC);

        if (transaction.getCreator() != null) {

            //LABEL CREATOR
            componentLevel++;
            labelGBC.gridy = componentLevel;
            JLabel creatorLabel = new JLabel(Lang.T("Creator") + ":");
            this.add(creatorLabel, labelGBC);

            //CREATOR
            detailGBC.gridy = componentLevel;
            MAccoutnTextField creator = new MAccoutnTextField(transaction.getCreator());

            creator.setEditable(false);

            this.add(creator, detailGBC);

            String personStr = transaction.getCreator().viewPerson();
            if (personStr.length() > 0) {
                //LABEL PERSON
                componentLevel++;
                detailGBC.gridy = componentLevel;
                //		this.add(new JLabel(personStr), detailGBC);
            }

            //LABEL CREATOR PUBLIC KEY
            componentLevel++;
            labelGBC.gridy = componentLevel;
            JLabel creator_Pub_keyLabel = new JLabel(Lang.T("Creator Publick Key") + ":");
            //	this.add(creator_Pub_keyLabel, labelGBC);

            //CREATOR
            detailGBC.gridy = componentLevel;

            JTextField creator_Pub_key = new JTextField(transaction.getCreator().getBase58());
            creator_Pub_key.setEditable(false);
            MenuPopupUtil.installContextMenu(creator_Pub_key);
            //	this.add(creator_Pub_key, detailGBC);

        }

        if (transaction.getSignature() != null) {
            componentLevel++;
            //LABEL SIGNATURE
            labelGBC.gridy = componentLevel;
            JLabel signatureLabel = new JLabel(Lang.T("Signature") + ":");
            //			this.add(signatureLabel, labelGBC);

            //SIGNATURE
            detailGBC.gridy = componentLevel;
            //JTextField signature = new JTextField(Base58.encode(record.getSignature()).substring(0, 12) + "..");
            signature = new JTextField(Base58.encode(transaction.getSignature()));
            signature.setEditable(false);
            MenuPopupUtil.installContextMenu(signature);
            //			this.add(signature, detailGBC);

        }
		
		/*
		//LABEL FEE POWER
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel feePowLabel = new JLabel(Lang.T("Size")+" & "+ Lang.T("Fee") + ":");
		this.add(feePowLabel, labelGBC);
						
		//FEE POWER
		detailGBC.gridy = componentLevel;
		JTextField feePow = new JTextField(
				String.valueOf(record.getDataLength(false)) + "^" + String.valueOf(record.getFeePow())
				+ "=" + record.getFeeLong() //+ ">>" + core.item.assets.AssetCls.FEE_ABBREV
				+ ">>" + record.getConfirmations(db));
		feePow.setEditable(false);
		MenuPopupUtil.installContextMenu(feePow);
		this.add(feePow, detailGBC);	
						
		//LABEL CONFIRMATIONS
		componentLevel ++;
		labelGBC.gridy = componentLevel;
		JLabel confirmationsLabel = new JLabel(Lang.T("Confirmations") + ":");
		this.add(confirmationsLabel, labelGBC);
								
		//CONFIRMATIONS
		detailGBC.gridy = componentLevel;
		JLabel confirmations = new JLabel(String.valueOf(record.getConfirmations(db)));
		this.add(confirmations, detailGBC);
		*/

        new JTextField(DateTimeFormat.timestamptoString(transaction.getTimestamp())

                + String.valueOf(transaction.getDataLength(Transaction.FOR_NETWORK, true)) + "^" + String.valueOf(transaction.getFeePow())
                + "=" + transaction.getFee() //+ ">>" + core.item.assets.AssetCls.FEE_ABBREV
                + ">>" + transaction.getConfirmations(dcSet));


        JPopupMenu shortInfoMeny = new JPopupMenu();

        JMenuItem copy_Transaction_Sign = new JMenuItem(Lang.T("Copy Signature"));
        copy_Transaction_Sign.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                StringSelection value;
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                if (transaction.getSignature() != null) {
                    value = new StringSelection(Base58.encode(transaction.getSignature()));
                } else {
                    value = new StringSelection("null");
                }
                clipboard.setContents(value, null);

            }
        });
        shortInfoMeny.add(copy_Transaction_Sign);

        JMenuItem copy_Heigt_Block = new JMenuItem(Lang.T("Copy Block"));
        copy_Heigt_Block.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(transaction.viewHeightSeq());
                clipboard.setContents(value, null);
            }
        });
        shortInfoMeny.add(copy_Heigt_Block);

        shortInfo.setComponentPopupMenu(shortInfoMeny);
    }

    public void linksTree() {
        detailGBC = new GridBagConstraints();
        detailGBC.gridx = 0;
        detailGBC.gridy = 2;
        detailGBC.fill = GridBagConstraints.BOTH;
        detailGBC.anchor = GridBagConstraints.FIRST_LINE_START;
        detailGBC.weightx = 0.3;
        detailGBC.weighty = 0.1;
        JTree tree = transaction.viewLinksTree();
        add(tree, detailGBC);


    }

}
