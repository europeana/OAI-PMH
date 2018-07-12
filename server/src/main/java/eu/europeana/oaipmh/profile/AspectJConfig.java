package eu.europeana.oaipmh.profile;

import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

@Configuration
@ConditionalOnExpression("${profiling.enabled:false}")
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
public class AspectJConfig {
    @Pointcut("execution(* eu.europeana.oaipmh.service.*.*(..))")
    public void allServiceMethods() {}

    @Pointcut("execution(* eu.europeana.oaipmh.service.SearchApi.*(..))")
    public void allIdentifierProviderMethods() {}

    @Pointcut("execution(* eu.europeana.oaipmh.service.DBRecordProvider.*(..))")
    public void allRecordProviderMethods() {}

    @Pointcut("@annotation(eu.europeana.oaipmh.profile.TrackTime)")
    public void allTrackTimeAnnotatedMethods() {}
}
