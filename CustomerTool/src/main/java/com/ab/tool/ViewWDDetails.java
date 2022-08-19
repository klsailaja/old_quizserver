package com.ab.tool;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ViewWDDetails extends JDialog implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	public ViewWDDetails(WithdrawRequest wdReq) {
		setTitle("View Withdraw Request Payment Details");
		setBounds(5, 5, 700, 400);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(null);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(this);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Closed");
				cancelButton.addActionListener(this);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		JLabel idLabel = new JLabel("ID:");
		idLabel.setBounds(30, 10, 200, 20);
		contentPanel.add(idLabel);
		
		JTextField idText = new JTextField();
		idText.setBounds(250, 10, 200, 20);
		contentPanel.add(idText);
		idText.setText(String.valueOf(wdReq.getId()));
		idText.setEditable(false);
		
		JLabel refIdLabel = new JLabel("RefId:");
		refIdLabel.setBounds(30, 40, 200, 20);
		contentPanel.add(refIdLabel);
		
		JTextField refIdText = new JTextField();
		refIdText.setBounds(250, 40, 200, 20);
		contentPanel.add(refIdText);
		refIdText.setText(wdReq.getRefId());
		refIdText.setEditable(false);
		
		JLabel userProfileIdLabel = new JLabel("User ID:");
		userProfileIdLabel.setBounds(30, 70, 200, 20);
		contentPanel.add(userProfileIdLabel);
		
		JTextField userProfileText = new JTextField();
		userProfileText.setBounds(250, 70, 200, 20);
		contentPanel.add(userProfileText);
		userProfileText.setText(String.valueOf(wdReq.getUserProfileId()));
		userProfileText.setEditable(false);
		
		JLabel payLabel = new JLabel("Payment Details:");
		payLabel.setBounds(30, 100, 200, 20);
		contentPanel.add(payLabel);
		
		JTextArea payDetails = new JTextArea(5,100);
		payDetails.setLineWrap(true);
		payDetails.setBounds(250, 100, 400, 50);
		payDetails.setEnabled(false);
		payDetails.setEditable(false);
		JScrollPane scroll = new JScrollPane (payDetails);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    scroll.setBounds(250, 100, 400, 50);
	    contentPanel.add(scroll);
	    
	    StringBuffer paymentDetails = new StringBuffer();
	    if (wdReq.getRequestType() == WithdrawReqType.BY_PHONE.getId()) {
	    	paymentDetails.append("Account Name=");
	    	paymentDetails.append(wdReq.getByPhone().getAccountHolderName());
	    	paymentDetails.append("\n");
	    	paymentDetails.append("Phone Number=");
	    	paymentDetails.append(wdReq.getByPhone().getPhNumber());
	    	paymentDetails.append("\n");
	    	paymentDetails.append("Payment Method=");
	    	PhonePaymentTypes phPaymentType = PhonePaymentTypes.findById(wdReq.getByPhone().getPaymentMethod());
	    	paymentDetails.append(phPaymentType.name());
	    	paymentDetails.append("\n");
	    	
	    } else if (wdReq.getRequestType() == WithdrawReqType.BY_BANK.getId()) {
	    	paymentDetails.append("User Name=");
	    	paymentDetails.append(wdReq.getByBank().getUserName());
	    	paymentDetails.append("\n");
	    	paymentDetails.append("Bank Name=");
	    	paymentDetails.append(wdReq.getByBank().getBankName());
	    	paymentDetails.append("\n");
	    	paymentDetails.append("Bank A/C Number=");
	    	paymentDetails.append(wdReq.getByBank().getAccountNumber());
	    	paymentDetails.append("\n");
	    	paymentDetails.append("Bank IFSC=");
	    	paymentDetails.append(wdReq.getByBank().getIfscCode());
	    	paymentDetails.append("\n");
	    }
	    payDetails.setText(paymentDetails.toString());
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String actionCmd = arg0.getActionCommand();
		if (actionCmd.equals("OK")) {
			this.setVisible(false);
			this.dispose();
		} else if (actionCmd.equals("Cancel")) {
			this.setVisible(false);
			this.dispose();
		}
	}
}
