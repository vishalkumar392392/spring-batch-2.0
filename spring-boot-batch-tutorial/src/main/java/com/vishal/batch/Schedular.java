package com.vishal.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class Schedular {
	
	@Autowired
	private Job job;
	
	@Autowired
	private JobLauncher jobLauncher;
	
	
	@Scheduled(cron = "0/5 * * * * *")
	public void run () throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		JobParametersBuilder paramBuilder = new JobParametersBuilder();
		paramBuilder.addString("runTime", String.valueOf(System.currentTimeMillis()));
		jobLauncher.run(job, paramBuilder.toJobParameters());
	}

}
