package com.ab.tool;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class KYCPanel extends JPanel implements ActionListener, ItemListener {
	
	private static final long serialVersionUID = 1L;
	
	private JPanel centerPanel = new JPanel();
	private JComboBox<String> docTypeCombo, panPhotoCombo, aaFrontPhotoCombo, aaBackPhotoCombo;
	private JTextField panTF;
	private JFrame mainFrame;
	private KYCEntry kycEntry;
	private String[] shortCodes = new String[] {"Verified", "Missing", "Not Clear", "Others"};
	private String[] detailedNotes = new String[] {
			"",
			"is not uploaded. Please upload.",
			"is not clear.Please upload once again.",
			""};
	
	public KYCPanel(JFrame mainFrame, KYCEntry kycEntry) {
		this.mainFrame = mainFrame;
		this.kycEntry = kycEntry;
		setLayout(new BorderLayout());
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton("OK");
		okButton.addActionListener(this);
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new FlowLayout());
		
		JLabel statusLabel = new JLabel("Status:");
		statusLabel.setBounds(10, 10, 100, 20);
		statusPanel.add(statusLabel);
		
		JTextArea statusCmts = new JTextArea(5,100);
		statusCmts.setBounds(110, 10, 400, 50);
		statusCmts.setLineWrap(true);
		statusCmts.setEnabled(false);
		statusCmts.setEditable(false);
		JScrollPane scroll = new JScrollPane (statusCmts);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    statusPanel.add(scroll);
	    
	    topPanel.add(statusPanel, BorderLayout.NORTH);
	    
	    JPanel pagePanel = new JPanel();
	    pagePanel.setLayout(new FlowLayout());
	    
	    JLabel paymentTypeLabel = new JLabel("Doc Type");
	    pagePanel.add(paymentTypeLabel);
		
	    docTypeCombo = new JComboBox<String>();
	    docTypeCombo.setModel(new DefaultComboBoxModel<String>(new String[] {"Aadhar Front Page", "Aadhar Back Page", "PAN Page"}));
		pagePanel.add(docTypeCombo);
		topPanel.add(pagePanel, BorderLayout.SOUTH);
		docTypeCombo.addItemListener(this);
	    
	    
		centerPanel = new JPanel();
		CardLayout cardLayout = new CardLayout();
		centerPanel.setLayout(cardLayout);
		
		
		JPanel aafpPanel = createAAFP();
		centerPanel.add("Aadhar Front Page", aafpPanel);
		JPanel aabpPanel = createAABP();
		centerPanel.add("Aadhar Back Page", aabpPanel);
		JPanel panPanel = createPAN();
		centerPanel.add("PAN Page", panPanel);
		
		add(topPanel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
		add(buttonPane, BorderLayout.SOUTH);
		
		CardLayout cl = (CardLayout)(centerPanel.getLayout());
		cl.show(centerPanel, "PAN Page");
	}
	
	private JPanel createPAN() {
		JPanel panPanel = new JPanel();
		panPanel.setLayout(new BorderLayout());
		
		JPanel panTopPanel = new JPanel();
		panTopPanel.setLayout(new FlowLayout());
		
		panPhotoCombo = new JComboBox<String>();
		panPhotoCombo.setModel(new DefaultComboBoxModel<String>(shortCodes));
		panTopPanel.add(panPhotoCombo);
		
		panTF = new JTextField(80);
		panTopPanel.add(panTF);
		
		panPanel.add(panTopPanel, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setSize(650, 450);
		JLabel photoLabel = new JLabel("PAN Card Photo Here");
		scrollPane.setViewportView(photoLabel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panPanel.add(scrollPane, BorderLayout.CENTER);
		return panPanel;
	}

	private JPanel createAABP() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		return panel;
	}

	private JPanel createAAFP() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
	}
	
	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getStateChange() == ItemEvent.SELECTED) {
			String str = (String) docTypeCombo.getSelectedItem();
			CardLayout cl = (CardLayout)(centerPanel.getLayout());
			cl.show(centerPanel, str);
		}
	}
}
