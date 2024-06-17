# OAI-PMH
Europeana OAI-PMH server and test client applications (based on Spring-Boot)

## Server
To use the server, fill in the missing properties (wskey, mongo, solr and socks) in the `oai-pmh.properties` file. 
Alternatively you can specify those properties in a separate `oai-pmh.user.properties` file. The latter is safer because
that file is in .gitignore so you don't accidentally commit the database credentials.

You can set up the server to use either the Record API (with the `recordProviderClass=eu.europeana.oaipmh.service.RecordApi`
setting) or retrieve data directly from a Mongo database (`recordProviderClass=eu.europeana.oaipmh.service.DBRecordProvider`)
However, the default is using Mongo database and using Record API is not officially supported.

## Client
The client application was designed to test the OAI-PMH server, so not to have a rich oai-pmh client application 
for harvesting. To use the client, start it with the OAI-PMH verb of the operation you'd like to do as a parameter (e.g
listIdentifiers, listRecords, identify etc.). Other request parameters like set, from and until should be placed in the
`oai-pmh-client.properties` file.

## Build
``mvn clean install`` (add ``-DskipTests``) to skip the unit tests during build

## Server deployment
1. Generate a Docker image using the project's [Dockerfile](Dockerfile)

2. Configure the application by generating a `oai-pmh.user.properties` file and placing this in the 
[k8s](k8s) folder. After deployment this file will override the settings specified in the `oai-pmh.properties` file
located in the [server/src/main/resources](server/src/main/resources) folder. The .gitignore file makes sure the .user.properties file
is never committed.

3. Configure the deployment by setting the proper environment variables specified in the configuration template files
in the [k8s](k8s) folder

4. Deploy to Kubernetes infrastructure
