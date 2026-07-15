package eu.europeana.oaipmh.profile;

//import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

/**
 * Configuration class for enabling AspectJ-based weaving and defining
 * pointcuts used for method-level profiling.
 *
 * This class is only active if the `profiling.enabled` property is set to `true`.
 * It enables load-time weaving via AspectJ and defines several reusable pointcuts
 * for profiling selected sets of methods or annotated methods.
 *
 */
@Configuration
@ConditionalOnProperty(prefix="profiling", name="enabled", havingValue = "true")
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
public class AspectJConfig {
//    @Pointcut("execution(* eu.europeana.oaipmh.service.*.*(..))")
//    public void allServiceMethods() {}
//
//    @Pointcut("execution(* eu.europeana.oaipmh.service.SearchApi.*(..))")
//    public void allIdentifierProviderMethods() {}
//
//    @Pointcut("execution(* eu.europeana.oaipmh.service.DBRecordProvider.*(..))")
//    public void allRecordProviderMethods() {}
//
//    @Pointcut("@annotation(eu.europeana.oaipmh.profile.TrackTime)")
//    public void allTrackTimeAnnotatedMethods() {}
}
