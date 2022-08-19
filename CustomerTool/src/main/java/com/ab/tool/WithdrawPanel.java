package com.ab.tool;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class WithdrawPanel extends JPanel implements ActionListener, MessageListener {
	
	private static final long serialVersionUID = 1L;
	
	private int startNumber = 0;
	private int lastQueryType = -1;
	private int lastPageLength = -1;
	
	private DefaultTableModel tableModel = new DefaultTableModel();
	private JTable tableView;
	private JLabel totalLabel = new JLabel();
	private JButton prevButton = new JButton("<<Previous");
	private JButton nextButton = new JButton("Next>>");
	
	private JPopupMenu popupMenu;
    private JMenuItem menuItemView;
    private JMenuItem menuItemUpdate;
    
    private List<WithdrawRequest> currentWDList = new ArrayList<>();
    
    private JFrame parentMainFrame;
	
	public WithdrawPanel(JFrame frame) {
		this.parentMainFrame = frame;
		setLayout(new BorderLayout(0, 0));
		
		JPanel queryPanel = new QueryPanel(this);
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
        menuItemUpdate = new JMenuItem("Update Receipt");
         
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
		
		totalLabel.setText("Showing 1-10 of 50");
		
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
		
		JButton exportButton = new JButton("Export");
		exportButton.setActionCommand("export");
		exportButton.addActionListener(this);
		
		buttonPanel.add(exportButton);
		
		add(buttonPanel, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String actionCmd = event.getActionCommand();
		if (actionCmd.equals("next")) {
			startNumber = startNumber + lastPageLength;
			String[] data = new String[1];
			data[0] = String.valueOf(lastPageLength);
			passData(lastQueryType, data);
			
		} else if (actionCmd.equals("prev")) {
			startNumber = startNumber - lastPageLength;
			String[] data = new String[1];
			data[0] = String.valueOf(lastPageLength);
			passData(lastQueryType, data);
		} else if (actionCmd.equals("export")) {
			Map<Integer, List<WithdrawRequest>> categorizedWDPayments = new HashMap<>();
			
			for (WithdrawRequest wd : currentWDList) {
				Integer paymentType = wd.getRequestType();
				System.out.println("paymentType" + paymentType);
				List<WithdrawRequest> list = categorizedWDPayments.get(paymentType);
				if (list == null) {
					list = new ArrayList<>();
				}
				list.add(wd);
				categorizedWDPayments.put(paymentType, list);
			}
			
			try (XSSFWorkbook workbook = new XSSFWorkbook()) {
				// spreadsheet object
				List<WithdrawRequest> phoneWDList = categorizedWDPayments.get(WithdrawReqType.BY_PHONE.getId());
				if (phoneWDList == null) {
					phoneWDList = new ArrayList<>();
				}
				System.out.println("phoneWDList size" + phoneWDList.size());
				if (phoneWDList.size() > 0) {
					XSSFSheet spreadsheet
					    = workbook.createSheet("PhonePayments");
					XSSFRow row;
					int rowId = 0;
					
					row = spreadsheet.createRow(rowId++);
					Cell cell = row.createCell(0);
					cell.setCellValue("SN");
					
					cell = row.createCell(1);
					cell.setCellValue("ID");
					
					cell = row.createCell(2);
					cell.setCellValue("RefID");
					
					cell = row.createCell(3);
					cell.setCellValue("UserProfileId");
					
					cell = row.createCell(4);
					cell.setCellValue("RequestType");
					
					cell = row.createCell(5);
					cell.setCellValue("Amount");
					
					cell = row.createCell(6);
					cell.setCellValue("OpenedTime");
					
					cell = row.createCell(7);
					cell.setCellValue("PhoneNumber");
					
					cell = row.createCell(8);
					cell.setCellValue("PaymentType");
					
					cell = row.createCell(9);
					cell.setCellValue("A/C Holder Name");
					
					for (WithdrawRequest wd : phoneWDList) {
						row = spreadsheet.createRow(rowId++);
						
						int cellid = 0;
						cell = row.createCell(cellid++);
						cell.setCellValue(String.valueOf(rowId - 1));
						
						cell = row.createCell(cellid++);
						cell.setCellValue(wd.getId());
						
						cell = row.createCell(cellid++);
						cell.setCellValue(wd.getRefId());
						
						cell = row.createCell(cellid++);
						cell.setCellValue(wd.getUserProfileId());
						
						cell = row.createCell(cellid++);
						WithdrawReqType wdReqType = WithdrawReqType.findById(wd.getRequestType());
						cell.setCellValue(wdReqType.name());
						
						cell = row.createCell(cellid++);
						cell.setCellValue(wd.getAmount());
						
						cell = row.createCell(cellid++);
						DateFormat df = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
						cell.setCellValue(df.format(wd.getOpenedTime()));
						//cell.setCellValue(wd.getOpenedTime());
						
						cell = row.createCell(cellid++);
						cell.setCellValue(wd.getByPhone().getPhNumber());
						
						cell = row.createCell(cellid++);
						PhonePaymentTypes phPaymentType = PhonePaymentTypes.findById(wd.getByPhone().getPaymentMethod()); 
						cell.setCellValue(phPaymentType.name());
					}
					
					List<WithdrawRequest> bankWDList = categorizedWDPayments.get(WithdrawReqType.BY_BANK.getId());
					if (bankWDList == null) {
						bankWDList = new ArrayList<>();
					}
					System.out.println("bankWDList size" + bankWDList.size());
					if (bankWDList.size() > 0) {
						XSSFSheet bankSheet
						    = workbook.createSheet("BankPayments");
						rowId = 0;
						
						row = bankSheet.createRow(rowId++);
						cell = row.createCell(0);
						cell.setCellValue("SN");
						
						cell = row.createCell(1);
						cell.setCellValue("ID");
						
						cell = row.createCell(2);
						cell.setCellValue("RefID");
						
						cell = row.createCell(3);
						cell.setCellValue("UserProfileId");
						
						cell = row.createCell(4);
						cell.setCellValue("RequestType");
						
						cell = row.createCell(5);
						cell.setCellValue("Amount");
						
						cell = row.createCell(6);
						cell.setCellValue("OpenedTime");
						
						cell = row.createCell(7);
						cell.setCellValue("Bank A/C Number");
						
						cell = row.createCell(8);
						cell.setCellValue("Bank IFSC Code");
						
						cell = row.createCell(9);
						cell.setCellValue("Bank A/C Holder Name");
						
						for (WithdrawRequest wd : phoneWDList) {
							row = spreadsheet.createRow(rowId++);
							
							int cellid = 0;
							cell = row.createCell(cellid++);
							cell.setCellValue(String.valueOf(rowId - 1));
							
							cell = row.createCell(cellid++);
							cell.setCellValue(wd.getId());
							
							cell = row.createCell(cellid++);
							cell.setCellValue(wd.getRefId());
							
							cell = row.createCell(cellid++);
							cell.setCellValue(wd.getUserProfileId());
							
							cell = row.createCell(cellid++);
							WithdrawReqType wdReqType = WithdrawReqType.findById(wd.getRequestType());
							cell.setCellValue(wdReqType.name());
							
							cell = row.createCell(cellid++);
							cell.setCellValue(wd.getAmount());
							
							cell = row.createCell(cellid++);
							DateFormat df = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
							cell.setCellValue(df.format(wd.getOpenedTime()));
							
							cell = row.createCell(cellid++);
							cell.setCellValue(wd.getByPhone().getPhNumber());
							
							cell = row.createCell(cellid++);
							PhonePaymentTypes phPaymentType = PhonePaymentTypes.findById(wd.getByPhone().getPaymentMethod()); 
							cell.setCellValue(phPaymentType.name());
						}
					}
					String fileNameSuffix = "WithdrawReqs_" + getDateTime() + ".xlsx"; 
					FileOutputStream out = new FileOutputStream(
					        new File("D:\\ExportWithdrawRequests\\" + fileNameSuffix));
					workbook.write(out);
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (event.getSource() instanceof JMenuItem){
			JMenuItem menu = (JMenuItem) event.getSource();
			if (menuItemView == menu) {
				System.out.println(tableView.getSelectedRow());
			} else if (menuItemUpdate == menu) {
				int currentRow = tableView.getSelectedRow();
				WithdrawRequest currentWDRequest = currentWDList.get(currentRow);
				UpdateReceipt updateReceipt = new UpdateReceipt(currentWDRequest);
				updateReceipt.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				updateReceipt.setVisible(true);
			}
		}
	}
	
	private void processDBQuery(final int operationType, final long getByIdVal) {
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
			WithdrawRequestsHolder wdSet = null;
		    @Override
		    protected String doInBackground() throws InterruptedException {
		    	try {
		    		wdSet = WithdrawDBHandler.getInstance().getWithdrawRequests(operationType, 
							startNumber, WithdrawReqState.OPEN.getId(), getByIdVal, lastPageLength);
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
		        currentWDList.clear();
		        currentWDList.addAll(wdSet.getList());
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
		processDBQuery(lastQueryType, wdRecordId);
	}
	
	private Vector<String> formTableColumns() {
		Vector<String> tableColumns = new Vector<String>();
		
		tableColumns.add("ID");
		tableColumns.add("ReferenceID");
		tableColumns.add("UserProfileID");
		tableColumns.add("Request Status");
		tableColumns.add("Amount");
		tableColumns.add("Open Time");
		tableColumns.add("Closed Time");
		tableColumns.add("Closed Coments");
		tableColumns.add("Receipt ID");
		
		return tableColumns;
	}
	
	private Vector<Vector<String>> formTableRows(WithdrawRequestsHolder wdDataHolder) {
		String datePattern = "dd:MMM:yyyy-HH:mm";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		
		Vector<Vector<String>> rows = new Vector<>();
		
		List<WithdrawRequest> rowsList = wdDataHolder.getList();
		for (WithdrawRequest row : rowsList) {
			Vector<String> rowVector = new Vector<String>();
			
			rowVector.add(String.valueOf(row.getId()));
			rowVector.add(row.getRefId());
			rowVector.add(String.valueOf(row.getUserProfileId()));
			
			int status = row.getReqStatus();
			String statusStr = "Open";
			if (status == 1) {
				statusStr = "Open";
			} else if (status == 2) {
				statusStr = "Closed";
			} else if (status == 3) {
				statusStr = "Cancelled";
			}
			
			rowVector.add(statusStr);
			rowVector.add(String.valueOf(row.getAmount()));
			
			Date openDate = new Date(row.getOpenedTime());
            simpleDateFormat.applyPattern(datePattern);
            String openTimeStr = simpleDateFormat.format(openDate);
            rowVector.add(openTimeStr);
            
            Date closedDate = new Date(row.getClosedTime());
            simpleDateFormat.applyPattern(datePattern);
            String closedTimeStr = simpleDateFormat.format(closedDate);
            rowVector.add(closedTimeStr);
            
            rowVector.add(row.getClosedComents());
            
            rowVector.add(String.valueOf(row.getReceiptId()));
            
			rows.add(rowVector);
		}
		
		return rows;
	}
	
	private final static String getDateTime()
	{
	    DateFormat df = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
	    return df.format(new Date());
	}
}
