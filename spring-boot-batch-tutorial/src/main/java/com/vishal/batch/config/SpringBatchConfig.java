package com.vishal.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.vishal.batch.entity.User;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

	@Bean
	public Job job(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,Step step,
			ItemReader<User> itemReader, ItemProcessor<User, User> itemProcessor, ItemWriter<User> itemWriter) {
		return jobBuilderFactory.get("ETL-JOB").incrementer(new RunIdIncrementer()).start(step).build();
	}

	@Bean
	public Step step(StepBuilderFactory stepBuilderFactory, ItemReader<User> itemReader,
			ItemProcessor<User, User> itemProcessor, ItemWriter<User> itemWriter) {
		Step step = stepBuilderFactory.get("ETL-STEP").<User, User>chunk(3).reader(itemReader).processor(itemProcessor)
				.writer(itemWriter).build();
		return step;
	}

	@Bean
	public FlatFileItemReader<User> fileItemReader(@Value("${input}") String path) {
		FlatFileItemReader<User> flatFileItemReader = new FlatFileItemReader<User>();
		flatFileItemReader.setLinesToSkip(1);
		flatFileItemReader.setResource(new FileSystemResource(path));
		flatFileItemReader.setName("itemreader");
		flatFileItemReader.setLineMapper(lineMapper());
		return flatFileItemReader;

	}

	@Bean
	public LineMapper<User> lineMapper() {

		DefaultLineMapper<User> lineMapper = new DefaultLineMapper<User>();
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setDelimiter(",");
		tokenizer.setStrict(false);
		tokenizer.setNames("name", "dept", "salary");
		lineMapper.setLineTokenizer(tokenizer);
		BeanWrapperFieldSetMapper<User> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(User.class);
		lineMapper.setFieldSetMapper(fieldSetMapper);

		return lineMapper;
	}
}
