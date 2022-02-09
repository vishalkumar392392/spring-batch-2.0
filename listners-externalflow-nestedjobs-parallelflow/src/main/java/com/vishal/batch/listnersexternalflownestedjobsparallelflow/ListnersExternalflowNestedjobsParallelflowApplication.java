package com.vishal.batch.listnersexternalflownestedjobsparallelflow;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;


@SpringBootApplication
@EnableBatchProcessing
public class ListnersExternalflowNestedjobsParallelflowApplication {
	

	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	
	@Bean
	public JobExecutionDecider customerDecider() {
		return new CustomerResponseDecider();
	}
	
	@Bean
	public JobExecutionDecider decider() {
		return new DeliveryDecider();
	}
	
	@Bean
	public Step thankYouStep() {
		return this.stepBuilderFactory.get("thankYouStep").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("thankYou customer..");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Step giveRefundStep() {
		return this.stepBuilderFactory.get("giveRefundStep").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Refund is initaited..");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Step leaveAtStepDoor() {
		return this.stepBuilderFactory.get("leaveAtStepDoor").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Leaving the package at the door...");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Step storePackageStep() {
		return this.stepBuilderFactory.get("storePackageStep").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Storing the package while the customer address is located..");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Step givePackageToCustomer() {
		return this.stepBuilderFactory.get("givePackageToCustomer").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Given the package to the customer");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Step  driveToAddressStep() {
		return this.stepBuilderFactory.get("driveToAddressStep").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//				if(true) {
//					throw new RuntimeException("Step running failed....");
//				}
				System.out.println("Successfully arrived at the address..");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Step packageItemStep() {
		return this.stepBuilderFactory.get("packageItemStep").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				String item = chunkContext.getStepContext().getJobParameters().get("item").toString();
				String date = chunkContext.getStepContext().getJobParameters().get("run.date").toString();

				
				System.out.println(String.format("The %s has been shipped on %s", item,date));
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
//	-------- EXTERNAL FLOW for reuse in flowers & delivery job start ------------ 
	
	@Bean
	public Flow deliveryFlow() {
		return new FlowBuilder<SimpleFlow>("deliveyFlow")
				.start(driveToAddressStep())
				.on("FAILED").fail()
				.from(driveToAddressStep())
					.on("*").to(decider())
						.on("PRESENT").to(givePackageToCustomer())
							.on("*").to(customerDecider())
								.on("HAPPY").to(thankYouStep())
							.from(customerDecider())
								.on("NOT_HAPPY").to(giveRefundStep())
					    .from(decider())
						.on("NOT_PRESENT").to(leaveAtStepDoor())
						.build();
	}
	
	
//	-------- EXTERNAL FLOW for reuse in flowers & delivery job end ------------ 

	
	
//	-------- Flowers related code start -----------------------
	
	@Bean
	public StepExecutionListener selectFlowerListner() {
		return new FlowerSelectionStepExecutionListner();
	}
	
	@Bean
	public Step selectFlowersStep() {
		return this.stepBuilderFactory.get("selectFlowersStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Gathering flowers for order");
				return RepeatStatus.FINISHED;
			}
		}).listener(selectFlowerListner()).build();
	}
	
	@Bean
	public Step arrangeFlowersStep() {
		return this.stepBuilderFactory.get("arrangeFlowersStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Arranging flowers for order");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Step removeThornSteps() {
		return this.stepBuilderFactory.get("removeThornSteps").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Remove thrones from roses. ");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Job prepareFlowers() {
		return this.jobBuilderFactory.get("prepareFlowersJob")
										.start(selectFlowersStep())
										.on("TRIM_REQUIRED").to(removeThornSteps()).next(arrangeFlowersStep())
										.from(selectFlowersStep())
										.on("NO_TRIM_REQUIRED").to(arrangeFlowersStep())
										.from(arrangeFlowersStep()).on("*").to(deliveryFlow())
										.end()
										.build();
	}
	
	
	
//	-------- Flowers related code end -------------------
	
	
//	-------- Billing job code started -------------------
	
	@Bean
	public Flow billingFlow() {
		return new FlowBuilder<SimpleFlow>("billingFlow")
										.start(sendInvoiceStep())
										.build();
	}
	
	@Bean
	public Step nestedBillingJobStep() {
		return this.stepBuilderFactory.get("nestedBillingJobStep").job(billingJob()).build();
	}
	
	@Bean
	public Step sendInvoiceStep() {
		return this.stepBuilderFactory.get("invoiceStep").tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Invoice is sent to the customer");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Job billingJob() {
		return this.jobBuilderFactory.get("billingJob")
										.start(sendInvoiceStep())
										.build();
	}
	
//	-------- Billing job code ended ---------------------
	
	
	@Bean
	public Job deliveryPackageJob() {
		return this.jobBuilderFactory.get("deliveryPackageJob")
				.start(packageItemStep())
//				.on("*").to(deliveryFlow())
//				.next(nestedBillingJobStep())
				.split(new SimpleAsyncTaskExecutor())
				.add(deliveryFlow(), billingFlow())
				.end()
				.build();
	}


	public static void main(String[] args) {
		SpringApplication.run(ListnersExternalflowNestedjobsParallelflowApplication.class, args);
	}

}
