package com.vishal.batch;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmployeeWriter implements ItemWriter<Employee> {
	
	
	@Autowired
	private EmployeeRepository repository;

	@Override
	public void write(List<? extends Employee> items) throws Exception {
		
		repository.saveAll(items);

	}

}
