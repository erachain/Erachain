package gui2;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.tree.DefaultMutableTreeNode;

import core.BlockChain;
import lang.Lang;

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

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Erachain");

        DefaultMutableTreeNode account_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Accounts"));
        DefaultMutableTreeNode person_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Persons"));
        DefaultMutableTreeNode document_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Documents"));
        DefaultMutableTreeNode mails_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Mails"));
        DefaultMutableTreeNode assets_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Assets"));
        DefaultMutableTreeNode templates_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Templates"));
        DefaultMutableTreeNode statuses_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Statuses"));
        DefaultMutableTreeNode unions_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Unions"));
        DefaultMutableTreeNode polls_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Polls"));
        DefaultMutableTreeNode hashes_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Unique Hashes"));
        DefaultMutableTreeNode linked_hashes_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Linked Hashes"));
        DefaultMutableTreeNode records_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Records"));
        DefaultMutableTreeNode other_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Network DashBoard"));
        DefaultMutableTreeNode bank_Tree = new DefaultMutableTreeNode(Lang.getInstance().translate("Bank"));
        DefaultMutableTreeNode telegrams_Tree = new DefaultMutableTreeNode(Lang.getInstance().translate("Telegrams"));
        

        if (BlockChain.DEVELOP_USE)
            root.add(bank_Tree);
        root.add(account_Node);
        root.add(person_Node);
        root.add(document_Node);
        root.add(mails_Node);
        root.add(assets_Node);
        root.add(templates_Node);
        root.add(statuses_Node);
        if (BlockChain.DEVELOP_USE)
            root.add(unions_Node);
        if (BlockChain.DEVELOP_USE)
            root.add(polls_Node);
        if (BlockChain.DEVELOP_USE)
            root.add(hashes_Node);
        if (BlockChain.DEVELOP_USE)
            root.add(linked_hashes_Node);
        if (BlockChain.DEVELOP_USE)
            root.add(telegrams_Tree);
        
        root.add(records_Node);
        root.add(other_Node);

        bank_Tree.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Send payment order")));
        bank_Tree.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Payments Orders")));

        person_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Favorite Persons")));
        person_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Persons")));
        person_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Search Persons")));
        person_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Issue Person")));
        person_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Insert Person")));

        account_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Accounts")));
        account_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Loans")));
        account_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Favorite Accounts")));

        // account_Node.add(new
        // DefaultMutableTreeNode(Lang.getInstance().translate("Search
        // Accounts")));
        // account_Node.add(new DefaultMutableTreeNode("Issue Account"));

        document_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Favorite Documents")));
        document_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Documents")));
        document_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Search Documents")));
        document_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Issue Document")));

        mails_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Incoming Mails")));
        mails_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Outcoming Mails")));
        mails_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Send Mail")));

        assets_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Favorite Assets")));
        assets_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Assets")));
        assets_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Search Assets")));
        assets_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Issue Asset")));
        assets_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Orders")));
        assets_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Balance")));
        assets_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Exchange")));

        templates_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Favorite Templates")));
        templates_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Search Templates")));
        templates_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Issue Template")));

        statuses_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Favorite Statuses")));
        statuses_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Search Statuses")));
        statuses_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Create Status")));

        unions_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Unions")));
        unions_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Search Unions")));
        unions_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Issue Union")));

        polls_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Polls")));
        polls_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Search Polls")));
        polls_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Issue Poll")));
        polls_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Favorite Polls")));

        records_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Records")));
        records_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Search Records")));
        records_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Unconfirmed Records")));

        other_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Other")));
        if (BlockChain.DEVELOP_USE)
            other_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Wallets Manager")));
        
        other_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Console")));
        other_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Blocks")));

        hashes_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Favorite Unique Hashes")));
        hashes_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Unique Hashes")));
        hashes_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Search Unique Hashes")));
        hashes_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Issue Unique Hash")));

        linked_hashes_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Issue Linked Hash")));
        linked_hashes_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Search Linked Hash")));
        
       //telegram
        telegrams_Tree.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Telegrams Panel")));
        telegrams_Tree.add(new DefaultMutableTreeNode(Lang.getInstance().translate("All Telegrams Panel")));
        
        tree = new JTree(root);
        // tree.addTreeSelectionListener(this);
        double dd = (double) UIManager.getFont("TextField.font").getSize() * 1.2;
        tree.setRowHeight((int) dd);
        tree.setRootVisible(false);
        for (int i = 0; i < tree.getRowCount(); i++)
            tree.expandRow(i);

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

        // -------------------------------------------
        // ��������� ����
        // setTitle("JTreeTest"); // ��������� ����
        // ����������� ������� ����
        // setPreferredSize(new Dimension(640, 480));
        // ��������� ���������� ��� �������� ����
        // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // pack(); // ������������� ����������� �������
        setVisible(true); // ���������� ����

    }

    /*
     * // ����� ���������� TreeSelectionListener public void
     * valueChanged(TreeSelectionEvent arg0) { jtf.setText("old selection: " +
     * arg0.getOldLeadSelectionPath() + ";  new selection: " // ������ ��� ����
     * ��������������� � ������ ������� toString +
     * arg0.getNewLeadSelectionPath().toString()); }
     */
}