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
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;

@SpringBootApplication
@EnableBatchProcessing
public class ResilientSkipsRetriesApplication {
	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private DataSource dataSource;
	
	public static String INSERT_ORDER_SQL = "insert into "
			+ "SHIPPED_ORDER_OUTPUT(order_id, first_name, last_name, email, item_id, item_name, cost, ship_date)"
			+ " values(?,?,?,?,?,?,?,?)";
	
	@Bean
	public Step chunkBasedStep() throws Exception {
		return this.stepBuilderFactory.get("chunkBasedStep").<Order, TrackedOrder>chunk(10)
				.reader(itemReader())
				.processor(compositeItemProcessor())
				.faultTolerant()
//				.skip(OrderProcessingException.class)
//				.skipLimit(5)
//				.listener(new CustomSkipListner())
				.retry(OrderProcessingException.class)
				.retryLimit(5)
				.listener(new CustomRetryListner())
				.writer(itemWriter()).build();
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
	public ItemWriter< TrackedOrder> itemWriter() {

		return new JsonFileItemWriterBuilder<TrackedOrder>()
				.jsonObjectMarshaller(new JacksonJsonObjectMarshaller<TrackedOrder>())
				.resource(new FileSystemResource("orders.json"))
				.name("jsonItemWriter")
				.build();
				
	}

	@Bean
	public ItemReader<Order> itemReader() throws Exception {

		return new JdbcPagingItemReaderBuilder<Order>().dataSource(dataSource).name("jdbcPagingItemReader")
				.queryProvider(queryProvider()).rowMapper(new OrderRowMapper()).pageSize(10).build();
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
		SpringApplication.run(ResilientSkipsRetriesApplication.class, args);
	}

}
