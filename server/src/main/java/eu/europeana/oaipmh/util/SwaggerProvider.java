/*
 * Copyright 2007-2019 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */

package eu.europeana.oaipmh.util;

import eu.europeana.oaipmh.web.VerbController;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by luthien on 19/03/2019.
 */

@Service
public class SwaggerProvider {

    @Value("${swaggerHost}")
    private String swaggerHost;

    @Value("${swaggerBasePath}")
    private String swaggerBasePath;

    @Value("${appVersion}")
    private String appVersion;


    private static final String ANY           = "*";
    private static final String ALLOWED       = "GET, HEAD";
    private static final String ALLOWHEADERS  = "If-Match, If-None-Match, If-Modified-Since";
    private static final String EXPOSEHEADERS = "Allow, ETag, Last-Modified, Link";
    private static final Logger LOG           = LogManager.getLogger(VerbController.class);

    private static final String HOST2BE       = "@@HOST@@";
    private static final String BASEPATH2BE   = "@@BASEPATH@@";
    private static final String VERSION2BE    = "@@VERSION@@";

    private String apiDocs;

    @PostConstruct
    private void init() {
        // done here, because properties aren't available until after the construction of the bean
        File file;
        try {
            file = ResourceUtils.getFile("classpath:api-docs.json");
            apiDocs = new String(Files.readAllBytes(file.toPath()));
        } catch (FileNotFoundException e) {
            LOG.error("Static Swagger config file not found: " + e.getMessage());
        } catch (IOException e) {
            LOG.error("Error reading static Swagger config file: " + e.getMessage());
        }
        if (StringUtils.isNotBlank(swaggerHost)){
            apiDocs = StringUtils.replace(apiDocs, HOST2BE, swaggerHost);
        }
        if (StringUtils.isNotBlank(swaggerBasePath)){
            apiDocs = StringUtils.replace(apiDocs, BASEPATH2BE, swaggerBasePath);
        }
        if (StringUtils.isNotBlank(appVersion)){
            apiDocs = StringUtils.replace(apiDocs, VERSION2BE, appVersion);
        }
        LOG.info("Swagger api-docs initialised");
    }

    public String getApiDocs() {
        return apiDocs;
    }

    public HttpHeaders generateSwaggerHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", ANY);
        headers.add("Access-Control-Allow-Methods", ALLOWED);
        headers.add("Access-Control-Allow-Headers", ALLOWHEADERS);
        headers.add("Access-Control-Expose-Headers", EXPOSEHEADERS);
        headers.add("Access-Control-Max-Age", "600");
        return headers;
    }
}
