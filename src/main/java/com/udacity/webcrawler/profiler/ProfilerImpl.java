package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

    private final Clock clock;
    private final ProfilingState state = new ProfilingState();
    private final ZonedDateTime startTime;

    @Inject
    ProfilerImpl(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
        this.startTime = ZonedDateTime.now(clock);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T wrap(Class<T> klass, T delegate) {
        Objects.requireNonNull(klass);

        // Check if the class has any @Profiled methods
        boolean hasProfiledMethods = false;
        for (java.lang.reflect.Method method : klass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Profiled.class)) {
                hasProfiledMethods = true;
                break;
            }
        }

        // If no @Profiled methods, throw IllegalArgumentException as required by tests
        if (!hasProfiledMethods) {
            throw new IllegalArgumentException("Interface does not contain any @Profiled methods");
        }

        // Create a dynamic proxy with the ProfilingMethodInterceptor
        ProfilingMethodInterceptor interceptor = new ProfilingMethodInterceptor(clock, delegate, state);

        return (T) Proxy.newProxyInstance(
                klass.getClassLoader(),
                new Class<?>[]{klass},
                interceptor);
    }

    @Override
    public void writeData(Path path) {
        Objects.requireNonNull(path);

        try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writeData(writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write profiling data to " + path, e);
        }
    }

    @Override
    public void writeData(Writer writer) throws IOException {
        writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
        writer.write(System.lineSeparator());
        state.write(writer);
        writer.write(System.lineSeparator());
    }
}