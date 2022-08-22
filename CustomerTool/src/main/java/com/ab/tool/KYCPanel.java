package com.ab.tool;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class KYCPanel extends JPanel implements ActionListener, MessageListener {
	
	private static final long serialVersionUID = 1L;
	
	private int startNumber = 0;
	private int lastQueryType = -1;
	private int lastPageLength = -1;
	private int lastQueryState = -1;
	
	private DefaultTableModel tableModel = new DefaultTableModel();
	private JTable tableView;
	private JLabel totalLabel = new JLabel();
	private JButton prevButton = new JButton("<<Previous");
	private JButton nextButton = new JButton("Next>>");
	
	private JPopupMenu popupMenu;
    private JMenuItem menuItemView;
    private JMenuItem menuItemUpdate;
    
    private List<KYCEntry> currentList = new ArrayList<>();
    
    private JFrame parentMainFrame;
    
    public KYCPanel(JFrame frame) {
    	this.parentMainFrame = frame;
		setLayout(new BorderLayout(0, 0));
		
		String[] wdStates = new String[]{"Open"};
		
		JPanel queryPanel = new QueryPanel(wdStates, this);
		add(queryPanel, BorderLayout.NORTH);
		
		Vector<String> columns = formTableColumns();
		Vector<Vector<String>> rowData = new Vector<>();
		
		tableModel.setDataVector(rowData, columns);
		tableView = new JTable(tableModel);
		
		tableView.setShowGrid(true);
		tableView.setShowVerticalLines(true);
		tableView.setRowSelectionAllowed(true);
		tableView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		popupMenu = new JPopupMenu();
        menuItemView = new JMenuItem("Dummy Item");
        menuItemUpdate = new JMenuItem("Update KYC Docs");
         
        menuItemView.addActionListener(this);
        menuItemUpdate.addActionListener(this);
         
        popupMenu.add(menuItemView);
        popupMenu.add(menuItemUpdate);
         
        // sets the popup menu for the table
        tableView.setComponentPopupMenu(popupMenu);
        
        tableView.addMouseListener(new TableMouseListener(tableView));
		JScrollPane tableScrollPane = new JScrollPane(tableView);
		
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout(0, 0));
		
		totalLabel.setText("Showing 0-0 of 0");
		
		tablePanel.add(totalLabel, BorderLayout.NORTH);
		tablePanel.add(tableScrollPane, BorderLayout.CENTER);
		
		add(tablePanel, BorderLayout.CENTER);
		
	
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		
		
		prevButton.setActionCommand("prev");
		prevButton.addActionListener(this);
		prevButton.setEnabled(false);
		
		buttonPanel.add(prevButton);
		
		
		nextButton.setActionCommand("next");
		nextButton.addActionListener(this);
		nextButton.setEnabled(false);
		
		buttonPanel.add(nextButton);
		
		add(buttonPanel, BorderLayout.SOUTH);
    }
    
    @Override
	public void actionPerformed(ActionEvent event) {
		String actionCmd = event.getActionCommand();
		if (actionCmd.equals("next")) {
			startNumber = startNumber + lastPageLength;
			String[] data = new String[1];
			data[0] = String.valueOf(lastPageLength);
			data[1] = String.valueOf(lastQueryState);
			passData(lastQueryType, data);
			
		} else if (actionCmd.equals("prev")) {
			startNumber = startNumber - lastPageLength;
			String[] data = new String[1];
			data[0] = String.valueOf(lastPageLength);
			data[1] = String.valueOf(lastQueryState);
			passData(lastQueryType, data);
		} else if (event.getSource() instanceof JMenuItem){
			JMenuItem menu = (JMenuItem) event.getSource();
			if (menuItemView == menu) {
				//int currentRow = tableView.getSelectedRow();
			} else if (menuItemUpdate == menu) {
				int currentRow = tableView.getSelectedRow();
				KYCEntry currentObject = currentList.get(currentRow);
				UpdateKYCDialog updateKYC = new UpdateKYCDialog(currentObject); 
				updateKYC.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				updateKYC.setVisible(true);
			}
		}
    }
    
    private void processDBQuery(final int operationType, final long getByIdVal, final int stateVal) {
		final JDialog loading = new JDialog();
		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(new JLabel("Please wait..."), BorderLayout.CENTER);
		loading.setUndecorated(true);
		loading.getContentPane().add(p1);
		loading.pack();
		loading.setSize(200, 100);
		loading.setLocationRelativeTo(parentMainFrame);
		loading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		loading.setModal(true);
		
		SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
			KYCEntriesHolder kycHolder = null; 
		    @Override
		    protected String doInBackground() throws InterruptedException {
		    	try {
		    		kycHolder = KYCDBHandler.getInstance().getKYCEntriesFromTool(operationType, 
							startNumber, getByIdVal, lastPageLength);
		    	} catch (NotAllowedException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
				}
		    	return "success";
		    }
		    @Override
		    protected void done() {
		    	loading.setVisible(false);
		    	loading.dispose();
		    	if (kycHolder == null) {
		    		return;
		    	}
		    	String totalPrefix = "Showing";
		        int start;
		        int end;
		        if (kycHolder.getTotal() == 0) {
		            start = 0;
		            end = 0;
		        } else {
		            start = startNumber + 1;
		            end = startNumber + kycHolder.getList().size();
		        }
		        final String totalStr = totalPrefix + start + " - " + end + " of " + kycHolder.getTotal();
		        currentList.clear();
		        currentList.addAll(kycHolder.getList());
		    	totalLabel.setText(totalStr);
				prevButton.setEnabled(kycHolder.isPrevEnabled());
				nextButton.setEnabled(kycHolder.isNextEnabled());
				Vector<String> columns = formTableColumns();
				Vector<Vector<String>> rowData = formTableRows(kycHolder);
				tableModel.setDataVector(rowData, columns);
				tableModel.fireTableDataChanged();
		    }
		};
		worker.execute(); //here the process thread initiates
		loading.setVisible(true);
		try {
		    worker.get(); //here the parent thread waits for completion
		} catch (Exception e1) {
		    e1.printStackTrace();
		}
	}

	@Override
	public void passData(int operationType, String[] data) {
		if (lastQueryType != operationType) {
			lastQueryType = operationType;
			startNumber = 0;
		}
		
		int pageLength = -1;
		long wdRecordId = -1;
		
		if (operationType == QueryPanel.IMPORTANT) {
			pageLength = Integer.parseInt(data[0]);
		} else if (operationType == QueryPanel.ALL_RECORDS) {
			pageLength = Integer.parseInt(data[0]);
		} else if (operationType == QueryPanel.GET_BY_ID) {
			try {
				wdRecordId = Long.parseLong(data[0]);
			} catch(NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Please Enter a valid id",
					      "Hey!", JOptionPane.ERROR_MESSAGE);
			}
		}
		lastPageLength = pageLength;
		lastQueryState = Integer.parseInt(data[1]);
		processDBQuery(lastQueryType, wdRecordId, lastQueryState);
	}
	
	private Vector<String> formTableColumns() {
		Vector<String> tableColumns = new Vector<String>();
		
		tableColumns.add("USERID");
		tableColumns.add("Aadhar Front Page");
		tableColumns.add("Aadhar Back Page");
		tableColumns.add("PAN Page");
		tableColumns.add("Last Updated Time");
		tableColumns.add("Status");
		
		return tableColumns;
	}
	
	private Vector<Vector<String>> formTableRows(KYCEntriesHolder kycHolder) {
		String datePattern = "dd:MMM:yyyy-HH:mm";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		
		Vector<Vector<String>> rows = new Vector<>();
		
		List<KYCEntry> rowsList = kycHolder.getList();
		for (KYCEntry row : rowsList) {
			Vector<String> rowVector = new Vector<String>();
			
			rowVector.add(String.valueOf(row.getUserId()));
			rowVector.add(String.valueOf(row.getAfpId()));
			rowVector.add(String.valueOf(row.getAbpId()));
			rowVector.add(String.valueOf(row.getPpId()));
			
			System.out.println("UT" + row.getLastUpdatedTime());
			Date openDate = new Date(row.getLastUpdatedTime());
            simpleDateFormat.applyPattern(datePattern);
            String openTimeStr = simpleDateFormat.format(openDate);
            rowVector.add(openTimeStr);
            
            rowVector.add(row.getStatus());
            
			rows.add(rowVector);
		}
		
		return rows;
	}
}
