package com.vishal.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class EmployeeProcessor implements ItemProcessor<Employee, Employee> {

	@Override
	public Employee process(Employee item) throws Exception {
		
		System.out.println("Processing employee: "+ item.getFirstName());
		
		return item;
	}

}
