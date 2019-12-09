package com.rabo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import com.rabo.processor.StatementProcessor;
import com.rabo.processor.impl.StatementProcessorImpl;
import com.rabo.reader.impl.CSVReaderImpl;
import com.rabo.reader.impl.XMLReaderImpl;

@Configuration
@ComponentScan("com.rabo")
@PropertySource("classpath:application.properties")
public class StatementProcessorConfig {
	
	@Bean 
	public StatementConfig statementConfig(){
		return new StatementConfig();
	}
	
	public StatementProcessor statementProcessor()
	{
		return new StatementProcessorImpl();
	}
	
	public CSVReaderImpl csvReaderImpl()
	{
		return new CSVReaderImpl();
	}
	
	public XMLReaderImpl xmlReaderImpl()
	{
		return new XMLReaderImpl();
	}
	

}
