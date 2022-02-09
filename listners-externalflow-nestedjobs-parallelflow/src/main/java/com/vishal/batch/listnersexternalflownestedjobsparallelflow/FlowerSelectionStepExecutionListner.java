package com.vishal.batch.listnersexternalflownestedjobsparallelflow;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class FlowerSelectionStepExecutionListner implements StepExecutionListener {

	@Override
	public void beforeStep(StepExecution stepExecution) {

		System.out.println("Execution before step logic");
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {

		String flowerType = stepExecution.getJobParameters().getString("type");
		return flowerType.equalsIgnoreCase("roses") ? new ExitStatus("TRIM_REQUIRED"): new ExitStatus("NO_TRIM_REQUIRED");
	}

}
