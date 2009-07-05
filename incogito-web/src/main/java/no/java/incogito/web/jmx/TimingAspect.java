package no.java.incogito.web.jmx;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class TimingAspect {

    private final ThreadLocal<Long> state = new ThreadLocal<Long>();

    private final ApplicationPerformanceRecorder recorder;

    @Autowired
    public TimingAspect(ApplicationPerformanceRecorder recorder) {
        this.recorder = recorder;
    }

    public void before(JoinPoint joinPoint) {
        state.set(System.currentTimeMillis());
    }

    public void afterReturning(JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String method = signature.toShortString();

        long start = state.get();
        recorder.addOkMeasurement(method, System.currentTimeMillis() - start);
    }

    public void afterThrowing(JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String method = signature.toShortString();

        long start = state.get();
        recorder.addExceptionMeasurement(method, System.currentTimeMillis() - start);
    }
}
