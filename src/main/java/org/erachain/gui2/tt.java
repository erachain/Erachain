package org.erachain.gui2;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.gui.IconPanel;
import org.erachain.gui.Wallets.WalletsManagerSplitPanel;
import org.erachain.gui.bank.IssueSendPaymentOrder;
import org.erachain.gui.bank.MyOrderPaymentsSplitPanel;
import org.erachain.gui.items.accounts.FavoriteAccountsSplitPanel;
import org.erachain.gui.items.accounts.MyAccountsSplitPanel;
import org.erachain.gui.items.accounts.MyLoansSplitPanel;
import org.erachain.gui.items.assets.*;
import org.erachain.gui.items.imprints.ImprintsFavoriteSplitPanel;
import org.erachain.gui.items.imprints.ImprintsSearchSplitPanel;
import org.erachain.gui.items.imprints.IssueImprintPanel;
import org.erachain.gui.items.imprints.MyImprintsTab;
import org.erachain.gui.items.link_hashes.IssueLinkedHashPanel;
import org.erachain.gui.items.mails.IncomingMailsSplitPanel;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.items.mails.OutcomingMailsSplitPanel;
import org.erachain.gui.items.other.OtherConsolePanel;
import org.erachain.gui.items.other.OtherSearchBlocks;
import org.erachain.gui.items.other.OtherSplitPanel;
import org.erachain.gui.items.persons.*;
import org.erachain.gui.items.polls.IssuePollPanel;
import org.erachain.gui.items.polls.PollsFavoriteSplitPanel;
import org.erachain.gui.items.polls.PollsMySplitPanel;
import org.erachain.gui.items.polls.SearchPollsSplitPanel;
import org.erachain.gui.items.records.FavoriteTransactionsSplitPanel;
import org.erachain.gui.items.records.MyTransactionsSplitPanel;
import org.erachain.gui.items.records.SearchTransactionsSplitPanel;
import org.erachain.gui.items.records.UnconfirmedTransactionsPanel;
import org.erachain.gui.items.statement.FavoriteStatementsSplitPanel;
import org.erachain.gui.items.statement.IssueDocumentPanel;
import org.erachain.gui.items.statement.SearchStatementsSplitPanel;
import org.erachain.gui.items.statement.StatementsMySplitPanel;
import org.erachain.gui.items.statuses.IssueStatusPanel;
import org.erachain.gui.items.statuses.SearchStatusesSplitPanel;
import org.erachain.gui.items.statuses.StatusesFavoriteSplitPanel;
import org.erachain.gui.items.templates.IssueTemplatePanel;
import org.erachain.gui.items.templates.SearchTemplatesSplitPanel;
import org.erachain.gui.items.templates.TemplateMySplitPanel;
import org.erachain.gui.items.templates.TemplatesFavoriteSplitPanel;
import org.erachain.gui.items.unions.IssueUnionPanel;
import org.erachain.gui.items.unions.MyUnionsTab;
import org.erachain.gui.items.unions.SearchUnionSplitPanel;
import org.erachain.gui.library.ASMutableTreeNode;
import org.erachain.gui.telegrams.ALLTelegramPanel;
import org.erachain.gui.telegrams.TelegramSplitPanel;
import org.erachain.gui2.lib.AS_tt_Render;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.TreeMap;

/**
 * @author DarkRaha
 */
public class tt extends JPanel { // implements TreeSelectionListener {

    private static final long serialVersionUID = 1L;
    public JTree tree;
    public JTextField jtf = new JTextField();

    public tt() {
        // ------------------------------------------
        // Container c = getContentPane();
        setLayout(new BorderLayout());

        // -------------------------------------------
        String pathIcons = Settings.getInstance().getPatnIcons();

        ASMutableTreeNode root = new ASMutableTreeNode("root", "<html><span style='font-size:1.1em;'><b>"
                + Controller.getInstance().APP_NAME, getIcon(
                Settings.getInstance().getUserPath() + "images" + File.separator + "icons" + File.separator + "favicon.png"));
        ASMutableTreeNode account_Node = new ASMutableTreeNode("account_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Accounts") + "</b></html>", getIcon(pathIcons + "account_Node.png"));
        ASMutableTreeNode person_Node = new ASMutableTreeNode("person_Node", "<html><span style='font-size:1.1em;'><b> " + Lang.getInstance().translate("Persons") + "</b></html>", getIcon(pathIcons + "person_Node.png"));
        ASMutableTreeNode document_Node = new ASMutableTreeNode("document_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Documents") + "</b></html>", getIcon(pathIcons + "document_Node.png"));
        ASMutableTreeNode mails_Node = new ASMutableTreeNode("mails_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Mails") + "</b></html>", getIcon(pathIcons + "mails_Node.png"));
        ASMutableTreeNode assets_Node = new ASMutableTreeNode("assets_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Assets") + "</b></html>", getIcon(pathIcons + "assets_Node.png"));
        ASMutableTreeNode exchange_Node = new ASMutableTreeNode("exchange_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Exchange") + "</b></html>", getIcon(pathIcons + "exchange_Node.png"));
        ASMutableTreeNode templates_Node = new ASMutableTreeNode("templates_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Templates") + "</b></html>", getIcon(pathIcons + "templates_Node.png"));
        ASMutableTreeNode statuses_Node = new ASMutableTreeNode("statuses_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Statuses") + "</b></html>", getIcon(pathIcons + "statuses_Node.png"));
        ASMutableTreeNode unions_Node = new ASMutableTreeNode("unions_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Unions") + "</b></html>", getIcon(pathIcons + "unions_Node.png"));
        ASMutableTreeNode polls_Node = new ASMutableTreeNode("polls_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Polls") + "</b></html>", getIcon(pathIcons + "polls_Node.png"));
        ASMutableTreeNode hashes_Node = new ASMutableTreeNode("hashes_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Unique Hashes") + "</b></html>", getIcon(pathIcons + "hashes_Node.png"));
        ASMutableTreeNode linked_hashes_Node = new ASMutableTreeNode("linked_hashes_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Linked Hashes") + "</b></html>", getIcon(pathIcons + "linked_hashes_Node.png"));
        ASMutableTreeNode records_Node = new ASMutableTreeNode("records_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Records") + "</b></html>", getIcon(pathIcons + "records_Node.png"));
        ASMutableTreeNode other_Node = new ASMutableTreeNode("other_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Network DashBoard") + "</b></html>", getIcon(pathIcons + "other_Node.png"));
        ASMutableTreeNode bank_Tree = new ASMutableTreeNode("bank_Tree", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Bank") + "</b></html>", getIcon(pathIcons + "bank_Tree.png"));
        ASMutableTreeNode telegrams_Tree = new ASMutableTreeNode("telegrams_Tree", "<html><span style='font-size:1.1em;'><b>" + Lang.getInstance().translate("Telegrams") + "</b></html>", getIcon(pathIcons + "telegrams_Tree.png"));

        TreeMap<String, DefaultMutableTreeNode> nodeList = new TreeMap<String, DefaultMutableTreeNode>();
        nodeList.put("account_Node", account_Node);
        nodeList.put("person_Node", person_Node);
        nodeList.put("document_Node", document_Node);
        nodeList.put("mails_Node", mails_Node);
        nodeList.put("assets_Node", assets_Node);
        nodeList.put("exchange_Node", exchange_Node);
        nodeList.put("templates_Node", templates_Node);
        nodeList.put("statuses_Node", statuses_Node);
        nodeList.put("unions_Node", unions_Node);
        nodeList.put("polls_Node", polls_Node);
        nodeList.put("hashes_Node", hashes_Node);
        nodeList.put("linked_hashes_Node", linked_hashes_Node);
        nodeList.put("records_Node", records_Node);
        nodeList.put("other_Node", other_Node);
        nodeList.put("bank_Tree", bank_Tree);
        nodeList.put("telegrams_Tree", telegrams_Tree);


        if (BlockChain.TEST_MODE)
            root.add(bank_Tree);

        root.add(account_Node);
        root.add(person_Node);
        root.add(document_Node);
        root.add(mails_Node);
        root.add(assets_Node);
        root.add(exchange_Node);
        root.add(templates_Node);
        root.add(statuses_Node);

        if (BlockChain.TEST_MODE)
            root.add(unions_Node);

        root.add(polls_Node);

        root.add(hashes_Node);
        root.add(linked_hashes_Node);

        if (true || BlockChain.TEST_MODE)
            root.add(telegrams_Tree);

        root.add(records_Node);
        root.add(other_Node);

        bank_Tree.add(new ASMutableTreeNode("IssueSendPaymentOrder", IssueSendPaymentOrder.TITLE));
        bank_Tree.add(new ASMutableTreeNode("MyOrderPaymentsSplitPanel", MyOrderPaymentsSplitPanel.TITLE));

        account_Node.add(new ASMutableTreeNode("MyAccountsSplitPanel", MyAccountsSplitPanel.TITLE));
        account_Node.add(new ASMutableTreeNode("MyLoansSplitPanel", MyLoansSplitPanel.TITLE));
        account_Node.add(new ASMutableTreeNode("FavoriteAccountsSplitPanel", FavoriteAccountsSplitPanel.TITLE));

        // account_Node.add(new
        // DefaultMutableTreeNode(Lang.getInstance().translate("Search
        // Accounts")));
        // account_Node.add(new DefaultMutableTreeNode("Issue Account"));

        person_Node.add(new ASMutableTreeNode("PersonsFavoriteSplitPanel", PersonsFavoriteSplitPanel.TITLE));
        person_Node.add(new ASMutableTreeNode("PersonsMySplitPanel", PersonsMySplitPanel.TITLE));
        person_Node.add(new ASMutableTreeNode("SearchPersonsSplitPanel", SearchPersonsSplitPanel.TITLE));
        person_Node.add(new ASMutableTreeNode("IssuePersonPanel", IssuePersonPanel.TITLE));
        person_Node.add(new ASMutableTreeNode("InsertPersonPanel", InsertPersonPanel.TITLE));

        document_Node.add(new ASMutableTreeNode("FavoriteStatementsSplitPanel", FavoriteStatementsSplitPanel.TITLE));
        document_Node.add(new ASMutableTreeNode("StatementsMySplitPanel", StatementsMySplitPanel.TITLE));
        document_Node.add(new ASMutableTreeNode("SearchStatementsSplitPanel", SearchStatementsSplitPanel.TITLE));
        document_Node.add(new ASMutableTreeNode("IssueDocumentPanel", IssueDocumentPanel.TITLE));

        mails_Node.add(new ASMutableTreeNode("IncomingMailsSplitPanel", IncomingMailsSplitPanel.TITLE));
        mails_Node.add(new ASMutableTreeNode("OutcomingMailsSplitPanel", OutcomingMailsSplitPanel.TITLE));
        mails_Node.add(new ASMutableTreeNode("MailSendPanel", MailSendPanel.TITLE));

        assets_Node.add(new ASMutableTreeNode("AssetsFavoriteSplitPanel", AssetsFavoriteSplitPanel.TITLE));
        assets_Node.add(new ASMutableTreeNode("AssetsMySplitPanel", AssetsMySplitPanel.TITLE));
        assets_Node.add(new ASMutableTreeNode("SearchAssetsSplitPanel", SearchAssetsSplitPanel.TITLE));
        assets_Node.add(new ASMutableTreeNode("IssueAssetPanel", IssueAssetPanel.TITLE));
        assets_Node.add(new ASMutableTreeNode("MyBalanceTab", MyBalanceTab.TITLE));

        if (Settings.EXCHANGE_IN_OUT) {
            exchange_Node.add(new ASMutableTreeNode("DepositExchange", DepositExchange.TITLE));
            exchange_Node.add(new ASMutableTreeNode("WithdrawExchange", WithdrawExchange.TITLE));
        }
        exchange_Node.add(new ASMutableTreeNode("ExchangePanel", ExchangePanel.TITLE));
        exchange_Node.add(new ASMutableTreeNode("MyOrderTab", MyOrderTab.TITLE));

        templates_Node.add(new ASMutableTreeNode("TemplatesFavoriteSplitPanel", TemplatesFavoriteSplitPanel.TITLE));
        templates_Node.add(new ASMutableTreeNode("TemplateMySplitPanel", TemplateMySplitPanel.TITLE));
        templates_Node.add(new ASMutableTreeNode("SearchTemplatesSplitPanel", SearchTemplatesSplitPanel.TITLE));
        templates_Node.add(new ASMutableTreeNode("IssueTemplatePanel", IssueTemplatePanel.TITLE));


        statuses_Node.add(new ASMutableTreeNode("StatusesFavoriteSplitPanel", StatusesFavoriteSplitPanel.TITLE));
        statuses_Node.add(new ASMutableTreeNode("SearchStatusesSplitPanel", SearchStatusesSplitPanel.TITLE));
        statuses_Node.add(new ASMutableTreeNode("IssueStatusPanel", IssueStatusPanel.TITLE));

        unions_Node.add(new ASMutableTreeNode("MyUnionsTab", MyUnionsTab.TITLE));
        unions_Node.add(new ASMutableTreeNode("SearchUnionSplitPanel", SearchUnionSplitPanel.TITLE));
        unions_Node.add(new ASMutableTreeNode("IssueUnionPanel", IssueUnionPanel.TITLE));

        polls_Node.add(new ASMutableTreeNode("PollsFavoriteSplitPanel", PollsFavoriteSplitPanel.TITLE));
        polls_Node.add(new ASMutableTreeNode("Polls_My_SplitPanel", PollsMySplitPanel.TITLE));
        polls_Node.add(new ASMutableTreeNode("SearchPollsSplitPanel", SearchPollsSplitPanel.TITLE));
        polls_Node.add(new ASMutableTreeNode("IssuePollPanel", IssuePollPanel.TITLE));

        records_Node.add(new ASMutableTreeNode("FavoriteTransactionsSplitPanel", FavoriteTransactionsSplitPanel.TITLE));
        records_Node.add(new ASMutableTreeNode("MyTransactionsSplitPanel", MyTransactionsSplitPanel.TITLE));
        records_Node.add(new ASMutableTreeNode("SearchTransactionsSplitPanel", SearchTransactionsSplitPanel.TITLE));
        records_Node.add(new ASMutableTreeNode("UnconfirmedTransactionsPanel", UnconfirmedTransactionsPanel.TITLE));

        hashes_Node.add(new ASMutableTreeNode("ImprintsFavoriteSplitPanel", ImprintsFavoriteSplitPanel.TITLE));
        hashes_Node.add(new ASMutableTreeNode("MyImprintsTab", MyImprintsTab.TITLE));
        hashes_Node.add(new ASMutableTreeNode("ImprintsSearchSplitPanel", ImprintsSearchSplitPanel.TITLE));
        hashes_Node.add(new ASMutableTreeNode("IssueImprintPanel", IssueImprintPanel.TITLE));

        linked_hashes_Node.add(new ASMutableTreeNode("IssueLinkedHashPanel", IssueLinkedHashPanel.TITLE));
        //linked_hashes_Node.add(new ASMutableTreeNode("SearchTransactionsSplitPanel", Lang.getInstance().translate("Search Linked Hash"), SearchTransactionsSplitPanel.TITLE));

        //telegram
        telegrams_Tree.add(new ASMutableTreeNode("TelegramSplitPanel", TelegramSplitPanel.TITLE));
        if (BlockChain.TEST_MODE)
            telegrams_Tree.add(new ASMutableTreeNode("ALLTelegramPanel", ALLTelegramPanel.TITLE));

        //////// OTHER
        other_Node.add(new ASMutableTreeNode("OtherSplitPanel", OtherSplitPanel.TITLE));
        if (BlockChain.TEST_MODE)
            other_Node.add(new ASMutableTreeNode("WalletsManagerSplitPanel", WalletsManagerSplitPanel.TITLE));

        other_Node.add(new ASMutableTreeNode("OtherConsolePanel", OtherConsolePanel.TITLE));
        other_Node.add(new ASMutableTreeNode("OtherSearchBlocks", OtherSearchBlocks.TITLE));

        tree = new JTree(root);
        tree.setCellRenderer(new AS_tt_Render());
        // tree.addTreeSelectionListener(this);
        double dd = (double) UIManager.getFont("TextField.font").getSize() * 180 / 100;
        tree.setRowHeight((int) dd);
        tree.setRootVisible(true);
        tree.setToggleClickCount(1);
        for (int i = 0; i < tree.getRowCount(); i++)
            tree.expandRow(i);

        // not collapse the root node
        tree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {

            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                if (((ASMutableTreeNode) event.getPath().getLastPathComponent()).isRoot()) {
                    tree.expandPath(event.getPath());
                }
            }
        });

        // menu
        JPopupMenu menu = new JPopupMenu("www");

        JMenuItem menuExpand = new JMenuItem(Lang.getInstance().translate("Expand All"));
        menuExpand.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                for (int i = 0; i < tree.getRowCount(); i++)
                    tree.expandRow(i);
            }
        });

        JMenuItem menuColapse = new JMenuItem(Lang.getInstance().translate("Collapse All"));
        menuColapse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                for (int i = 0; i < tree.getRowCount(); i++)
                    tree.collapseRow(i);
            }
        });
        menu.add(menuExpand);
        menu.add(menuColapse);

        menu.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void ancestorMoved(AncestorEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void ancestorRemoved(AncestorEvent arg0) {
                // TODO Auto-generated method stub

            }
        });

        tree.setComponentPopupMenu(menu);

        add(new JScrollPane(tree));
        add(jtf, BorderLayout.SOUTH);

        setVisible(true);

    }

    public static Image getIcon(String iconFile) {
        return IconPanel.getIcon(iconFile);
    }

}