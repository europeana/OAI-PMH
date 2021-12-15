package eu.europeana.oaipmh.profile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.StopWatch;
import org.springframework.util.StopWatch.TaskInfo;

/**
 * Profiler to log process time.
 */
@Aspect
public class SimpleProfiler {

    private static final Logger LOG = LogManager.getLogger(SimpleProfiler.class);

    /**
     * Profiling method. Use pointcuts defined in AspectJConfig. The default is @Around("eu.europeana.oaipmh.profile.AspectJConfig.allServiceMethods()").
     * For other possibilities check the <code>{@link AspectJConfig}</code> class.
     *
     **/
    @Around("eu.europeana.oaipmh.profile.AspectJConfig.allServiceMethods()")
    public Object profile(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(proceedingJoinPoint.toShortString());
        boolean isExceptionThrown = false;
        try {
            return proceedingJoinPoint.proceed();
        } catch (RuntimeException e) {
            isExceptionThrown = true;
            throw e;
        } finally {
            stopWatch.stop();
            TaskInfo taskInfo = stopWatch.getLastTaskInfo();
            String profileMessage = taskInfo.getTaskName() + ":\t" + taskInfo.getTimeMillis() + "\tms" +
                    (isExceptionThrown ? " (thrown Exception)" : "");
            LOG.debug("Profiling " + profileMessage);
        }
    }
}