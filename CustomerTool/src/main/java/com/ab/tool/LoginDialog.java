package com.ab.tool;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

@SpringBootApplication
public class LoginDialog extends JDialog implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField userNameTxtField;
	private JPasswordField passwdTxtField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			LoginDialog dialog = new LoginDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public LoginDialog() {
		setBounds(100, 100, 450, 300);
		setTitle("Login Window");
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel userIdLabel = new JLabel("User Name:");
		userIdLabel.setBounds(20, 40, 100, 25);
		contentPanel.add(userIdLabel);
		
		userNameTxtField = new JTextField();
		userNameTxtField.setBounds(150, 40, 100, 25);
		contentPanel.add(userNameTxtField);
		userNameTxtField.setColumns(20);
		userNameTxtField.setText("rajasekhar");
		
		JLabel lblNewLabel = new JLabel("Password:");
		lblNewLabel.setBounds(20, 80, 100, 25);
		contentPanel.add(lblNewLabel);
		
		passwdTxtField = new JPasswordField();
		passwdTxtField.setColumns(20);
		passwdTxtField.setBounds(150, 80, 100, 25);
		passwdTxtField.setText("Raj@12345$");
		contentPanel.add(passwdTxtField);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				okButton.addActionListener(this);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				cancelButton.addActionListener(this);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String actionCmd = arg0.getActionCommand();
		if (actionCmd.equals("OK")) {
			String userName = userNameTxtField.getText().trim();
			@SuppressWarnings("deprecation")
			String passwd = passwdTxtField.getText().trim();
			boolean authResult = UserMgmt.getInstance().authenticate(userName, passwd);
			//boolean authResult = true;
			if (!authResult) {
				JOptionPane.showMessageDialog(contentPanel, "Authentication Not Success");
				return;
			}
			this.setVisible(false);
			this.dispose();
			MainFrame frame = new MainFrame();
			frame.setVisible(true);
		} else if (actionCmd.equals("Cancel")) {
			this.setVisible(false);
			this.dispose();
			System.exit(0);
		}
	}
}
