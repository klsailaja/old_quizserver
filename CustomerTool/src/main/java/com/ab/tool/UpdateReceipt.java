package com.ab.tool;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class UpdateReceipt extends JDialog implements ItemListener, ActionListener {

	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	
	private JComboBox<String> phPaymentCombo;
	private JTextField phNumPayTxtField;
	private JTextField phNumPaidAmtTxtField;
	private JTextArea phNumClosedComents;
	private JTextField phNumReceiptFilePath;
	private JLabel phNumSelectedReceiptImg;
	
	private JTextField bankACTxtField;
	private JTextField bankIfscCodeTxtField;
	private JTextField bankPaidAmtTxtField;
	private JTextArea bankClosedComents;
	private JTextField bankReceiptFilePath;
	private JLabel bankSelectedReceiptImg;
	
	
	
	private JPanel centerPanel;
	private JComboBox<String> paymentTypeCombo;
	
	private int UI_MODE = -1;
	private static final int PAY_TO_PHONE = 1;
	private static final int PAY_TO_BANK = 2;
	
	private WithdrawRequest originalWRObject;
	
	
	public static void main(String[] args) {
		try {
			UpdateReceipt dialog = new UpdateReceipt(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public UpdateReceipt(WithdrawRequest originalWRObject) {
		setBounds(5, 5, 700, 700);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
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
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(this);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout());
		
		JLabel paymentTypeLabel = new JLabel("Payment Type");
		//paymentTypeLabel.setBounds(30, 50, 200, 20);
		topPanel.add(paymentTypeLabel);
		
		paymentTypeCombo = new JComboBox<>();
		paymentTypeCombo.setModel(new DefaultComboBoxModel<String>(new String[] {"Phone Payment", "Bank Payment"}));
		topPanel.add(paymentTypeCombo);
		getContentPane().add(topPanel, BorderLayout.NORTH);
		paymentTypeCombo.addItemListener(this);
		UI_MODE = PAY_TO_PHONE;
		
		centerPanel = new JPanel();
		CardLayout cardLayout = new CardLayout();
		centerPanel.setLayout(cardLayout);
		
		JPanel phPanel = createPhonePaymentPage();
		centerPanel.add("Phone Payment", phPanel);
		JPanel bankPanel = createBankPaymentPage();
		centerPanel.add("Bank Payment", bankPanel);
		
		getContentPane().add(centerPanel, BorderLayout.CENTER);
		this.originalWRObject = originalWRObject;
	}
	
	private JPanel createBankPaymentPage() {
		
		JPanel panel = new JPanel();
		
		panel.setLayout(null);
		
		JLabel acLabel = new JLabel("Bank A/C Number");
		acLabel.setBounds(30, 25, 200, 20);
		panel.add(acLabel);
		
		bankACTxtField = new JTextField();
		bankACTxtField.setBounds(250, 25, 200, 20);
		panel.add(bankACTxtField);
		bankACTxtField.setColumns(20);
		
		JLabel bankIfscCodeLabel = new JLabel("IFSC Code");
		bankIfscCodeLabel.setBounds(30, 50, 200, 20);
		panel.add(bankIfscCodeLabel);
		
		bankIfscCodeTxtField = new JTextField();
		bankIfscCodeTxtField.setBounds(250, 50, 200, 20);
		panel.add(bankIfscCodeTxtField);
		bankIfscCodeTxtField.setColumns(20);
		
		JLabel amountLabel = new JLabel("Paid Amount");
		amountLabel.setBounds(30, 75, 200, 20);
		panel.add(amountLabel);
		
		bankPaidAmtTxtField = new JTextField();
		bankPaidAmtTxtField.setColumns(10);
		bankPaidAmtTxtField.setBounds(250, 75, 200, 20);
		panel.add(bankPaidAmtTxtField);
		
		JLabel closedCmtsLabel = new JLabel("Close Comments");
		closedCmtsLabel.setBounds(30, 100, 200, 20);
		panel.add(closedCmtsLabel);
		
		bankClosedComents = new JTextArea(5,100);
		bankClosedComents.setLineWrap(true);
		bankClosedComents.setBounds(250, 100, 400, 20);
		JScrollPane scroll = new JScrollPane (bankClosedComents);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    scroll.setBounds(250, 100, 400, 50);
		panel.add(scroll);
		
		JLabel filePathLabel = new JLabel("Paid Receipt File Path");
		filePathLabel.setBounds(30, 160, 200, 20);
		panel.add(filePathLabel);
		
		bankReceiptFilePath = new JTextField();
		bankReceiptFilePath.setColumns(30);
		bankReceiptFilePath.setBounds(250, 160, 200, 20);
		panel.add(bankReceiptFilePath);
		
		JButton browseBut = new JButton("Browse");
		browseBut.addActionListener(this);
		browseBut.setActionCommand("browse");
		browseBut.setBounds(452, 160, 100, 20);
		panel.add(browseBut);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(30, 185, 600, 500);
		panel.add(scrollPane);
		
		bankSelectedReceiptImg = new JLabel("Receipt appears here");
		scrollPane.setViewportView(bankSelectedReceiptImg);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		return panel;
	}
	
	private JPanel createPhonePaymentPage() {
		JPanel phoneNumberPanel = new JPanel();
		
		phoneNumberPanel.setLayout(null);
		
		JLabel paymentLabel = new JLabel("Phone Payment Type");
		paymentLabel.setBounds(30, 25, 200, 20);
		phoneNumberPanel.add(paymentLabel);
		
		phPaymentCombo = new JComboBox<>();
		phPaymentCombo.setModel(new DefaultComboBoxModel<String>(new String[] {"PhonePe", "PayTM"}));
		phPaymentCombo.setBounds(250, 25, 200, 20);
		phoneNumberPanel.add(phPaymentCombo);
		
		JLabel phPayNumLabel = new JLabel("Phone Number");
		phPayNumLabel.setBounds(30, 50, 200, 20);
		phoneNumberPanel.add(phPayNumLabel);
		
		phNumPayTxtField = new JTextField();
		phNumPayTxtField.setBounds(250, 50, 200, 20);
		phoneNumberPanel.add(phNumPayTxtField);
		phNumPayTxtField.setColumns(10);
		
		JLabel amountLabel = new JLabel("Paid Amount");
		amountLabel.setBounds(30, 75, 200, 20);
		phoneNumberPanel.add(amountLabel);
		
		phNumPaidAmtTxtField = new JTextField();
		phNumPaidAmtTxtField.setColumns(10);
		phNumPaidAmtTxtField.setBounds(250, 75, 200, 20);
		phoneNumberPanel.add(phNumPaidAmtTxtField);
		
		JLabel closedCmtsLabel = new JLabel("Close Comments");
		closedCmtsLabel.setBounds(30, 100, 200, 20);
		phoneNumberPanel.add(closedCmtsLabel);
		
		phNumClosedComents = new JTextArea(5,100);
		phNumClosedComents.setLineWrap(true);
		phNumClosedComents.setBounds(250, 100, 400, 20);
		JScrollPane scroll = new JScrollPane (phNumClosedComents);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    scroll.setBounds(250, 100, 400, 50);
		phoneNumberPanel.add(scroll);
		
		JLabel receiptFilePathLabel = new JLabel("Paid Receipt File Path");
		receiptFilePathLabel.setBounds(30, 175, 200, 20);
		phoneNumberPanel.add(receiptFilePathLabel);
		
		phNumReceiptFilePath = new JTextField();
		phNumReceiptFilePath.setColumns(30);
		phNumReceiptFilePath.setBounds(250, 175, 200, 20);
		phoneNumberPanel.add(phNumReceiptFilePath);
		
		JButton browseBut = new JButton("Browse");
		browseBut.addActionListener(this);
		browseBut.setActionCommand("browse");
		browseBut.setBounds(452, 175, 100, 20);
		phoneNumberPanel.add(browseBut);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(30, 200, 650, 450);
		phoneNumberPanel.add(scrollPane);
		
		phNumSelectedReceiptImg = new JLabel("Receipt appears here");
		scrollPane.setViewportView(phNumSelectedReceiptImg);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		return phoneNumberPanel;
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getStateChange() == ItemEvent.SELECTED) {
			String str = (String) paymentTypeCombo.getSelectedItem();
			CardLayout cl = (CardLayout)(centerPanel.getLayout());
			cl.show(centerPanel, str);
			UI_MODE = paymentTypeCombo.getSelectedIndex() + 1;
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String actionCmd = arg0.getActionCommand();
		if (actionCmd.equals("browse")) {
			JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
            fileChooser.setAcceptAllFileFilterUsed(false);
            
            int option = fileChooser.showOpenDialog(this);
            String chosenFilePath = "File Choosing Cancelled";
            boolean isValid = false;
            if(option == JFileChooser.APPROVE_OPTION){
               File file = fileChooser.getSelectedFile();
               chosenFilePath = file.getAbsolutePath();
               isValid = true;
            }
            if (UI_MODE == PAY_TO_PHONE) {
         	   phNumReceiptFilePath.setText(chosenFilePath);
            } else {
         	   bankReceiptFilePath.setText(chosenFilePath);
            }
            if (isValid) {
            	ImageIcon image = new ImageIcon(chosenFilePath);
            	phNumSelectedReceiptImg.setText("");
            	if (UI_MODE == PAY_TO_PHONE) {
              	  	phNumSelectedReceiptImg.setIcon(image);
                } else {
                	bankSelectedReceiptImg.setIcon(image);
                }
            }
		} else if (actionCmd.equals("OK")) {
			String errMsg = validateInputs();
			if (errMsg.length() == 0) {
				int result = JOptionPane.showConfirmDialog(this,"Sure? You want to Update?", "Confirmation",
			               JOptionPane.YES_NO_OPTION,
			               JOptionPane.QUESTION_MESSAGE);
				
				if (result == JOptionPane.NO_OPTION) {
					return;
				}
				String currentReceiptFilePath = phNumReceiptFilePath.getText().trim();
				if (UI_MODE == PAY_TO_BANK) {
					currentReceiptFilePath = bankReceiptFilePath.getText().trim();
				}
				String closedCmds = phNumClosedComents.getText().trim();
				if (UI_MODE == PAY_TO_BANK) {
					closedCmds = bankClosedComents.getText().trim();
				}
				boolean exceptionThrown = false;
				try {
					WithdrawDBHandler.getInstance().closeWithDrawRequest(currentReceiptFilePath, originalWRObject.getRefId(), closedCmds);
				} catch (Exception e) {
					exceptionThrown = true;
					e.printStackTrace();
					String exceptionErrorMsg = null;
					if (e instanceof NotAllowedException) {
						exceptionErrorMsg = e.getMessage();
					} else if (e instanceof FileNotFoundException) {
						exceptionErrorMsg = "File not found. Please check for file existance";
					} else if (e instanceof SQLException) {
						exceptionErrorMsg = "SQL Exception";
					} else {
						exceptionErrorMsg = "Core server not reachable";
					}
					JOptionPane.showMessageDialog(null, exceptionErrorMsg.toString(),
						      "Hey!", JOptionPane.ERROR_MESSAGE);
				}
				if (!exceptionThrown) {
					JOptionPane.showMessageDialog(null, "Successfully Updated the Withdraw Receipt",
						      "Success!", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			
		} else if (actionCmd.equals("Cancel")) {
			this.setVisible(false);
			this.dispose();
		}
	}
	
	private String validateInputs() {
		StringBuffer errorMsg = new StringBuffer();
		if (originalWRObject.getRequestType() != UI_MODE) {
			errorMsg.append("Basic Payment Type mismatch\n");
			JOptionPane.showMessageDialog(null, errorMsg.toString() ,
				      "Hey!", JOptionPane.ERROR_MESSAGE);
			return errorMsg.toString();
		}
		if (UI_MODE == PAY_TO_PHONE) {
			int phonePaymentType = paymentTypeCombo.getSelectedIndex();
			PhonePaymentTypes phonePaymentTypeEnum = PhonePaymentTypes.findById(phonePaymentType + 1); 
			String paidPhNum = phNumPayTxtField.getText().trim();
			String paidAmt =  phNumPaidAmtTxtField.getText().trim();
			String closedComents = phNumClosedComents.getText().trim();
			String receiptFilePath = phNumReceiptFilePath.getText().trim();
			
			if ((paidPhNum == null) || (paidPhNum.length() == 0)) {
				errorMsg.append("Phone number is empty\n");
			}
			if ((paidAmt == null) || (paidAmt.length() == 0)) {
				errorMsg.append("Paid Amount is empty\n");
			}
			if ((closedComents == null) || (closedComents.length() == 0)) {
				errorMsg.append("Closed Coments is empty\n");
			}
			if ((receiptFilePath == null) || (receiptFilePath.length() == 0)) {
				errorMsg.append("Please choose the receipt picture\n");
			}
			if (errorMsg.length() > 0) {
				JOptionPane.showMessageDialog(null, errorMsg.toString(),
					      "Hey!", JOptionPane.ERROR_MESSAGE);
				return errorMsg.toString();
			}
			
			if (phonePaymentTypeEnum.getId() != originalWRObject.getByPhone().getPaymentMethod()) {
				errorMsg.append("Check the Payment Type\n");
			}
			if (!paidPhNum.equalsIgnoreCase(originalWRObject.getByPhone().getPhNumber())) {
				errorMsg.append("Check the Phone Number\n");
			}
			if (!paidAmt.equalsIgnoreCase(String.valueOf(originalWRObject.getAmount()))) {
				errorMsg.append("Check the Paid Amount\n");
			}
			if (errorMsg.length() > 0) {
				JOptionPane.showMessageDialog(null, errorMsg.toString(),
					      "Hey!", JOptionPane.ERROR_MESSAGE);
				return errorMsg.toString();
			}
		} else if (UI_MODE == PAY_TO_BANK) {
			String accountNumber = bankACTxtField.getText().trim();
			String ifscCode = bankIfscCodeTxtField.getText().trim();
			String requestedMoney = bankPaidAmtTxtField.getText().trim(); 
			String closeComents = bankClosedComents.getText().trim();
			String receiptFilePath = bankReceiptFilePath.getText().trim();
			
			if ((accountNumber == null) || (accountNumber.length() == 0)) {
				errorMsg.append("Account number is empty\n");
			}
			if ((ifscCode == null) || (ifscCode.length() == 0)) {
				errorMsg.append("IFSC Code is empty\n");
			}
			if ((requestedMoney == null) || (requestedMoney.length() == 0)) {
				errorMsg.append("Paid Amount is empty\n");
			}
			if ((closeComents == null) || (closeComents.length() == 0)) {
				errorMsg.append("Closed Coments is empty\n");
			}
			if ((receiptFilePath == null) || (receiptFilePath.length() == 0)) {
				errorMsg.append("Please choose the receipt picture\n");
			}
			if (errorMsg.length() > 0) {
				JOptionPane.showMessageDialog(null, errorMsg.toString(),
					      "Hey!", JOptionPane.ERROR_MESSAGE);
				return errorMsg.toString();
			}
			if (!accountNumber.equals(originalWRObject.getByBank().getAccountNumber())) {
				errorMsg.append("Please check the account number\n");
			}
			if (!ifscCode.equals(originalWRObject.getByBank().getIfscCode())) {
				errorMsg.append("Please check the account number\n");
			}
			if (!requestedMoney.equalsIgnoreCase(String.valueOf(originalWRObject.getAmount()))) {
				errorMsg.append("Check the Paid Amount\n");
			}
			if (errorMsg.length() > 0) {
				JOptionPane.showMessageDialog(null, errorMsg.toString(),
					      "Hey!", JOptionPane.ERROR_MESSAGE);
				return errorMsg.toString();
			}
		}
		return errorMsg.toString();
	}
}
