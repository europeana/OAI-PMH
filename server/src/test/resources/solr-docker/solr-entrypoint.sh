#!/bin/bash

# Please note, this file should have unix-based end of line
# in case of errors use notepad++ to change EOL to unix LF (use EDIT -> EOL Conversion)

set -e

# Create new configset based off default one
cp -r /opt/solr/server/solr/configsets/_default/ /opt/solr/server/solr/configsets/"$SEARCH_CORE"/

# Overwrite Files copied to /opt/oaipmh-conf in Dockerfile
mv /opt/oaipmh-conf/* /opt/solr/server/solr/configsets/"$SEARCH_CORE"/conf/

# Set access rights for the configset
chown -R solr:solr /opt/solr/server/solr/configsets/"$SEARCH_CORE"

precreate-core "$SEARCH_CORE" /opt/solr/server/solr/configsets/"$SEARCH_CORE"

# Set access rights for entity-management core data
chown -R solr:solr /opt/solr/server/solr/mycores/"$SEARCH_CORE"/

## drop access to solr and run cmd
exec gosu solr "$@"


