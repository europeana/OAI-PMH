package eu.europeana.oaipmh.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:oai-pmh-client.properties")
public class OAIPMH2Client implements CommandLineRunner {
    @Autowired
    private OAIPMHServiceClient oaipmhServiceClient;

    @Override
    public void run(String... args) throws Exception {
        oaipmhServiceClient.execute(args[0]);
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder().sources(OAIPMH2Client.class).web(false).run(args);
    }
}
