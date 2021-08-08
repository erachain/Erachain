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
import org.erachain.gui.items.statuses.MyStatusesTab;
import org.erachain.gui.items.statuses.SearchStatusesSplitPanel;
import org.erachain.gui.items.statuses.StatusesFavoriteSplitPanel;
import org.erachain.gui.items.templates.IssueTemplatePanel;
import org.erachain.gui.items.templates.SearchTemplatesSplitPanel;
import org.erachain.gui.items.templates.TemplateMySplitPanel;
import org.erachain.gui.items.templates.TemplatesFavoriteSplitPanel;
import org.erachain.gui.items.unions.IssueUnionPanel;
import org.erachain.gui.items.unions.MyUnionsTab;
import org.erachain.gui.items.unions.SearchUnionSplitPanel;
import org.erachain.gui.items.unions.UnionsFavoriteSplitPanel;
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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author DarkRaha
 */
public class MainTreeMenu extends JPanel { // implements TreeSelectionListener {

    private static final long serialVersionUID = 1L;
    public JTree tree;
    public JTextField jtf = new JTextField();

    public MainTreeMenu() {
        // ------------------------------------------
        // Container c = getContentPane();
        setLayout(new BorderLayout());

        // -------------------------------------------
        String pathIcons = Settings.getInstance().getPatnIcons();

        ASMutableTreeNode root = new ASMutableTreeNode("root", "<html><span style='font-size:1.1em;'><b>"
                + Controller.getInstance().APP_NAME, getIcon(
                Settings.getInstance().getUserPath() + "images" + File.separator + "icons" + File.separator + "favicon.png"));
        ASMutableTreeNode account_Node = new ASMutableTreeNode("account_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.T("Accounts") + "</b></html>", getIcon(pathIcons + "account_Node.png"));
        ASMutableTreeNode person_Node = new ASMutableTreeNode("person_Node", "<html><span style='font-size:1.1em;'><b> " + Lang.T("Persons") + "</b></html>", getIcon(pathIcons + "person_Node.png"));
        ASMutableTreeNode document_Node = new ASMutableTreeNode("document_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.T("Documents") + "</b></html>", getIcon(pathIcons + "document_Node.png"));
        ASMutableTreeNode mails_Node = new ASMutableTreeNode("mails_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.T("Mails") + "</b></html>", getIcon(pathIcons + "mails_Node.png"));
        ASMutableTreeNode assets_Node = new ASMutableTreeNode("assets_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.T("Assets") + "</b></html>", getIcon(pathIcons + "assets_Node.png"));
        ASMutableTreeNode exchange_Node = new ASMutableTreeNode("exchange_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.T("Exchange") + "</b></html>", getIcon(pathIcons + "exchange_Node.png"));
        ASMutableTreeNode templates_Node = new ASMutableTreeNode("templates_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.T("Templates") + "</b></html>", getIcon(pathIcons + "templates_Node.png"));
        ASMutableTreeNode statuses_Node = new ASMutableTreeNode("statuses_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.T("Statuses") + "</b></html>", getIcon(pathIcons + "statuses_Node.png"));
        ASMutableTreeNode unions_Node = new ASMutableTreeNode("unions_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.T("Unions") + "</b></html>", getIcon(pathIcons + "unions_Node.png"));
        ASMutableTreeNode polls_Node = new ASMutableTreeNode("polls_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.T("Polls") + "</b></html>", getIcon(pathIcons + "polls_Node.png"));
        ASMutableTreeNode hashes_Node = new ASMutableTreeNode("hashes_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.T("Unique Hashes") + "</b></html>", getIcon(pathIcons + "hashes_Node.png"));
        ASMutableTreeNode linked_hashes_Node = new ASMutableTreeNode("linked_hashes_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.T("Linked Hashes") + "</b></html>", getIcon(pathIcons + "linked_hashes_Node.png"));
        ASMutableTreeNode records_Node = new ASMutableTreeNode("records_Node", "<html><span style='font-size:1.1em;'><b>" + Lang.T("Transactions") + "</b></html>", getIcon(pathIcons + "records_Node.png"));
        ASMutableTreeNode other_Node = new ASMutableTreeNode("other_Node", "<html><span style='font-size:1.1em;'><b>"
                + Lang.T("Network DashBoard") + "</b></html>", getIcon(pathIcons + "other_Node.png"));
        ASMutableTreeNode bank_Tree = new ASMutableTreeNode("bank_Tree", "<html><span style='font-size:1.1em;'><b>" + Lang.T("Bank") + "</b></html>", getIcon(pathIcons + "bank_Tree.png"));
        ASMutableTreeNode telegrams_Tree = new ASMutableTreeNode("telegrams_Tree", "<html><span style='font-size:1.1em;'><b>" + Lang.T("Telegrams") + "</b></html>", getIcon(pathIcons + "telegrams_Tree.png"));

        if (BlockChain.TEST_MODE)
            root.add(bank_Tree);

        root.add(account_Node);
        root.add(telegrams_Tree);
        root.add(records_Node);
        root.add(mails_Node);
        root.add(document_Node);
        root.add(exchange_Node);
        root.add(assets_Node);
        root.add(person_Node);
        root.add(polls_Node);
        root.add(templates_Node);
        root.add(statuses_Node);

        if (BlockChain.TEST_MODE)
            root.add(unions_Node);


        root.add(hashes_Node);
        root.add(linked_hashes_Node);

        root.add(other_Node);

        bank_Tree.add(new ASMutableTreeNode(IssueSendPaymentOrder.NAME, IssueSendPaymentOrder.TITLE));
        bank_Tree.add(new ASMutableTreeNode(MyOrderPaymentsSplitPanel.NAME, MyOrderPaymentsSplitPanel.TITLE));

        account_Node.add(new ASMutableTreeNode(MyAccountsSplitPanel.NAME, MyAccountsSplitPanel.TITLE));
        account_Node.add(new ASMutableTreeNode(MyLoansSplitPanel.NAME, MyLoansSplitPanel.TITLE));
        account_Node.add(new ASMutableTreeNode(FavoriteAccountsSplitPanel.NAME, FavoriteAccountsSplitPanel.TITLE));

        // account_Node.add(new
        // DefaultMutableTreeNode(Lang.T(Search
        // Accounts")));
        // account_Node.add(new DefaultMutableTreeNode(Issue Account"));

        person_Node.add(new ASMutableTreeNode(PersonsFavoriteSplitPanel.NAME, PersonsFavoriteSplitPanel.TITLE));
        person_Node.add(new ASMutableTreeNode(PersonsMySplitPanel.NAME, PersonsMySplitPanel.TITLE));
        person_Node.add(new ASMutableTreeNode(SearchPersonsSplitPanel.NAME, SearchPersonsSplitPanel.TITLE));
        person_Node.add(new ASMutableTreeNode(IssuePersonPanel.NAME, IssuePersonPanel.TITLE));
        person_Node.add(new ASMutableTreeNode(InsertPersonPanel.NAME, InsertPersonPanel.TITLE));

        document_Node.add(new ASMutableTreeNode(FavoriteStatementsSplitPanel.NAME, FavoriteStatementsSplitPanel.TITLE));
        document_Node.add(new ASMutableTreeNode(StatementsMySplitPanel.NAME, StatementsMySplitPanel.TITLE));
        document_Node.add(new ASMutableTreeNode(SearchStatementsSplitPanel.NAME, SearchStatementsSplitPanel.TITLE));
        document_Node.add(new ASMutableTreeNode(IssueDocumentPanel.NAME, IssueDocumentPanel.TITLE));

        mails_Node.add(new ASMutableTreeNode(IncomingMailsSplitPanel.NAME, IncomingMailsSplitPanel.TITLE));
        mails_Node.add(new ASMutableTreeNode(OutcomingMailsSplitPanel.NAME, OutcomingMailsSplitPanel.TITLE));
        mails_Node.add(new ASMutableTreeNode(MailSendPanel.NAME, MailSendPanel.TITLE));

        assets_Node.add(new ASMutableTreeNode(AssetsFavoriteSplitPanel.NAME, AssetsFavoriteSplitPanel.TITLE));
        assets_Node.add(new ASMutableTreeNode(AssetsMySplitPanel.NAME, AssetsMySplitPanel.TITLE));
        assets_Node.add(new ASMutableTreeNode(SearchAssetsSplitPanel.NAME, SearchAssetsSplitPanel.TITLE));
        assets_Node.add(new ASMutableTreeNode(IssueAssetPanel.NAME, IssueAssetPanel.TITLE));
        assets_Node.add(new ASMutableTreeNode(IssueAssetSeriesPanel.NAME, IssueAssetSeriesPanel.TITLE));
        assets_Node.add(new ASMutableTreeNode(MyBalanceTab.NAME, MyBalanceTab.TITLE));

        if (Settings.EXCHANGE_IN_OUT) {
            exchange_Node.add(new ASMutableTreeNode(DepositExchange.NAME, DepositExchange.TITLE));
            exchange_Node.add(new ASMutableTreeNode(WithdrawExchange.NAME, WithdrawExchange.TITLE));
        }
        exchange_Node.add(new ASMutableTreeNode(ExchangePanel.NAME, ExchangePanel.TITLE));
        exchange_Node.add(new ASMutableTreeNode(MyOrderTab.NAME, MyOrderTab.TITLE));

        templates_Node.add(new ASMutableTreeNode(TemplatesFavoriteSplitPanel.NAME, TemplatesFavoriteSplitPanel.TITLE));
        templates_Node.add(new ASMutableTreeNode(TemplateMySplitPanel.NAME, TemplateMySplitPanel.TITLE));
        templates_Node.add(new ASMutableTreeNode(SearchTemplatesSplitPanel.NAME, SearchTemplatesSplitPanel.TITLE));
        templates_Node.add(new ASMutableTreeNode(IssueTemplatePanel.NAME, IssueTemplatePanel.TITLE));


        statuses_Node.add(new ASMutableTreeNode(StatusesFavoriteSplitPanel.NAME, StatusesFavoriteSplitPanel.TITLE));
        statuses_Node.add(new ASMutableTreeNode(MyStatusesTab.NAME, MyStatusesTab.TITLE));
        statuses_Node.add(new ASMutableTreeNode(SearchStatusesSplitPanel.NAME, SearchStatusesSplitPanel.TITLE));
        statuses_Node.add(new ASMutableTreeNode(IssueStatusPanel.NAME, IssueStatusPanel.TITLE));

        unions_Node.add(new ASMutableTreeNode(UnionsFavoriteSplitPanel.NAME, UnionsFavoriteSplitPanel.TITLE));
        unions_Node.add(new ASMutableTreeNode(MyUnionsTab.NAME, MyUnionsTab.TITLE));
        unions_Node.add(new ASMutableTreeNode(SearchUnionSplitPanel.NAME, SearchUnionSplitPanel.TITLE));
        unions_Node.add(new ASMutableTreeNode(IssueUnionPanel.NAME, IssueUnionPanel.TITLE));

        polls_Node.add(new ASMutableTreeNode(PollsFavoriteSplitPanel.NAME, PollsFavoriteSplitPanel.TITLE));
        polls_Node.add(new ASMutableTreeNode(PollsMySplitPanel.NAME, PollsMySplitPanel.TITLE));
        polls_Node.add(new ASMutableTreeNode(SearchPollsSplitPanel.NAME, SearchPollsSplitPanel.TITLE));
        polls_Node.add(new ASMutableTreeNode(IssuePollPanel.NAME, IssuePollPanel.TITLE));

        records_Node.add(new ASMutableTreeNode(FavoriteTransactionsSplitPanel.NAME, FavoriteTransactionsSplitPanel.TITLE));
        records_Node.add(new ASMutableTreeNode(MyTransactionsSplitPanel.NAME, MyTransactionsSplitPanel.TITLE));
        records_Node.add(new ASMutableTreeNode(SearchTransactionsSplitPanel.NAME, SearchTransactionsSplitPanel.TITLE));
        records_Node.add(new ASMutableTreeNode(UnconfirmedTransactionsPanel.NAME, UnconfirmedTransactionsPanel.TITLE));

        hashes_Node.add(new ASMutableTreeNode(ImprintsFavoriteSplitPanel.NAME, ImprintsFavoriteSplitPanel.TITLE));
        hashes_Node.add(new ASMutableTreeNode(MyImprintsTab.NAME, MyImprintsTab.TITLE));
        hashes_Node.add(new ASMutableTreeNode(ImprintsSearchSplitPanel.NAME, ImprintsSearchSplitPanel.TITLE));
        hashes_Node.add(new ASMutableTreeNode(IssueImprintPanel.NAME, IssueImprintPanel.TITLE));

        linked_hashes_Node.add(new ASMutableTreeNode(IssueLinkedHashPanel.NAME, IssueLinkedHashPanel.TITLE));
        //linked_hashes_Node.add(new ASMutableTreeNode(SearchTransactionsSplitPanel.NAME, Lang.T("Search Linked Hash"), SearchTransactionsSplitPanel.TITLE));

        //telegram
        telegrams_Tree.add(new ASMutableTreeNode(TelegramSplitPanel.NAME, TelegramSplitPanel.TITLE));
        if (BlockChain.TEST_MODE)
            telegrams_Tree.add(new ASMutableTreeNode(ALLTelegramPanel.NAME, ALLTelegramPanel.TITLE));

        //////// OTHER
        other_Node.add(new ASMutableTreeNode(OtherSplitPanel.NAME, OtherSplitPanel.TITLE));
        if (BlockChain.TEST_MODE)
            other_Node.add(new ASMutableTreeNode(WalletsManagerSplitPanel.NAME, WalletsManagerSplitPanel.TITLE));

        other_Node.add(new ASMutableTreeNode(OtherConsolePanel.NAME, OtherConsolePanel.TITLE));
        other_Node.add(new ASMutableTreeNode(OtherSearchBlocks.NAME, OtherSearchBlocks.TITLE));

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

        JMenuItem menuExpand = new JMenuItem(Lang.T("Expand All"));
        menuExpand.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                for (int i = 0; i < tree.getRowCount(); i++)
                    tree.expandRow(i);
            }
        });

        JMenuItem menuColapse = new JMenuItem(Lang.T("Collapse All"));
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