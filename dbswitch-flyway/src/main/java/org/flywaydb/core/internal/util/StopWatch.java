package org.flywaydb.core.internal.util;

import java.util.concurrent.TimeUnit;

public class StopWatch {
        private long start;

        private long stop;

        public void start() {
        start = nanoTime();
    }

        public void stop() {
        stop = nanoTime();
    }

    private long nanoTime() {
        return System.nanoTime();
    }

        public long getTotalTimeMillis() {
        long duration = stop - start;
        return TimeUnit.NANOSECONDS.toMillis(duration);
    }
}