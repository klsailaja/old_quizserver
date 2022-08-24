package com.ab.tool;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

public class MainFrame extends JFrame implements TreeSelectionListener {

	private static final long serialVersionUID = 1L;
	
	private JPanel mainPanel, rightPanel;
	private JTree tree;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize);
		setTitle("Customer Tool");
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(0, 0));
		
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Tool");
		DefaultMutableTreeNode withdrawReqs = new DefaultMutableTreeNode("Withdraws");
		DefaultMutableTreeNode customerReqs = new DefaultMutableTreeNode("CustomerCare");
		DefaultMutableTreeNode addUserNode = new DefaultMutableTreeNode("AddUser");
		DefaultMutableTreeNode kycViewNode = new DefaultMutableTreeNode("KYCView");
		
		rootNode.add(withdrawReqs);
		rootNode.add(customerReqs);
		if (UserMgmt.getInstance().isAdmin()) {
			rootNode.add(addUserNode);
		}
		rootNode.add(kycViewNode);
		
		tree = new JTree(rootNode);
		JScrollPane treeView = new JScrollPane(tree);
		mainPanel.add(treeView, BorderLayout.WEST);
		
		tree.getSelectionModel().setSelectionMode
        (TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(this);
		
		rightPanel = new JPanel();
		CardLayout cardLayout = new CardLayout();
		rightPanel.setLayout(cardLayout);
		
		JPanel wdPanel = new WithdrawPanel(this);
		rightPanel.add("Withdraws", wdPanel);
		
		rightPanel.add("CustomerCare", new CustomerCarePanel(this));
		
		AddNewUser addNewUser = new AddNewUser(this);
		addNewUser.init();
		rightPanel.add("AddUser", addNewUser);
		rightPanel.add("KYCView", new KYCPanel(this));
		
		mainPanel.add(rightPanel, BorderLayout.CENTER);
		
		setContentPane(mainPanel);
		
		cardLayout.show(rightPanel, "Withdraws");
		this.pack();
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();

		if (node == null)
			//Nothing is selected.     
			return;

		String nodeName = (String) node.getUserObject();
		CardLayout cl = (CardLayout)(rightPanel.getLayout());
        cl.show(rightPanel, nodeName);
	}
}
