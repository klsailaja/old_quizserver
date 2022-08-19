package com.ab.tool;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class QueryPanel extends JPanel implements ActionListener, ItemListener {
	
	private static final long serialVersionUID = 1L;
	
	private JTextField viewByIdText;
	private MessageListener messageListener;
	private JRadioButton impRadio,allRadio,byIdRadio;
	private JComboBox<String> comboBox;
	private JComboBox<String> stateCombo;
	
	public static final int IMPORTANT = 1;
	public static final int ALL_RECORDS = 2;
	public static final int GET_BY_ID = 3;
	
	/**
	 * Create the panel.
	 */
	public QueryPanel(String[] stateMsgs, MessageListener messageListener) {
		this.messageListener = messageListener;
		setLayout(new FlowLayout());
		
		comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"25", "50", "75", "100"}));
		comboBox.setBounds(120, 81, 40, 25);
		
		stateCombo = new JComboBox<String>();
		stateCombo.setModel(new DefaultComboBoxModel<String>(stateMsgs));
		stateCombo.setBounds(120, 81, 40, 25);
		if (UserMgmt.getInstance().isAdmin()) {
			add(comboBox);
		}
		
		ButtonGroup bg = new ButtonGroup();  
		
		impRadio = new JRadioButton("Important");
		impRadio.setBounds(6, 45, 80, 23);
		impRadio.setSelected(true);
		impRadio.addItemListener(this);
		if (UserMgmt.getInstance().isAdmin()) {
			add(impRadio);
			bg.add(impRadio);
		}
		
		allRadio = new JRadioButton("All Records");
		allRadio.setBounds(6, 82, 80, 23);
		allRadio.addItemListener(this);
		if (UserMgmt.getInstance().isAdmin()) {
			add(allRadio);
			bg.add(allRadio);
		}
		
		stateCombo.setEnabled(false);
		if (UserMgmt.getInstance().isAdmin()) {
			add(stateCombo);
		}
		
		byIdRadio = new JRadioButton("View By Id");
		byIdRadio.setBounds(6, 118, 80, 23);
		byIdRadio.addItemListener(this);
		add(byIdRadio);
		bg.add(byIdRadio);
		
		viewByIdText = new JTextField();
		viewByIdText.setBounds(120, 117, 100, 25);
		add(viewByIdText);
		viewByIdText.setColumns(10);
		viewByIdText.setEnabled(false);
		
		JButton filterApplyButton = new JButton("Apply");
		filterApplyButton.setBounds(91, 172, 89, 23);
		add(filterApplyButton);
		filterApplyButton.setActionCommand("apply");
		filterApplyButton.addActionListener(this);
	}
	
	
	

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String actionCmd = arg0.getActionCommand();
		if (actionCmd.equals("apply")) {
			int type = -1;
			String pageLen = null;
			int stateVal = -1;
			if (impRadio.isSelected()) {
				type = IMPORTANT;
				pageLen = (String)comboBox.getSelectedItem();
			} else if (allRadio.isSelected()) {
				type = ALL_RECORDS;
				pageLen = (String)comboBox.getSelectedItem();
				stateVal = stateCombo.getSelectedIndex();
				stateVal++;
			} else if (byIdRadio.isSelected()) {
				type = GET_BY_ID;
				pageLen = viewByIdText.getText().trim();
			}
			String[] data = new String[2];
			data[0] = pageLen;
			data[1] = String.valueOf(stateVal);
			this.messageListener.passData(type, data);
		}
	}




	@Override
	public void itemStateChanged(ItemEvent arg0) {
		viewByIdText.setEnabled(false);
		stateCombo.setEnabled(false);
		if (arg0.getSource() == allRadio) {
			stateCombo.setEnabled(true);
		} else if (arg0.getSource() == byIdRadio) { 
			viewByIdText.setEnabled(true);
		} 
	}
}



