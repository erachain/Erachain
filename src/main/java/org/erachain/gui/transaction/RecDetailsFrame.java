package org.erachain.gui.transaction;

import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.exLink.ExLinkAuthor;
import org.erachain.core.exdata.exLink.ExLinkSource;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.items.persons.SearchPersonsSplitPanel;
import org.erachain.gui.items.records.SearchTransactionsSplitPanel;
import org.erachain.gui.items.templates.SearchTemplatesSplitPanel;
import org.erachain.gui.library.MAccoutnTextField;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class RecDetailsFrame extends JPanel //JFrame
{

    public GridBagConstraints labelGBC;
    public GridBagConstraints fieldGBC;
    public JTextField signature;
    protected Transaction transaction;
    protected JTree linksTree;

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
        //this.setIconImages(icons);

        //CLOSE
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //LAYOUT
        this.setLayout(new GridBagLayout());

        //PADDING
//		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));

        //LABEL GBC
        labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(10, 5, 5, 5);
        //labelGBC.fill = GridBagConstraints.;
        labelGBC.anchor = GridBagConstraints.FIRST_LINE_START;//..NORTHWEST;
        labelGBC.weightx = 0.1;
        labelGBC.weighty = 0;

        //DETAIL GBC
        fieldGBC = new GridBagConstraints();
        fieldGBC.insets = new Insets(10, 5, 5, 5);
        fieldGBC.fill = GridBagConstraints.HORIZONTAL;
        fieldGBC.anchor = GridBagConstraints.FIRST_LINE_START;//.NORTHWEST;
        fieldGBC.gridwidth = 3;
        fieldGBC.gridx = 1;
        fieldGBC.weightx = 0.9;
        fieldGBC.weighty = 0;


        int componentLevel = 0;

        //LABEL Height + Seq
        labelGBC.gridy = componentLevel;
        JLabel heSeqLabel = new JLabel(Lang.T("Short Info") + ":");
        this.add(heSeqLabel, labelGBC);

        //Height + Seq
        fieldGBC.gridy = componentLevel++;
        JTextField shortInfo = new JTextField(DateTimeFormat.timestamptoString(transaction.getTimestamp())
                + " [" + transaction.viewHeightSeq() + " "
                + String.valueOf(transaction.getDataLength(Transaction.FOR_NETWORK, true)) + "^" + String.valueOf(transaction.getFeePow())
                + "=" + transaction.getFee() //+ ">>" + core.item.assets.AssetCls.FEE_ABBREV
                + ">>" + transaction.getConfirmations(dcSet));
        shortInfo.setEditable(false);
//		MenuPopupUtil.installContextMenu(shortInfo);
        this.add(shortInfo, fieldGBC);

        if (transaction.getCreator() != null) {

            //LABEL CREATOR
            componentLevel++;
            labelGBC.gridy = componentLevel;
            JLabel creatorLabel = new JLabel(Lang.T("Creator") + ":");
            this.add(creatorLabel, labelGBC);

            //CREATOR
            fieldGBC.gridy = componentLevel;
            MAccoutnTextField creator = new MAccoutnTextField(transaction.getCreator());

            creator.setEditable(false);

            this.add(creator, fieldGBC);

            String personStr = transaction.getCreator().viewPerson();
            if (personStr.length() > 0) {
                //LABEL PERSON
                componentLevel++;
                fieldGBC.gridy = componentLevel;
                //		this.add(new JLabel(personStr), detailGBC);
            }

            //LABEL CREATOR PUBLIC KEY
            componentLevel++;
            labelGBC.gridy = componentLevel;
            JLabel creator_Pub_keyLabel = new JLabel(Lang.T("Creator Public Key") + ":");
            //	this.add(creator_Pub_keyLabel, labelGBC);

            //CREATOR
            fieldGBC.gridy = componentLevel;

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
            fieldGBC.gridy = componentLevel;
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

        linksTree(shortInfo);

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

    public void linksTree(JComponent component) {

        DefaultMutableTreeNode rootLinkTree = transaction.viewLinksTree();
        if (rootLinkTree == null)
            return;

        linksTree = new JTree(rootLinkTree);
        linksTree.setBackground(component.getBackground());

        linksTree.setToggleClickCount(1);
        ++labelGBC.gridy;
        JLabel linksLabel = new JLabel(Lang.T("Links # Связи") + ":");
        this.add(linksLabel, labelGBC);

        fieldGBC.gridy = labelGBC.gridy;
        add(linksTree, fieldGBC);

        linksTree.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                // TODO Auto-generated method stub
                if (arg0.getClickCount() == 2) {

                    Component component = arg0.getComponent();
                    if (component instanceof JTree) {
                        JTree jTree = ((JTree) component);
                        if (jTree.getLastSelectedPathComponent() == null)
                            return;

                        Object obj = ((DefaultMutableTreeNode) jTree.getLastSelectedPathComponent()).getUserObject();
                        if (obj instanceof Transaction) {
                            String seqNo = ((Transaction) obj).viewHeightSeq();
                            SearchTransactionsSplitPanel panel = new SearchTransactionsSplitPanel();
                            panel.transactionsTableModel.clear();
                            panel.searchTextField2.setText(seqNo);
                            panel.transactionsTableModel.setBlockNumber(seqNo);
                            String title = Lang.T("Link # Связь");
                            SplitPanel oldPanel = (SplitPanel) MainPanel.getInstance().getTabComponent(title);
                            if (oldPanel != null)
                                panel.setDividerParameters(oldPanel.getDividerParameters());
                            MainPanel.getInstance().insertNewTab(title, panel);
                            panel.jTableJScrollPanelLeftPanel.addRowSelectionInterval(0, 0);

                        } else if (obj instanceof ExLinkSource) {
                            long dbRef = ((ExLinkSource) obj).getRef();
                            String seqNo = Transaction.viewDBRef(dbRef);
                            SearchTransactionsSplitPanel panel = new SearchTransactionsSplitPanel();
                            panel.transactionsTableModel.clear();
                            panel.searchTextField2.setText(seqNo);
                            panel.transactionsTableModel.setBlockNumber(seqNo);
                            String title = Lang.T("Link # Связь");
                            SplitPanel oldPanel = (SplitPanel) MainPanel.getInstance().getTabComponent(title);
                            if (oldPanel != null)
                                panel.setDividerParameters(oldPanel.getDividerParameters());
                            MainPanel.getInstance().insertNewTab(title, panel);
                            panel.jTableJScrollPanelLeftPanel.addRowSelectionInterval(0, 0);

                        } else if (obj instanceof TemplateCls) {
                            long key = ((TemplateCls) obj).getKey();
                            SearchTemplatesSplitPanel panel = new SearchTemplatesSplitPanel();
                            panel.itemKey.setText("" + key);
                            panel.startSearchKey();
                            MainPanel.getInstance().insertNewTab(Lang.T("Template"), panel);

                        } else if (obj instanceof ExLinkAuthor) {
                            long key = ((ExLinkAuthor) obj).getRef();
                            SearchPersonsSplitPanel panel = new SearchPersonsSplitPanel();
                            panel.itemKey.setText("" + key);
                            panel.startSearchKey();
                            MainPanel.getInstance().insertNewTab(Lang.T("Author"), panel);
                        }
                    }
                }
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

            }

        });

        linksTree.setCellRenderer(new MyCellRenderer());

    }

    public class MyCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Color getBackgroundNonSelectionColor() {
            return null;
        }

        @Override
        public Color getBackgroundSelectionColor() {
            //color = getTex
            return new Color(90, 90, 90); //Color.GREEN;
        }

        @Override
        public Color getBackground() {
            return null;
        }

        //@Override
        //public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
        //    final Component ret = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        ///final DefaultMutableTreeNode node = ((DefaultMutableTreeNode) (value));
        //this.setText(value.toString());
        //return ret;
        //}
    }
}
