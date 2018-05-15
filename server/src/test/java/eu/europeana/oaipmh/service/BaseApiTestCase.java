package eu.europeana.oaipmh.service;

import org.junit.BeforeClass;

import java.nio.file.Paths;

public class BaseApiTestCase {
    private static final String LINUX_SEPARATOR = "/";

    private static final String WINDOWS_SEPARATOR = "\\";

    protected static String resDir;

    @BeforeClass
    public static void init() {
        resDir = Paths.get(Paths.get(".").toAbsolutePath().normalize().toString(), "src/test/resources").toAbsolutePath().normalize().toString().replace(WINDOWS_SEPARATOR, LINUX_SEPARATOR);
    }
}
