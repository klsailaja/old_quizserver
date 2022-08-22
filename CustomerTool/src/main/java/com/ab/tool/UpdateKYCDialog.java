package com.ab.tool;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class UpdateKYCDialog extends JDialog implements ActionListener, ItemListener {
	
	private static final long serialVersionUID = 1L;
	
	private JPanel centerPanel = new JPanel();
	private JComboBox<String> docTypeCombo, panPhotoCombo, aaFrontPhotoCombo, aaBackPhotoCombo;
	private JTextField panTF, aaFrontTF, aaBackTF;
	private int panSelectedIndex, aaFrontSelectedIndex, aaBackSelectedIndex;
	private JTextArea statusCmts;
	
	private KYCEntry kycEntry;
	private String[] shortCodes = new String[] {"Please Verify", "Verified", "Missing", "Not Clear", "Others"};
	private String[] detailedNotes = new String[] {
			"",
			"",
			"is not uploaded. Please upload.",
			"is not clear.Please upload once again.",
			""};
	
	public UpdateKYCDialog(KYCEntry kycEntry) {
		setTitle("Update Cmts for KYC");
		setBounds(5, 5, 800, 700);
		this.kycEntry = kycEntry;
		getContentPane().setLayout(new BorderLayout());
		
		
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
		
		JButton genButton = new JButton("Generate");
		genButton.addActionListener(this);
		genButton.setActionCommand("Generate");
		buttonPane.add(genButton);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		JLabel statusLabel = new JLabel("Status:");
		statusPanel.add(statusLabel);
		
		statusCmts = new JTextArea(5,50);
		//statusCmts.setBounds(110, 10, 400, 50);
		statusCmts.setLineWrap(true);
		statusCmts.setEnabled(false);
		statusCmts.setEditable(false);
		JScrollPane scroll = new JScrollPane (statusCmts);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    statusPanel.add(scroll);
	    statusCmts.setText(kycEntry.getStatus());
	    
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
		
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(centerPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		CardLayout cl = (CardLayout)(centerPanel.getLayout());
		cl.show(centerPanel, "Aadhar Front Page");
	}
	
	private JPanel createPAN() {
		JPanel panPanel = new JPanel();
		panPanel.setLayout(new BorderLayout());
		
		JPanel panTopPanel = new JPanel();
		panTopPanel.setLayout(new GridLayout());
		
		panPhotoCombo = new JComboBox<String>();
		panPhotoCombo.setModel(new DefaultComboBoxModel<String>(shortCodes));
		panTopPanel.add(panPhotoCombo);
		panPhotoCombo.addItemListener(this);
		
		panTF = new JTextField(90);
		panTopPanel.add(panTF);
		panTF.setEditable(false);
		panSelectedIndex = 0;
		
		panPanel.add(panTopPanel, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setSize(650, 450);
		JLabel photoLabel = new JLabel("PAN Card Photo Here");
		scrollPane.setViewportView(photoLabel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panPanel.add(scrollPane, BorderLayout.CENTER);
		try {
			byte[] picBytes = PictureDBHandler.getInstance().getPictureFileContents(kycEntry.getPpId());
			if (picBytes != null) {
				ImageIcon image = new ImageIcon(picBytes);
				photoLabel.setIcon(image);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return panPanel;
	}

	private JPanel createAABP() {
		JPanel panPanel = new JPanel();
		panPanel.setLayout(new BorderLayout());
		
		JPanel panTopPanel = new JPanel();
		panTopPanel.setLayout(new GridLayout());
		
		aaBackPhotoCombo = new JComboBox<String>();
		aaBackPhotoCombo.setModel(new DefaultComboBoxModel<String>(shortCodes));
		panTopPanel.add(aaBackPhotoCombo);
		aaBackPhotoCombo.addItemListener(this);
		
		aaBackTF = new JTextField(90);
		panTopPanel.add(aaBackTF);
		aaBackTF.setEditable(false);
		aaBackSelectedIndex = 0;
		
		panPanel.add(panTopPanel, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setSize(650, 450);
		JLabel photoLabel = new JLabel("Aadhar Back Photo Here");
		scrollPane.setViewportView(photoLabel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panPanel.add(scrollPane, BorderLayout.CENTER);
		try {
			byte[] picBytes = PictureDBHandler.getInstance().getPictureFileContents(kycEntry.getAbpId());
			if (picBytes != null) {
				ImageIcon image = new ImageIcon(picBytes);
				photoLabel.setIcon(image);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return panPanel;
	}

	private JPanel createAAFP() {
		JPanel panPanel = new JPanel();
		panPanel.setLayout(new BorderLayout());
		
		JPanel panTopPanel = new JPanel();
		panTopPanel.setLayout(new GridLayout());
		
		aaFrontPhotoCombo = new JComboBox<String>();
		aaFrontPhotoCombo.setModel(new DefaultComboBoxModel<String>(shortCodes));
		panTopPanel.add(aaFrontPhotoCombo);
		aaFrontPhotoCombo.addItemListener(this);
		
		aaFrontTF = new JTextField(90);
		panTopPanel.add(aaFrontTF);
		aaFrontTF.setEditable(false);
		aaFrontSelectedIndex = 0;
		
		panPanel.add(panTopPanel, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setSize(650, 450);
		JLabel photoLabel = new JLabel("Aadhar Front Photo Here");
		scrollPane.setViewportView(photoLabel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panPanel.add(scrollPane, BorderLayout.CENTER);
		try {
			byte[] picBytes = PictureDBHandler.getInstance().getPictureFileContents(kycEntry.getAfpId());
			if (picBytes != null) {
				ImageIcon image = new ImageIcon(picBytes);
				photoLabel.setIcon(image);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return panPanel;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String actionCmd = arg0.getActionCommand();
		if (actionCmd.equals("Cancel")) {
			this.setVisible(false);
			this.dispose();
		} if (actionCmd.equals("Generate")) {
			if ((panSelectedIndex <= 0) || (aaFrontSelectedIndex <= 0) || (aaBackSelectedIndex <= 0)) {
				JOptionPane.showMessageDialog(this, "Please verify all the docs",
					      "Hey!", JOptionPane.ERROR_MESSAGE);
				return;
			}
			StringBuffer statusContents = new StringBuffer();
			if ((panSelectedIndex == 1) && (aaFrontSelectedIndex == 1) && (aaBackSelectedIndex == 1)) {
				statusContents.append("APPROVED");
			} else {
				statusContents.append(aaFrontTF.getText().trim());
				statusContents.append("\n");
				statusContents.append(aaBackTF.getText().trim());
				statusContents.append("\n");
				statusContents.append(panTF.getText().trim());
			}
			statusCmts.setText(statusContents.toString());
		} else if (actionCmd.equals("OK")) {
			String statusStr = statusCmts.getText().trim();
			if (statusStr.length() == 0) {
				JOptionPane.showMessageDialog(this, "Status is empty. Please verify all the docs",
					      "Hey!", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (statusStr.length() > 199) {
				JOptionPane.showMessageDialog(this, "Status exceeding 200 chars. Please reduce",
					      "Hey!", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			int result = JOptionPane.showConfirmDialog(this,"Sure? You want to Update?", "Confirmation",
		               JOptionPane.YES_NO_OPTION,
		               JOptionPane.QUESTION_MESSAGE);
			
			if (result == JOptionPane.NO_OPTION) {
				return;
			}
			String exceptionErrorMsg = null;
			boolean exceptionThrown = false;
			try {
				boolean updateResult = KYCDBHandler.getInstance().updateKYCEntryCmts(kycEntry.getUserId(), 
					statusStr);
				if (updateResult) {
					exceptionErrorMsg = "Successfully Updated the KYC Comments";
				}
			} catch (SQLException e) {
				e.printStackTrace();
				exceptionThrown = true;
				exceptionErrorMsg = "SQL Exception while updating KYC Comments";
			}
			
			if (exceptionThrown) {
				JOptionPane.showMessageDialog(this, exceptionErrorMsg.toString(),
					      "Hey!", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, exceptionErrorMsg,
				      "Success!", JOptionPane.INFORMATION_MESSAGE);
			}
			this.setVisible(false);
			this.dispose();
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent arg0) {
		
		if (arg0.getSource() == docTypeCombo) {
			if (arg0.getStateChange() == ItemEvent.SELECTED) {
				String str = (String) docTypeCombo.getSelectedItem();
				CardLayout cl = (CardLayout)(centerPanel.getLayout());
				cl.show(centerPanel, str);
			}
		} else {
			int selectedIndex = -1;
			JTextField sourceTF = null;
			String componentName = null;
			if (arg0.getStateChange() == ItemEvent.SELECTED) {
				if (arg0.getSource() == aaFrontPhotoCombo) {
					selectedIndex = aaFrontPhotoCombo.getSelectedIndex();
					aaFrontSelectedIndex = selectedIndex; 
					sourceTF = aaFrontTF;
					componentName = "AADHAR FRONT PAGE:";
				} else if (arg0.getSource() == aaBackPhotoCombo) {
					selectedIndex = aaBackPhotoCombo.getSelectedIndex();
					aaBackSelectedIndex = selectedIndex; 
					sourceTF = aaBackTF;
					componentName = "AADHAR BACK PAGE:";
				} else if (arg0.getSource() == panPhotoCombo) {
					selectedIndex = panPhotoCombo.getSelectedIndex();
					panSelectedIndex = selectedIndex;
					sourceTF = panTF;
					componentName = "PAN PAGE:";
				}
			}
			if (selectedIndex > -1) {
				if (selectedIndex > 1) {
					sourceTF.setText(componentName + detailedNotes[selectedIndex]);
				} 
				if (selectedIndex == 4) {
					sourceTF.setEditable(true);
				} else {
					sourceTF.setEditable(false);
				}
			}
		}
	}
}
