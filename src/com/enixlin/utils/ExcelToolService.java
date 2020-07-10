/**
 * 
 */
package com.enixlin.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * @author linzhenhuan
 *
 */
public class ExcelToolService {
	
	
	/**
	 * 将数据写入excel文件中
	 * 
	 * @author linzhenhuan </br>
	 * 方法说明： </br>
	 * @param allUnitPerformance
	 * @param excelPath void 创建时间：2019年8月29日
	 */
	public void exportToexcel(ArrayList<LinkedHashMap<String, String>> allUnitPerformance,
			String excelPath, String start, String end, String unit,
			String title) {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(excelPath);
		int recordColumns = allUnitPerformance.get(0).size();
		if (recordColumns <= 1) {
			recordColumns = 1;
		}
		if (allUnitPerformance.size() == 0) {
			return;
		}
		int nRowNum = 0;

		// 这里要实现一个表格标题
		HSSFRow rowTitle = sheet.createRow(nRowNum);
		CellRangeAddress region1 = new CellRangeAddress(nRowNum, (short) nRowNum,0,
				(short) recordColumns - 1);
		sheet.addMergedRegion(region1);
		HSSFCell cellTitle = rowTitle.createCell(0);
		CellStyle cs = wb.createCellStyle();
		// 水平居中对齐
		cs.setAlignment(HorizontalAlignment.CENTER);
		cellTitle.setCellStyle(cs);
		cellTitle.setCellValue(title);
		nRowNum++;

		// 添加表格日期时间和单位
		HSSFRow rowDate = sheet.createRow(nRowNum);
		HSSFCell cellDate = rowDate.createCell(0);
		CellRangeAddress region2 = new CellRangeAddress(nRowNum, nRowNum,
				(short) 0, (short) 2);
		sheet.addMergedRegion(region2);
		cellDate.setCellValue("统计时段：" + start + "-" + end);
		HSSFCell cellUnit = rowDate.createCell(recordColumns - 1);
		cellUnit.setCellValue(unit);
		nRowNum++;

		// 先将hashmap的键值取出来，做表头
		HSSFRow row = sheet.createRow(nRowNum);
		nRowNum++;
		int nColumnNum = 0;
		for (String key : allUnitPerformance.get(0).keySet()) {
			HSSFCell cell = row.createCell(nColumnNum);
			CellStyle cellStyle = wb.createCellStyle(); // 创建单元格样式
			cell.setCellValue(key);
			// 添加边框
			// 水平居中对齐
			this.setAllBorder(cellStyle);
			cellStyle.setAlignment(HorizontalAlignment.CENTER);
			cell.setCellStyle(cellStyle);
			nColumnNum++;
		}
		// 将数据写入
		CellStyle cellStyle = wb.createCellStyle(); // 创建单元格样式
		for (HashMap<String, String> record : allUnitPerformance) {
			HSSFRow rowData = sheet.createRow(nRowNum);
			nColumnNum = 0;
			for (String key : record.keySet()) {
				HSSFCell cell = rowData.createCell(nColumnNum);
				// 在这里可以做格式化判断，将数值格式化
				if (key.contains("金额") || key.contains("笔数")
						|| key.contains("度任务")) {

					if (key.contains("金额")) {
						cell.setCellValue(
								Double.valueOf(record.get(key).toString()));
						
						cellStyle.setAlignment(HorizontalAlignment.RIGHT); // 设置单元格水平方向对其方式
						cellStyle
								.setVerticalAlignment(VerticalAlignment.CENTER); // 设置单元格垂直方向对其方式
						HSSFDataFormat format = wb.createDataFormat();
						// 添加边框
						this.setAllBorder(cellStyle);
						cellStyle.setDataFormat(
								format.getFormat("#,##0.00_);[Red](#,##0.00)"));
						cell.setCellStyle(cellStyle); // 设置单元格样式
					}
					if (key.contains("笔数")) {
						cell.setCellValue(
								Double.valueOf(record.get(key).toString()));
//						CellStyle cellStyle = wb.createCellStyle(); // 创建单元格样式
						cellStyle.setAlignment(HorizontalAlignment.RIGHT); // 设置单元格水平方向对其方式
						cellStyle
								.setVerticalAlignment(VerticalAlignment.CENTER); // 设置单元格垂直方向对其方式
						HSSFDataFormat format = wb.createDataFormat();
						// 添加边框
						this.setAllBorder(cellStyle);
						cellStyle.setDataFormat(
								format.getFormat("#,##0_);[Red](#,##0)"));
						cell.setCellStyle(cellStyle); // 设置单元格样式
					}
					if (key.contains("度任务")) {
						cell.setCellValue(
								Double.valueOf(record.get(key).toString()));
//						CellStyle cellStyle = wb.createCellStyle(); // 创建单元格样式
						cellStyle.setAlignment(HorizontalAlignment.RIGHT); // 设置单元格水平方向对其方式
						cellStyle
								.setVerticalAlignment(VerticalAlignment.CENTER); // 设置单元格垂直方向对其方式
						HSSFDataFormat format = wb.createDataFormat();
						// 添加边框
						this.setAllBorder(cellStyle);
						cellStyle.setDataFormat(
								format.getFormat("#,##0_);[Red](#,##0)"));
						cell.setCellStyle(cellStyle); // 设置单元格样式
					}

				} else if (key.contains("完成率")) {
					cell.setCellValue(
							Double.valueOf(record.get(key).toString()));
//					CellStyle cellStyle = wb.createCellStyle(); // 创建单元格样式
					cellStyle.setAlignment(HorizontalAlignment.RIGHT); // 设置单元格水平方向对其方式
					cellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 设置单元格垂直方向对其方式
					// 添加边框
					this.setAllBorder(cellStyle);

					if (cell.getNumericCellValue() >= 100) {
						HSSFDataFormat format = wb.createDataFormat();
						cellStyle.setDataFormat(
								format.getFormat("[green]#,##0.00"));
					} else if (cell.getNumericCellValue() >= 50) {
						HSSFDataFormat format = wb.createDataFormat();
						cellStyle.setDataFormat(
								format.getFormat("[blue]#,##0.00"));
					} else {
						HSSFDataFormat format = wb.createDataFormat();
						cellStyle.setDataFormat(
								format.getFormat("[Red]#,##0.00"));
					}
					cell.setCellStyle(cellStyle); // 设置单元格样式
					
				} else {
//					CellStyle cellStyle = wb.createCellStyle(); // 创建单元格样式
					// 添加边框
					this.setAllBorder(cellStyle);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(String.valueOf(record.get(key)));
				}

				nColumnNum++;
			}
			nRowNum++;
		}

		this.autoSizeColumn(sheet, recordColumns);

		try {
			File f = new File("输出文件\\");
			if (!f.exists()) {
				f.mkdirs();
			}
			wb.write(new File("输出文件\\"+excelPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void setAllBorder(CellStyle cellStyle) {
		cellStyle.setBorderBottom(BorderStyle.THIN); // 下边框
		cellStyle.setBorderLeft(BorderStyle.THIN);// 左边框
		cellStyle.setBorderTop(BorderStyle.THIN);// 上边框
		cellStyle.setBorderRight(BorderStyle.THIN);// 右边框
	}

	public void autoSizeColumn(HSSFSheet sheet, int nColumns) {
		int j = 0;
		while (j < nColumns) {
			sheet.autoSizeColumn((short) j);
			j++;
		}
	}


}
