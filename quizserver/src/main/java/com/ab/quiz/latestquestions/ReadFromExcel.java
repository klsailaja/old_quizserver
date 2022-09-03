package com.ab.quiz.latestquestions;

import java.io.File;  
import java.io.FileInputStream;  
import java.util.Iterator;  
import org.apache.poi.ss.usermodel.Cell;  
import org.apache.poi.ss.usermodel.Row;  
import org.apache.poi.xssf.usermodel.XSSFSheet;  
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReadFromExcel  
{  
	public static void main(String[] args)   
	{  
		try  
		{  
			File file = new File("D:\\Projects\\Games\\MovieDetails.xlsx");  
			FileInputStream fis = new FileInputStream(file);   //obtaining bytes from the file  
			try (//creating Workbook instance that refers to .xlsx file  
			XSSFWorkbook wb = new XSSFWorkbook(fis)) {
				XSSFSheet sheet = wb.getSheetAt(0);     //creating a Sheet object to retrieve object  
				Iterator<Row> itr = sheet.iterator();    //iterating over excel file  
				while (itr.hasNext())                 
				{  
					Row row = itr.next();  
					Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column  
					while (cellIterator.hasNext())   
					{  
						Cell cell = cellIterator.next();
						System.out.print(cell.getStringCellValue() + "\t\t\t");
					}  
					System.out.println("");  
				}
			}  
		}  
		catch(Exception e)  
		{  
			e.printStackTrace();  
		}  
	}  
} 