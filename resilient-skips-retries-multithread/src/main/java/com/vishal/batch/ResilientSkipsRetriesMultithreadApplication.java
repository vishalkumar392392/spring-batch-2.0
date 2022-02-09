package com.vishal.batch;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableBatchProcessing
public class ResilientSkipsRetriesMultithreadApplication {


	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private DataSource dataSource;
	
	public static String INSERT_ORDER_SQL = "insert into "
			+ "TRACKED_ORDER(order_id, first_name, last_name, email, item_id, item_name, cost, ship_date, tracking_number, free_shipping)"
			+ " values(:orderId,:firstName,:lastName,:email,:itemId,:itemName,:cost,:shipDate,:trackingNumber, :freeShipping)";
	
	@Bean
	public Step chunkBasedStep() throws Exception {
		return this.stepBuilderFactory.get("chunkBasedStep").<Order, TrackedOrder>chunk(10)
				.reader(itemReader())
				.processor(compositeItemProcessor())
				.faultTolerant()
				.retry(OrderProcessingException.class)
				.retryLimit(5)
				.listener(new CustomRetryListner())
				.writer(itemWriter())
				.taskExecutor(taskExecutor())
				.build();
	}
	
	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
		exec.setCorePoolSize(2);
		exec.setMaxPoolSize(10);
		return exec;
	}
	
	@Bean
	public ItemWriter<TrackedOrder> itemWriter() {
			return new JdbcBatchItemWriterBuilder<TrackedOrder>().dataSource(dataSource)
					.sql(INSERT_ORDER_SQL)
					.beanMapped()
					.build();
	}

	
	@Bean
	public ItemProcessor< Order, TrackedOrder> compositeItemProcessor() {

		return new CompositeItemProcessorBuilder<Order, TrackedOrder>()
				.delegates(orderValidatingProcessor(), trackedOrderItemProcessor(), shippingItemProcessor())
				.build();
	}

	@Bean
	public ItemProcessor<Order,TrackedOrder> trackedOrderItemProcessor() {

		return new TrackedOrderItemProcessor();
	}
	
	@Bean
	public ItemProcessor<TrackedOrder,TrackedOrder> shippingItemProcessor() {

		return new FreeShippingItemProcessor();
	}

	@Bean
	public ItemProcessor<Order,  Order> orderValidatingProcessor() {
		
		BeanValidatingItemProcessor<Order>  itemProcessor = new BeanValidatingItemProcessor<Order>();
		itemProcessor.setFilter(true);
		return itemProcessor;
	}
	

	@Bean
	public ItemReader<Order> itemReader() throws Exception {

		return new JdbcPagingItemReaderBuilder<Order>().dataSource(dataSource).name("jdbcPagingItemReader")
				.queryProvider(queryProvider()).rowMapper(new OrderRowMapper()).pageSize(10).saveState(false).build();
	}

	@Bean
	public PagingQueryProvider queryProvider() throws Exception {

		SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
		factory.setSelectClause("select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date ");
		factory.setFromClause("SHIPPED_ORDER");
		factory.setSortKey("order_id");
		factory.setDataSource(dataSource);
		return factory.getObject();
	}

	@Bean
	public Job job() throws Exception {

		return this.jobBuilderFactory.get("job").start(chunkBasedStep()).build();
	}


	
	public static void main(String[] args) {
		SpringApplication.run(ResilientSkipsRetriesMultithreadApplication.class, args);
	}

}
