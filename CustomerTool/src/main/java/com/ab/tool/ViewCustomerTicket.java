package com.ab.tool;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ViewCustomerTicket extends JDialog implements ActionListener {
	
	private JTextArea exDetails;
	private JLabel problemImg;
	
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	
	
	ViewCustomerTicket(CustomerTicket ticket) {
		
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
		
		JLabel typeLabel = new JLabel("Type:");
		typeLabel.setBounds(30, 10, 200, 20);
		contentPanel.add(typeLabel);
		
		JTextField typeText = new JTextField();
		typeText.setBounds(250, 10, 200, 20);
		contentPanel.add(typeText);
		CustomerCareReqType reqTypeEnum = CustomerCareReqType.findById(ticket.getRequestType());
		typeText.setText(reqTypeEnum.name());
		typeText.setEditable(false);
		
		JLabel closedCmtsLabel = new JLabel("Extra Details");
		closedCmtsLabel.setBounds(30, 40, 200, 20);
		contentPanel.add(closedCmtsLabel);
		
		exDetails = new JTextArea(5,100);
		exDetails.setLineWrap(true);
		exDetails.setBounds(250, 40, 400, 20);
		exDetails.setEnabled(false);
		exDetails.setEditable(false);
		JScrollPane scroll = new JScrollPane (exDetails);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    scroll.setBounds(250, 40, 400, 40);
	    contentPanel.add(scroll);
	    exDetails.setText(ticket.getExtraDetails());
	    
	    JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(30, 100, 650, 550);
		contentPanel.add(scrollPane);
		
		problemImg = new JLabel("Problem image here");
		scrollPane.setViewportView(problemImg);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		try {
			byte[] picBytes = PictureDBHandler.getInstance().getPictureFileContents(ticket.getResolvedPicId());
			if (picBytes != null) {
				ImageIcon image = new ImageIcon(picBytes);
				problemImg.setIcon(image);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
