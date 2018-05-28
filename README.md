# OAI-PMH2
Europeana OAI-PMH client and server applications (based on Spring-Boot)

## Server
To use the server, fill in the missing properties (wskey, mongo, solr and socks) in the `oai-pmh.properties` file. 
Alternatively you can specify those properties in a separate `oai-pmh.user.properties` file. The latter is safer because
that file is in .gitignore so you don't accidentally commit the properties.

You can set up the server to use either the Record API (with the `recordProviderClass=eu.europeana.oaipmh.service.RecordApi`
setting) or retrieve data directly from a Mongo database (`recordProviderClass=eu.europeana.oaipmh.service.DBRecordProvider`)

## Client
The client application was designed to test the OAI-PMH server, so not to have a rich oai-pmh client application 
for harvesting. To use the client, start it with the OAI-PMH verb of the operation you'd like to do as a parameter (e.g
listIdentifiers, listRecords, identify etc.). Other request parameters like set, from and until should be placed in the
`oai-pmh-client.properties` file.


