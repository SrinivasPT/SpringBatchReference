package com.example.demo.batch;

import com.example.demo.model.Address;
import com.example.demo.model.EmployeeFlatRecord;
import com.example.demo.model.EmployeeJson;
import com.example.demo.model.EmployeeJsonUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

@EnableBatchProcessing
@Configuration
public class BatchJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    public BatchJobConfig(JobBuilderFactory jobBuilderFactory,
        StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    // This method now requires segment information to filter data
    @Bean
    @StepScope
    public JdbcCursorItemReader<EmployeeFlatRecord> itemReader(
        @Value("#{stepExecutionContext['segment']}") Integer segment) {
        return new JdbcCursorItemReaderBuilder<EmployeeFlatRecord>()
            .name("customerReader")
            .dataSource(dataSource)
            .sql("SELECT * FROM VW_EmployeeRecord WHERE segment = ?")
            .rowMapper(new BeanPropertyRowMapper<>(EmployeeFlatRecord.class))
            .fetchSize(1000)
            .preparedStatementSetter(new ArgumentPreparedStatementSetter(new Object[]{segment}))
            .build();
    }


    @Bean
    public ItemProcessor<EmployeeFlatRecord, EmployeeJsonUpdate> itemProcessor() {
        ObjectMapper objectMapper = new ObjectMapper();

        return record -> {
            try {
                // Direct mappings are handled here; assume this part is automated or manually handled
                EmployeeJson employeeJson = new EmployeeJson();
                employeeJson.setId(record.getId());
                employeeJson.setFirstName(record.getFirstName());
                employeeJson.setLastName(record.getLastName());
                employeeJson.setAge(record.getAge());
                employeeJson.setDateOfBirth(record.getDateOfBirth());

                // Custom logic for addresses
                List<Address> addresses = new ArrayList<>();
                if (record.getCurTypeCode()
                    != null) { // Assuming non-null type code implies a valid address
                    addresses.add(new Address(record.getCurTypeCode(), record.getCurLineOne(),
                        record.getCurLineTwo(), record.getCurZip()));
                }
                if (record.getPerTypeCode() != null) {
                    addresses.add(new Address(record.getPerTypeCode(), record.getPerLineOne(),
                        record.getPerLineTwo(), record.getPerZip()));
                }
                employeeJson.setAddresses(addresses);

                // Serialize to JSON
                String json = objectMapper.writeValueAsString(employeeJson);
                return new EmployeeJsonUpdate(record.getId(), json);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        };
    }


    @Bean
    public ItemWriter<EmployeeJsonUpdate> itemWriter() {
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        return records -> {
            if (records.isEmpty()) {
                return;
            }

            String sql = "UPDATE Employee SET JsonString = :jsonString WHERE Id = :id";

            // Initialize an array of SqlParameterSource based on the size of the records
            SqlParameterSource[] batchParams = new SqlParameterSource[records.size()];

            // Manually populate the array
            for (int i = 0; i < records.size(); i++) {
                EmployeeJsonUpdate record = records.get(i);
                SqlParameterSource paramSource = new MapSqlParameterSource()
                    .addValue("id", record.getId())
                    .addValue("jsonString", record.getJsonString());
                batchParams[i] = paramSource;
            }

            jdbcTemplate.batchUpdate(sql, batchParams);
        };
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
            .<EmployeeFlatRecord, EmployeeJsonUpdate>chunk(1000)
            .reader(itemReader(
                null)) // Pass null because the actual value will be provided by the partitioner
            .processor(itemProcessor())
            .writer(itemWriter())
            .build();
    }

    @Bean
    public Step partitionedMasterStep(TaskExecutor taskExecutor) {
        return stepBuilderFactory.get("partitionedMasterStep")
            .partitioner("step1", partitioner())
            .step(step1())
            .taskExecutor(taskExecutor)
            .build();
    }

    @Bean
    public Partitioner partitioner() {
        return new ColumnRangePartitioner(dataSource);
    }

    @Bean
    public Job importEmployeeJob(Step partitionedMasterStep) {
        return jobBuilderFactory.get("importEmployeeJob")
            .incrementer(new RunIdIncrementer())
            .start(partitionedMasterStep)
            .build(); // No need to call end() after start() for simple job configurations
    }
}
