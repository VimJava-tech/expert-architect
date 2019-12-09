package com.rabo.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "records")
public class Records {
	
	List<Transaction> records;

	public List<Transaction> getRecords() {
		return records;
	}

	@XmlElement(name = "record")
	public void setRecords(List<Transaction> records) {
		this.records = records;
	}

}
