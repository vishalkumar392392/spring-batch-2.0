package com.vishal.batch.listnersexternalflownestedjobsparallelflow;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

public class CustomerResponseDecider implements JobExecutionDecider {

	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		// TODO Auto-generated method stub
		double random = Math.random();
		if(random>0.5) {
			return new FlowExecutionStatus("HAPPY");
		}
		if(random<0.5) {
			return new FlowExecutionStatus("NOT_HAPPY");
		}
		return null;
	}

}
