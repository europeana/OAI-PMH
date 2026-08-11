// This script creates the record database and allows the given user to authenticate
// The root user is created because of setting the given env variables (e.g. MONGO_INITDB_ROOT_USERNAME), and it is automatically placed in the "admin" db
//see https://github.com/docker-library/docs/blob/master/mongo/README.md#initializing-a-fresh-instance
db.getSiblingDB('$RECORD_DB').createCollection('record');
