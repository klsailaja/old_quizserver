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
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class CustomerCarePanel extends JPanel implements ActionListener, MessageListener {
	
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
    
    private List<CustomerTicket> currentList = new ArrayList<>();
    
    private JFrame parentMainFrame;
    
    public CustomerCarePanel(JFrame frame) {
		this.parentMainFrame = frame;
		setLayout(new BorderLayout(0, 0));
	
		String[] ccStates = new String[]{"Open", "Closed", "Not A Issue", "Cancelled"};
		JPanel queryPanel = new QueryPanel(ccStates, this);
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
        menuItemView = new JMenuItem("View Details");
        menuItemUpdate = new JMenuItem("Update Row");
         
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
				int currentRow = tableView.getSelectedRow();
				CustomerTicket currentObject = currentList.get(currentRow);
				ViewCustomerTicket viewTk = new ViewCustomerTicket(currentObject); 
				viewTk.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				viewTk.setVisible(true);
			} else if (menuItemUpdate == menu) {
				int currentRow = tableView.getSelectedRow();
				CustomerTicket currentObject = currentList.get(currentRow);
				UpdateCustomerTicket updateTkt = new UpdateCustomerTicket(currentObject);
				updateTkt.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				updateTkt.setVisible(true);
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
			CCTicketsHolder wdSet = null;
		    @Override
		    protected String doInBackground() throws InterruptedException {
		    	try {
		    		wdSet = CustomerCareDBHandler.getInstance().getCCTicketsFromTool(operationType, 
							startNumber, stateVal, getByIdVal, lastPageLength);
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
		    	if (wdSet == null) {
		    		return;
		    	}
		    	String totalPrefix = "Showing";
		        int start;
		        int end;
		        if (wdSet.getTotal() == 0) {
		            start = 0;
		            end = 0;
		        } else {
		            start = startNumber + 1;
		            end = startNumber + wdSet.getList().size();
		        }
		        final String totalStr = totalPrefix + start + " - " + end + " of " + wdSet.getTotal();
		        currentList.clear();
		        currentList.addAll(wdSet.getList());
		    	totalLabel.setText(totalStr);
				prevButton.setEnabled(wdSet.isPrevEnabled());
				nextButton.setEnabled(wdSet.isNextEnabled());
				Vector<String> columns = formTableColumns();
				Vector<Vector<String>> rowData = formTableRows(wdSet);
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
			wdRecordId = Long.parseLong(data[0]);
		}
		lastPageLength = pageLength;
		lastQueryState = Integer.parseInt(data[1]);
		processDBQuery(lastQueryType, wdRecordId, lastQueryState);
	}
	
	private Vector<String> formTableColumns() {
		Vector<String> tableColumns = new Vector<String>();
		
		tableColumns.add("ID");
		tableColumns.add("ReferenceID");
		tableColumns.add("UserProfileID");
		tableColumns.add("Request Type");
		tableColumns.add("Open Time");
		tableColumns.add("Status");
		
		return tableColumns;
	}
	
	private Vector<Vector<String>> formTableRows(CCTicketsHolder holder) {
		String datePattern = "dd:MMM:yyyy-HH:mm";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		
		Vector<Vector<String>> rows = new Vector<>();
		
		List<CustomerTicket> rowsList = holder.getList();
		for (CustomerTicket row : rowsList) {
			Vector<String> rowVector = new Vector<String>();
			
			rowVector.add(String.valueOf(row.getId()));
			rowVector.add(row.getRefId());
			rowVector.add(String.valueOf(row.getUserId()));
			
			CustomerCareReqType reqType = CustomerCareReqType.findById(row.getRequestType()); 
			rowVector.add(reqType.name());
						
			Date openDate = new Date(row.getOpenedTime());
            simpleDateFormat.applyPattern(datePattern);
            String openTimeStr = simpleDateFormat.format(openDate);
            rowVector.add(openTimeStr);
            
            CustomerCareReqState reqState = CustomerCareReqState.findById(row.getStatus());
            rowVector.add(reqState.name());
            
			rows.add(rowVector);
		}
		
		return rows;
	}
}
