package com.vishal.batch;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class JobConfiguration {
	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	
	@Bean
	public StaxEventItemReader<Employee> reader() {

		StaxEventItemReader<Employee> staxEventItemReader = new StaxEventItemReader<>();

		Map<String, Class> aliases = new HashMap<>();
		aliases.put("employee", Employee.class);

		XStreamMarshaller unMarshaller = new XStreamMarshaller();
		unMarshaller.setAliases(aliases);

		staxEventItemReader.setResource(new ClassPathResource("/xml/emps.xml"));
		staxEventItemReader.setFragmentRootElementName("employee");
		staxEventItemReader.setUnmarshaller(unMarshaller);

		return staxEventItemReader;
	}
	
	@Bean
	public Step step1(ItemReader<Employee>itemReader,ItemWriter<Employee> itemWriter,ItemProcessor<Employee, Employee> itemProcessor) {
		return this.stepBuilderFactory.get("step1").<Employee,Employee>chunk(5).reader(itemReader).processor(itemProcessor)
				
//				.writer(emps -> {
//			for (Object emp : emps) {
//				System.out.println(emp);
//			}
//		})
				.writer(itemWriter)
				.build();
	}

	@Bean
	public Job myJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,Step step) {

		return jobBuilderFactory.get("My-First-Job").incrementer(new RunIdIncrementer()).start(step).build();
	}

}
