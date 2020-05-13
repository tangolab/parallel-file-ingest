package my.foo.batch;

import java.io.FileNotFoundException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import my.foo.batch.domain.Person;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
	/**
	 *
	 */
	private static final String WRITER_SQL = "INSERT INTO people (firstName, lastName) VALUES (:firstName, :lastName)";
	private final DataSource dataSource;
	private final ResourceLoader resourceLoader;
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchConfiguration.class);

	@Autowired
	private ApplicationArguments applicationArguments;
	
	@Autowired
	public BatchConfiguration(final DataSource dataSource, final JobBuilderFactory jobBuilderFactory,
			final StepBuilderFactory stepBuilderFactory, final ResourceLoader resourceLoader) {
		this.dataSource = dataSource;
		this.resourceLoader = resourceLoader;
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
		LOGGER.info("In BatchConfiguration");
	}

	// JobCompletionNotificationListener (File loader)
	@Bean
	public JobExecutionListener listener() {
		return new JobCompletionListener();
	}

	@Bean
	@StepScope
	public ItemStreamReader<Person> reader(@Value("#{jobParameters['localFilePath']}") String filePath)
			throws Exception, FileNotFoundException {

		LOGGER.info("In ItemStreamReader setup");

		if (filePath == null) {
			throw new Exception("localFilePath must be presented as an argument");
		} else if (!filePath.matches("[a-z]+:.*")) {
			filePath = "file:" + filePath;
		}
		if (!resourceLoader.getResource(filePath).exists()) {
			throw new FileNotFoundException(filePath);
		}
		return new FlatFileItemReaderBuilder<Person>().name("reader").resource(resourceLoader.getResource(filePath))
				.delimited().names(new String[] { "firstName", "lastName" }).fieldSetMapper(new PersonFieldSetMapper())
				.build();
	}

	@Bean
	public ItemProcessor<Person, Person> processor() {
		LOGGER.info("In ItemProcessor setup");
		return new PersonItemProcessor();
	}

	@Bean
	public ItemWriter<Person> writer() {
		LOGGER.info("In ItemWriter");
		return new JdbcBatchItemWriterBuilder<Person>().beanMapped().dataSource(this.dataSource).sql(WRITER_SQL)
				.build();
	}

	@Bean
	public Job ingestJob() throws FileNotFoundException, Exception {
		LOGGER.info("In ingestJob setup");
		return jobBuilderFactory.get("ingestJob").incrementer(new RunIdIncrementer()).listener(listener()).flow(step1()).end().build();
	}

	@Bean
	public TaskExecutor taskExecutor() {
		SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor("spring_batch");
		asyncTaskExecutor.setConcurrencyLimit(20);
		return asyncTaskExecutor;
	}

	@Bean
	public Step step1() throws Exception {
		String[] sourceArgs = applicationArguments.getSourceArgs();
		LOGGER.info("In step1");
		if (sourceArgs[1].equals("-p")) {
			return stepBuilderFactory.get("ingest").<Person, Person>chunk(1000).reader(reader(null))
					.processor(processor()).writer(writer()).taskExecutor(taskExecutor()).build();
		} else {
			return stepBuilderFactory.get("ingest").<Person, Person>chunk(1000).reader(reader(null))
					.processor(processor()).writer(writer()).build();
		}
	}
}