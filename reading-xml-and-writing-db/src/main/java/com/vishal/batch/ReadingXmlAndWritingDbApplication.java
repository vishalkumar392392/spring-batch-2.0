package com.vishal.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
public class ReadingXmlAndWritingDbApplication {
	
	
	@Autowired
	private JobLauncher jobLaucher;
	
	@Autowired
	private Job job;
	
	
	@Scheduled(cron = "0/10 * * * * *")
	public void run() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		JobParametersBuilder paramBuilder = new JobParametersBuilder();
		paramBuilder.addString("runTime", String.valueOf(System.currentTimeMillis()));
		this.jobLaucher.run(job, paramBuilder.toJobParameters());
	}

	public static void main(String[] args) {
		SpringApplication.run(ReadingXmlAndWritingDbApplication.class, args);
	}

}
