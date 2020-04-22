package spring.batch.jobs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.dsl.Files;

import java.io.File;

import static org.springframework.integration.dsl.Pollers.fixedDelay;

@Configuration
@EnableIntegration
public class JobOneConfig {


    /**
     * When this flow is included as a child context in a modular batch job config it throws an exception
     * `NoSuchBeanDefinitionException: No bean named 'javaDslInChildContextFlow.channel#0' available`
     * @return
     */
    @Bean
    public IntegrationFlow javaDslInChildContextFlow() {
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
