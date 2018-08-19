package gui.transaction;

import core.crypto.Base58;
import core.transaction.Genesis_Record;
import core.transaction.Transaction;
import datachain.DCSet;
import lang.Lang;
import utils.MenuPopupUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class RecGenesis_DetailsFrame extends JPanel //JFrame
{

    public GridBagConstraints labelGBC = new GridBagConstraints();
    public GridBagConstraints detailGBC = new GridBagConstraints();

    public RecGenesis_DetailsFrame(final Genesis_Record record) {
//		super(Lang.getInstance().translate(controller.Controller.APP_NAME) + " - " + Lang.getInstance().translate(record.viewTypeName()));

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
        labelGBC.insets = new Insets(0, 5, 5, 0);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.weightx = 0;
        labelGBC.gridx = 0;


        //DETAIL GBC
        detailGBC = new GridBagConstraints();
        detailGBC.insets = new Insets(0, 5, 5, 0);
        detailGBC.fill = GridBagConstraints.HORIZONTAL;
        detailGBC.anchor = GridBagConstraints.NORTHWEST;
        detailGBC.weightx = 1;
        detailGBC.gridwidth = 3;
        detailGBC.gridx = 1;


        int componentLevel = 0;

        //LABEL SIGNATURE
        labelGBC.gridy = componentLevel;
        JLabel signatureLabel = new JLabel(Lang.getInstance().translate("Signature") + ":");
        this.add(signatureLabel, labelGBC);

        //SIGNATURE
        detailGBC.gridy = componentLevel;
        JTextField signature = new JTextField(Base58.encode(record.getSignature()));
        signature.setEditable(false);
        MenuPopupUtil.installContextMenu(signature);
        this.add(signature, detailGBC);

        //LABEL SIZE
        componentLevel++;
        labelGBC.gridy = componentLevel;
        JLabel feePowLabel = new JLabel(Lang.getInstance().translate("Size") + ":");
        this.add(feePowLabel, labelGBC);

        //SIZE
        detailGBC.gridy = componentLevel;
        JTextField feePow = new JTextField(String.valueOf(record.getDataLength(Transaction.FOR_NETWORK, true)));
        feePow.setEditable(false);
        MenuPopupUtil.installContextMenu(feePow);
        this.add(feePow, detailGBC);

        //LABEL CONFIRMATIONS
        componentLevel++;
        labelGBC.gridy = componentLevel;
        JLabel confirmationsLabel = new JLabel(Lang.getInstance().translate("Confirmations") + ":");
        this.add(confirmationsLabel, labelGBC);

        //CONFIRMATIONS
        detailGBC.gridy = componentLevel;
        JLabel confirmations = new JLabel(String.valueOf(record.getConfirmations(DCSet.getInstance())));
        this.add(confirmations, detailGBC);

    }
}
