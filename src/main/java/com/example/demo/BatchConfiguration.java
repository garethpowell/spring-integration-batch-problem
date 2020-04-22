package com.example.demo;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.dsl.Files;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import spring.batch.jobs.JobOneConfig;

import javax.sql.DataSource;
import java.io.File;

import static org.springframework.integration.dsl.Pollers.fixedDelay;

@Configuration
@EnableIntegration
@EnableBatchProcessing(modular = true)
public class BatchConfiguration {

    @Bean
    public DataSource dataSource(){
        EmbeddedDatabaseBuilder embeddedDatabaseBuilder = new EmbeddedDatabaseBuilder();
        return embeddedDatabaseBuilder.addScript("classpath:org/springframework/batch/core/schema-drop-hsqldb.sql")
                .addScript("classpath:org/springframework/batch/core/schema-hsqldb.sql")
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean
    public ApplicationContextFactory modularJobInChildContext() {
        return new GenericApplicationContextFactory(JobOneConfig.class);
    }


    /**
     * When this flow is included within the parent context in a modular batch job config it works fine.
     * It's identical to the flow that doesn't work.
     */
    @Bean
    public IntegrationFlow javaDslInParentContextFlow() {
        return IntegrationFlows
                .from(
                        Files.inboundAdapter(new File("/tmp/file/in"))
                                .patternFilter("*.xml")
                                .preventDuplicates(true)
                                .autoCreateDirectory(true),
                        configure -> configure.poller(fixedDelay(1000))
                                .autoStartup(true)
                )
                .handle(
                        Files.outboundGateway(new File("/tmp/file/out"))
                                .autoCreateDirectory(true)
                                .deleteSourceFiles(true),
                        endpoint -> endpoint.requiresReply(true)
                )
                .log()
                .get();
    }

}
