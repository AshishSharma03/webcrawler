package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

    private final Clock clock;
    private final Object target;
    private final ProfilingState state;

    ProfilingMethodInterceptor(Clock clock, Object target, ProfilingState state) {
        this.clock = Objects.requireNonNull(clock);
        this.target = Objects.requireNonNull(target);
        this.state = Objects.requireNonNull(state);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Handle Object.equals() - delegate to the wrapped object
        if (method.getName().equals("equals") && method.getParameterCount() == 1) {
            return target.equals(args[0]);
        }

        // Handle Object.hashCode() - delegate to the wrapped object
        if (method.getName().equals("hashCode") && method.getParameterCount() == 0) {
            return target.hashCode();
        }

        // Check if method is annotated with @Profiled
        boolean isProfiled = method.isAnnotationPresent(Profiled.class);

        if (isProfiled) {
            // Record start time
            Instant startTime = clock.instant();

            try {
                // Invoke the actual method
                Object result = method.invoke(target, args);

                // Record the duration (success case)
                Instant endTime = clock.instant();
                Duration elapsed = Duration.between(startTime, endTime);
                state.record(target.getClass(), method, elapsed);

                return result;

            } catch (InvocationTargetException ex) {
                // Record the duration even if method threw an exception
                Instant endTime = clock.instant();
                Duration elapsed = Duration.between(startTime, endTime);
                state.record(target.getClass(), method, elapsed);

                // Unwrap and re-throw the original exception to avoid UndeclaredThrowableException
                throw ex.getCause();

            } catch (IllegalAccessException ex) {
                // This shouldn't happen if proxy is set up correctly, but handle it
                Instant endTime = clock.instant();
                Duration elapsed = Duration.between(startTime, endTime);
                state.record(target.getClass(), method, elapsed);

                throw new RuntimeException("Unexpected access exception", ex);
            }

        } else {
            // Method is not profiled, just invoke it normally
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException ex) {
                // Unwrap and re-throw the original exception
                throw ex.getCause();
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Unexpected access exception", ex);
            }
        }
    }
}