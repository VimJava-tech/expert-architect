package com.rabo.reader.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.rabo.model.Transaction;
import com.rabo.processor.impl.StatementProcessorImpl;
import com.rabo.reader.FileReader;

@Component
public class CSVReaderImpl implements FileReader {

	static Logger logger = Logger.getLogger(StatementProcessorImpl.class.getName());

	/**
	 * Retrieves List of Transactions from list of CSV files
	 */
	@Override
	public List<Transaction> getTransactions(List<File> files) {
		logger.info("Entering getTransactions Method");
		List<Transaction> allTransactions = new ArrayList<>();
		for (File file : files) {
			try (Stream<String> stream = Files.lines(Paths.get(file.toURI()))) {

				allTransactions = stream.skip(1).map(mapToTransaction).collect(Collectors.toList());

			} catch (IOException e) {
				logger.error("Error converting csv to Java Object :" + e);
			}

		}
		logger.info("Exiting getTransactions Method");
		return allTransactions;
	}

	/**
	 * This method validates that a number can start with either + or -
	 * 
	 * @param data
	 * @return
	 */
	private boolean isNumber(String data) {
		return data != null && data.length() > 0 && data.matches("[-+]?[0-9]*\\.?[0-9]*");
	}

	/**
	 * This function converts the string into transaction
	 */
	private Function<String, Transaction> mapToTransaction = (line) -> {
		String[] p = line.split(",");// a CSV has comma separated lines
		Transaction item = new Transaction();
		if (p.length == 6) {
			item.setTxnReference(p[0]);
			item.setAcctNumber(p[1]);
			item.setDescription(p[2]);
			if (isNumber(p[3]))
				item.setStartBalance(Double.parseDouble(p[3]));
			if (isNumber(p[4]))
				item.setMutation(Double.parseDouble(p[4]));
			if (isNumber(p[5]))
				item.setEndBalance(Double.parseDouble(p[5]));

		}
		return item;
	};

}
