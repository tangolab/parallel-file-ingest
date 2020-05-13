package my.foo.batch;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
  
/**
 *
 */
public class JobCompletionListener extends JobExecutionListenerSupport {
 
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonItemProcessor.class);

    @Autowired
	private JobControlReport jobControlReport;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            LOGGER.info("Writing Control Report");
            Path path = Paths.get("Control Report.csv");
            try (BufferedWriter fileWriter = Files.newBufferedWriter(path)) {
                fileWriter.newLine();
                fileWriter.write("Processed - " + jobControlReport.getCounter() + " records");
                fileWriter.newLine();

            } catch (Exception e) {
                LOGGER.error("Fetal error: error occurred while writing {} file", path.getFileName());
            }
        }
    }
}