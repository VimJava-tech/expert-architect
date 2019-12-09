package com.rabo.reader;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import com.rabo.model.Transaction;

public interface FileReader {
	
	public List<Transaction> getTransactions(List<File> files);

}
