package com.rabo.processor.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.rabo.config.StatementConfig;
import com.rabo.model.StatementReport;
import com.rabo.model.Transaction;
import com.rabo.processor.StatementProcessor;
import com.rabo.reader.impl.CSVReaderImpl;
import com.rabo.reader.impl.XMLReaderImpl;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

@Component
public class StatementProcessorImpl implements StatementProcessor {

	static Logger logger = Logger.getLogger(StatementProcessorImpl.class.getName());

	@Autowired
	StatementConfig statementConfig;

	@Autowired
	CSVReaderImpl csvReaderImpl;

	@Autowired
	XMLReaderImpl xmlReaderImpl;

	/**
	 * This method will process statement and outputs the failed transactions
	 */
	@Override
	public void processStatements() {
		logger.info("Entering processStatements Method");
		List<Transaction> transactions = new ArrayList<Transaction>();
		List<Transaction> xmlTransactions = getTransactions("xml");
		List<Transaction> csvTransactions = getTransactions("csv");
		if (csvTransactions != null && csvTransactions.size() > 0)
			transactions.addAll(csvTransactions);
		if (xmlTransactions != null && xmlTransactions.size() > 0)
			transactions.addAll(xmlTransactions);

		Map<String, Long> transactionCount = transactions.stream()
				.collect(Collectors.groupingBy(Transaction::getTxnReference, Collectors.counting()));
		List<String> duplicateTxn = new ArrayList<String>();
		for (Map.Entry<String, Long> entry : transactionCount.entrySet()) {
			if (entry.getValue() > 1)
				duplicateTxn.add(entry.getKey());
		}
		List<String> invalidBalanceTxn = transactions.stream().filter(txn -> isValidBalance(txn))
				.map(Transaction::getTxnReference).collect(Collectors.toList());
		List<StatementReport> report = new ArrayList<StatementReport>();
		List<StatementReport> duplicateTxnReport = getStatementReport(duplicateTxn,
				"Duplicate Transaction Reference Exist");
		List<StatementReport> invalidBalTxnReport = getStatementReport(invalidBalanceTxn,
				"Invalid End Balance Calculation");
		if (duplicateTxnReport != null & duplicateTxnReport.size() > 0)
			report.addAll(duplicateTxnReport);
		if (invalidBalTxnReport != null & invalidBalTxnReport.size() > 0)
			report.addAll(invalidBalTxnReport);
		String fileName = generateReport(report);

		logger.info("Generated Report Path : " + fileName);

		logger.info("Exiting processStatements Method");
	}

	/**
	 * This method retrieves list of failed transactions after validation
	 * 
	 * @param failedTxns
	 * @param errorMessage
	 * @return
	 */
	private List<StatementReport> getStatementReport(List<String> failedTxns, String errorMessage) {
		logger.info("Entering getStatementReport Method");
		List<StatementReport> report = new ArrayList<StatementReport>();
		for (String failedTxn : failedTxns) {
			StatementReport failedValidation = new StatementReport();
			failedValidation.setTxnReference(failedTxn);
			failedValidation.setDescription(errorMessage);
			report.add(failedValidation);
		}
		logger.info("Exiting getStatementReport Method");
		return report;
	}

	/**
	 * This method validates the balance for transactions
	 * 
	 * @param txn
	 * @return
	 */
	private boolean isValidBalance(Transaction txn) {
		logger.info("Entering isValidBalance Method");
		if (txn.getStartBalance() != null && txn.getMutation() != null && txn.getEndBalance() != null) {

			return Math.round(txn.getStartBalance() + txn.getMutation()) != Math.round(txn.getEndBalance());
		}
		logger.info("Exiting isValidBalance Method");
		return false;
	}

	/**
	 * Returns List of Transactions from the Input Files
	 * 
	 * @param fileType
	 * @return
	 */
	private List<Transaction> getTransactions(String fileType) {
		logger.info("Entering getTransactions Method");
		Stream<Path> paths = null;
		try {
			paths = Files.walk(Paths.get(statementConfig.getStatementPath()));
		} catch (IOException e) {

			logger.error("Unable to access input file path :" + statementConfig.getStatementPath());
		}
		List<File> csvFiles = paths.filter(Files::isRegularFile).map(Path::toFile)
				.filter(file -> file.getName().endsWith(fileType)).collect(Collectors.toList());
		List<Transaction> transactions = new ArrayList<>();
		if (fileType.equalsIgnoreCase("csv"))
			transactions = csvReaderImpl.getTransactions(csvFiles);
		else if (fileType.equalsIgnoreCase("xml"))
			transactions = xmlReaderImpl.getTransactions(csvFiles);

		logger.info("Exiting getTransactions Method");
		return transactions;
	}

	/**
	 * Generates report for Failed Transactions
	 * 
	 * @param report
	 * @return
	 */
	public String generateReport(List<StatementReport> report) {
		logger.info("Entering generateReport Method");
		HSSFWorkbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("Report");
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 13);
		headerFont.setColor(IndexedColors.BLUE.getIndex());
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		Row headerRow = sheet.createRow(0);
		Cell txnHeaderCell = headerRow.createCell(0);
		txnHeaderCell.setCellValue("TxnReference");
		txnHeaderCell.setCellStyle(headerCellStyle);
		Cell descriptionCell = headerRow.createCell(1);
		descriptionCell.setCellValue("Description");
		descriptionCell.setCellStyle(headerCellStyle);
		int rowNum = 1;
		for (StatementReport stmtReport : report) {
			Row row = sheet.createRow(rowNum++);

			row.createCell(0).setCellValue(stmtReport.getTxnReference());

			row.createCell(1).setCellValue(stmtReport.getDescription());

		}
		String fileName = getFileName();
		try {
			if (fileName != null) {
				FileOutputStream fileOut = new FileOutputStream(fileName);
				workbook.write(fileOut);
				fileOut.close();
				workbook.close();
			}

		} catch (IOException e) {
			logger.error("Error Occured during report generation");
		}
		logger.info("Exiting generateReport Method");
		return fileName;

	}

	/**
	 * This method creates Report File Name - report+time in minutes
	 * 
	 * @return
	 */
	public String getFileName() {
		logger.info("Entering getFileName Method");
		Date dt = new Date();
		Path path = Paths.get(statementConfig.getStatementReportPath());
		String fileName = null;
		if (Files.notExists(path))
			logger.error("Output Folder path is not exist. Report Creation Failed - "
					+ statementConfig.getStatementReportPath());
		else
			fileName = statementConfig.getStatementReportPath() + "\\report" + dt.getTime() + ".xls";
		logger.info("Exiting getFileName Method");
		return fileName;
	}

}
