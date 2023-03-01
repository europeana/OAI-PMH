package eu.europeana.oaipmh.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:oai-pmh-client.properties")
public class OAIPMHClient implements CommandLineRunner {
    @Autowired
    private OAIPMHServiceClient oaipmhServiceClient;

    @Override
    public void run(String... args) throws Exception {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Please specify a verb as first argument (e.g. ListIdentifiers, ListRecords)");
        }
        oaipmhServiceClient.execute(args[0]);
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder().sources(OAIPMHClient.class).web(WebApplicationType.NONE).run(args);
    }
}
