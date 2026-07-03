package eu.europeana.metis.mongo.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import com.mongodb.client.MongoClient;

import dev.morphia.query.FindOptions;
import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;
import dev.morphia.Datastore;
import dev.morphia.mapping.MappingException;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.record.api.WebMetaInfo;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordDaoNew extends RecordDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordDao.class);

    public RecordDaoNew(MongoClient mongoClient, String dbName) {
        super(mongoClient, dbName);
    }

    public RecordDaoNew(MongoClient mongoClient, String dbName, boolean createIndexes) {
        super(mongoClient, dbName, createIndexes);
      }
    

    public Optional<FullBean> getRecord(String id) throws EuropeanaException {
        return getRecords(Filters.eq("about", id), new FindOptions()).findFirst();
    }

    public boolean hasRecord(String id) throws EuropeanaException {
        try {
            return (getDatastore().find(FullBeanImpl.class)
                                  .filter(Filters.eq("about", id))
                                  .count() > 0);
        }
        catch (RuntimeException re) { throw processException(re); }
    }

    public Stream<FullBean> getRecords(String... ids) throws EuropeanaException {
        return getRecords(Arrays.asList(ids));
    }

    public Stream<FullBean> getRecords(Collection<String> ids) throws EuropeanaException {
        FindOptions opts = new FindOptions().batchSize(ids.size());
        return getRecords(Filters.in("about", ids), opts);
    }

    public Stream<FullBean> getRecords(Filter filter, FindOptions opts) throws EuropeanaException {
        try {
            return getDatastore().find(FullBeanImpl.class).filter(filter)
                                 .stream(opts).map(r -> injectWebMeta(r) );
        }
        catch (RuntimeException re) { throw processException(re); }
    }

    protected FullBean injectWebMeta(FullBean bean) {
        WebMetaInfo.injectWebMetaInfoBatch(bean, this, null);
        return bean;
    }

    protected EuropeanaException processException(RuntimeException re) {
        if (re.getCause() != null && (re.getCause() instanceof MappingException 
                                   || re.getCause() instanceof ClassCastException)) {
            return new MongoDBException(ProblemType.RECORD_RETRIEVAL_ERROR, re);
        } else {
            return new MongoRuntimeException(ProblemType.MONGO_UNREACHABLE, re);
        }
    }
}
