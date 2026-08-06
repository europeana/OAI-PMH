# Please note, this file should have unix-based end of line
# Use same version as CloudFoundry deployment
#FROM library/mongo:4.4.15
FROM library/mongo:6.0.14-jammy

EXPOSE 27017

#redundant, set programatically
#ENV MONGO_INITDB_ROOT_USERNAME admin_user
#ENV MONGO_INITDB_ROOT_PASSWORD admin_password

#scripts need to be fixed, but anyway redundant as Morphia creates the database
#COPY init-mongo.sh /docker-entrypoint-initdb.d/init-mongo.sh
#COPY init-mongo.sh /docker-entrypoint-initdb.d/init-annotation-db.js