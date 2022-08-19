package com.ab.tool;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class AddNewUser extends JPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField userNameTxtField;
	private JTextField passwdTxtField;
	
	private JFrame mainFrame;
	
	public AddNewUser(JFrame mainFrame) {
		this.mainFrame = mainFrame; 
	}
	
	public void init() {
		JPanel newUserPanel = new JPanel();
		newUserPanel.setLayout(null);
		JLabel userIdLabel = new JLabel("User Name:");
		userIdLabel.setBounds(20, 40, 100, 25);
		newUserPanel.add(userIdLabel);
		
		userNameTxtField = new JTextField();
		userNameTxtField.setBounds(150, 40, 100, 25);
		newUserPanel.add(userNameTxtField);
		userNameTxtField.setColumns(20);
		
		JLabel lblNewLabel = new JLabel("Password:");
		lblNewLabel.setBounds(20, 70, 100, 25);
		newUserPanel.add(lblNewLabel);
		
		passwdTxtField = new JPasswordField();
		passwdTxtField.setColumns(20);
		passwdTxtField.setBounds(150, 70, 100, 25);
		newUserPanel.add(passwdTxtField);
		
		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		okButton.setBounds(150, 120, 100, 25);
		newUserPanel.add(okButton);
		
		this.setLayout(new BorderLayout());
		add(newUserPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String actionCmd = arg0.getActionCommand();
		if (actionCmd.equals("OK")) {
			int result = JOptionPane.showConfirmDialog(mainFrame, "Sure to add this user", "New User",
		               		JOptionPane.YES_NO_OPTION,
		               		JOptionPane.QUESTION_MESSAGE);
		    if(result == JOptionPane.YES_OPTION) {
		    	String userName = userNameTxtField.getText().trim();
		    	String passwd = passwdTxtField.getText().trim();
		    	if ((userName.length() == 0) || (passwd.length() == 0)) {
		    		JOptionPane.showMessageDialog(mainFrame, "Inputs are empty",
						      "Hey!", JOptionPane.ERROR_MESSAGE);
		    	}
		    	try {
		    		UserMgmt.getInstance().addUser(userName, passwd);
		    	} catch(NotAllowedException ex) {
		    		JOptionPane.showMessageDialog(mainFrame, ex.toString(),
						      "Hey!", JOptionPane.ERROR_MESSAGE);
		    	}
		    } else if (result == JOptionPane.NO_OPTION){
		    }
		} 
	}
}
