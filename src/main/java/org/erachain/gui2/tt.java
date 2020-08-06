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
import org.erachain.gui.items.polls.Polls_My_SplitPanel;
import org.erachain.gui.items.polls.SearchPollsSplitPanel;
import org.erachain.gui.items.records.FavoriteTransactionsSplitPanel;
import org.erachain.gui.items.records.MyTransactionsSplitPanel;
import org.erachain.gui.items.records.SearchTransactionsSplitPanel;
import org.erachain.gui.items.records.UnconfirmedTransactionsPanel;
import org.erachain.gui.items.statement.FavoriteStatementsSplitPanel;
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

        bank_Tree.add(new ASMutableTreeNode("IssueSendPaymentOrder",
                IssueSendPaymentOrder.TITLE, IconPanel.getIcon(IssueSendPaymentOrder.ICON)));
        bank_Tree.add(new ASMutableTreeNode("MyOrderPaimentsSplitPanel", MyOrderPaymentsSplitPanel.TI"My Payments Orders", MyOrderPaymentsSplitPanel.getIcon()));

        account_Node.add(new ASMutableTreeNode("MyAccountsSplitPanel", Lang.getInstance().translate("My Accounts"), MyAccountsSplitPanel.getIcon()));
        account_Node.add(new ASMutableTreeNode("MyLoansSplitPanel", Lang.getInstance().translate("My Loans"), MyLoansSplitPanel.getIcon()));
        account_Node.add(new ASMutableTreeNode("FavoriteAccountsSplitPanel", Lang.getInstance().translate("Favorite Accounts"), FavoriteAccountsSplitPanel.getIcon()));

        // account_Node.add(new
        // DefaultMutableTreeNode(Lang.getInstance().translate("Search
        // Accounts")));
        // account_Node.add(new DefaultMutableTreeNode("Issue Account"));

        person_Node.add(new ASMutableTreeNode("PersonsFavoriteSplitPanel", Lang.getInstance().translate("Favorite Persons"), PersonsFavoriteSplitPanel.getIcon()));
        person_Node.add(new ASMutableTreeNode("PersonsMySplitPanel", Lang.getInstance().translate("My Persons"), PersonsMySplitPanel.getIcon()));
        person_Node.add(new ASMutableTreeNode("SearchPersonsSplitPanel", Lang.getInstance().translate("Search Persons"), SearchPersonsSplitPanel.getIcon()));
        person_Node.add(new ASMutableTreeNode("IssuePersonPanel", Lang.getInstance().translate("Issue Person"), IssuePersonPanel.getIcon()));
        person_Node.add(new ASMutableTreeNode("InsertPersonPanel", Lang.getInstance().translate("Insert Person"), InsertPersonPanel.getIcon()));

        document_Node.add(new ASMutableTreeNode("FavoriteStatementsSplitPanel", Lang.getInstance().translate("Favorite Documents"), FavoriteStatementsSplitPanel.getIcon()));
        document_Node.add(new ASMutableTreeNode("StatementsMySplitPanel", Lang.getInstance().translate("My Documents"), StatementsMySplitPanel.getIcon()));
        document_Node.add(new ASMutableTreeNode("SearchStatementsSplitPanel", Lang.getInstance().translate("Search Documents"), SearchStatementsSplitPanel.getIcon()));
        //document_Node.add(new ASMutableTreeNode("IssueDocumentPanel", Lang.getInstance().translate("Issue Document"), IssueDocumentPanel.getIcon()));
        document_Node.add(new ASMutableTreeNode("IssueDocumentPanel", Lang.getInstance().translate("Issue Document"),
                IconPanel.getIcon(IssueDocumentPanel)));

        mails_Node.add(new ASMutableTreeNode("IncomingMailsSplitPanel", Lang.getInstance().translate("Incoming Mails"), IncomingMailsSplitPanel.getIcon()));
        mails_Node.add(new ASMutableTreeNode("OutcomingMailsSplitPanel", Lang.getInstance().translate("Outcoming Mails"), OutcomingMailsSplitPanel.getIcon()));
        mails_Node.add(new ASMutableTreeNode("MailSendPanel", Lang.getInstance().translate("Send Mail"), MailSendPanel.getIcon()));

        assets_Node.add(new ASMutableTreeNode("AssetsFavoriteSplitPanel", Lang.getInstance().translate("Favorite Assets"), AssetsFavoriteSplitPanel.getIcon()));
        assets_Node.add(new ASMutableTreeNode("AssetsMySplitPanel", Lang.getInstance().translate("My Assets"), AssetsMySplitPanel.getIcon()));
        assets_Node.add(new ASMutableTreeNode("SearchAssetsSplitPanel", Lang.getInstance().translate("Search Assets"), SearchAssetsSplitPanel.getIcon()));
        assets_Node.add(new ASMutableTreeNode("IssueAssetPanel", Lang.getInstance().translate("Issue Asset"), IssueAssetPanel.getIcon()));
        assets_Node.add(new ASMutableTreeNode("MyBalanceTab", Lang.getInstance().translate("My Balance"), MyBalanceTab.getIcon()));

        if (Settings.EXCHANGE_IN_OUT) {
            exchange_Node.add(new ASMutableTreeNode("DepositExchange", Lang.getInstance().translate("Deposit Exchange"), DepositExchange.getIcon()));
            exchange_Node.add(new ASMutableTreeNode("WithdrawExchange", Lang.getInstance().translate("Withdraw Exchange"), WithdrawExchange.getIcon()));
        }
        exchange_Node.add(new ASMutableTreeNode("ExchangePanel", Lang.getInstance().translate("Exchange"), ExchangePanel.getIcon()));
        exchange_Node.add(new ASMutableTreeNode("MyOrderTab", Lang.getInstance().translate("My Orders"), MyOrderTab.getIcon()));

        templates_Node.add(new ASMutableTreeNode("TemplatesFavoriteSplitPanel", Lang.getInstance().translate("Favorite Templates"), TemplatesFavoriteSplitPanel.getIcon()));
        templates_Node.add(new ASMutableTreeNode("TemplateMySplitPanel", Lang.getInstance().translate("My Templates"), TemplateMySplitPanel.getIcon()));
        templates_Node.add(new ASMutableTreeNode("SearchTemplatesSplitPanel", Lang.getInstance().translate("Search Templates"), SearchTemplatesSplitPanel.getIcon()));
        templates_Node.add(new ASMutableTreeNode("IssueTemplatePanel", Lang.getInstance().translate("Issue Template"), IssueTemplatePanel.getIcon()));


        statuses_Node.add(new ASMutableTreeNode("StatusesFavoriteSplitPanel", Lang.getInstance().translate("Favorite Statuses"), StatusesFavoriteSplitPanel.getIcon()));
        statuses_Node.add(new ASMutableTreeNode("SearchStatusesSplitPanel", Lang.getInstance().translate("Search Statuses"), SearchStatusesSplitPanel.getIcon()));
        statuses_Node.add(new ASMutableTreeNode("IssueStatusPanel", Lang.getInstance().translate("Create Status"), IssueStatusPanel.getIcon()));

        unions_Node.add(new ASMutableTreeNode("MyUnionsTab", Lang.getInstance().translate("My Unions"), MyUnionsTab.getIcon()));
        unions_Node.add(new ASMutableTreeNode("SearchUnionSplitPanel", Lang.getInstance().translate("Search Unions"), SearchUnionSplitPanel.getIcon()));
        unions_Node.add(new ASMutableTreeNode("IssueUnionPanel", Lang.getInstance().translate("Issue Union"), IssueUnionPanel.getIcon()));

        polls_Node.add(new ASMutableTreeNode("PollsFavoriteSplitPanel", Lang.getInstance().translate("Favorite Polls"), PollsFavoriteSplitPanel.getIcon()));
        polls_Node.add(new ASMutableTreeNode("Polls_My_SplitPanel", Lang.getInstance().translate("My Polls"), Polls_My_SplitPanel.getIcon()));
        polls_Node.add(new ASMutableTreeNode("SearchPollsSplitPanel", Lang.getInstance().translate("Search Polls"), SearchPollsSplitPanel.getIcon()));
        polls_Node.add(new ASMutableTreeNode("IssuePollPanel", Lang.getInstance().translate("Issue Poll"), IssuePollPanel.getIcon()));

        records_Node.add(new ASMutableTreeNode("FavoriteTransactionsSplitPanel", Lang.getInstance().translate("Favorite Records"), FavoriteTransactionsSplitPanel.getIcon()));
        records_Node.add(new ASMutableTreeNode("MyTransactionsSplitPanel", Lang.getInstance().translate("My Records"), MyTransactionsSplitPanel.getIcon()));
        records_Node.add(new ASMutableTreeNode("SearchTransactionsSplitPanel", Lang.getInstance().translate("Search Records"), SearchTransactionsSplitPanel.getIcon()));
        records_Node.add(new ASMutableTreeNode("UnconfirmedTransactionsPanel", Lang.getInstance().translate("Unconfirmed Records"), UnconfirmedTransactionsPanel.getIcon()));

        other_Node.add(new ASMutableTreeNode("OtherSplitPanel", Lang.getInstance().translate("Other"), OtherSplitPanel.getIcon()));
        if (BlockChain.TEST_MODE)
            other_Node.add(new ASMutableTreeNode("WalletsManagerSplitPanel", Lang.getInstance().translate("Wallets Manager"), WalletsManagerSplitPanel.getIcon()));

        other_Node.add(new ASMutableTreeNode("OtherConsolePanel", Lang.getInstance().translate("Console"), OtherConsolePanel.getIcon()));
        other_Node.add(new ASMutableTreeNode("OtherSearchBlocks", Lang.getInstance().translate("Blocks"), OtherSearchBlocks.getIcon()));

        hashes_Node.add(new ASMutableTreeNode("ImprintsFavoriteSplitPanel", Lang.getInstance().translate("Favorite Unique Hashes"), ImprintsFavoriteSplitPanel.getIcon()));
        hashes_Node.add(new ASMutableTreeNode("MyImprintsTab", Lang.getInstance().translate("My Unique Hashes"), MyImprintsTab.getIcon()));
        hashes_Node.add(new ASMutableTreeNode("ImprintsSearchSplitPanel", Lang.getInstance().translate("Search Unique Hashes"), ImprintsSearchSplitPanel.getIcon()));
        hashes_Node.add(new ASMutableTreeNode("IssueImprintPanel", Lang.getInstance().translate("Issue Unique Hash"), IssueImprintPanel.getIcon()));

        linked_hashes_Node.add(new ASMutableTreeNode("IssueLinkedHashPanel", Lang.getInstance().translate("Issue Linked Hash"), IssueLinkedHashPanel.getIcon()));
        //linked_hashes_Node.add(new ASMutableTreeNode("SearchLinkedHash", Lang.getInstance().translate("Search Linked Hash"), SearchLinkedHash.getIcon()));
        linked_hashes_Node.add(new ASMutableTreeNode("SearchTransactionsSplitPanel", Lang.getInstance().translate("Search Linked Hash"), SearchTransactionsSplitPanel.getIcon()));

        //telegram
        telegrams_Tree.add(new ASMutableTreeNode("TelegramSplitPanel", Lang.getInstance().translate("Telegrams Panel"), TelegramSplitPanel.getIcon()));
        if (BlockChain.TEST_MODE)
            telegrams_Tree.add(new ASMutableTreeNode("ALLTelegramPanel", Lang.getInstance().translate("All Telegrams Panel"), ALLTelegramPanel.getIcon()));

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
        IconPanel.getIcon(iconFile);
    }

}