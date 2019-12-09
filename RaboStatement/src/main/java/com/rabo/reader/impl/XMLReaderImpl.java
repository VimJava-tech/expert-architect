package com.rabo.reader.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.rabo.model.Records;
import com.rabo.model.Transaction;
import com.rabo.processor.impl.StatementProcessorImpl;
import com.rabo.reader.FileReader;

@Component
public class XMLReaderImpl implements FileReader {
	static Logger logger = Logger.getLogger(StatementProcessorImpl.class.getName());

	/**
	 * This method retrieves list of transactions from XML files
	 */
	@Override
	public List<Transaction> getTransactions(List<File> files) {

		logger.info("Entering getTransactions XML Method");
		List<Records> recordsList = new ArrayList<Records>();
		try {

			Records records = new Records();
			JAXBContext jaxbContext = JAXBContext.newInstance(Records.class);

			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			for (File file : files) {
				records = (Records) unmarshaller.unmarshal(file);
				recordsList.add(records);
			}

		} catch (JAXBException e) {
			logger.error("Error converting the xml to Java Object " + e);

		}
		logger.info("Exiting getTransactions XML Method");
		return fetchTransactionList(recordsList);
	}

	private List<Transaction> fetchTransactionList(List<Records> recordsList) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		if (recordsList != null & recordsList.size() > 0) {
			for (Records records : recordsList) {
				transactions.addAll(records.getRecords());
			}
		}
		return transactions;

	}

}
