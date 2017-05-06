package gui2;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import lang.Lang;

/**
 * @author DarkRaha
 * 
 */
public class tt extends JPanel { //implements TreeSelectionListener {

private static final long serialVersionUID = 1L;
public JTree tree; // ���� ������
public JTextField jtf = new JTextField();

public tt() {
   // ------------------------------------------
   // ���������� � ��������� ���������
 //  Container c = getContentPane(); // ���������� ������� ����
   setLayout(new BorderLayout()); // �������� �����������

   //-------------------------------------------
   // ���������� ������		
   // �������� �����
   DefaultMutableTreeNode root = new DefaultMutableTreeNode("Aronicle");

   // �������� ���� �������� �����
   DefaultMutableTreeNode account_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Accounts")); // ����� ������
   DefaultMutableTreeNode person_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Persons")); // ����� ������
   DefaultMutableTreeNode document_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Documents")); // ����� ������
   DefaultMutableTreeNode mails_Node = new DefaultMutableTreeNode(Lang.getInstance().translate("Mails"));

   // ���������� �� �������� �����
   root.add(account_Node);
   root.add(person_Node);
   root.add(document_Node);
   root.add(mails_Node);

   // ��������� �������� �������� (������) ������ �����
   person_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Persons")));
   person_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Search Persons")));
   person_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Issue Person")));
   
   
   account_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Accounts")));
  // account_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Search Accounts")));
   //account_Node.add(new DefaultMutableTreeNode("Issue Account"));
   
   document_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("My Statements")));
   document_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Search Statements")));
   document_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Issue Statement")));
   
   mails_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Incoming Mails")));
   mails_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Outcoming Mails")));
   mails_Node.add(new DefaultMutableTreeNode(Lang.getInstance().translate("Send Mail")));
   

   // ������� ������� ���������� � ��������� ������� ������
   tree = new JTree(root);
 //  tree.addTreeSelectionListener(this);
   tree.setRootVisible(false);
   for(int i = 0; i < tree.getRowCount(); i ++) tree.expandRow(i);
   
   // ��������� �������� ���������� � ����
   add(new JScrollPane(tree));
   add(jtf, BorderLayout.SOUTH);

   // -------------------------------------------
   // ��������� ����
  // setTitle("JTreeTest"); // ��������� ����
   // ����������� ������� ����
 //  setPreferredSize(new Dimension(640, 480));
   // ��������� ���������� ��� �������� ����
//   setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//   pack(); // ������������� ����������� �������
   setVisible(true); // ���������� ����

}

/*
// ����� ���������� TreeSelectionListener
public void valueChanged(TreeSelectionEvent arg0) {
   jtf.setText("old selection: " 
         + arg0.getOldLeadSelectionPath()
         + ";  new selection: "
         // ������ ��� ���� ��������������� � ������ ������� toString
         + arg0.getNewLeadSelectionPath().toString());
}
*/
}