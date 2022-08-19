package com.ab.tool;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class UpdateCustomerTicket extends JDialog implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();
	
	private JComboBox<String> statusCombo;
	private JTextArea ticketClosedComents;
	
	private JTextField resolvedFilePath;
	private JLabel resolvedImg;
	private CustomerTicket ticket;
	
	public UpdateCustomerTicket(CustomerTicket ticket) {
		this.ticket = ticket;
		setBounds(5, 5, 700, 700);
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
		
		JLabel statusLabel = new JLabel("Status:");
		statusLabel.setBounds(30, 10, 200, 20);
		contentPanel.add(statusLabel);
		
		statusCombo = new JComboBox<>();
		statusCombo.setModel(new DefaultComboBoxModel<String>(new String[] {"Open", "Closed", "Not An Issue"}));
		statusCombo.setBounds(250, 10, 200, 20);
		contentPanel.add(statusCombo);
		int stateSelectionIndex = ticket.getStatus();
		stateSelectionIndex--;
		statusCombo.setSelectedIndex(stateSelectionIndex);
		
		JLabel closedCmtsLabel = new JLabel("Close Comments");
		closedCmtsLabel.setBounds(30, 50, 200, 20);
		contentPanel.add(closedCmtsLabel);
		
		ticketClosedComents = new JTextArea(5,100);
		ticketClosedComents.setLineWrap(true);
		ticketClosedComents.setBounds(250, 50, 400, 20);
		JScrollPane scroll = new JScrollPane (ticketClosedComents);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    scroll.setBounds(250, 50, 400, 50);
	    contentPanel.add(scroll);
	    ticketClosedComents.setText(ticket.getClosedCmts());
	    
	    JLabel filePathLabel = new JLabel("Resoved Pic File Path");
		filePathLabel.setBounds(30, 120, 200, 20);
		contentPanel.add(filePathLabel);
		
		resolvedFilePath = new JTextField();
		resolvedFilePath.setColumns(30);
		resolvedFilePath.setBounds(250, 120, 200, 20);
		contentPanel.add(resolvedFilePath);
		
		JButton browseBut = new JButton("Browse");
		browseBut.addActionListener(this);
		browseBut.setActionCommand("browse");
		browseBut.setBounds(452, 120, 100, 20);
		contentPanel.add(browseBut);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(30, 140, 650, 450);
		contentPanel.add(scrollPane);
		
		resolvedImg = new JLabel("Receipt appears here");
		scrollPane.setViewportView(resolvedImg);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		try {
			byte[] picBytes = PictureDBHandler.getInstance().getPictureFileContents(ticket.getResolvedPicId());
			if (picBytes != null) {
				//resolvedFilePath.setText(resolvedPic.getFilePath());
				ImageIcon image = new ImageIcon(picBytes);
	        	resolvedImg.setIcon(image);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
            resolvedFilePath.setText(chosenFilePath);
            if (isValid) {
            	ImageIcon image = new ImageIcon(chosenFilePath);
            	resolvedImg.setText("");
            	resolvedImg.setIcon(image);
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
				String currentReceiptFilePath = resolvedFilePath.getText().trim();
				if (currentReceiptFilePath.length() == 0) {
					currentReceiptFilePath = null;
				}
				boolean exceptionThrown = false;
				try {
					String tktRefId = ticket.getRefId().trim();
					int stateSelectedIndex = statusCombo.getSelectedIndex();
					stateSelectedIndex++;
					CustomerCareReqState selectedReqStateEnum = CustomerCareReqState.findById(stateSelectedIndex);
					String closedCmtsStr = ticketClosedComents.getText().trim(); 
					CustomerCareDBHandler.getInstance().updateCCRequestState(tktRefId, 
							selectedReqStateEnum, closedCmtsStr, currentReceiptFilePath);
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
					JOptionPane.showMessageDialog(this, "Successfully Updated the Customer Ticket",
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
		
		String closedComents = ticketClosedComents.getText().trim();
		if ((closedComents == null) || (closedComents.length() == 0)) {
			errorMsg.append("Closed Coments is empty\n");
		}
		if (errorMsg.length() > 0) {
			JOptionPane.showMessageDialog(null, errorMsg.toString(),
				      "Hey!", JOptionPane.ERROR_MESSAGE);
		}
		return errorMsg.toString();
	}
}
