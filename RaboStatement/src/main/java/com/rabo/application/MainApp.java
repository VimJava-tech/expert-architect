package com.rabo.application;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.rabo.config.StatementProcessorConfig;
import com.rabo.processor.StatementProcessor;

public class MainApp {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = 
		         new AnnotationConfigApplicationContext();
		ctx.register(StatementProcessorConfig.class);
		ctx.refresh();
		StatementProcessor processor = ctx.getBean(StatementProcessor.class);
		processor.processStatements();

	}

}
