package eu.europeana.oaipmh.utils;


import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

/** This class creates a Mongo container using the dockerfile in the docker-scripts directory. */
public class MongoContainer extends GenericContainer<MongoContainer> {

    private final String recordDb;
    private final String adminUsername = "admin_user";
    private final String adminPassword = "admin_password";


    private final boolean useFixedPorts = false;
    private int defaultMongoPort = 27017;

    /**
     * Creates a new Mongo container instance
     *
     * @param recordDb record database
     */
    public MongoContainer(String recordDb) {
        this(
                new ImageFromDockerfile()
                        // in test/resources directory
                        .withFileFromClasspath("Dockerfile", "mongo-docker/Dockerfile")
                        .withFileFromClasspath("init-mongo.sh", "mongo-docker/init-mongo.sh"),
                recordDb);
    }

    private MongoContainer(
            ImageFromDockerfile dockerImageName, String recordDb) {
        super(dockerImageName);

        if (useFixedPorts) {
            this.addFixedExposedPort(27018, defaultMongoPort);
        } else {
            this.withExposedPorts(defaultMongoPort);
        }

        this.withEnv("MONGO_INITDB_ROOT_USERNAME", adminUsername);
        this.withEnv("MONGO_INITDB_ROOT_PASSWORD", adminPassword);
        this.withEnv("MONGO_INITDB_DATABASE", recordDb);

        this.waitingFor(Wait.forLogMessage("(?i).*Waiting for connections.*", 1));
        this.recordDb = recordDb;
    }

    public String getConnectionUrl() {
        if (!this.isRunning()) {
            throw new IllegalStateException("MongoDBContainer should be started first");
        } else {
            //"oaipmh-it"
            String connectionUrl = String.format(
                    "mongodb://%s:%s@%s:%d/%s?authSource=admin&ssl=false",
                    adminUsername, adminPassword, this.getHost(), this.getMappedPort(defaultMongoPort), this.getRecordDb());
            return connectionUrl;
        }

    }

    public String getRecordDb() {
        return recordDb;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

}

