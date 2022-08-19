package com.ab.tool;

import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ShowProgressDialog {
	private JFrame d;
	
	public ShowProgressDialog(JFrame parentFrame) {
		d = new JFrame();
		d.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		d.setBounds(100,100,400,200);
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		panel.add(new JLabel("Processing...Please Wait!"));
		d.setContentPane(panel);
		d.pack();
	}
	
	public void show() {
		d.setVisible(true);
		d.validate();
		d.invalidate();
	}
	
	public void close() {
		//d.setVisible(false);
		//d.dispose();
	}
}
