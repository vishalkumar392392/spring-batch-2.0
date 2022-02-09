package com.vishal.batch;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;

@SpringBootApplication
@EnableBatchProcessing
public class WritingToCsvApplication {
	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private DataSource dataSource;

	public static String ORDER_SQL = "select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date "
			+ "from SHIPPED_ORDER order by order_id";
	
	public static String[] names = new String[] { "orderId", "firstName", "lastName", "email", "cost", "itemId",
			"itemName", "shipDate" };

	@Bean
	public Step chunkBasedStep() throws Exception {
		return this.stepBuilderFactory.get("chunkBasedStep").<Order, Order>chunk(3).reader(itemReader())
				.writer(itemWriter()).build();
	}
	
	@Bean
	public ItemWriter< Order> itemWriter() {

		FlatFileItemWriter<Order> itemWriter = new FlatFileItemWriter<Order>();

		itemWriter.setResource(new FileSystemResource("shipped_orders_output.csv"));

		DelimitedLineAggregator<Order> aggregator = new DelimitedLineAggregator<Order>();
		aggregator.setDelimiter(",");

		BeanWrapperFieldExtractor<Order> fieldExtractor = new BeanWrapperFieldExtractor<Order>();
		fieldExtractor.setNames(names);
		aggregator.setFieldExtractor(fieldExtractor);

		itemWriter.setLineAggregator(aggregator);
		return itemWriter;
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
		SpringApplication.run(WritingToCsvApplication.class, args);
	}

}
