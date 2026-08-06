package eu.europeana.oaipmh.utils;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.IOException;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

/** Creates a docker container for Solr using the dockerfile in docker-scripts directory */
public class SolrContainer extends GenericContainer<SolrContainer> {

  private static final int DEFAULT_SOLR_PORT = 8983;
  private final String searchCore;
  private final boolean useFixedPorts = false;

  private static final Network network = Network.newNetwork();

  public SolrContainer(String searchCore) {
    this(
        new ImageFromDockerfile()
            // in test/resources directory
            .withFileFromClasspath("Dockerfile", "solr-docker/Dockerfile")
            .withFileFromClasspath("solr-entrypoint.sh", "solr-docker/solr-entrypoint.sh")
            .withFileFromClasspath("conf", "solr-docker/conf/")
            .withFileFromClasspath("accent-map.txt", "solr-docker/conf/accent-map.txt")
            .withFileFromClasspath("schema.xml", "solr-docker/conf/schema.xml")
            .withFileFromClasspath("solrconfig.xml", "solr-docker/conf/solrconfig.xml"),
            searchCore);
  }

  private SolrContainer(ImageFromDockerfile dockerImageName, String searchCore) {
    super(dockerImageName);
    if (useFixedPorts) {
      this.addFixedExposedPort(DEFAULT_SOLR_PORT, DEFAULT_SOLR_PORT);
    } else {
      this.withExposedPorts(DEFAULT_SOLR_PORT);
    }

    this.withEnv("SEARCH_CORE", searchCore);
//    this.withNetwork(network);
//    this.withNetworkAliases("solr");
    this.waitStrategy =
        new LogMessageWaitStrategy()
            .withRegEx(".*o\\.e\\.j\\.s\\.Server Started.*")
            .withStartupTimeout(Duration.of(60, SECONDS));

    this.searchCore = searchCore;
  }

  public String getConnectionUrl() {
    if (!this.isRunning()) {
      throw new IllegalStateException("Solr container should be started first");
    } else {
      return String.format(
              "http://%s:%d/solr/%s",
              this.getContainerIpAddress(),
              this.getMappedPort(DEFAULT_SOLR_PORT),
              searchCore);
    }
  }

  public String getSearchCore() {
    return searchCore;
  }

  public void createCollection() {
      try {
          this.execInContainer(
                  "solr",
                  "create_collection",
                  "-c",
                 searchCore,
                  "-d",
                 searchCore
          );
      } catch (IOException | InterruptedException e) {
          throw new RuntimeException("Could not solr collection : " +searchCore, e);
      }
  }
}