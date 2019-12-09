package com.rabo.config;

import org.springframework.beans.factory.annotation.Value;

public class StatementConfig {
	
	@Value("${statement.filepath}")
	private String statementPath;
	
	public String getStatementPath() {
		return statementPath;
	}

	public String getStatementReportPath() {
		return statementReportPath;
	}

	@Value("${report.outputfilepath}")
	private String statementReportPath;
	


}
